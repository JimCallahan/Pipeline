// $Id: PluginMgr.java,v 1.27 2009/02/11 16:32:39 jlee Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.jar.*;
import java.util.zip.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.LogMgr.*;
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
      
      pEditors     = new PluginCache();
      pActions     = new PluginCache();
      pComparators = new PluginCache();
      pTools       = new PluginCache();
      pAnnotations = new PluginCache();
      pArchivers   = new PluginCache();
      pMasterExts  = new PluginCache();
      pQueueExts   = new PluginCache();
      pKeyChoosers = new PluginCache();
      pBuilderCollection = new PluginCache();
      
      pBuilderCollectionLayouts = 
        new TripleMap<String, String, VersionID, LayoutGroup>();
      
      pAnnotationPermissions = 
        new TripleMap<String, String, VersionID, AnnotationPermissions>();
      
      pSerialVersionUIDs = new TreeMap<Long,String>(); 
    }

    /* Initialize and setup the required, loaded and unknown plugins data 
        structures. */
    {
      pRequiredPluginCount = 0;

      pRequiredPlugins = new MappedSet<PluginType,PluginID>();
      pLoadedPlugins   = new MappedSet<PluginType,PluginID>();
      pUnknownPlugins  = new MappedSet<PluginType,PluginID>();

      pBootstrapPlugins = new TreeMap<String,MappedSet<PluginType,PluginID>>();
      pVendorPlugins    = new TreeMap<String,MappedSet<PluginType,PluginID>>();

      pUpToDate = new AtomicBoolean(false);
    }

    /* If the bootstrap option is used, walk the Pipeline plugins directory and 
        collect the non Temerity plugins. Then write a GLUE file for each Vendor.  
        The bootstrap process performs all the checking that loadAllPlugin does, but 
        does not load the plugins.  The bootstrap flag  allows for the same code to be 
        used. */
    if(bootstrapDir != null) {
      if(!bootstrapDir.isDirectory())
        throw new IllegalStateException
          ("The given required plugins bootstrap directory (" + bootstrapDir +
          ") does not exist!");

      File bootstrapPluginsDir = new File(bootstrapDir, "plugins");

      /* The PluginMgrOptsParser performs the following 2 checks, ensures the supplied 
          Pipeline root directory exists and that there is a plugin directory under it.  
          So if these exceptions are thrown then somehow the PluginMgrOptParser's checks 
          were bypassed. */
      if(!bootstrapPluginsDir.isDirectory())
        throw new IllegalStateException
          ("[PluginMgr] The given required plugins bootstrap directory (" + bootstrapDir + 
          ") does not contain a plugins directory!  Please check that you are using " + 
          "the correct previous Pipeline directory or do not use --bootstrap=(previous " + 
          "Pipeline root directory)");

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info, 
        "[PluginMgr] In required plugins bootstrap mode using (" + bootstrapDir + ") " + 
        "as the root of the previous Pipeline version.");

      loadAllPlugins(bootstrapPluginsDir, true);

      for(String vname : pBootstrapPlugins.keySet()) {
        if(vname.equals("Temerity")) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Info, 
            "For some reason Temerity plugins are in the bootstrap plugins " + 
            "hashtable, this is a problem but they are going to be ignored.  " + 
            "If you see message in your log file please post the forum about this.");
        }
        else {
          try {
            writeRequiredPlugins(vname, true);
          }
          catch(PipelineException ex) {
            throw new IllegalStateException(ex);
          }
        }
      }
    }

    /* Load the required plugins from glue files. */
    try {
      readRequiredPlugins();
    }
    catch(PipelineException ex) {
      throw new IllegalStateException("Error reading required plugins GLUE files!");
    }

    //loadAllPlugins();
    loadAllPlugins(PackageInfo.sPluginsPath.toFile(), false);
    checkRequiredPlugins();
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
      for (String name : builders.keySet()) {
        for (String vendor : builders.keySet(name)) {
          for (VersionID id : builders.keySet(name, vendor)) {
            groups.put(name, vendor, id, pBuilderCollectionLayouts.get(name, vendor, id));
          }
        }
      }

      TripleMap<String, String, VersionID, Object[]> annotations = 
        pAnnotations.collectUpdated(cycleID);
      TripleMap<String, String, VersionID, AnnotationPermissions> permissions =
        new TripleMap<String, String, VersionID, AnnotationPermissions>();
      for (String name : annotations.keySet()) {
        for (String vendor : annotations.keySet(name)) {
          for (VersionID id : annotations.keySet(name, vendor)) {
            permissions.put(name, vendor, id, pAnnotationPermissions.get(name, vendor, id));
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
                                 permissions);
    }
    finally {
      pPluginLock.readLock().unlock();
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets any required plugins that need to be installed and unregistered plugins that 
   * have been detected at startup.
   *
   * @return
   *   <CODE>PluginListRequiredRsp<CODE> if sucessful or 
   *   <CODE>FailureRsp</CODE> if unable to get the list of required plugins.
   */
  public Object
  listRequired
  ()
  {
    TaskTimer timer = new TaskTimer();

    timer.aquire();
    pPluginLock.readLock().lock();
    try {
      timer.resume();

      MappedSet<PluginType,PluginID> requiredPlugins = 
        new MappedSet<PluginType,PluginID>();

      for(PluginType plgType : pRequiredPlugins.keySet()) {
        for(PluginID plgID : pRequiredPlugins.get(plgType)) {
          requiredPlugins.put(plgType, plgID);
        }
      }

      MappedSet<PluginType,PluginID> unknownPlugins = 
        new MappedSet<PluginType,PluginID>();

      for(PluginType plgType : pUnknownPlugins.keySet()) {
        for(PluginID plgID : pUnknownPlugins.get(plgType)) {
          unknownPlugins.put(plgType, plgID);
        }
      }

      return new PluginListRequiredRsp(timer, requiredPlugins, unknownPlugins);
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
      loadPluginHelper(
        req.getClassFile(), cname, req.getVersionID(), 
        contents, 
        req.getExternal(), req.getRename(), false); 

      /* save the plugin class bytes in a file */ 
      Path path = null; 
      try {
  boolean isJar = (contents.size() > 1);
  path = new Path(
      PackageInfo.sPluginsPath, 
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
      String ename = (key.replace('.', '/') + ".class");
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

      /* Now that the plugin has been tested and saved to disk we can now 
          do the required plugins accounting and update the GLUE file. */
      PluginClassLoader loader = new PluginClassLoader(contents);

      try {
        Class cls = loader.loadClass(cname);
        BasePlugin plg = (BasePlugin)cls.newInstance();
        String vendor = plg.getVendor();

        /* Only non-Temerity plugins can be installed */
        if(!vendor.equals("Temerity")) {
          addRequiredPlugin(plg);
          writeRequiredPlugins(plg.getVendor(), false);
        }
        else {
          LogMgr.getInstance().log
            (LogMgr.Kind.Plg, LogMgr.Level.Severe, 
            "Cannot install plugins from the vendor Temerity!");
        }
      }
      /* These exceptions should not be raised since the loadPluginHelper method 
          would have done the same class loading and instantiation. */
      catch(ClassNotFoundException ex) {
        throw new PipelineException(ex);
      }
      catch(InstantiationException ex) {
        throw new PipelineException(ex);
      }
      catch(IllegalAccessException ex) {
        throw new PipelineException(ex);
      }

      int requiredPluginCount = checkRequiredPlugins();
      int unknownPluginCount  = checkUnknownPlugins();

      /* The plugin has been successfully installed at this point but if there are
          required plugins that need to be installed return a PluginCount response 
          rather than Success response.  This is not an error, only adds plugins counts 
          to inform the user of the state of required plugins. */
      if(requiredPluginCount > 0 || unknownPluginCount > 0) {
        return new PluginCountRsp(timer, requiredPluginCount, unknownPluginCount);
      }
      else {
        return new SuccessRsp(timer);
      }
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
   * @param bootstrap
   *   Whether in required plugins bootstrap mode.
   */ 
  private void 
  loadAllPlugins 
  (
    File root, 
    boolean bootstrap
  )
  {
    File[] dirs = root.listFiles();
    int wk;
    for(wk=0; wk<dirs.length; wk++) {
      if(dirs[wk].isDirectory()) 
  loadBelow(root, dirs[wk], bootstrap);
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
   * @param bootstrap
   *   Whether in required plugins bootstrap mode.
   */
  private void 
  loadBelow
  (
   File root, 
   File dir, 
   boolean bootstrap
  ) 
  {
    File[] fs = dir.listFiles();
    int wk;
    for(wk=0; wk<fs.length; wk++) {
      if(fs[wk].isFile()) {
  try {
    File file = fs[wk];

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

    loadPlugin(root, file, bootstrap);
  }
  catch(PipelineException ex) {
    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Warning,
       ex.getMessage());
  }
      }
      else if(fs[wk].isDirectory()) {
  loadBelow(root, fs[wk], bootstrap);
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
   * @param bootstrap
   *   Whether in required plugins bootstrap mode.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPlugin
  (
    File classdir, 
    File pluginfile, 
    boolean bootstrap
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
    String jcname = path.substring(0, path.length() - 6).replace('/', '.');

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

      loadPluginHelper(pluginfile, cname, pkgID, contents, true, true, bootstrap);
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
   * @param bootstrap
   *   Whether in required plugins bootstrap mode.
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
    boolean bootstrap
  ) 
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents);
    try {
      if(!bootstrap)
  LogMgr.getInstance().log
    (LogMgr.Kind.Plg, LogMgr.Level.Finer,
    "Loading: " + cname);

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

      /* Ignore Temerity plugins during bootstrap mode */
      if(bootstrap && plg.getVendor().equals("Temerity"))
        return;

      if(!plg.getVersionID().equals(pkgID)) 
  throw new PipelineException
    ("The revision number (v" + plg.getVersionID() + ") of the instantiated " + 
     "plugin class (" + cname + ") does not match the revision number " + 
     "(" + pkgID + ") derived from the name of the directory containing the " + 
     "class file (" + pluginfile + ")!");
      
      if(plg.getSupports().isEmpty()) 
  throw new PipelineException
    ("The plugin class (" + cname + ") does not support execution under any " + 
     "type of operating system!  At least one OS must be supported.");

      {
  ObjectStreamClass osc = ObjectStreamClass.lookup(cls);
  if(osc == null) 
    throw new PipelineException
      ("The plugin (" + cname + ") does not implement Serializable!");

  long serialID = osc.getSerialVersionUID();
  if(serialID == 0L) 
    throw new PipelineException
      ("No member (serialVersionUID) was declared for plugin (" + cname + ")!");

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


      if(plg instanceof BaseEditor) { 
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pEditors, cname, plg);

  if(checkRequiredPlugin(plg))
    pEditors.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseAction) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pActions, cname, plg); 

  BaseAction action = (BaseAction) plg;
  if(action.supportsSourceParams() && (action.getInitialSourceParams() == null))
    throw new PipelineException
      ("The action plugin (" + cname + ") claims to support source parameters, but " + 
        "does not actually create any source parameters.");

  if(checkRequiredPlugin(plg))
    pActions.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseComparator) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pComparators, cname, plg);

  if(checkRequiredPlugin(plg))
    pComparators.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseTool) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pTools, cname, plg);

  if(checkRequiredPlugin(plg))
    pTools.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseAnnotation) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pAnnotations, cname, plg);

  if(checkRequiredPlugin(plg))
    pAnnotations.addPlugin(plg, cname, contents);

  BaseAnnotation annot = (BaseAnnotation) plg;
  AnnotationPermissions permissions = 
    new AnnotationPermissions(
      annot.isUserAddable(), 
      annot.isUserRemovable());
  pAnnotationPermissions.put
    (plg.getVendor(), plg.getName(), plg.getVersionID(), permissions);
        }
      }

      else if(plg instanceof BaseArchiver) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pArchivers, cname, plg);

  if(checkRequiredPlugin(plg))
    pArchivers.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseMasterExt) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pMasterExts, cname, plg);

  if(checkRequiredPlugin(plg))
    pMasterExts.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseQueueExt) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pQueueExts, cname, plg);

  if(checkRequiredPlugin(plg))
    pQueueExts.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseKeyChooser) {
        if(!bootstrap) {
  if(!rename) 
    checkForPluginAliasing(pKeyChoosers, cname, plg);

  if(checkRequiredPlugin(plg))
    pKeyChoosers.addPlugin(plg, cname, contents);
        }
      }

      else if(plg instanceof BaseBuilderCollection) {
        if(!bootstrap) {
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

  if(checkRequiredPlugin(plg))
    pBuilderCollection.addPlugin(plg, cname, contents);

  LayoutGroup group = collection.getLayout();
  pBuilderCollectionLayouts.put
    (plg.getVendor(), plg.getName(), plg.getVersionID(), group);
        }
      }

      else {
  throw new PipelineException
    ("The class file (" + pluginfile + ") does not contain a Pipeline plugin!");
      }

      /* If in bootstrap mode we bypass the loading of plugins but will record 
          a set of required plugins from the previous Pipeline plugins directory.  
          Ignore all Temerity plugins as they will be updated by the latest Pipeline 
          rpm install. Store all plugins in pBootstrapPlugins.  This will written to 
          disk prior to doing the normal loading of plugins. */
      if(bootstrap) {
        PluginType plgType = plg.getPluginType();
        PluginID   plgID   = plg.getPluginID();

        String vname = plgID.getVendor();

        if(!pBootstrapPlugins.containsKey(vname))
          pBootstrapPlugins.put(vname, new MappedSet<PluginType,PluginID>());

        pBootstrapPlugins.get(vname).put(plgType, plgID);
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
         "as the plugin being installed, but it is implemented with a different Java class! " + 
         "The existing plugin's Java class is (" + plugin.getClassName() + ") while the " + 
         "new plugin is implemented by the (" + cname + ") Java class. This may " + 
         "be due to copying the source code from another plugin and forgetting to " + 
         "update the Name, VersionID and Vendor properties of the new plugin.\n" +
         "\n" + 
         "You can use the --rename option to plplugin(1) if you need to install " + 
         "plugins with different Java class names for the same plugin identifier.");
    }
  }

  /**
   * After a successful install of a plugin it should be added to the vendor plugins table 
   * so the complete set of plugins from the vendor can be saved to GLUE file.
   *
   * @param plg
   *   The installed plugin.
   */
  public void
  addRequiredPlugin
  (
    BasePlugin plg
  )
    throws PipelineException
  {
    String vendor = plg.getVendor();

    PluginType plgType = plg.getPluginType();
    PluginID   plgID   = plg.getPluginID();

    /* If the plugin was intially dectected as unknown and is being installed 
        the unknown status should be removed. */
    if(pUnknownPlugins.containsKey(plgType) && 
      pUnknownPlugins.get(plgType).contains(plgID)) {

      pUnknownPlugins.get(plgType).remove(plgID);
    }

    /* Newly installed, or reinstalled plugins should be added to the Vendor 
        plugins table. */
    if(!pVendorPlugins.containsKey(vendor))
      pVendorPlugins.put(vendor, new MappedSet<PluginType,PluginID>());

    pVendorPlugins.get(vendor).put(plgType, plgID);
  }

  /**
   * Every plugin found on disk is checked against the required plugins table.  
   * If a plugin is found in the required plugins table it is removed and placed into 
   * the loaded plugins table.  This way we can account for all plugins properly loaded 
   * and the required plugin that need to be installed.  Also, any plugin found on disk but 
   * not found in a required plugins GLUE file is not loaded and placed in the unknown 
   * plugins table.
   *
   * Returns a boolean to indicate whether a plugin has been found in the required plugins 
   * table.
   *
   * @param plg
   *   Plugin being loaded from disk.
   */
  private boolean
  checkRequiredPlugin
  (
    BasePlugin plg
  )
    throws PipelineException
  {
    if(pUpToDate.get())
      return true;

    boolean isRequiredPlugin = false;

    {
      PluginType plgType = plg.getPluginType();
      PluginID   plgID   = plg.getPluginID();

      if(pRequiredPlugins.containsKey(plgType) && 
        pRequiredPlugins.get(plgType).contains(plgID)) {

        pLoadedPlugins.put(plgType, plgID);

        pRequiredPlugins.get(plgType).remove(plgID);

        isRequiredPlugin = true;
      }
      else {
        pUnknownPlugins.put(plgType, plgID);
      }
    }

    int loadedPluginCount = 0;
    for(PluginType plgType : pLoadedPlugins.keySet())
      loadedPluginCount += pLoadedPlugins.get(plgType).size();

    int unknownPluginCount = 0;
    for(PluginType plgType : pUnknownPlugins.keySet())
      unknownPluginCount += pUnknownPlugins.get(plgType).size();

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
      "\n" + 
      "    Required plugins = " + pRequiredPluginCount + "\n" + 
      "      Loaded plugins = " + loadedPluginCount   + "\n" + 
      "Unregistered plugins = " + unknownPluginCount  + "\n" + 
      "\n" + 
      "Remaining required plugins to install = " + 
      (pRequiredPluginCount - loadedPluginCount));

    return isRequiredPlugin;
  }

  /**
   * Return the number of required plugins that need to be installed.
   */
  private int 
  checkRequiredPlugins()
  {
    if(pUpToDate.get())
      return 0;

    /*
      An empty pRequiredPlugins signals that all required plugins 
      are loaded and PluginMgr is ready to respond to all request.
    */
    int requiredPluginCount = 0;

    for(PluginType plgType : pRequiredPlugins.keySet())
      requiredPluginCount += pRequiredPlugins.get(plgType).size();

    if(requiredPluginCount == 0) {
      pUpToDate.set(true);

      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info,
        "plpluginmgr ready to accept connections from other servers.");
    }
    else {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Info, 
        "plpluginmgr NOT ready to accept connections from other servers.");
    }

    return requiredPluginCount;
  }

  /**
   * Return the number of unregistered plugins detected.
   */
  private int
  checkUnknownPlugins()
  {
    int unknownPluginCount = 0;

    for(PluginType plgType : pUnknownPlugins.keySet())
      unknownPluginCount += pUnknownPlugins.get(plgType).size();

    return unknownPluginCount;
  }

  /*
    Read all required-plugins files from the pluginmgr root directory.
  */
  private void
  readRequiredPlugins()
    throws PipelineException
  {
    File root = new File(pPluginMgrDir, "required");
    File[] requiredPlugins = root.listFiles();

    pRequiredPluginCount = 0;

    for(int i = 0 ; i < requiredPlugins.length ; i++) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
        "Reading " + requiredPlugins[i]);

      if(!requiredPlugins[i].isDirectory()) {
        MappedSet<PluginType,PluginID> plugins = null;

        try {
          plugins = (MappedSet<PluginType,PluginID>)
            GlueDecoderImpl.decodeFile("RequiredPlugins", requiredPlugins[i]);

          for(PluginType plgType : plugins.keySet())
          for(PluginID plgID : plugins.get(plgType)) {
            String vendor = plgID.getVendor();

            if(!pVendorPlugins.containsKey(vendor)) {
              pVendorPlugins.put(vendor, new MappedSet<PluginType,PluginID>());
            }

            pRequiredPlugins.put(plgType, plgID);
            pVendorPlugins.get(vendor).put(plgType, plgID);
            pRequiredPluginCount++;
          }
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }
    }

    LogMgr.getInstance().log
      (LogMgr.Kind.Plg, LogMgr.Level.Finest, 
      pRequiredPluginCount + " required plugins");
  }

  /**
   * After each successful installation of a plugin save that vendor's plugins 
   * in a GLUE file.  This ensures that required plugins GLUE files are always up 
   * to date.
   */
  private void
  writeRequiredPlugins
  (
    String vendor, 
    boolean bootstrap
  )
    throws PipelineException
  {
    MappedSet<PluginType,PluginID> plugins = null;

    if(bootstrap)
      plugins = pBootstrapPlugins.get(vendor);
    else {
      plugins = pVendorPlugins.get(vendor);
    }

    /* this error should not occur, since the only times required plugins files 
        are written are right after a plugin has been successfully install or 
        during the bootstrap process.  In both cases the Vendor string should be 
        valid and there should be plugins. */
    if(plugins == null)
      throw new PipelineException
        ("The Vendor specified does not have an entry in the " + 
        (bootstrap ? "bootstrap" : "Vendor") + " table!");

    try {
      File file = new File(pPluginMgrDir, "required/"  + vendor.replace(' ', '-'));

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

      pContents = contents;
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

    public TreeMap<String,byte[]>
    getClasses()
    {
      return pClasses;
    }

    public TreeMap<String,String>
    getResources()
    {
      return pResources;
    }

    public TreeMap<String,byte[]>
    getResourceChecksums()
    {
      return pChecksums;
    }

    private long                    pCycleID; 
    private String                  pClassName;
    private TreeSet<OsType>         pSupports; 
    private boolean                 pIsUnderDevelopment; 

    private TreeMap<String,byte[]>  pContents;

    private TreeMap<String,byte[]>  pClasses;
    private TreeMap<String,String>  pResources;
    private TreeMap<String,byte[]>  pChecksums;
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
  
  private TripleMap<String,String,VersionID,LayoutGroup> pBuilderCollectionLayouts;
  
  private TripleMap<String, String, VersionID, AnnotationPermissions> pAnnotationPermissions; 

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
   *  The installed plugins loaded from a glue file.  These are the plugins from the 
   *  previous run of PluginMgr, it is the complete list of plugins installed and loaded 
   *  and is saved into a GLUE file.
   */
  private MappedSet<PluginType,PluginID>  pRequiredPlugins;

  /**
   * A table of all plugins loaded from disk that are found in a required 
   * plugins GLUE file.
   */
  private MappedSet<PluginType,PluginID>  pLoadedPlugins;

  /**
   * Table of all plugins found in the plugins directory but not found in a 
   * required plugins GLUE file.
   */
  private MappedSet<PluginType,PluginID>  pUnknownPlugins;

  /**
   * A table of all required plugins loaded from GLUE files and plugins 
   * properly installed.
   *
   * Vendor string is the key to MappedSet of PluginType and PluginID.
   */
  private TreeMap<String,MappedSet<PluginType,PluginID>>  pVendorPlugins;

  /**
   * The number of required plugins read from GLUE files.
   */
  private int  pRequiredPluginCount;

  /**
   * Does PluginMgr have all required plugins installed?
   */
  private AtomicBoolean  pUpToDate;

  /**
   * The root directory where required plugins GLUE files are located.
   */
  private File  pPluginMgrDir;

}

