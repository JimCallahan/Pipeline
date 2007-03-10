/*
 * Created on Nov 2, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders.names
 * 
 */
package us.temerity.pipeline.builder.names;

import us.temerity.pipeline.*;

public interface 
BuildsAssetNames
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
}
