// $Id: PluginMgr.java,v 1.14 2007/06/15 00:27:31 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.lang.reflect.*; 
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.jar.*; 
import java.util.zip.*;

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
      
      pEditors     = new TripleMap<String,String,VersionID,Plugin>();  
      pActions     = new TripleMap<String,String,VersionID,Plugin>();  
      pComparators = new TripleMap<String,String,VersionID,Plugin>();  
      pTools  	   = new TripleMap<String,String,VersionID,Plugin>();   
      pAnnotations = new TripleMap<String,String,VersionID,Plugin>();   
      pArchivers   = new TripleMap<String,String,VersionID,Plugin>();  
      pMasterExts  = new TripleMap<String,String,VersionID,Plugin>();  
      pQueueExts   = new TripleMap<String,String,VersionID,Plugin>();  

      pSerialVersionUIDs = new TreeMap<Long,String>(); 
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

      TripleMap<String,String,VersionID,Object[]> annotations =
	collectUpdated(cycleID, pAnnotations);

      TripleMap<String,String,VersionID,Object[]> archivers = 
	collectUpdated(cycleID, pArchivers);

      TripleMap<String,String,VersionID,Object[]> masterExts = 
	collectUpdated(cycleID, pMasterExts);

      TripleMap<String,String,VersionID,Object[]> queueExts = 
	collectUpdated(cycleID, pQueueExts);

      return new PluginUpdateRsp(timer, pLoadCycleID, 
				 editors, actions, comparators, tools, annotations, 
                                 archivers, masterExts, queueExts);
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
      for(String vendor : plugins.keySet()) {
	for(String name : plugins.get(vendor).keySet()) {
	  for(VersionID vid : plugins.get(vendor).get(name).keySet()) {
      	    Plugin plg = plugins.get(vendor, name, vid);

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
      TreeMap<String,byte[]> contents = req.getContents();

      /* load the class and cache the class bytes */ 
      pLoadCycleID++;
      loadPluginHelper(req.getClassFile(), cname, req.getVersionID(), contents); 

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
    File root = PackageInfo.sPluginsPath.toFile();
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
	      ("The directory containing plugin files (" + dir + ") does " +
	       "not conform to the naming convention of \"v#_#_#\" used to denote " +
	       "the plugin version!  Ignoring plugin file (" + file + ").\n" + 
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
   * Load the plugin from the given class or JAR file.
   * 
   * @param classdir
   *   The sole Java CLASSPATH directory used to load the class. 
   * 
   * @param pluginfile
   *   The plugin class or JAR file.
   * 
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPlugin
  (
   File classdir, 
   File pluginfile
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

      loadPluginHelper(pluginfile, cname, pkgID, contents);
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
   * @throws PipelineException
   *   If unable to load the plugin.
   */ 
  private void 
  loadPluginHelper
  (
   File pluginfile, 
   String cname, 
   VersionID pkgID, 
   TreeMap<String,byte[]> contents
  ) 
    throws PipelineException
  {
    ClassLoader loader = new PluginClassLoader(contents);
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

      if(plg instanceof BaseEditor) 
	addPlugin(plg, cname, contents, pEditors);
      else if(plg instanceof BaseAction) 
	addPlugin(plg, cname, contents, pActions);
      else if(plg instanceof BaseComparator) 
	addPlugin(plg, cname, contents, pComparators);
      else if(plg instanceof BaseTool) 
	addPlugin(plg, cname, contents, pTools);
      else if(plg instanceof BaseAnnotation) 
	addPlugin(plg, cname, contents, pAnnotations);
      else if(plg instanceof BaseArchiver) 
	addPlugin(plg, cname, contents, pArchivers);
      else if(plg instanceof BaseMasterExt) 
	addPlugin(plg, cname, contents, pMasterExts);
      else if(plg instanceof BaseQueueExt) 
	addPlugin(plg, cname, contents, pQueueExts);
      else 
	throw new PipelineException
	  ("The class file (" + pluginfile + ") does not contain a Pipeline plugin!");
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
   * @param contents
   *   The raw plugin class bytes indexed by class name.
   * 
   * @param table
   *   The cached plugins.
   */ 
  private void 
  addPlugin
  (
   BasePlugin plg,
   String cname, 
   TreeMap<String,byte[]> contents, 
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
	      new Plugin(pLoadCycleID, cname, 
			 plg.getSupports(), plg.isUnderDevelopment(), 
			 contents));
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
   * The loaded Annotation plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pAnnotations; 

  /**
   * The loaded Archiver plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pArchivers; 

  /**
   * The loaded Master Extension plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pMasterExts; 

  /**
   * The loaded Queue Extension plugins indexed by plugin name and revision number.
   */
  private TripleMap<String,String,VersionID,Plugin>  pQueueExts; 

  /**
   * The serialVersionUIDs of all loaded plugins used to test for conflicts.
   * 
   * The table contains plugin identifying strings indexed by the serialVersionUID of
   * the plugin.
   */ 
  private TreeMap<Long,String>  pSerialVersionUIDs;

}
