// $Id: BasePluginMgrClient.java,v 1.3 2005/01/22 01:36:35 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   P L U G I N   M G R   C L I E N T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class of class which provide connections to the Pipeline plugin 
 * manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline plugin manager daemon 
 * <A HREF="../../../../man/plpluginmgr.html"><B>plpluginmgr</B><A>(1).  
 */
public
class BasePluginMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   **/
  protected 
  BasePluginMgrClient() 
  {
    super(PackageInfo.sPluginServer, PackageInfo.sPluginPort, 
	  PluginRequest.Disconnect, PluginRequest.Shutdown);

    pEditors     = new TreeMap<String,TreeMap<VersionID,Class>>();  
    pActions     = new TreeMap<String,TreeMap<VersionID,Class>>();  
    pComparators = new TreeMap<String,TreeMap<VersionID,Class>>();  
    pTools  	 = new TreeMap<String,TreeMap<VersionID,Class>>();   
    pArchivers   = new TreeMap<String,TreeMap<VersionID,Class>>();  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get any new or updated plugin classes from the plugin manager.
   * 
   * @throws PipelineException
   *   If unable to update the plugins.
   */ 
  public synchronized void 
  update() 
    throws PipelineException
  {
    verifyConnection();

    PluginUpdateReq req = new PluginUpdateReq(pCycleID);

    Object obj = performTransaction(PluginRequest.Update, req);
    if(obj instanceof PluginUpdateRsp) {
      PluginUpdateRsp  rsp = (PluginUpdateRsp) obj;
     
      updatePlugins(rsp.getEditors(), pEditors);     
      updatePlugins(rsp.getActions(), pActions);     
      updatePlugins(rsp.getComparators(), pComparators);     
      updatePlugins(rsp.getTools(), pTools);     
      updatePlugins(rsp.getArchivers(), pArchivers);
     
      pCycleID = rsp.getCycleID();
    }
    else {
      handleFailure(obj);
    }    
  }

  /**
   * Copy the new or updated plugin classess into the cached plugin class table.
   */ 
  private void 
  updatePlugins
  (
   TreeMap<String,TreeMap<VersionID,Object[]>> source, 
   TreeMap<String,TreeMap<VersionID,Class>> target
  ) 
    throws PipelineException
  {
    for(String name : source.keySet()) {
      TreeMap<VersionID,Object[]> sversions = source.get(name);
      TreeMap<VersionID,Class> tversions = target.get(name);
      if(tversions == null) {
	tversions = new TreeMap<VersionID,Class>();
	target.put(name, tversions);
      }

      for(VersionID vid : sversions.keySet()) {
	Object[] objs = sversions.get(vid);
	String cname = (String) objs[0];
	byte[] bytes = (byte[]) objs[1];
	
	ClassLoader loader = new PluginClassLoader(bytes);
	try {
	  LogMgr.getInstance().log
(LogMgr.Kind.Plg, LogMgr.Level.Finer,
"Updating: " + cname);
	  Class cls = loader.loadClass(cname);
	  tversions.put(vid, cls);
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
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names and version numbers of all available editor plugins. <P> 
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getEditors() 
  {
    return getPlugins(pEditors);
  }

  /**
   * Get the names and version numbers of all available action plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getActions()
  {
    return getPlugins(pActions);
  }

  /**
   * Get the names and version numbers of all available comparator plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getComparators()
  {
    return getPlugins(pComparators);
  }

  /**
   * Get the names and version numbers of all available tool plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getTools()
  {
    return getPlugins(pTools);
  }

  /**
   * Get the names and version numbers of all available archiver plugins.
   */ 
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getArchivers()
  {
    return getPlugins(pArchivers);
  }

  /**
   * Get the names and version numbers of all plugins in the given table.
   */ 
  private TreeMap<String,TreeSet<VersionID>>
  getPlugins
  (
   TreeMap<String,TreeMap<VersionID,Class>> plugins
  ) 
  {
    TreeMap<String,TreeSet<VersionID>> table = new TreeMap<String,TreeSet<VersionID>>();
    for(String name : plugins.keySet()) 
      table.put(name, new TreeSet<VersionID>(plugins.get(name).keySet()));

    return table;
  }


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
    return (BaseComparator) newPlugin("Comparator", pComparators, name, vid);
  }

  /**
   * Create a new archiver plugin instance with the given name and version. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseArchiver#getName BaseArchiver.getName} for the 
   * returned archiver.
   * 
   * @param name 
   *   The name of the archiver plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the archiver to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no archiver plugin can be found for the given or instantiation fails for some 
   *   reason.
   */
  public synchronized BaseArchiver
  newArchiver
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    return (BaseArchiver) newPlugin("Archiver", pArchivers, name, vid);
  }

  /**
   * Create a new tool plugin instance with the given name and version. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseTool#getName BaseTool.getName} for the 
   * returned tool.
   * 
   * @param name 
   *   The name of the tool plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the tool to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @throws  PipelineException
   *   If no tool plugin can be found for the given or instantiation fails for some 
   *   reason.
   */
  public synchronized BaseTool
  newTool
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    return (BaseTool) newPlugin("Tool", pTools, name, vid);
  }

  /**
   * Create a new plugin instance with the given name and version. <P> 
   * 
   * @param ptype 
   *   The kind of plugin being instantiated: Editor, Comparator, Action, Tool or Archiver
   * 
   * @param table 
   *   The table of cached plugin classes to search.
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
  private BasePlugin
  newPlugin
  (
   String ptype,
   TreeMap<String,TreeMap<VersionID,Class>> table, 
   String name, 
   VersionID vid
  ) 
    throws PipelineException
  {
    TreeMap<VersionID,Class> versions = table.get(name);
    if(versions == null) 
      throw new PipelineException
	("No " + ptype + " plugin named (" + name + ") exists!");

    VersionID pvid = vid;
    if(pvid == null) 
      pvid = versions.lastKey();

    Class cls = versions.get(pvid);
    if(cls == null) {
      throw new PipelineException
	("Unable to find the " + ptype + " plugin (" + name + " v" + pvid + ")!");
    }

    try {
      return (BasePlugin) cls.newInstance();  
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
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plpluginmgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The plugin load cycle sequence identifier of the last update of the plugin class cache.
   */ 
  private Long  pCycleID; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached Editor plugin classes indexed by class name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Class>>  pEditors; 

  /**
   * The cached Action plugin classes indexed by class name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Class>>  pActions; 

  /**
   * The cached Comparator plugin classes indexed by class name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Class>>  pComparators; 

  /**
   * The cached Tool plugin classes indexed by class name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Class>>  pTools; 

  /**
   * The cached Archiver plugin classes indexed by class name and revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Class>>  pArchivers; 

}


