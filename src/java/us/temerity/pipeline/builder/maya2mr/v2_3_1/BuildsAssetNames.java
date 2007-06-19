package us.temerity.pipeline.builder.maya2mr.v2_3_1;

import us.temerity.pipeline.FileSeq;
import us.temerity.pipeline.PipelineException;

public 
interface BuildsAssetNames
{
  public void 
  generateNames() 
    throws PipelineException;

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
   * @return the placeholder generation mel script
   */
  public String
  getPlaceholderScriptName();
  
  /**
   * @return the auto-rigging mel script
   */
  public String 
  getAutoRigScriptName();
}
