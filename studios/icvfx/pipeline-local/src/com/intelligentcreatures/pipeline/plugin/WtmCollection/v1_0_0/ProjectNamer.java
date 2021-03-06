// $Id: ProjectNamer.java,v 1.27 2008/08/01 20:19:14 jim Exp $

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
   * @param info
   *   The builder information instance.
   *
   * @param studioDefs
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   */
  public 
  ProjectNamer
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation info,
   StudioDefinitions studioDefs
  )
    throws PipelineException
  {
    super("ProjectNamer",
          "The basic naming class for project specific files.",
          mclient, qclient, info);

    pStudioDefs = studioDefs;
    pBasePaths  = new DoubleMap<AssetType, TaskType, Path>();

    {
      UtilityParam param =
        new StringUtilityParam
        (StudioDefinitions.aProjectName,
         "The Name of the project.",
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
  /*   S O U N D   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node name of the placeholder sound file.
   */
  public String
  getMissingSoundtrackNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Sound),
			 new Path(AppDirs.Placeholder.toDirPath(), "soundtrack"));
    return path.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P L A T E   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the undistorted camera grid
   * geometry file.
   */
  public String
  getPlatesCameraGridNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates),
			 new Path(AppDirs.Geo.toDirPath(), "cameraGrid"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the master Nuke script used to
   * undistort the plates.
   */
  public String
  getPlatesUndistortNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates),
			 new Path(AppDirs.Nuke.toDirPath(), "plateUndistort"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing Houdini scene which generates the
   * undistored UV map used by the master undistort Nuke script.
   */
  public String
  getPlatesUndistortHipNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Plates),
			 new Path(AppDirs.Hip.toDirPath(), "plateUndistort"));
    return path.toString();
  }




  /*----------------------------------------------------------------------------------------*/
  /*   M A T T E S   A S S E T S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the placeholder matte
   * creating Nuke scene.
   */
  public String
  getMattesPlaceholderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Mattes),
			 new Path(TaskType.Placeholder.toDirPath(), "mattes"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the Nuke script used to
   * verify the matte images.
   */
  public String
  getMattesVerifyNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Mattes),
			 new Path(AppDirs.Nuke.toDirPath(), "verify_mattes"));
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
   * Returns the fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking temp renders.
   */
  public String
  getTrackTempGlobalsNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking),
			 new Path(AppDirs.MEL.toDirPath(), "track_temp_globals"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to
   * attach shaders and setup the tracking temp render Maya scene.
   */
  public String
  getTrackTempPrepNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking),
			 new Path(AppDirs.MEL.toDirPath(), "track_temp_prep"));
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

  public String
  getTrackCameraChanNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking),
			 new Path(AppDirs.MEL.toDirPath(), "camera_chan"));
    return path.toString();
  }

  public String
  getTrackHatChanNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking),
			 new Path(AppDirs.MEL.toDirPath(), "hat_chan"));
    return path.toString();
  }

  public String
  getTrackMayaCamNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Tracking),
			 new Path(AppDirs.Otl.toDirPath(), "maya_cam"));
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

  /**
   * Returns the fully resolved name of the node containing the MEL script which transfers
   * animation from a rigged head to the clean non-rigged version.
   */
  public String
  getMatchPrebakeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Match),
			 new Path(AppDirs.MEL.toDirPath(), "match_prebake"));
    return path.toString();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   B L O T   A S S E T S                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to
   * attach the blot textures and shaders in the animation test render scene.
   */
  public String
  getBlotAttachPreviewNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Blot),
			 new Path(AppDirs.MEL.toDirPath(), "attach_preview"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to
   * attach shaders the shaders for the blot animation scene.
   */
  public String
  getBlotAnimPrepNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Blot),
			 new Path(AppDirs.MEL.toDirPath(), "blot_anim_prep"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to
   * attach shaders the shaders for the blot animation test render scene.
   */
  public String
  getBlotTestPrepNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Blot),
			 new Path(AppDirs.MEL.toDirPath(), "blot_test_prep"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the Maya render globals
   * settings for blot test renders.
   */
  public String
  getBlotTestGlobalsNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Blot),
			 new Path(AppDirs.MEL.toDirPath(), "blot_test_globals"));
    return path.toString();
  }

  public String
  getBlotLibOffsetNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Blot),
			 new Path(AppDirs.MEL.toDirPath(), "blot_lib_offset"));
    return path.toString();
  }

  /*----------------------------------------------------------------------------------------*/
  /*   N O I S E   A S S E T S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the Nuke script used to noise
   * up the blot animation textures.
   */
  public String
  getAddNoiseNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Noise),
			 new Path(AppDirs.Nuke.toDirPath(), "add_noise"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the Houdini scene used to
   * generate the noise displacement textures.
   */
  public String
  getNoiseDisplaceHipNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Noise),
			 new Path(AppDirs.Hip.toDirPath(), "displace"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the combined MEL scripts to
   * attach shaders for the noise animation test render scene.
   */
  public String
  getNoiseTestPrepNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Noise),
			 new Path(AppDirs.MEL.toDirPath(), "noise_test_prep"));
    return path.toString();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   L I G H T I N G   A S S E T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the Houdini script for lighting
   * assembly creation.
   */
  public String
  getLightingAssemblyPrepNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Cmd.toDirPath(), "assembly_prep"));
    return path.toString();
  }

  public String
  getLightingRigNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Hip.toDirPath(), "light_rig"));
    return path.toString();
  }

  public String
  getLightingPreAmbOccCmdNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Cmd.toDirPath(), "pre_ambOcc"));
    return path.toString();
  }

  public String
  getLightingRorAmbOccRenderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Otl.toDirPath(), "ror_ambOcc_render"));
    return path.toString();
  }

  public String
  getLightingRorBeautyRenderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Otl.toDirPath(), "ror_beauty_render"));
    return path.toString();
  }

  public String
  getLightingPreBeautyCmdNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Cmd.toDirPath(), "pre_beauty"));
    return path.toString();
  }

  public String
  getLightingPreInkConstCmdNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Cmd.toDirPath(), "pre_ink_const"));
    return path.toString();
  }

  public String
  getLightingRorInkblotRenderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Otl.toDirPath(), "ror_inkblot_render"));
    return path.toString();
  }

  public String
  getLightingGeoBuildNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Hip.toDirPath(), "geoBuild"));
    return path.toString();
  }

  public String
  getLightingSlapcompNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Nuke.toDirPath(), "slapcomp"));
    return path.toString();
  }

  public String
  getLightingPlateConvertNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Lighting),
			 new Path(AppDirs.Nuke.toDirPath(), "plate_convert"));
    return path.toString();
  }

  /*----------------------------------------------------------------------------------------*/
  /*   R O R S C H A C H   A S S E T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public String
  getRorschachMaskInkShaderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev),
			 new Path(AppDirs.Otl.toDirPath(), "mask_ink_shader"));
    return path.toString();
  }

  public String
  getRorschachMaskColNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
			 new Path(AppDirs.Rat.toDirPath(), "mask_col"));
    return path.toString();
  }

  public String
  getRorschachNeckBlendNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
			 new Path(AppDirs.Rat.toDirPath(), "neck_blend"));
    return path.toString();
  }

  public String
  getRorschachMaskFluffShaderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev),
			 new Path(AppDirs.Otl.toDirPath(), "mask_fluff_shader"));
    return path.toString();
  }

  public String
  getRorschachMaskShaderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev),
			 new Path(AppDirs.Otl.toDirPath(), "mask_shader"));
    return path.toString();
  }

  public String
  getRorschachMaskAoShaderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev),
			 new Path(AppDirs.Otl.toDirPath(), "mask_ao_shader"));
    return path.toString();
  }

  public String
  getRorschachClothBmpNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
			 new Path(AppDirs.Rat.toDirPath(), "cloth_bmp"));
    return path.toString();
  }

  public String
  getRorschachPaintBmpNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
			 new Path(AppDirs.Rat.toDirPath(), "paint_bmp"));
    return path.toString();
  }

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
   * rig used in the tracking temp renders.
   */
  public String
  getRorschachTempModelNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Modeling),
			 "ror_mdl_temp");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a Maya scene which provides a
   * clean unrigged model.
   */
  public String
  getRorschachHiresModelNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Modeling),
			 "ror_mdl_hires");
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

  /**
   * Returns the fully resolved name of the node containing the hat rig Maya scene.
   */
  public String
  getRorschachHatRigNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Rigging),
			 "hat_rig");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the blot animation verification test renders.
   */
  public String
  getRorschachPreviewShadersNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.LookDev),
			 "ror_shd_preview");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a placeholder Maya scene which
   * will eventually contain the blot animation.
   */
  public String
  getRorschachBlotAnimPlaceholderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Placeholder),
			 "ror_blot_anim");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a placeholder Maya scene which
   * will eventually contain the match animation.
   */
  public String
  getRorschachMatchAnimPlaceholderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Placeholder),
			 "ror_match");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the face guidelines image.
   */
  public String
  getRorschachGuidelinesNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Misc),
			 "guidelines");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the temp inkblot texture.
   */
  public String
  getRorschachTempTextureNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
			 "ror_tex_temp");
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the inkblot UVs
   */
  public String
  getRorschachInkblotUvNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Geometry),
    		new Path(AppDirs.Obj.toDirPath(), "lo_head_inkblot_uv"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing the mask UVs
   */
  public String
  getRorschachMaskUvNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Geometry),
    		new Path(AppDirs.Obj.toDirPath(), "lo_head_mask_uv"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing Rorschach's geo in OTL format
   */
  public String
  getRorschachGeoOtlNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Geometry),
    		new Path(AppDirs.Otl.toDirPath(), "ror_geo"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the placeholder Noise texture.
   */
  public String
  getRorschachNoisePlaceholderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Rorschach, TaskType.Texture),
    		new Path(AppDirs.Rat.toDirPath(), "noise_disp"));
    return path.toString();
  }

  /*----------------------------------------------------------------------------------------*/
  /*   T E M P   C O M P   A S S E T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing Nuke script used to redistort
   * CG elements to match the original plates.
   */
  public String
  getTempCompRedistortNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Compositing),
			 new Path(AppDirs.Nuke.toDirPath(), "cg_redistort"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing Nuke script used to perform
   * the temp comp.
   */
  public String
  getTempCompNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Compositing),
			 new Path(AppDirs.Nuke.toDirPath(), "temp_comp"));
    return path.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   Q U I C K T I M E   A S S E T S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * Nuke scripts for adding slates and per-frame overlays to the source images for a
   * deliverable QuickTime movie.
   */
  public Path
  getSlateNukeScriptsParentPath()
  {
    return new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
                    new Path(AppDirs.Nuke.toDirPath(), "slates"));
  }

  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * Nuke scripts for performing and final image reformatting before encoding the deliverable
   * images into a QuickTime movie.
   */
  public Path
  getFormatNukeScriptsParentPath()
  {
    return new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
                    new Path(AppDirs.Nuke.toDirPath(), "formats"));
  }

  /**
   * Returns the fully resolved node directory path to the parent directory of all existing
   * QuickTime codec settings files.
   */
  public Path
  getQtCodecSettingsParentPath()
  {
    return new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
                    new Path(AppDirs.QuickTime.toDirPath(), "codecs"));
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

  /**
  * TODO: Find out purpose
  */
  public String
  getGetChanFilePathNode()
  {
  	Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
			new Path(AppDirs.MEL.toDirPath(), "getChanFilePath"));
 	return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script to key the file
   * texture sequence for animated textures.
   */
  public String
  getSetFiletexSeqNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
			 new Path(AppDirs.MEL.toDirPath(), "set_filetex_seq"));
    return path.toString();
  }

  /**
   * Returns the fully resolved name of the node containing a MEL script to half the
   * currently set renderresolution.
   */
  public String
  getHalfResRenderNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
			 new Path(AppDirs.MEL.toDirPath(), "half_res_render"));
    return path.toString();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the fully resolved name of the node containing the master Nuke script used
   * to perform test comps.
   */
  public String
  getTestCompNukeNode()
  {
    Path path = new Path(pBasePaths.get(AssetType.Common, TaskType.Misc),
			 new Path(AppDirs.Nuke.toDirPath(), "test_comp"));
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
