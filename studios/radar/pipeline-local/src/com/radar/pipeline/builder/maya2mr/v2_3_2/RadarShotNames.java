package com.radar.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.BaseNames;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.AnswersBuilderQueries;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.BuildsShotNames;

/*------------------------------------------------------------------------------------------*/
/*   R A D A R   S H O T   N A M E S                                                        */
/*------------------------------------------------------------------------------------------*/

public class RadarShotNames
  extends BaseNames
  implements BuildsShotNames
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  RadarShotNames
  (
    String project,
    boolean existingDirs,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderInfo
  )
    throws PipelineException 
  {
    super("RadarShotNames", 
          "The basic naming class for shots built at Radar",
          mclient,
          qclient);
    
    pBuilderInfo = builderInfo;
    pProject = project;
    pExistingDirs = existingDirs;
    
    if (pExistingDirs) {
      ArrayList<String> seqs = pBuilderInfo.getSequenceList(pProject, null);
      UtilityParam param =
	new EnumUtilityParam
	(aSequenceName,
	 "The Sequence the created shot should be in.",
	 seqs.get(0),
	 seqs);
      addParam(param);
    }
    else
    {
      UtilityParam param =
	new StringUtilityParam
	(aSequenceName,
	 "The Sequence the created shot should be in.",
	 null);
      addParam(param);
    }
    {
      UtilityParam param =
	new StringUtilityParam
	(aShotName,
	 "The name of the shot to be created.",
	 null);
      addParam(param);
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void generateNames()
    throws PipelineException
  {
    setContext((UtilContext) getParamValue(aUtilContext));
    
    pSeqName = getStringParamValue(new ParamMapping(aSequenceName));
    pShotName = getStringParamValue(new ParamMapping(aShotName));
    
    pNamePrefix = pSeqName + "_" + pShotName;
    
    Path pShotStart = new Path(new Path("/projects"), pProject);
    pShotWork    = new Path(new Path(new Path(pShotStart, "shotWork"), pSeqName), pShotName);
    pShotOutput  = new Path(new Path(new Path(pShotStart, "shotOutput"), pSeqName), pShotName);
    pBlastOutput = new Path(new Path(new Path(pShotStart, "blastOutput"), pSeqName), pShotName);
    
    pWorkScenes   = new Path(pShotWork, "scenes");
    pLocalOutput  = new Path(pShotWork, "localOutput");
    pRenderScenes = new Path(pShotWork, "renderScenes");
    pParticles    = new Path(pShotWork, "particles");
    pLayoutWork   = new Path(new Path(new Path(new Path(pShotStart, "shotWork"), pSeqName), "layout/localWork/layoutDef"), pShotName);
    pLayoutDef    = new Path(new Path(new Path(new Path(pShotStart, "shotWork"), pSeqName), "layout/localOutput/layoutDef"), pShotName);
    
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /*---Stuff---------------------------------------------------------------------------------*/
  
  /**
   * A node that contains information about the shot.
   */
  public String
  getShotInfoNodeName()
  {
    return new Path(pLayoutDef, "shotInfo").toString();
  }
  
  public String 
  getShotName()
  {
    return pShotName;  
  }
  
  public String 
  getSequenceName()
  {
    return pSeqName;
  }
  
  public String 
  getCameraNodeName()
  {
    return new Path(new Path(new Path("/projects"), pProject), "camera/renderCam_cam").toString();
  }
  
  /*---Layout--------------------------------------------------------------------------------*/
  
  /**
   * A node containing the exported animation from the layout for a particular character. 
   */
  public String
  getLayoutExportPrepareNodeName
  (
    String assetName  
  )
  {
    return new Path(pLayoutWork, assetName + "_anim").toString();
  }

  /**
   * A node containing a copy of the exported animation from the layout for creating the
   * animation scene. 
   */
  public String
  getLayoutExportProductNodeName
  (
    String assetName  
  )
  {
    return new Path(pLayoutDef, assetName + "_anim").toString();
  }
  
  /**
   * The rendered (or playblasted) images for layout submission. 
   */
  public String
  getLayoutImgNodeName()
  {
    return new Path(new Path(pBlastOutput, "layout"), pNamePrefix) .toString();
  }
  
  /**
   * The thumbnail image for the playblast images. 
   */
  public String
  getLayoutThumbNodeName()
  {
    return new Path(new Path(pBlastOutput, "layout"), pNamePrefix + "_thumb") .toString();
  }
  
  /**
   * The layout submission node.
   */
  public String
  getLayoutSubmitNodeName()
  {
    return new Path(pLayoutWork, pNamePrefix + "submit").toString();
  }
  
  /**
   * The layout approve node. 
   */
  public String
  getLayoutApproveNodeName()
  {
    return new Path(pLayoutDef, pNamePrefix + "approve").toString();
  }
  
  /*---Anim----------------------------------------------------------------------------------*/
  
  /**
   * The node that a user will edit to create the animation or the node that is build from
   * component curve files if the animation is all coming from fbx files.
   */
  public String
  getAnimEditNodeName()
  {
    return new Path(pWorkScenes, "animation_work").toString();
  }
  
  /**
   * The node where the animation is reapplied to test the exported animation
   * and create a render for approval. 
   */
  public String
  getAnimBuildNodeName()
  {
    return new Path(pLocalOutput, "renderBuild").toString();
  }
  
  /**
   * A render (or playblast) of the animation that is submitted for approval. 
   */
  public String
  getAnimImgNodeName()
  {
    return new Path(new Path(pBlastOutput, "animation"), pNamePrefix).toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getAnimThumbNodeName()
  {
    return new Path(new Path(pBlastOutput, "animation"), pNamePrefix + "_thumb").toString();
  }
  
  /**
   *  The submit node for the animation task. 
   */
  public String
  getAnimSubmitNodeName()
  {
    return new Path(pWorkScenes, "animation_submit").toString();
  }
  
  /**
   *  The approve node for the animation task. 
   */
  public String
  getAnimApproveNodeName()
  {
    return new Path(pLocalOutput, "animation_approve").toString();
  }
  
  /**
   * A node containing one asset's exported animation from the animation scene. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getAnimExportPrepareNodeName
  (
    String assetName
  )
  {
    return new Path(new Path(pLocalOutput, "animation"), assetName + "_anim").toString();
  }
  
  /**
   * A node containing one asset's exported animation from the animation scene that
   * is used to build the lighting scene and the individual mi exporting scenes. 
   * 
   * @param assetName
   *   The asset that this node will correspond to.
   */
  public String
  getAnimExportProductNodeName
  (
    String assetName
  )
  {
    return new Path(new Path(pShotOutput, "animation"), assetName + "_anim").toString();
  }
  
  /**
   * A node built by applying exported animation to lighting models.  This node
   * is referenced by the lighting scene.
   */  
  public String
  getPreLightNodeName()
  {
    return new Path(pShotOutput, "renderBuild").toString();
  }
  
  /**
   * A scene specific gathering MEL script that is run on the prelight as it is being 
   * built.  Allows shot specific overrides. 
   */
  public String
  getPreLightMELNodeName()
  {
    return new Path(new Path(pShotWork, "scripts"), "renderBuild").toString();
  }
  
  /*---Light---------------------------------------------------------------------------------*/
  
  /**
   * The node which the artist edits to add lighting to the shot. 
   */
  public String
  getLightEditNodeName()
  {
    return new Path(pWorkScenes, "render_work").toString();
  }

  /**
   *  The rendered images which are submitted for approval of the lighting scene. 
   */
  public String
  getLightImagesNodeName()
  {
    return new Path(new Path(pShotOutput, "render_work"), pNamePrefix).toString();
  }
  
  /**
   * The submit node for the lighting task.
   */
  public String
  getLightSubmitNodeName()
  {
    return new Path(pWorkScenes, "render_submit").toString();
  }
  
  /**
   * The approve node for the lighting task.
   */
  public String
  getLightApproveNodeName()
  {
    return new Path(pLocalOutput, "render_approve").toString();
  }
  
  /**
   * Generated on lighting approval.  This node is used to render all the final
   * render passes that depend upon lighting. 
   */
  public String
  getFinalLightNodeName()
  {
    return new Path(pShotOutput, "render").toString();
  }
  
  /**
   * A thumbnail of the images.
   */
  public String
  getLightThumbNodeName()
  {
    return new Path(new Path(pShotOutput, "render_work"), pNamePrefix + "_thumb").toString();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   N O T   U S E D                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public String 
  getAnimExportPrepareNodeName()
  {
    return null;
  }

  public String 
  getAnimExportProductNodeName()
  {
    return null;
  }

  public String getAnimModelExportNodeName
  (
    String assetName
  )
  {
    return null;
  }

  public String 
  getAnimProductNodeName()
  {
    return null;
  }

  public String 
  getAnimSourceCurvesNodeName
  (
    String assetName
  )
  {
    return null;
  }

  public String 
  getAnimSourceFBXNodeName
  (
    String assetName
  )
  {
    return null;
  }

  public String 
  getCamOverrideMiNodeName()
  {
    return null;
  }

  public String 
  getCameraMiNodeName()
  {
    return null;
  }

  public String 
  getGeoInstMiNodeName
  (
    String assetName
  )
  {
    return null;
  }

  public String 
  getGeoMiNodeName
  (
    String assetName
  )
  {
    return null;
  }

  public String 
  getLayoutBuildNodeName()
  {
    return null;
  }

  public String 
  getLayoutEditNodeName()
  {
    return null;
  }

  public String 
  getLayoutExportPrepareNodeName()
  {
    return null;
  }

  public String 
  getLayoutExportProductNodeName()
  {
    return null;
  }

  public String 
  getLayoutProductNodeName()
  {
    return null;
  }

  public String 
  getLightMiNodeName()
  {
    return null;
  }

  public String 
  getMovieName()
  {
    return null;
  }

  public String 
  getOptionsMiNodeName()
  {
    return null;
  }

  public String 
  getShaderMiNodeName
  (
    String assetName
  )
  {
    return null;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aLocation = "Location";
  public final static String aSequenceName = "SequenceName";
  public final static String aShotName = "ShotName";
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pExistingDirs;
  
  private AnswersBuilderQueries pBuilderInfo;
  
  private String pProject;
  private String pSeqName;
  private String pShotName;
  
  private String pNamePrefix;
  
  private Path pShotWork;
  private Path pShotOutput;
  private Path pBlastOutput;
  private Path pWorkScenes;
  private Path pLocalOutput;
  private Path pRenderScenes;
  private Path pLayoutDef;
  private Path pLayoutWork;
  private Path pParticles;
  
  private static final long serialVersionUID = 7390431314064554474L;
}
