// $Id: MatchBuilder.java,v 1.18 2010/01/07 19:21:09 jesse Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T C H   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Match task.<P>
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
class MatchBuilder
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
  MatchBuilder
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
  MatchBuilder
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
    super("Match",
          "A builder for constructing the nodes associated with the Match task.",
          mclient, qclient, builderInfo, studioDefs,
	  projectNamer, shotNamer, TaskType.Match);

    /* setup builder parameters */
    {
      /* selects the project, sequence and shot for the task */
      addLocationParam();
    }

    /* initialize the project namer */
    initProjectNamer();

    /* initialize fields */
//    pFinalStages = new ArrayList<FinalizableStage>();

    /* create the setup passes */
    {
      addSetupPass(new MatchSetupShotEssentials());
      addSetupPass(new GetPrerequisites());
    }

    /* setup the default editors */
    setCommonDefaultEditors();

    /* create the construct passes */
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);

      ConstructPass qd = new QueueDisablePass();
      addConstructPass(qd);
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
	AdvancedLayoutGroup sub = new AdvancedLayoutGroup("GetPrerequisites", true);

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
    plugins.add(new PluginContext("CatFiles"));
    plugins.add(new PluginContext("Composite"));
    plugins.add(new PluginContext("MayaBuild"));
    plugins.add(new PluginContext("MayaMEL"));
    plugins.add(new PluginContext("MayaMakeGeoCache"));
    plugins.add(new PluginContext("MayaObjExport"));
    plugins.add(new PluginContext("MayaRender"));
    plugins.add(new PluginContext("NukeThumbnail", "Temerity",
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("MayaAttachSound"));

    MappedArrayList<String, PluginContext> toReturn =
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class MatchSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public
    MatchSetupShotEssentials()
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
	/* tracking assets */
	pTrackVerifyGlobalsNodeName = pProjectNamer.getTrackVerifyGlobalsNode();
	pRequiredNodeNames.add(pTrackVerifyGlobalsNodeName);

	/* match assets */
	pMatchPrepNodeName = pProjectNamer.getMatchPrepNode();
	pRequiredNodeNames.add(pMatchPrepNodeName);

	pMatchPrebakeNodeName = pProjectNamer.getMatchPrebakeNode();
	pRequiredNodeNames.add(pMatchPrebakeNodeName);

	/* rorschach assets */
	pConstrainRigNodeName = pProjectNamer.getConstrainRigNode();
	pRequiredNodeNames.add(pConstrainRigNodeName);

	pRorschachHiresModelNodeName = pProjectNamer.getRorschachHiresModelNode();
	pRequiredNodeNames.add(pRorschachHiresModelNodeName);

	pRorschachRigNodeName = pProjectNamer.getRorschachRigNode();
	pRequiredNodeNames.add(pRorschachRigNodeName);

	pRorschachTestShadersNodeName = pProjectNamer.getRorschachTestShadersNode();
	pRequiredNodeNames.add(pRorschachTestShadersNodeName);

	/* misc assets */
	pHideCameraPlaneNodeName = pProjectNamer.getHideCameraPlaneNode();
	pRequiredNodeNames.add(pHideCameraPlaneNodeName);
      }
    }

    private static final long serialVersionUID = -4398882102667402411L;
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
      /* soundtrack */
      pSoundtrackNodeName = pShotNamer.getSoundtrackNode();
      if(!nodeExists(pSoundtrackNodeName)) {
  	SoundBuilder builder = new SoundBuilder(pClient, pQueue, getBuilderInformation(),
  						pStudioDefs, pProjectNamer, pShotNamer);
  	addSubBuilder(builder);

  	/* map the CheckInWhenDone parameter from this builder to the Sound builder */
  	addMappedParam(builder.getName(), aCheckinWhenDone, aCheckinWhenDone);

  	/* map the ProjectName, SequenceName and ShotName parameters from this builder
  	     to the Sound builder */
        addMappedParam
          (builder.getName(),
           new ParamMapping(aLocation, StudioDefinitions.aProjectName),
           new ParamMapping(aLocation, StudioDefinitions.aProjectName));

  	addMappedParam
  	  (builder.getName(),
  	   new ParamMapping(aLocation, StudioDefinitions.aSequenceName),
  	   new ParamMapping(aLocation, StudioDefinitions.aSequenceName));

  	addMappedParam
  	  (builder.getName(),
  	   new ParamMapping(aLocation, StudioDefinitions.aShotName),
  	   new ParamMapping(aLocation, StudioDefinitions.aShotName));
      }
      pRequiredNodeNames.add(pSoundtrackNodeName);

      /* get the render resolution MEL script */
      pResolutionNodeName = pShotNamer.getResolutionNode();
      pRequiredNodeNames.add(pResolutionNodeName);

      /* extracted camera/track nodes */
      {
	pExtractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
	pRequiredNodeNames.add(pExtractedCameraNodeName);

	pExtractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
	pRequiredNodeNames.add(pExtractedTrackNodeName);

	pApprovedTrackingMarkersNodeName = pShotNamer.getApprovedTrackingMarkersNode();
	pRequiredNodeNames.add(pApprovedTrackingMarkersNodeName);
      }

      /* the background plates node */
      pUndistorted2kPlateNodeName = pShotNamer.getApprovedUndistorted2kPlateNode();
      pRequiredNodeNames.add(pUndistorted2kPlateNodeName);

      /* lookup the frame range of the shot by looking at the undistorted 2k plates node */
      {
	NodeVersion vsn = pClient.getCheckedInVersion(pUndistorted2kPlateNodeName, null);
	if(vsn == null)
	  throw new PipelineException
	    ("Somehow no checked-in version of the undistorted 2k plates node " +
	     "(" + pUndistorted2kPlateNodeName + ") exists!");

	pFrameRange = vsn.getPrimarySequence().getFrameRange();
      }
    }

    private static final long serialVersionUID = 1589975630815370641L;
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
            "Creates the nodes which make up the Match task.");
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

	String prereqNodeName = pShotNamer.getMatchPrereqNode();
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

	String matchAttachSoundtrackNodeName = pShotNamer.getMatchAttachSoundtrackNode();
 	{
 	  AttachSoundtrackStage stage =
 	    new AttachSoundtrackStage(stageInfo, pContext, pClient,
 	    			matchAttachSoundtrackNodeName, pSoundtrackNodeName);
 	  addTaskAnnotation(stage, NodePurpose.Prepare);
 	  stage.build();
 	}

	String preMatchAnimNodeName = pShotNamer.getPreMatchAnimNode();
	{
	  BuildPreMatchStage stage =
	    new BuildPreMatchStage
	    (stageInfo, pContext, pClient,
	     preMatchAnimNodeName, pConstrainRigNodeName,
	     pRorschachRigNodeName, pExtractedCameraNodeName, pExtractedTrackNodeName,
	     pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	pMatchAnimNodeName = pShotNamer.getMatchAnimNode();
	{
	  BuildMatchStage stage =
	    new BuildMatchStage(stageInfo, pContext, pClient,
				pMatchAnimNodeName, preMatchAnimNodeName,
				pApprovedTrackingMarkersNodeName, pResolutionNodeName,
				matchAttachSoundtrackNodeName,
				pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Edit);
	  stage.build();
	  addToDisableList(pMatchAnimNodeName); 
	}

	String verifyNodeName = pShotNamer.getMatchVerifyNode();
	{
	  BuildMatchVerifyStage stage =
	    new BuildMatchVerifyStage
	    (stageInfo, pContext, pClient,
	     verifyNodeName, pMatchAnimNodeName, pRorschachTestShadersNodeName,
	     pMatchPrepNodeName, pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String matchPreRenderScriptNodeName = pShotNamer.getMatchPreRenderScriptNode();
	{
	  LinkedList<String> sources = new LinkedList<String>();
	  sources.add(pHideCameraPlaneNodeName);
	  sources.add(pTrackVerifyGlobalsNodeName);
	  sources.add(pResolutionNodeName);

	  CatScriptStage stage =
	    new CatScriptStage(stageInfo, pContext, pClient,
			       matchPreRenderScriptNodeName, "mel", sources);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String verifyImagesRedNodeName = pShotNamer.getMatchVerifyRedImagesNode();
	{
	  RenderTaskVerifyStage stage =
	    new RenderTaskVerifyStage
	    (stageInfo, pContext, pClient,
	    		verifyImagesRedNodeName, pFrameRange, "tif", verifyNodeName,
	     "match:prep:cam:camera01", matchPreRenderScriptNodeName, "red");
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
	}

	String verifyCompNodeName = pShotNamer.getMatchVerifyCompNode();
	{
	  BashCompStage stage =
	    new BashCompStage(stageInfo, pContext, pClient,
			      verifyCompNodeName, pFrameRange,
			      verifyImagesRedNodeName, pUndistorted2kPlateNodeName);
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
	}

	String verifyImagesGrayNodeName = pShotNamer.getMatchVerifyGrayImagesNode();
	{
	  RenderTaskVerifyStage stage =
	    new RenderTaskVerifyStage
	    (stageInfo, pContext, pClient,
	    		verifyImagesGrayNodeName, pFrameRange, "tif", verifyNodeName,
	     "match:prep:cam:camera01", matchPreRenderScriptNodeName, "gray");
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
	}

	String grayCompNodeName = pShotNamer.getMatchGrayCompNode();
	{
	  BashCompStage stage =
	    new BashCompStage(stageInfo, pContext, pClient,
	    		grayCompNodeName, pFrameRange,
	    		verifyImagesGrayNodeName, pUndistorted2kPlateNodeName);
	  addTaskAnnotation(stage, NodePurpose.Focus);
	  stage.build();
	}

	String verifyThumbNodeName = pShotNamer.getMatchVerifyThumbNode();
	{
	  NukeThumbnailStage stage =
	    new NukeThumbnailStage
	      (stageInfo, pContext, pClient,
	       verifyThumbNodeName, "tif", verifyCompNodeName,
	       pFrameRange.getStart(), 150, 1.0, true, true, new Color3d());
	  addTaskAnnotation(stage, NodePurpose.Thumbnail);
	  stage.build();
	}

	String submitNodeName = pShotNamer.getMatchSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(verifyThumbNodeName);
	  sources.add(grayCompNodeName);

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
	String matchPrebakeSceneNodeName = pShotNamer.getMatchPrebakeSceneNode();
	{
	  BuildMatchPrebakeStage stage =
	    new BuildMatchPrebakeStage(stageInfo, pContext, pClient,
				       matchPrebakeSceneNodeName,
				       pMatchAnimNodeName, pRorschachHiresModelNodeName,
				       pMatchPrebakeNodeName, pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Prepare);
	  stage.build();
	}

	String matchGeoCacheNodeName = pShotNamer.getMatchGeoCacheNode();
	{
	  MatchGeoCacheStage stage =
	    new MatchGeoCacheStage(stageInfo, pContext, pClient,
				   matchGeoCacheNodeName, matchPrebakeSceneNodeName,
                                   "rorHead_GEOShape");
	  addTaskAnnotation(stage, NodePurpose.Product);
	  stage.build();
	}

	String matchMaskGeoNodeName = pShotNamer.getMatchMaskGeoNode();
	{
	  MatchMaskGeoStage stage =
	    new MatchMaskGeoStage(stageInfo, pContext, pClient,
				  matchMaskGeoNodeName, matchPrebakeSceneNodeName,
                                  "ror_GEO", pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Product);
	  stage.build();
	}

 	String approveNodeName = pShotNamer.getMatchApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
 	  sources.add(matchGeoCacheNodeName);
 	  sources.add(matchMaskGeoNodeName);

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

    private static final long serialVersionUID = 1327196062674500407L;
  }


  /*----------------------------------------------------------------------------------------*/

  protected 
  class QueueDisablePass
    extends ConstructPass
  {
    public 
    QueueDisablePass() 
    {
      super("Queue and Disable Actions", 
	    "");
    }
    
    /**
     * Return nodes which will have their actions disabled to be queued now.
     */ 
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      return new LinkedList<String>(getDisableList());
    }
    
    /**
     * Disable the actions for the second pass nodes. 
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      disableActions();
    }

    private static final long serialVersionUID = 6063704518254978295L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 881301831541481949L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking verification test renders.
   */
  private String pTrackVerifyGlobalsNodeName;

  /**
   * Returns the fully resolved name of the node containing the approved copy of the
   * 2D tracking data exported from PFTrack.
   */
  private String pApprovedTrackingMarkersNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to
   * attach shaders and verify the match test render Maya scene.
   */
  private String pMatchPrepNodeName;

  /**
   * The fully resolved name of the node containing the MEL script which transfers
   * animation from a rigged head to the clean non-rigged version.
   */
  private String pMatchPrebakeNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script used to add head
   * and neck constraints to the match rig.
   */
  private String pConstrainRigNodeName;

  /**
   * The fully resolved name of the node containing a Maya scene which provides a
   * clean unrigged model.
   */
  private String pRorschachHiresModelNodeName;

  /**
   * The fully resolved name of the node containing the match rig Maya scene.
   */
  private String pRorschachRigNodeName;

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the tracking verification test renders.
   */
  private String pRorschachTestShadersNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script to hide all camera
   * image planes from view before rendering.
   */
  private String pHideCameraPlaneNodeName;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script used to set
   * render resolutions which match that of the undistorted plates.
   */
  private String pResolutionNodeName;

  /**
   * The fully resolved name of the node containing the extracted world space camera
   * with all tracking animation baked.
   */
  private String pExtractedCameraNodeName;

  /**
   * The fully resolved name of the node containing the extracted world space
   * locators with all tracking animation baked.
   */
  private String pExtractedTrackNodeName;

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */
  private String pUndistorted2kPlateNodeName;

  /**
   * The frame range of the shot.
   */
  private FrameRange pFrameRange;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node for the Maya scene used to perform the
   * final head and facial matching animation.
   */
  private String pMatchAnimNodeName;
  private String pSoundtrackNodeName;



  private ArrayList<FinalizableStage> pFinalStages;

}
