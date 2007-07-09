package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import us.temerity.pipeline.FileSeq;
import us.temerity.pipeline.PipelineException;

public 
interface BuildsAssetNames
{
  /**
   * @return the assetName
   */
  public String 
  getAssetName();
  
  /**
   * @return the assetName
   */
  public String 
  getNameSpace();

  /**
   * @return the assetType
   */
  public String 
  getAssetType();
  
  /**
   * @return the finalNodeName
   */
  public String 
  getFinalNodeName();
  
  /**
   * @return the lowRezFinalNodeName
   */
  public String 
  getLowRezFinalNodeName();

  /**
   * @return the lowRezMaterialNodeName
   */
  public String 
  getLowRezMaterialNodeName();

  /**
   * @return the lowRezModelNodeName
   */
  public String 
  getLowRezModelNodeName();

  /**
   * @return the lowRezRigNodeName
   */
  public String 
  getLowRezRigNodeName();

  /**
   * @return the materialNodeName
   */
  public String 
  getMaterialNodeName();

  /**
   * @return the materialExportNodeName
   */
  public String 
  getMaterialExportNodeName();
  
  /**
   * @return the modelNodeName
   */
  public String 
  getModelNodeName();
  
  /**
   * @return the headModelNodeName
   */
  public String 
  getHeadModelNodeName();
  
  /**
   * @return the headModelNodeName
   */
  public String 
  getBlendShapeModelNodeName();

  /**
   * @return the project
   */
  public String 
  getProject();

  /**
   * @return the rigNodeName
   */
  public String 
  getRigNodeName();
  
  /**
   * @return the rigInfoNodeName
   */
  public String 
  getRigInfoNodeName();
  
  /**
   * @return the skeletonNodeName
   */
  public String 
  getSkeletonNodeName();
  
  /**
   * @return the shaderExportNodeName
   */
  public String 
  getShaderExportNodeName();

  /**
   * @return the shaderIncludeGroupSecSeq
   */
  public FileSeq 
  getShaderIncludeGroupSecSeq();

  /**
   * @return the shaderIncludeNodeName
   */
  public String 
  getShaderIncludeNodeName();

  /**
   * @return the shaderNodeName
   */
  public String 
  getShaderNodeName();

  /**
   * @return the textureNodeName
   */
  public String 
  getTextureNodeName();
  
  /**
   * @return the name of the finalize mel script
   */
  public String
  getFinalizeScriptName();
  
  /**
   * @return the name of the low rez finalize mel script
   */
  public String
  getLowRezFinalizeScriptName();
  
  /**
   * @return the Mental Ray initialization mel script
   */
  public String
  getMRInitScriptName();
  
  /**
   * @return the name of the model verification mel Script
   */
  public String
  getModelVerificationScriptName();
  
  /**
   * @return the name of the rig verification mel Script
   */
  public String
  getRigVerificationScriptName();
  
  /**
   * @return the name of the shader verification mel Script
   */
  public String
  getShaderVerificationScriptName();
  
  /**
   * @return the placeholder generation mel script
   */
  public String
  getPlaceholderScriptName();
  
  /**
   * @return the auto-rigging mel script
   */
  public String 
  getAutoRigScriptName();
  
  /*
   * Model Nodes in Approval Method
   */
  
  /**
   * @return the model edit node name
   */
  public String
  getModelEditNodeName();
  
  /**
   * @return the model verify node name
   */
  public String
  getModelVerifyNodeName();
  
  /**
   * @return the model piece node name
   */
  public String
  getModelPieceNodeName(String piece);

  /**
   * @return the final model name.
   */
  public String
  getModelFinalNodeName();
  
  /**
   * @return the model approve node name
   */
  public String
  getModelApproveNodeName();
  
  /**
   * @return the model submit node name.
   */
  public String
  getModelSubmitNodeName();
  
  /**
   * @return the model turntable node name
   */
  public String
  getModelTTNodeName();
  
  /**
   * @return the model turntable images node name.
   */
  public String
  getModelTTImagesNodeName();
  
  /*
   * Rig nodes in the approval method
   */
  
  /**
   * @return The rig edit node name.
   */
  public String
  getRigEditNodeName();
  
  /**
   * @return the re-rig node name.
   */
  public String
  getReRigNodeName();
  
  /**
   * @return The rig submit node name.
   */
  public String
  getRigSubmmitNodeName();
  
  /**
   * @return the rig approve node name.
   */
  public String
  getRigApproveNodeName();
  
  /**
   * @return the rig anim node name.
   */
  public String
  getRigAnimNodeName();
  
  /**
   * @return the rig anim images node name.
   */
  public String
  getRigAnimImagesNodeName();
  
  /**
   * @return the rig final node name.
   */
  public String
  getRigFinalNodeName();
  
  /**
   * @return the model MI node name.
   */
  public String
  getModelMiNodeName();
  
  /**
   * @return the rig anim curve node name.
   */
  public String
  getRigAnimCurvesNodeName();
  
  /**
   * @return the rig anim FBX node name.
   */
  public String
  getRigAnimFBXNodeName();
  
  public String
  getRigMELNodeName();
  
  public String
  getReRigMELNodeName();
}