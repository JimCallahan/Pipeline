// $Id: NoiseBuilder.java,v 1.3 2008/03/17 17:49:15 jim Exp $

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
      addSetupPass(new NoiseSetupShotEssentials());
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
    plugins.add(new PluginContext("Copy")); 
    plugins.add(new PluginContext("MayaBuild")); 		
    plugins.add(new PluginContext("MayaAttachGeoCache")); 
    plugins.add(new PluginContext("MayaFTNBuild")); 			
    plugins.add(new PluginContext("MayaRender")); 		
    plugins.add(new PluginContext("NukeThumbnail"));		
    plugins.add(new PluginContext("DjvUnixQt"));			

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
			     TaskType.Noise.toString(), builderID);
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
                      TaskType.Noise.toString()); 
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
                      TaskType.Noise.toString()); 
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

        /* blot assets */ 
        pBlotAttachPreviewNodeName = pProjectNamer.getBlotAttachPreviewNode();
	pRequiredNodeNames.add(pBlotAttachPreviewNodeName); 

        pBlotTestPrepNodeName = pProjectNamer.getBlotTestPrepNode(); 
	pRequiredNodeNames.add(pBlotTestPrepNodeName); 
	
	/* rorschach assets */ 
	pRorschachHiresModelNodeName = pProjectNamer.getRorschachHiresModelNode(); 
	pRequiredNodeNames.add(pRorschachHiresModelNodeName); 
        
        pRorschachPreviewShadersNodeName = pProjectNamer.getRorschachPreviewShadersNode();
	pRequiredNodeNames.add(pRorschachPreviewShadersNodeName);    
      }
    }

    private static final long serialVersionUID = -8860185784531713731L;
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
      /* blot textures */ 
      pBlotApprovedTexturesNodeName = pShotNamer.getBlotApprovedTexturesNode(); 
      pRequiredNodeNames.add(pBlotApprovedTexturesNodeName); 

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

	String noiseQuickTimeNodeName = pShotNamer.getNoiseQuickTimeNode();
	{
	  DjvUnixQtStage stage = 
	    new DjvUnixQtStage(stageInfo, pContext, pClient,
			       noiseQuickTimeNodeName, pNoiseTexturesNodeName, "24");
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String noiseThumbNodeName = pShotNamer.getNoiseThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   noiseThumbNodeName, "tif", pNoiseTexturesNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}
	
 	String noiseTextureSceneNodeName = pShotNamer.getNoiseTextureSceneNode();
 	{
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(stageInfo, pContext, pClient, 
				  new MayaContext(), noiseTextureSceneNodeName, true);
	  stage.addLink(new LinkMod(pNoiseTexturesNodeName, LinkPolicy.Dependency)); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String noiseAttachCacheNodeName = pShotNamer.getNoiseAttachCacheNode();
	{
	  AttachGeoCacheStage stage = 
	    new AttachGeoCacheStage(stageInfo, pContext, pClient, 
				    noiseAttachCacheNodeName, pMatchGeoCacheNodeName, 
				    "mdl:rorHead_GEOShape", pBlotAttachPreviewNodeName); 
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

 	String noiseTestImagesNodeName = pShotNamer.getNoiseTestImagesNode();
 	{
	  RenderTaskVerifyStage stage = 
	    new RenderTaskVerifyStage
	      (stageInfo, pContext, pClient, 
	       noiseTestImagesNodeName, pFrameRange, noiseTestSceneNodeName, 
	       "cam:camera01", pBlotTestPrepNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
 	}

	String noiseTestQuickTimeNodeName = pShotNamer.getNoiseTestQuickTimeNode(); 
	{
	  DjvUnixQtStage stage = 
	    new DjvUnixQtStage(stageInfo, pContext, pClient,
			       noiseTestQuickTimeNodeName, noiseTestImagesNodeName, "24");
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 	  
	}
	
	String noiseTestThumbNodeName = pShotNamer.getNoiseTestThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   noiseTestThumbNodeName, "tif", noiseTestImagesNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getNoiseSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(noiseQuickTimeNodeName);
	  sources.add(noiseThumbNodeName);
	  sources.add(noiseTestQuickTimeNodeName);
	  sources.add(noiseTestThumbNodeName);

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

 	String approveNodeName = pShotNamer.getNoiseApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
 	  sources.add(noiseApprovedTexturesNodeName);

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
 
    private static final long serialVersionUID = 3260739941337787344L;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3788746046365936548L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the Nuke script used to noise
   * up the blot textures.
   */ 
  private String pAddNoiseNukeNodeName; 


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
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders the shaders for the blot animation test render scene.
   */ 
  private String pBlotTestPrepNodeName;

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
