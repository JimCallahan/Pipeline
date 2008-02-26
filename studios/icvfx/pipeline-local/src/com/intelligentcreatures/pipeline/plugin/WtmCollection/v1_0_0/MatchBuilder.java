// $Id: MatchBuilder.java,v 1.5 2008/02/26 20:26:35 jim Exp $

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
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

    /* initialize fields */ 
    {
//       pFinalStages = new ArrayList<FinalizableStage>(); 
    }

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 
    }
     
    /* initialize the project namer */ 
    initProjectNamer(); 
    
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
      addPassDependency(build, qd);
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
//     plugins.add(new PluginContext("Touch")); 
//     plugins.add(new PluginContext("Copy"));   		
//     plugins.add(new PluginContext("NukeCatComp")); 		
//     plugins.add(new PluginContext("NukeExtract"));		
//     plugins.add(new PluginContext("NukeQt"));			
//     plugins.add(new PluginContext("NukeReformat"));		
//     plugins.add(new PluginContext("NukeRead"));			
//     plugins.add(new PluginContext("NukeRescale")); 		
//     plugins.add(new PluginContext("NukeThumbnail"));          

    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);

    return toReturn;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param builderID
   *   The unique ID of the approval builder.
   */ 
  protected void
  addAproveTaskAnnotation
  (
   BaseStage stage, 
   BuilderID builderID
  )
    throws PipelineException
  {
    addApproveTaskAnnotation(stage,
			     pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
			     TaskType.Match.toString(), builderID);
  }

  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins which will be added to the node built by the given Stage.<P> 
   * 
   * @param stage
   *   The stage to be modified.
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
   BaseStage stage,
   NodePurpose purpose
  )
    throws PipelineException
  {
    addTaskAnnotation(stage, purpose, 
                      pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
                      TaskType.Match.toString()); 
  }

  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins on the given node. <P> 
   * 
   * @param nodeName
   *   The fully resolved name of the node to be annotated. 
   * 
   * @param purpose
   *   The purpose of the node within the task.
   */ 
  protected void
  addTaskAnnotation
  (
   String nodeName, 
   NodePurpose purpose
  )
    throws PipelineException
  {
    addTaskAnnotation(nodeName, purpose, 
                      pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
                      TaskType.Match.toString()); 
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
	pConstrainRigNodeName = pProjectNamer.getConstrainRigNode(); 
	pRequiredNodeNames.add(pConstrainRigNodeName); 

	pRorschachRigNodeName = pProjectNamer.getRorschachRigNode(); 
	pRequiredNodeNames.add(pRorschachRigNodeName); 

	pMatchPrepNodeName = pProjectNamer.getMatchPrepNode(); 
	pRequiredNodeNames.add(pMatchPrepNodeName); 

	/* rorschach assets */ 
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
      /* get the render resolution MEL script */ 
      pResolutionNodeName = pShotNamer.getResolutionNode(); 
      pRequiredNodeNames.add(pResolutionNodeName); 
      
      /* extracted camera/track nodes */ 
      {
	pExtractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
	pRequiredNodeNames.add(pExtractedCameraNodeName); 
	
	pExtractedTrackNodeName = pShotNamer.getTrackingExtractedTrackNode();
	pRequiredNodeNames.add(pExtractedTrackNodeName); 
      }

      /* the background plates node */ 
      pUndistorted2kPlateNodeName = pShotNamer.getUndistorted2kPlateNode(); 
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

	String matchAnimNodeName = pShotNamer.getMatchAnimNode(); 
	{
	  BuildMatchStage stage = 
	    new BuildMatchStage(stageInfo, pContext, pClient, 
				matchAnimNodeName, preMatchAnimNodeName, pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  addToDisableList(preMatchAnimNodeName); 	  
	}

	String verifyNodeName = pShotNamer.getMatchVerifyNode(); 
	{
	  BuildMatchVerifyStage stage = 
	    new BuildMatchVerifyStage
	    (stageInfo, pContext, pClient, 
	     verifyNodeName, matchAnimNodeName, pRorschachTestShadersNodeName, 
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

	  CatMelStage stage = 
	    new CatMelStage(stageInfo, pContext, pClient, 
			    matchPreRenderScriptNodeName, sources);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String verifyImagesNodeName = pShotNamer.getMatchVerifyImagesNode(); 
	{
	  RenderTaskVerifyStage stage = 
	    new RenderTaskVerifyStage
	    (stageInfo, pContext, pClient, 
	     verifyImagesNodeName, pFrameRange, verifyNodeName, 
	     "match:prep:cam:camera01", matchPreRenderScriptNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
	}

	String verifyCompNodeName = pShotNamer.getMatchVerifyCompNode(); 
	{
	  BashCompStage stage = 
	    new BashCompStage(stageInfo, pContext, pClient, 
			      verifyCompNodeName, pFrameRange, 
			      verifyImagesNodeName, pUndistorted2kPlateNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 	 
	  stage.build(); 
	}

	String verifyThumbNodeName = pShotNamer.getMatchVerifyThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   verifyThumbNodeName, "tif", verifyCompNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getMatchSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(verifyThumbNodeName);

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



 	String approveNodeName = pShotNamer.getMatchApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
// 	  sources.add();
// 	  sources.add();
// 	  sources.add();

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
    public TreeSet<String> 
    preBuildPhase()
    {
      return getDisableList();
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
   * The stages which require running their finalizeStage() method before check-in.
   */ 
//   private ArrayList<FinalizableStage> pFinalStages; 


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
   * The fully resolved name of the node containing a MEL script used to add head 
   * and neck constraints to the match rig.
   */ 
  private String pConstrainRigNodeName;

  /**
   * The fully resolved name of the node containing the match rig Maya scene.
   */ 
  private String pRorschachRigNodeName;

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders and verify the match test render Maya scene.
   */ 
  private String pMatchPrepNodeName;

  /**
   * The fully resolved name of the node containing a MEL script which used to set
   * the Maya render globals for tracking verification test renders.
   */ 
  private String pTrackVerifyGlobalsNodeName;  

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

}
