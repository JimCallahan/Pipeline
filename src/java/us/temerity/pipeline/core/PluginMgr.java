// $Id: PluginMgr.java,v 1.36 2009/04/01 01:19:27 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.lang.reflect.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.jar.*;
import java.util.zip.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.LogMgr.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The manager of Pipeline plugin classes. <P> 
 */
class PluginMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new plugin manager.
   * 
   * @param bootstrapDir
   *   The user can supply a directory to bootstrap the required plugins feature, 
   *   this should be set to the previous Pipeline root directory.
   */
  public
  PluginMgr
  (
   File bootstrapDir
  ) 
  {
    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [PluginMgr]...");
    LogMgr.getInstance().flush();

    pMakeDirLock = new Object();

    pAdminPrivileges = new AdminPrivileges();

    {
      pPluginLock = new ReentrantReadWriteLock();
      
      pLoadCycleID = 1L;
      
      pEditors           = new PluginCache();  
      pActions           = new PluginCache();  
      pComparators       = new PluginCache();  
      pTools             = new PluginCache();   
      pAnnotations       = new PluginCache();   
      pArchivers         = new PluginCache();  
      pMasterExts        = new PluginCache();  
      pQueueExts         = new PluginCache();  
      pKeyChoosers       = new PluginCache();
      pBuilderCollection = new PluginCache();

      pBuilderCollectionLayouts = 
        new TripleMap<String,String,VersionID,LayoutGroup>();
      
      pAnnotationPermissions = 
        new TripleMap<String,String,VersionID,AnnotationPermissions>();
      
      pAnnotationContexts = 
        new TripleMap<String,String,VersionID,TreeSet<AnnotationContext>>();
      
      pSerialVersionUIDs = new TreeMap<Long,String>(); 
    }

    /* Required plugin related fields. */
    {
      /* The path to the required plugins GLUE files. */
      pRequiredPluginsPath = new Path(PackageInfo.sPluginMgrPath, "plugins/required");

      /* VendorPluginsCache of the plugins detected during bootstrap. */
      pBootstrapPlugins = new VendorPluginsCache();
      
      /* The status of all plugins encounted. */
      pPluginStatus = new DoubleMap<PluginType,PluginID,PluginStatus>();

      /* VendorPluginCache of all required plugins. */
      pVendorPlugins = new VendorPluginsCache();

      /* Flag indicating if all required plugins have been loaded. */
      pUpToDate = new AtomicBoolean(false);
    }

    /* Plugin resource related fields. */
    {
      /* The path to scratch directory in tmp which will be used to initially
         write the resource files.  At startup the directory is deleted so remnants 
	 from previous plpluginmgr runs are removed. */
      pPluginScratchPath = new Path
	(PackageInfo.sTempPath, "plpluginmgr/scratch");

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Removing (" + pPluginScratchPath + ")");

      /* Recursive delete the contents of the plugin scratch path. */
      try {
	recursiveDelete(pPluginScratchPath.toFile());
      }
      catch(PipelineException ex) {
	throw new IllegalStateException(ex);
      }

      /* After we have removed the scratch directory, make it again. */
      {
	File pluginScratchDir = pPluginScratchPath.toFile();

	if(!pluginScratchDir.isDirectory()) {
	  if(!pluginScratchDir.mkdirs())
	    throw new IllegalStateException
	      ("Unable to create the directory (" + pluginScratchDir +")!");
	}
      }

      /* The session ID identifies each resource install. */
      pSessionID = 1L;

      /* The lock to protect the session ID increments and resource installs to 
         the current plugin version directory. */
      pResourceLock = new ReentrantReadWriteLock();

      /* Stores the request information for each resource install, so that when 
         all the resource have been transferred from the client the install can 
	 continue. */
      pPluginResourceInstalls = new TreeMap<Long,PluginResource>();

      /* Stores the checksums in a convience class to make the responses to 
         getting the installed resources checksums easier. */
      pResourceChecksums = new ResourceChecksums();
    }

    /* If the bootstrap option is used, walk the Pipeline plugins directory and 
       collect the non Temerity plugins. Then write a GLUE file for each Vendor.  
       The bootstrap process performs all the checking that loadAllPlugin does, but 
       does not load the plugins.  The bootstrap flag  allows for the same code to be 
       used.  After the bootstrap process writes the required plugins GLUE file for 
       Vendor plugins detected, plpluginmgr starts up as normal, now it has additional 
       GLUE files to examine. */

    if(bootstrapDir != null) {
      if(!bootstrapDir.isDirectory())
        throw new IllegalStateException
          ("The given required plugins bootstrap directory (" + bootstrapDir + ")" + 
	   " does not exist!");

      File bootstrapPluginsDir = new File(bootstrapDir, "plugins");

      /* The PluginMgrOptsParser performs the following 2 checks, ensures the supplied 
         Pipeline root directory exists and that there is a plugin directory under it.  
         So if these exceptions are thrown then somehow the PluginMgrOptParser's checks 
         were bypassed. */
      if(!bootstrapPluginsDir.isDirectory())
        throw new IllegalStateException
          ("The given required plugins bootstrap directory (" + bootstrapDir + ") does " + 
           "not contain a plugins directory!  Please check that you are using the correct " + 
           "previous Pipeline directory or do not use --bootstrap=(previous " + 
           "Pipeline root directory)");

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info, 
         "In required plugins bootstrap mode using (" + bootstrapDir + ") as the root of " + 
         "the previous Pipeline version.");

      loadAllPlugins(bootstrapPluginsDir, PluginLoadType.Bootstrap);

      int pluginCount = 0;

      /* Here is an instance of using the VendorPluginsCache class, the method "vendors()"
         returns a set of the vendor strings.  It is a wrapper to keySet on the 
	 underlying data structure. */
      for(String vname :pBootstrapPlugins.vendors()) {
	if(!vname.equals("Temerity")) {
	  /* This is an instance of using the VendorPluginsCache class, the method
	     "getPlugins" takes a vendor String and returns the 
	     MappedSet<PluginType,PluginID>. */
	  MappedSet<PluginType,PluginID> plugins = pBootstrapPlugins.getPlugins(vname);

	  for(PluginType plgType : plugins.keySet())
	    pluginCount += plugins.get(plgType).size();
	}
      }

      if(pluginCount == 0) {
	throw new IllegalStateException
	  ("The given required plugins bootstrap directory (" + bootstrapDir + ") " + 
	   "does not contain any plugins.  Please check the directory again or do not " + 
           "use the bootstrap option");
      }

      /* Note the VendorPluginsCache is being used here. */
      for(String vname : pBootstrapPlugins.vendors()) {
        if(vname.equals("Temerity")) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
             "For some reason Temerity plugins are in the bootstrap plugins hashtable, " + 
             "this is a problem but they are going to be ignored. If you see message in " + 
             "your log file please post the forum about this.");
        }
        else {
          try {
            writeRequiredPlugins(vname, pBootstrapPlugins.getPlugins(vname));
          }
          catch(PipelineException ex) {
            throw new IllegalStateException(ex.getMessage());
          }
        }
      }

      displayBootstrapPlugins();

      /* After the bootstrap plugins are written to GLUE files it can be nulled out, 
         since it will not be used again. */
      pBootstrapPlugins = null;
    }

    /* Load the required plugins from glue files. */
    try {
      readRequiredPlugins();
    }
    catch(PipelineException ex) {
      throw new IllegalStateException(ex.getMessage());
    }

    loadAllPlugins(PackageInfo.sPluginsPath.toFile(), PluginLoadType.Startup);

    displayVendorPlugins(); 

    /* Check that all required plugins have been loaded. */
    if(pMissingCount == 0) {
      pUpToDate.set(true);
    }
    displayPluginSummary();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O U T P U T                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print a table of plugins required, loaded and unknown by vendor.
   */
  private void
  displayBootstrapPlugins() 
  {
    int maxLength = 0;
    for(String plgType : PluginType.titles())
      maxLength = Math.max(plgType.length()+1, maxLength);

    String totalMsg = "TOTAL";

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Info,
       tbar(80) + "\n" + 
       title("BootstrappedPlugins"));

    for(String vendor : pBootstrapPlugins.vendors()) {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" + 
         " " + vendor + " Plugins\n" + 
         pad(maxLength) + "   required\n" +
         pad(maxLength) + "   --------");

      int total = 0;
      MappedSet<PluginType,PluginID> pset = pBootstrapPlugins.getPlugins(vendor);
      for(PluginType ptype : pset.keySet()) {
        int cnt = pset.get(ptype).size();
        if(cnt > 0) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Info,
             lpad(ptype.toString(), maxLength) + " :  " + 
             lpad(Integer.toString(cnt), 5)); 
        }
          
        total += cnt;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         "\n" + lpad("TOTAL", maxLength) + " :  " + 
         lpad(Integer.toString(total), 5) + "\n");
    }
  }


  /**
   * Print a table of plugins required, loaded and unknown by vendor.
   */ 
  private void
  displayVendorPlugins() 
  {
    int maxLength = 0;
    for(String plgType : PluginType.titles())
      maxLength = Math.max(plgType.length()+1, maxLength);

    String totalMsg = "TOTAL";

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Info,
       tbar(80) + "\n" + 
       title("ProcessedPlugins"));

    for(String vendor : pVendorPlugins.vendors()) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" + 
         " " + vendor + " Plugins\n" + 
         pad(maxLength) + "   required  loaded  unknown\n" +
         pad(maxLength) + "   -------------------------");

      int totalM = 0;
      int totalL = 0;
      int totalU = 0;

      for(PluginType ptype : pPluginStatus.keySet()) {
        int mcnt = 0;
	int lcnt = 0;
	int ucnt = 0;

	for(PluginID pid : pPluginStatus.keySet(ptype)) {
	  if(!pid.getVendor().equals(vendor))
	    continue;

	  PluginStatus pstat = pPluginStatus.get(ptype, pid);

	  switch(pstat) {
	    case Missing:
	      {
		mcnt++;
	      }
	      break;
	    case Installed:
	    case UnderDevelopment:
	    case Permanent:
	      {
		lcnt++;
	      }
	      break;
	    case Unknown:
	      {
		ucnt++;
	      }
	      break;
	  }
	}

        if((mcnt > 0) || (lcnt > 0) || (ucnt > 0)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Info,
             lpad(ptype.toString(), maxLength) + " :  " + 
             lpad(Integer.toString(lcnt+mcnt), 5) + 
             lpad(Integer.toString(lcnt), 8) + 
             lpad(Integer.toString(ucnt), 9));
        }
          
        totalM += mcnt;
        totalL += lcnt;
        totalU += ucnt;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         "\n" + lpad("TOTAL", maxLength) + " :  " + 
         lpad(Integer.toString(totalM+totalL), 5) + 
         lpad(Integer.toString(totalL), 8) + 
         lpad(Integer.toString(totalU), 9) + "\n");

      if(totalM > 0) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Plg, LogMgr.Level.Info,
           pad(maxLength) + "   (" + (totalM) + " REQUIRED PLUGIN" + 
           (((totalM) > 1) ? "S" : "") + " MISSING!)\n"); 
      }
      else {
        LogMgr.getInstance().log
          (LogMgr.Kind.Plg, LogMgr.Level.Info,
           pad(maxLength) + "   (all plugins found)\n"); 
      }
    }
  }

  /**
   * Print a summary of required, loaded, unknown and missing plugins.
   */
  private void
  displayPluginSummary()
  {
    if(pMissingCount == 0) {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" +
         "All required plugins have now been loaded!\n" + 
         "You may start all other Pipeline servers and begin normal operation.\n" + 
         bar(80));
    }
    else {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
         bar(80) + "\n" +
         "Not yet accepting connections from other servers until all required " + 
         "plugins have been installed.  You must use plplugin(1) to install all required " + 
         "plugins before the rest of the Pipeline servers can be started.\n" + 
         "\n" + 
         "Current Plugin Counts:\n\n" +
         "   Required = " + pRequiredCount + "\n" + 
         "     Loaded = " + pLoadedCount + "\n" + 
         "    Unknown = " + pUnknownCount + "\n" + 
         "    Missing = " + pMissingCount + "\n" + 
         "\n" + 
         "You can use plplugin(1) with the \"--list --status=miss\" options to get the " + 
         "full listing of the specific required plugins which are currently missing and " + 
         "still need to be installed.\n" + 
         bar(80));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the work groups and administrative privileges from the MasterMgr.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to update the privileges.
   */ 
  public Object
  updateAdminPrivileges
  (
   MiscUpdateAdminPrivilegesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("PluginMgr.updateAdminPrivileges()");

    timer.aquire();
    pAdminPrivileges.updateAdminPrivileges(timer, req);
    return new SuccessRsp(timer);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L U G I N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get any new or updated plugin classes.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>PluginUpdateRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to get the updated plugins.
   */ 
  public Object 
  update
  ( 
   PluginUpdateReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pPluginLock.readLock().lock();
    try {
      timer.resume();
      
      Long cycleID = req.getCycleID();

      TripleMap<String, String, VersionID, Object[]> builders = 
        pBuilderCollection.collectUpdated(cycleID);
      
      TripleMap<String, String, VersionID, LayoutGroup> groups =
        new TripleMap<String, String, VersionID, LayoutGroup>();
      for(String vendor : builders.keySet()) {
        for(String name : builders.keySet(vendor)) {
          for(VersionID vid : builders.keySet(vendor, name)) {
            groups.put(vendor, name, vid, pBuilderCollectionLayouts.get(vendor, name, vid));
          }
        }
      }
      
      TripleMap<String, String, VersionID, Object[]> annotations = 
        pAnnotations.collectUpdated(cycleID);
      TripleMap<String, String, VersionID, AnnotationPermissions> annotPerms =
        new TripleMap<String,String,VersionID,AnnotationPermissions>();
      TripleMap<String,String,VersionID,TreeSet<AnnotationContext>> annotContexts = 
        new TripleMap<String,String,VersionID,TreeSet<AnnotationContext>>();
      for(String vendor : annotations.keySet()) {
        for(String name : annotations.keySet(vendor)) {
          for(VersionID vid : annotations.keySet(vendor, name)) {
            annotPerms.put(vendor, name, vid, pAnnotationPermissions.get(vendor, name, vid));
            annotContexts.put(vendor, name, vid, pAnnotationContexts.get(vendor, name, vid));
          }
        }
      }

      /* Prepare a copy of the PluginStatus table.  This contains the status for 
         all plugins that are loaded, missing, unknown and even the details of 
	 being permanent or underdevelopment. 
	 
	 PluginStatus is indexed by PluginType and PluginID. */
      DoubleMap<PluginType,PluginID,PluginStatus> pluginStatus = 
	new DoubleMap<PluginType,PluginID,PluginStatus>();

      for(PluginType ptype : pPluginStatus.keySet()) {
	for(PluginID pid : pPluginStatus.keySet(ptype)) {
	  PluginStatus pstat = pPluginStatus.get(ptype, pid);

	  pluginStatus.put(ptype, pid, pstat);
	}
      }
      
      return new PluginUpdateRsp(timer, pLoadCycleID, 
                                 pEditors.collectUpdated(cycleID), 
                                 pActions.collectUpdated(cycleID), 
                                 pComparators.collectUpdated(cycleID), 
                                 pTools.collectUpdated(cycleID), 
                                 annotations, 
                                 pArchivers.collectUpdated(cycleID), 
                                 pMasterExts.collectUpdated(cycleID), 
                                 pQueueExts.collectUpdated(cycleID), 
                                 pKeyChoosers.collectUpdated(cycleID),
                                 builders,
                                 groups,
                                 annotPerms, annotContexts, 
				 pluginStatus); 
    }
    finally {
      pPluginLock.readLock().unlock();
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Install a new or updated plugin class.
   *
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>PlugingCountRsp</CODE> if there are required plugins that need to 
   *   be installed or
   *   <CODE>FailureRsp</CODE> if unable to install the plugin. 
   */ 
  public Object
  install
  (
   PluginInstallReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pPluginLock.writeLock().lock();
    try {
      timer.resume();

      if(!pAdminPrivileges.isDeveloper(req))
	throw new PipelineException
	  ("Only a user with Developer privileges may install plugins!");

      String cname = req.getClassName();
      boolean isDryRun = req.getDryRun();

      TreeMap<String,byte[]> contents = req.getContents();

      /* For plugins with resources cache the file sizes and the checksums. */
      TreeMap<String,Long> resources = new TreeMap<String,Long>();
      TreeMap<String,byte[]> checksums = new TreeMap<String,byte[]>();

      if(req instanceof PluginResourceInstallReq) {
	PluginResourceInstallReq resourceReq = (PluginResourceInstallReq) req;

	TreeMap<String,Long>   resourcesFromReq = resourceReq.getResources();
	TreeMap<String,byte[]> checksumsFromReq = resourceReq.getChecksums();

	if(resourcesFromReq != null) {
	  resources.putAll(resourcesFromReq);

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "Resources table size (" + resources.size() + ").");
	}

	if(checksumsFromReq != null) {
	  checksums.putAll(checksumsFromReq);

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "Checksums table size (" + checksums.size() + ").");
	}
      }

      /* load the class and cache the class bytes */ 
      pLoadCycleID++;
      loadPluginHelper(req.getClassFile(), cname, req.getVersionID(), contents, 
                       resources, checksums, 
                       req.getExternal(), req.getRename(), 
		       (isDryRun ? PluginLoadType.DryRun : PluginLoadType.Install)); 


      if(!isDryRun) {
	/* save the plugin class bytes in a file */ 
	Path path = null; 
	try {
	  boolean isJar = (contents.size() > 1);
	  path = new Path(PackageInfo.sPluginsPath, 
			  cname.replace(".", "/") + (isJar ? ".jar" : ".class"));
	  
	  File dir = path.getParentPath().toFile();
	  if(!dir.exists()) {
	    if(!dir.mkdirs()) 
	      throw new IOException
		("Unable to create the directory (" + dir + ") where the plugin " + 
	         "(" + cname + ") will be installed!");
	  }
	  else {
	    /* remove old class or JAR file if the plugin has changed format */ 
	    Path opath = new Path(PackageInfo.sPluginsPath, 
				  cname.replace(".", "/") + (isJar ? ".class" : ".jar"));
	    File ofile = opath.toFile();
	    if(ofile.isFile()) 
	      ofile.delete();
	  }
	  
	  if(isJar) {
	    JarOutputStream out = new JarOutputStream(new FileOutputStream(path.toFile()));
	  
	    for(String key : contents.keySet()) {
	      String ename = (key.replace(".", "/") + ".class");
	      byte bs[] = contents.get(key);
	    
	      out.putNextEntry(new ZipEntry(ename));
	      out.write(bs, 0, bs.length);
	      out.closeEntry();
	    }

	    out.close();
	  }
	  else {
	    FileOutputStream out = new FileOutputStream(path.toFile());
	    out.write(contents.get(cname)); 
	    out.close();
	  }
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to save the plugin (" + cname + ") to file (" + path + ")!");
	}

	/* If the plugin contains resources save the resources and checksums tables 
	   in the same directory as the java class or jar file. */
	if(checksums != null && !checksums.isEmpty()) {
	  Path checksumsPath = new Path(path.getParentPath(), "checksums.glue");

	  try {
	    GlueEncoderImpl.encodeFile
	      ("checksums", checksums, checksumsPath.toFile());
	  }
	  catch(GlueException ex) {
	    throw new PipelineException(ex);
	  }
	}

	if(resources != null && !resources.isEmpty()) {
	  Path resourcesPath = new Path(path.getParentPath(), "resources.glue");

	  try {
	    GlueEncoderImpl.encodeFile
	      ("resources", resources, resourcesPath.toFile());
	  }
	  catch(GlueException ex) {
	    throw new PipelineException(ex);
	  }
	}

	/* Check that all required plugins have been loaded. */
	if(!pUpToDate.get()) {
	  if(pMissingCount == 0)
	    pUpToDate.set(true);

	  displayPluginSummary();
	}

	/* The plugin has been successfully installed at this point but if there are
           required plugins that need to be installed return a PluginCount response 
           rather than Success response.  This is not an error, only adds plugins counts 
           to inform the user of the state of required plugins. */

	if(!pUpToDate.get()) {
	  if(pMissingCount > 0 || pUnknownCount > 0)
	    return new PluginCountRsp(timer, pMissingCount, pUnknownCount);
	}
      }
      /* end if(!rsp.getDryRun()) */

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pPluginLock.writeLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Provide the checksum table for a loaded plugin that has resources.
   *
   * @param req
   *   The checksum request for a plugin.
   *
   * @return
   *   <CODE>PluginChecksumRsp</CODE> if able to retrieve the checksums table,
   *   note that a null value can be return for the checksums table if the plugin 
   *   does not have resources installed therefore the client must check for that.
   *   <CODE>FailureRsp</CODE> if unable to retrieve the checksums table.
   */
  public Object
  checksum
  (
   PluginChecksumReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pPluginLock.readLock().lock();
    try {
      timer.resume();

      PluginType ptype = req.getPluginType();
      PluginID   pid   = req.getPluginID();

      if(ptype == null) {
	throw new PipelineException("PluginType is (null) in this request!");
      }

      if(pid == null) {
	throw new PipelineException("PluginID is (null) in this request!");
      }

      TreeMap<String,byte[]> checksums = pResourceChecksums.getChecksums(ptype, pid);

      return new PluginChecksumRsp(timer, checksums);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pPluginLock.readLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Prepare for the install of resources.
   *
   * @param req
   *   The resource install request for a plugin.
   *
   * @return
   *   <CODE>PluginResourceInstallRsp</CODE> if the prep work is successful and ready
   *   to receive the resource chunks.
   *   <CODE>PluginCountRsp</CODE> if no resources needed to be transferred and the plugin 
   *   install was successful but required plugins are missing.
   *   <CODE>SuccessRsp</CODE> if no resourcces needed to be transferred and the plugin 
   *   install was successful.
   *   <CODE>FailureRsp</CODE> if unable to do the resource install prep work or the 
   *   plugin install was unsuccessful.
   */
  public Object
  prepInstallResource
  (
    PluginResourceInstallReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    try {
      timer.resume();

      if(!pAdminPrivileges.isDeveloper(req))
	throw new PipelineException
	  ("Only a user with Developer privileges may install plugins!");

      String cname = req.getClassName();

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Plugin (" + cname + ")");

      long sessionID = -1;

      /* Setup the resource install by getting a session ID and creating a 
         directory in the scratch directory with the session ID as the name.  
	 Using the session ID directory name allows for multiple plugins with 
	 resources installs, even with the same plugin and not require any 
	 locking.  Also add the sesion ID and the request into the 
	 pPluginResourceInstalls table. */

      /* Note that since each resource install gets its own directory to work within
         this is only place for locking.  All the file copying can occur without a lock. */
      pResourceLock.writeLock().lock();
      try {
	sessionID = pSessionID++;

	/* Save the details of the install request to use later. */
	pPluginResourceInstalls.put
	  (sessionID, new PluginResource(sessionID, req));
      }
      finally {
	pResourceLock.writeLock().unlock();
      }

      if(sessionID == -1)
	throw new PipelineException
	  ("Invalid sessionID (" + sessionID + ") encountered while " + 
	   "performing plugin resource install prep!");

      /* The scratch directory is named after the session ID for the install.  In the 
         successful case it will be moved to the plugin version directory. */
      Path scratchPath = 
	new Path(pPluginScratchPath, Long.toString(sessionID));

      /* All the resources are stored in a directory named "resources" to keep
         the plugin version directory clean.  The only possible contents of a plugin 
	 version directory are a resources directory, and jar or class file. */
      Path scratchResourcePath = 
	new Path(scratchPath, "resources");

      File scratchDir = scratchPath.toFile();
      File scratchResourceDir = scratchResourcePath.toFile();

      if(scratchDir.exists())
	recursiveDelete(scratchDir);

      if(!scratchDir.mkdirs()) {
	throw new PipelineException
	  ("Unable to mkdir (" + scratchDir + ")!");
      }

      PluginResource pluginResource = 
	pPluginResourceInstalls.get(sessionID);

      if(pluginResource == null)
	throw new PipelineException("Error in the plugin resource install prep!");

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "SessionID (" + sessionID + ")");

      /* The request will only send the file sizes of the resources that will be 
         updated.  It can be empty if only the plugin classes are being updated and the 
	 resources are the same.  The checksums table is always sent in full, if it is 
	 empty that means there are resources installed for the plugin but now will be 
	 removed from the plugin, this signals that none of the previous resource will be 
	 copied from current plugin version directory. */
      TreeMap<String,Long>   resources = req.getResources();
      TreeMap<String,byte[]> checksums = req.getChecksums();

      TreeMap<String,Long> localResources = new TreeMap<String,Long>();

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "(" + resources.size() + ") " + 
	 "(" + localResources.size() + ") " + 
	 "(" + checksums.size() + ")");

      Path cpath = new Path(cname.replace('.', '/'));
      Path currentPluginPath = new Path(PackageInfo.sPluginsPath, cpath.getParentPath());
      Path currentResourcePath = new Path(currentPluginPath, "resources");

      if(!checksums.isEmpty()) {
	/* If the checksums table is not empty we will copy the resources that do not 
	   need to be updated from the current plugin version directory.  Otherwise, 
	   make empty files in its place. */
	if(!scratchResourceDir.mkdirs()) {
	  throw new PipelineException
	    ("Unable to mkdir (" + scratchResourceDir + ")!");
	}

	for(String path : checksums.keySet()) {
	  Path resourcePath = new Path(scratchResourcePath, path);

	  /* Create the intermediate directories if necessary. */
	  if(!resourcePath.hasParent())
	    throw new PipelineException
	      ("Path (" + resourcePath + ") does not have a parent!");

	  File parentDir = resourcePath.getParentPath().toFile();

	  if(!parentDir.exists())
	    if(!parentDir.mkdirs())
	      throw new PipelineException
		("Unable to mkdir (" + parentDir + ")!");

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "Resource file (" + resourcePath + ")");

	  try {
	    /* If the key path is in the resources (really the filesizes) table 
	       then that means the bytes will be sent, else there is no update 
	       to the file and copy it from the installed plugin directory into 
	       the scratch directory.  The use of a scratch directory eliminates 
	       the need to worry about resources that have been deleted from the 
	       plugin since the checksums tables will contain all the resources. */
	    if(!resources.containsKey(path)) {
	      Path srcPath = new Path(currentResourcePath, path);

	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	         "The plugin resource file (" + path + ") does not need to be updated, " + 
	         "copying the previously installed file from (" + srcPath + ")");
	  
	      File srcFile = srcPath.toFile();

	      long filesize = srcFile.length();

	      FileChannel src = new FileInputStream(srcFile).getChannel();
	      FileChannel dst = new FileOutputStream(resourcePath.toFile()).getChannel();

	      dst.transferFrom(src, 0, filesize);

	      dst.close();
	      src.close();

	      localResources.put(path, filesize);
	    }
	    else {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	         "The plugin resource file (" + path + ") will be updated.");

	      long filesize = resources.get(path);

	      RandomAccessFile resourceFile = 
		new RandomAccessFile(resourcePath.toFile(), "rw");

	      resourceFile.setLength(filesize);
	      resourceFile.close();
	    }
	  }
	  catch(FileNotFoundException ex) {
	    throw new PipelineException(ex);
	  }
	  catch(SecurityException ex) {
	    throw new PipelineException(ex);
	  }
	  catch(IOException ex) {
	    throw new PipelineException(ex);
	  }
	}
      }

      /* The PluginResource class manages the receipt of resource files. */
      pluginResource.updateResources(resources, localResources);

      /* If at this point the file size table from the request is empty, this 
         means that no resource files needed to be transferred from the client and 
	 we can begin the install process. */
      if(resources.isEmpty()) {
	return installResource(pluginResource);
      }

      return new PluginResourceInstallRsp(timer, sessionID);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Install the plugin after all resources files have been copied or transferred 
   * from the client.  There is directory moving and deleting and a final call to 
   * install the plugin.
   *
   * @param pluginResource
   *   A PluginResource object that contains the info from a install request.
   *
   */
  private Object
  installResource
  (
   PluginResource pluginResource
  )
  {
    long sessionID = pluginResource.getSessionID();

    String cname = pluginResource.getClassName();
    String cpath = 
      cname.substring(0, cname.lastIndexOf('.')).replace('.', '/');

    Path scratchPath = new Path(pPluginScratchPath, Long.toString(sessionID));
      
    Path currentVersionPath = new Path(PackageInfo.sPluginsPath, cpath);
    Path backupVersionPath  = new Path(PackageInfo.sPluginsPath, cpath + "-backup");

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Current plugin path (" + currentVersionPath + ")");

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Backup plugin path (" + backupVersionPath + ")");

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Scratch plugin path (" + scratchPath + ")");

    File scratchDir = scratchPath.toFile();

    File scratchVersionDir = scratchPath.toFile();
    File currentVersionDir = currentVersionPath.toFile();
    File backupVersionDir  = backupVersionPath.toFile();

    boolean isNewInstall = !currentVersionDir.exists();

    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pResourceLock.writeLock().lock();
    try {
      if(currentVersionDir.exists()) {
	if(!currentVersionDir.renameTo(backupVersionDir))
	  throw new PipelineException
	    ("Unable to rename (" + currentVersionDir + ") " + 
	     "to (" + backupVersionDir + ")!");
      }

      if(scratchVersionDir.exists()) {
	if(!scratchVersionDir.renameTo(currentVersionDir))
	  throw new PipelineException
	    ("Unable to rename (" + scratchVersionDir + ") " + 
	     "to (" + currentVersionDir + ")!");
      }

      PluginResourceInstallReq installReq = 
	new PluginResourceInstallReq(pluginResource.getClassFile(), 
		                     pluginResource.getClassName(), 
				     pluginResource.getVersionID(), 
				     pluginResource.getContents(), 
				     pluginResource.getResources(), 
				     pluginResource.getChecksums(), 
				     pluginResource.getExternal(), 
				     pluginResource.getRename(), 
				     pluginResource.getDryRun());
	  
      Object rsp = install(installReq);

      try {
	if(rsp instanceof SuccessRsp) {
	  if(currentVersionDir.exists())
	    recursiveDelete(backupVersionDir);
	}
	else {
	  if(currentVersionDir.exists())
	    recursiveDelete(currentVersionDir);

	  if(isNewInstall) {
	    if(backupVersionDir.exists())
	      recursiveDelete(backupVersionDir);
	  }
	  else {
	    if(backupVersionDir.exists()) {
	      if(!backupVersionDir.renameTo(currentVersionDir))
		throw new PipelineException
		  ("Unable to rename (" + backupVersionDir + ") " + 
	           "to (" + currentVersionDir + ")!");
	    }
	  }
	}

	if(scratchDir.exists())
	  recursiveDelete(scratchDir);
      }
      catch(PipelineException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Resource install cleanup error (" + ex.getMessage() + ")");

	throw ex;
      }

      pPluginResourceInstalls.remove(sessionID);

      return rsp;
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      pResourceLock.writeLock().unlock();
    }
  }

  /**
   * Used by PluginMgrServer to cleanup interrupted resource installs.
   */
  public void
  cleanupResourceInstall
  (
   long sessionID
  )
    throws PipelineException
  {
    pResourceLock.writeLock().lock();
    try {
      if(!pPluginResourceInstalls.containsKey(sessionID))
	throw new PipelineException
	  ("SessionID (" + sessionID + ") is invalid!");

      Path scratchPath = 
	new Path(pPluginScratchPath, Long.toString(sessionID));

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Removing scratch path (" + scratchPath + ")");

      {
	File scratchDir = scratchPath.toFile();

	if(scratchDir.exists())
	  recursiveDelete(scratchDir);
      }

      pPluginResourceInstalls.remove(sessionID);
    }
    finally {
      pResourceLock.writeLock().unlock();
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Resource files are installed in chunks sizes.  The PluginResource object 
   * keeps track of all the file chunks received and whether all resources have 
   * received.
   */
  public Object
  installResourceChunk
  (
   PluginResourceChunkInstallReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    try {
      timer.resume();

      if(!pAdminPrivileges.isDeveloper(req))
	throw new PipelineException
	  ("Only a user with Developer privileges may install plugins!");

      long sessionID = req.getSessionID();

      pResourceLock.readLock().lock();
      try {
	if(!pPluginResourceInstalls.containsKey(sessionID))
	  throw new PipelineException("Session ID (" + sessionID + ") is invalid!");
      }
      finally {
	pResourceLock.readLock().unlock();
      }

      int chunksize = req.getChunkSize();

      if(chunksize < 1)
	throw new PipelineException("There are zero bytes in this request!");

      byte[] bytes = req.getBytes();
      String path  = req.getResourcePath();

      Path scratchPath = 
	new Path(pPluginScratchPath, Long.toString(sessionID));

      Path scratchResourcePath = 
	new Path(scratchPath, "resources");

      Path rpath = new Path(scratchResourcePath, path);

      try {
	RandomAccessFile resourceFile = 
	  new RandomAccessFile(rpath.toFile(), "rw");

	long startPosition = req.getStartBytePosition();

	resourceFile.seek(startPosition);
	resourceFile.write(bytes, 0, chunksize);
	resourceFile.close();
      }
      catch(IOException ex) {
	throw new PipelineException(ex);
      }

      PluginResource pluginResource = null;

      pResourceLock.readLock().lock();
      try {
	pluginResource = pPluginResourceInstalls.get(sessionID);
      }
      finally {
	pResourceLock.readLock().unlock();
      }

      pluginResource.updateResourceChunk(path, chunksize, rpath);

      /* When all resources have been received we can start the install 
         process and the return the response to the client. */
      if(pluginResource.hasReceivedAllResources()) {
	return installResource(pluginResource);
      }

      return new PluginResourceInstallRsp(timer, sessionID);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a means to delete directories.
   */
  private boolean
  recursiveDelete
  (
   File dir
  )
    throws PipelineException
  {
    if(dir.isDirectory()) {
      File[] filelist = dir.listFiles();

      for(int i = 0 ; i < filelist.length ; i++)
	if(!recursiveDelete(filelist[i]))
	  throw new PipelineException("Unable to remove (" + filelist[i] + ")!");
    }

    return dir.delete();
  }

  /**
   * Load all installed plugin classes.
   * 
   * @param root
   *   Root of the Pipeline plugins directory.
   * 
   * @param pluginLoadType
   *   Which rules the plugin loading process should follow.
   */ 
  private void 
  loadAllPlugins
  (
   File root, 
   PluginLoadType pluginLoadType
  ) 
  {
    File[] dirs = root.listFiles();
    int wk;
    for(wk=0; wk<dirs.length; wk++) {
      if(dirs[wk].isDirectory()) 
	loadBelow(root, dirs[wk], pluginLoadType);
    }
  }

  /** 
   * Recursively load all installed plugin classes under the given directory.
   * 
   * @param root
   *   The root directory of installed plugins.
   * 
   * @param dir
   *   The current directory.
   * 
   * @param pluginLoadType
   *   Which rules the plugin loading process should follow.
   */
  private void 
  loadBelow
  (
   File root, 
   File dir, 
   PluginLoadType pluginLoadType
  ) 
  {
    if(!dir.isDirectory())
      return;

    File[] fs = dir.listFiles();

    ArrayList<Path> pluginList = new ArrayList<Path>();
    {
      int wk;
      for(wk=0; wk<fs.length; wk++) {
	if(fs[wk].isFile()) {
	  String filename = fs[wk].getName();
	
	  if(filename.endsWith(".class") || filename.endsWith(".jar")) {
	    pluginList.add(new Path(fs[wk]));
	    break;
	  }
	}
      }
    }

    int pluginCount = pluginList.size();

    switch(pluginCount) {
      case 0:
	{
	  int wk;
	  for(wk=0; wk<fs.length; wk++) {
	    if(fs[wk].isDirectory()) {
	      loadBelow(root, fs[wk], pluginLoadType);
	    }
	  }
	}
	break;

	case 1:
	  {
	    Path pluginPath = pluginList.get(0);
	    String dname = dir.getName();
	    boolean isBackup = false;

	    if(dname.endsWith("-backup")) {
	      isBackup = true;
	    }

	    try {
	      VersionID vid = null;
	      try {
		if(dname.startsWith("v")) {
		  int endIndx = dname.length() - (isBackup ? 7 : 0);

		  String vstr = 
		    dname.substring(1, endIndx).replace('_','.');

		  vid = new VersionID(vstr);
		}
		else {
		  throw new IllegalArgumentException("Missing \"v\" prefix.");
		}
	      }
	      catch(IllegalArgumentException ex) {
		throw new PipelineException 
		("The directory containing plugin files (" + dir + ") does " +
	         "not conform to the naming convention of \"v#_#_#\" used to denote " +
	         "the plugin version!  Ignoring plugin file (" + pluginPath + ").\n" + 
	         ex.getMessage());
	      }

	      Path parentPath = new Path(dir.getParentFile());

	      /* If we are in a backup plugin-version directory, as indicated by a 
	         directory that ends with "-backup," first check to see that a 
		 directory without the "-backup" exists, if so then do not 
		 process the directory.
		 
		 If the directory without backup does not exist then attempt to 
		 load the plugin from the backup directory.  First rename the backup 
		 to current then attempt the plugin load.  If it fails remove current. */
	      if(isBackup) {
		Path currentPath = 
		  new Path(parentPath, dname.substring(0, dname.length() - 7));
		Path backupPath = 
		  new Path(parentPath, dname);

		File currentDir = currentPath.toFile();
		File backupDir  = backupPath.toFile();

		if(currentDir.exists()) {
		  LogMgr.getInstance().log
		    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
		     "A backup plugin-version directory (" + backupPath + ") " + 
		     "exists, however a current plugin-version directory " + 
		     "(" + currentPath + ") also exists.  PluginMgr will attempt " + 
		     "to load the plugin from the current plugin-version directory " + 
		     "first.");
		}
		else {
		  LogMgr.getInstance().log
		    (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
		     "A backup plugin-version directory (" + backupPath + ") " + 
		     "exists, and a current plugin-version directory " + 
		     "does not exist.  PluginMgr will attempt to load the plugin " + 
		     "from the backup plugin-version directory.");

		  if(!backupDir.renameTo(currentDir)) {
		    throw new PipelineException
		      ("Unable to rename backup " + 
		       "(" + backupPath + ") to current (" + currentPath + ")!");
		  }

		  try {
		    Path backupPlugin = new Path(currentPath, pluginPath.getName());

		    if(backupPlugin.toFile().exists())
		      loadPlugin(root, backupPlugin.toFile(), pluginLoadType);
		    else
		      throw new PipelineException("Error loading the backup plugin!");
		  }
		  catch(PipelineException ex) {
		    LogMgr.getInstance().log
		      (LogMgr.Kind.Plg, LogMgr.Level.Severe, 
		       "Unable to load the plugin using the backup plugin-version " + 
		       "directory (" + backupPath + ")!  The backup plugin-version " + 
		       "directory will be removed.  " + ex.getMessage());

		    if(backupPath.toFile().exists())
		      recursiveDelete(backupPath.toFile());

		    if(currentPath.toFile().exists())
		      recursiveDelete(currentPath.toFile());
		  }
		}
	      }
	      /* We are in a normal plugin-version directory, which will be known
	         as current when a backup directory exists.  Attempt the plugin load 
		 as normal.  If the plugin load fails then check for a backup directory. 
		 Remove current and rename backup to current, then attempt the plugin 
		 load.  If the plugin load fails then remove current. */
	      else {
		Path currentPath = new Path(parentPath, dname);
		Path backupPath = new Path(parentPath, dname + "-backup");

		File currentDir = currentPath.toFile();
		File backupDir  = backupPath.toFile();

		try {
		  loadPlugin(root, pluginPath.toFile(), pluginLoadType);
		}
		catch(PipelineException ex) {
		  LogMgr.getInstance().log
		    (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
		     "Unable to load the plugin using the current plugin-version " + 
		     "directory (" + currentPath + ").  checking for a backup " + 
		     "plugin-version.  " + ex.getMessage());

		  if(backupPath.toFile().exists()) {
		    LogMgr.getInstance().log
		      (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
		       "A backup plugin-version directory (" + backupPath + ") " + 
		       "exists.  PlugimMgr will attempt to load the plugin using " + 
		       "the backup.");

		    recursiveDelete(currentPath.toFile());

		    if(!backupPath.toFile().renameTo(currentPath.toFile()))
		      throw new PipelineException
			("Unable to rename the backup " + 
			 "plugin-version directory (" + backupPath +") to the " + 
			 "current plugin-version directory (" + currentPath + ")!");

		    try {
		      ArrayList<Path> plugins = new ArrayList<Path>();
		      {
			plugins.add(new Path(currentPath, parentPath.getName() + ".jar"));
			plugins.add(new Path(currentPath, parentPath.getName() + ".class"));
		      }

		      for(Path plugin : plugins) {
			File pluginFile = plugin.toFile();

			if(pluginFile.exists()) {
			  loadPlugin(root, pluginFile, pluginLoadType);

			  LogMgr.getInstance().log
			    (LogMgr.Kind.Plg, LogMgr.Level.Info, 
			     "Successfully loaded the plugin using the backup " + 
			     "plugin-version directory.  The backup will be renamed " + 
			     "to the current plugin-version directory.");

			  break;
			}
		      }
		    }
		    catch(PipelineException ex2) {
		      LogMgr.getInstance().log
			(LogMgr.Kind.Plg, LogMgr.Level.Severe, 
			 "Unable to load the plugin using the backup plugin-version " + 
			 "directory (" + backupPath + ")!  The backup plugin-version " + 
			 "directory will be removed.  " + ex2.getMessage());

		      if(currentDir.exists()) {
			recursiveDelete(currentDir);
		      }
		    }
		  }
		}

		if(backupDir.exists()) {
		  recursiveDelete(backupDir);
		}
	      }
	    }
	    catch(PipelineException ex) {
	      LogMgr.getInstance().log
		(LogMgr.Kind.Plg, LogMgr.Level.Warning,
	         ex.getMessage());
	    }
	  }
	  break;

	default:
	  // TODO log error message about how there are more than one java file.
    }
  }

  /**
   * Load the plugin from the given class or JAR file.
   * 
   * @param classdir
   *   The sole Java CLASSPATH directory used to load the class. 
   * 
   * @param pluginfile
   *   The plugin class or JAR file.
   * 
   * @param pluginLoadType
   *   Which rules the plugin loading process should follow.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPlugin
  (
   File classdir, 
   File pluginfile, 
   PluginLoadType pluginLoadType
  ) 
    throws PipelineException 
  {
    /* the canonical class directory */ 
    File cdir = null;
    try {
      cdir = classdir.getCanonicalFile();

      if(!cdir.isDirectory()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin directory (" + classdir + ") was not a valid directory!");
    }

    /* the canonical class file */ 
    File cfile = null;
    try {
      cfile = pluginfile.getCanonicalFile();

      if(!cfile.isFile()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The plugin file (" + pluginfile + ") was not a valid file!");
    }

    /* the class file relative to the class directory */ 
    File rfile = null;
    {
      String fpath = cfile.getPath();
      String dpath = cdir.getPath();

      if(!fpath.startsWith(dpath)) 
	throw new PipelineException 
	  ("The plugin file (" + cfile + ") was not located under the " + 
	   "plugin directory (" + cdir + ")!");
      
      rfile = new File(fpath.substring(dpath.length()));
    }

    /* the Java package name and plugin revision number */ 
    String pkgName = null; 
    VersionID pkgID = null;
    try {
      File parent = rfile.getParentFile();
      pkgName = parent.getPath().substring(1).replace('/', '.'); 
      
      String vstr = parent.getName();
      if(!vstr.startsWith("v")) 
	throw new IllegalArgumentException
	  ("The directory (" + vstr + ") did not match the pattern (v#_#_#)!");

      pkgID = new VersionID(vstr.substring(1).replace("_", "."));
    }
    catch(IllegalArgumentException ex) {
      throw new PipelineException
	("The plugin file (" + pluginfile + ") was not located under a directory who's " +
	 "name designates a legal plugin revision number:\n" + ex.getMessage());
    }

    /* the class name */ 
    String cname = null;
    boolean isJar = false;
    {
      String parts[] = cfile.getName().split("\\.");
      if((parts.length == 2) && (parts[1].equals("class") || parts[1].equals("jar"))) {
	isJar = parts[1].equals("jar");

	cname = (pkgName + "." + parts[0]);
      }
      else {
	throw new PipelineException 
	  ("The plugin file (" + pluginfile + ") was not a Java class or JAR file!");
      }
    }
    
    /* load, instantiate and validate the plugin class or JAR file */ 
    {
      TreeMap<String,byte[]> contents  = new TreeMap<String,byte[]>(); 
      if(isJar) {
	try {
	  JarInputStream in = new JarInputStream(new FileInputStream(cfile)); 
      
	  byte buf[] = new byte[4096];
	  while(true) {
	    JarEntry entry = in.getNextJarEntry();
	    if(entry == null) 
	      break;

	    if(!entry.isDirectory()) {
	      String path = entry.getName(); 
	      if(path.endsWith("class")) {
		String jcname = path.substring(0, path.length()-6).replace('/', '.'); 
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		while(true) {
		  int len = in.read(buf, 0, buf.length); 
		  if(len == -1) 
		    break;
		  out.write(buf, 0, len);
		}
		
		contents.put(jcname, out.toByteArray());
	      }
	    }
	  }

	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin JAR file (" + cfile + ")!");
	}

	if(!contents.containsKey(cname)) 
	  throw new PipelineException
	    ("The plugin JAR file (" + cfile + ") did not contain the required " + 
	     "plugin class (" + cname + ")!");
      }
      else {
	byte[] bytes = new byte[(int) cfile.length()];

	try {
	  FileInputStream in = new FileInputStream(cfile);
	  in.read(bytes);
	  in.close();
	}
	catch(IOException ex) {
	  throw new PipelineException
	    ("Unable to read the plugin class file (" + cfile + ")!");
	}

	contents.put(cname, bytes);
      }

      /* Read the reources.glue file and load the resources table. */
      TreeMap<String,Long> resources = new TreeMap<String,Long>();
      {
	Path parentPath = new Path(cfile.getParentFile());
	Path resourcesPath = new Path(parentPath, "resources.glue");

	File resourcesFile = resourcesPath.toFile();

	if(resourcesFile.exists()) {
	  try {
	    TreeMap<String,Long> resourcesGLUE = 
	      (TreeMap<String,Long>) GlueDecoderImpl.decodeFile("resources", resourcesFile);

	    if(resourcesGLUE != null) {
	      resources.putAll(resourcesGLUE);
	    }
	  }
	  catch(GlueException ex) {
	    throw new PipelineException(ex);
	  }
	}
      }
      
      /* Read the checksums.glue file and load the checksums table. */
      TreeMap<String,byte[]> checksums = new TreeMap<String,byte[]>();
      {
	Path parentPath = new Path(cfile.getParentFile());
	Path checksumsPath = new Path(parentPath, "checksums.glue");

	File checksumsFile = checksumsPath.toFile();

	if(checksumsFile.exists()) {
	  try {
	    TreeMap<String,byte[]> checksumsGLUE = 
	      (TreeMap<String,byte[]>) GlueDecoderImpl.decodeFile("checksums", checksumsFile);

	    if(checksumsGLUE != null) {
	      checksums.putAll(checksumsGLUE);
	    }
	  }
	  catch(GlueException ex) {
	    throw new PipelineException(ex);
	  }
	}
      }

      loadPluginHelper(pluginfile, cname, pkgID, 
                       contents, resources, checksums, 
		       true, true, 
		       pluginLoadType);
    }
  }
    
  /**
   * Load the plugin.
   * 
   * @param pluginfile
   *   The plugin class or JAR file.
   * 
   * @param cname
   *   The full class name.
   * 
   * @param pkgID
   *   The revision number component of the class package.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   * 
   * @param external
   *   Whether to ignore the Local Vendor check.
   *
   * @param rename
   *   Whether to ignore the Java class/package aliasing check.
   * 
   * @param pluginLoadType
   *   Which rules the plugin loading process should follow.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPluginHelper
  (
   File pluginfile, 
   String cname, 
   VersionID pkgID, 
   TreeMap<String,byte[]> contents, 
   TreeMap<String,Long> resources, 
   TreeMap<String,byte[]> checksums, 
   boolean external, 
   boolean rename, 
   PluginLoadType pluginLoadType
  ) 
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents);

    try {
      boolean isDryRun     = false;
      boolean isBootstrap  = false;
      boolean isStartup    = false;
      boolean isInstall    = false;
      boolean isLoadPlugin = false;

      switch(pluginLoadType) {
	case Bootstrap:
	  {
	    isDryRun = true;
	    isBootstrap = true;
	  }
	  break;
	case DryRun:
	  {
	    isDryRun = true;
	  }
	  break;
	case Startup:
	  {
	    isStartup = true;
	    isLoadPlugin = true;
	  }
	  break;
	case Install:
	  {
	    isInstall = true;
	    isLoadPlugin = true;
	  }
	  break;
	default:
	  throw new PipelineException
	    ("Unknown PluginLoadType (" + pluginLoadType + ")!");
      }

      if(isDryRun) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finer,
	   "Validating: " + cname);
      }
      else {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finer,
	   "Loading: " + cname);
      }

      Class cls = loader.loadClass(cname);
      
      if(!BasePlugin.class.isAssignableFrom(cls)) 
	throw new PipelineException
	  ("The loaded class (" + cname + ") was not a Pipeline plugin!");
      
      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest,
	 "Instantiating Plugin: " + cname);
      BasePlugin plg = null; 
      try {
	plg = (BasePlugin) cls.newInstance();
      }
      catch(InstantiationException ex) {
	throw new PipelineException
	  ("Unable to intantiate plugin class (" + cls.getName() + "):\n" +
	   ex.getMessage());
      }
      catch(IllegalAccessException ex) {
	throw new PipelineException
	  ("Unable to access plugin class (" + cls.getName() + "):\n" +
	   ex.getMessage());
      }
      catch(Exception ex) {
	throw new PipelineException
	  ("Exception thrown by constructor of plugin class (" + cls.getName() + "):\n" + 
	   ex.getMessage());
      }

      if(!plg.getVersionID().equals(pkgID)) 
	throw new PipelineException
	  ("The revision number (v" + plg.getVersionID() + ") of the instantiated " + 
	   "plugin class (" + cname + ") does not match the revision number " + 
	   "(" + pkgID + ") derived from the name of the directory containing the " + 
	   "class file (" + pluginfile + ")!");

      /* Perform the same check that plconfig performs on Vendor names.  Since the Vendor
         name is being used as the filename for the required plugins GLUE file, it should 
         be checked for illegal characters.  Vendor names should only contain letters, 
         digits or '_' '-' '.'*/
      {
        String vname = plg.getVendor();

        char[] cs = vname.toCharArray();
        int wk;
        for(wk=0; wk<cs.length; wk++) {
          if(!(Character.isLetterOrDigit(cs[wk]) || 
              (cs[wk] == '_') ||(cs[wk] == '-') ||(cs[wk] == '.'))) {
            throw new PipelineException
              ("The Vendor of the plugin (" + cname + ") contains illegal characters, " + 
               vname + ", the Vendor name should only contain letters, digits, or the " + 
               "following 3 characters: _ - . (underbar, dash, period).");
          }
        }
      }

      /* If in bootstrap mode we only need to gather info about the plugin to 
         create a required plugins GLUE file for a vendor.  At this point 
	 if there is a plugin class that can be instantiated we know the 
	 PluginID and the PluginType.  However, I am making the assumption that 
	 previous plugins can be instantiated, version id is in proper format 
	 and the vendor name is valid.  If they are not they will not be added 
	 to the bootstrap plugin table since they would have thrown an 
	 Exception and PluginMgr would continue.  We could throw a more 
	 fatal runtime exception if in bootstrap mode to fail PluginMgr 
	 when the minimum tests were not passed. */

      if(isBootstrap) {
	String vendor = plg.getVendor();

	/* Ignore Temerity plugins during bootstrap mode */
	if(!vendor.equals("Temerity")) {
	  PluginType ptype = plg.getPluginType();
	  PluginID   pid   = plg.getPluginID();

	  pBootstrapPlugins.addPlugin(ptype, pid);
	}

	return;
      }

      if(plg.getSupports().isEmpty()) 
	throw new PipelineException
	  ("The plugin class (" + cname + ") does not support execution under any " + 
	   "type of operating system!  At least one OS must be supported.");	
      
      /* Check that the plugin defines a serialVersionUID by using Java reflection.  
         The getDeclaredField method of Class allows access to all fields defined 
	 within a class, SecurityManager permitting.  Testing this code showed me 
	 that I failed to add a serialVersionUID to one of the plugins I installed.  
	 During startup it caught the NoSuchException and did not load the plugin.  
	 plplugin will balk at the install of the plugin until the serialVersionUID 
	 field is added.  This code should be moved to PluginMgrControlClient so the 
	 checking occurs client side, and not waste resources to send over the network 
	 and perform the check on the server side.  Also, the serialVersionID that 
	 ObjectStreamClass reported for my plugin missing the field was not the same 
	 as the value that serialver reports. */

      try {
	Field serialVersionUID = cls.getDeclaredField("serialVersionUID");
      }
      catch(NoSuchFieldException ex) {
	throw new PipelineException
	  ("The plugin class (" + cname + ") does not define a serialVersionUID " + 
	   "field!  Please run serialver to obtain a serialVersionUID.");
      }

      {
	ObjectStreamClass osc = ObjectStreamClass.lookup(cls);
	if(osc == null) 
	  throw new PipelineException
	    ("The plugin (" + cname + ") does not implement Serializable!");

	long serialID = osc.getSerialVersionUID();
	if(serialID == 0L) 
	  throw new PipelineException
	    ("No member (serialVersionUID) was declared for plugin (" + cname + ")!");

	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "The serialID for (" + cname + ") : " + serialID);

	String sname = pSerialVersionUIDs.get(serialID); 
	if((sname != null) && !sname.equals(cname)) 
	  throw new PipelineException
	    ("The member (serialVersionUID) of plugin (" + cname + ") with a value " + 
	     "of (" +  serialID + ") is already being used by the installed plugin " + 
	     "(" + sname + ")!"); 
	
	pSerialVersionUIDs.put(serialID, cname); 
      }

      if(!external && !plg.getVendor().equals(PackageInfo.sLocalVendor)) 
        throw new PipelineException
          ("The Vendor of the plugin (" + cname + ") does not match the default Local " + 
           "Vendor (" + PackageInfo.sLocalVendor + ") for this site!  This may " + 
           "be due to copying the source code from another plugin and forgetting to " + 
           "update the Name, VersionID and Vendor properties of the new plugin.\n" + 
           "\n" + 
           "You can use the --external option to plplugin(1) if you want to install " + 
           "plugins from other vendors and override this check."); 

      if(isInstall &&  
         plg.getVendor().equals("Temerity") && 
	!PackageInfo.sLocalVendor.equals("Temerity"))
	throw new PipelineException
	  ("Local Vendor (" + PackageInfo.sLocalVendor + ") " + 
	   "cannot install plugins from the Temerity vendor!");

      /* If there are resource files with the plugin, verify they exists and 
         the checksum of the file on disk matches the value from the GLUE file. */
      if(isLoadPlugin) {
	if(resources != null) {
	  for(String path : resources.keySet()) {
	    String cpath = 
	      cname.substring(0, cname.lastIndexOf('.')).replace('.', '/');

	    Path pluginPath = new Path(PackageInfo.sPluginsPath, cpath);
	    Path resourcesPath = new Path(pluginPath, "resources");
	    Path rpath = new Path(resourcesPath, path);

	    if(!rpath.toFile().isFile()) {
	      throw new PipelineException
		("Resource file (" + path + ") " + 
		 "for plugin (" + cname + ") does not exist!  " + 
		 "Please reinstall the plugin.");
	    }

	    long filesize = resources.get(path);

	    if(rpath.toFile().length() != filesize) {
	      throw new PipelineException
		("The filsize for resource file (" + path + ") " + 
		 "for plugin (" + cname + ") is incorrect! " + 
		 "Please reinstall the plugin.");
	    }
	  }
	}
      }



      if(plg instanceof BaseEditor) { 
        if(!rename) 
          checkForPluginAliasing(pEditors, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pEditors, cname, plg);
	}
	else {
	  loadValidatedPlugin(pEditors, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseAction) {
        if(!rename) 
          checkForPluginAliasing(pActions, cname, plg); 

        BaseAction action = (BaseAction) plg;
        if(action.supportsSourceParams() && (action.getInitialSourceParams() == null))
          throw new PipelineException
            ("The action plugin (" + cname + ") claims to support source parameters, but " + 
             "does not actually create any source parameters."); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pActions, cname, plg);
	}
	else {
	  loadValidatedPlugin(pActions, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseComparator) {
        if(!rename) 
          checkForPluginAliasing(pComparators, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pComparators, cname, plg);
	}
	else {
	  loadValidatedPlugin(pComparators, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseTool) {
        if(!rename) 
          checkForPluginAliasing(pTools, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pTools, cname, plg);
	}
	else {
	  loadValidatedPlugin(pTools, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseAnnotation) {
        if(!rename) 
          checkForPluginAliasing(pAnnotations, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pAnnotations, cname, plg);
	}
	else {
	  loadValidatedPlugin(pAnnotations, cname, plg, 
			      contents, resources, checksums, isStartup);

	  BaseAnnotation annot = (BaseAnnotation) plg;

	  AnnotationPermissions permissions = 
	    new AnnotationPermissions(annot.isUserAddable(), annot.isUserRemovable());
	  pAnnotationPermissions.put(plg.getVendor(), plg.getName(), plg.getVersionID(), 
                                     permissions);
          
          pAnnotationContexts.put(plg.getVendor(), plg.getName(), plg.getVersionID(),
                                  new TreeSet<AnnotationContext>(annot.getContexts()));
	}
      }
      else if(plg instanceof BaseArchiver) {
        if(!rename) 
          checkForPluginAliasing(pArchivers, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pArchivers, cname, plg);
	}
	else {
	  loadValidatedPlugin(pArchivers, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseMasterExt) {
        if(!rename) 
          checkForPluginAliasing(pMasterExts, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pMasterExts, cname, plg);
	}
	else {
	  loadValidatedPlugin(pMasterExts, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseQueueExt) {
        if(!rename) 
          checkForPluginAliasing(pQueueExts, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pQueueExts, cname, plg);
	}
	else {
	  loadValidatedPlugin(pQueueExts, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseKeyChooser) {
        if(!rename) 
          checkForPluginAliasing(pKeyChoosers, cname, plg); 

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pKeyChoosers, cname, plg);
	}
	else {
	  loadValidatedPlugin(pKeyChoosers, cname, plg, 
			      contents, resources, checksums, isStartup);
	}
      }
      else if(plg instanceof BaseBuilderCollection) {
        if(!rename) 
          checkForPluginAliasing(pBuilderCollection, cname, plg); 
        
        BaseBuilderCollection collection = (BaseBuilderCollection) plg;

        /* if we can contact the MasterMgr and QueueMgr, 
           attempt a trial instantiation of each builder... */ 
        {
          MasterMgrClient mclient = null;
          QueueMgrClient qclient = null;
          try {
            boolean isConnected = true;
            try {
              mclient = new MasterMgrClient();
              mclient.verifyConnection();
            
              qclient = new QueueMgrClient();
              qclient.verifyConnection();
            }
            catch (PipelineException ex) {
              isConnected = false;
            }
            
            if(isConnected) {
              for (String builderName : collection.getBuildersProvided().keySet()) {
                BaseBuilder builder = 
                  collection.instantiateBuilder(builderName, mclient, qclient,
                                                false, true, false, false, 
                                                new MultiMap<String, String>());
                
                PassLayoutGroup bLayout = builder.getLayout();
                if(bLayout == null)
                  throw new PipelineException
                    ("The builder (" + builderName + ") in collection " +
                     "(" + collection.getName() + ") does not contain a valid parameter " + 
                     "layout.");
              }
            }
            else {
              LogMgr.getInstance().logAndFlush
                (Kind.Plg, Level.Warning, 
                 "The Builders provided by the BuilderCollection plugin " + 
                 "(" + plg.getName() + " v" + plg.getVersionID() + ") from vendor " + 
                 "(" + plg.getVendor() + ") cannot be instantiated to perform the full " + 
                 "suite of validation checks at this time.  The Master Manager and Queue " + 
                 "Manager daemons are required for these tests and do not appear to be " + 
                 "running currently."); 
            }
          }
          finally {
            if(mclient != null) 
              mclient.disconnect();
            if(qclient != null) 
              qclient.disconnect();
          }
        }

	if(isDryRun) {
	  checkForPluginUnderDevelopment(pBuilderCollection, cname, plg);
	}
	else {
	  loadValidatedPlugin(pBuilderCollection, cname, plg, 
			      contents, resources, checksums, isStartup);

	  LayoutGroup group = collection.getLayout();
	  pBuilderCollectionLayouts.put
	    (plg.getVendor(), plg.getName(), plg.getVersionID(), group);
	}
      }
      else {
	throw new PipelineException
	  ("The class file (" + pluginfile + ") does not contain a Pipeline plugin!");
      }
    }
    catch(LinkageError ex) {
      throw new PipelineException
	("Unable to link plugin class (" + cname + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      throw new PipelineException
	("Unable to find plugin class (" + cname + "):\n" +
	 ex.getMessage());
    }	  
    finally {
      LogMgr.getInstance().flush();
    }
  }

  /**
   * Check the Java class of any existing plugin to make sure it matches the new one. 
   */ 
  private void 
  checkForPluginAliasing
  (
   PluginCache cache, 
   String cname, 
   BasePlugin plg
  )
    throws PipelineException
  {
    Plugin plugin = cache.get(plg.getVendor(), plg.getName(), plg.getVersionID()); 
    if((plugin != null) && !plugin.getClassName().equals(cname)) {
      throw new PipelineException
        ("There already exists a plugin identified by the same Name, VersionID and Vendor " + 
         "(" + plg.getName() + ", v" + plg.getVersionID() + ", " + plg.getVendor() + ") " +
         "as the plugin being installed, but it is implemented with a different Java " + 
         "class! The existing plugin's Java class is (" + plugin.getClassName() + ") while " + 
         "the new plugin is implemented by the (" + cname + ") Java class. This may " + 
         "be due to copying the source code from another plugin and forgetting to " + 
         "update the Name, VersionID and Vendor properties of the new plugin.\n" +
         "\n" + 
         "You can use the --rename option to plplugin(1) if you need to install " + 
         "plugins with different Java class names for the same plugin identifier.");
    }
  }

  /**
   * Check that the plugin being installed in dry run mode is not under development.
   */
  private void
  checkForPluginUnderDevelopment
  (
   PluginCache cache, 
   String cname, 
   BasePlugin plg
  )
    throws PipelineException
  {
    PluginID pid = plg.getPluginID();

    String vendor = pid.getVendor();
    String name   = pid.getName();

    VersionID vid = pid.getVersionID();

    Plugin plugin = cache.get(vendor, name, vid);
    if((plugin != null) && (!plugin.isUnderDevelopment()))
      throw new PipelineException 
	("Cannot install the plugin class (" + cname + ") " + 
	 "because a previously installed version of the plugin " + 
	 "(" + plg.getName() + ", v" + plg.getVersionID() + ", " + 
	 plg.getVendor() + ") exists which is no longer under development!");
  }

  /**
   * After all plugin validation steps have been taken add the plugin to
   * the PluginCache and perform all the required plugins accounting.
   */
  private void
  loadValidatedPlugin
  (
   PluginCache cache, 
   String cname, 
   BasePlugin plg, 
   TreeMap<String,byte[]> contents, 
   TreeMap<String,Long> resources, 
   TreeMap<String,byte[]> checksums, 
   boolean isStartup
  )
    throws PipelineException
  {
    PluginType ptype = plg.getPluginType();
    PluginID   pid   = plg.getPluginID();

    String vendor = pid.getVendor();
    String name   = pid.getName();

    VersionID vid = pid.getVersionID();

    if(isStartup) {
      PluginStatus pstat = pPluginStatus.get(ptype, pid);

      /* This handles the case where unknown plugins are detected during startup. */
      if(pstat == null) {
	pPluginStatus.put(ptype, pid, PluginStatus.Unknown);
	pUnknownCount++;

	// TODO improve error message.
	throw new PipelineException("Unknown plugin encountered!");
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Adding the plugin (" + cname + ") to the " + ptype + " cache.");

    cache.addPlugin(plg, cname, contents, resources);
    pResourceChecksums.addChecksums(ptype, pid, checksums);
    
    PluginStatus pstat = pPluginStatus.get(ptype, pid);

    if(pstat == null) {
      pLoadedCount++;
    }
    else {
      switch(pstat) {
	case Missing:
	  {
	    pMissingCount--;
	    pLoadedCount++;
	  }
	  break;
	case Unknown:
	  {
	    pUnknownCount--;
	    pLoadedCount++;
	  }
	  break;
      }
    }

    Plugin plugin = cache.get(plg.getVendor(), plg.getName(), plg.getVersionID());

    if(plugin.isUnderDevelopment())
      pPluginStatus.put(ptype, pid, PluginStatus.UnderDevelopment);
    else
      pPluginStatus.put(ptype, pid, PluginStatus.Permanent);

    pVendorPlugins.addPlugin(ptype, pid);

    if(!isStartup) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
         "Writing " + vendor + " required plugins GLUE file to disk.");

      writeRequiredPlugins(vendor, pVendorPlugins.getPlugins(vendor));
    }
  }

  /*
    Read all required-plugins files from the pluginmgr root directory.
  */
  private void
  readRequiredPlugins()
    throws PipelineException
  {
    File[] requiredPlugins = pRequiredPluginsPath.toFile().listFiles();

    if(requiredPlugins == null || requiredPlugins.length == 0)
      throw new PipelineException
	(pRequiredPluginsPath + " does not contain any required plugins GLUE files");

    for(int i = 0 ; i < requiredPlugins.length ; i++) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
        "Reading " + requiredPlugins[i]);

      if(requiredPlugins[i].isFile()) {
	MappedSet<PluginType,PluginID> plugins = null;

        try {
	  plugins = (MappedSet<PluginType,PluginID>)
	    GlueDecoderImpl.decodeFile("RequredPlugins", requiredPlugins[i]);

	  for(PluginType ptype : plugins.keySet()) {
	    for(PluginID pid : plugins.get(ptype)) {
	      String vendor = pid.getVendor();

	      pPluginStatus.put(ptype, pid, PluginStatus.Missing);

	      pRequiredCount++;
	      pMissingCount++;

	      pVendorPlugins.addPlugin(ptype, pid);
	    }
	  }
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }
  }

  /**
   * After each successful installation of a plugin save that vendor's plugins 
   * in a GLUE file.  This ensures that required plugins GLUE files are always up 
   * to date.
   * 
   * @param vendor
   *   The plugin vendor.
   * 
   * @param pset
   *   The MappedSet of PluginType and PluginID.
   */
  private void
  writeRequiredPlugins
  (
    String vendor, 
    MappedSet<PluginType,PluginID> plugins
  )
    throws PipelineException
  {
    Path requiredPluginsPath = 
      new Path(pRequiredPluginsPath, vendor);

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "Writing " + requiredPluginsPath);

    {
      File requiredPluginsFile = requiredPluginsPath.toFile();

      if(requiredPluginsFile.exists()) {
	if(!requiredPluginsFile.delete())
          throw new PipelineException
            ("Unable to remove the old installed plugins file " + 
	     "(" + requiredPluginsFile + ")!");
      }
    }

    try {
      GlueEncoderImpl.encodeFile
	("RequiredPlugins", plugins, requiredPluginsPath.toFile());
    }
    catch(GlueException ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
         "Error saving the installed plugins list = " + ex);
      
      throw new PipelineException(ex);
    }
    finally {
      LogMgr.getInstance().flush();
    }
  }

  /**
   *  Get the state of the PluginMgr loaded plugins.
   *  This will return false if the list of installed plugins are not all loaded.
   *  The IT group can install the missing plugins using plplugin, when all are installed
   *  the flag is flipped.
   *
   *  This flag is to ensure that other servers do not get an incomplete set of plugins
   *  which can generate errors.
   */
  public boolean
  isUpToDate()
  {
    return pUpToDate.get();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a Jim title string.  Used while iterating through all the PluginType enums.
   * All strings should be NOT null and greater than zero length, but still doing the check
   * using the garbage in garbage out scheme.
   */
  public String
  title
  (
   String title
  )
  {
    if(title == null)
      return title;

    if(title.length() == 0)
      return title;

    StringBuilder buf = new StringBuilder();

    char[] cs = title.toCharArray();

    buf.append(" ");
    buf.append(" ");
    buf.append(cs[0]);

    for(int i = 1 ; i < cs.length ; i++) {
      buf.append(" ");
      if(Character.isUpperCase(cs[i])) {
	buf.append(" ");
	buf.append(" ");
	buf.append(cs[i]);
      }
      else {
	buf.append(Character.toUpperCase(cs[i]));
      }
    }

    return buf.toString();
  }

  /**
   * Generate a string consisting the the given character repeated N number of times.
   */ 
  public String
  repeat
  (
   char c,
   int size
  ) 
  {
    StringBuilder buf = new StringBuilder();
    int wk;
    for(wk=0; wk<size; wk++) 
      buf.append(c);
    return buf.toString();
  }

  /**
   * Generate a horizontal bar.
   */ 
  public String
  bar
  (
   int size
  ) 
  {
    return repeat('-', size);
  }

  /**
   * Generate a horizontal title bar.
   */ 
  public String
  tbar
  (
   int size
  ) 
  {
    return repeat('=', size);
  }

  /**
   * Pad the given string so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str, 
   char c,
   int size
  ) 
  {
    return (str + repeat(c, Math.max(0, size - str.length())));
  }

  /**
   * Pad the given string with spaces so that it is at least N characters long.
   */ 
  public String
  pad
  (
   String str,
   int size
  ) 
  {
    return pad(str, ' ', size);
  }

  /**
   * Generate N spaces. 
   */ 
  public String
  pad
  (
   int size
  ) 
  {
    return repeat(' ', size);
  }

  /**
   * Left pad the given string so that it is at least N characters long.
   */ 
  public String
  lpad
  (
   String str, 
   char c,
   int size
  ) 
  {
    return (repeat(c, Math.max(0, size - str.length())) + str);
  }

  /**
   * Left pad the given string with spaces so that it is at least N characters long.
   */ 
  public String
  lpad
  (
   String str,
   int size
  ) 
  {
    return lpad(str, ' ', size);
  }

  /**
   * Line wrap the given String at word boundries.
   */ 
  public String
  wordWrap
  (
   String str,
   int indent, 
   int size
  ) 
  {
    if(str.length() + indent < size) 
      return str;

    StringBuilder buf = new StringBuilder();
    String words[] = str.split("\\p{Blank}");
    int cnt = indent;
    int wk;
    for(wk=0; wk<words.length; wk++) {
      int ws = words[wk].length();
      if(ws > 0) {
	if((size - cnt - ws) > 0) {
	  buf.append(words[wk]);
	  cnt += ws;
	}
	else {
	  buf.append("\n" + repeat(' ', indent) + words[wk]);
	  cnt = indent + ws;
	}

	if(wk < (words.length-1)) {
	  if((size - cnt) > 0) {
	    buf.append(' ');
	    cnt++;
	  }
	  else {
	    buf.append("\n" + repeat(' ', indent));
	    cnt = indent;
	  }
	}
      }
    }

    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A loaded plugin class and the load cycle when the class was loaded.
   */ 
  private class
  Plugin
  {
    /*
    public
    Plugin
    (
     long cycleID, 
     String cname, 
     SortedSet<OsType> supports, 
     boolean underDevelopment,
     TreeMap<String,byte[]> contents
    )
    {
      this(cycleID, cname, supports, underDevelopment, contents, null);
    }
    */

    public 
    Plugin
    (
     long cycleID, 
     String cname, 
     SortedSet<OsType> supports, 
     boolean underDevelopment,
     TreeMap<String,byte[]> contents, 
     TreeMap<String,Long> resources
    )
    {
      pCycleID            = cycleID; 
      pClassName          = cname;
      pSupports           = new TreeSet<OsType>(supports);
      pIsUnderDevelopment = underDevelopment;
      pContents           = contents; 

      pResources = new TreeMap<String,Long>();

      if(resources != null)
	pResources.putAll(resources);
    }


    public long
    getCycleID()
    {
      return pCycleID; 
    }
    
    public String
    getClassName() 
    {
      return pClassName; 
    }

    public TreeSet<OsType>
    getSupports()
    {
      return pSupports;
    }

    public boolean 
    isUnderDevelopment()
    {
      return pIsUnderDevelopment;
    }

    public TreeMap<String,byte[]>
    getContents() 
    {
      return pContents; 
    }

    public TreeMap<String,Long>
    getResources()
    {
      return pResources;
    }

    private long                    pCycleID; 
    private String                  pClassName;
    private TreeSet<OsType>         pSupports; 
    private boolean                 pIsUnderDevelopment; 
    private TreeMap<String,byte[]>  pContents; 
    private TreeMap<String,Long>    pResources;
  }

  /**
   * Stores all the details of plugin resource install.
   */
  private class
  PluginResource
  {
    public
    PluginResource
    (
     long sessionID, 
     PluginResourceInstallReq req
    )
    {
      pSessionID = sessionID;

      pRequestor = req.getRequestor();
      pClassFile = req.getClassFile();
      pClassName = req.getClassName();
      pVersionID = req.getVersionID();
      pExternal  = req.getExternal();
      pRename    = req.getRename();
      pDryRun    = req.getDryRun();

      pContents  = new TreeMap<String,byte[]>();
      pResources = new TreeMap<String,Long>();
      pChecksums = new TreeMap<String,byte[]>();

      if(req.getContents() == null)
	throw new IllegalArgumentException
	  ("There are no Java class files for plugin (" + pClassName + ")");

      pContents.putAll(req.getContents());

      if(req.getChecksums() == null)
	throw new IllegalArgumentException
	  ("There are no resource files for plugin (" + pClassName + ")");
      
      pChecksums.putAll(req.getChecksums());

      pChunkSizes = new TreeMap<String,Long>();

      for(String path : pChecksums.keySet()) {
	pChunkSizes.put(path, 0L);

	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Resource path " + 
	   "(" + path + ")");
      }
    }

    public void
    updateResourceChunk
    (
     String path, 
     int bytesRead, 
     Path rpath
    )
      throws PipelineException
    {
      if(!pChunkSizes.containsKey(path))
	throw new PipelineException
	  ("The resource (" + path + ") is NOT part of plugin (" + pClassName + ")!");

      long chunksize = pChunkSizes.get(path) + bytesRead;
      long filesize  = pResources.get(path);

      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Resource (" + path + ") " + 
	 "(" + chunksize + ") " + 
	 "(" + filesize  + ")");

      if(chunksize == filesize) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Recevied all bytes for (" + path + ")");

	byte[] checksum = null;
	try {
	  checksum = NativeFileSys.md5sum(rpath);
	}
	catch(IOException ex) {
	  throw new PipelineException(ex);
	}

	byte[] checksumFromRequest = pChecksums.get(path);

	if(!Arrays.equals(checksum, checksumFromRequest)) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "The resource (" + path + ") checksum is incorrect!");

	  throw new PipelineException
	    ("The resource (" + path + ") checksum is incorrect!");
	}

	pChunkSizes.remove(path);
      }
      else {
	pChunkSizes.put(path, chunksize);
      }
    }

    public boolean
    hasReceivedAllResources()
    {
      return pChunkSizes.isEmpty();
    }

    public void
    updateResources
    (
     TreeMap<String,Long> resources, 
     TreeMap<String,Long> localResources
    )
    {
      for(String path : resources.keySet()) {
	pResources.put(path, resources.get(path));
      }

      for(String path: localResources.keySet()) {
	pResources.put(path, localResources.get(path));
	pChunkSizes.remove(path);
      }

      for(String path : pResources.keySet()) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Resource path " + 
	   "(" + path + ") " + 
	   "(" + pResources.get(path) + ")");
      }
    }

    public long
    getSessionID()
    {
      return pSessionID;
    }

    public String
    getRequestor()
    {
      return pRequestor;
    }

    public File
    getClassFile()
    {
      return pClassFile;
    }

    public String
    getClassName()
    {
      return pClassName;
    }

    public VersionID
    getVersionID()
    {
      return pVersionID;
    }

    public TreeMap<String,byte[]>
    getContents()
    {
      //return Collections.unmodifiableSortedMap(pContents);
      return pContents;
    }

    public TreeMap<String,Long>
    getResources()
    {
      //return Collections.unmodifiableSortedMap(pResources);
      return pResources;
    }

    public TreeMap<String,byte[]>
    getChecksums()
    {
      //return Collections.unmodifiableSortedMap(pChecksums);
      return pChecksums;
    }

    public boolean
    getExternal()
    {
      return pExternal;
    }

    public boolean
    getRename()
    {
      return pRename;
    }

    public boolean
    getDryRun()
    {
      return pDryRun;
    }

    private long  pSessionID;
    private String  pRequestor;
    private File  pClassFile;
    private String  pClassName;
    private VersionID  pVersionID;
    private TreeMap<String,byte[]>  pContents;
    private TreeMap<String,Long>    pResources;
    private TreeMap<String,byte[]>  pChecksums;
    private boolean  pExternal;
    private boolean  pRename;
    private boolean  pDryRun;

    private TreeMap<String,Long>  pChunkSizes;
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The loaded plugins indexed by plugin vendor, name and revision number.
   */
  private 
  class PluginCache
    extends TripleMap<String,String,VersionID,Plugin>
  {
    /**
     * Construct a new cache.
     */
    public 
    PluginCache() 
    {
      super();
    }

    /**
     * Collect the plugin classes which have been loaded after the given load cycle. <P> 
     * 
     * @param cycleID
     *   The load cycle or <CODE>null</CODE> to collect all plugin classes.
     */ 
    private TripleMap<String,String,VersionID,Object[]>
    collectUpdated
    (
     Long cycleID
    ) 
    {
      TripleMap<String,String,VersionID,Object[]> updated = 
        new TripleMap<String,String,VersionID,Object[]>(); 
    
      if(cycleID == null) {
        for(String vendor : keySet()) {
          for(String name : get(vendor).keySet()) {
            for(VersionID vid : get(vendor).get(name).keySet()) {
              Plugin plg = get(vendor, name, vid);
              
              Object[] objs = new Object[4];
              objs[0] = plg.getClassName();
              objs[1] = plg.getContents();
              objs[2] = plg.getSupports();
	      objs[3] = plg.getResources();
              
              updated.put(vendor, name, vid, objs);
            }
          }
        }
      }
      else {  
        for(String vendor : keySet()) {
          for(String name : get(vendor).keySet()) {
            for(VersionID vid : get(vendor).get(name).keySet()) {
              Plugin plg = get(vendor, name, vid);
              
              if(cycleID < plg.getCycleID()) {
                Object[] objs = new Object[4];
                objs[0] = plg.getClassName();
                objs[1] = plg.getContents();
                objs[2] = plg.getSupports();
		objs[3] = plg.getResources();
                
                updated.put(vendor, name, vid, objs);
              }
            }
          }
        }
      }
      
      return updated;
    }

    /**
     * Add a newly loaded plugin to the cached plugin table.
     * 
     * @param plg 
     *   An instance of the loaded plugin class.
     * 
     * @param cname
     *   The full name of the plugin class.
     * 
     * @param contents
     *   The raw plugin class bytes indexed by class name.
     */ 
    private void 
    addPlugin
    (
     BasePlugin plg,
     String cname, 
     TreeMap<String,byte[]> contents, 
     TreeMap<String,Long> resources
    ) 
      throws PipelineException 
    {
      Plugin plugin = get(plg.getVendor(), plg.getName(), plg.getVersionID());
      if((plugin != null) && (!plugin.isUnderDevelopment())) 
        throw new PipelineException 
          ("Cannot install the plugin class (" + cname + ") because a previously installed " + 
           "version of the plugin (" + plg.getName() + ", v" + plg.getVersionID() + ", " + 
           plg.getVendor() + ") exists which is no longer under development!");
      
      put(plg.getVendor(), plg.getName(), plg.getVersionID(),
	new Plugin(pLoadCycleID, cname, 
                   plg.getSupports(), plg.isUnderDevelopment(), 
                   contents, resources));
    }

    static final long serialVersionUID = 6780638964799823468L;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A convenience class to manage MappedSet<PluginType,PluginID> indexed by vendor String.
   */
  private class
  VendorPluginsCache
  {
    public
    VendorPluginsCache()
    {
      pVendorPlugins = new TreeMap<String,MappedSet<PluginType,PluginID>>();
    }

    private void
    addPlugin
    (
     PluginType ptype, 
     PluginID pid
    )
    {
      String vendor = pid.getVendor();

      if(!pVendorPlugins.containsKey(vendor))
	pVendorPlugins.put(vendor, new MappedSet<PluginType,PluginID>());

      MappedSet<PluginType,PluginID> plugins = pVendorPlugins.get(vendor);

      plugins.put(ptype, pid);
    }

    private MappedSet<PluginType,PluginID>
    getPlugins
    (
     String vendor
    )
    {
      return pVendorPlugins.get(vendor);
    }

    private Set<String>
    vendors()
    {
      return Collections.unmodifiableSet(pVendorPlugins.keySet());
    }

    private TreeMap<String,MappedSet<PluginType,PluginID>>  pVendorPlugins;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * A convenience class to manage DoubleMap<PluginType,PluginID,TreeMap<String,byte[]>> 
   * used for responding to checksum requests.
   */
  private class
  ResourceChecksums
  {
    public
    ResourceChecksums()
    {
      pResourceChecksums = new DoubleMap<PluginType,PluginID,TreeMap<String,byte[]>>();
    }

    private void
    addChecksums
    (
     PluginType ptype, 
     PluginID pid, 
     TreeMap<String,byte[]> checksums
    )
    {
      pResourceChecksums.put(ptype, pid, checksums);
    }

    private TreeMap<String,byte[]>
    getChecksums
    (
     PluginType ptype, 
     PluginID pid
    )
    {
      return pResourceChecksums.get(ptype, pid);
    }

    private DoubleMap<PluginType,PluginID,TreeMap<String,byte[]>>  pResourceChecksums;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Rather than deal with a boolean to deal with the current type of plugin loading, 
   * this enum expresses all the types.
   */
  private
  enum PluginLoadType
  {
    /* Loading plugins from a previous Pipeline root directory to bootstrap the 
       required plugins process.  Plugins go through the validation but are not loaded. */
    Bootstrap, 

    /* Loading plugins during plpluginmgr startup.  Plugins go through the validation and
       are loaded if they found in the required plugins GLUE db. */
    Startup, 

    /* Install plugin.  Plugins are validated, loaded, added to the required plugins GLUE, 
       and the GLUE file is written to disk. */
    Install, 

    /* Go through the plugin validation but do not load the plugin. */
    DryRun;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file system directory creation lock.
   */
  private Object  pMakeDirLock;
  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The combined work groups and adminstrative privileges.
   */ 
  private AdminPrivileges  pAdminPrivileges; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The lock which protects access to the loaded plugin classes and load cycle counter.
   */ 
  private ReentrantReadWriteLock  pPluginLock;

  /**
   * The load cycle identifier.
   */ 
  private Long  pLoadCycleID;

  /**
   * The various types of loaded plugins.
   */
  private PluginCache  pEditors;
  private PluginCache  pActions; 
  private PluginCache  pComparators; 
  private PluginCache  pTools; 
  private PluginCache  pAnnotations; 
  private PluginCache  pArchivers; 
  private PluginCache  pMasterExts; 
  private PluginCache  pQueueExts; 
  private PluginCache  pKeyChoosers;
  private PluginCache  pBuilderCollection;
  
  /**
   * Menu layout information for the builders contained with a builder collection
   * indexed by plugin vendor, name and version.
   */ 
  private TripleMap<String,String,VersionID,LayoutGroup> pBuilderCollectionLayouts;

  /** 
   * The annotation creation and editing flags 
   * indexed by plugin vendor, name and version.
   */ 
  private TripleMap<String,String,VersionID,AnnotationPermissions> pAnnotationPermissions; 

  /** 
   * The contexts in which each annotation plugin can be used 
   * indexed by plugin vendor, name and version.
   */ 
  private TripleMap<String,String,VersionID,TreeSet<AnnotationContext>> pAnnotationContexts; 

  /**
   * The serialVersionUIDs of all loaded plugins used to test for conflicts.
   * 
   * The table contains plugin identifying strings indexed by the serialVersionUID of
   * the plugin.
   */ 
  private TreeMap<Long,String>  pSerialVersionUIDs;

  /**
   * Plugins loaded from a previous Pipeline plugins directory.  This is used to help 
   * studios bootstrap the inital required plugins GLUE file.
   *
   * Note that this is using a class called VendorPluginsCache, which is a wrapper 
   * to a TreeMap of MappedSet<PluginType,PluginID> keyed by String for the vendor and 
   * provides convience methods for dealing with plugins of a vendor.
   */
  private VendorPluginsCache  pBootstrapPlugins;

  /**
   * A table of all PluginStatus for all plugins keyed by PluginType and PluginID.
   * When required plugins are read during startup they are marked as Missing.
   */
  private DoubleMap<PluginType,PluginID,PluginStatus>  pPluginStatus;
  
  /**
   * All the required plugins.
   *
   * Note that this is a class called VendorPluginsCache, which is a wrapper 
   * to a TreeMap of MappedSet<PluginType,PluginID> keyed by String for the vendor and 
   * provides convience methods for dealing with plugins of a vendor.
   */
  private VendorPluginsCache  pVendorPlugins;

  /**
   * Keeps the count of all plugins read from required plugin GLUE files.
   */
  private int  pRequiredCount;

  /**
   * Keeps the count of all plugins loaded/installed.
   */
  private int  pLoadedCount;

  /**
   * Keeps the count of all missing plugins, when required plugins are read from 
   * GLUE files the plugins are considered to be missing.
   */
  private int  pMissingCount;

  /**
   * Keeps the count of all unknown plugins detected during startup.
   */
  private int  pUnknownCount;

  /**
   * Stores the answer to the question: Does PluginMgr have all 
   * required plugins installed?
   */
  private AtomicBoolean  pUpToDate;

  /**
   * The lock which protects access to the plugin resources session ID, and the
   * install of resources.
   */ 
  private ReentrantReadWriteLock  pResourceLock;

  /**
   * The root plugin scratch directory.
   */
  private Path  pPluginScratchPath;

  /**
   * The root directory where the required plugins GLUE files are stored.
   */
  private Path  pRequiredPluginsPath;

  /**
   * The unique session ID for each plugin install with resources.
   * Using an id allows for multiple plugin resource installs to occur 
   * with less locking.
   */
  private long  pSessionID;

  /**
   * Table of PluginResource objects indexed by a session ID.
   */
  private TreeMap<Long,PluginResource>  pPluginResourceInstalls;

  /**
   * Stores all the resource checksum tables for loaded plugins.
   *
   * Note that is a class called ResourceChecksums which is wrapper class to 
   * DoubleMap<PluginType,PluginID,TreeMap<String,byte[]>>
   */
  private ResourceChecksums  pResourceChecksums;

}

