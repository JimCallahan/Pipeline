// $Id: ShotNamer.java,v 1.29 2008/04/02 20:56:16 jim Exp $

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
   * Create a new namer.
   * 
   * @param name
   *    The name of the namer. 
   * 
   * @param desc
   *    A description of what the namer does. 
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
  protected 
  ShotNamer
  (
   String name, 
   String desc, 
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient);
    
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

  /**
   * Create a new namer.
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
  public 
  ShotNamer
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    this("ShotNamer", 
	 "Provides the names of nodes and node directories which are shot specific.", 
	 mclient, qclient, studioDefs);
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
  /*   S O U N D                                                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the prerequisites node for the Sound task.
   */ 
  public String
  getSoundPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Sound, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "sound_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the shot soundtrack node.
   */ 
  public String
  getSoundtrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Sound, NodePurpose.Edit), 
			 joinNames(getFullShotName(), "soundtrack")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Sound task.
   */ 
  public String
  getSoundSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Sound, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "sound_submit")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the MEL script used to load the soundtrack.
   */ 
  public String
  getAttachSoundtrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Sound, NodePurpose.Product), 
			 joinNames(getFullShotName(), "attach_soundtrack")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Sound task.
   */ 
  public String
  getSoundApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Sound, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "sound_approve")); 
    return path.toString(); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H D R I                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the prerequisites node for the HDRI task.
   */ 
  public String
  getHdriPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "hdri_prereq")); 
    return path.toString(); 
  }
  
  
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
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the LatLon format HDR environment map.
   */ 
  public String
  getDiagnosticHdrThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.HDRI, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "env")); 
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
   * Returns the fully resolved name of the prereq node for the Plates task.
   */ 
  public String
  getPlatesPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "plates_prereq")); 
    return path.toString(); 
  }


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
   * Returns the fully resolved name of the node containing the undistorted grid dots image
   * exported from PFTrack.
   */
  public String
  getDotGridImageNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
			 new Path(AppDirs.Grids.toDirPath(), 
				  joinNames(getFullShotName(), "dotGrid"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing the undistorted UV wedge image
   * exported from PFTrack.
   */
  public String
  getUvWedgeImageNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Edit), 
			 new Path(AppDirs.Grids.toDirPath(), 
				  joinNames(getFullShotName(), "uvWedge"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the Houdini command script
   * which loads undistorted grid dots image exported from PFTrack.
   */
  public String
  getDotGridCommandNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Cmd.toDirPath(), 
				  joinNames(getFullShotName(), "dotGrid"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing the Houdini command script
   * which loads the undistorted UV wedge image exported from PFTrack.
   */
  public String
  getUvWedgeCommandNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Cmd.toDirPath(), 
				  joinNames(getFullShotName(), "uvWedge"))); 
    return path.toString(); 
  }
    
  /**
   * Returns the fully resolved name of the node containing combined pre-render Houdini
   * command script which loads the undistorted UV wedge and grid dots images.
   */
  public String
  getRenderUvCommandNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Cmd.toDirPath(), 
				  joinNames(getFullShotName(), "render_uv"))); 
    return path.toString(); 
  }
    
  /**
   * Returns the fully resolved name of the node containing undistorted UV image rendered
   * by Houdni.
   */
  public String
  getUndistortUvImageNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.UV.toDirPath(), 
				  joinNames(getFullShotName(), "ud_uv_2k"))); 
    return path.toString(); 
  }
    
  /**
   * Returns the fully resolved name of the node containing Nuke script to read the 
   * undistorted UV image.
   */
  public String
  getReadUndistortUvImageNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "ud_uv_2k"))); 
    return path.toString(); 
  }
 
  /**
   * Returns the fully resolved name of the node contaning the Nuke script fragment
   * use to read in the raw cineon plates.
   */ 
  public String
  getReadPlatesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "plates"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  public String
  getUndistorted2kPlateNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Focus), 
			 new Path(AppDirs.Image2k.toDirPath(), 
				  joinNames(getFullShotName(), "bg_ud_2k"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the undistorted/linearized ~2k plate images.
   */ 
  public String
  getUndistorted2kPlateThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "bg_ud_2k")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Plates task.
   */ 
  public String
  getPlatesSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "plates_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the node containing redistorted UV image rendered
   * by Houdini.
   */
  public String
  getRedistortUvImageNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.UV.toDirPath(), 
				  joinNames(getFullShotName(), "rd_uv_2k"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the approved 
   * undistorted/linearized ~2k Cineon format plate images.
   */ 
  public String
  getApprovedUndistorted2kCineonPlateNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.Image2k.toDirPath(), 
				  new Path(AppDirs.Cin.toDirPath(), 
					   joinNames(getFullShotName(), "bg_ud_2k")))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the approved 
   * undistorted/linearized ~2k plate images.
   */ 
  public String
  getApprovedUndistorted2kPlateNode() 
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
   * Returns the fully resolved name of the node containing a MEL script used to set 
   * render resolutions which match that of the undistorted plates.
   */ 
  public String
  getResolutionNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Product), 
			 new Path(AppDirs.MEL.toDirPath(), 
				  joinNames(getFullShotName(), "resolution"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Plates task.
   */ 
  public String
  getPlatesApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Plates, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "plates_approve")); 
    return path.toString(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M A T T E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the prerequisites node for the Mattes task.
   */ 
  public String
  getMattesPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "mattes_prereq")); 
    return path.toString(); 
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node for containing the Nuke script edited 
   * by artists to generate the matte images.
   */ 
  public String
  getMattesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Edit), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "mattes"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node contaning the Nuke script fragment
   * use to read in the raw cineon plates.
   */ 
  public String
  getMattesReadPlatesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "plates"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the generated RGB channel 
   * encoded matte images.
   */ 
  public String
  getMattesImagesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Focus), 
			 new Path(AppDirs.Mattes.toDirPath(), 
				  joinNames(getFullShotName(), "mattes_rgb"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node contaning the Nuke script fragment
   * use to read in the generated RGB channel encoded matte images.
   */ 
  public String
  getMattesReadImagesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "mattes_rgb"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the composited matte images 
   * over the raw cineon plates. 
   */ 
  public String
  getMattesVerifyCompNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Focus), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "mattes_verify"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the composited mattes verification images.
   */ 
  public String
  getMattesVerifyThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "mattes_verify")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Mattes task.
   */ 
  public String
  getMattesSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "mattes_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the approved copy of the 
   * generated RGB channel encoded matte images.
   */ 
  public String
  getMattesApprovedImagesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Product), 
			 new Path(AppDirs.Mattes.toDirPath(), 
				  joinNames(getFullShotName(), "mattes_rgb"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Mattes task.
   */ 
  public String
  getMattesApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Mattes, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "mattes_approve")); 
    return path.toString(); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   T R A C K I N G                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the prerequisites node for the Tracking task.
   */ 
  public String
  getTrackingPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "track_prereq")); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the prerequisites node for the InternalTracking task.
   */ 
  public String
  getInternalTrackingPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "internal_track_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * the camera/model tracking data.
   */ 
  public String
  getTrackNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Edit), 
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
   * Returns the fully resolved name of the node for the pre-render MEL script used
   * to render the tracking verification images.
   */ 
  public String
  getTrackingPreRenderScriptNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prepare), 
			 new Path(AppDirs.MEL.toDirPath(), 
				  joinNames(getFullShotName(), "render"))); 
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
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the composited tracking verification images.
   */ 
  public String
  getTrackingVerifyThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "comp_verify")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing 2D tracking data exported
   * from PFTrack.
   */ 
  public String
  getTrackingMarkersNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Edit), 
			 new Path(AppDirs.PFTrack.toDirPath(), 
				  joinNames(getFullShotName(), "tracking_markers"))); 
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
   * Returns the fully resolved name of the node containing the extracted world space camera 
   * with all tracking animation baked.
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
   * Returns the fully resolved name of the node containing the extracted world space 
   * locators with all tracking animation baked.
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
   * Returns the fully resolved name of the node containing the approved copy of the 
   * 2D tracking data exported from PFTrack.
   */ 
  public String
  getApprovedTrackingMarkersNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Product), 
			 new Path(AppDirs.PFTrack.toDirPath(), 
				  joinNames(getFullShotName(), "tracking_markers"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * temp render scene.
   */ 
  public String
  getTrackingTempRenderNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "temp_render"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * temp texture loading scene.
   */ 
  public String
  getTrackingTempTextureNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "temp_tex"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of node containig the inkblot temp render images. 
   */ 
  public String
  getTrackingInkblotNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Tracking, NodePurpose.Product), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "inkblot"))); 
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
  /*   M A T C H                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the prerequisites node for the Match task.
   */ 
  public String
  getMatchPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "match_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node for the Maya scene used to perform the 
   * final head and facial matching animation.
   */ 
  public String
  getMatchAnimNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Edit), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "match"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing the full
   * match animation rig to be animated.
   */ 
  public String
  getPreMatchAnimNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "prematch"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the Maya scene containing 
   * match animation verification render scene.
   */ 
  public String
  getMatchVerifyNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "match_verify"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node for the pre-render MEL script used
   * to render the match animation verification images.
   */ 
  public String
  getMatchPreRenderScriptNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Prepare), 
			 new Path(AppDirs.MEL.toDirPath(), 
				  joinNames(getFullShotName(), "render"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the rendered match animation 
   * verification images.
   */ 
  public String
  getMatchVerifyImagesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "match"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the composited match animation 
   * verification images over the undistored 2k plates.
   */ 
  public String
  getMatchVerifyCompNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Focus), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "comp_match"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the composited match animation verification images.
   */ 
  public String
  getMatchVerifyThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "comp_match")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Match task.
   */ 
  public String
  getMatchSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "match_submit")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the pre-bake Maya scene.
   */ 
  public String
  getMatchPrebakeSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "prebake"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the baked Maya geometry cache. 
   */ 
  public String
  getMatchGeoCacheNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Product), 
			 new Path(AppDirs.Cache.toDirPath(), 
				  joinNames(getFullShotName(), "geo_cache"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing the per-frame baked
   * OBJ models.
   */ 
  public String
  getMatchMaskGeoNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Product), 
			 new Path(AppDirs.Obj.toDirPath(), 
				  joinNames(getFullShotName(), "mask_geo"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Match task.
   */ 
  public String
  getMatchApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Match, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "match_approve")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   B L O T                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the prerequisites node for the Blot task.
   */ 
  public String
  getBlotPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "blot_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the blot animation Maya scene node.
   */ 
  public String
  getBlotAnimSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Edit), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "blot_anim"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the rendered blot textures node.
   */ 
  public String
  getBlotAnimTexturesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "blot_anim"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the blot textures QuickTime movie.
   */ 
  public String
  getBlotAnimQuickTimeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), 
					   joinNames(getFullShotName(), "blot_anim")))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the blot textures. 
   */ 
  public String
  getBlotAnimThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "blot_anim")); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the blot texture loading Maya scene.
   */ 
  public String
  getBlotTextureSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "blot_tex"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the MEL script which attaches the geometry 
   * cache to the clean unrigged model.
   */ 
  public String
  getBlotAttachCacheNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Prepare), 
			 new Path(AppDirs.MEL.toDirPath(), 
				  joinNames(getFullShotName(), "attach_cache"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the blot test render Maya scene.
   */ 
  public String
  getBlotTestSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "blot_test"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the blot test render images.
   */ 
  public String
  getBlotTestImagesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "blot_test"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the blot test render QuickTime movie.
   */ 
  public String
  getBlotTestQuickTimeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), 
					   joinNames(getFullShotName(), "blot_test")))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the blot test render images.
   */ 
  public String
  getBlotTestThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "blot_test")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Blot task.
   */ 
  public String
  getBlotSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "blot_submit")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the rendered blot textures node.
   */ 
  public String
  getBlotApprovedTexturesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Product), 
			 new Path(AppDirs.Texture.toDirPath(), 
				  joinNames(getFullShotName(), "blot_anim"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the baked IGES curves exported from the blot 
   * animation Maya scene.
   */ 
  public String
  getBlotCurveGeoNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Product), 
			 new Path(AppDirs.Iges.toDirPath(), 
				  joinNames(getFullShotName(), "blot_curves"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Blot task.
   */ 
  public String
  getBlotApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Blot, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "blot_approve")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   N O I S E                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the prerequisites node for the Noise task.
   */ 
  public String
  getNoisePrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "noise_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the Nuke script used to read in the blot animation
   * textures.
   */ 
  public String
  getBlotAnimNukeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "blot_anim"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the rendered noise textures node.
   */ 
  public String
  getNoiseTexturesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "noise"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the noise textures QuickTime movie.
   */ 
  public String
  getNoiseQuickTimeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), 
					   joinNames(getFullShotName(), "noise")))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the noise textures. 
   */ 
  public String
  getNoiseThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "noise")); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the noise texture loading Maya scene.
   */ 
  public String
  getNoiseTextureSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "noise_tex"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the MEL script which attaches the geometry 
   * cache to the clean unrigged model.
   */ 
  public String
  getNoiseAttachCacheNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Prepare), 
			 new Path(AppDirs.MEL.toDirPath(), 
				  joinNames(getFullShotName(), "attach_cache"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the noise test render Maya scene.
   */ 
  public String
  getNoiseTestSceneNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Prepare), 
			 new Path(AppDirs.Maya.toDirPath(), 
				  joinNames(getFullShotName(), "noise_test"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the noise test render images.
   */ 
  public String
  getNoiseTestImagesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  joinNames(getFullShotName(), "noise_test"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the noise test render QuickTime movie.
   */ 
  public String
  getNoiseTestQuickTimeNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Focus), 
			 new Path(AppDirs.Render.toDirPath(), 
				  new Path(AppDirs.QuickTime.toDirPath(), 
					   joinNames(getFullShotName(), "noise_test")))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the noise test render images.
   */ 
  public String
  getNoiseTestThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "noise_test")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the Noise task.
   */ 
  public String
  getNoiseSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "noise_submit")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the rendered noise textures node.
   */ 
  public String
  getNoiseApprovedTexturesNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Product), 
			 new Path(AppDirs.Texture.toDirPath(), 
				  joinNames(getFullShotName(), "noise"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the Noise task.
   */ 
  public String
  getNoiseApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.Noise, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "noise_approve")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T E M P   C O M P                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the fully resolved name of the prerequisites node for the TempComp task.
   */ 
  public String
  getTempCompPrereqNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prereq), 
			 joinNames(getFullShotName(), "temp_comp_prereq")); 
    return path.toString(); 
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the outsourcer generated
   * temporary inkblot renders.
   */ 
  public String
  getTempCompInkblotNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Edit), 
                         joinNames(getFullShotName(), "inkblot")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the Nuke script to read 
   * the temporary inkblot renders.
   */ 
  public String
  getTempCompReadInkblotNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "inkblot"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the Nuke script to read 
   * the redistorted UV image.
   */ 
  public String
  getTempCompReadRedistortNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "rd_uv_2k"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the redisorted temporary
   * inkblot rendered images.
   */ 
  public String
  getTempCompInkblotRedistortedNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prepare), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "inkblot_rd"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the Nuke script to read 
   * the redisorted temporary inkblot renders.
   */ 
  public String
  getTempCompReadInkblotRedistortedNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "inkblot_rd"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node contaning the Nuke script fragment
   * use to read in the raw cineon plates.
   */ 
  public String
  getTempCompReadPlatesNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Prepare), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "plates"))); 
    return path.toString(); 
  }
  
  /**
   * Returns the fully resolved name of the node contaning the Nuke script which 
   * performs the temporary comp.
   */ 
  public String
  getTempCompNukeEditNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Edit), 
			 new Path(AppDirs.Nuke.toDirPath(), 
				  joinNames(getFullShotName(), "temp_comp"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing the temp composited images.
   */ 
  public String
  getTempCompNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Focus), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "temp_comp"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the node containing a thumbnail image extracted
   * from the temp composited images.
   */ 
  public String
  getTempCompThumbNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Thumbnail), 
			 joinNames(getFullShotName(), "temp_comp")); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the submit node for the TempComp task.
   */ 
  public String
  getTempCompSubmitNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Submit), 
			 joinNames(getFullShotName(), "temp_comp_submit")); 
    return path.toString(); 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the temp composited images.
   */ 
  public String
  getApprovedTempCompNode() 
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Product), 
			 new Path(AppDirs.Comp.toDirPath(), 
				  joinNames(getFullShotName(), "temp_comp"))); 
    return path.toString(); 
  }

  /**
   * Returns the fully resolved name of the approve node for the TempComp task.
   */ 
  public String
  getTempCompApproveNode()
  {
    Path path = new Path(pBasePaths.get(TaskType.TempComp, NodePurpose.Approve), 
			 joinNames(getFullShotName(), "temp_comp_approve")); 
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
  
  private static final long serialVersionUID = -5674419897754929249L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  protected StudioDefinitions pStudioDefs;

  
  /*-- GENERATED ---------------------------------------------------------------------------*/

  /**
   * Cached short names of the current project, shot sequence, shot and combined seqshot.
   */ 
  protected String pProjectName;
  protected String pSeqName;
  protected String pShotName;
  
  /**
   * Cached fully resolved node directory paths for all combinations of task type and
   * node purpose for this shot.
   */ 
  protected DoubleMap<TaskType, NodePurpose, Path>  pBasePaths;
  
}
