package us.temerity.pipeline.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class MayaMiExportStage
  extends StandardStage
{
  protected 
  MayaMiExportStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    FrameRange range,
    Integer padding,
    String mayaScene,
    String exportSet
  )
    throws PipelineException
  {
    super(name, 
      desc, 
      stageInformation, 
      context, 
      client, 
      nodeName, 
      range, 
      padding, 
      "mi", 
      null, 
      new PluginContext("MayaMiExport"));
    setMayaScene(mayaScene);
    if (exportSet != null)
      addSingleParamValue("ExportSet", exportSet);
  }
  
  protected 
  MayaMiExportStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String mayaScene,
    String exportSet
  )
    throws PipelineException
  {
    super(name, 
      desc, 
      stageInformation, 
      context, 
      client, 
      nodeName, 
      "mi", 
      null, 
      new PluginContext("MayaMiExport"));
    setMayaScene(mayaScene);
    if (exportSet != null)
      addSingleParamValue("ExportSet", exportSet);
  }
  
  private void 
  setMayaScene
  (
   String mayaScene 
  )
    throws PipelineException
  {
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", mayaScene);
  }
  
  /**
   * Utility method for linking a node and setting the <code>PreExportMEL</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  public void 
  setPreExportMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PreRenderMEL", melScript);
    }
  }
  
  /**
   * Utility method for linking a node and setting the <code>PostExport</code> single
   * parameter value that many Maya Actions share.
   * <p>
   * The name of a MEL script is passed to this method. That MEL script is then added as a
   * link to the stage and the correct single parameter is set on the Action. This should only
   * be called after the stage's Action has already been created.
   * 
   * @param melScript
   *        The value for the parameter.
   */
  public void 
  setPostExportMel
  (
    String melScript
  ) 
    throws PipelineException
  {
    if(melScript != null) {
      addLink(new LinkMod(melScript, LinkPolicy.Dependency));
      addSingleParamValue("PostRenderMEL", melScript);
    }
  }
  
  @SuppressWarnings("unchecked")
  public void
  setGeoAllExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, true);
    values.put(aGroups, false);
    values.put(aLights, false);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, true);
    values.put(aLightInstances, false);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, false);
    values.put(aCustomPhenomena, false);
    values.put(aExportMaterials, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setMentalRayLightsExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, false);
    values.put(aGroups, false);
    values.put(aLights, true);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, false);
    values.put(aLightInstances, true);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, true);
    values.put(aCustomPhenomena, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setLightsExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, false);
    values.put(aGroups, false);
    values.put(aLights, true);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, true);
    values.put(aFunctionDecls, true);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, false);
    values.put(aLightInstances, true);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, true);
    values.put(aCustomPhenomena, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setCameraExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, false);
    values.put(aGroups, false);
    values.put(aLights, false);
    values.put(aCameras, true);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, false);
    values.put(aLightInstances, false);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, true);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, false);
    values.put(aCustomPhenomena, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setOptionsExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, false);
    values.put(aGroups, false);
    values.put(aLights, false);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, true);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, false);
    values.put(aLightInstances, false);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, false);
    values.put(aCustomPhenomena, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setGeoDefExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, true);
    values.put(aGroups, false);
    values.put(aLights, false);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, false);
    values.put(aLightInstances, false);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, false);
    values.put(aCustomPhenomena, false);
    values.put(aExportMaterials, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setGeoInstExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, false);
    values.put(aIncludes, false);
    values.put(aVersions, false);
    values.put(aTextures, false);
    values.put(aObjects, true);
    values.put(aGroups, false);
    values.put(aLights, false);
    values.put(aCameras, false);
    values.put(aMaterials, false);
    values.put(aOptions, false);
    values.put(aFunctions, false);
    values.put(aFunctionDecls, false);
    values.put(aPhenomenaDecls, false);
    values.put(aUserData, false);
    values.put(aObjectInstances, true);
    values.put(aLightInstances, false);
    values.put(aGroupInstances, false);
    values.put(aCameraInstances, false);
    values.put(aFunctionInstances, false);
    values.put(aRender, false);
    values.put(aCustomText, false);
    values.put(aCustomShaders, false);
    values.put(aCustomPhenomena, false);
    setPresets(pAction, values);
  }
  
  @SuppressWarnings("unchecked")
  public void
  setAllExport()
  {
    TreeMap<String, Comparable> values = new TreeMap<String, Comparable>();
    values.put(aLinks, true);
    values.put(aIncludes, true);
    values.put(aVersions, true);
    values.put(aTextures, true);
    values.put(aObjects, true);
    values.put(aGroups, true);
    values.put(aLights, true);
    values.put(aCameras, true);
    values.put(aMaterials, true);
    values.put(aOptions, true);
    values.put(aFunctions, true);
    values.put(aFunctionDecls, true);
    values.put(aPhenomenaDecls, true);
    values.put(aUserData, true);
    values.put(aObjectInstances, true);
    values.put(aLightInstances, true);
    values.put(aGroupInstances, true);
    values.put(aCameraInstances, true);
    values.put(aFunctionInstances, true);
    values.put(aRender, true);
    values.put(aCustomText, true);
    values.put(aCustomShaders, true);
    values.put(aCustomPhenomena, true);
    setPresets(pAction, values);
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aTextFile;
  }

  
  private static final long serialVersionUID = -570821544247319782L;
  
  public static final String sExportSet             = "ExportSet";
  public static final String aExportChildren        = "ExportChildren";
  public static final String aExportMaterials       = "ExportMaterials";
  public static final String aExportConnections     = "ExportConnections";
  public static final String aLinks                 = "Links";
  public static final String aIncludes              = "Includes";
  public static final String aVersions              = "Versions";
  public static final String aTextures              = "Textures";
  public static final String aObjects               = "Objects";
  public static final String aGroups                = "Groups";
  public static final String aLights                = "Lights";
  public static final String aCameras               = "Cameras";
  public static final String aMaterials             = "Materials";
  public static final String aOptions               = "Options";
  public static final String aFunctions             = "Functions";
  public static final String aFunctionDecls         = "FunctionDecls";
  public static final String aPhenomenaDecls        = "PhenomenaDecls";
  public static final String aUserData              = "UserData";
  public static final String aObjectInstances       = "ObjectInstances";
  public static final String aLightInstances        = "LightInstances";
  public static final String aGroupInstances        = "GroupInstances";
  public static final String aCameraInstances       = "CameraInstances";
  public static final String aFunctionInstances     = "FunctionInstances";
  public static final String aRender                = "Render";
  public static final String aCustomText            = "CustomText";
  public static final String aCustomShaders         = "CustomShaders";
  public static final String aCustomPhenomena       = "CustomPhenomena";
  //public static final String aExportExactHierarchy  = "ExportExactHierarchy";
  //public static final String aExportFullDagPath     = "ExportFullDagPath";
  //public static final String aExportTexturesFirst   = "ExportTexturesFirst";
  //public static final String aExportPostEffects     = "ExportPostEffects";
  //public static final String aExportAssignedOnly    = "ExportAssignedOnly";
  //public static final String aExportVisibleOnly     = "ExportVisibleOnly";
  //public static final String aOptimizeAnimDetection = "OptimizeAnimDetection";
  //public static final String aUseDefaultLight       = "UseDefaultLight";
}
