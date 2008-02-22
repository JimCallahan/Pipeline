// $Id: ProjectNamer.java,v 1.10 2008/02/22 09:22:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*; 

import java.util.TreeMap;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   N A M E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public 
class ProjectNamer 
  extends BaseNames 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Create a new shot namer.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param studioDefs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  public ProjectNamer
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
    StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("ProjectNamer", 
          "The basic naming class for project specific files.",
          mclient, qclient);

    pStudioDefs = studioDefs;
    pBasePaths  = new DoubleMap<AssetType, TaskType, Path>(); 

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The Name of the Project the asset should live in", 
         null);
      addParam(param);
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @SuppressWarnings("unused")
  @Override
  public void 
  generateNames() 
    throws PipelineException
  {
    /* initialize the local util context (author, view and toolset) */ 
    setContext((UtilContext) getParamValue(aUtilContext)); 
    
    /* lookup the namer's parameter values */ 
    pProjectName = getStringParamValue(new ParamMapping(StudioDefinitions.aProjectName));
  
    /* initialize the cached fully resolved node directory paths for all combinations 
         of asset and task type */
    for(AssetType atype : AssetType.values()) { 
      Path apath = pStudioDefs.getAssetPath(pProjectName, atype.toDirName()); 
      for(TaskType ttype : TaskType.values()) {
	pBasePaths.put(atype, ttype, new Path(apath, ttype.toDirPath()));
      }
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The short project name. 
   */ 
  public String 
  getProjectName() 
  {
    return pProjectName;
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   P L A T E   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node name of the original undistored red checkerboard 
   * reference image for use in calibrating lens distortion.
   */ 
  public String
  getPlatesRedCheckerNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), 
			 new Path(AppDirs.Images.toDirPath(), "red_checker"));
    return path.toString();
  }
  
  /**
   * Returns the fully resolved node name of the original undistored green checkerboard 
   * reference image for use in calibrating lens distortion.
   */ 
  public String
  getPlatesGreenCheckerNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), 
			 new Path(AppDirs.Images.toDirPath(), "green_checker"));
    return path.toString();
  }
  
  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment 
   * that will grade and warp the original reference grid images.
   */ 
  public String
  getGridGradeWarpNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), 
			 new Path(AppDirs.Nuke.toDirPath(), "grid_grade_warp"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment 
   * that will grade the distorted reference grid images and diff them against the graded
   * and warped original reference grid images.
   */ 
  public String
  getGridGradeDiffNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), 
			 new Path(AppDirs.Nuke.toDirPath(), "grid_grade_diff"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment 
   * that adds a generic BlackOutside Nuke node.
   */ 
  public String
  getBlackOutsideNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates), 
			 new Path(AppDirs.Nuke.toDirPath(), "black_outside"));
    return path.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T R A C K I N G   A S S E T S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and verify the tracking test render Maya scene.
   */ 
  public String
  getTrackPrepNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "track_prep"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script which verifies the 
   * contents of the Maya scene containing tracking data exported from PFTrack.
   */ 
  public String
  getTrackVerifyNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "track_verify"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking verification test renders.
   */ 
  public String
  getTrackVerifyGlobalsNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "track_verify_globals"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the combined pre-render MEL 
   * script for the tracking verification test renders.
   */ 
  public String
  getTrackVerifyRenderNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "track_verify_render"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script which creates a 
   * worldspace duplicate of the tracked camera with baked animation and saves it into 
   * a clean scene.
   */ 
  public String
  getTrackExtractCameraNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "extract_camera"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script which creates a 
   * worldspace locator with baked animation and saves it into a clean scene.
   */ 
  public String
  getTrackExtractTrackingNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking), 
			 new Path(AppDirs.MEL.toDirPath(), "extract_tracking"));
    return path.toString(); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   M A T C H   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and verify the match test render Maya scene.
   */ 
  public String
  getMatchPrepNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Match), 
			 new Path(AppDirs.MEL.toDirPath(), "match_prep"));
    return path.toString(); 
  }
  
  

  

  /*----------------------------------------------------------------------------------------*/
  /*   R O R S C H A C H   A S S E T S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Returns the fully resolved name of the node containing a Maya scene which provides the 
   * test rig used in the tracking verification test renders.
   */ 
  public String
  getRorschachVerifyModelNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Modeling), 
			 "ror_mdl_verify");
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the tracking verification test renders.
   */ 
  public String
  getRorschachTestShadersNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev), 
			 "ror_shd_test");
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Maya scene which provides the
   * test lights used in the tracking verification test renders.
   */ 
  public String
  getRorschachTestLightsNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev), 
			 "ror_lts_test");
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a placeholder Maya scene which 
   * will eventually contain the camera/model tracking data exported from PFTrack.
   */ 
  public String
  getRorschachTrackPlaceholderNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Placeholder), 
			 "ror_track");
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script used to add head 
   * and neck constraints to the match rig.
   */ 
  public String
  getConstrainRigNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Rigging), 
			 new Path(AppDirs.MEL.toDirPath(), "constrain_rig"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the match rig Maya scene.
   */ 
  public String
  getRorschachRigNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Rigging), 
			 "ror_rig");
    return path.toString(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   M I S C   A S S E T S                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Returns the fully resolved name of the node containing the definition of a MEL procedure 
   * which attached an arbitrary material to an object.
   */ 
  public String 
  getAttachShaderNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc), 
			 new Path(AppDirs.MEL.toDirPath(), "attach_shader"));
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script to hide all camera
   * image planes from view before rendering.
   */ 
  public String 
  getHideCameraPlaneNode() 
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc), 
			 new Path(AppDirs.MEL.toDirPath(), "hide_camera_plane"));
    return path.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2625175852662491653L;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;

 
  /*-- GENERATED ---------------------------------------------------------------------------*/

  /**
   * Cached short names of the current project.
   */ 
  protected String pProjectName;

  /**
   * Cached fully resolved node directory paths for all combinations of asset and task type.
   */ 
  private DoubleMap<AssetType, TaskType, Path>  pBasePaths;
  

}
