package us.temerity.pipeline.builder.maya2mr;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

public 
class DefaultAssetNames
  extends BaseNames
  implements BuildsAssetNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  DefaultAssetNames()
    throws PipelineException 
  {
    super("DefaultAssetNames", 
          "The basic naming class for an asset provided by Temerity");
    {
      UtilityParam param =
	new StringUtilityParam
	(aProjectName,
	 "The Name of the Project the asset should live in", 
	 null);
      addParam(param);
    }
    {
      UtilityParam param = 
	new StringUtilityParam
	(aAssetName, 
	 "The Name of the asset", 
	 null);
      addParam(param);
    }
    {
      UtilityParam param = 
	new StringUtilityParam
	(aAssetType, 
	 "The Type of the asset", 
	 null);
      addParam(param);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  generateNames() 
  {
    setGlobalContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    pAssetName =  getStringParamValue(new ParamMapping(aAssetName));
    pAssetType =  getStringParamValue(new ParamMapping(aAssetType));
    
    pNameSpace = pAssetName;

    String assetStart = "/projects/" + pProject + "/assets/" + pAssetType + "/" + pAssetName;
    String melStart = "/projects/" + pProject + "/assets/tools/mel/";

    pModelNodeName = assetStart + "/model/" + pAssetName + "_mod";
    pHeadModelNodeName = assetStart + "/model/" + pAssetName + "_mod_head";
    pBlendShapeModelNodeName = assetStart + "/model/" + pAssetName + "_mod_blends";
    pRigNodeName = assetStart + "/rig/" + pAssetName + "_rig";
    pRigInfoNodeName = assetStart + "/rig/" + pAssetName + "_info";
    pSkeletonNodeName = assetStart + "/rig/" + pAssetName + "_skel";
    pMaterialNodeName = assetStart + "/material/" + pAssetName + "_mat";
    pMaterialExportNodeName = assetStart + "/material/" + pAssetName + "_matExp";
    pFinalNodeName = assetStart + "/" + pAssetName;

    pLowRezModelNodeName = assetStart + "/model/" + pAssetName + "_mod_lr";
    pLowRezRigNodeName = assetStart + "/rig/" + pAssetName + "_rig_lr";
    pLowRezMaterialNodeName = assetStart + "/material/" + pAssetName + "_mat_lr";
    pLowRezFinalNodeName = assetStart + "/" + pAssetName + "_lr";

    pShaderNodeName = assetStart + "/shader/" + pAssetName + "_shd";
    pShaderExportNodeName = assetStart + "/shader/" + pAssetName + "_shdExp";
    pShaderIncludeNodeName = assetStart + "/shader/shaders/" + pAssetName + "_shdInc";
    pShaderIncludeGroupSecSeq = new FileSeq("mi_shader_defs", "mi");

    pTextureNodeName = assetStart + "/textures/" + pAssetName + "_tex";
    
    pFinalizeMELNodeName = melStart + "finalize-" + pAssetType;
    pLowRezFinalizeMELNodeName = melStart + "finalize-" + pAssetType + "_lr";
    pMRInitMELNodeName = melStart + "mr-init";
    pAutoRigMELNodeName = melStart + "auto-rig";
    pPlaceholderMELNodeName = null;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * @return the pAssetName
   */
  public String 
  getAssetName()
  {
    return pAssetName;
  }

  /**
   * @return the pAssetType
   */
  public String 
  getAssetType()
  {
    return pAssetType.toString();
  }
  
  /**
   * @return the Name Space
   */
  public String 
  getNameSpace()
  {
    return pNameSpace;
  }

  /**
   * @return the pFinalNodeName
   */
  public String 
  getFinalNodeName()
  {
    return pFinalNodeName;
  }

  /**
   * @return the pLowRezFinalNodeName
   */
  public String 
  getLowRezFinalNodeName()
  {
    return pLowRezFinalNodeName;
  }

  /**
   * @return the pLowRezMaterialNodeName
   */
  public String 
  getLowRezMaterialNodeName()
  {
    return pLowRezMaterialNodeName;
  }

  /**
   * @return the pLowRezModelNodeName
   */
  public String 
  getLowRezModelNodeName()
  {
    return pLowRezModelNodeName;
  }

  /**
   * @return the pLowRezRigNodeName
   */
  public String 
  getLowRezRigNodeName()
  {
    return pLowRezRigNodeName;
  }

  /**
   * @return the pMaterialNodeName
   */
  public String 
  getMaterialNodeName()
  {
    return pMaterialNodeName;
  }

  /**
   * @return the pMaterialExportNodeName
   */
  public String 
  getMaterialExportNodeName()
  {
    return pMaterialExportNodeName;
  }

  /**
   * @return the pModelNodeName
   */
  public String 
  getModelNodeName()
  {
    return pModelNodeName;
  }
  
  /**
   * @return the headModelNodeName
   */
  public String 
  getHeadModelNodeName()
  {
    return pHeadModelNodeName;
  }
  
  /**
   * @return the headModelNodeName
   */
  public String 
  getBlendShapeModelNodeName()
  {
    return pBlendShapeModelNodeName; 
  }

  /**
   * @return the pProject
   */
  public String 
  getProject()
  {
    return pProject;
  }

  /**
   * @return the pRigNodeName
   */
  public String 
  getRigNodeName()
  {
    return pRigNodeName;
  }
  
  /**
   * @return the rigInfoNodeName
   */
  public String 
  getRigInfoNodeName()
  {
    return pRigInfoNodeName;
  }
  
  /**
   * @return the skeletonNodeName
   */
  public String 
  getSkeletonNodeName()
  {
    return pSkeletonNodeName;
  }

  /**
   * @return the pShaderExportNodeName
   */
  public String 
  getShaderExportNodeName()
  {
    return pShaderExportNodeName;
  }

  /**
   * @return the pShaderIncludeGroupSecSeq
   */
  public FileSeq 
  getShaderIncludeGroupSecSeq()
  {
    return pShaderIncludeGroupSecSeq;
  }

  /**
   * @return the pShaderIncludeNodeName
   */
  public String 
  getShaderIncludeNodeName()
  {
    return pShaderIncludeNodeName;
  }

  /**
   * @return the pShaderNodeName
   */
  public String 
  getShaderNodeName()
  {
    return pShaderNodeName;
  }

  /**
   * @return the pTextureNodeName
   */
  public String 
  getTextureNodeName()
  {
    return pTextureNodeName;
  }
  
  /**
   * @return the finalize script name
   */
  public String getFinalizeScriptName()
  {
    return pFinalizeMELNodeName;
  }

  /**
   * @return the low rez finalize script name
   */
  public String getLowRezFinalizeScriptName()
  {
    return pLowRezFinalizeMELNodeName;
  }

  /**
   * @return the mental ray init script name
   */
  public String getMRInitScriptName()
  {
    return pMRInitMELNodeName;
  }
  
  /**
   * @return the auto rig script name
   */
  public String getAutoRigScriptName()
  {
    return pAutoRigMELNodeName;
  }

  /**
   * @return the placeholder script name
   */
  public String getPlaceholderScriptName()
  {
    return pPlaceholderMELNodeName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";

  public final static String aAssetName = "AssetName";

  public final static String aAssetType = "AssetType";
  
  private static final long serialVersionUID = 4290381193739660113L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private String pProject;

  private String pAssetName;

  private String pAssetType;
  
  private String pNameSpace;

  // Hi Rez Models
  private String pModelNodeName;
  
  private String pHeadModelNodeName;
  
  private String pBlendShapeModelNodeName;

  private String pRigNodeName;
  
  private String pRigInfoNodeName;
  
  private String pSkeletonNodeName;

  private String pMaterialNodeName;

  private String pMaterialExportNodeName;

  private String pFinalNodeName;

  // Low Rez Models
  private String pLowRezModelNodeName;

  private String pLowRezRigNodeName;

  private String pLowRezMaterialNodeName;

  private String pLowRezFinalNodeName;

  // Shader Nodes
  private String pShaderNodeName;

  private String pShaderIncludeNodeName;

  private FileSeq pShaderIncludeGroupSecSeq;

  private String pShaderExportNodeName;

  // Texture Nodes
  private String pTextureNodeName;
  
  private String pFinalizeMELNodeName;
  private String pLowRezFinalizeMELNodeName;
  private String pMRInitMELNodeName;
  private String pPlaceholderMELNodeName;
  private String pAutoRigMELNodeName;
}
