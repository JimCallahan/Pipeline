// LightingBuilder.java
// Intelligent Creatures
// Author: Ryan Cameron
// Version 1.1.0
// July 4, 2008

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.SetupPass;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I G H T I N G   B U I L D E R                                                        */
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

    /* the background plate images */
    {
      UtilityParam param =
        new PlaceholderUtilityParam
        (aBackgroundPlate,
         "Select the existing scanned images node to use as the background plates for " +
         "this shot.");
      addParam(param);
    }

    /* initialize the project namer */
    initProjectNamer();

    /* create the setup passes */
    {
      addSetupPass(new LightingSetupShotEssentials());
      addSetupPass(new SetupImageParams());
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

      {
		AdvancedLayoutGroup sub = new AdvancedLayoutGroup("GetPrerequisites", true);
	        sub.addEntry(1, aBackgroundPlate);

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

    	  pGeoBuildNodeName = pProjectNamer.getLightingGeoBuildNode();
    	  pRequiredNodeNames.add(pGeoBuildNodeName);

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

    	  // Preview network
    	  pBaseSlapcompNukeNodeName = pProjectNamer.getLightingSlapcompNukeNode();
    	  pRequiredNodeNames.add(pBaseSlapcompNukeNodeName);
      }
    }

    private static final long serialVersionUID = -6691101175651749910L;
  }

  /*----------------------------------------------------------------------------------------*/

  private
  class SetupImageParams
  extends SetupPass
  {
    public
    SetupImageParams()
    {
      super("Setup Image Params",
            "Setup the actual source image parameters.");
    }

    /**
     * Replace the placeholder ReferenceImages and BackgroundPlate parameters with a
     * real ones.
     */
    @Override
    public void
    initPhase()
      throws PipelineException
    {
      {
	Path path = pShotNamer.getPlatesScannedParentPath();
	ArrayList<String> pnames = findChildNodeNames(path);
	if((pnames == null) || pnames.isEmpty())
	  throw new PipelineException
	    ("Unable to find any scanned image nodes in (" + path + ")!");

	EnumUtilityParam param =
          new EnumUtilityParam
          (aBackgroundPlate,
           "Select the existing scanned images node to use as the background plates for " +
           "this shot.",
           pnames.get(0), pnames);

        replaceParam(param);
      }
    }

    private static final long serialVersionUID = 3897345314400900929L;
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
        /* the original background plates node */

		String bgName = (String) getParamValue(aBackgroundPlate);
		if(bgName == null)
			throw new PipelineException
		      ("No " + aBackgroundPlate + " image node was selected!");
		Path path = new Path(pShotNamer.getPlatesScannedParentPath(), bgName);

	  	try
	  	{
	  		NodeVersion vsn = pClient.getCheckedInVersion(path.toString(), null);
	  		pFrameRange = vsn.getPrimarySequence().getFrameRange();
	  		pBackgroundPlateNodeName = vsn.getName();
	  		pRequiredNodeNames.add(pBackgroundPlateNodeName);
	  	}
	  	catch(PipelineException ex)
	  	{
	  		throw new PipelineException
	  	    	("Somehow no checked-in version of the " + aBackgroundPlate + " node " +
	  	    	"(" + path + ") exists!");
	  	}

    	// This pass ensures that all of the "prereqs" are in place.
  	  	pResolutionMelNodeName = pShotNamer.getResolutionNode();
  	  	pRequiredNodeNames.add(pResolutionMelNodeName);

	  	pTrackExtractedCamNodeName = pShotNamer.getTrackingExtractedCameraNode();
	  	pRequiredNodeNames.add(pTrackExtractedCamNodeName);

	  	pTrackExtractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
  	  	pRequiredNodeNames.add(pTrackExtractedTrackNodeName);

  	  	pMatchMaskGeoNodeName = pShotNamer.getMatchMaskGeoNode();
  	  	pRequiredNodeNames.add(pMatchMaskGeoNodeName);

  	  	pNoiseDispAllNodeName = pShotNamer.getNoiseDisplaceAllNode();
  	  	pRequiredNodeNames.add(pNoiseDispAllNodeName);

  	  	pUndistorted1kQuickTimeNodeName = pShotNamer.getUndistorted1kQuickTimeNode();
  	  	pRequiredNodeNames.add(pUndistorted1kQuickTimeNodeName);

  	  	pNoiseDisplaceNodeName = pShotNamer.getNoiseApprovedDisplaceNode();
  	  	pRequiredNodeNames.add(pNoiseDisplaceNodeName);

  	  	pRedistortUvImageNode = pShotNamer.getRedistortUvImageNode();
		pRequiredNodeNames.add(pRedistortUvImageNode);

		pMattesApprovedImagesNodeName = pShotNamer.getMattesApprovedImagesNode();
		pRequiredNodeNames.add(pMattesApprovedImagesNodeName);

		pUndistorted1kQuickTimeNode = pShotNamer.getUndistorted1kQuickTimeNode();
        pRequiredNodeNames.add(pUndistorted1kQuickTimeNode);

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

	  /*
	   * SUBMIT/APPROVE/PREVIEW NETWORKS
	   */
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
    					  "/obj/HAT_CHAN/chan_in", "file");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_obj.cmd
    	  String rorObjCmdNodeName = pShotNamer.getLightingObjCmdNode();
    	  {
    		  HfsReadCmdStage stage =
    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
    					  rorObjCmdNodeName, pMatchMaskGeoNodeName,
    					  "/obj/READ_OBJ/read_obj", "file");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_geoBuild.cmd
    	  String geoBuildCmdNodeName = pShotNamer.getLightingGeoBuildCmdNode();
    	  {
    		  LinkedList<String> sources = new LinkedList<String>();
    		  sources.add(rorObjCmdNodeName);
    		  sources.add(rorHatCmdNodeName);

    		  CatFilesStage stage =
    			  new CatFilesStage(stageInfo, pContext, pClient,
    					  geoBuildCmdNodeName, "cmd", sources);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_mask_geo.#.bgeo
    	  String maskBGeoNodeName = pShotNamer.getMatchMaskBGeoNode();
    	  {
    		  HfsGeoStage stage =
    			  new HfsGeoStage(stageInfo, pContext, pClient,
    					  maskBGeoNodeName, pFrameRange, FRAME_PADDING,
    					  "bgeo", pGeoBuildNodeName, "/obj/WRITE_BGEO/write_bgeo",
    					  false, null, geoBuildCmdNodeName, null, null, null);

      		  addTaskAnnotation(stage, NodePurpose.Product);
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

    	  // Build node: <vfxno>_ambOcc.hip
    	  String ambOccHipNodeName = pShotNamer.getLightingAmbOccHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ordered.add(preAmbOccHipNodeName);

    		  HfsBuildStage stage =
    			  new HfsBuildStage(stageInfo, pContext, pClient,
    					  ambOccHipNodeName, ordered, null, false,
    					  null, null, null, null);

      		  addTaskAnnotation(stage, NodePurpose.Edit);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ambOcc_2k.ifd
    	  String ambOcc2kIfdNodeName = pShotNamer.getLightingAmbOcc2kIfdNode();
    	  {
    		  HfsGenerateStage stage =
    			  new HfsGenerateStage(stageInfo, pContext, pClient,
    					  ambOcc2kIfdNodeName, pFrameRange, FRAME_PADDING,
    					  "ifd", ambOccHipNodeName,
    					  "/out/ambOcc", null, false, null);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ambOcc_2k.exr
    	  String ambOcc2kExrNodeName = pShotNamer.getLightingAmbOcc2kExrNode();
    	  {
    		  HfsMantraStage stage =
    			  new HfsMantraStage(stageInfo, pContext, pClient,
    					  ambOcc2kExrNodeName, pFrameRange, FRAME_PADDING,
    					  "exr", ambOcc2kIfdNodeName, 1, null, null,
    					  "Color Image", "Natural", "Full", "-j 0",
    					  "Mixed", true, "All", true, true, 1.0, null, 1.0,
    					  "Default", 4096, 10, 1024, 1.0, "0 (None)", "None");

      		  addTaskAnnotation(stage, NodePurpose.Focus);
      		  stage.build();
    	  }

    	  String ambOcc2kJpgNodeName = pShotNamer.getLightingAmbOcc2kJpgNode();
    	  {
    		  HfsIConvertStage stage =
    			    new HfsIConvertStage(stageInfo, pContext, pClient,
    			    		ambOcc2kJpgNodeName, ambOcc2kExrNodeName,
    			    		pFrameRange, FRAME_PADDING, "jpg", "8-Bit (byte)");

      		  addTaskAnnotation(stage, NodePurpose.Focus);
      		  stage.build();
    	  }

    	  // BEAUTY STREAM


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

    	  // Build node: _mask_disp.cmd
    	  String maskDispCmdNodeName = pShotNamer.getLightingMaskDispCmdNode();
    	  {
    		  HfsReadCmdStage stage =
    			  new HfsReadCmdStage(stageInfo, pContext, pClient,
    					  maskDispCmdNodeName, pNoiseDisplaceNodeName,
    					  "/shop/mask", "InkblotMap");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

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
  	  	  // The presets rig is in the repository, so we'll need to check it out
  	  	  else
  	  	  {
  	  		  pClient.checkOut(getAuthor(), getView(), presetLightHipNodeName, null,
                    CheckOutMode.OverwriteAll, CheckOutMethod.AllFrozen);
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
       		  ordered.add(maskDispCmdNodeName);
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
    	  // Build node: createShdDir
    	  String createShdDirNodeName = pShotNamer.getLightingCreateShdDirNode();
    	  {
    		  EmptyFileStage stage =
    			  new EmptyFileStage(stageInfo, pContext, pClient,
    					  createShdDirNodeName);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_beauty.hip
    	  String beautyHipNodeName = pShotNamer.getLightingBeautyHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ArrayList<String> unordered = new ArrayList<String>();

    		  ordered.add(preBeautyHipNodeName);
    		  unordered.add(createShdDirNodeName);

    		  HfsBuildStage stage =
    			  new HfsBuildStage(stageInfo, pContext, pClient,
    					  beautyHipNodeName, ordered, unordered, false,
    					  null, null, null, null);

      		  addTaskAnnotation(stage, NodePurpose.Edit);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_beauty_2k.ifd
    	  String beauty2kIfdNodeName = pShotNamer.getLightingBeauty2kIfdNode();
    	  {
    		  HfsGenerateStage stage =
    			  new HfsGenerateStage(stageInfo, pContext, pClient,
    					  beauty2kIfdNodeName, pFrameRange, FRAME_PADDING,
    					  "ifd", beautyHipNodeName,
    					  "/out/beauty", null, false, null);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String beautyPreviewPythonNodeName = pShotNamer.getLightingBeautyPreviewPythonNode();
    	  {
       		  WriteFileStage stage =
    			  new WriteFileStage("CreateBeautyPreviewPython",
    					  "Creates a Python script which modifies the render resolution at run-time.",
    					  stageInfo, pContext, pClient,
    					  beautyPreviewPythonNodeName, "py", "TextFile",
    					  generateBeautyPreviewPython());

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
    		  stage.build();
    	  }

    	  // Build node: <vfxno>_beauty_2k.exr
    	  String beauty2kExrNodeName = pShotNamer.getLightingBeauty2kExrNode();
    	  {
    		  String extraOptions = "-j 0 ";
    		  extraOptions += "-P $WORKING" + beautyPreviewPythonNodeName + ".py";

    		  HfsMantraStage stage =
    			  new HfsMantraStage(stageInfo, pContext, pClient,
    					  beauty2kExrNodeName, pFrameRange, FRAME_PADDING,
    					  "exr", beauty2kIfdNodeName, 1, null, null,
    					  "Color Image", "Natural", "Full", extraOptions,
    					  "Mixed", true, "All", true, true, 1.0, null, 1.0,
    					  "Default", 4096, 10, 1024, 1.0, "0 (None)", "None");

    		  stage.addLink(new LinkMod(beautyPreviewPythonNodeName,
    			  		LinkPolicy.Dependency));

      		  addTaskAnnotation(stage, NodePurpose.Focus);
      		  stage.build();
    	  }

    	  String beauty2kJpgNodeName = pShotNamer.getLightingBeauty2kJpgNode();
    	  {
    		  HfsIConvertStage stage =
    			    new HfsIConvertStage(stageInfo, pContext, pClient,
    			    		beauty2kJpgNodeName, beauty2kExrNodeName,
    			    		pFrameRange, FRAME_PADDING, "jpg", "8-Bit (byte)");

      		  addTaskAnnotation(stage, NodePurpose.Focus);
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
    		  ordered.add(maskDispCmdNodeName);
    		  ordered.add(inkPrepCmdNodeName);

    		  unordered.add(pRorClothBmpNodeName);
    		  unordered.add(pRorPaintBmpNodeName);

    		  BuildLightingAssemblyStage stage =
    			  new BuildLightingAssemblyStage(stageInfo, pContext, pClient,
    					  preInkHipNodeName, ordered, unordered, true);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ink_const.hip
    	  String inkHipNodeName = pShotNamer.getLightingInkHipNode();
    	  {
    		  ArrayList<String> ordered = new ArrayList<String>();
    		  ordered.add(preInkHipNodeName);

    		  HfsBuildStage stage =
    			  new HfsBuildStage(stageInfo, pContext, pClient,
    					  inkHipNodeName, ordered, null, false,
    					  null, null, null, null);

      		  addTaskAnnotation(stage, NodePurpose.Edit);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ink_const_2k.ifd
    	  String ink2kIfdNodeName = pShotNamer.getLightingInk2kIfdNode();
    	  {
    		  HfsGenerateStage stage =
    			  new HfsGenerateStage(stageInfo, pContext, pClient,
    					  ink2kIfdNodeName, pFrameRange, FRAME_PADDING,
    					  "ifd", inkHipNodeName,
    					  "/out/ink_const_2k", null, false, null);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ink_const_2k.exr
    	  String ink2kExrNodeName = pShotNamer.getLightingInk2kExrNode();
    	  {
    		  HfsMantraStage stage =
    			  new HfsMantraStage(stageInfo, pContext, pClient,
    					  ink2kExrNodeName, pFrameRange, FRAME_PADDING,
    					  "exr", ink2kIfdNodeName, 1, null, null,
    					  "Color Image", "Natural", "Full", "-j 0",
    					  "Mixed", true, "All", true, true, 1.0, null, 1.0,
    					  "Default", 4096, 10, 1024, 1.0, "0 (None)", "None");

      		  addTaskAnnotation(stage, NodePurpose.Focus);
      		  stage.build();
    	  }

    	  // Build node: <vfxno>_ink_const_2k.jpg
    	  String ink2kJpgNodeName = pShotNamer.getLightingInk2kJpgNode();
    	  {
    		  HfsIConvertStage stage =
    			    new HfsIConvertStage(stageInfo, pContext, pClient,
    			    		ink2kJpgNodeName, ink2kExrNodeName,
    			    		pFrameRange, FRAME_PADDING, "jpg", "8-Bit (byte)");

      		  addTaskAnnotation(stage, NodePurpose.Focus);
      		  stage.build();
    	  }

    	  // Build submit root node
    	  String submitNodeName = pShotNamer.getLightingSubmitNode();
    	  {
    		  TreeSet<String> sources = new TreeSet<String>();

    		  sources.add(ambOcc2kJpgNodeName);
    	  	  sources.add(beauty2kJpgNodeName);
    	  	  sources.add(ink2kJpgNodeName);


    	  	  TargetStage stage =
    			    new TargetStage(stageInfo, pContext, pClient,
    					    submitNodeName, sources);
    		  addTaskAnnotation(stage, NodePurpose.Submit);

    		  stage.addLink(new LinkMod(pNoiseDispAllNodeName,
    			  		LinkPolicy.Association));
    		  stage.addLink(new LinkMod(pBackgroundPlateNodeName,
				  		LinkPolicy.Association));
    		  stage.addLink(new LinkMod(pUndistorted1kQuickTimeNodeName,
				  		LinkPolicy.Association));

    		  stage.build();
    	  }

    	  /*
    	   * APPROVE NETWORK
    	   */

    	  String approvedAmbOccNodeName = pShotNamer.getLightingApprovedAmbOccNode();
    	  {
    		  CopyImagesStage stage =
    			  new CopyImagesStage(stageInfo, pContext, pClient,
    					  approvedAmbOccNodeName, pFrameRange, 4, "exr",
    					  ambOcc2kExrNodeName);

    		  addTaskAnnotation(stage, NodePurpose.Product);
    		  stage.build();
    	  }

    	  String approvedBeautyNodeName = pShotNamer.getLightingApprovedBeautyNode();
    	  {
    		  CopyImagesStage stage =
    			  new CopyImagesStage(stageInfo, pContext, pClient,
    					  approvedBeautyNodeName, pFrameRange, 4, "exr",
    					  beauty2kExrNodeName);

    		  addTaskAnnotation(stage, NodePurpose.Product);
    		  stage.build();
    	  }

    	  String approvedInkNodeName = pShotNamer.getLightingApprovedInkNode();
    	  {
    		  CopyImagesStage stage =
    			  new CopyImagesStage(stageInfo, pContext, pClient,
    					  approvedInkNodeName, pFrameRange, 4, "exr",
    					  ink2kExrNodeName);

    		  addTaskAnnotation(stage, NodePurpose.Product);
    		  stage.build();
    	  }

    	  String approveNodeName = pShotNamer.getLightingApproveNode();
    	  {
    		  TreeSet<String> sources = new TreeSet<String>();

    		  sources.add(approvedAmbOccNodeName);
    	  	  sources.add(approvedBeautyNodeName);
    	  	  sources.add(approvedInkNodeName);

    	  	  TargetStage stage =
    			    new TargetStage(stageInfo, pContext, pClient,
    			    		approveNodeName, sources);
    		  addTaskAnnotation(stage, NodePurpose.Approve);
    		  stage.build();
    	  }

    	  /*
    	   * PREVIEW NETWORK
    	   */

    	  String ambOccSlapNkNodeName = pShotNamer.getLightingAmbOccSlapNkNode();
    	  {
    		  NukeReadStage stage =
    			  new NukeReadStage(stageInfo, pContext, pClient,
    					  ambOccSlapNkNodeName, ambOcc2kExrNodeName,
    					  "Error");

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String beautySlapNkNodeName = pShotNamer.getLightingBeautySlapNkNode();
    	  {
    		  NukeReadStage stage =
    			  new NukeReadStage(stageInfo, pContext, pClient,
    					  beautySlapNkNodeName, beauty2kExrNodeName,
    					  "Error");

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String mattesSlapNkNodeName = pShotNamer.getLightingMattesSlapNkNode();
    	  {
    		  NukeReadStage stage =
    			  new NukeReadStage(stageInfo, pContext, pClient,
    					  mattesSlapNkNodeName, pMattesApprovedImagesNodeName,
    					  "Error");

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String plateSlapNkNodeName = pShotNamer.getLightingPlateSlapNkNode();
    	  {
    		  NukeReadStage stage =
    			  new NukeReadStage(stageInfo, pContext, pClient,
    					  plateSlapNkNodeName, pBackgroundPlateNodeName,
    					  "Error");

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String redistortSlapNkNodeName = pShotNamer.getLightingRedistortSlapNkNode();
    	  {
    		  NukeReadStage stage =
    			  new NukeReadStage(stageInfo, pContext, pClient,
    					  redistortSlapNkNodeName, pRedistortUvImageNode,
    					  "Error");

    		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String slapcompNkNodeName = pShotNamer.getLightingSlapcompNkNode();
    	  {
    		  TreeMap<String, String> subst = new TreeMap<String, String>();
    		  subst.put(mattesSlapNkNodeName, "Mattes");
    		  subst.put(beautySlapNkNodeName, "Mask");
    		  subst.put(ambOccSlapNkNodeName, "AmbOcc");
    		  subst.put(plateSlapNkNodeName, "Plates");
    		  subst.put(redistortSlapNkNodeName, "Distort");

    		  NukeSubstCompStage stage =
    			    new NukeSubstCompStage
    			    (stageInfo, pContext, pClient,
    			     slapcompNkNodeName,
    			    pBaseSlapcompNukeNodeName,
    			     subst);

      		  addTaskAnnotation(stage, NodePurpose.Prepare);
      		  stage.build();
    	  }

    	  String beautyTestCineonNodeName = pShotNamer.getLightingBeautyTestCineonNode();
    	  {
    		 NukeSubstCompStage stage =
  			    new NukeSubstCompStage
  			    (stageInfo, pContext, pClient,
  			     beautyTestCineonNodeName, pFrameRange, 4, "cin",
  			     "Process", slapcompNkNodeName,
  			   new TreeMap<String,String>(), new PluginContext("NukeFrameCycler"));

      		  addTaskAnnotation(stage, NodePurpose.Edit);
      		  stage.build();
    	  }

    	  String previewSubmitNodeName = pShotNamer.getLightingPreviewSubmitNode();
    	  {
    		  TreeSet<String> sources = new TreeSet<String>();

    		  sources.add(beautyTestCineonNodeName);

    		  TargetStage stage =
    			    new TargetStage(stageInfo, pContext, pClient,
    			    		previewSubmitNodeName, sources);
    		  addTaskAnnotation(stage, NodePurpose.Submit);
    		  stage.build();
    	  }
      }
    }

    // TODO: Proper generation of these .cmd nodes. Builder shouldn't be generating these;
    // Actions should.

    private String generateAmbOccPrepCmd()
    {
    	String script = "";

    	script += "set -g START = " + pFrameRange.getStart() + "\n";
    	script += "set -g END = " + pFrameRange.getEnd() + "\n";
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

    	script += "set -g START = " + pFrameRange.getStart() + "\n";
    	script += "set -g END = " + pFrameRange.getEnd() + "\n";
    	script += "set -g SEQ = " + pShotNamer.getSequenceName() + "\n";
    	script += "set -g SHOTNUM = " + pShotNamer.getShotName() + "\n";
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

    	script += "set -g START = " + pFrameRange.getStart() + "\n";
    	script += "set -g END = " + pFrameRange.getEnd() + "\n";
    	script += "set -g SEQ = " + pShotNamer.getSequenceName() + "\n";
    	script += "set -g SHOTNUM = " + pShotNamer.getShotName() + "\n";
    	script += "opparm ink_const soho_diskfile " +
    			"( '$WORKING" + pShotNamer.getLightingInk2kIfdNode() + ".$F4.ifd' )" + "\n";
    	script += "opparm ink_const vm_picture " +
				"( '$WORKING" + pShotNamer.getLightingInk2kExrNode() + ".$F4.exr' )" + "\n";
    	script += "mwrite $WORKING" + pShotNamer.getLightingPreInkHipNode() + ".hip" + "\n";

    	return script;
    }

    private String generateBeautyPreviewPython() throws PipelineException
    {
    	String script = "";

    	// FIXME: Working area path hard-coded; next iteration, generate with actions
    	String resMel = "/prod/working/" + pContext.getAuthor() + "/"
    				+ pContext.getView() + pShotNamer.getResolutionNode() + ".mel";
    	try
    	{
    		Scanner s = new Scanner(new File(resMel));
    		s.nextLine();s.nextLine();

    		/*
    		 Lines are of this form in the resolution.mel file:
    			setAttr "defaultResolution.width" 2074;
    			setAttr "defaultResolution.height" 1576;
    		*/

    		String[] widthLine = s.nextLine().split(" ");
    		String[] heightLine = s.nextLine().split(" ");

    		Double width = (new Double(widthLine[2].split(";")[0]))*PREVIEW_SCALE;
    		Double height = (new Double(heightLine[2].split(";")[0]))*PREVIEW_SCALE;

        	script += "import sys, mantra" + "\n\n";
        	script += "def filterCamera():" + "\n";
        	script += "\t" + "mantra.setproperty('image:resolution', [" +
        				width.intValue() + ", " +
        				height.intValue() + "])" + "\n";
        	script += "\t" + "mantra.setproperty('renderer:shadingfactor', [0.25])" + "\n";

    	}
    	catch (FileNotFoundException f)
    	{
    		throw new PipelineException("File not found: " + resMel + "\n This is needed to" +
    				" build the beautyPython script.");
    	}

    	return script;
    }

    private static final long serialVersionUID = -5216068758078265109L;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4601321412376464763L;

  public final static String aBackgroundPlate = "BackgroundPlate";


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
  private String pGeoBuildNodeName;

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
  private String pRorBeautyRenderNodeName;
  private String pPreBeautyCmdNodeName;
  private String pRorMaskFluffShaderNodeName;
  private String pRorMaskShaderNodeName;
  private String pRorNeckBlendNodeName;
  private String pRorMaskColNodeName;

  private String pNoiseDisplaceNodeName;
  private String pNoiseDispAllNodeName;
  private String pUndistorted1kQuickTimeNodeName;

  private String pPreInkConstCmdNodeName;
  private String pRorInkblotRenderNodeName;
  private String pRorMaskInkShaderNodeName;

  private String pLightRigNodeName;
  private String pBackgroundPlateNodeName;

  private String pSlapcompHip;
  private String pRedistortUvImageNode;
  private String pMattesApprovedImagesNodeName;
  private String pUndistorted1kQuickTimeNode;
  private String pBaseSlapcompNukeNodeName;

  private FrameRange pFrameRange;
  private static final Integer FRAME_PADDING = 4;
  private static final Double PREVIEW_SCALE = 0.35;
}

