package us.temerity.pipeline.plugin;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   A F T E R   F X   A C T I O N   U T I L S                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Superclass of Action plugins which interact with After Effects scenes.
 * <p>
 * This class provides convenience methods which make it easier to write Action plugins
 * which create dynamic JavaScripts scripts and After Effects scenes.
 */
public 
class AfterFXActionUtils
  extends CompositeActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct with the given name, version, vendor and description.
   * 
   * @param name
   *                The short name of the action.
   * @param vid
   *                The action plugin revision number.
   * @param vendor
   *                The name of the plugin vendor.
   * @param desc
   *                A short description of the action.
   */
  protected 
  AfterFXActionUtils
  (
    String name, 
    VersionID vid, 
    String vendor, 
    String desc
  )
  {
    super(name, vid, vendor, desc);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T Y    M E T H O D S                                                       */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the abstract path to the single primary After Effects scene associated with the
   * target node.
   * 
   * @param agenda
   *                The agenda to be accomplished by the action.
   * @return The path to the target After Effects scene.
   */
  public Path 
  getAfterFXSceneTargetPath
  (
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimaryTargetPath(agenda, "aep", "After Effects scene file");
  }
  
  /**
   * Get the abstract path to the single primary JavaScript script associated with a source
   * node specified by the given parameter.
   * 
   * @param pname
   *                The name of the single valued MEL parameter.
   * @param agenda
   *                The agenda to be accomplished by the action.
   * @return The path to the MEL script or null if none was specified.
   */
  public Path
  getJavaScriptSourcePath
  (
    String pname,
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimarySourcePath(pname, agenda, "jsx", "JavaScript file");
  }

  /**
   * Get the abstract path to the single primary After Effects Scene associated with a
   * source node specified by the given parameter.
   * 
   * @param pname
   *                The name of the single valued Scene parameter.
   * @param agenda
   *                The agenda to be accomplished by the action.
   * @return The path to the MEL script or null if none was specified.
   */
  public Path
  getAfterFXSceneSourcePath
  (
    String pname,
    ActionAgenda agenda
  )
    throws PipelineException
  {
    return getPrimarySourcePath(pname, agenda, "aep", "After Effects scene file");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2432054682863935407L;
  
  
  public static final String aAfterFXScene = "AfterFXScene";
  public static final String aPreRenderScript = "PreRenderScript";
  public static final String aPostRenderScript = "PostRenderScript";
}
