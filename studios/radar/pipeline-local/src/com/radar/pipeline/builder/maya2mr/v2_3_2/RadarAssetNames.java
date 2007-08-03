package com.radar.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.Arrays;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.BuildsAssetNames;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   A S S E T   N A M E S                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class RadarAssetNames 
  extends BaseNames 
  implements BuildsAssetNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  RadarAssetNames
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient
  )
    throws PipelineException 
  {
    super("RadarAssetNames", 
      	  new VersionID("2.3.2"),
      	  "Radar",
          "The basic naming class for assets built at Radar",
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
  }  

  @Override
  public void 
  generateNames() 
    throws PipelineException 
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pProject = getStringParamValue(new ParamMapping(aProjectName));
    pAssetName =  getStringParamValue(new ParamMapping(aAssetName));
    pAssetType =  getStringParamValue(new ParamMapping(aAssetType));
    
    pNameSpace = pAssetName;
    
    Path startPath = new Path("/projects/" + pProject);
    
    pBuildWork = new Path(new Path(new Path(startPath, "buildWork"), pAssetType), pAssetName);
    pBuildOutput = new Path(new Path(new Path(startPath, "buildOutput"), pAssetType), pAssetName);
    
    pWorkMod = new Path(pBuildWork, "model");
    pWorkRig = new Path(pBuildWork, "rigging");
    pWorkShd = new Path(pBuildWork, "shading");
    pWorkTex = new Path(pBuildWork, "sourceImages");

    pLocalWorkMod = new Path(pWorkMod, "localOutput");
    pLocalWorkRig = new Path(pWorkRig, "localOutput");
    pLocalWorkShd = new Path(pWorkShd, "localOutput");

    pOutMod = new Path(pBuildOutput, "model");
    pOutRig = new Path(pBuildOutput, "rigging");
    pOutShd = new Path(pBuildOutput, "shading");
    pOutTex = new Path(pBuildOutput, "sourceImages");

  }
  
  private Path pBuildWork;
  private Path pBuildOutput;
  
  private Path pWorkMod;
  private Path pWorkRig;
  private Path pWorkShd;
  private Path pWorkTex;
  
  private Path pLocalWorkMod;
  private Path pLocalWorkRig;
  private Path pLocalWorkShd;
  
  private Path pOutMod;
  private Path pOutRig;
  private Path pOutShd;
  private Path pOutTex;

  public String 
  getAssetName() 
  {
    return pAssetName;
  }

  public String 
  getAssetType() 
  {
    return pAssetType;
  }
  
  public String 
  getNameSpace()
  {
    return pNameSpace;
  }
  
  /**
   * @return the pProject
   */
  public String 
  getProject()
  {
    return pProject;
  }

  
  public String 
  getAnimFinalNodeName()
  {
    return new Path(pBuildOutput, pAssetName + "_animationRig").toString();
  }
  
  public String 
  getRenderFinalNodeName()
  {
    return new Path(pBuildOutput, pAssetName + "_renderRig").toString();
  }
  
  /**
   * @return the headModelNodeName
   */
  public String 
  getBlendShapeModelNodeName()
  {
    return new Path(pWorkMod, pAssetName + "_blendShapes").toString();
  }

  /**
   * @return the skeletonNodeName
   */
  public String 
  getSkeletonNodeName()
  {
    return new Path(pWorkRig, pAssetName + "_skeleton").toString();
  }
  
  /**
   * @return the pTextureNodeName
   */
  public String 
  getTextureNodeName()
  {
    return new Path(pWorkTex, pAssetName + "_textures").toString();
  }
  
  public String
  getTextureFinalNodeName()
  {
    return new Path(pOutTex, pAssetName + "_textures").toString();
  }

  public String 
  getModelEditNodeName()
  {
    return new Path(pWorkMod, pAssetName + "_model_work").toString();
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
    return new Path(pLocalWorkMod, pAssetName + "_model_pending").toString();
  }
  
  public String
  getModelFinalNodeName()
  {
    return new Path(pOutMod, pAssetName + "_model").toString(); 
  }
  
  public String
  getModelApproveNodeName()
  {
    return new Path(pOutMod, pAssetName + "_model_approve").toString();
  }
  
  public String
  getModelSubmitNodeName()
  {
    return new Path(pWorkMod, pAssetName + "_model_submit").toString();
  }
  
  public String
  getModelTTNodeName()
  {
    return new Path(pLocalWorkMod, pAssetName + "_model_tt").toString();
  }
  
  public String
  getModelTTImagesNodeName()
  {
    return new Path(pLocalWorkMod, pAssetName + "_model_img").toString();
  }
  
  public String
  getModelThumbNodeName()
  {
    return new Path(pLocalWorkMod, pAssetName + "_model_thumb").toString();
  }

  /**
   * @return The rig edit node name.
   */
  public String
  getRigEditNodeName()
  {
    return new Path(pWorkRig, pAssetName + "_rig_work").toString();
  }
  
  /**
   * @return the re-rig node name.
   */
  public String
  getReRigNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_rig_pending").toString();
  }
  
  /**
   * @return The rig submit node name.
   */
  public String
  getRigSubmitNodeName()
  {
    return new Path(pWorkRig, pAssetName + "_animationRig_submit").toString();
  }
  
  /**
   * @return the rig approve node name.
   */
  public String
  getRigApproveNodeName()
  {
    return new Path(pOutRig, pAssetName + "_animationRig_approve").toString();
  }
  
  /**
   * @return the rig anim node name.
   */
  public String
  getRigAnimNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_animationRig_tt").toString();
  }
  
  /**
   * @return the rig anim images node name.
   */
  public String
  getRigAnimImagesNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_animationRig_img").toString();
  }
  
  /**
   * @return the rig final node name.
   */
  public String
  getRigFinalNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_animationRig_pending").toString();
  }
  
  /**
   * @return the rig anim curve node name.
   */
  public String
  getRigAnimCurvesNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_rig_crv").toString();
  }
  
  /**
   * @return the rig mat export node name.
   */
  public String
  getRigMatExportNodeName()
  {
    return new Path(pLocalWorkRig, pAssetName + "_rig_matExp").toString();
  }
  
  public String
  getRigThumbNodeName()
  {
    return new Path(pLocalWorkMod, pAssetName + "_rig_thumb").toString();
  }
  
  /**
   * @return the pTextureNodeName
   */
  public String 
  getAnimTextureNodeName()
  {
    return new Path(pWorkTex, pAssetName + "_rigTex").toString();
  }
  
  public String
  getAnimTextureFinalNodeName()
  {
    return new Path(pOutTex, pAssetName + "_rigTex").toString();
  }

  /**
   * @return the pMaterialNodeName
   */
  public String 
  getMaterialNodeName()
  {
      return new Path(pWorkShd, pAssetName + "_shading_work").toString();
  }

  /**
   * @return the pMaterialExportNodeName
   */
  public String 
  getMaterialExportNodeName()
  {
    return new Path(pLocalWorkShd, pAssetName + "_shaders").toString();
  }
  
  public String
  getMaterialRenderNodeName()
  {
    return new Path(pLocalWorkShd, pAssetName + "_renderRig_img").toString();
  }
  
  public String
  getMaterialTTNodeName()
  {
    return new Path(pLocalWorkShd, pAssetName + "_renderRig_tt").toString();
  }
  
  public String
  getMaterialVerifyNodeName()
  {
    return new Path(pLocalWorkShd, pAssetName + "_renderRig_pending").toString();
  }
  
  public String
  getMaterialThumbNodeName()
  {
    return new Path(pLocalWorkShd, pAssetName + "_renderRig_thumb").toString();
  }
  
  public String
  getMaterialSubmitNodeName()
  {
    return new Path(pWorkShd, pAssetName + "_renderRig_submit").toString();
  }
  
  public String
  getMaterialApproveNodeName()
  {
    return new Path(pOutShd, pAssetName + "_renderRig_approve").toString();
  }
  
  public String 
  getFinalNodeName()
  {
    return null;
  }

  public String 
  getHeadModelNodeName()
  {
    return null;
  }

  public String 
  getLowRezFinalNodeName()
  {
    return null;
  }

  public String 
  getLowRezMaterialNodeName()
  {
    return null;
  }

  public String 
  getLowRezModelNodeName()
  {
    return null;
  }

  public String 
  getLowRezRigNodeName()
  {
    return null;
  }

  public String 
  getModelMiNodeName()
  {
    return null;
  }

  public String 
  getModelNodeName()
  {
    return null;
  }

  public String 
  getRigAnimFBXNodeName()
  {
    return null;
  }

  public String 
  getRigInfoNodeName()
  {
    return null;
  }

  public String 
  getRigNodeName()
  {
    return null;
  }

  public String 
  getShaderApproveNode()
  {
    return null;
  }

  public String 
  getShaderCamMiNodeName()
  {
    return null;
  }

  public String 
  getShaderCamOverMiNodeName()
  {
    return null;
  }

  public String 
  getShaderExportFinalNodeName()
  {
    return null;
  }

  public String 
  getShaderExportNodeName()
  {
    return null;
  }

  public FileSeq 
  getShaderIncludeGroupSecSeq()
  {
    return null;
  }

  public String 
  getShaderIncludeNodeName()
  {
    return null;
  }

  public String 
  getShaderLgtMiNodeName()
  {
    return null;
  }

  public String 
  getShaderNodeName()
  {
    return null;
  }

  public String 
  getShaderRenderNodeName()
  {
    return null;
  }

  public String 
  getShaderShdMiNodeName()
  {
    return null;
  }

  public String 
  getShaderSubmitNode()
  {
    return null;
  }

  public String 
  getShaderTTNodeName()
  {
    return null;
  }

  public String 
  getShaderThumbNodeName()
  {
    return null;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";
  public final static String aAssetName   = "AssetName";
  public final static String aAssetType   = "AssetType";

  private static final long serialVersionUID = 5316042296615430772L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private String pProject;
  private String pAssetName;
  private String pAssetType;
  private String pNameSpace;
}
