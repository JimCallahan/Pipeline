package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultProjectNames.GlobalsType;

public 
interface BuildsProjectNames
{
  public String
  getDummyFile();
  
  public String
  getAssetModelTTSetup
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetModelTTSetup();
  
  public String
  getAssetModelTTGlobals
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetModelTTGlobals();
  
  public String
  getAssetRigAnimSetup
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetRigAnimSetup();
  
  public String
  getAssetRigAnimGlobals
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetRigAnimGlobals();
  
  public String
  getAssetShaderTTSetup
  (
    String assetName,
    String assetType
  );
  
  public String
  getAssetShaderTTSetup();
  
  public String
  getAssetShaderTTGlobals
  (
    String assetName,
    String assetType,
    GlobalsType type
  );
  
  public String
  getAssetShaderTTGlobals
  (
    GlobalsType type  
  );
  
  public String
  getTaskName
  (
    String assetName,
    String assetType
  );
  
  public String
  getModelingTaskName();
  
  public String
  getRiggingTaskName();
  
  public String
  getShadingTaskName();
  
  public String
  getLayoutTaskName();
  
  public String
  getAnimTaskName();
  
  public String
  getLightingTaskName();
  
  public String
  getCompositingTaskName();
  
  /**
   * @return the name of the finalize mel script
   */
  public String
  getFinalizeScriptName
  (
    String assetName,
    String assetType
  );
  
  public String
  getFinalizeScriptName();
  
  /**
   * @return the name of the low rez finalize mel script
   */
  public String
  getLowRezFinalizeScriptName
  (
    String assetName,
    String assetType
  );
  
  public String
  getLowRezFinalizeScriptName();

  
  /**
   * @return the Mental Ray initialization mel script
   */
  public String
  getMRayInitScriptName();
  
  /**
   * @return the name of the model verification mel Script
   */
  public String
  getModelVerificationScriptName();
  
  public String
  getModelVerificationScriptName
  (
    String assetName,
    String assetType
  );
  
  /**
   * @return the name of the rig verification mel Script
   */
  public String
  getRigVerificationScriptName();
  
  public String
  getRigVerificationScriptName
  (
    String assetName,
    String assetType
  );
  
  /**
   * @return the name of the shader verification mel Script
   */
  public String
  getShaderVerificationScriptName();
  
  public String
  getShaderVerificationScriptName
  (
    String assetName,
    String assetType
  );
  
  /**
   * @return the name of the asset verification mel Script
   */
  public String
  getAssetVerificationScriptName();
  
  public String
  getAssetVerificationScriptName
  (
    String assetName,
    String assetType
  );
  
  /**
   * @return the placeholder generation mel script
   */
  public String
  getPlaceholderScriptName();
  
  /**
   * @return the auto-rigging mel script
   */
  public String 
  getFinalRigScriptName();
  
  public String 
  getAutoRigScriptName
  (
    String assetName,
    String assetType
  );
  
  /**
   * @return the shader copying mel script name
   */
  public String
  getShaderCopyScriptName();
  
  public String
  getRigCopyScriptName();
  
  public String
  getRemoveReferenceScriptName();

  public String 
  getPlaceholderSkelScriptName();

  public String 
  getPlaceholderTTCircleScriptName();

  public String 
  getPlaceholderTTCenterScriptName();

  public String 
  getEffectsTaskName();

  public String 
  getPlaceholderCameraScriptName();

  public String 
  getAnimGlobals();

  public String 
  getLgtGlobals();
}
