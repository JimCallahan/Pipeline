// $Id: TrackingBuilder.java,v 1.15 2008/04/09 20:16:18 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T R A C K I N G   B U I L D E R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Tracking task.<P> 
 * 
 * Primarily used to validate the a Maya scene containing the tracked camera and head model.
 * The validation process includes performing MEL based tests on the scene as well as 
 * rendering/comping the shot using a rig and shaders designed to show flaws in the tracking 
 * data. <P> 
 * 
 * This task relies on the existence of a Maya scene containing the camera/model tracking 
 * data in the following location: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   /projects/wtm/shots/SEQ/SHOT/tracking/edit/SEQSHOT_track.ma
 * </DIV>
 * 
 * Where SEQ is the two character sequence name and SHOT is the 3 digit shot number.  This
 * file can be created one of two ways.  It can either have been created by a tracking 
 * outsourcing studio and manually registered and checked-in or it can be built before hand 
 * using the InternalTracking builder.  This builder will fail if this Maya scene doesn not 
 * already exist with the correct name and location before the builder is run.<P> 
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
 *   </DIV> <P>
 * 
 *   Temp Render <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to add nodes require to create the temp render using a static inkblot texture
 *     and the tracking data.
 *   </DIV> <BR>
 * </DIV> 
 */
public 
class TrackingBuilder 
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
  TrackingBuilder
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
  TrackingBuilder
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
    super("Tracking",
          "A builder for constructing the nodes associated with the Tracking task.", 
          mclient, qclient, builderInfo, studioDefs, 
	  projectNamer, shotNamer, TaskType.Tracking);  

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 

      /* optional temp render pass */ 
      {
	UtilityParam param = 
	  new BooleanUtilityParam
	    (aTempRender, 
	     "Whether to add nodes require to create the temp render using a static " + 
	     "inkblot texture and the tracking data.",
	     false);	   
        addParam(param);
      }
    }

    /* initialize the project namer */ 
    initProjectNamer(); 

    /* initialize fields */ 
    pFinalStages = new ArrayList<FinalizableStage>(); 

    /* create the setup passes */ 
    addSetupPass(new TrackingSetupShotEssentials());
    addSetupPass(new SetupTrackingEssentials()); 
    
    /* setup the default editors */ 
    setCommonDefaultEditors(); 

    /* create the construct passes */ 
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);
      
      ConstructPass qdc = new QueueDisableCleanupPass(); 
      addConstructPass(qdc); 
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
        sub.addEntry(1, null);
        sub.addEntry(1, aTempRender);

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
    //plugins.add(new PluginContext("PFTrackBuild", "ICVFX"));		
    plugins.add(new PluginContext("Copy")); 		
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("CatFiles"));  
    plugins.add(new PluginContext("Composite"));   		
    plugins.add(new PluginContext("MayaBuild"));  		
    plugins.add(new PluginContext("MayaRender")); 		
    plugins.add(new PluginContext("NukeThumbnail"));		

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class TrackingSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    TrackingSetupShotEssentials()
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
	pTrackPrepNodeName = pProjectNamer.getTrackPrepNode(); 
	pRequiredNodeNames.add(pTrackPrepNodeName); 

	pTrackVerifyGlobalsNodeName = pProjectNamer.getTrackVerifyGlobalsNode(); 
	pRequiredNodeNames.add(pTrackVerifyGlobalsNodeName); 

	pTrackExtractCameraNodeName = pProjectNamer.getTrackExtractCameraNode(); 
	pRequiredNodeNames.add(pTrackExtractCameraNodeName); 

	pTrackExtractTrackingNodeName = pProjectNamer.getTrackExtractTrackingNode(); 
	pRequiredNodeNames.add(pTrackExtractTrackingNodeName); 

	/* rorschach assets */ 
        pRorschachTrackPlaceholderNodeName = 
          pProjectNamer.getRorschachTrackPlaceholderNode(); 
        pRequiredNodeNames.add(pRorschachTrackPlaceholderNodeName); 

	pRorschachHatRigNodeName = pProjectNamer.getRorschachHatRigNode();
	pRequiredNodeNames.add(pRorschachHatRigNodeName); 
	
	pRorschachVerifyModelNodeName = pProjectNamer.getRorschachVerifyModelNode(); 
	pRequiredNodeNames.add(pRorschachVerifyModelNodeName); 

	pRorschachTestShadersNodeName = pProjectNamer.getRorschachTestShadersNode(); 
	pRequiredNodeNames.add(pRorschachTestShadersNodeName); 

	/* misc assets */ 
	pHideCameraPlaneNodeName = pProjectNamer.getHideCameraPlaneNode(); 
	pRequiredNodeNames.add(pHideCameraPlaneNodeName); 
      }

      /* are we doing temp renders? */ 
      {
	Boolean tf = (Boolean) getParamValue(aTempRender);
	pDoTempRender = ((tf != null) && tf);
      }

      /* optional temp render related prerequisites */ 
      if(pDoTempRender) {
	pTrackTempPrepNodeName = pProjectNamer.getTrackTempPrepNode(); 
	pRequiredNodeNames.add(pTrackTempPrepNodeName); 

	pRorschachTempModelNodeName = pProjectNamer.getRorschachTempModelNode(); 
	pRequiredNodeNames.add(pRorschachTempModelNodeName); 

	pRorschachTempTextureNodeName = pProjectNamer.getRorschachTempTextureNode(); 
	pRequiredNodeNames.add(pRorschachTempTextureNodeName); 

	pTrackTempGlobalsNodeName = pProjectNamer.getTrackTempGlobalsNode(); 
	pRequiredNodeNames.add(pTrackTempGlobalsNodeName); 
      }

    }
    
    private static final long serialVersionUID = -7271559542044510237L;
  }


  /*----------------------------------------------------------------------------------------*/
    
  protected
  class SetupTrackingEssentials
    extends SetupPass
  {
    public 
    SetupTrackingEssentials()
    {
      super("Setup Tracking Essentials", 
	    "Lookup the names of nodes required by the tracking task."); 
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
      /* get the 1k/2k plates */ 
      pUndistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
      pRequiredNodeNames.add(pUndistorted1kPlateNodeName); 

      pUndistorted2kPlateNodeName = pShotNamer.getApprovedUndistorted2kPlateNode(); 
      pRequiredNodeNames.add(pUndistorted2kPlateNodeName); 

      /* get the render resolution MEL script */ 
      pResolutionNodeName = pShotNamer.getResolutionNode(); 
      pRequiredNodeNames.add(pResolutionNodeName); 
      
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
    
    private static final long serialVersionUID = 3499087100113059674L; 
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
            "Creates the nodes which make up the Tracking task."); 
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

	String prereqNodeName = pShotNamer.getTrackingPrereqNode();
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
	pTrackNodeName = pShotNamer.getTrackNode(); 
	if(nodeExists(pTrackNodeName)) {
	  lockLatest(pTrackNodeName); 
	  addTaskAnnotation(pTrackNodeName, NodePurpose.Edit); 
	}
	else {
	  PlaceholderMayaSceneStage stage = 
	    new PlaceholderMayaSceneStage
	      (stageInfo, pContext, pClient, 
	       pTrackNodeName, pRorschachTrackPlaceholderNodeName);
	  stage.addLink(new LinkMod(pUndistorted1kPlateNodeName, 
				    LinkPolicy.Association, LinkRelationship.None, null));
	  stage.addLink(new LinkMod(pRorschachHatRigNodeName,  
				    LinkPolicy.Association, LinkRelationship.None, null));
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  pFinalStages.add(stage);
	}

	String verifyNodeName = pShotNamer.getTrackingVerifyNode(); 
	{
	  BuildTrackingVerifyStage stage = 
	    new BuildTrackingVerifyStage
	    (stageInfo, pContext, pClient, 
	     verifyNodeName, pTrackNodeName, pRorschachVerifyModelNodeName, 
	     pRorschachTestShadersNodeName, pTrackPrepNodeName, pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String trackingPreRenderScriptNodeName = pShotNamer.getTrackingPreRenderScriptNode();
	{
	  LinkedList<String> sources = new LinkedList<String>(); 
	  sources.add(pHideCameraPlaneNodeName); 
	  sources.add(pTrackVerifyGlobalsNodeName); 
	  sources.add(pResolutionNodeName); 

	  CatScriptStage stage = 
	    new CatScriptStage(stageInfo, pContext, pClient, 
			       trackingPreRenderScriptNodeName, "mel", sources);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String verifyImagesNodeName = pShotNamer.getTrackingVerifyImagesNode(); 
	{
	  RenderTaskVerifyStage stage = 
	    new RenderTaskVerifyStage
	    (stageInfo, pContext, pClient, 
	     verifyImagesNodeName, pFrameRange, verifyNodeName, 
	     "track:camera01", trackingPreRenderScriptNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
	}

	String verifyCompNodeName = pShotNamer.getTrackingVerifyCompNode(); 
	{
	  BashCompStage stage = 
	    new BashCompStage(stageInfo, pContext, pClient, 
			      verifyCompNodeName, pFrameRange, 
			      verifyImagesNodeName, pUndistorted2kPlateNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 	 
	  stage.build(); 
	}

	String verifyThumbNodeName = pShotNamer.getTrackingVerifyThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   verifyThumbNodeName, "tif", verifyCompNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	pTrackingMarkersNodeName = pShotNamer.getTrackingMarkersNode();
	{
	  TouchStage stage = 
	    new TouchStage("TrackingMarkers", "Create an empty tracking markers file.", 
			   stageInfo, pContext, pClient, 
			   pTrackingMarkersNodeName, "2dt", new TreeSet<String>());
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  addToDisableList(pTrackingMarkersNodeName); 
	}

	String submitNodeName = pShotNamer.getTrackingSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(verifyThumbNodeName); 
	  sources.add(pTrackingMarkersNodeName); 

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
	String extractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
	{
	  BuildTrackingExtractStage stage = 
	    new BuildTrackingExtractStage
	    (stageInfo, pContext, pClient, 
	     extractedCameraNodeName, pTrackNodeName, pTrackExtractCameraNodeName, 
	     pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String extractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
	{
	  BuildTrackingExtractStage stage = 
	    new BuildTrackingExtractStage
	    (stageInfo, pContext, pClient, 
	     extractedTrackNodeName, pTrackNodeName, pTrackExtractTrackingNodeName, 
	     pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approvedTrackingMarkersNodeName = pShotNamer.getApprovedTrackingMarkersNode();
	{
	  CopyTrackingMarkersStage stage = 
	    new CopyTrackingMarkersStage
	      (stageInfo, pContext, pClient, 
	       approvedTrackingMarkersNodeName, pTrackingMarkersNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	if(pDoTempRender) {
	  String trackingTempTextureNodeName = pShotNamer.getTrackingTempTextureNode();
	  {
	    MayaFTNBuildStage stage = 
	      new MayaFTNBuildStage(stageInfo, pContext, pClient, 
				    new MayaContext(), trackingTempTextureNodeName, true);
	    stage.addLink(new LinkMod(pRorschachTempTextureNodeName, LinkPolicy.Dependency)); 
	    addTaskAnnotation(stage, NodePurpose.Prepare); 
	    stage.build();  
	  }
 
	  String trackingTempRenderMayaNodeName = pShotNamer.getTrackingTempRenderMayaNode();
	  {
	    BuildTestRenderStage stage = 
	      new BuildTestRenderStage
	        (stageInfo, pContext, pClient, 
		 trackingTempRenderMayaNodeName, pRorschachTempModelNodeName, pTrackNodeName, 
		 trackingTempTextureNodeName, pTrackTempPrepNodeName, pFrameRange); 
	    addTaskAnnotation(stage, NodePurpose.Prepare); 
	    stage.build();  
	  }
	  
	  String trackingTempRenderNodeName = pShotNamer.getTrackingTempRenderNode();
	  {
	    LinkedList<String> sources = new LinkedList<String>();
	    sources.add(pTrackTempGlobalsNodeName); 
	    sources.add(pResolutionNodeName); 

	    CatScriptStage stage = 
	      new CatScriptStage(stageInfo, pContext, pClient, 
				 trackingTempRenderNodeName, "mel", sources);
	    addTaskAnnotation(stage, NodePurpose.Prepare); 
	    stage.build();  
	  }

	  pTrackingInkblotNodeName = pShotNamer.getTrackingInkblotNode(); 
	  {
	    RenderTaskVerifyStage stage = 
	      new RenderTaskVerifyStage
	        (stageInfo, pContext, pClient, 
		 pTrackingInkblotNodeName, pFrameRange, "sgi", 
		 trackingTempRenderMayaNodeName, "camera01", trackingTempRenderNodeName); 
	    addTaskAnnotation(stage, NodePurpose.Product); 
	    stage.build();  
	  }
	}

	String approveNodeName = pShotNamer.getTrackingApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(extractedCameraNodeName); 
	  sources.add(extractedTrackNodeName); 
	  sources.add(approvedTrackingMarkersNodeName); 

	  if(pDoTempRender) 
	    sources.add(pTrackingInkblotNodeName); 

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

    private static final long serialVersionUID = -3481579311780824310L; 
  }
   

  /*----------------------------------------------------------------------------------------*/

  protected 
  class QueueDisableCleanupPass
    extends ConstructPass
  {
    public 
    QueueDisableCleanupPass()
    {
      super("Queue, Disable Actions and Cleanup", 
	    "");
    }
    
    /**
     * Return both finalizable stage nodes and nodes which will have their actions
     * disabled to be queued now.
     */ 
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      TreeSet<String> regenerate = new TreeSet<String>();

      regenerate.addAll(getDisableList());
      for(FinalizableStage stage : pFinalStages) 
 	regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    /**
     * Cleanup any temporary node structures used setup the network and 
     * disable the actions of the newly regenerated nodes.
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for(FinalizableStage stage : pFinalStages) 
	stage.finalizeStage();
      disableActions();
    }
    
    private static final long serialVersionUID = -2641506858911279542L;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2163808030372775045L; 

  public static final String aTempRender = "TempRender"; 


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The stages which require running their finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~1k plate images.
   */ 
  protected String pUndistorted1kPlateNodeName; 

  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  protected String pUndistorted2kPlateNodeName; 

  /**
   * The frame range of the shot.
   */ 
  private FrameRange pFrameRange; 

  /**
   * The fully resolved name of the node containing a MEL script used to set 
   * render resolutions which match that of the undistorted plates.
   */ 
  private String pResolutionNodeName; 

  /** 
   * Whether to add the nodes required for a temp render.
   */ 
  private boolean pDoTempRender; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and verify the tracking test render Maya scene.
   */ 
  private String pTrackPrepNodeName;  

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and setup the tracking temp render Maya scene.
   */ 
  private String pTrackTempPrepNodeName;  

  /**
   * The fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking verification test renders.
   */ 
  private String pTrackVerifyGlobalsNodeName;  

  /**
   * The fully resolved name of the node for the Maya scene containing 
   * the camera/model tracking data.
   */ 
  private String pTrackNodeName; 

  /**
   * The fully resolved name of the node containing 2D tracking data exported from PFTrack.
   */ 
  private String pTrackingMarkersNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script which creates a 
   * worldspace duplicate of the tracked camera with baked animation and saves it into 
   * a clean scene.
   */ 
  private String pTrackExtractCameraNodeName;  

  /**
   * The fully resolved name of the node containing a MEL script which creates a 
   * worldspace locator with baked animation and saves it into a clean scene.
   */ 
  private String pTrackExtractTrackingNodeName;  

  /**
   * The fully resolved name of node containig the inkblot temp render images. 
   */ 
  private String pTrackingInkblotNodeName; 

  /** 
   * The fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking temp renders.
   */ 
  private String pTrackTempGlobalsNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a placeholder Maya scene which will 
   * eventually contain the camera/model tracking data exported from PFTrack.
   */ 
  private String pRorschachTrackPlaceholderNodeName; 

  /**
   * The fully resolved name of the node containing the hat rig Maya scene.
   */ 
  private String pRorschachHatRigNodeName; 

  /**
   * The fully resolved name of the node containing a Maya scene which provides the 
   * test rig used in the tracking verification test renders.
   */ 
  private String pRorschachVerifyModelNodeName;  

  /**
   * The fully resolved name of the node containing a Maya scene which provides the 
   * rig used in the tracking temp renders.
   */ 
  private String pRorschachTempModelNodeName;  

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test shaders used in the tracking verification test renders.
   */ 
  private String pRorschachTestShadersNodeName;  

  /**
   * The fully resolved name of the node containing a Maya scene which provides the
   * test lights used in the tracking verification test renders.
   */ 
  private String pRorschachTestLightsNodeName;  

  /**
   * The fully resolved name of the node containing the temp inkblot texture.
   */ 
  private String pRorschachTempTextureNodeName;  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a MEL script to hide all camera
   * image planes from view before rendering.
   */ 
  private String pHideCameraPlaneNodeName;  

}
