// $Id: PluginMgr.java,v 1.3 2004/09/10 15:40:09 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   P L U G I N   M G R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of static methods for mamanging Pipeline plugin classes.
 */
public
class PluginMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   **/
  private 
  PluginMgr() 
  {
    pTimeStamps = new TreeMap<File,Long>();

    pEditors    = new TreeMap<String,TreeMap<VersionID,Plugin>>(); 
    pActions    = new TreeMap<String,TreeMap<VersionID,Plugin>>(); 
    pComps      = new TreeMap<String,TreeMap<VersionID,Plugin>>(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N I T I A L I Z A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the plugin manager instance. 
   */
  public static void 
  init() 
    throws PipelineException
  {
    assert(sPluginMgr == null);
    sPluginMgr = new PluginMgr();
    sPluginMgr.refresh();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the UIMaster instance.
   */ 
  public static PluginMgr
  getInstance() 
  {
    return sPluginMgr;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names and version numbers of all available editor plugins. <P> 
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getEditors() 
  {
    try {
      refresh();
    }
    catch(PipelineException ex) {
      Logs.plg.warning(ex.getMessage());
    }

    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : pEditors.keySet()) {
      TreeMap<VersionID,Plugin> plgs = pEditors.get(name);
      table.put(name, new TreeSet<VersionID>(plgs.keySet()));
    }

    return table;
  }

  /**
   * Get the names and version numbers of all available action plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getActions()
  {
    try {
      refresh();
    }
    catch(PipelineException ex) {
      Logs.plg.warning(ex.getMessage());
    } 

    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : pActions.keySet()) {
      TreeMap<VersionID,Plugin> plgs = pActions.get(name);
      table.put(name, new TreeSet<VersionID>(plgs.keySet()));
    }

    return table;
  }

  /**
   * Get the names and version numbers of all available comparator plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getComparators() 
  {
    try {
      refresh();
    }
    catch(PipelineException ex) {
      Logs.plg.warning(ex.getMessage());
    } 

    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : pComps.keySet()) {
      TreeMap<VersionID,Plugin> plgs = pComps.get(name);
      table.put(name, new TreeSet<VersionID>(plgs.keySet()));
    }

    return table;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N S T A N T I A T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new editor plugin instance with the given name and version. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseEditor#getName BaseEditor.getName} for the returned 
   * editor.
   * 
   * @param name 
   *   The name of the editor plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the editor to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no editor plugin can be found for the given or instantiation fails for some reason.
   */
  public synchronized BaseEditor
  newEditor
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    return (BaseEditor) newPlugin("Editor", pEditors, name, vid);
  }
  
  /**
   * Create a new action plugin instance with the given name and version. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseAction#getName BaseAction.getName} for the returned 
   * action.
   * 
   * @param name 
   *   The name of the action plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the action to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no action plugin can be found for the given or instantiation fails for some reason.
   */
  public synchronized BaseAction
  newAction
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    return (BaseAction) newPlugin("Action", pActions, name, vid);
  }
  
  /**
   * Create a new comparator plugin instance with the given name and version. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseComparator#getName BaseComparator.getName} for the 
   * returned comparator.
   * 
   * @param name 
   *   The name of the comparator plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the comparator to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no comparator plugin can be found for the given or instantiation fails for some 
   *   reason.
   */
  public synchronized BaseComparator
  newComparator
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    return (BaseComparator) newPlugin("Comparator", pComps, name, vid);
  }
    

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N S T A L L A T I O N                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Print information about the currently installed Pipeline plugins to STDOUT.
   */ 
  public synchronized void
  listPlugins() 
    throws PipelineException
  {
    try {
      refresh();
    }
    catch(PipelineException ex) {
      Logs.plg.warning(ex.getMessage());
    }

    StringBuffer buf = new StringBuffer(); 
    printPlugins("Editor", pEditors, buf);
    printPlugins("Action", pActions, buf);
    printPlugins("Comparators", pComps, buf);

    Logs.plg.info(buf.toString());
    Logs.flush();
  }

  /**
   * Append information about the given type of plugins to the given string buffer.
   */ 
  private synchronized void
  printPlugins
  (
   String ptype,
   TreeMap<String,TreeMap<VersionID,Plugin>> table,
   StringBuffer buf
  ) 
    throws PipelineException
  {
    buf.append(ptype.toUpperCase() + " PLUGINS:\n\n");

    for(String name : table.keySet()) {	
      TreeMap<VersionID,Plugin> plgs = table.get(name);
      for(VersionID vid : plgs.keySet()) {
	BasePlugin plg = newPluginHelper(ptype, table, name, vid);

	Plugin plugin = plgs.get(vid);
	Date stamp = new Date(plugin.getPluginFile().lastModified());
	
	buf.append("  Name        : " + plg.getName() + "\n" + 
		   "  Version     : " + plg.getVersionID() + "\n" + 
		   "  Description : " + plg.getDescription() + "\n" + 
		   "  Class       : " + plg.getClass().getName() + "\n" + 
		   "  Installed   : " + Dates.format(stamp) + "\n\n");
      }
    }
  }

  /**
   * Print information about the Pipeline plugin specified by the given class file.
   */ 
  public synchronized void
  inspectPlugin
  (
   File file
  ) 
    throws PipelineException
  {
    BasePlugin plg = verifyPlugin(file);
    
    String type = null;
    if(plg instanceof BaseEditor) 
      type = "EDITOR";
    else if(plg instanceof BaseAction) 
      type = "ACTION";
    else if(plg instanceof BaseComparator) 
      type = "COMPARATOR";
    
    Logs.plg.info
      (type + " PLUGIN: " + file + "\n\n" + 
       "  Name        : " + plg.getName() + "\n" + 
       "  Version     : " + plg.getVersionID() + "\n" + 
       "  Description : " + plg.getDescription() + "\n" + 
       "  Class       : " + plg.getClass().getName() + "\n\n");      
    Logs.flush();
  }
  
  /**
   * Install the Pipeline plugin specified by the given class file.
   * 
   * This method will throw an exception if the current user is not the pipeline user.
   * 
   * @param file
   *   The compiled Java class file contiaing the plugin.
   * 
   * @param force
   *   Whether to replace any existing plugin with the same name and version.
   */ 
  public synchronized void
  installPlugin
  (
   File file, 
   boolean force
  ) 
     throws PipelineException
  {
    if(!PackageInfo.sUser.equals(PackageInfo.sPipelineUser)) 
      throw new PipelineException
	("Plugins may only be installed by the (" + PackageInfo.sPipelineUser + ") user!");
    
    aquireFileLock();
    try {
      BasePlugin plg = verifyPlugin(file);
      if(plg instanceof BaseEditor) 
	installPluginHelper("Editor", pEditors, plg, file, force);
      else if(plg instanceof BaseAction) 
	installPluginHelper("Action", pActions, plg, file, force);
      else if(plg instanceof BaseComparator) 
	installPluginHelper("Comparator", pComps, plg, file, force);
    }
    finally {
      releaseFileLock(true);
    }
  }
  
  /**
   * Temporarily instantiate an prospective plugin in order to verify its correctness.
   * 
   * @param file
   *   The plugin class file.
   */ 
  private synchronized BasePlugin
  verifyPlugin
  (
   File file
  )
    throws PipelineException
  {
    String work = null;
    String cname = null;
    VersionID vid = null;
    try {
      work = new File(System.getProperty("user.dir")).getCanonicalPath();
      String canon = file.getCanonicalPath();
      if((canon.length() <= work.length()) || (!canon.startsWith(work)))
	throw new PipelineException
	  ("The class file (" + file + ") was not located below the current directory!");
      
      String cpath = canon.substring(work.length()+1);
      if(!cpath.endsWith(".class")) 
	throw new PipelineException
	  ("The class file (" + file + ") did not have a \".class\" extension!");
      
      cname = cpath.substring(0, cpath.length()-6).replace('/', '.');

      try {
	String[] parts = cname.split("\\.");
	if(parts.length < 2)
	  throw new IOException(); 
	
	String vstr = parts[parts.length-2];
	if(!vstr.startsWith("v")) 
	  throw new IOException(); 
	
	vid = new VersionID(vstr.substring(1).replace('_','.'));
      }
      catch(Exception ex) {
	throw new PipelineException 
	  ("The class file (" + file + ") must be located in a directory which " + 
	   "corresponds to the plugin version!");
      }
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to resolve the path to the class file (" + file + ")!");
    }

    URL classpath[] = null;
    try {
      File path = new File(work);
      URL url = path.toURI().toURL();      
      classpath = new URL[]{url};
    } 
    catch(MalformedURLException ex) {
      throw new PipelineException
	("Unable to construct a proper plugin class path!");
    }
    
    ClassLoader loader = new URLClassLoader(classpath);
    try {
      Logs.plg.finer("Loading: " + cname);
      Class cls = loader.loadClass(cname);

      if(!(BaseEditor.class.isAssignableFrom(cls) || 
	   BaseAction.class.isAssignableFrom(cls) || 
	   BaseComparator.class.isAssignableFrom(cls)))
	throw new PipelineException
	  ("The class file (" + file + ") does not contain a legal Pipeline plugin!");
	
      BasePlugin plg = (BasePlugin) cls.newInstance();
      
      if(!plg.getVersionID().equals(vid)) 
	throw new PipelineException
	  ("The version (" + plg.getVersionID() + ") of the instantiated plugin does " + 
	   "not correspond to the location of the class file (" + file + ")!");

      return plg;
    }
    catch(LinkageError ex) {
      throw new PipelineException
	("Unable to link plugin class (" + cname + "):\n" + 
	 ex.getMessage());
    }
    catch(ClassNotFoundException ex) {
      throw new PipelineException
	("Unable to find plugin class (" + cname + ") in class file " + file + ")!");
    }   
    catch (IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access the constructor for plugin class (" + cname + " v" + vid + ")!");
    }
    catch (InstantiationException ex) { 
      throw new PipelineException
	("Unable to instantiate the plugin class (" + cname + " v" + vid + "):\n" +
	 ex.getMessage());
    } 
  }
  
  /**
   * Install a previously verified plugin class file.
   */ 
  private synchronized void 
  installPluginHelper
  ( 
   String ptype,
   TreeMap<String,TreeMap<VersionID,Plugin>> table, 
   BasePlugin plg,
   File file, 
   boolean force
  ) 
    throws PipelineException 
  {
    TreeMap<VersionID,Plugin> versions = table.get(plg.getName());
    if(versions == null) {
      versions = new TreeMap<VersionID,Plugin>();
      table.put(plg.getName(), versions);
    }
    
    if(versions.containsKey(plg.getVersionID()) && !force)
      throw new PipelineException
	("An " + ptype + " plugin named (" + plg.getName() + ") version " + 
	 "(" + plg.getVersionID() + ") already exists!\n" + 
	 "Use --force to replace it with the new plugin in class file (" + file + ").");
    
    {
      File target = new File(PackageInfo.sInstDir, "plugins/" + 
			     plg.getClass().getName().replace('.', '/') + ".class");
	  
      File dir = target.getParentFile();
      if(!dir.isDirectory()) 
	if(!dir.mkdirs()) 
	  throw new PipelineException
	    ("Unable to create plugin install directory (" + dir + ")!");
      
      ArrayList<String> args = new ArrayList<String>();
      args.add(file.toString());
      args.add(target.toString());
      
      Map<String,String> env = System.getenv();
      File work = new File(System.getProperty("user.dir"));
      
      SubProcess proc = 
	new SubProcess("Install-Plugin", "cp", args, env, work);
      proc.start();
      
      try {
	proc.join();
	if(!proc.wasSuccessful()) 
	  throw new PipelineException
	    ("Unable to install plugin class file (" + file + "):\n" + 
	     proc.getStdErr());
      }
      catch(InterruptedException ex) {
	throw new PipelineException
	  ("Interrupted while installing plugin class file (" + file + ")!");
      }

      Plugin plugin = new Plugin(plg.getClass(), file);
      versions.put(plg.getVersionID(), plugin);  

      Date stamp = new Date(plugin.getPluginFile().lastModified());
      
      Logs.net.info
	("INSTALLED " + ptype.toUpperCase() + " PLUGIN: " + file + "\n\n" +
	 "  Name        : " + plg.getName() + "\n" + 
	 "  Version     : " + plg.getVersionID() + "\n" + 
	 "  Description : " + plg.getDescription() + "\n" + 
	 "  Class       : " + plg.getClass().getName() + "\n" + 
	 "  Installed   : " + Dates.format(stamp) + "\n\n");
      Logs.flush();
    }	  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C L A S S   H E L P E R S                                                            */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new plugin instance with the given name and version. <P> 
   * 
   * If a newer class file exists in the plugin directory than the last file loaded
   * for the given name and version, it will be loaded before the plugin is instantiated. <P>
   * 
   * @param ptype 
   *   The kind of plugin being instantiated: Editor, Comparator or Action.
   * 
   * @param table 
   *   The table of plugin to search.
   * 
   * @param name 
   *   The name of the plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the plugin to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no plugin can be found for the given <CODE>name</CODE> or instantiation fails.
   */
  private synchronized BasePlugin
  newPlugin
  (
   String ptype,
   TreeMap<String,TreeMap<VersionID,Plugin>> table, 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(table == null) 
      throw new PipelineException("The plugins have not been intialized!");

    /* refresh the given plugin (or all plugin tables if necessary) in order to make sure
       that the plugin is instantiated from the latest available class file */ 
    {
      boolean refreshAll = false;
      if(vid == null) 
	refreshAll = true; 
      else {
	TreeMap<VersionID,Plugin> versions = table.get(name);
	if((versions == null) || versions.isEmpty())
	  refreshAll = true; 
	else {
	  VersionID pvid = vid;
	  if(pvid == null) 
	    pvid = versions.lastKey();
	
	  Plugin plugin = versions.get(pvid);
	  if(plugin != null) {
	    File file = new File(PackageInfo.sInstDir, "plugins/lock");
	    if(!file.isFile()) 
	      throw new PipelineException("The plugin directory lock file was missing!");
	    
	    if((pLastRefresh == null) || (pLastRefresh <= file.lastModified())) {
	      aquireFileLock();
	      try {
		refreshPlugin(plugin.getPluginFile(), pvid);
	      }
	      finally {
		releaseFileLock(false);
	      }
	      
	      pLastRefresh = new Long(Dates.now().getTime());
	    }
	  }
	  else 
	    refreshAll = true;
	}
      }

      if(refreshAll) 
	refresh();
    }

    /* instantiate the plugin */ 
    return newPluginHelper(ptype, table, name, vid);
  }

  /**
   * Create a new plugin instance looked up from the given table of classes using the 
   * given name and version.
   * 
   * @param ptype 
   *   The kind of plugin being instantiated: Editor, Comparator or Action.
   * 
   * @param table 
   *   The table of plugins to search.
   * 
   * @param name 
   *   The name of the plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the plugin to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no plugin can be found for the given <CODE>name</CODE> or instantiation fails.
   */
  private synchronized BasePlugin
  newPluginHelper
  (
   String ptype,
   TreeMap<String,TreeMap<VersionID,Plugin>> table, 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    if(table == null) 
      throw new PipelineException("The plugins have not been intialized!");

    TreeMap<VersionID,Plugin> versions = table.get(name);
    if(versions == null) 
      throw new PipelineException
	("No " + ptype + " plugin named (" + name + ") exists!");

    VersionID pvid = vid;
    if(pvid == null) 
      pvid = versions.lastKey();

    Plugin plugin = versions.get(pvid);
    if(plugin == null) {
      throw new PipelineException
	("Unable to find the " + ptype + " plugin (" + name + " v" + pvid + ")!");
    }

    try {
      return (BasePlugin) plugin.getPluginClass().newInstance();  
    }
    catch (IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access the constructor for plugin class (" + name + " v" + pvid + ")!");
    }
    catch (InstantiationException ex) { 
      throw new PipelineException
	("Unable to instantiate the plugin class (" + name + " v" + pvid + "):\n\n" +
	 ex.getMessage());
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Loads all {@link BaseEditor BaseEditor}, {@link BaseComparator BaseComparator} and 
   * {@link BaseAction BaseAction} classes found in the installed Pipeline plugin 
   * directories which are newer than previously loaded plugin classes.
   */
  private synchronized void 
  refresh() 
    throws PipelineException
  {
    {
      File file = new File(PackageInfo.sInstDir, "plugins/lock");
      if(!file.isFile()) 
	throw new PipelineException("The plugin directory lock file was missing!");
      
      if((pLastRefresh != null) && (pLastRefresh > file.lastModified())) 
	return;
    }

    TaskTimer timer = new TaskTimer("Plugins Refreshed");

    timer.aquire();
    aquireFileLock();
    try {
      timer.resume();
      
      File root = new File(PackageInfo.sInstDir, "plugins");
      File[] dirs = root.listFiles();
      int wk;
      for(wk=0; wk<dirs.length; wk++) {
	if(dirs[wk].isDirectory()) 
	  refreshBelow(dirs[wk]);
      }
    }
    finally {
      releaseFileLock(false);
    }

    pLastRefresh = new Long(Dates.now().getTime());

    Logs.plg.finer(timer.toString()); 
    if(Logs.plg.isLoggable(Level.FINER))
      Logs.flush();
  }

  /**
   * Recursively search the current directory and its subdirectories for plugin class files
   * which are newer than the currently loaded plugins and reload them. <P> 
   * 
   * @param dir
   *   The current directory.
   */ 
  public synchronized void 
  refreshBelow
  (
   File dir
  ) 
    throws PipelineException
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
	  
	  refreshPlugin(file, vid);
	}
	catch(PipelineException ex) {
	  Logs.plg.warning(ex.getMessage());
	}
      }
      else if(fs[wk].isDirectory()) {
	refreshBelow(fs[wk]);
      }
    }
  }
  
  /**
   * Reload the plugin in the given class file using a unique class loaded if it has 
   * been modified since the last time the plugin was loaded.
   * 
   * @param file
   *   The plugin class file.
   * 
   * @param vid
   *   The expected revision number of the plugin.
   */ 
  private synchronized void
  refreshPlugin
  (
   File file,
   VersionID vid
  ) 
    throws PipelineException
  {
    String rootPath = (PackageInfo.sInstDir + "/plugins/");
    String dirPath = file.getParent();
    String pkgName = dirPath.substring(rootPath.length()).replace('/', '.');

    Long ostamp = pTimeStamps.get(file);
    long nstamp = file.lastModified();
    if((ostamp == null) || (nstamp > ostamp)) {
      pTimeStamps.put(file, nstamp);
      
      String parts[] = file.getName().split("\\.");
      if((parts.length == 2) && parts[1].equals("class")) {
	Logs.plg.finer("Found class file: " + file);
	String cname = (pkgName + "." + parts[0]);
	  
	URL classpath[] = null;
	try {
	  File path = new File(PackageInfo.sInstDir + "/plugins/");
	  URL url = path.toURI().toURL();      
	  classpath = new URL[]{url};
	} 
	catch(MalformedURLException ex) {
	  throw new PipelineException
	    ("Unable to construct a proper plugin class path!");
	}
    
	ClassLoader loader = new URLClassLoader(classpath);
	try {
	  Logs.plg.finer("Loading: " + cname);
	  Class cls = loader.loadClass(cname);
	  
	  if(!(updatePlugin(    BaseEditor.class,     "Editor", pEditors, cls, file, vid) ||
	       updatePlugin(    BaseAction.class,     "Action", pActions, cls, file, vid) ||
	       updatePlugin(BaseComparator.class, "Comparator",   pComps, cls, file, vid)))
	    throw new PipelineException
	      ("The class file (" + file + ") does not contain a legal Editor, Action or " + 
	       "Comparator plugin!");
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

	Logs.flush();
      }
      else {
	throw new PipelineException 
	  ("Found a non-class file (" + file + ") in the plugin directory!");
      }
    }
  }

  /**
   * Instantiate the plugin class, verify that it matches the expected type and version 
   * and update the stored plugin information.
   * 
   * @param baseCls
   *   The expected base class of the loaded plugin.
   * 
   * @param ptype 
   *   The kind of plugin being instantiated: Editor, Comparator or Action.
   * 
   * @param table 
   *   The table used to store the loaded plugin.
   * 
   * @param cls
   *   The plugin class.
   * 
   * @param file
   *   The file containing the plugin class.
   * 
   * @param vid
   *   The revision number of the plugin.
   * 
   * @return 
   *   Whether the plugin was successfully updated.
   */ 
  private synchronized boolean
  updatePlugin
  (
   Class baseCls, 
   String ptype,
   TreeMap<String,TreeMap<VersionID,Plugin>> table, 
   Class cls, 
   File file, 
   VersionID vid
  ) 
    throws PipelineException 
  {
    if(!baseCls.isAssignableFrom(cls)) 
      return false;

    Logs.plg.finest("Instantiating " + ptype + " Plugin: " + cls.getName());      
    try {
      BasePlugin plg = (BasePlugin) cls.newInstance();

      if(!plg.getVersionID().equals(vid)) 
	throw new PipelineException
	  ("The version (" + plg.getVersionID() + ") of the instantiated " + ptype + " " + 
	   "plugin does not match the installed location of the class file (" + file + ")!");
      
      String name = plg.getName();
      TreeMap<VersionID,Plugin> versions = table.get(name);
      if(versions == null) {
	versions = new TreeMap<VersionID,Plugin>();
	table.put(name, versions);
      }
      
      Plugin plugin = new Plugin(cls, file);
      versions.put(vid, plugin);    
      
      Logs.plg.fine("Updated " + ptype + " Plugin: " + name + " (v" + vid + ")");      
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

    return true;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   L O C K I N G                                                              */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Wait to aquire the plugin directory file lock.
   */ 
  private synchronized void 
  aquireFileLock() 
    throws PipelineException
  {
    File file = new File(PackageInfo.sInstDir, "plugins/lock");

    if((pFileChannel != null) || (pFileLock != null)) 
      throw new PipelineException
	("Somehow the plugin directory file lock as already been aquired!");

    Logs.plg.finest("Aquiring plugin directory file lock...");

    int cnt = 0;
    while(true) {
      try {
	pFileChannel = new RandomAccessFile(file, "rw").getChannel();
	pFileLock = pFileChannel.tryLock();
	if(pFileLock != null) 
	  break;
      }
      catch (Exception ex) {
	throw new PipelineException
	  ("Unable to aquire plugin directory lock file!", ex); 
      }

      try {
	Thread.sleep(1000);
      }
      catch(InterruptedException ex) {
	throw new PipelineException
	  ("Interrupted while waiting to aquire the plugin directory file lock!");
      }

      if(cnt > 5) {
	Logs.plg.warning("Waiting to aquire the plugin directory file lock...");
	Logs.flush();
      }

      cnt++;
    }

    Logs.plg.finest("Lock aquired!");
  }

  /**
   * Release the previously aquired plugin directory file lock.
   */ 
  private synchronized void 
  releaseFileLock
  (
   boolean update
  )
    throws PipelineException
  {
    if((pFileChannel != null) && (pFileLock != null)) {
      try {
	if(update) {
	  Date now = new Date();
	  ByteBuffer buf = ByteBuffer.wrap(("Updated on: " + now.toString()).getBytes());
	  pFileChannel.write(buf); 
	}
	
	pFileLock.release();
	pFileChannel.close();
      }
      catch(IOException ex) {
	Logs.plg.severe(ex.getMessage());
      }

      Logs.plg.finest("Lock released!");
    }

    pFileLock    = null; 
    pFileChannel = null; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class Plugin
  {
    public
    Plugin
    (
     Class cls,  
     File file
    ) 
    {
      pPluginClass = cls;
      pPluginFile  = file;
    }

    public Class
    getPluginClass() 
    {
      return pPluginClass;
    }

    public File
    getPluginFile() 
    {
      return pPluginFile;
    }

    private Class pPluginClass; 
    private File  pPluginFile; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance.
   */ 
  private static PluginMgr  sPluginMgr = null;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of the last time the plugins where refreshed.
   */ 
  private Long  pLastRefresh; 

  /**
   * The last modified timestamps each plugin file at the time the plugin was loaded.
   */ 
  private TreeMap<File,Long>  pTimeStamps;

  /** 
   * The table of loaded editors plugins indexed by plugin name and version.
   */
  private TreeMap<String,TreeMap<VersionID,Plugin>>  pEditors;

  /** 
   * The table of loaded actions plugins indexed by plugin name and version.
   */
  private TreeMap<String,TreeMap<VersionID,Plugin>>  pActions;

  /** 
   * The table of loaded comparators plugins indexed by plugin name and version.
   */
  private TreeMap<String,TreeMap<VersionID,Plugin>>  pComps;
  

  /**
   * The plugin directory file lock.
   */ 
  private FileChannel  pFileChannel = null; 
  private FileLock     pFileLock    = null; 
}


