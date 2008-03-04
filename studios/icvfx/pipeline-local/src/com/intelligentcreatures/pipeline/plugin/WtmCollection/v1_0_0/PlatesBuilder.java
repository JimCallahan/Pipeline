// $Id: PlatesBuilder.java,v 1.21 2008/03/04 08:15:16 jesse Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L A T E S   B U I L D E R                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Plates task.<P> 
 * 
 * Contains the scanned plate images, camera data and any other reference images shot on 
 * set.  Any required painting fixes are applied and then the images are undistored and 
 * linearized.  A GridWarp Nuke node is produced which can be used to redistort rendered
 * images later along with a MEL script to set the undistored image resolution for renders.
 * Finally, the undistored plates are resized down to 1k, a QuickTime movie is built and 
 * a thumbnail image is extracted. <P> 
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
class PlatesBuilder 
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
  PlatesBuilder
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
  PlatesBuilder
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
    super("Plates",
          "A builder for constructing the nodes associated with the Plates task.", 
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

    /* initialize fields */ 
    {
      pMiscReferenceNodeNames = new TreeSet<String>(); 
      pFinalStages = new ArrayList<FinalizableStage>(); 
    }

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
      addSetupPass(new PlatesSetupShotEssentials());
      addSetupPass(new SetupImageParams());
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
    plugins.add(new PluginContext("LensInfo", "ICVFX"));	
    //plugins.add(new PluginContext("PFTrackBuild", "ICVFX"));	
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("Copy"));   		
    plugins.add(new PluginContext("MayaResolution"));  
    plugins.add(new PluginContext("NukeCatComp")); 		
    plugins.add(new PluginContext("NukeExtract"));		
    plugins.add(new PluginContext("NukeQt"));			
    plugins.add(new PluginContext("NukeReformat"));		
    plugins.add(new PluginContext("NukeRead"));			
    plugins.add(new PluginContext("NukeRescale")); 		
    plugins.add(new PluginContext("NukeThumbnail"));          

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
			     TaskType.Plates.toString(), builderID);
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
                      TaskType.Plates.toString()); 
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
                      TaskType.Plates.toString()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class PlatesSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    PlatesSetupShotEssentials()
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
	pPlatesRedCheckerNodeName = pProjectNamer.getPlatesRedCheckerNode();
	pRequiredNodeNames.add(pPlatesRedCheckerNodeName); 
	
	pPlatesGreenCheckerNodeName = pProjectNamer.getPlatesGreenCheckerNode();
	pRequiredNodeNames.add(pPlatesGreenCheckerNodeName); 
	
	pGridGradeWarpNodeName = pProjectNamer.getGridGradeWarpNode();
	pRequiredNodeNames.add(pGridGradeWarpNodeName); 
	
	pGridGradeDiffNodeName = pProjectNamer.getGridGradeDiffNode();
	pRequiredNodeNames.add(pGridGradeDiffNodeName); 
	
	pBlackOutsideNodeName = pProjectNamer.getBlackOutsideNode();
	pRequiredNodeNames.add(pBlackOutsideNodeName); 
      }
    }
    
    private static final long serialVersionUID = -6691101175651749909L;
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

    private static final long serialVersionUID = 5742118589827518495L;
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
      /* the background plates node */ 
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

      /* the miscellaneous reference images */ 
      {
        Path mpath = pShotNamer.getPlatesMiscReferenceParentPath(); 
        for(String rname : findChildNodeNames(mpath)) {
          Path path = new Path(mpath, rname);
          String nodeName = path.toString();
          pMiscReferenceNodeNames.add(nodeName); 
          pRequiredNodeNames.add(nodeName); 
        }
      }
    }

    private static final long serialVersionUID = 7830642124642481656L;
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
            "Creates the nodes which make up the Plates task."); 
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

	String prereqNodeName = pShotNamer.getPlatesPrereqNode();
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

      /* add Edit annotations to all reference images and plates */ 
      {
	addTaskAnnotation(pBackgroundPlateNodeName, NodePurpose.Edit); 
	for(String name : pMiscReferenceNodeNames) 
	  addTaskAnnotation(name, NodePurpose.Edit); 
      }

      /* the submit network */
      {
	String vfxRefNodeName = pShotNamer.getVfxReferenceNode(NodePurpose.Prepare); 
	{
	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  LensInfoStage stage = 
	    new LensInfoStage(stageInfo, pContext, pClient, 
			      vfxShotDataNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	}

	String solveDistortionNodeName = pShotNamer.getSolveDistortionNode(); 
	{
	  PFTrackBuildStage stage = 
	    new PFTrackBuildStage(stageInfo, pContext, pClient, 
				  solveDistortionNodeName, pBackgroundPlateNodeName, 
				  vfxShotDataNodeName); 
	  stage.addLink(new LinkMod(pPlatesRedCheckerNodeName, 
				    LinkPolicy.Association, LinkRelationship.None, null));
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build();  
	  addToDisableList(solveDistortionNodeName);
	}
	
	pDistortedGridNodeName = pShotNamer.getDistortedGridNode(); 
	{
	  DistortedGridStage stage = 
	    new DistortedGridStage(stageInfo, pContext, pClient, 
				   pDistortedGridNodeName, pPlatesRedCheckerNodeName, 
				   solveDistortionNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	  pFinalStages.add(stage);
	}

	String readDistortedNodeName = pShotNamer.getReadDistortedNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(stageInfo, pContext, pClient, 
			      readDistortedNodeName, pDistortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	String reformatOriginalNodeName = pShotNamer.getReformatOriginalNode(); 
	{
	  NukeReadReformatStage stage = 
	    new NukeReadReformatStage(stageInfo, pContext, pClient, 
				      reformatOriginalNodeName, 
				      pPlatesGreenCheckerNodeName, pDistortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	pGridAlignNodeName = pShotNamer.getGridAlignNode();
	{
	  LinkedList<String> sources = new LinkedList<String>(); 
	  sources.add(reformatOriginalNodeName); 
	  sources.add(pGridGradeWarpNodeName); 
	  sources.add(readDistortedNodeName); 
	  sources.add(pGridGradeDiffNodeName); 
 
	  NukeCatStage stage = 
	    new NukeCatStage(stageInfo, pContext, pClient, 
			     pGridAlignNodeName, sources); 
	  stage.addLink(new LinkMod(pPlatesGreenCheckerNodeName, LinkPolicy.Reference));
	  stage.addLink(new LinkMod(pDistortedGridNodeName, LinkPolicy.Reference));
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	}
	
	String gridAlignImageNodeName = pShotNamer.getGridAlignImageNode();
	{
	  LinkedList<String> nukeScripts = new LinkedList<String>(); 
	  nukeScripts.add(pGridAlignNodeName); 

	  NukeCatCompStage stage = 
	    new NukeCatCompStage(stageInfo, pContext, pClient,
				 gridAlignImageNodeName, "tif", nukeScripts);  
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String gridAlignThumbNodeName = pShotNamer.getGridAlignThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   gridAlignThumbNodeName, "tif", gridAlignImageNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getPlatesSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(gridAlignThumbNodeName);
	  sources.add(vfxRefNodeName);

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
	String vfxRefNodeName = pShotNamer.getVfxReferenceNode(NodePurpose.Product); 
	{
	  TargetStage stage = 
	    new TargetStage(stageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String lensWarpNodeName = pShotNamer.getLensWarpNode(); 
	{
	  NukeExtractStage stage = 
	    new NukeExtractStage(stageInfo, pContext, pClient, 
				 lensWarpNodeName, pGridAlignNodeName, 
				 "GridWarp", ".*", false); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}
        
	String reformatBgNodeName = pShotNamer.getReformatBgNode(); 
	{
	  NukeReadReformatStage stage = 
	    new NukeReadReformatStage(stageInfo, pContext, pClient, 
				      reformatBgNodeName, 
				      pBackgroundPlateNodeName, pDistortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	String undistorted2kPlateNodeName = pShotNamer.getUndistorted2kPlateNode(); 
	{
	  LinkedList<String> nukeScripts = new LinkedList<String>(); 
	  nukeScripts.add(reformatBgNodeName); 
	  nukeScripts.add(pBlackOutsideNodeName); 
	  nukeScripts.add(lensWarpNodeName); 

	  LinkedList<String> inputImages = new LinkedList<String>(); 
	  inputImages.add(pBackgroundPlateNodeName); 

	  NukeCatCompStage stage = 
	    new NukeCatCompStage(stageInfo, pContext, pClient,
				 undistorted2kPlateNodeName, pFrameRange, 4, "tif", 
				 nukeScripts, inputImages);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String undistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
	{
	  NukeRescaleStage stage = 
	    new NukeRescaleStage(stageInfo, pContext, pClient,
				 undistorted1kPlateNodeName, pFrameRange, 4, "tif",
				 undistorted2kPlateNodeName, 0.5);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String undistorted1kQuickTimeNodeName = pShotNamer.getUndistorted1kQuickTimeNode(); 
	{
	  NukeQtStage stage = 
	    new NukeQtStage(stageInfo, pContext, pClient,
			    undistorted1kQuickTimeNodeName, undistorted1kPlateNodeName, 24.0);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  addTaskAnnotation(vfxShotDataNodeName, NodePurpose.Product);   
	}
	
	String resolutionNodeName = pShotNamer.getResolutionNode();
	{
	  MayaResolutionStage stage = 
	    new MayaResolutionStage(stageInfo, pContext, pClient,
				    resolutionNodeName, pDistortedGridNodeName, 1.0); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String approveNodeName = pShotNamer.getPlatesApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(undistorted1kQuickTimeNodeName);
	  sources.add(vfxRefNodeName);
	  sources.add(vfxShotDataNodeName);
	  sources.add(resolutionNodeName);

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

    private static final long serialVersionUID = -5216068758078265108L;
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
     * 
     * Then register the second pass nodes to be queued and disabled... 
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      /* process first pass nodes first */ 
      for(FinalizableStage stage : pFinalStages) 
	stage.finalizeStage();
      disableActions();
      
      /* reset the disable list to clear out first pass nodes */ 
      clearDisableList();

      /* register second level nodes to be disabled */ 
      addToDisableList(pGridAlignNodeName);
    }
    
    private static final long serialVersionUID = 4574894365632367362L;
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
    
    private static final long serialVersionUID = 6643366544737486251L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4601321412376464762L;
  
  public final static String aReferenceImages = "ReferenceImages";
  public final static String aBackgroundPlate = "BackgroundPlate";

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The stages which require running their finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name and frame range of background plates node. 
   */ 
  private String pBackgroundPlateNodeName;
  private FrameRange pFrameRange; 
  
  /**
   * The fully resolved names of all reference image nodes.
   */ 
  private TreeSet<String> pMiscReferenceNodeNames; 

  /**
   * The fully resolved name of the original undistored checkerboard nodes.
   */ 
  private String pPlatesRedCheckerNodeName; 
  private String pPlatesGreenCheckerNodeName; 

  /**
   * The fully resolved name of the node containing the distorted reference 
   * grid image exported from the PFTrack scene. 
   */ 
  private String pDistortedGridNodeName; 

  /**
   * Miscellaneous Nuke script fragements used in the undistort process.
   */ 
  private String pGridGradeWarpNodeName; 
  private String pGridGradeDiffNodeName;
  private String pBlackOutsideNodeName; 

  /**
   * The fully resolved name of the node containing a Nuke script used by
   * artists to manually match the distortion of the PFTrack exported grid with the 
   * original reference grid.
   */ 
  private String pGridAlignNodeName;


}
