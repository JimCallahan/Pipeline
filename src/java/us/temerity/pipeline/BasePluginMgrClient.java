// $Id: BasePluginMgrClient.java,v 1.8 2006/08/20 05:46:51 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.net.*; 
import java.io.*; 
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

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

    pEditors     = new TripleMap<String,String,VersionID,PluginData>();  
    pActions     = new TripleMap<String,String,VersionID,PluginData>();  
    pComparators = new TripleMap<String,String,VersionID,PluginData>();  
    pTools  	 = new TripleMap<String,String,VersionID,PluginData>();   
    pArchivers   = new TripleMap<String,String,VersionID,PluginData>();  
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
      PluginUpdateRsp rsp = (PluginUpdateRsp) obj;
     
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
   TripleMap<String,String,VersionID,Object[]> source, 
   TripleMap<String,String,VersionID,PluginData> target
  ) 
    throws PipelineException
  {
    for(String vendor : source.keySet()) {
      for(String name : source.get(vendor).keySet()) {
	for(VersionID vid : source.get(vendor).get(name).keySet()) {
	  Object[] objs = source.get(vendor).get(name).get(vid);
	  String cname = (String) objs[0];
	  TreeMap<String,byte[]> contents = (TreeMap<String,byte[]>) objs[1];
	  TreeSet<OsType> supports = (TreeSet<OsType>) objs[2];
	  
	  ClassLoader loader = new PluginClassLoader(contents);
	  try {
	    LogMgr.getInstance().log
	      (LogMgr.Kind.Plg, LogMgr.Level.Finer,
	       "Updating Plugin Class: " + cname + "\n" + 
	       "     Name = " + name + "\n" + 
	       "  Version = " + vid + "\n" + 
	       "   Vendor = " + vendor);
	    
	    Class cls = loader.loadClass(cname);
	    target.put(vendor, name, vid, new PluginData(cls, supports)); 
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
  }
  


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available editor plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getEditors() 
  {
    return getPlugins(pEditors);
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available action plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getActions() 
  {
    return getPlugins(pActions);
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available comparator plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getComparators() 
  {
    return getPlugins(pComparators);
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available tool plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getTools() 
  {
    return getPlugins(pTools);
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available archiver plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getArchivers() 
  {
    return getPlugins(pArchivers);
  }

  /**
   * Get the vender, names and version numbers of all plugins in the given table.
   */ 
  private TripleMap<String,String,VersionID,TreeSet<OsType>>
  getPlugins
  (
   TripleMap<String,String,VersionID,PluginData> plugins
  ) 
  {
    TripleMap<String,String,VersionID,TreeSet<OsType>> table = 
      new TripleMap<String,String,VersionID,TreeSet<OsType>>();

    for(String vendor : plugins.keySet()) {
      for(String name : plugins.get(vendor).keySet()) {
	for(VersionID vid : plugins.get(vendor).get(name).keySet()) {
	  PluginData data = plugins.get(vendor, name, vid);
	  table.put(vendor, name, vid, new TreeSet<OsType>(data.getSupports())); 
	}
      }
    }

    return table;
  }
   

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new editor plugin instance. <P> 
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
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no editor plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseEditor
  newEditor
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseEditor) newPlugin("Editor", pEditors, name, vid, vendor);
  }

  /**
   * Create a new action plugin instance. <P> 
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
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no action plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseAction
  newAction
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseAction) newPlugin("Action", pActions, name, vid, vendor);
  }

  /**
   * Create a new comparator plugin instance. <P> 
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
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no comparator plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseComparator
  newComparator
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseComparator) newPlugin("Comparator", pComparators, name, vid, vendor);
  }

  /**
   * Create a new tool plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseTool#getName BaseTool.getName} for the returned 
   * tool.
   *
   * @param name 
   *   The name of the tool plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the tool to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no tool plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseTool
  newTool
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseTool) newPlugin("Tool", pTools, name, vid, vendor);
  }

  /**
   * Create a new archiver plugin instance. <P> 
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
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no archiver plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseArchiver
  newArchiver
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseArchiver) newPlugin("Archiver", pArchivers, name, vid, vendor);
  }
  
  /**
   * Create a new plugin instance. <P> 
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
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws  PipelineException
   *   If no plugin can be found for the given <CODE>name</CODE> or instantiation fails.
   */
  private BasePlugin
  newPlugin
  (
   String ptype,
   TripleMap<String,String,VersionID,PluginData> table, 
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    String vend = vendor;
    if(vend == null) 
      vend = "Temerity";

    TreeMap<String,TreeMap<VersionID,PluginData>> plugins = table.get(vend);
    if(plugins == null) 
      throw new PipelineException
	("No plugins created by the (" + vend + ") vendor exist!");

    TreeMap<VersionID,PluginData> versions = plugins.get(name);
    if(versions == null) 
      throw new PipelineException
	("No " + ptype + " plugin named (" + name + ") created by the (" + vend + ") " + 
	 "vendor exists!");

    VersionID pvid = vid;
    if(pvid == null) 
      pvid = versions.lastKey();

    PluginData data = versions.get(pvid);
    if(data == null) {
      throw new PipelineException
	("Unable to find the " + ptype + " plugin (" + name + " v" + pvid + ") " + 
	 "created by the (" + vend + ") vendor!");
    }

    try {
      return (BasePlugin) data.getPluginClass().newInstance();  
    }
    catch (IllegalAccessException ex) {
      throw new PipelineException
	("Unable to access the constructor for plugin (" + name + " v" + pvid + ") " +
	 "created by the (" + vend + ") vendor!");
    }
    catch (InstantiationException ex) { 
      throw new PipelineException
	("Unable to instantiate the plugin (" + name + " v" + pvid + ")" +
	 "created by the (" + vend + ") vendor:\n" +
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached plugin class and supported operating system types.
   */ 
  private class
  PluginData
  {
    PluginData
    (
     Class cls, 
     TreeSet<OsType> supports
    ) 
    {
      pClass = cls;
      pSupports = supports;
    }

    Class 
    getPluginClass()
    {
      return pClass;
    }

    TreeSet<OsType> 
    getSupports()
    {
      return pSupports;
    }

    private Class            pClass;
    private TreeSet<OsType>  pSupports;
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
   * The cached Editor plugin data indexed by vendor, class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,PluginData>  pEditors; 

  /**
   * The cached Action plugin data indexed by vendor, class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,PluginData>  pActions; 

  /**
   * The cached Comparator plugin data indexed by vendor, class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,PluginData>  pComparators; 

  /**
   * The cached Tool plugin data indexed by vendor, class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,PluginData>  pTools; 

  /**
   * The cached Archiver plugin data indexed by vendor, class name and revision number.
   */ 
  private TripleMap<String,String,VersionID,PluginData>  pArchivers; 


}


