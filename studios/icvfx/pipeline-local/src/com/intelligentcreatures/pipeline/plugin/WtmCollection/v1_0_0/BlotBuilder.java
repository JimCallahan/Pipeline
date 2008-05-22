// $Id: BlotBuilder.java,v 1.11 2008/05/22 20:34:31 jesse Exp $

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
          mclient, qclient, builderInfo, studioDefs, 
	  projectNamer, shotNamer, TaskType.Blot);

    /* setup builder parameters */ 
    {
      /* selects the project, sequence and shot for the task */ 
      addLocationParam(); 
    }
     
    /* initialize the project namer */ 
    initProjectNamer(); 
    
    /* initialize fields */ 
    pFinalStages = new ArrayList<FinalizableStage>(); 

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
    plugins.add(new PluginContext("NukeQt", "Temerity", 
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
    plugins.add(new PluginContext("MayaBuild")); 		
    plugins.add(new PluginContext("MayaAttachGeoCache")); 	
    plugins.add(new PluginContext("MayaAttachSound")); 	
    plugins.add(new PluginContext("MayaFTNBuild")); 	
    plugins.add(new PluginContext("MayaIgesExport")); 			
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
        pBlotAttachPreviewNodeName = pProjectNamer.getBlotAttachPreviewNode();
	pRequiredNodeNames.add(pBlotAttachPreviewNodeName); 

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
      /* soundtrack */ 
      pAttachSoundtrackNodeName = pShotNamer.getAttachSoundtrackNode(); 
      if(!nodeExists(pAttachSoundtrackNodeName)) {
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

      pSoundtrackNodeName = pShotNamer.getSoundtrackNode(); 
      pRequiredNodeNames.add(pSoundtrackNodeName);

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
	pAttachSoundtrackNodeName = pShotNamer.getAttachSoundtrackNode(); 
	{
	  if(!nodeExists(pAttachSoundtrackNodeName)) 
	    throw new PipelineException
	      ("Somehow the required attach soundtrack MEL script node " + 
	       "(" + pAttachSoundtrackNodeName + ") does not exist!"); 

	  try {
	    checkOutLatest(pAttachSoundtrackNodeName, 
			   CheckOutMode.KeepModified, CheckOutMethod.Modifiable);
	  }
	  catch(PipelineException ex) {
	    try {
	      /* if it already exists in the working area, just leave it alone */ 
	      pClient.getWorkingVersion(getAuthor(), getView(), pAttachSoundtrackNodeName); 
	    }
	    catch(PipelineException ex2) {
	      throw new PipelineException
		("Somehow no working version of the required attach soundtrack MEL " + 
		 "script node (" + pAttachSoundtrackNodeName + ") exists in the current " + 
		 "working area (" + getAuthor() + "|" + getView() + " and it has never " + 
		 "been checked-in!");
	    }
	  }
	}

	pBlotAnimSceneNodeName = pShotNamer.getBlotAnimSceneNode();
	{
	  BuildBlotAnimStage stage = 
	    new BuildBlotAnimStage
	      (stageInfo, pContext, pClient, 
	       pBlotAnimSceneNodeName, pRorschachBlotAnimPlaceholderNodeName, 
	       pRorschachGuidelinesNodeName, pAttachSoundtrackNodeName, pFrameRange); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  pFinalStages.add(stage);
	}

	pBlotAnimTexturesNodeName = pShotNamer.getBlotAnimTexturesNode();
	{
	  RenderTaskVerifyStage stage = 
	    new RenderTaskVerifyStage
	    (stageInfo, pContext, pClient, 
	     pBlotAnimTexturesNodeName, pFrameRange, pBlotAnimSceneNodeName, 
	     "camera01", pBlotAnimPrepNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
	}

	String blotAnimQuickTimeNodeName = pShotNamer.getBlotAnimQuickTimeNode();
	{
	  NukeQtStage stage = 
	    new NukeQtStage(stageInfo, pContext, pClient,
			    blotAnimQuickTimeNodeName, pBlotAnimTexturesNodeName, 
			    pSoundtrackNodeName, 24.0);
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String blotAnimThumbNodeName = pShotNamer.getBlotAnimThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   blotAnimThumbNodeName, "tif", pBlotAnimTexturesNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}
	
 	String blotTextureSceneNodeName = pShotNamer.getBlotTextureSceneNode();
 	{
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(stageInfo, pContext, pClient, 
				  new MayaContext(), blotTextureSceneNodeName, true);
	  stage.addLink(new LinkMod(pBlotAnimTexturesNodeName, LinkPolicy.Dependency)); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String blotAttachCacheNodeName = pShotNamer.getBlotAttachCacheNode();
	{
	  AttachGeoCacheStage stage = 
	    new AttachGeoCacheStage(stageInfo, pContext, pClient, 
				    blotAttachCacheNodeName, pMatchGeoCacheNodeName, 
				    "mdl:rorHead_GEOShape", pBlotAttachPreviewNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

 	String blotTestSceneNodeName = pShotNamer.getBlotTestSceneNode();
 	{ 
	  BuildBlotTestStage stage = 
	    new BuildBlotTestStage
	      (stageInfo, pContext, pClient, 
	       blotTestSceneNodeName, pRorschachHiresModelNodeName, 
	       pRorschachPreviewShadersNodeName, blotTextureSceneNodeName, 
	       pExtractedCameraNodeName, pUndistorted1kPlateNodeName,
	       blotAttachCacheNodeName, pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
 	}

 	String blotTestImagesNodeName = pShotNamer.getBlotTestImagesNode();
 	{
	  RenderTaskVerifyStage stage = 
	    new RenderTaskVerifyStage
	      (stageInfo, pContext, pClient, 
	       blotTestImagesNodeName, pFrameRange, blotTestSceneNodeName, 
	       "cam:camera01", pBlotTestPrepNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build();  
 	}

	String blotTestQuickTimeNodeName = pShotNamer.getBlotTestQuickTimeNode(); 
	{
	  NukeQtStage stage = 
	    new NukeQtStage(stageInfo, pContext, pClient,
			    blotTestQuickTimeNodeName, blotTestImagesNodeName, 
			    pSoundtrackNodeName, 24.0);
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 	  
	}
	
	String blotTestThumbNodeName = pShotNamer.getBlotTestThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   blotTestThumbNodeName, "tif", blotTestImagesNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getBlotSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(blotAnimQuickTimeNodeName);
	  sources.add(blotAnimThumbNodeName);
	  sources.add(blotTestQuickTimeNodeName);
	  sources.add(blotTestThumbNodeName);

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
	String blotApprovedTexturesNodeName = pShotNamer.getBlotApprovedTexturesNode();
	{
	  CopyImagesStage stage = 
	    new CopyImagesStage
	      (stageInfo, pContext, pClient, 
	       blotApprovedTexturesNodeName, pFrameRange, 4, "tif", 
	       pBlotAnimTexturesNodeName);  
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String blotCurveGeoNodeName = pShotNamer.getBlotCurveGeoNode(); 
	{
	  BlotCurveGeoStage stage = 
	    new BlotCurveGeoStage(stageInfo, pContext, pClient,
				  blotCurveGeoNodeName, pBlotAnimSceneNodeName, 
				  "CURVES", pFrameRange);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}
	
 	String approveNodeName = pShotNamer.getBlotApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
 	  sources.add(blotApprovedTexturesNodeName);
 	  sources.add(blotCurveGeoNodeName); 

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
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> regenerate = new LinkedList<String>();

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
   * The stages which require running their finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach the blot textures and shaders in the animation test render scene.
   */ 
  private String pBlotAttachPreviewNodeName; 

  /**
   * The fully resolved name of the node containing the combined MEL scripts to 
   * attach shaders the shaders for the blot animation scene.
   */ 
  private String pBlotAnimPrepNodeName;

  /**
   * The fully resolved name of the blot animation Maya scene node.
   */ 
  private String pBlotAnimSceneNodeName; 

  /**
   * The  fully resolved name of the rendered blot textures node.
   */ 
  private String pBlotAnimTexturesNodeName; 

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
   * Returns the fully resolved name of the MEL script used to load the soundtrack.
   */ 
  private String pAttachSoundtrackNodeName; 
  
  /**
   * The fully resolved name of the shot soundtrack node.
   */ 
  private String pSoundtrackNodeName; 

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
