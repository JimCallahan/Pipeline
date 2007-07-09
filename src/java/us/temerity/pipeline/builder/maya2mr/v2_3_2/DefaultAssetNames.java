package us.temerity.pipeline.builder.maya2mr.v2_3_2;

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
  DefaultAssetNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super("DefaultAssetNames", 
      	  new VersionID("2.3.2"),
      	  "Temerity",
          "The basic naming class for an asset provided by Temerity",
          mclient,
          qclient);
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
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aApprovalFormat, 
         "Is this namer being used in the approval format?", 
         true);
      addParam(param);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  generateNames() 
    throws PipelineException 
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    boolean pApprovalFormat = getBooleanParamValue(new ParamMapping(aApprovalFormat));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    pAssetName =  getStringParamValue(new ParamMapping(aAssetName));
    pAssetType =  getStringParamValue(new ParamMapping(aAssetType));
    
    pNameSpace = pAssetName;

    Path assetPath = new Path("/projects/" + pProject + "/assets/" + pAssetType + "/" + pAssetName);
    String assetStart = assetPath.toString();
    
    String edit = getEditDirectory();
    String submit = getSubmitDirectory();
    String approve = getApproveDirectory();
    String intermediate = getIntermediateDirectory();
    String product = getProductDirectory();

//  No Approval Format
    if (!pApprovalFormat) {
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
    }
//  Approval Format
    else {
      Path modelStart = new Path(assetPath, "model");
      {
        Path modelEdit = new Path(modelStart, edit);
        pModelEditNodeName = new Path(modelEdit, pAssetName + "_mod").toString();
      }
      {
        Path modelSubmit = new Path(modelStart, submit);
        pModelSubmitNodeName = new Path(modelSubmit, pAssetName + "_mod_submit").toString();
        {
          Path modelIntermediate = new Path(modelSubmit, intermediate);
          pModelVerifyNodeName = new Path(modelIntermediate, pAssetName + "_mod").toString();
          pModelTTNodeName = new Path(modelIntermediate, pAssetName + "_mod_tt").toString();
          pModelTTImagesNodeName = new Path(modelIntermediate, pAssetName + "_mod_img").toString();
        }
      }
      {
        Path modelApprove = new Path(modelStart, approve);
        pModelApproveNodeName = new Path(modelApprove, pAssetName + "_mod_approve").toString();
        {
          Path modelProduct = new Path(modelApprove, product);
          pModelFinalNodeName = new Path(modelProduct, pAssetName + "_mod").toString(); 
        }
      }
    }
    {
      Path rigStart = new Path(assetPath, "rig");
      {
        Path rigEdit = new Path(rigStart, edit);
        pRigEditNodeName = new Path(rigEdit, pAssetName + "_rig").toString();
        pBlendShapeModelNodeName = new Path(rigEdit, pAssetName + "_blends").toString();
        pSkeletonNodeName = new Path(rigEdit, pAssetName + "_skel").toString();
      }
      {
        Path rigSubmit = new Path(rigStart, submit);
        pRigSubmitNodeName = new Path(rigSubmit, pAssetName + "_rig_submit").toString();
        {
          Path rigIntermediate = new Path(rigSubmit, intermediate);
          pReRigNodeName = new Path(rigIntermediate, pAssetName + "_rig").toString();
          pRigFinalNodeName = new Path(rigIntermediate, pAssetName).toString();
          pRigAnimNodeName = new Path(rigIntermediate, pAssetName + "_rig_anim").toString();
          pRigAnimImagesNodeName = new Path(rigIntermediate, pAssetName + "_rig_img").toString();
          pRigAnimCurvesNodeName = new Path(rigIntermediate, pAssetName + "_rig_crv").toString();
          pRigAnimFBXNodeName = new Path(rigIntermediate, pAssetName + "_rig_fbx").toString();
          pRigMELNodeName = new Path(rigIntermediate, pAssetName + "_rigMEL").toString();
          pReRigMELNodeName = new Path(rigIntermediate, pAssetName + "_rerigMEL").toString();
        }
      }
      {
        Path rigApprove = new Path(rigStart, approve);
        pRigApproveNodeName = new Path(rigApprove, pAssetName + "_rig_approve").toString();
        {
          Path rigProduct = new Path(rigApprove, product);
          pModelMiNodeName = new Path(rigProduct, pAssetName + "_geo").toString();
        }
        pFinalNodeName = new Path(assetPath, pAssetName).toString();
        pLowRezFinalNodeName = new Path(assetPath, pAssetName + "_").toString();
      }
    }
    {
      Path matStart = new Path(assetPath, "mat");
      {
        Path matEdit = new Path(matStart, edit);
        pMaterialNodeName =  new Path(matEdit, pAssetName + "_mat").toString();
      }
      {
        Path matSubmit = new Path(matStart, submit);
        {
          Path matIntermediate = new Path(matSubmit, intermediate);
          pMaterialExportNodeName = new Path(matIntermediate, pAssetName + "_matExport").toString();
        }
      }
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D A B L E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected void
  generateMELScripts()
  {
    String melStart = "/projects/" + pProject + "/assets/tools/mel/";
    
    pFinalizeMELNodeName = melStart + "finalize-" + pAssetType;
    pLowRezFinalizeMELNodeName = melStart + "finalize-" + pAssetType + "_lr";
    pMRInitMELNodeName = melStart + "mr-init";
    pAutoRigMELNodeName = melStart + "auto-rig";
    pModelVerifyMELNodeName = melStart + "verify-model";
    pRigVerifyMELNodeName = melStart + "verify-rig";
    pShaderVerifyMELNodeName = melStart + "verify-shader";
    pPlaceholderMELNodeName = null;
  }
  
  protected String
  getEditDirectory()
  {
    return "edit";
  }
  
  protected String
  getSubmitDirectory()
  {
    return "submit";
  }
  
  protected String
  getApproveDirectory()
  {
    return "approve";
  }
  
  protected String
  getIntermediateDirectory()
  {
    return "int";
  }
  
  protected String
  getProductDirectory()
  {
    return "product";
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   M E L   S C R I P T S                                                                */
  /*----------------------------------------------------------------------------------------*/
  
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
  
  /**
   * @return the model verification script name
   */
  public String
  getModelVerificationScriptName()
  {
    return pModelVerifyMELNodeName;
  }
  
  /**
   * @return the rig verification script name
   */
  public String
  getRigVerificationScriptName()
  {
    return pRigVerifyMELNodeName;
  }
  
  /**
   * @return the shader verification script name
   */
  public String
  getShaderVerificationScriptName()
  {
    return pShaderVerifyMELNodeName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A P P R O V A L   F O R M A T                                                        */
  /*----------------------------------------------------------------------------------------*/

  public String 
  getModelEditNodeName()
  {
    return pModelEditNodeName;
  }
  
  public String 
  getModelPieceNodeName
  (  
    String piece
  )
  {
    return pModelEditNodeName + "_" + piece;
  }

  public String 
  getModelVerifyNodeName()
  {
    return pModelVerifyNodeName;
  }
  
  public String
  getModelFinalNodeName()
  {
    return pModelFinalNodeName;
  }
  
  public String
  getModelApproveNodeName()
  {
    return pModelApproveNodeName;
  }
  
  public String
  getModelSubmitNodeName()
  {
    return pModelSubmitNodeName;
  }
  
  public String
  getModelTTNodeName()
  {
    return pModelTTNodeName;
  }
  
  public String
  getModelTTImagesNodeName()
  {
    return pModelTTImagesNodeName;
  }
  
  /**
   * @return The rig edit node name.
   */
  public String
  getRigEditNodeName()
  {
    return pRigEditNodeName;
  }
  
  /**
   * @return the re-rig node name.
   */
  public String
  getReRigNodeName()
  {
    return pReRigNodeName;
  }
  
  /**
   * @return The rig submit node name.
   */
  public String
  getRigSubmmitNodeName()
  {
    return pRigSubmitNodeName;
  }
  
  /**
   * @return the rig approve node name.
   */
  public String
  getRigApproveNodeName()
  {
    return pRigApproveNodeName;
  }
  
  /**
   * @return the rig anim node name.
   */
  public String
  getRigAnimNodeName()
  {
    return pRigAnimNodeName;
  }
  
  /**
   * @return the rig anim images node name.
   */
  public String
  getRigAnimImagesNodeName()
  {
    return pRigAnimImagesNodeName;
  }
  
  /**
   * @return the rig final node name.
   */
  public String
  getRigFinalNodeName()
  {
    return pRigFinalNodeName;
  }
  
  /**
   * @return the model MI node name.
   */
  public String
  getModelMiNodeName()
  {
    return pModelMiNodeName;
  }
  
  /**
   * @return the rig anim curve node name.
   */
  public String
  getRigAnimCurvesNodeName()
  {
    return pRigAnimCurvesNodeName;
  }
  
  /**
   * @return the rig anim FBX node name.
   */
  public String
  getRigAnimFBXNodeName()
  {
    return pRigAnimFBXNodeName;
  }
  
  public String
  getRigMELNodeName()
  {
    return pRigMELNodeName;
  }
  
  public String
  getReRigMELNodeName()
  {
    return pReRigMELNodeName;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";

  public final static String aAssetName = "AssetName";

  public final static String aAssetType = "AssetType";
  
  public final static String aApprovalFormat = "ApprovalFormat";
  
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
  private String pModelVerifyMELNodeName;
  private String pRigVerifyMELNodeName;
  private String pShaderVerifyMELNodeName;
  
  // Model Approval Setup
  private String pModelEditNodeName;
  private String pModelVerifyNodeName;
  private String pModelFinalNodeName;
  private String pModelApproveNodeName;
  private String pModelSubmitNodeName;
  private String pModelTTNodeName;
  private String pModelTTImagesNodeName;

  //Rig Approval Setup
  private String pRigEditNodeName;
  private String pReRigNodeName;
  private String pRigSubmitNodeName;
  private String pRigApproveNodeName;
  private String pRigAnimNodeName;
  private String pRigAnimImagesNodeName;
  private String pRigFinalNodeName;
  private String pModelMiNodeName;
  private String pRigAnimCurvesNodeName;
  private String pRigAnimFBXNodeName;
  private String pRigMELNodeName;
  private String pReRigMELNodeName;
  
}

