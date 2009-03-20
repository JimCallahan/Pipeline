// $Id: PluginMgr.java,v 1.34 2009/03/20 03:10:38 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.lang.reflect.*;
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

    pPluginMgrDir = PackageInfo.sPluginMgrPath.toFile();

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

    {
      pBootstrapPlugins = new TreeMap<String,MappedSet<PluginType,PluginID>>();
      pVendorPlugins    = new TreeMap<String,MappedSet<PluginType,PluginID>>();

      pPluginStatus = new TripleMap<String,PluginType,PluginID,PluginStatus>();

      pUpToDate = new AtomicBoolean(false);
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
      for(String vname :pBootstrapPlugins.keySet()) {
	if(!vname.equals("Temerity")) {
	  MappedSet<PluginType,PluginID> plugins = pBootstrapPlugins.get(vname);

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

      for(String vname : pBootstrapPlugins.keySet()) {
        if(vname.equals("Temerity")) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Warning, 
             "For some reason Temerity plugins are in the bootstrap plugins hashtable, " + 
             "this is a problem but they are going to be ignored. If you see message in " + 
             "your log file please post the forum about this.");
        }
        else {
          try {
            writeRequiredPlugins(vname, PluginLoadType.Bootstrap);
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

    for(String vendor : pBootstrapPlugins.keySet()) {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" + 
         " " + vendor + " Plugins\n" + 
         pad(maxLength) + "   required\n" +
         pad(maxLength) + "   --------");

      int total = 0;
      MappedSet<PluginType,PluginID> pset = pBootstrapPlugins.get(vendor);
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

    for(String vendor : pPluginStatus.keySet()) {

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         bar(80) + "\n" + 
         " " + vendor + " Plugins\n" + 
         pad(maxLength) + "   required  loaded  unknown\n" +
         pad(maxLength) + "   -------------------------");

      int totalR = 0;
      int totalL = 0;
      int totalU = 0;
      for(PluginType ptype : PluginType.all()) {
	TreeMap<PluginID,PluginStatus> plugins = pPluginStatus.get(vendor, ptype);

	if(plugins == null)
	  continue;

        int rcnt = 0;
	int lcnt = 0;
	int ucnt = 0;

	for(PluginID pid : plugins.keySet()) {
	  PluginStatus pstat = plugins.get(pid);

	  switch(pstat) {
	    case Missing:
	      {
		rcnt++;
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

        if((rcnt > 0) || (lcnt > 0) || (ucnt > 0)) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Info,
             lpad(ptype.toString(), maxLength) + " :  " + 
             lpad(Integer.toString(lcnt+rcnt), 5) + 
             lpad(Integer.toString(lcnt), 8) + 
             lpad(Integer.toString(ucnt), 9));
        }
          
        totalR += rcnt;
        totalL += lcnt;
        totalU += ucnt;
      }

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
         "\n" + lpad("TOTAL", maxLength) + " :  " + 
         lpad(Integer.toString(totalR+totalL), 5) + 
         lpad(Integer.toString(totalL), 8) + 
         lpad(Integer.toString(totalU), 9) + "\n");

      if(totalR > totalL) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Plg, LogMgr.Level.Info,
           pad(maxLength) + "   (" + (totalR-totalL) + " REQUIRED PLUGIN" + 
           (((totalR-totalL) > 1) ? "S" : "") + " MISSING!)\n"); 
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
         "All required plugins have now been loaded!  You may start all other Pipeline " + 
         "servers and begin normal operation.\n" + 
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

      /* Send the plugin status table in PluginUpdateRsp, but the TripleMap is keyed 
         by PluginType, Vendor string, and PluginID. */
      TripleMap<PluginType,String,PluginID,PluginStatus> pluginStatus = 
	new TripleMap<PluginType,String,PluginID,PluginStatus>();
      for(String vendor : pPluginStatus.keySet()) {
	for(PluginType ptype : pPluginStatus.keySet(vendor)) {
	  for(PluginID pid : pPluginStatus.keySet(vendor, ptype)) {
	    PluginStatus pstat = pPluginStatus.get(vendor, ptype, pid);
	    pluginStatus.put(ptype, vendor, pid, pstat);
	  }
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
   *   <CODE>PlugingCountRsp</CODE> if there are required plugins that need to be installed or
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
      TreeMap<String,byte[]> contents = req.getContents();

      /* load the class and cache the class bytes */ 
      pLoadCycleID++;
      loadPluginHelper(req.getClassFile(), cname, req.getVersionID(), contents, 
                       req.getExternal(), req.getRename(), 
		       (req.getDryRun() ? PluginLoadType.DryRun : PluginLoadType.Install)); 

      if(!req.getDryRun()) {
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

	if(pMissingCount > 0 || pUnknownCount > 0)
	  return new PluginCountRsp(timer, pMissingCount, pUnknownCount);
	else
	  return new SuccessRsp(timer);
      }
      /* end if(!rsp.getDryRun() */
      
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

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
    File[] fs = dir.listFiles();
    int wk;
    for(wk=0; wk<fs.length; wk++) {
      if(fs[wk].isFile()) {
	try {
	  File file = fs[wk];
	  String filename = file.getName();

	  /* Ignore all non java class or jar files.  This way resource files 
	     do not throw an exception when they do not conform to having a 
	     parent directory v#_#_#. */
	  if(!filename.endsWith(".class") && !filename.endsWith(".jar"))
	    continue;

	  VersionID vid = null;
	  try {
	    String dname = dir.getName();
	    if(dname.startsWith("v")) {
	      String vstr = dname.substring(1, dname.length()).replace('_','.');
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
	       "the plugin version!  Ignoring plugin file (" + file + ").\n" + 
	       ex.getMessage());
	  }

	  loadPlugin(root, file, pluginLoadType);

	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	     "classdir (" + root + ") pluginfile (" + file + ")");
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Warning,
	     ex.getMessage());
	}	
      }
      else if(fs[wk].isDirectory()) {
	loadBelow(root, fs[wk], pluginLoadType);
      }
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
      TreeMap<String,byte[]> contents = new TreeMap<String,byte[]>(); 
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

      loadPluginHelper(pluginfile, cname, pkgID, contents, true, true, pluginLoadType);
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
   boolean external, 
   boolean rename, 
   PluginLoadType pluginLoadType
  ) 
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents);

    try {
      switch(pluginLoadType) {
	case Bootstrap:
	case DryRun:
	  {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Plg, LogMgr.Level.Finer,
	       "Validating: " + cname);
	  }
	  break;
	case Startup:
	case Install:
	  {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Plg, LogMgr.Level.Finer,
	       "Loading: " + cname);
	  }
	  break;
	default:
	  throw new PipelineException
	    ("Unknown PluginLoadType (" + pluginLoadType + ")!");
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

      if(pluginLoadType == PluginLoadType.Bootstrap) {
	String vendor = plg.getVendor();

	/* Ignore Temerity plugins during bootstrap mode */
	if(!vendor.equals("Temerity")) {
	  PluginType ptype = plg.getPluginType();
	  PluginID   pid   = plg.getPluginID();

	  if(!pBootstrapPlugins.containsKey(vendor))
	    pBootstrapPlugins.put(vendor, new MappedSet<PluginType,PluginID>());

	  pBootstrapPlugins.get(vendor).put(ptype, pid);
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



      if(plg instanceof BaseEditor) { 
        if(!rename) 
          checkForPluginAliasing(pEditors, cname, plg); 

	checkLoadPlugin(pEditors, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseAction) {
        if(!rename) 
          checkForPluginAliasing(pActions, cname, plg); 

        BaseAction action = (BaseAction) plg;
        if(action.supportsSourceParams() && (action.getInitialSourceParams() == null))
          throw new PipelineException
            ("The action plugin (" + cname + ") claims to support source parameters, but " + 
             "does not actually create any source parameters."); 

	checkLoadPlugin(pActions, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseComparator) {
        if(!rename) 
          checkForPluginAliasing(pComparators, cname, plg); 

	checkLoadPlugin(pComparators, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseTool) {
        if(!rename) 
          checkForPluginAliasing(pTools, cname, plg); 

	checkLoadPlugin(pTools, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseAnnotation) {
        if(!rename) 
          checkForPluginAliasing(pAnnotations, cname, plg); 

	if(checkLoadPlugin(pAnnotations, plg, cname, contents, pluginLoadType)) {
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

	checkLoadPlugin(pArchivers, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseMasterExt) {
        if(!rename) 
          checkForPluginAliasing(pMasterExts, cname, plg); 

	checkLoadPlugin(pMasterExts, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseQueueExt) {
        if(!rename) 
          checkForPluginAliasing(pQueueExts, cname, plg); 

	checkLoadPlugin(pQueueExts, plg, cname, contents, pluginLoadType);
      }
      else if(plg instanceof BaseKeyChooser) {
        if(!rename) 
          checkForPluginAliasing(pKeyChoosers, cname, plg); 

	checkLoadPlugin(pKeyChoosers, plg, cname, contents, pluginLoadType);
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
                if (bLayout == null)
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
	if(checkLoadPlugin(pBuilderCollection, plg, cname, contents, pluginLoadType)) {
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
   * Perform plugin verification and loading based on the PluginLoadType.
   * 
   * @param plgCache
   *   The PluginCache for the plugin to load.
   * 
   * @param plg
   *   The plugin to be loaded.
   * 
   * @param cname
   *   The full class name.
   * 
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   * 
   * @param pluginLoadType
   *   Which rules the plugin loading process should follow.
   * 
   * @return
   *   true  if the plugin was   successfully loaded.
   *   false if the plugin was unsuccessfully loaded.
   */
  public boolean
  checkLoadPlugin
  (
   PluginCache plgCache, 
   BasePlugin plg, 
   String cname, 
   TreeMap<String,byte[]> contents, 
   PluginLoadType pluginLoadType
  )
    throws PipelineException
  {
    boolean loadPlugin = false;

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
       "The plugin load type (" + pluginLoadType + ")");

    PluginType ptype = plg.getPluginType();
    PluginID   pid   = plg.getPluginID();

    String vendor = pid.getVendor();
    String name   = pid.getName();
    VersionID vid = pid.getVersionID();

    switch(pluginLoadType) {
      case Startup:
	{
	  /* In Startup mode first check if the plugin is in the required plugins table. 
	     If the plugin is in the table then load the plugin, else make an entry in 
	     the unknown plugins table. */

	  PluginStatus pstat = pPluginStatus.get(vendor, ptype, pid);

	  if(pstat == null) {
	    pPluginStatus.put(vendor, ptype, pid, PluginStatus.Unknown);
	    pUnknownCount++;
	  }
	  else {
	    if(pstat == PluginStatus.Missing)
	      loadPlugin = true;
	  }
	}
	break;

      case Install:
	{
	  /* Only Temerity developers are allowed to install plugins using Temerity 
	     as the vendor.  This is expressed by the local vendor.  It is possible 
	     for any studio to use Temerity as the local plugin vendor name, perhaps 
	     some further restriction can be placed on the usage of the Temerity 
	     vendor.  For now I will assume that studios will use their own local 
	     plugin vendor name. */

	  if(!vendor.equals("Temerity")) {
	    loadPlugin = true;
	  }
	  else if(PackageInfo.sLocalVendor.equals("Temerity")) {
	    loadPlugin = true;
	  }
	  else {
	    throw new PipelineException
	      ("Local Vendor (" + PackageInfo.sLocalVendor + ") " + 
	       "cannot install plugins from the Temerity vendor!");
	  }
	}
	break;

      case DryRun:
	{
	  /* Dry run mode allows for the plugin to run the gamut of verification but 
	     does not load the plugin into a PluginCache. */

	  Plugin plugin = plgCache.get(vendor, name, vid);
	  if((plugin != null) && (!plugin.isUnderDevelopment()))
	    throw new PipelineException 
	      ("Cannot install the plugin class (" + cname + ") " + 
	       "because a previously installed version of the plugin " + 
	       "(" + plg.getName() + ", v" + plg.getVersionID() + ", " + 
	       plg.getVendor() + ") exists which is no longer under development!");
	}
	break;

      default:
	throw new PipelineException
	  ("Unknown PluginLoadType (" + pluginLoadType + ")!");
    }

    if(loadPlugin) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	 "Adding the plugin (" + cname + ") to the " + ptype + " cache.");

      plgCache.addPlugin(plg, cname, contents);

      {
	PluginStatus pstat = pPluginStatus.get(vendor, ptype, pid);

	/* Plugin status accounting is expressed through pPluginStatus, 
	   for each plugin indexed by vendor, plugin type and plugin id there 
	   is a PluginStatus enum that describes the state of the plugin.  
	   When the plugin is initially read from required GLUE files the 
	   status is Missing and the pMissingCount is incremented.  When a plugin
	   is successfully added to a plugin cache the pLoadedCount is incremented 
	   and pMissingCount and pUnknownCount is decremented. */

	/* If the current pstat is null that means the plugin being added to the 
	   plugin cache is being installed and new to PluginMgr. */
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

	Plugin plugin = plgCache.get(plg.getVendor(), plg.getName(), plg.getVersionID());

	/* After a plugin is loaded, determine if the plugin is under development and 
	   change the PluginStatus appropiately. */
	if(plugin.isUnderDevelopment())
	  pPluginStatus.put(vendor, ptype, pid, PluginStatus.UnderDevelopment);
	else
	  pPluginStatus.put(vendor, ptype, pid, PluginStatus.Permanent);

	if(!pVendorPlugins.containsKey(vendor))
	  pVendorPlugins.put(vendor, new MappedSet<PluginType,PluginID>());

	pVendorPlugins.get(vendor).put(ptype, pid);
      }
      
      if(pluginLoadType == PluginLoadType.Install) {
	/* After every successful plugin install write a new required plugins GLUE 
	   file to keep the db as up to date as possible.  A future optimization 
	   could be to incrementally or patch the GLUE file if this becomes a 
	   performance bottleneck, which I doubt will ever be the case. */

	LogMgr.getInstance().log
	  (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
	   "Writing " + vendor + " required plugins GLUE file to disk.");

	writeRequiredPlugins(vendor, PluginLoadType.Install);
      }
    }

    return loadPlugin;
  }

  /*
    Read all required-plugins files from the pluginmgr root directory.
  */
  private void
  readRequiredPlugins()
    throws PipelineException
  {
    File root = new File(pPluginMgrDir, "plugins/required");
    File[] requiredPlugins = root.listFiles();

    if(requiredPlugins == null || requiredPlugins.length == 0)
      throw new PipelineException
	(root + " does not contain any required plugins GLUE files");

    for(int i = 0 ; i < requiredPlugins.length ; i++) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
        "Reading " + requiredPlugins[i]);

      if(!requiredPlugins[i].isDirectory()) {
        MappedSet<PluginType,PluginID> pset = null;

        try {
          pset = (MappedSet<PluginType,PluginID>)
            GlueDecoderImpl.decodeFile("RequiredPlugins", requiredPlugins[i]);

          for(PluginType ptype : pset.keySet()) {
	    for(PluginID pid : pset.get(ptype)) {
	      String vendor = pid.getVendor();

	      pPluginStatus.put(vendor, ptype, pid, PluginStatus.Missing);

	      pRequiredCount++;
	      pMissingCount++;
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
   * @param pluginLoadType
   *   Which rules the plugin writing process should follow.
   */
  private void
  writeRequiredPlugins
  (
    String vendor, 
    PluginLoadType pluginLoadType
  )
    throws PipelineException
  {
    MappedSet<PluginType,PluginID> plugins = null;
    boolean bootstrapMode = false;

    switch(pluginLoadType) {
      case Bootstrap:
	{
	  plugins = pBootstrapPlugins.get(vendor);
	  bootstrapMode = true;
	}
	break;

      case Install:
	{
	  plugins = pVendorPlugins.get(vendor);
	}
	break;

      default:
	throw new PipelineException
	  ("Invalid PluginLoadType (" + pluginLoadType + ") for writeRequiredPlugins!");
    }

    /* this error should not occur, since the only times required plugins files 
       are written are right after a plugin has been successfully install or 
       during the bootstrap process.  In both cases the Vendor string should be 
       valid and there should be plugins. */

    if(plugins == null)
      throw new PipelineException
        ("The Vendor specified does not have an entry in the " + 
         (bootstrapMode ? "bootstrap" : "Vendor") + " table!");

    try {
      File file = new File(pPluginMgrDir, "plugins/required/"  + vendor);

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
        "Writing " + file);

      if(file.exists()) {
        if(!file.delete())
          throw new PipelineException
            ("Unable to remove the old installed plugins file (" + file + ")!");
      }
      
      GlueEncoderImpl.encodeFile("RequiredPlugins", plugins, file);
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
      pCycleID            = cycleID; 
      pClassName          = cname;
      pSupports           = new TreeSet<OsType>(supports);
      pIsUnderDevelopment = underDevelopment;
      pContents           = contents; 
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

    private long                    pCycleID; 
    private String                  pClassName;
    private TreeSet<OsType>         pSupports; 
    private boolean                 pIsUnderDevelopment; 
    private TreeMap<String,byte[]>  pContents; 
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
              
              Object[] objs = new Object[3];
              objs[0] = plg.getClassName();
              objs[1] = plg.getContents();
              objs[2] = plg.getSupports();
              
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
                Object[] objs = new Object[3];
                objs[0] = plg.getClassName();
                objs[1] = plg.getContents();
                objs[2] = plg.getSupports();
                
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
     * 
     * @param validatePluginOnly
     *   Whether to just perform the plugin validation and not load the plugin.
     */ 
    private void 
    addPlugin
    (
     BasePlugin plg,
     String cname, 
     TreeMap<String,byte[]> contents
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
                     contents));
    }

    static final long serialVersionUID = 6780638964799823468L;
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
   */
  private TreeMap<String,MappedSet<PluginType,PluginID>>  pBootstrapPlugins;

  /**
   * A table of all plugins properly loaded/installed.
   *
   * Vendor string is the key to MappedSet of PluginType and PluginID.
   */
  private TreeMap<String,MappedSet<PluginType,PluginID>>  pVendorPlugins;

  /**
   * A table of all PluginStatus for all plugins.  When required plugins are read during 
   * startup they are marked as Missing.
   * 
   * The 3 keys are Vendor string, PluginType and PluginID keyed to a PluginStatus.
   */
  private TripleMap<String,PluginType,PluginID,PluginStatus>  pPluginStatus;

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
   * Does PluginMgr have all required plugins installed?
   */
  private AtomicBoolean  pUpToDate;

  /**
   * The root directory where required plugins GLUE files are located.
   */
  private File  pPluginMgrDir;

}

