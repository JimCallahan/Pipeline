// $Id: BasePluginMgrClient.java,v 1.30 2009/12/18 07:20:55 jim Exp $
  
package us.temerity.pipeline;

import java.util.TreeMap;
import java.util.TreeSet;

import us.temerity.pipeline.builder.BaseBuilderCollection;
import us.temerity.pipeline.message.*;

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
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link #performTransaction} like 
   *   {@link #performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   * 
   * @param clientID
   *   The clientID sent to the server.
   **/
  protected 
  BasePluginMgrClient
  (
   boolean forceLongTransactions, 
   String clientID
  )
  {
    super(PackageInfo.sPluginServer, PackageInfo.sPluginPort, forceLongTransactions, 
          PluginRequest.Disconnect, PluginRequest.Shutdown, clientID);

    pEditors            = new PluginDataCache(PluginType.Editor);  
    pActions            = new PluginDataCache(PluginType.Action);  
    pComparators        = new PluginDataCache(PluginType.Comparator);  
    pTools              = new PluginDataCache(PluginType.Tool);   
    pAnnotations        = new PluginDataCache(PluginType.Annotation);   
    pArchivers          = new PluginDataCache(PluginType.Archiver);  
    pMasterExts         = new PluginDataCache(PluginType.MasterExt);  
    pQueueExts          = new PluginDataCache(PluginType.QueueExt);
    pKeyChoosers        = new PluginDataCache(PluginType.KeyChooser);
    pBuilderCollections = new PluginDataCache(PluginType.BuilderCollection);
    
    
    pBuilderCollectionLayouts = 
      new TripleMap<String,String,VersionID,LayoutGroup>();

    pAnnotationPermissions = 
      new TripleMap<String,String,VersionID,AnnotationPermissions>();

    pAnnotationContexts = 
      new TripleMap<String,String,VersionID,TreeSet<AnnotationContext>>();

    pPluginStatus = 
      new DoubleMap<PluginType,PluginID,PluginStatus>();
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
     
      pEditors.updatePlugins(rsp.getEditors()); 
      pActions.updatePlugins(rsp.getActions()); 
      pComparators.updatePlugins(rsp.getComparators());
      pTools.updatePlugins(rsp.getTools()); 
      pAnnotations.updatePlugins(rsp.getAnnotations()); 
      pArchivers.updatePlugins(rsp.getArchivers()); 
      pMasterExts.updatePlugins(rsp.getMasterExts()); 
      pQueueExts.updatePlugins(rsp.getQueueExts());
      pKeyChoosers.updatePlugins(rsp.getKeyChoosers());
      pBuilderCollections.updatePlugins(rsp.getBuilderCollections());
      
      pBuilderCollectionLayouts.putAll(rsp.getBuilderCollectionLayouts());

      pAnnotationPermissions.putAll(rsp.getAnnotationPermissions());
      pAnnotationContexts.putAll(rsp.getAnnotationContexts());

      pPluginStatus.putAll(rsp.getPluginStatus());
     
      pCycleID = rsp.getCycleID();
    }
    else {
      handleFailure(obj);
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
    return pEditors.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available action plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getActions() 
  {
    return pActions.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available comparator plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getComparators() 
  {
    return pComparators.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available tool plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getTools() 
  {
    return pTools.getPlugins();
  }

  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available annotation plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getAnnotations() 
  {
    return pAnnotations.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available archiver plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getArchivers() 
  {
    return pArchivers.getPlugins();
  }

  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available Master Extension plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getMasterExts() 
  {
    return pMasterExts.getPlugins();
  }

  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available Queue Extension plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getQueueExts() 
  {
    return pQueueExts.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available Key Chooser plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getKeyChoosers() 
  {
    return pKeyChoosers.getPlugins();
  }
  
  /**
   * Get the vender, names, version numbers and supported operating system types 
   * of all available Builder Collection plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<OsType>>
  getBuilderCollections() 
  {
    return pBuilderCollections.getPlugins();
  }

   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the vender, names, version numbers and layout group of all available Builder 
   * Collection plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,LayoutGroup>
  getBuilderCollectionLayouts() 
  {
    return new TripleMap<String,String,VersionID,LayoutGroup>(pBuilderCollectionLayouts);
  }
  
  /**
   * Get the vender, names, version numbers and permissions of all available 
   * Annotation plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,AnnotationPermissions>
  getAnnotationPermissions() 
  {
    return new TripleMap<String,String,VersionID,AnnotationPermissions>
                 (pAnnotationPermissions);
  }

  /**
   * Get the vender, names, version numbers and contexts of all available 
   * Annotation plugins. <P> 
   */ 
  public synchronized TripleMap<String,String,VersionID,TreeSet<AnnotationContext>>
  getAnnotationContexts() 
  {
    return new TripleMap<String,String,VersionID,TreeSet<AnnotationContext>>
                 (pAnnotationContexts);
  }

  /**
   * The PluginStatus of a plugin indexed by PluginType and PluginID.
   */
  public synchronized DoubleMap<PluginType,PluginID,PluginStatus>
  getPluginStatus()
  {
    return new DoubleMap<PluginType,PluginID,PluginStatus>(pPluginStatus);
  }
 

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new editor plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseEditor#getName() BaseEditor.getName} for the 
   * returned editor.
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
    return (BaseEditor) pEditors.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new editor plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no editor plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseEditor
  newEditor
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newEditor(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new action plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseAction#getName() BaseAction.getName} for the 
   * returned action.
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
    return (BaseAction) pActions.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new action plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no action plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseAction
  newAction
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newAction(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new comparator plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseComparator#getName() BaseComparator.getName} for the 
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
    return (BaseComparator) pComparators.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new comparator plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no comparator plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseComparator
  newComparator
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newComparator(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new tool plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseTool#getName() BaseTool.getName} for the returned 
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
    return (BaseTool) pTools.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new tool plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no tool plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseTool
  newTool
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newTool(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new annotation plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseAnnotation#getName() BaseAnnotation.getName} for the 
   * returned annotation.
   *
   * @param name 
   *   The name of the annotation plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the annotation to instantiate or <CODE>null</CODE> for the 
   *   latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no annotation plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseAnnotation
  newAnnotation
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseAnnotation) pAnnotations.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new annotation plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no annotation plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseAnnotation
  newAnnotation
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newAnnotation(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new archiver plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseArchiver#getName() BaseArchiver.getName} for the 
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
    return (BaseArchiver) pArchivers.newPlugin(name, vid, vendor);
  }
  
  /**
   * Create a new archiver plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no archiver plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseArchiver
  newArchiver
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newArchiver(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new master extension plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseMasterExt#getName() BaseMasterExt.getName} for the 
   * returned master extension.
   *
   * @param name 
   *   The name of the master extension plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the master extension to instantiate 
   *   or <CODE>null</CODE> for the latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no master extension plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseMasterExt
  newMasterExt
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseMasterExt) pMasterExts.newPlugin(name, vid, vendor);
  }
  
  /**
   * Create a new master extension plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no master extension plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseMasterExt
  newMasterExt
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newMasterExt(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new queue extension plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseQueueExt#getName() BaseQueueExt.getName} for the 
   * returned queue extension.
   *
   * @param name 
   *   The name of the queue extension plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the queue extension to instantiate 
   *   or <CODE>null</CODE> for the latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no queue extension plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseQueueExt
  newQueueExt
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseQueueExt) pQueueExts.newPlugin(name, vid, vendor);
  }
  
  /**
   * Create a new queue extension plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no queue extension plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseQueueExt
  newQueueExt
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newQueueExt(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new key chooser plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseKeyChooser#getName() BaseKeyChooser.getName} for the 
   * returned key chooser.
   *
   * @param name 
   *   The name of the key chooser plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the key chooser to instantiate 
   *   or <CODE>null</CODE> for the latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no key chooser plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseKeyChooser
  newKeyChooser
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseKeyChooser) pKeyChoosers.newPlugin(name, vid, vendor);
  }
  
  /**
   * Create a new key chooser plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no key chooser plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseKeyChooser
  newKeyChooser
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newKeyChooser(pluginID.getName(), pluginID.getVersionID(), pluginID.getVendor());
  }

  /**
   * Create a new builder collection plugin instance. <P> 
   * 
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseBuilderCollection#getName() 
   * BaseBuilderCollection.getName} for the returned builder collection.
   *
   * @param name 
   *   The name of the builder collection plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the builder collection to instantiate 
   *   or <CODE>null</CODE> for the latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no builder collection plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseBuilderCollection
  newBuilderCollection
  (
   String name, 
   VersionID vid, 
   String vendor
  ) 
    throws PipelineException
  {
    return (BaseBuilderCollection) pBuilderCollections.newPlugin(name, vid, vendor);
  }

  /**
   * Create a new builder collection plugin instance using an existing classloader context. 
   * <P> 
   * This method exists to allow builder collections to instantiate builders in other 
   * collections that have some classes in common.  Java treats the same class in different 
   * class loaders as different classes.  This means that a builder with non-standard 
   * constructor arguments cannot be invoked cross-collection (since the arguments will
   * resolve to different classes in each Collection).  
   * <p>
   * Note that the <CODE>name</CODE> argument is not the name of the class, but rather the 
   * name obtained by calling {@link BaseBuilderCollection#getName() 
   * BaseBuilderCollection.getName} for the returned builder collection.
   *
   * @param parentLoader
   *   The classloader which will be the parent classloader for the plugin.  
   *
   * @param name 
   *   The name of the builder collection plugin to instantiate.  
   * 
   * @param vid
   *   The revision number of the builder collection to instantiate 
   *   or <CODE>null</CODE> for the latest version.
   * 
   * @param vendor
   *   The name of the plugin vendor or <CODE>null</CODE> for Temerity.
   * 
   * @throws PipelineException
   *   If no builder collection plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseBuilderCollection
  newBuilderCollection
  (
   ClassLoader parentLoader, 
   String name, 
   VersionID vid, 
   String vendor
  )
    throws PipelineException
  {
    BaseBuilderCollection builderCollection = 
      (BaseBuilderCollection) pBuilderCollections.newPlugin(name, vid, vendor);

    Class cls = builderCollection.getClass();
    String cname = cls.getName();

    PluginClassLoader childLoader = (PluginClassLoader) cls.getClassLoader();
    PluginClassLoader loader = 
      new PluginClassLoader(childLoader, parentLoader);

    try {
      Class childBuilderCollection = loader.loadClass(cname);

      return (BaseBuilderCollection) childBuilderCollection.newInstance();
    }
    catch(ClassNotFoundException ex) {
      throw new PipelineException(ex);
    }
    catch(InstantiationException ex) {
      throw new PipelineException(ex);
    }
    catch(IllegalAccessException ex) {
      throw new PipelineException(ex);
    }
  }
  
  /**
   * Create a new builder collection plugin instance. <P> 
   * 
   * @param pluginID
   *   The unique plugin identifier. 
   * 
   * @throws PipelineException
   *   If no builder collection plugin can be found or instantiation fails for some reason.
   */
  public synchronized BaseBuilderCollection
  newBuilderCollection
  (
   PluginID pluginID
  ) 
    throws PipelineException
  {
    return newBuilderCollection(pluginID.getName(), 
                                pluginID.getVersionID(), 
                                pluginID.getVendor());
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
    return ("Unable to contact the plpluginmgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Cached plugin class and supported operating system types.
   */ 
  private 
  class PluginData
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


  /**
   * The loaded plugins indexed by plugin name and revision number.
   */
  private 
  class PluginDataCache
    extends TripleMap<String,String,VersionID,PluginData>
  {
    /**
     * Construct a new plugin cache.
     *
     * @param ptype 
     *   The kind of plugin being instantiated: Editor, Comparator, Action, Tool, Annotation, 
     *   Archiver, MasterExt, QueueExt or KeyChooser.
     */
    public 
    PluginDataCache
    (
     PluginType ptype
    ) 
    {
      super();
      pPluginType = ptype;
    }

    /**
     * Copy the new or updated plugin classes into the cached plugin class table.
     */ 
    public void 
    updatePlugins
    (
     TripleMap<String,String,VersionID,Object[]> source
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
	    TreeMap<String,Long> resources = (TreeMap<String,Long>) objs[3];

	    PluginID pid = (PluginID) objs[4];
	    //PluginType ptype = (PluginType) objs[5];
            
            ClassLoader loader = 
	      new PluginClassLoader(contents, resources, 
	                            pid, pPluginType, null);

            try {
              LogMgr.getInstance().log
                (LogMgr.Kind.Plg, LogMgr.Level.Finer,
                 "Updating Plugin Class: " + cname + "\n" + 
                 "     Name = " + name + "\n" + 
                 "  Version = " + vid + "\n" + 
                 "   Vendor = " + vendor);
              
              Class cls = loader.loadClass(cname);
              put(vendor, name, vid, new PluginData(cls, supports)); 
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

    /**
     * Get the vender, names and version numbers of all plugins in the given table.
     */ 
    public TripleMap<String,String,VersionID,TreeSet<OsType>>
    getPlugins() 
    {
      TripleMap<String,String,VersionID,TreeSet<OsType>> table = 
        new TripleMap<String,String,VersionID,TreeSet<OsType>>();
      
      for(String vendor : keySet()) {
        for(String name : get(vendor).keySet()) {
          for(VersionID vid : get(vendor).get(name).keySet()) {
            PluginData data = get(vendor, name, vid);
            table.put(vendor, name, vid, new TreeSet<OsType>(data.getSupports())); 
          }
        }
      }
      
      return table;
    }

    /**
     * Create a new plugin instance. <P> 
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
    public BasePlugin
    newPlugin
    (
     String name, 
     VersionID vid, 
     String vendor
    ) 
      throws PipelineException
    {
      String vend = vendor;
      if(vend == null) 
        vend = "Temerity";
      
      TreeMap<String,TreeMap<VersionID,PluginData>> plugins = get(vend);
      if(plugins == null) 
        throw new PipelineException
          ("Unable to instantiate the (" + name + ((vid!=null)?(" v" + vid):"") + ") " + 
           "plugin, because no plugins created by the (" + vend + ") vendor exist!");
      
      TreeMap<VersionID,PluginData> versions = plugins.get(name);
      if(versions == null) 
        throw new PipelineException
          ("No " + pPluginType + " plugin named (" + name + ") created by the " +
           "(" + vend + ") vendor exists!");
      
      VersionID pvid = vid;
      if(pvid == null) 
        pvid = versions.lastKey();
      
      PluginData data = versions.get(pvid);
      if(data == null) {
        throw new PipelineException
          ("Unable to find the " + pPluginType + " plugin (" + name + " v" + pvid + ") " + 
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

    static final long serialVersionUID = 2146004185163196669L;

    private PluginType pPluginType;
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
   * The cached plugins. 
   */ 
  private PluginDataCache  pEditors; 
  private PluginDataCache  pActions; 
  private PluginDataCache  pComparators; 
  private PluginDataCache  pTools; 
  private PluginDataCache  pAnnotations; 
  private PluginDataCache  pArchivers; 
  private PluginDataCache  pMasterExts; 
  private PluginDataCache  pQueueExts;
  private PluginDataCache  pKeyChoosers;
  private PluginDataCache  pBuilderCollections;
  
  /**
   * The layout group for all available Builders contained in a BuilderCollection
   * indexed by vender, plugin name and revision number.
   */ 
  private TripleMap<String,String,VersionID,LayoutGroup> pBuilderCollectionLayouts;
  
  /**
   * The AnnotationPermissions associated with all available Annotations indexed by 
   * vender, plugin name and revision number.
   */ 
  private TripleMap<String,String,VersionID,AnnotationPermissions> pAnnotationPermissions;
 
  /**
   * The AnnotationContext associated with all available Annotations indexed by 
   * vender, plugin name and revision number.
   */ 
  private TripleMap<String,String,VersionID,TreeSet<AnnotationContext>> pAnnotationContexts;

  /**
   * The PluginStatus for a plugin indexed by PluginType and PluginID.
   */
  private DoubleMap<PluginType,PluginID,PluginStatus> pPluginStatus;

}


