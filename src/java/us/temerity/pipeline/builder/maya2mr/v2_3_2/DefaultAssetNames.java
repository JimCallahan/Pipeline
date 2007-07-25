package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.Arrays;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;

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
      String each[] = {"prop", "set", "character"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each)); 
      UtilityParam param = 
	new EnumUtilityParam
	(aAssetType, 
	 "The Type of the asset", 
	 "character",
	 choices);
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
    
    pApprovalMode = getBooleanParamValue(new ParamMapping(aApprovalFormat));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    pAssetName =  getStringParamValue(new ParamMapping(aAssetName));
    pAssetType =  getStringParamValue(new ParamMapping(aAssetType));
    
    pNameSpace = pAssetName;

    pAssetPath = new Path("/projects/" + pProject + "/assets/" + pAssetType + "/" + pAssetName);
    String assetStart = pAssetPath.toString();
    
    String edit = getEditDirectory();
    String submit = getSubmitDirectory();
    String approve = getApproveDirectory();
    String prepare = getPrepareDirectory();
    String product = getProductDirectory();
    String thumb = getThumbnailDirectory();

//  No Approval Format
    if (!pApprovalMode) {
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
      pModStart        = new Path(pAssetPath, "model");
      pModEdit         = new Path(pModStart, edit);
      pModSubmit       = new Path(pModStart, submit);
      pModPrepare      = new Path(pModSubmit, prepare);
      pModApprove      = new Path(pModStart, approve);
      pModProduct      = new Path(pModApprove, product);
      pModThumb        = new Path(pModStart, thumb);

      pRigStart        = new Path(pAssetPath, "rig");
      pRigEdit         = new Path(pRigStart, edit);
      pRigSubmit       = new Path(pRigStart, submit);
      pRigPrepare      = new Path(pRigSubmit, prepare);
      pRigApprove      = new Path(pRigStart, approve);
      pRigProduct      = new Path(pRigApprove, product);
      pRigThumb        = new Path(pRigStart, thumb);

      pMatStart        = new Path(pAssetPath, "mat");
      pMatEdit         = new Path(pMatStart, edit);
      pMatSubmit       = new Path(pMatStart, submit);
      pMatPrepare      = new Path(pMatSubmit, prepare);

      pShdStart        = new Path(pAssetPath, "shd");
      pShdEdit         = new Path(pShdStart, edit);
      pShdSubmit       = new Path(pShdStart, submit);
      pShdPrepare      = new Path(pShdSubmit, prepare);
      pShdApprove      = new Path(pShdStart, approve);
      pShdProduct      = new Path(pShdApprove, product);
      pShdThumb        = new Path(pShdStart, thumb);
      
      pTexStart        = new Path(pAssetPath, "tex");
      pTexEdit         = new Path(pTexStart, edit);
      pTexSubmit       = new Path(pTexStart, submit);
      pTexPrepare      = new Path(pTexSubmit, prepare);
      pTexApprove      = new Path(pTexStart, approve);
      pTexProduct      = new Path(pTexApprove, product);
    }
    done();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D A B L E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
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
  getPrepareDirectory()
  {
    return "prepare";
  }
  
  protected String
  getProductDirectory()
  {
    return "product";
  }
  
  protected String
  getThumbnailDirectory()
  {
    return "thumb";
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
    if (pApprovalMode)
      return new Path(pAssetPath, pAssetName).toString();
    return pFinalNodeName;
  }

  /**
   * @return the pLowRezFinalNodeName
   */
  public String 
  getLowRezFinalNodeName()
  {
    if (pApprovalMode)
      return new Path(pAssetPath, pAssetName + "_anim").toString();
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
    if (pApprovalMode)
      return new Path(pMatEdit, pAssetName + "_mat").toString();
    return pMaterialNodeName;
  }

  /**
   * @return the pMaterialExportNodeName
   */
  public String 
  getMaterialExportNodeName()
  {
    if (pApprovalMode)
      return new Path(pMatPrepare, pAssetName + "_matExport").toString();
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
    if (pApprovalMode)
      return new Path(pRigEdit, pAssetName + "_blends").toString();
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
    if (pApprovalMode)
      return new Path(pRigEdit, pAssetName + "_skel").toString();
    return pSkeletonNodeName;
  }

  /**
   * @return the pShaderExportNodeName
   */
  public String 
  getShaderExportNodeName()
  {
    if (pApprovalMode)
      return new Path(pShdPrepare, pAssetName + "_shdExp").toString();
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
    if (pApprovalMode)
      return new Path(pShdEdit, pAssetName + "_shd").toString();
    return pShaderNodeName;
  }

  /**
   * @return the pTextureNodeName
   */
  public String 
  getTextureNodeName()
  {
    if (pApprovalMode)
      return new Path(pTexEdit, pAssetName + "_tex").toString();
    return pTextureNodeName;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A P P R O V A L   F O R M A T                                                        */
  /*----------------------------------------------------------------------------------------*/

  public String 
  getModelEditNodeName()
  {
    return new Path(pModEdit, pAssetName + "_mod").toString();
  }
  
  public String 
  getModelPieceNodeName
  (  
    String piece
  )
  {
    return getModelEditNodeName() + "_" + piece;
  }

  public String 
  getModelVerifyNodeName()
  {
    return new Path(pModPrepare, pAssetName + "_mod").toString();
  }
  
  public String
  getModelFinalNodeName()
  {
    return new Path(pModProduct, pAssetName + "_mod").toString(); 
  }
  
  public String
  getModelApproveNodeName()
  {
    return new Path(pModApprove, pAssetName + "_mod_approve").toString();
  }
  
  public String
  getModelSubmitNodeName()
  {
    return new Path(pModSubmit, pAssetName + "_mod_submit").toString();
  }
  
  public String
  getModelTTNodeName()
  {
    return new Path(pModPrepare, pAssetName + "_mod_tt").toString();
  }
  
  public String
  getModelTTImagesNodeName()
  {
    return new Path(pModPrepare, pAssetName + "_mod_img").toString();
  }
  
  public String
  getModelThumbNodeName()
  {
    return new Path(pModThumb, pAssetName + "_mod_thumb").toString();
  }
  
  /**
   * @return The rig edit node name.
   */
  public String
  getRigEditNodeName()
  {
    return new Path(pRigEdit, pAssetName + "_rig").toString();
  }
  
  /**
   * @return the re-rig node name.
   */
  public String
  getReRigNodeName()
  {
    return new Path(pRigPrepare, pAssetName + "_rig").toString();
  }
  
  /**
   * @return The rig submit node name.
   */
  public String
  getRigSubmitNodeName()
  {
    return new Path(pRigSubmit, pAssetName + "_rig_submit").toString();
  }
  
  /**
   * @return the rig approve node name.
   */
  public String
  getRigApproveNodeName()
  {
    return new Path(pRigApprove, pAssetName + "_rig_approve").toString();
  }
  
  /**
   * @return the rig anim node name.
   */
  public String
  getRigAnimNodeName()
  {
    return new Path(pRigPrepare, pAssetName + "_rig_anim").toString();
  }
  
  /**
   * @return the rig anim images node name.
   */
  public String
  getRigAnimImagesNodeName()
  {
    return new Path(pRigPrepare, pAssetName + "_rig_img").toString();
  }
  
  /**
   * @return the rig final node name.
   */
  public String
  getRigFinalNodeName()
  {
    return  new Path(pRigPrepare, pAssetName).toString();
  }
  
  /**
   * @return the model MI node name.
   */
  public String
  getModelMiNodeName()
  {
    return new Path(pRigProduct, pAssetName + "_geo").toString();
  }
  
  /**
   * @return the rig anim curve node name.
   */
  public String
  getRigAnimCurvesNodeName()
  {
    return new Path(pRigPrepare, pAssetName + "_rig_crv").toString();
  }
  
  /**
   * @return the rig anim FBX node name.
   */
  public String
  getRigAnimFBXNodeName()
  {
    return new Path(pRigPrepare, pAssetName + "_rig_fbx").toString();
  }
  
  public String
  getRigThumbNodeName()
  {
    return new Path(pRigThumb, pAssetName + "_rig_thumb").toString();
  }
  
  /**
   * @return the pTextureNodeName
   */
  public String 
  getAnimTextureNodeName()
  {
    return new Path(pTexEdit, pAssetName + "_animTex").toString();
  }
  
  public String
  getAnimTextureFinalNodeName()
  {
    return new Path(pTexProduct, pAssetName + "_animTex").toString();
  }
  
  public String
  getTextureFinalNodeName()
  {
    return new Path(pTexProduct, pAssetName + "_tex").toString();
  }
  
  public String
  getShaderSubmitNode()
  {
    return new Path(pShdSubmit, pAssetName + "_shd_submit").toString();
  }
  
  public String
  getShaderApproveNode()
  {
    return new Path(pShdApprove, pAssetName + "_shd_approve").toString();
  }
  
  public String
  getShaderRenderNodeName()
  {
    return new Path(pShdPrepare, pAssetName + "_shd_img").toString();
  }

  public String
  getShaderExportFinalNodeName()
  {
    return new Path(pShdProduct, pAssetName + "_shdExp").toString();
  }
  
  public String
  getShaderLgtMiNodeName()
  {
    return new Path(pShdPrepare, "lgt").toString();
  }
  
  public String
  getShaderCamMiNodeName()
  {
    return new Path(pShdPrepare, "cam").toString();
  }
  
  public String
  getShaderCamOverMiNodeName()
  {
    return new Path(pShdPrepare, "camOver").toString();
  }
  
  public String
  getShaderShdMiNodeName()
  {
    return new Path(pShdPrepare, "shd").toString();
  }
  
  public String
  getShaderTTNodeName()
  {
    return new Path(pShdEdit, pAssetName + "_shd_tt").toString();
  }
  
  public String
  getShaderThumbNodeName()
  {
    return new Path(pShdThumb, pAssetName + "_shd_thumb").toString();
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
  
  private boolean pApprovalMode;
  
  // Paths
  private Path pAssetPath;
  private Path pModStart;
  private Path pModEdit;
  private Path pModPrepare;
  private Path pModSubmit;
  private Path pModApprove;
  private Path pModProduct;
  private Path pModThumb;
  
  private Path pRigStart;
  private Path pRigEdit;
  private Path pRigPrepare;
  private Path pRigSubmit;
  private Path pRigApprove;
  private Path pRigProduct;
  private Path pRigThumb;
  
  private Path pMatStart;
  private Path pMatEdit;
  private Path pMatPrepare;
  private Path pMatSubmit;
  @SuppressWarnings("unused")
  private Path pMatApprove;
  @SuppressWarnings("unused")
  private Path pMatProduct;

  private Path pShdStart;
  private Path pShdEdit;
  private Path pShdPrepare;
  private Path pShdSubmit;
  private Path pShdApprove;
  private Path pShdProduct;
  private Path pShdThumb;
  
  private Path pTexStart;
  private Path pTexEdit;
  @SuppressWarnings("unused")
  private Path pTexPrepare;
  private Path pTexSubmit;
  private Path pTexApprove;
  private Path pTexProduct;
  

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
}


