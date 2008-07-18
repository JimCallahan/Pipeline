// $Id: NoiseBuilder.java,v 1.11 2008/08/01 20:19:14 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O I S E   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Noise task.<P>
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
 * </DIV>
 */
public
class NoiseBuilder
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
  NoiseBuilder
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
  NoiseBuilder
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
    super("Noise",
          "A builder for constructing the nodes associated with the Noise task.",
          mclient, qclient, builderInfo, studioDefs,
	  projectNamer, shotNamer, TaskType.Noise);

    /* setup builder parameters */
    {
      /* selects the project, sequence and shot for the task */
      addLocationParam();

      /* the background plate images */
      {
        UtilityParam param =
          new PlaceholderUtilityParam
          (aBackgroundPlate,
           "Select the existing scanned images node to use as the background plates for " +
           "this shot.");
        addParam(param);
      }
    }

    /* initialize the project namer */
    initProjectNamer();

    /* create the setup passes */
    {
      addSetupPass(new NoiseSetupShotEssentials());
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
    plugins.add(new PluginContext("Touch"));
    plugins.add(new PluginContext("Copy"));
    plugins.add(new PluginContext("MayaBuild"));
    plugins.add(new PluginContext("MayaAttachGeoCache", "Temerity",
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("MayaFTNBuild"));
    plugins.add(new PluginContext("MayaRender"));
    plugins.add(new PluginContext("NukeSubstComp", "Temerity",
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("NukeThumbnail", "Temerity",
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("HfsReadCmd"));
    plugins.add(new PluginContext("HfsComposite", "Temerity",
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("DjvUnixQt"));
    plugins.add(new PluginContext("HfsIConvert"));

    MappedArrayList<String, PluginContext> toReturn =
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class NoiseSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public
    NoiseSetupShotEssentials()
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
	/* noise assets */
	pAddNoiseNukeNodeName = pProjectNamer.getAddNoiseNukeNode();
	pRequiredNodeNames.add(pAddNoiseNukeNodeName);

	pNoiseDisplaceHipNodeName = pProjectNamer.getNoiseDisplaceHipNode();
	pRequiredNodeNames.add(pNoiseDisplaceHipNodeName);

        /* blot assets */
        pBlotAttachPreviewNodeName = pProjectNamer.getBlotAttachPreviewNode();
	pRequiredNodeNames.add(pBlotAttachPreviewNodeName);

        pBlotTestGlobalsNodeName = pProjectNamer.getBlotTestGlobalsNode();
	pRequiredNodeNames.add(pBlotTestGlobalsNodeName);

	/* rorschach assets */
	pRorschachHiresModelNodeName = pProjectNamer.getRorschachHiresModelNode();
	pRequiredNodeNames.add(pRorschachHiresModelNodeName);

        pRorschachPreviewShadersNodeName = pProjectNamer.getRorschachPreviewShadersNode();
	pRequiredNodeNames.add(pRorschachPreviewShadersNodeName);

	/* misc assets */
	pHideCameraPlaneNodeName = pProjectNamer.getHideCameraPlaneNode();
	pRequiredNodeNames.add(pHideCameraPlaneNodeName);

	pSetFiletexSeqNodeName = pProjectNamer.getSetFiletexSeqNode();
	pRequiredNodeNames.add(pSetFiletexSeqNodeName);

	pHalfResRenderNodeName = pProjectNamer.getHalfResRenderNode();
	pRequiredNodeNames.add(pHalfResRenderNodeName);

	pTestCompNukeNodeName = pProjectNamer.getTestCompNukeNode();
	pRequiredNodeNames.add(pTestCompNukeNodeName);
      }
    }

    private static final long serialVersionUID = -8860185784531713731L;
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

    private static final long serialVersionUID = 3897345314400900933L;
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
      {
        String bgName = (String) getParamValue(aBackgroundPlate);
        if(bgName == null)
          throw new PipelineException
            ("No " + aBackgroundPlate + " image node was selected!");
        Path path = new Path(pShotNamer.getPlatesScannedParentPath(), bgName);

	try {
	  NodeVersion vsn = pClient.getCheckedInVersion(path.toString(), null);
	  pFrameRange = vsn.getPrimarySequence().getFrameRange();
	  pBackgroundPlateNodeName = vsn.getName();
	  pRequiredNodeNames.add(pBackgroundPlateNodeName);
	}
	catch(PipelineException ex) {
	  throw new PipelineException
	    ("Somehow no checked-in version of the " + aBackgroundPlate + " node " +
	     "(" + path + ") exists!");
	}
      }

      /* the background undistored 1k plates node */
      pUndistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode();
      pRequiredNodeNames.add(pUndistorted1kPlateNodeName);

      /* redistorted UV image */
      pRedistortUvImageNodeName = pShotNamer.getRedistortUvImageNode();
      pRequiredNodeNames.add(pRedistortUvImageNodeName);

      /* get the render resolution MEL script */
      pResolutionNodeName = pShotNamer.getResolutionNode();
      pRequiredNodeNames.add(pResolutionNodeName);

      /* blot textures */
      pBlotApprovedTexturesNodeName = pShotNamer.getBlotApprovedTexturesNode();
      pRequiredNodeNames.add(pBlotApprovedTexturesNodeName);

      /* the geometry cache */
      pMatchGeoCacheNodeName = pShotNamer.getMatchGeoCacheNode();
      pRequiredNodeNames.add(pMatchGeoCacheNodeName);

      /* extracted camera */
      pExtractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
      pRequiredNodeNames.add(pExtractedCameraNodeName);

      /* matte images */
      pMattesImagesNodeName = pShotNamer.getMattesImagesNode();
      pRequiredNodeNames.add(pMattesImagesNodeName);
    }

    private static final long serialVersionUID = 5628655760654846839L;
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
            "Creates the nodes which make up the Noise task.");
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

	String prereqNodeName = pShotNamer.getNoisePrereqNode();
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

      /* the submit network */
      {
	String blotAnimNukeNodeName = pShotNamer.getBlotAnimNukeNode();
	{
	  NukeReadStage stage =
	    new NukeReadStage(stageInfo, pContext, pClient,
			      blotAnimNukeNodeName, pBlotApprovedTexturesNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	pNoiseTexturesNodeName = pShotNamer.getNoiseTexturesNode();
	{
	  AddNoiseStage stage =
	    new AddNoiseStage
	    (stageInfo, pContext, pClient,
	     pNoiseTexturesNodeName, pAddNoiseNukeNodeName, blotAnimNukeNodeName,
	     pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
	}

	String noiseThumbNodeName = pShotNamer.getNoiseThumbNode();
	{
	  NukeThumbnailStage stage =
	    new NukeThumbnailStage
	      (stageInfo, pContext, pClient,
	       noiseThumbNodeName, "tif", pNoiseTexturesNodeName,
	       pFrameRange.getStart(), 150, 1.0, true, true, new Color3d());
	  addTaskAnnotation(stage, NodePurpose.Thumbnail);
	  stage.build();
	}

	String noiseTextureCmdNodeName = pShotNamer.getNoiseTextureCmdNode();
	{
	  HfsReadCmdStage stage =
	    new HfsReadCmdStage(stageInfo, pContext, pClient,
				noiseTextureCmdNodeName, pNoiseTexturesNodeName,
				"/img/inkblot/noise_in", "filename");
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	pNoiseDisplaceNodeName = pShotNamer.getNoiseDisplaceNode();
	{
	  HfsRatCompositeStage stage =
	    new HfsRatCompositeStage(stageInfo, pContext, pClient,
				  pNoiseDisplaceNodeName, pFrameRange, 4,
				  pNoiseDisplaceHipNodeName, "/out/disp", false,
				  noiseTextureCmdNodeName, null, null, null);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseDisplaceThumbNodeName = pShotNamer.getNoiseDisplaceThumbNode();
	{
	  HfsThumbnailStage stage =
	    new HfsThumbnailStage
	      (stageInfo, pContext, pClient,
	       noiseDisplaceThumbNodeName, "tif", pNoiseDisplaceNodeName,
	       pFrameRange.getStart(), 150, 1.0, true, true, new Color3d());
	  addTaskAnnotation(stage, NodePurpose.Thumbnail);
	  stage.build();
	}

	String noiseDisplaceAllNodeName = pShotNamer.getNoiseDisplaceAllNode();
	{
		HfsIConvertStage stage =
	    new HfsIConvertStage(stageInfo, pContext, pClient,
	    		noiseDisplaceAllNodeName, pNoiseDisplaceNodeName,
	    		pFrameRange, 4, "jpg", "Natural");
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

 	String noiseTextureSceneNodeName = pShotNamer.getNoiseTextureSceneNode();
 	{
	  MayaFTNBuildStage stage =
	    new MayaFTNBuildStage(stageInfo, pContext, pClient,
				  new MayaContext(), noiseTextureSceneNodeName, true);
	  stage.addLink(new LinkMod(noiseDisplaceAllNodeName, LinkPolicy.Dependency));
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseAttachCacheNodeName = pShotNamer.getNoiseAttachCacheNode();
	{
	  AttachGeoCacheStage stage =
	    new AttachGeoCacheStage(stageInfo, pContext, pClient,
				    noiseAttachCacheNodeName, pMatchGeoCacheNodeName,
				    "rorHead_GEOShape", "mdl:rorHead_GEOShape",
				    pBlotAttachPreviewNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

 	String noiseTestSceneNodeName = pShotNamer.getNoiseTestSceneNode();
 	{
	  BuildBlotTestStage stage =
	    new BuildBlotTestStage
	      (stageInfo, pContext, pClient,
	       noiseTestSceneNodeName, pRorschachHiresModelNodeName,
	       pRorschachPreviewShadersNodeName, noiseTextureSceneNodeName,
	       pExtractedCameraNodeName, pUndistorted1kPlateNodeName,
	       noiseAttachCacheNodeName, pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
 	}

	String noiseTestPrepNodeName = pShotNamer.getNoiseTestPrepNode();
	{
	  LinkedList<String> sources = new LinkedList<String>();
	  sources.add(pHideCameraPlaneNodeName);
	  sources.add(pSetFiletexSeqNodeName);
	  sources.add(pBlotTestGlobalsNodeName);
	  sources.add(pResolutionNodeName);
	  sources.add(pHalfResRenderNodeName);

	  CatScriptStage stage =
	    new CatScriptStage(stageInfo, pContext, pClient,
			       noiseTestPrepNodeName, "mel", sources);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

 	String noiseTestImagesNodeName = pShotNamer.getNoiseTestImagesNode();
 	{
	  RenderTaskVerifyStage stage =
	    new RenderTaskVerifyStage
	      (stageInfo, pContext, pClient,
	       noiseTestImagesNodeName, pFrameRange, noiseTestSceneNodeName,
	       "cam:camera01", noiseTestPrepNodeName);
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
 	}

	String noiseTestThumbNodeName = pShotNamer.getNoiseTestThumbNode();
	{
	  NukeThumbnailStage stage =
	    new NukeThumbnailStage
	      (stageInfo, pContext, pClient,
	       noiseTestThumbNodeName, "tif", noiseTestImagesNodeName,
	       pFrameRange.getStart(), 150, 1.0, true, true, new Color3d());
	  addTaskAnnotation(stage, NodePurpose.Thumbnail);
	  stage.build();
	}

	String noiseReadRedistortNodeName = pShotNamer.getNoiseReadRedistortNode();
	{
	  NukeReadStage stage =
	    new NukeReadStage(stageInfo, pContext, pClient,
			      noiseReadRedistortNodeName, pRedistortUvImageNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseReadPlatesNodeName = pShotNamer.getNoiseReadPlatesNode();
	{
	  NukeReadStage stage =
	    new NukeReadStage(stageInfo, pContext, pClient,
			      noiseReadPlatesNodeName, pBackgroundPlateNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseReadTestImagesNodeName = pShotNamer.getNoiseReadTestImagesNode();
	{
	  NukeReadStage stage =
	    new NukeReadStage(stageInfo, pContext, pClient,
			      noiseReadTestImagesNodeName, noiseTestImagesNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseReadMattesNodeName = pShotNamer.getNoiseReadMattesNode();
	{
	  NukeReadStage stage =
	    new NukeReadStage(stageInfo, pContext, pClient,
			      noiseReadMattesNodeName, pMattesImagesNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String noiseTestCompImagesNodeName = pShotNamer.getNoiseTestCompImagesNode();
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>();
	  subst.put(noiseReadRedistortNodeName, "RD_UV_Map");
	  subst.put(noiseReadPlatesNodeName, "Plate");
	  subst.put(noiseReadTestImagesNodeName, "TestRender");
	  subst.put(noiseReadMattesNodeName, "Mattes");

	  NukeSubstCompStage stage =
	    new NukeSubstCompStage
	      (stageInfo, pContext, pClient,
	       noiseTestCompImagesNodeName, pFrameRange, 4, "jpg",
	       "Append & Process", pTestCompNukeNodeName, subst);
 	  addTaskAnnotation(stage, NodePurpose.Focus);
 	  stage.build();
	}

	String noiseTestCompThumbNodeName = pShotNamer.getNoiseTestCompThumbNode();
	{
	  NukeThumbnailStage stage =
	    new NukeThumbnailStage
	      (stageInfo, pContext, pClient,
	       noiseTestCompThumbNodeName, "tif", noiseTestCompImagesNodeName,
	       pFrameRange.getStart(), 150, 1.0, true, true, new Color3d());
	  addTaskAnnotation(stage, NodePurpose.Thumbnail);
	  stage.build();
	}

	String submitNodeName = pShotNamer.getNoiseSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(noiseThumbNodeName);
	  sources.add(noiseDisplaceThumbNodeName);
	  sources.add(noiseTestThumbNodeName);
	  sources.add(noiseTestCompThumbNodeName);

	  TargetStage stage =
	    new TargetStage(stageInfo, pContext, pClient,
			    submitNodeName, sources);
	  addTaskAnnotation(stage, NodePurpose.Submit);
	  stage.build();
	  addToQueueList(submitNodeName);
	  addToCheckInList(submitNodeName);
	}
      }

      /* the approve network */
      {
	String noiseApprovedTexturesNodeName = pShotNamer.getNoiseApprovedTexturesNode();
	{
	  CopyImagesStage stage =
	    new CopyImagesStage
	      (stageInfo, pContext, pClient,
	       noiseApprovedTexturesNodeName, pFrameRange, 4, "tif",
	       pNoiseTexturesNodeName);
	  addTaskAnnotation(stage, NodePurpose.Product);
	  stage.build();
	}

	String noiseApprovedDisplaceNodeName = pShotNamer.getNoiseApprovedDisplaceNode();
	{
	  CopyImagesStage stage =
	    new CopyImagesStage
	      (stageInfo, pContext, pClient,
	       noiseApprovedDisplaceNodeName, pFrameRange, 4, "rat",
	       pNoiseDisplaceNodeName);
	  addTaskAnnotation(stage, NodePurpose.Product);
	  stage.build();
	}

 	String approveNodeName = pShotNamer.getNoiseApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
 	  sources.add(noiseApprovedTexturesNodeName);
 	  sources.add(noiseApprovedDisplaceNodeName);

 	  TargetStage stage =
 	    new TargetStage(stageInfo, pContext, pClient,
 			    approveNodeName, sources);
 	  addTaskAnnotation(stage, NodePurpose.Approve);
 	  stage.build();
 	  addToQueueList(approveNodeName);
 	  addToCheckInList(approveNodeName);
 	}
      }
    }

    private static final long serialVersionUID = 8483012262557814886L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3788746046365936548L;

  public final static String aBackgroundPlate = "BackgroundPlate";


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the Nuke script used to noise
   * up the blot textures.
   */
  private String pAddNoiseNukeNodeName;

  /**
   * The fully resolved name of the node containing the Houdini scene used to
   * generate the noise displacement textures.
   */
  private String pNoiseDisplaceHipNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to
   * attach the blot textures and shaders in the animation test render scene.
   */
  private String pBlotAttachPreviewNodeName;

  /**
   * The fully resolved name of the rendered noised textures node.
   */
  private String pNoiseTexturesNodeName;

  /**
   * Returns the fully resolved name of the composited displace A textures.
   */
  private String pNoiseDisplaceNodeName;

  /**
   * The fully resolved name of the node containing the Maya render globals
   * settings for blot test renders.
   */
  private String pBlotTestGlobalsNodeName;

  /**
   * The fully resolved name of the rendered blot textures node.
   */
  private String pBlotApprovedTexturesNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a Maya scene which provides a
   * clean unrigged model.
   */
  private String pRorschachHiresModelNodeName;

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the blot animation verification test renders.
   */
  private String pRorschachPreviewShadersNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script to hide all camera
   * image planes from view before rendering.
   */
  private String pHideCameraPlaneNodeName;

  /**
   * The fully resolved name of the node containing a MEL script to key the file
   * texture sequence for animated textures.
   */
  private String pSetFiletexSeqNodeName;

  /**
   * The fully resolved name of the node containing a MEL script to half the
   * currently set renderresolution.
   */
  private String pHalfResRenderNodeName;

  /**
   * The fully resolved name of the node containing the master Nuke script used
   * to perform test comps.
   */
  private String pTestCompNukeNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script used to set
   * render resolutions which match that of the undistorted plates.
   */
  private String pResolutionNodeName;

  /**
   * The fully resolved name of the node containing the baked Maya geometry cache.
   */
  private String pMatchGeoCacheNodeName;

  /**
   * The fully resolved name of the node containing the extracted world space camera
   * with all tracking animation baked.
   */
  private String pExtractedCameraNodeName;

  /**
   * The fully resolved name and frame range of background plates node.
   */
  private String pBackgroundPlateNodeName;

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~1k plate images.
   */
  private String pUndistorted1kPlateNodeName;

  /**
   * The frame range of the shot.
   */
  private FrameRange pFrameRange;

  /**
   * The fully resolved name of the node containing the generated RGB channel
   * encoded matte images.
   */
  private String pMattesImagesNodeName;

  /**
   * The fully resolved name of the node containing redistorted UV image rendered
   * by Houdini.
   */
  private String pRedistortUvImageNodeName;

}
