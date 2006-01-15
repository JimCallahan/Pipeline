// $Id: PluginMgr.java,v 1.7 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

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
   */
  public
  PluginMgr() 
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing...");
    LogMgr.getInstance().flush();

    pMakeDirLock = new Object();

    pAdminPrivileges = new AdminPrivileges();

    {
      pPluginLock = new ReentrantReadWriteLock();
      
      pLoadCycleID = 1L;
      
      pEditors     = new TripleMap<String,String,VersionID,Plugin>();  
      pActions     = new TripleMap<String,String,VersionID,Plugin>();  
      pComparators = new TripleMap<String,String,VersionID,Plugin>();  
      pTools  	   = new TripleMap<String,String,VersionID,Plugin>();   
      pArchivers   = new TripleMap<String,String,VersionID,Plugin>();  
    }

    loadAllPlugins(); 
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

      TripleMap<String,String,VersionID,Object[]> editors = 
	collectUpdated(cycleID, pEditors);

      TripleMap<String,String,VersionID,Object[]> actions = 
	collectUpdated(cycleID, pActions);

      TripleMap<String,String,VersionID,Object[]> comparators =
	collectUpdated(cycleID, pComparators);

      TripleMap<String,String,VersionID,Object[]> tools =
	collectUpdated(cycleID, pTools);

      TripleMap<String,String,VersionID,Object[]> archivers = 
	collectUpdated(cycleID, pArchivers);

      return new PluginUpdateRsp(timer, pLoadCycleID, 
				 editors, actions, comparators, tools, archivers);
    }
    finally {
      pPluginLock.readLock().unlock();
    }
  }  

  /**
   * Collect the plugin classes which have been loaded after the given load cycle. <P> 
   * 
   * @param cycleID
   *   The load cycle or <CODE>null</CODE> to collect all plugin classes.
   * 
   * @param plugins
   *   The loaded plugins.
   */ 
  private TripleMap<String,String,VersionID,Object[]>
  collectUpdated
  (
   Long cycleID, 
   TripleMap<String,String,VersionID,Plugin> plugins
  ) 
  {
    TripleMap<String,String,VersionID,Object[]> updated = 
      new TripleMap<String,String,VersionID,Object[]>(); 
    
    if(cycleID == null) {
      for(String vendor : plugins.keySet()) {
	for(String name : plugins.get(vendor).keySet()) {
	  for(VersionID vid : plugins.get(vendor).get(name).keySet()) {
	    Plugin plg = plugins.get(vendor, name, vid);
	    
	    Object[] objs = new Object[2];
	    objs[0] = plg.getClassName();
	    objs[1] = plg.getBytes();

	    updated.put(vendor, name, vid, objs);
	  }
	}
      }
    }
    else {  
      for(String vendor : plugins.keySet()) {
	for(String name : plugins.get(vendor).keySet()) {
	  for(VersionID vid : plugins.get(vendor).get(name).keySet()) {
      	    Plugin plg = plugins.get(vendor, name, vid);

	    if(cycleID < plg.getCycleID()) {
	      Object[] objs = new Object[2];
	      objs[0] = plg.getClassName();
	      objs[1] = plg.getBytes();

	      updated.put(vendor, name, vid, objs);
	    }
	  }
	}
      }
    }

    return updated;
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
      byte bytes[] = req.getBytes();

      /* load the class and cache the class bytes */ 
      pLoadCycleID++;
      loadPluginHelper(req.getClassFile(), cname, req.getVersionID(), bytes);      

      /* save the plugin class bytes in a file */ 
      File file = null;
      try {
	File root = new File(PackageInfo.sInstDir, "plugins");
	File path = new File(cname.replace(".", "/"));
	file = new File(root, path + ".class");
	  
	File dir = file.getParentFile();
	if(!dir.exists()) 
	  if(!dir.mkdirs()) 
	    throw new IOException
	      ("Unable to create the directory (" + dir + ") where the plugin " + 
	       "(" + cname + ") will be installed!");

	FileOutputStream out = new FileOutputStream(file);
	out.write(bytes);
	out.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to save the plugin (" + cname + ") to file (" + file + ")!");
      }

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
   */ 
  private void 
  loadAllPlugins() 
  {
    File root = new File(PackageInfo.sInstDir, "plugins");
    File[] dirs = root.listFiles();
    int wk;
    for(wk=0; wk<dirs.length; wk++) {
      if(dirs[wk].isDirectory()) 
	loadBelow(root, dirs[wk]);
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
   */
  private void 
  loadBelow
  (
   File root, 
   File dir
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
	      ("The directory containing plugin class files (" + dir + ") does " +
	       "not conform to the naming convention of \"v#_#_#\" used to denote " +
	       "the plugin version!  Ignoring class file (" + file + ").\n" + 
	       ex.getMessage());
	  }

	  loadPlugin(root, file);
	}
	catch(PipelineException ex) {
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Plg, LogMgr.Level.Warning,
	     ex.getMessage());
	}	
      }
      else if(fs[wk].isDirectory()) {
	loadBelow(root, fs[wk]);
      }
    }
  }

  /**
   * Load the plugin from the given class file.
   * 
   * @param classdir
   *   The sole Java CLASSPATH directory used to load the class. 
   * 
   * @param classfile
   *   The plugin class file.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPlugin
  (
   File classdir, 
   File classfile
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
	("The class directory (" + classdir + ") was not a valid directory!");
    }

    /* the canonical class file */ 
    File cfile = null;
    try {
      cfile = classfile.getCanonicalFile();

      if(!cfile.isFile()) 
	throw new IOException();
    }
    catch(IOException ex) {
      throw new PipelineException 
	("The class file (" + classfile + ") was not a valid file!");
    }

    /* the class file relative to the class directory */ 
    File rfile = null;
    {
      String fpath = cfile.getPath();
      String dpath = cdir.getPath();

      if(!fpath.startsWith(dpath)) 
	throw new PipelineException 
	  ("The class file (" + cfile + ") was not located under the class directory " + 
	   "(" + cdir + ")!");
      
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
	("The class file (" + classfile + ") was not located under a directory who's " +
	 "name designates a legal plugin revision number:\n" + ex.getMessage());
    }

    /* the class name */ 
    String cname = null;
    {
      String parts[] = cfile.getName().split("\\.");
      if((parts.length == 2) && parts[1].equals("class")) 
	cname = (pkgName + "." + parts[0]);
      else 
	throw new PipelineException 
	  ("The class file (" + classfile + ") was not a Java class file!");
    }
    
    /* load, instantiate and validate the class */ 
    {
      byte[] bytes = new byte[(int) cfile.length()];
      try {
	FileInputStream in = new FileInputStream(cfile);
	in.read(bytes);
	in.close();
      }
      catch(IOException ex) {
	throw new PipelineException
	  ("Unable to read the class file (" + cfile + ")!");
      }
      
      loadPluginHelper(classfile, cname, pkgID, bytes);
    }
  }
    
  /**
   * Load the plugin.
   * 
   * @param classfile
   *   The plugin class file.
   * 
   * @param cname
   *   The full class name.
   * 
   * @param pkgID
   *   The revision number component of the class package.
   * 
   * @param bytes
   *   The raw class bytes.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPluginHelper
  (
   File classfile, 
   String cname, 
   VersionID pkgID, 
   byte[] bytes
  ) 
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(bytes);
    try {
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

      if(!plg.getVersionID().equals(pkgID)) 
	throw new PipelineException
	  ("The revision number (" + plg.getVersionID() + ") of the instantiated " + 
	   "plugin class (" + cname + ") does not match the revision number " + 
	   "(" + pkgID + ") derived from the name of the directory containing the " + 
	   "class file (" + classfile + ")!");
      
      if(plg instanceof BaseEditor) 
	addPlugin(plg, cname, bytes, pEditors);
      else if(plg instanceof BaseAction)
	addPlugin(plg, cname, bytes, pActions);
      else if(plg instanceof BaseComparator)
	addPlugin(plg, cname, bytes, pComparators);
      else if(plg instanceof BaseTool)
	addPlugin(plg, cname, bytes, pTools);
      else if(plg instanceof BaseArchiver)
	addPlugin(plg, cname, bytes, pArchivers);
      else 
	throw new PipelineException
	  ("The class file (" + classfile + ") does not contain a Pipeline plugin!");
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
   * Add a newly loaded plugin to the cached plugin table.
   * 
   * @param plg 
   *   An instance of the loaded plugin class.
   * 
   * @param cname
   *   The full name of the plugin class.
   * 
   * @param bytes
   *   The raw class bytes.
   * 
   * @param table
   *   The cached plugins.
   */ 
  private void 
  addPlugin
  (
   BasePlugin plg,
   String cname, 
   byte[] bytes, 
   TripleMap<String,String,VersionID,Plugin> table
  ) 
    throws PipelineException 
  {
    Plugin plugin = table.get(plg.getVendor(), plg.getName(), plg.getVersionID());
    if((plugin != null) && (!plugin.isUnderDevelopment())) 
      throw new PipelineException 
	("Cannot install the plugin (" + cname + ") because a previously installed " + 
	 "version of this plugin exists which is no longer under development!");

    table.put(plg.getVendor(), plg.getName(), plg.getVersionID(), 
	      new Plugin(pLoadCycleID, cname, plg.isUnderDevelopment(), bytes));
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
     boolean underDevelopment,
     byte[] bytes
    )
    {
      pCycleID            = cycleID; 
      pClassName          = cname;
      pIsUnderDevelopment = underDevelopment;
      pBytes              = bytes; 
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

    public boolean 
    isUnderDevelopment()
    {
      return pIsUnderDevelopment;
    }

    public byte[]
    getBytes() 
    {
      return pBytes; 
    }

    private long     pCycleID; 
    private String   pClassName;
    private boolean  pIsUnderDevelopment; 
    private byte[]   pBytes; 
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
   * The loaded Editor plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pEditors; 

  /**
   * The loaded Action plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pActions; 

  /**
   * The loaded Comparator plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pComparators; 

  /**
   * The loaded Tool plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pTools; 

  /**
   * The loaded Archiver plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pArchivers; 

}

