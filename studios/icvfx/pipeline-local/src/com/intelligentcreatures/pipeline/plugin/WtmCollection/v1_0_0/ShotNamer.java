// $Id: ShotNamer.java,v 1.11 2008/02/19 09:26:36 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   N A M E R                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides the names of nodes and node directories which are shot specific.
 */
public 
class ShotNamer 
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
  public ShotNamer
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("ShotNamer", 
          "Provides the names of nodes and node directories which are shot specific.", 
          mclient, qclient);
    
    pStudioDefs = studioDefs;
    pBasePaths  = new DoubleMap<TaskType, NodePurpose, Path>(); 
    
    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The short name of the project.", 
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aSequenceName,
         "The short name of the shot sequence.",
         null);
      addParam(param);
    }

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aShotName,
         "The short name of the shot.",
         null);
      addParam(param);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Intialize any internal datastructures used by the naming methods based on the values
   * of the namer's parameters. 
   */ 
  @Override
  public void 
  generateNames() 
    throws PipelineException 
  {
    /* initialize the local util context (author, view and toolset) */ 
    setContext((UtilContext) getParamValue(aUtilContext));

    /* lookup the namer's parameter values */ 
    pProjectName = getStringParamValue(new ParamMapping(StudioDefinitions.aProjectName));
    pSeqName     = getStringParamValue(new ParamMapping(StudioDefinitions.aSequenceName));
    pShotName    = getStringParamValue(new ParamMapping(StudioDefinitions.aShotName));
    
    /* initialize the cached fully resolved node directory paths for all combinations 
         of task type and node purpose for this shot. */ 
    {
      Path spath = StudioDefinitions.getShotPath(pProjectName, pSeqName, pShotName);
      for(TaskType type : TaskType.values()) {
        for(NodePurpose purpose : NodePurpose.values()) {
          Path path = new Path(new Path(spath, type.toDirPath()), purpose.toDirPath());
          pBasePaths.put(type, purpose, path); 
        }
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

  /**
   * The short shot sequence name. 
   */ 
  public String 
  getSequenceName() 
  {
    return pSeqName;
  }

  /**
   * The short shot name. 
   */ 
  public String 
  getShotName() 
  {
    return pShotName;
  }

  /**
   * The short combined sequence/shot name. 
   */ 
  public String 
  getFullShotName() 
  {
    return (pSeqName + pShotName); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H D R I                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved names of the (3) nodes containing the A series of varying 
   * exposure raw digital images used to construct the single high dynamic range (HDR) 
   * environment map.
   */
  public ArrayList<String> 
  getRawExposuresNodes() 
  {
    ArrayList<String> names = new ArrayList<String>(); 
    names.add(getRawExposuresNodeHelper("A")); 
    names.add(getRawExposuresNodeHelper("B")); 
    names.add(getRawExposuresNodeHelper("C")); 

    return names; 
  }

  private String 
  getRawExposuresNodeHelper
  (
   String letter
  ) 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Edit), 
			 new Path(AppDirs.Raw.toDirPath(), 
				  joinNames(getFullShotName(), "raw" + letter))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing the plain text exposure times.
   */
  public String
  getExposureTimesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Edit), 
			 joinNames(getFullShotName(), "exp_times")); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing the combined LatLon format 
   * HDR environment map and diagnostic images.
   */ 
  public String
  getDiagnosticHdrImageNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Focus), 
			 new Path(AppDirs.HDR.toDirPath(), 
				  joinNames(getFullShotName(), "env"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the HDRI task.
   */ 
  public String
  getHdriSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "hdri_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the final combined LatLon format 
   * HDR environment map. 
   */ 
  public String
  getFinalHdrImageNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Product), 
			 new Path(AppDirs.HDR.toDirPath(), 
				  joinNames(getFullShotName(), "env"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the Maya scene that will 
   * load the final combined LatLon format HDR environment map.
   */ 
  public String
  getFinalHdrMayaNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Product), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "hdr_env"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the HDRI task.
   */ 
  public String
  getHdriApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "hdri_approve")); 
    return path.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L A T E S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * scanned 2k images for this shot. 
   */
  public Path
  getPlatesScannedParentPath()
  {
    return new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
                    AppDirs.Image2k.toDirPath()); 
  }
  
  /**
   * Returns the fully resolved node directory path to the parent directory of all 
   * miscellanous on-set reference images.
   */
  public Path
  getPlatesMiscReferenceParentPath() 
  {
    return new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
                    AppDirs.Misc.toDirPath());  
  }
  
  /**
   * Returns the fully resolved name of the grouping node for all existing miscellanous 
   * on-set reference images. 
   * 
   * @param purpose
   *   The purpose of the node within the task.  Only supports: Prepare or Product.
   */
  public String
  getVfxReferenceNode
  (
   NodePurpose purpose
  ) 
    throws PipelineException 
  {
    switch(purpose) {
    case Prepare:
    case Product:
      {
	Path path = new Path(pBasePaths.get(TaskType.Plates, purpose), 
			     joinNames(getFullShotName(), "vfx_reference"));
	return path.toString(); 
      }
      
    default:
      throw new PipelineException
	("The " + purpose + " purpose is not supported for the VfxReference node!"); 
    }
  }
    
  /**
   * Returns the fully resolved name of the node containing lens and other data
   * collected on-set about the shot.
   */
  public String
  getVfxShotDataNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
			 joinNames(getFullShotName(), "vfx_shot_data")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the PFTrack scene which 
   * is used to specify the lens distortion by undistorting a reference grid.
   */ 
  public String
  getSolveDistortionNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
			 new Path(AppDirs.PFTrack.toDirPath(), 
				  joinNames(getFullShotName(), "solve_distortion"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the distorted reference 
   * grid image exported from the PFTrack scene. 
   */ 
  public String
  getDistortedGridNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 joinNames(getFullShotName(), "distorted_grid")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment 
   * that will read the distorted reference grid image.
   */ 
  public String
  getReadDistortedNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "read_distorted"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment 
   * that will read the original reference grid and reformatting it to match the resolution
   * of the distorted grid image.
   */ 
  public String
  getReformatOriginalNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "reformat_original"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script used by
   * artists to manually match the distortion of the PFTrack exported grid with the 
   * original reference grid.
   */ 
  public String
  getGridAlignNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "grid_align"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the grid comparison image
   * generated by the grid align Nuke script.
   */ 
  public String
  getGridAlignImageNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Focus), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "grid_align"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail images extracted
   * from the grid align images. 
   */ 
  public String
  getGridAlignThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Thumbnail), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "grid_align"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Plates task.
   */ 
  public String
  getPlateSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "plates_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the node containing the GridWarp Nuke script 
   * fragment which will be used to undistort the plates and redistort the rendering images. 
   */ 
  public String
  getLensWarpNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "lens_warp"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a Nuke script fragment that 
   * will read the original scanned plates and reformatting them to match the resolution
   * of the distorted grid image.
   */ 
  public String
  getReformatBgNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "reformat_bg"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  public String
  getUndistorted2kPlateNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.Image2k.toDirPath(), 
				  joinNames(getFullShotName(), "bg_ud_2k"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the undistorted/linearized
   * ~1k plate images.
   */ 
  public String
  getUndistorted1kPlateNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.Image1k.toDirPath(), 
				  joinNames(getFullShotName(), "bg_ud_1k"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the undistorted/linearized
   * ~1k QuickTime movie.
   */ 
  public String
  getUndistorted1kQuickTimeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.Image1k.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), 
					   joinNames(getFullShotName(), "bg_ud_1k")))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the approve node for the Plates task.
   */ 
  public String
  getPlateApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "plates_approve")); 
    return path.toString(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T R A C K I N G                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node for the PFTrack scene which 
   * is used to track the shot.
   */ 
  public String
  getPFTrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Edit), 
			 new Path(AppDirs.PFTrack.toDirPath(), 
				  joinNames(getFullShotName(), "pftrack"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * the camera/model tracking data.
   */ 
  public String
  getTrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "track"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * tracking verification render scene.
   */ 
  public String
  getTrackingVerifyNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "verify"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the rendered tracking 
   * verification images.
   */ 
  public String
  getTrackingVerifyImagesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "verify"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the composited tracking 
   * verification images over the undistored 2k plates.
   */ 
  public String
  getTrackingVerifyCompNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Focus), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "comp_verify"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Tracking task.
   */ 
  public String
  getTrackingSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "track_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing 
   */ 
  public String
  getTrackingExtractedCameraNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Product), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "camera"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing 
   */ 
  public String
  getTrackingExtractedTrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Product), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "track"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Tracking task.
   */ 
  public String
  getTrackingApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "track_approve")); 
    return path.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Join two strings together, inserting an underscore between them.
   */
  public String
  joinNames
  (
   String a,
   String b
  )
  {
    return a + "_" + b;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  static final long serialVersionUID = -5674419897754929249L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;

  
  /*-- GENERATED ---------------------------------------------------------------------------*/

  /**
   * Cached short names of the current project, shot sequence, shot and combined seqshot.
   */ 
  private String pProjectName;
  private String pSeqName;
  private String pShotName;
  
  /**
   * Cached fully resolved node directory paths for all combinations of task type and
   * node purpose for this shot.
   */ 
  private DoubleMap<TaskType, NodePurpose, Path>  pBasePaths;
  
}
