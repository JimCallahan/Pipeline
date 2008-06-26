// LightingBuilder.java
// Intelligent Creatures
// Author: Ryan Cameron

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I G H T I N G   B U I L D E R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Lighting task.<P>
 *
 *
 * Besides the common parameters shared by all builders, this builder defines the following
 * additional parameters: <BR>
 *
 * <DIV style="margin-left: 40px;">
 *   Project Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the overall project.
 *   </DIV> <BR>
 *
 *   Sequence Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the shot sequence.
 *   </DIV> <BR>
 *
 *   Shot Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The short name of the shot within a sequence.
 *   </DIV> <BR>
 *   <P>
 *
 *   Background Plate<BR>
 *   <DIV style="margin-left: 40px;">
 *     The prefix of the existing scanned images node to use as the background plates for
 *     this shot.
 *   </DIV> <BR>
 * </DIV>
 */
public
class LightingBuilder
  extends BaseShotBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor for to launch the builder.
   *
   * @param mclient
   *   The master manager connection.
   *
   * @param qclient
   *   The queue manager connection.
   *
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   */
  public
  LightingBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo
  )
    throws PipelineException
  {
    this(mclient, qclient, builderInfo,
         new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         null, null);
  }

  /**
   * Provided to allow parent builders to create instances and share namers.
   *
   * @param mclient
   *   The master manager connection.
   *
   * @param qclient
   *   The queue manager connection.
   *
   * @param builderInfo
   *   Information that is shared among all builders in a given invocation.
   *
   * @param studioDefs
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   *
   * @param projectNamer
   *   Provides project-wide names of nodes and node directories.
   *
   * @param shotNamer
   *   Provides the names of nodes and node directories which are shot specific.
   */
  public
  LightingBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation builderInfo,
   StudioDefinitions studioDefs,
   ProjectNamer projectNamer,
   ShotNamer shotNamer
  )
    throws PipelineException
  {
    super("Lighting",
          "A builder for constructing the nodes associated with the Lighting task.",
          mclient, qclient, builderInfo, studioDefs,
	  projectNamer, shotNamer, TaskType.Lighting);

    /* initialize fields */

    /* setup builder parameters */
    {
      /* selects the project, sequence and shot for the task */
      addLocationParam();
    }

    /* initialize the project namer */
    initProjectNamer();

    /* create the setup passes */
    {
      addSetupPass(new LightingSetupShotEssentials());
      addSetupPass(new GetPrerequisites());
    }

    /* setup the default editors */
    setCommonDefaultEditors();

    /* create the construct passes */
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);
    }

    /* specify the layout of the parameters for each pass in the UI */
    {
      PassLayoutGroup layout = new PassLayoutGroup("Root", "Root Layout");

      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("ShotEssentials", true);

        sub.addEntry(1, aUtilContext);
        sub.addEntry(1, null);
        sub.addEntry(1, aCheckinWhenDone);
        sub.addEntry(1, aActionOnExistence);
        sub.addEntry(1, aReleaseOnError);
        sub.addEntry(1, null);
        sub.addEntry(1, aLocation);

        layout.addPass(sub.getName(), sub);
      }

      {
        AdvancedLayoutGroup sub = new AdvancedLayoutGroup("None", true);
        layout.addPass(sub.getName(), sub);
      }

      setLayout(layout);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O V E R R I D E S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns a list of Actions required by this Builder, indexed by the toolset that
   * needs to contain them.
   * <p>
   * Builders should override this method to provide their own requirements.  This
   * validation gets performed after all the Setup Passes have been run but before
   * any Construct Passes are run.
   */
  @SuppressWarnings("unchecked")
  @Override
  public MappedArrayList<String, PluginContext>
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();

    // TODO: Update this list
    plugins.add(new PluginContext("MayaMEL"));
    plugins.add(new PluginContext("HfsScript"));
    plugins.add(new PluginContext("HfsReadCmd"));
    plugins.add(new PluginContext("HfsGConvert"));
    plugins.add(new PluginContext("Copy"));
    plugins.add(new PluginContext("CatFiles"));
    plugins.add(new PluginContext("HfsGEO"));
    plugins.add(new PluginContext("HfsBuild"));
    plugins.add(new PluginContext("HfsGenerate"));
    plugins.add(new PluginContext("HfsIConvert"));
    plugins.add(new PluginContext("HfsMantra"));
    plugins.add(new PluginContext("Touch"));

    MappedArrayList<String, PluginContext> toReturn =
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class LightingSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public
    LightingSetupShotEssentials()
    {
      super();
    }

    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void
    validatePhase()
      throws PipelineException
    {
      super.validatePhase();

      /* register the required (locked) nodes */
      {
    	  pGetChanFilePathNodeName = pProjectNamer.getGetChanFilePathNode();
    	  pRequiredNodeNames.add(pGetChanFilePathNodeName);

    	  pCameraChanNodeName = pProjectNamer.getTrackCameraChanNode();
    	  pRequiredNodeNames.add(pCameraChanNodeName);

    	  pHatChanNodeName = pProjectNamer.getTrackHatChanNode();
    	  pRequiredNodeNames.add(pHatChanNodeName);

    	  pAssemblyPrepNodeName = pProjectNamer.getLightingAssemblyPrepNode();
    	  pRequiredNodeNames.add(pAssemblyPrepNodeName);

    	  pMayaCamNodeName = pProjectNamer.getTrackMayaCamNode();
    	  pRequiredNodeNames.add(pMayaCamNodeName);

    	  pInkblotUvNodeName = pProjectNamer.getRorschachInkblotUvNode();
    	  pRequiredNodeNames.add(pInkblotUvNodeName);

    	  pMaskUvNodeName = pProjectNamer.getRorschachMaskUvNode();
    	  pRequiredNodeNames.add(pMaskUvNodeName);

    	  pRorGeoOtlNodeName = pProjectNamer.getRorschachGeoOtlNode();
    	  pRequiredNodeNames.add(pRorGeoOtlNodeName);

    	  // AmbOcc

    	  pPreAmbOccCmdNodeName = pProjectNamer.getLightingPreAmbOccCmdNode();
    	  pRequiredNodeNames.add(pPreAmbOccCmdNodeName);

    	  pRorAmbOccRenderNode = pProjectNamer.getLightingRorAmbOccRenderNode();
    	  pRequiredNodeNames.add(pRorAmbOccRenderNode);

    	  pRorMaskAoShaderNodeName = pProjectNamer.getRorschachMaskAoShaderNode();
    	  pRequiredNodeNames.add(pRorMaskAoShaderNodeName);

    	  // AmbOcc and Beauty

    	  pRorClothBmpNodeName = pProjectNamer.getRorschachClothBmpNode();
    	  pRequiredNodeNames.add(pRorClothBmpNodeName);

    	  pRorPaintBmpNodeName = pProjectNamer.getRorschachPaintBmpNode();
    	  pRequiredNodeNames.add(pRorPaintBmpNodeName);

    	  // Beauty

    	  pRorBeautyRenderNodeName = pProjectNamer.getLightingRorBeautyRenderNode();
    	  pRequiredNodeNames.add(pRorBeautyRenderNodeName);

    	  pPreBeautyCmdNodeName = pProjectNamer.getLightingPreBeautyCmdNode();
    	  pRequiredNodeNames.add(pPreBeautyCmdNodeName);

    	  pRorMaskFluffShaderNodeName = pProjectNamer.getRorschachMaskFluffShaderNode();
    	  pRequiredNodeNames.add(pRorMaskFluffShaderNodeName);

    	  pRorMaskShaderNodeName = pProjectNamer.getRorschachMaskShaderNode();
    	  pRequiredNodeNames.add(pRorMaskShaderNodeName);

    	  pRorNeckBlendNodeName = pProjectNamer.getRorschachNeckBlendNode();
    	  pRequiredNodeNames.add(pRorNeckBlendNodeName);

    	  pRorMaskColNodeName = pProjectNamer.getRorschachMaskColNode();
    	  pRequiredNodeNames.add(pRorMaskColNodeName);

    	  pLightRigNodeName = pProjectNamer.getLightingRigNode();
    	  pRequiredNodeNames.add(pLightRigNodeName);

    	  // Ink
    	  pPreInkConstCmdNodeName = pProjectNamer.getLightingPreInkConstCmdNode();
    	  pRequiredNodeNames.add(pPreInkConstCmdNodeName);

    	  pRorInkblotRenderNodeName = pProjectNamer.getLightingRorInkblotRenderNode();
    	  pRequiredNodeNames.add(pRorInkblotRenderNodeName);

    	  pRorMaskInkShaderNodeName = pProjectNamer.getRorschachMaskInkShaderNode();
    	  pRequiredNodeNames.add(pRorMaskInkShaderNodeName);

      }
    }

    private static final long serialVersionUID = -6691101175651749910L;
  }

  /*----------------------------------------------------------------------------------------*/

  private
  class GetPrerequisites
  extends SetupPass
  {
    public
    GetPrerequisites()
    {
      super("Get Prerequisites",
            "Get the names of the prerequitsite nodes.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void
    validatePhase()
      throws PipelineException
    {
    	// This pass ensures that all of the "prereqs" are in place.

  	  	pResolutionMelNodeName = pShotNamer.getResolutionNode();
  	  	pRequiredNodeNames.add(pResolutionMelNodeName);

	  	pTrackExtractedCamNodeName = pShotNamer.getTrackingExtractedCameraNode();
	  	pRequiredNodeNames.add(pTrackExtractedCamNodeName);

	  	pTrackExtractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
  	  	pRequiredNodeNames.add(pTrackExtractedTrackNodeName);

  	  	pMatchMaskGeoNodeName = pShotNamer.getMatchMaskGeoNode();
  	  	pRequiredNodeNames.add(pMatchMaskGeoNodeName);

  	  	pNoiseDisplaceNodeName = pShotNamer.getNoiseDisplaceNode();
  	  	// FIXME: pRequiredNodeNames.add(pNoiseDisplaceNodeName);

        /* the background plates node */
        pUndistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode();
        pRequiredNodeNames.add(pUndistorted1kPlateNodeName);

        /* lookup the frame range of the shot by looking at the undistorted 1k plates node */
	  	NodeVersion vsn = pClient.getCheckedInVersion(pUndistorted1kPlateNodeName, null);
	  	if(vsn == null)
	  	  throw new PipelineException
	  	    ("Somehow no checked-in version of the undistorted 1k plates node " +
	  	     "(" + pUndistorted1kPlateNodeName + ") exists!");

	  	pFrameRange = vsn.getPrimarySequence().getFrameRange();
    }

   private static final long serialVersionUID = 7830642124642481657L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  protected
  class BuildNodesPass
    extends ConstructPass
  {
    public
    BuildNodesPass()
    {
      super("Build Submit/Approve Nodes",
            "Creates the nodes which make up the Lighting task.");
    }

    /**
     * Create the plates node networks.
     */
    @Override
    public void
    buildPhase()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();

      /* stage prerequisites */
      {
	/* lock the latest version of all of the prerequisites */
	lockNodePrerequisites();

	String prereqNodeName = pShotNamer.getLightingPrereqNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.addAll(pRequiredNodeNames);

	  TargetStage stage =
	    new TargetStage(stageInfo, pContext, pClient,
			    prereqNodeName, sources);
	  addTaskAnnotation(stage, NodePurpose.Prereq);
	  stage.build();
	  addToQueueList(prereqNodeName);
	  addToCheckInList(prereqNodeName);
	}
      }

//      /* add Edit annotations to all undistort images, reference images and plates */
//      {
//	addMissingTaskAnnotation(pDotGridImageNodeName, NodePurpose.Edit);
//	addMissingTaskAnnotation(pUvWedgeImageNodeName, NodePurpose.Edit);
//	addMissingTaskAnnotation(pBackgroundPlateNodeName, NodePurpose.Edit);
//	for(String name : pMiscReferenceNodeNames)
//	  addMissingTaskAnnotation(name, NodePurpose.Edit);
//      }

      /* the submit network */
      {
    	  // Build node: <vfxno>_camera.chan
    	  // NOTE! The order that these are added to the list determines the order
    	  // in which they are run.
    	  String cameraChanNodeName = pShotNamer.getTrackingCameraChanNode();
    	  {
        	  ArrayList<String> scripts = new ArrayList<String>();
        	  scripts.add(pResolutionMelNodeName);
        	  scripts.add(pGetChanFilePathNodeName);
        	  scripts.add(pCameraChanNodeName);

    		  MayaMelStage stage =
    			  new MayaMelStage(stageInfo, pContext, pClient,
    					  cameraChanNodeName, pTrackExtractedCamNodeName,
    					  scripts, false, null, null, null, "chan");

    		  addTaskAnnotation(stage, NodePurpose.Product);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_hat.chan
    	  String hatChanNodeName = pShotNamer.getTrackingHatChanNode();
    	  {
        	  ArrayList<String> scripts = new ArrayList<String>();
        	  scripts.add(pGetChanFilePathNodeName);
        	  scripts.add(pHatChanNodeName);

    		  MayaMelStage stage =
    			  new MayaMelStage(stageInfo, pContext, pClient,
    					  hatChanNodeName, pTrackExtractedTrackNodeName,
    					  scripts, false, null, null, null, "chan");

    		  addTaskAnnotation(stage, NodePurpose.Product);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_mask_geo.#.bgeo
    	  String maskBGeoNodeName = pShotNamer.getMatchMaskBGeoNode();
    	  {
    		  HfsGConvertStage stage =
    			  new HfsGConvertStage(stageInfo, pContext, pClient,
    					  maskBGeoNodeName, pMatchMaskGeoNodeName, pFrameRange, FRAME_PADDING);

      		  addTaskAnnotation(stage, NodePurpose.Product);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_camera.cmd
    	  String rorCamCmdNodeName = pShotNamer.getLightingCamCmdNode();
    	  {
    		  HfsReadCmdStage stage =
    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
    					  rorCamCmdNodeName, cameraChanNodeName,
    					  "/obj/CAMERA", "chanFile");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_hat.cmd
    	  String rorHatCmdNodeName = pShotNamer.getLightingHatCmdNode();
    	  {
    		  HfsReadCmdStage stage =
    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
    					  rorHatCmdNodeName, hatChanNodeName,
    					  "/obj/RORSCHACH", "hatChanFile");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ror_geo.cmd
    	  String rorGeoCmdNodeName = pShotNamer.getLightingRorGeoCmdNode();
    	  {
    		  HfsReadCmdStage stage =
    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
    					  rorGeoCmdNodeName, maskBGeoNodeName,
    					  "/obj/RORSCHACH", "geoFile");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_assembly_create.cmd
    	  String assemblyCreateCmdNodeName = pShotNamer.getLightingAssemblyCreateCmdNode();
    	  {
    		  WriteFileStage stage =
    			  new WriteFileStage("CreateAssemblyCmd",
    					  "Creates a Houdini cmd file which saves the assembly.hip file.",
    					  stageInfo, pContext, pClient,
    					  assemblyCreateCmdNodeName, "cmd", "TextFile",
    					  "mwrite " + "$WORKING" + pShotNamer.getLightingHipAssemblyNode() + ".hip");

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_assembly.hip
    	  String hipAssemblyNodeName = pShotNamer.getLightingHipAssemblyNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ArrayList<String> unordered = new ArrayList<String>();

    		  ordered.add(pRorGeoOtlNodeName);
    		  ordered.add(pMayaCamNodeName);
    		  ordered.add(pAssemblyPrepNodeName);
    		  ordered.add(rorGeoCmdNodeName);
    		  ordered.add(rorHatCmdNodeName);
    		  ordered.add(rorCamCmdNodeName);
    		  ordered.add(assemblyCreateCmdNodeName);

    		  unordered.add(pInkblotUvNodeName);
    		  unordered.add(pMaskUvNodeName);

    		  BuildLightingAssemblyStage stage =
    			  new BuildLightingAssemblyStage(stageInfo, pContext, pClient,
    					  hipAssemblyNodeName, ordered, unordered, true);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: ambOcc_prep.cmd
    	  String ambOccPrepCmdNodeName = pShotNamer.getLightingAmbOccPrepCmdNode();
    	  {
       		  WriteFileStage stage =
    			  new WriteFileStage("CreateAmbOccPrepCmd",
    					  "Creates a Houdini cmd which preps the Amb Occ phase.",
    					  stageInfo, pContext, pClient,
    					  ambOccPrepCmdNodeName, "cmd", "TextFile",
    					  generateAmbOccPrepCmd());

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_pre_ambOcc.hip
    	  String preAmbOccHipNodeName = pShotNamer.getLightingPreAmbOccHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ArrayList<String> unordered = new ArrayList<String>();

    		  ordered.add(hipAssemblyNodeName);
    		  ordered.add(pRorMaskAoShaderNodeName);
    		  ordered.add(pRorAmbOccRenderNode);
    		  ordered.add(pPreAmbOccCmdNodeName);
    		  ordered.add(ambOccPrepCmdNodeName);

    		  unordered.add(pRorClothBmpNodeName);
    		  unordered.add(pRorPaintBmpNodeName);

    		  BuildLightingAssemblyStage stage =
    			  new BuildLightingAssemblyStage(stageInfo, pContext, pClient,
    					  preAmbOccHipNodeName, ordered, unordered, true);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: beauty_prep.cmd
    	  String beautyPrepCmdNodeName = pShotNamer.getLightingBeautyPrepCmdNode();
    	  {
       		  WriteFileStage stage =
    			  new WriteFileStage("CreateBeautyPrepCmd",
    					  "Creates a Houdini cmd which preps the beauty phase.",
    					  stageInfo, pContext, pClient,
    					  beautyPrepCmdNodeName, "cmd", "TextFile",
    					  generateBeautyPrepCmd());

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
    		  stage.build();
    	  }

    	  // TODO: uncomment this when the noise network is fixed
    	  // Build node: _mask_disp.cmd
//    	  String maskDispCmdNodeName = pShotNamer.getLightingMaskDispCmdNode();
//    	  {
//    		  HfsReadCmdStage stage =
//    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
//    					  maskDispCmdNodeName, pNoiseDisplaceNodeName,
//    					  "/shop/mask", "InkblotMap");
//
//      		  addTaskAnnotation(stage, NodePurpose.Prepare);
//      		  stage.build();
//    	  }

    	  // Note: building ri_preset_lgt.hip node
    	  // We don't build it if it's already in the repository
    	  String presetLightHipNodeName = StudioDefinitions.getSequencePath(pShotNamer.getProjectName(),
    			  pShotNamer.getSequenceName()).toString() + "/presets/lighting/edit/hip/"
    			  + pShotNamer.getSequenceName() + "_preset_lgt" ;

    	  NodeVersion vsn = pClient.getCheckedInVersion(presetLightHipNodeName, null);
  	  	  if(vsn == null)
    	  {
    		  DoCopyStage stage =
    			  new DoCopyStage("CopyLightRig", "Copy the general light rig to the shot level.",
    					  stageInfo, pContext, pClient,
    					  presetLightHipNodeName, "hip",
    					  pLightRigNodeName);

      		  addTaskAnnotation(stage, NodePurpose.Edit);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_pre_beauty.hip
    	  String preBeautyHipNodeName = pShotNamer.getLightingPreBeautyHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ArrayList<String> unordered = new ArrayList<String>();

    		  ordered.add(hipAssemblyNodeName);
    		  ordered.add(presetLightHipNodeName);
    		  ordered.add(pRorMaskShaderNodeName);
    		  ordered.add(pRorMaskFluffShaderNodeName);
    		  ordered.add(pRorBeautyRenderNodeName);
       		  ordered.add(pPreBeautyCmdNodeName);
    		 // TODO: ordered.add(maskDispCmdNodeName);
    		  ordered.add(beautyPrepCmdNodeName);

    		  unordered.add(pRorClothBmpNodeName);
    		  unordered.add(pRorPaintBmpNodeName);
    		  unordered.add(pRorNeckBlendNodeName);
    		  unordered.add(pRorMaskColNodeName);

    		  BuildLightingAssemblyStage stage =
    			  new BuildLightingAssemblyStage(stageInfo, pContext, pClient,
    					  preBeautyHipNodeName, ordered, unordered, true);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: ink_prep.cmd
    	  String inkPrepCmdNodeName = pShotNamer.getLightingInkPrepCmdNode();
    	  {
       		  WriteFileStage stage =
    			  new WriteFileStage("CreateInkPrepCmd",
    					  "Creates a Houdini cmd which preps the ink phase.",
    					  stageInfo, pContext, pClient,
    					  inkPrepCmdNodeName, "cmd", "TextFile",
    					  generateInkPrepCmd());

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_pre_ink_const.hip
    	  String preInkHipNodeName = pShotNamer.getLightingPreInkHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ArrayList<String> unordered = new ArrayList<String>();

    		  ordered.add(hipAssemblyNodeName);
    		  ordered.add(pRorInkblotRenderNodeName);
    		  ordered.add(pRorMaskInkShaderNodeName);
    		  ordered.add(pPreInkConstCmdNodeName);
    		 // TODO: ordered.add(maskDispCmdNodeName);
    		  ordered.add(inkPrepCmdNodeName);

    		  unordered.add(pRorClothBmpNodeName);
    		  unordered.add(pRorPaintBmpNodeName);

    		  BuildLightingAssemblyStage stage =
    			  new BuildLightingAssemblyStage(stageInfo, pContext, pClient,
    					  preInkHipNodeName, ordered, unordered, true);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

//    	  String submitNodeName = pShotNamer.getLightingSubmitNode();
//    	  {
//    		  TreeSet<String> sources = new TreeSet<String>();
//    		  sources.add(cameraChanNodeName);
//    		  sources.add(hatChanNodeName);
//
//    		  TargetStage stage =
//    			    new TargetStage(stageInfo, pContext, pClient,
//    					    submitNodeName, sources);
//    		  addTaskAnnotation(stage, NodePurpose.Submit);
//    		  stage.build();
//    		  addToQueueList(submitNodeName);
//    		  addToCheckInList(submitNodeName);
//    	  }

      }
    }

    private String generateAmbOccPrepCmd()
    {
    	String script = "";

    	script += "set START = " + pFrameRange.getStart() + "\n";
    	script += "set END = " + pFrameRange.getEnd() + "\n";
    	script += "opparm ambOcc soho_diskfile " +
    			"( '$WORKING" + pShotNamer.getLightingAmbOcc2kIfdNode() + ".$F4.ifd' )" + "\n";
    	script += "opparm ambOcc vm_picture " +
				"( '$WORKING" + pShotNamer.getLightingAmbOcc2kExrNode() + ".$F4.exr' )" + "\n";
    	script += "mwrite $WORKING" + pShotNamer.getLightingPreAmbOccHipNode() + ".hip" + "\n";

    	return script;
    }

    private String generateBeautyPrepCmd()
    {
    	String script = "";

    	script += "set START = " + pFrameRange.getStart() + "\n";
    	script += "set END = " + pFrameRange.getEnd() + "\n";
    	script += "set SEQ = " + pShotNamer.getSequenceName() + "\n";
    	script += "set SHOTNUM = " + pShotNamer.getShotName() + "\n";
    	script += "opparm beauty soho_diskfile " +
    			"( '$WORKING" + pShotNamer.getLightingBeauty2kIfdNode() + ".$F4.ifd' )" + "\n";
    	script += "opparm beauty vm_picture " +
				"( '$WORKING" + pShotNamer.getLightingBeauty2kExrNode() + ".$F4.exr' )" + "\n";
    	script += "mwrite $WORKING" + pShotNamer.getLightingPreBeautyHipNode() + ".hip" + "\n";

    	return script;
    }

    private String generateInkPrepCmd()
    {
    	String script = "";

    	script += "set START = " + pFrameRange.getStart() + "\n";
    	script += "set END = " + pFrameRange.getEnd() + "\n";
    	script += "set SEQ = " + pShotNamer.getSequenceName() + "\n";
    	script += "set SHOTNUM = " + pShotNamer.getShotName() + "\n";
    	script += "opparm ink_const soho_diskfile " +
    			"( '$WORKING" + pShotNamer.getLightingInk2kIfdNode() + ".$F4.ifd' )" + "\n";
    	script += "opparm ink_const vm_picture " +
				"( '$WORKING" + pShotNamer.getLightingInk2kExrNode() + ".$F4.exr' )" + "\n";
    	script += "mwrite $WORKING" + pShotNamer.getLightingPreInkHipNode() + ".hip" + "\n";

    	return script;
    }

    private static final long serialVersionUID = -5216068758078265109L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4601321412376464763L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Input (required) nodes. These will be locked.
   */
  private String pGetChanFilePathNodeName;
  private String pCameraChanNodeName;
  private String pResolutionMelNodeName;
  private String pTrackExtractedCamNodeName;
  private String pHatChanNodeName;
  private String pTrackExtractedTrackNodeName;
  private String pMatchMaskGeoNodeName;
  private String pUndistorted1kPlateNodeName;

  private String pAssemblyPrepNodeName;
  private String pMayaCamNodeName;
  private String pInkblotUvNodeName;
  private String pMaskUvNodeName;
  private String pRorGeoOtlNodeName;

  private String pPreAmbOccCmdNodeName;
  private String pRorAmbOccRenderNode;
  private String pRorMaskAoShaderNodeName;
  private String pRorClothBmpNodeName;
  private String pRorPaintBmpNodeName;

  private String pNoiseDisplaceNodeName;

  private String pRorBeautyRenderNodeName;
  private String pPreBeautyCmdNodeName;
  private String pRorMaskFluffShaderNodeName;
  private String pRorMaskShaderNodeName;
  private String pRorNeckBlendNodeName;
  private String pRorMaskColNodeName;

  // Ink
  private String pPreInkConstCmdNodeName;
  private String pRorInkblotRenderNodeName;
  private String pRorMaskInkShaderNodeName;

  private String pLightRigNodeName;

  private FrameRange pFrameRange;
  private static final Integer FRAME_PADDING = 4;
}

// Ink

