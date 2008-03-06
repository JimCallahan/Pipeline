// $Id: BlotBuilder.java,v 1.1 2008/03/06 06:14:49 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B L O T   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Blot task.<P> 
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
class BlotBuilder 
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
  BlotBuilder
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
  BlotBuilder
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
    super("Blot",
          "A builder for constructing the nodes associated with the Blot task.", 
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 
    }
     
    /* initialize the project namer */ 
    initProjectNamer(); 
    
    /* create the setup passes */ 
    {
      addSetupPass(new BlotSetupShotEssentials());
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
    plugins.add(new PluginContext("MayaBuild")); 		
    plugins.add(new PluginContext("MayaAttachGeoCache")); 	
    plugins.add(new PluginContext("MayaFTNBuild")); 		
    plugins.add(new PluginContext("MayaRender")); 		
    plugins.add(new PluginContext("NukeThumbnail"));		
    plugins.add(new PluginContext("NukeQt"));			

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
			     TaskType.Blot.toString(), builderID);
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
                      TaskType.Blot.toString()); 
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
                      TaskType.Blot.toString()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class BlotSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    BlotSetupShotEssentials()
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
        /* blot assets */ 
        pBlotAttachPreviewNode = pProjectNamer.getBlotAttachPreviewNode();
	pRequiredNodeNames.add(pBlotAttachPreviewNode); 

        pBlotAnimPrepNodeName = pProjectNamer.getBlotAnimPrepNode(); 
	pRequiredNodeNames.add(pBlotAnimPrepNodeName); 
        
        pBlotTestPrepNodeName = pProjectNamer.getBlotTestPrepNode(); 
	pRequiredNodeNames.add(pBlotTestPrepNodeName); 

	/* rorschach assets */ 
	pRorschachHiresModelNodeName = pProjectNamer.getRorschachHiresModelNode(); 
	pRequiredNodeNames.add(pRorschachHiresModelNodeName); 
        
        pRorschachPreviewShadersNodeName = pProjectNamer.getRorschachPreviewShadersNode();
	pRequiredNodeNames.add(pRorschachPreviewShadersNodeName);    

        pRorschachBlotAnimPlaceholderNodeName = 
          pProjectNamer.getRorschachBlotAnimPlaceholderNode();
        pRequiredNodeNames.add(pRorschachBlotAnimPlaceholderNodeName);        

	pRorschachGuidelinesNodeName = pProjectNamer.getRorschachGuidelinesNode(); 
	pRequiredNodeNames.add(pRorschachGuidelinesNodeName); 
      }
    }
    
    private static final long serialVersionUID = -8173445067750517854L;
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
      /* the geometry cache */ 
      pMatchGeoCacheNodeName = pShotNamer.getMatchGeoCacheNode();
      pRequiredNodeNames.add(pMatchGeoCacheNodeName); 

      /* extracted camera */ 
      pExtractedCameraNodeName = pShotNamer.getTrackingExtractedCameraNode();
      pRequiredNodeNames.add(pExtractedCameraNodeName); 

      /* the background plates node */ 
      pUndistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
      pRequiredNodeNames.add(pUndistorted1kPlateNodeName); 

      /* lookup the frame range of the shot by looking at the undistorted 1k plates node */ 
      {
	NodeVersion vsn = pClient.getCheckedInVersion(pUndistorted1kPlateNodeName, null); 
	if(vsn == null) 
	  throw new PipelineException
	    ("Somehow no checked-in version of the undistorted 1k plates node " + 
	     "(" + pUndistorted1kPlateNodeName + ") exists!"); 	

	pFrameRange = vsn.getPrimarySequence().getFrameRange(); 
      }
    }

    private static final long serialVersionUID = -4286766691223998369L;
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
            "Creates the nodes which make up the Blot task."); 
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

	String prereqNodeName = pShotNamer.getBlotPrereqNode();
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
	//	pSoundTrackNodeName = pShotNamer.getSoundTrackNode(); 






// 	String preBlotAnimNodeName = pShotNamer.getPreBlotAnimNode(); 
// 	{
// 	  BuildPreBlotStage stage = 
// 	    new BuildPreBlotStage
// 	    (stageInfo, pContext, pClient, 
// 	     preBlotAnimNodeName, pConstrainRigNodeName, 
// 	     pRorschachRigNodeName, pExtractedCameraNodeName, pExtractedTrackNodeName, 
// 	     pFrameRange); 
// 	  addTaskAnnotation(stage, NodePurpose.Prepare); 
// 	  stage.build();  
// 	}

// 	pBlotAnimNodeName = pShotNamer.getBlotAnimNode(); 
// 	{
// 	  BuildBlotStage stage = 
// 	    new BuildBlotStage(stageInfo, pContext, pClient, 
// 				pBlotAnimNodeName, preBlotAnimNodeName, pFrameRange); 
// 	  addTaskAnnotation(stage, NodePurpose.Edit); 
// 	  stage.build(); 
// 	  addToDisableList(preBlotAnimNodeName); 	  
// 	}

// 	String verifyNodeName = pShotNamer.getBlotVerifyNode(); 
// 	{
// 	  BuildBlotVerifyStage stage = 
// 	    new BuildBlotVerifyStage
// 	    (stageInfo, pContext, pClient, 
// 	     verifyNodeName, pBlotAnimNodeName, pRorschachTestShadersNodeName, 
// 	     pBlotPrepNodeName, pFrameRange); 
// 	  addTaskAnnotation(stage, NodePurpose.Prepare); 
// 	  stage.build();  
// 	}

// 	String matchPreRenderScriptNodeName = pShotNamer.getBlotPreRenderScriptNode();
// 	{
// 	  LinkedList<String> sources = new LinkedList<String>(); 
// 	  sources.add(pHideCameraPlaneNodeName); 
// 	  sources.add(pTrackVerifyGlobalsNodeName); 
// 	  sources.add(pResolutionNodeName); 

// 	  CatMelStage stage = 
// 	    new CatMelStage(stageInfo, pContext, pClient, 
// 			    matchPreRenderScriptNodeName, sources);
// 	  addTaskAnnotation(stage, NodePurpose.Prepare); 
// 	  stage.build();  
// 	}

// 	String verifyImagesNodeName = pShotNamer.getBlotVerifyImagesNode(); 
// 	{
// 	  RenderTaskVerifyStage stage = 
// 	    new RenderTaskVerifyStage
// 	    (stageInfo, pContext, pClient, 
// 	     verifyImagesNodeName, pFrameRange, verifyNodeName, 
// 	     "match:prep:cam:camera01", matchPreRenderScriptNodeName); 
// 	  addTaskAnnotation(stage, NodePurpose.Focus); 
// 	  stage.build();  
// 	}

// 	String verifyCompNodeName = pShotNamer.getBlotVerifyCompNode(); 
// 	{
// 	  BashCompStage stage = 
// 	    new BashCompStage(stageInfo, pContext, pClient, 
// 			      verifyCompNodeName, pFrameRange, 
// 			      verifyImagesNodeName, pUndistorted2kPlateNodeName); 
// 	  addTaskAnnotation(stage, NodePurpose.Focus); 	 
// 	  stage.build(); 
// 	}

// 	String verifyThumbNodeName = pShotNamer.getBlotVerifyThumbNode();
// 	{
// 	  NukeThumbnailStage stage = 
// 	    new NukeThumbnailStage(stageInfo, pContext, pClient,
// 				   verifyThumbNodeName, "tif", verifyCompNodeName, 
// 				   1, 150, 1.0, true, true, new Color3d()); 
// 	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
// 	  stage.build(); 
// 	}

	String submitNodeName = pShotNamer.getBlotSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
// 	  sources.add();
// 	  sources.add();
// 	  sources.add();
// 	  sources.add();

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
// 	String matchPrebakeSceneNodeName = pShotNamer.getBlotPrebakeSceneNode();
// 	{
// 	  BuildBlotPrebakeStage stage = 
// 	    new BuildBlotPrebakeStage(stageInfo, pContext, pClient, 
// 				       matchPrebakeSceneNodeName, 
// 				       pBlotAnimNodeName, pRorschachHiresModelNodeName, 
// 				       pBlotPrebakeNodeName, pFrameRange); 
// 	  addTaskAnnotation(stage, NodePurpose.Prepare); 
// 	  stage.build(); 
// 	}

// 	String matchGeoCacheNodeName = pShotNamer.getBlotGeoCacheNode();
// 	{
// 	  BlotGeoCacheStage stage = 
// 	    new BlotGeoCacheStage(stageInfo, pContext, pClient, 
// 				   matchGeoCacheNodeName, matchPrebakeSceneNodeName, 
//                                    "rorHead_GEOShape"); 
// 	  addTaskAnnotation(stage, NodePurpose.Product); 
// 	  stage.build(); 
// 	}

// 	String matchMaskGeoNodeName = pShotNamer.getBlotMaskGeoNode();
// 	{
// 	  BlotMaskGeoStage stage = 
// 	    new BlotMaskGeoStage(stageInfo, pContext, pClient, 
// 				  matchMaskGeoNodeName, matchPrebakeSceneNodeName, 
//                                   "rorHead_GEO", pFrameRange); 
// 	  addTaskAnnotation(stage, NodePurpose.Product); 
// 	  stage.build(); 
// 	}

 	String approveNodeName = pShotNamer.getBlotApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
//  	  sources.add();
//  	  sources.add();

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

    private static final long serialVersionUID = -6819376171666233045L;
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
    
    private static final long serialVersionUID = 6212862876117764637L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4882006094499927504L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach the blot textures and shaders in the animation test render scene.
   */ 
  private String pBlotAttachPreviewNode; 

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders the shaders for the blot animation scene.
   */ 
  private String pBlotAnimPrepNodeName;

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders the shaders for the blot animation test render scene.
   */ 
  private String pBlotTestPrepNodeName;


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
  
  /**
   * The fully resolved name of the node containing a placeholder Maya scene which 
   * will eventually contain the blot animation.
   */ 
  private String pRorschachBlotAnimPlaceholderNodeName; 

  /**
   * The fully resolved name of the node containing the face guidelines image. 
   */ 
  private String pRorschachGuidelinesNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the shot soundtrack node.
   */ 
  private String pSoundTrackNodeName; 

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
   * The fully resolved name of the node containing the undistorted/linearized
   * ~1k plate images.
   */ 
  private String pUndistorted1kPlateNodeName; 

  /**
   * The frame range of the shot.
   */ 
  private FrameRange pFrameRange; 


}
