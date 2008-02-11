// $Id: PlatesBuilder.java,v 1.11 2008/02/11 22:59:23 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L A T E S   D E F I N I T I O N S                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes associated with the Plates task.<P> 
 * 
 * Contains the scanned plate images, camera data and any other reference images shot on 
 * set.  Any required painting fixes are applied and then the images are undistored and 
 * linearized.  A GridWarp Nuke node is produced which can be used to redistort rendered
 * images later along with a MEL script to set the undistored image resolution for renders.
 * Finally, the undistored plates are resized down to 1k, a QuickTime movie is built and 
 * a thumbnail image is extracted. 
 */
public 
class PlatesBuilder 
  extends ICTaskBuilder 
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
          "Builder to construct basic  network.",
          mclient, qclient, builderInfo);

    /* initialize fields */ 
    {
      pStudioDefs = studioDefs;
      pProjectNamer = projectNamer;
      if(pProjectNamer == null) 
	pProjectNamer = new ProjectNamer(mclient, qclient, pStudioDefs);	
      pShotNamer = shotNamer;

      pRequiredNodeNames = new TreeSet<String>(); 
      pMiscReferenceNodeNames = new TreeSet<String>(); 

      pFinalStages  = new ArrayList<FinalizableStage>(); 
    }

    /* setup builder parameters */ 
    {
      /* select the project, sequence and shot for the task */ 
      {
        UtilityParam param = 
          new DoubleMapUtilityParam
          (aLocation, 
           "The Project, Sequence, and Shot names.",
           StudioDefinitions.aProjectName,
           "Select the name of the project.",
           StudioDefinitions.aSequenceName,
           "Select the name of the shot sequence.",
           StudioDefinitions.aShotName,
           "Select the name of the shot.",
           pStudioDefs.getAllProjectsAllNames());
        addParam(param);
      }
      
      {
        UtilityParam param = 
          new PlaceholderUtilityParam
          (aBackgroundPlate, 
           "Select the existing scanned images node to use as the background plates for " + 
           "this shot."); 
        addParam(param);
      }

      addCheckinWhenDoneParam();
    }
     
    /* if no parent builder has already generated the names for ProjectNamer, 
         this builder should take over control of naming the project */ 
    if(!pProjectNamer.isGenerated()) {
      addSubBuilder(pProjectNamer);

      /* link the nested ProjectName parameter inside the complex parameter Location
           (of this builder) with the simple parameter ProjectName of the ProjectNamer */ 
      addMappedParam(pProjectNamer.getName(), 
                     new ParamMapping(StudioDefinitions.aProjectName), 
                     new ParamMapping(aLocation, StudioDefinitions.aProjectName)); 
    }
    
    /* create the setup passes */ 
    {
      addSetupPass(new SetupShotEssentials());
      addSetupPass(new SetupImageParams());
      addSetupPass(new GetPrerequisites());
    }
    
    /* setup the default editors */ 
    {
      setDefaultEditor(ICStageFunction.aNone, null);

      setDefaultEditor(ICStageFunction.aMayaScene, new PluginContext("MayaProject"));

      setDefaultEditor(ICStageFunction.aTextFile, new PluginContext("NEdit"));
      setDefaultEditor(ICStageFunction.aScriptFile, new PluginContext("NEdit"));

      setDefaultEditor(ICStageFunction.aRenderedImage, new PluginContext("NukeViewer"));
      setDefaultEditor(ICStageFunction.aSourceImage, new PluginContext("NukeViewer"));
      setDefaultEditor(ICStageFunction.aNukeScript, new PluginContext("Nuke"));

      setDefaultEditor(ICStageFunction.aQuickTime, new PluginContext("QuickTime")); 

      setDefaultEditor(ICStageFunction.aPFTrackScene, new PluginContext("PFTrack", "ICVFX"));
    }

    /* create the construct passes */ 
    {
      ConstructPass build = new BuildNodesPass();
      addConstructPass(build);
      
      ConstructPass qdc = new QueueDisableCleanupPass(); 
      addConstructPass(qdc); 
      addPassDependency(build, qdc);

      ConstructPass qd = new QueueDisablePass(); 
      addConstructPass(qd); 
      addPassDependency(qdc, qd);
    }

    /* specify the layout of the parameters for each pass in the UI */ 
    {
      PassLayoutGroup layout = new PassLayoutGroup();

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
  protected MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();	
    plugins.add(new PluginContext("LensInfo", "ICVFX"));	
    //plugins.add(new PluginContext("PFTrackBuild", "ICVFX"));	
    plugins.add(new PluginContext("Touch")); 
    plugins.add(new PluginContext("Copy"));   		
    plugins.add(new PluginContext("NukeCatComp")); 		
    plugins.add(new PluginContext("NukeExtract"));		
    plugins.add(new PluginContext("NukeQt"));			
    plugins.add(new PluginContext("NukeThumbnail"));		
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
  class SetupShotEssentials
  extends SetupPass
  {
    public 
    SetupShotEssentials()
    {
      super("Setup Shot Essentials", 
            "Set the common builder properties as well as essential stuff for all shots" + 
            "like project, sequence and shot names.");
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
      /* sets up the built-in parameters common to all builders */ 
      validateBuiltInParams();

      /* setup the StudioDefinitions version of the UtilContext */ 
      pStudioDefs.setContext(pContext);  
      
      /* lookup the selected sequence/shot from the builder's Location parameter, 
	   we'll need this in the initPhase() to initialize the ShotNamer */ 
      {
        ParamMapping seqMapping = 
          new ParamMapping(aLocation, StudioDefinitions.aSequenceName);
        pSequenceName = getStringParamValue(seqMapping);

        ParamMapping shotMapping = 
          new ParamMapping(aLocation, StudioDefinitions.aShotName);
        pShotName = getStringParamValue(shotMapping);
      }

      /* register the project-wide required nodes */ 
      {
	pPlatesOriginalGridNodeName = pProjectNamer.getPlatesOriginalGridNode();
	pRequiredNodeNames.add(pPlatesOriginalGridNodeName); 

	pGridGradeWarpNodeName = pProjectNamer.getGridGradeWarpNode();
	pRequiredNodeNames.add(pGridGradeWarpNodeName); 
	
	pGridGradeDiffNodeName = pProjectNamer.getGridGradeDiffNode();
	pRequiredNodeNames.add(pGridGradeDiffNodeName); 
	
	pBlackOutsideNodeName = pProjectNamer.getBlackOutsideNode();
	pRequiredNodeNames.add(pBlackOutsideNodeName); 
      }

      /* turn on the DoAnnotations flag for the StageInformation shared by all 
         of the Stages created by this builder since we always want task annotations */
      pStageInfo.setDoAnnotations(true);
    }
    
    /**
     * Phase in which new Sub-Builders should be created and added to the current Builder.
     */ 
    @Override
    public void 
    initPhase() 
      throws PipelineException 
    {
      /* if we haven't been passed in a ShotNamer from a parent builder, make one now */ 
      if(pShotNamer == null) 
        pShotNamer = new ShotNamer(pClient, pQueue, pStudioDefs);

      /* if no parent builder as already generated and initialized the ShotNamer, 
	   lets create one ourselves... */ 
      if(!pShotNamer.isGenerated()) {
        addSubBuilder(pShotNamer);
        
        /* always link the nested ProjectName parameter inside the complex parameter 
             Location (of this builder) with the simple ProjectName parameter of the 
	     ShotNamer */
        addMappedParam
          (pShotNamer.getName(), 
           new ParamMapping(StudioDefinitions.aProjectName), 
           new ParamMapping(aLocation, StudioDefinitions.aProjectName));

        /* if we are NOT creating a new shot, 
             then link the nested SequenceName parameter inside the complex parameter 
             Location (of this builder) with the simple SequenceName parameter of the 
             ShotNamer */ 
        if(!pSequenceName.equals(StudioDefinitions.aNEW))  {
          addMappedParam
            (pShotNamer.getName(), 
             new ParamMapping(StudioDefinitions.aSequenceName), 
             new ParamMapping(aLocation, StudioDefinitions.aSequenceName));
        }

        /* if we are NOT creating a new shot, 
             then link the nested ShotName parameter inside the complex parameter 
             Location (of this builder) with the simple ShotName parameter of the 
             ShotNamer */ 
        if(!pShotName.equals(StudioDefinitions.aNEW))  {
          addMappedParam
            (pShotNamer.getName(), 
             new ParamMapping(StudioDefinitions.aShotName), 
             new ParamMapping(aLocation, StudioDefinitions.aShotName));
        }
      }
    }
    
    private static final long serialVersionUID = -3841709462425715524L;

    private String pSequenceName;
    private String pShotName;
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
	
	NodeVersion vsn = pClient.getCheckedInVersion(path.toString(), null); 
	if(vsn == null) 
	  throw new PipelineException
	    ("Somehow no checked-in version of the " + aBackgroundPlate + " node " + 
	     "(" + path + ") exists!"); 

	pFrameRange = vsn.getPrimarySequence().getFrameRange(); 

        pBackgroundPlateNodeName = vsn.getName(); 
        pRequiredNodeNames.add(pBackgroundPlateNodeName); 
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
      /* lock the latest version of all of the prerequisites */ 
      for(String name : pRequiredNodeNames) {
	if(!nodeExists(name)) 
	  throw new PipelineException
	    ("The required prerequisite node (" + name + ") does not exist!"); 
	lockLatest(name); 
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
	    new TargetStage(pStageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  LensInfoStage stage = 
	    new LensInfoStage(pStageInfo, pContext, pClient, 
			      vfxShotDataNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	}

	String solveDistortionNodeName = pShotNamer.getSolveDistortionNode(); 
	{
	  PFTrackBuildStage stage = 
	    new PFTrackBuildStage(pStageInfo, pContext, pClient, 
				  solveDistortionNodeName, pPlatesOriginalGridNodeName, 
				  pBackgroundPlateNodeName, vfxShotDataNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build();  
	  addToDisableList(solveDistortionNodeName);
	}
	
	pDistortedGridNodeName = pShotNamer.getDistortedGridNode(); 
	{
	  DistortedGridStage stage = 
	    new DistortedGridStage(pStageInfo, pContext, pClient, 
				   pDistortedGridNodeName, pPlatesOriginalGridNodeName, 
				   solveDistortionNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	  pFinalStages.add(stage);
	}

	String readDistortedNodeName = pShotNamer.getReadDistortedNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(pStageInfo, pContext, pClient, 
			      readDistortedNodeName, pDistortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	String reformatOriginalNodeName = pShotNamer.getReformatOriginalNode(); 
	{
	  NukeReadReformatStage stage = 
	    new NukeReadReformatStage(pStageInfo, pContext, pClient, 
				      reformatOriginalNodeName, 
				      pPlatesOriginalGridNodeName, pDistortedGridNodeName); 
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
	    new NukeCatStage(pStageInfo, pContext, pClient, 
			     pGridAlignNodeName, sources); 
	  stage.addLink(new LinkMod(pPlatesOriginalGridNodeName, LinkPolicy.Reference));
	  stage.addLink(new LinkMod(pDistortedGridNodeName, LinkPolicy.Reference));
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	}
	
	String gridAlignImageNodeName = pShotNamer.getGridAlignImageNode();
	{
	  LinkedList<String> nukeScripts = new LinkedList<String>(); 
	  nukeScripts.add(pGridAlignNodeName); 

	  NukeCatCompStage stage = 
	    new NukeCatCompStage(pStageInfo, pContext, pClient,
				 gridAlignImageNodeName, "tif", nukeScripts);  
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String gridAlignThumbNodeName = pShotNamer.getGridAlignThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(pStageInfo, pContext, pClient,
				   gridAlignThumbNodeName, "tif", gridAlignImageNodeName, 
				   1, 150, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}

	String submitNodeName = pShotNamer.getPlateSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(gridAlignThumbNodeName);
	  sources.add(vfxRefNodeName);

	  TargetStage stage = 
	    new TargetStage(pStageInfo, pContext, pClient, 
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
	    new TargetStage(pStageInfo, pContext, pClient, 
			    vfxRefNodeName, pMiscReferenceNodeNames); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String lensWarpNodeName = pShotNamer.getLensWarpNode(); 
	{
	  NukeExtractStage stage = 
	    new NukeExtractStage(pStageInfo, pContext, pClient, 
				 lensWarpNodeName, pGridAlignNodeName, 
				 "GridWarp", ".*", false); 
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}
        
	String reformatBgNodeName = pShotNamer.getReformatBgNode(); 
	{
	  NukeReadReformatStage stage = 
	    new NukeReadReformatStage(pStageInfo, pContext, pClient, 
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
	    new NukeCatCompStage(pStageInfo, pContext, pClient,
				 undistorted2kPlateNodeName, pFrameRange, 4, "tif", 
				 nukeScripts, inputImages);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String undistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
	{
	  NukeRescaleStage stage = 
	    new NukeRescaleStage(pStageInfo, pContext, pClient,
				 undistorted1kPlateNodeName, pFrameRange, 4, "tif",
				 undistorted2kPlateNodeName, 0.5);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String undistorted1kQuickTimeNodeName = pShotNamer.getUndistorted1kQuickTimeNode(); 
	{
	  NukeQtStage stage = 
	    new NukeQtStage(pStageInfo, pContext, pClient,
			    undistorted1kQuickTimeNodeName, undistorted1kPlateNodeName, 24.0);
	  addTaskAnnotation(stage, NodePurpose.Product); 
	  stage.build(); 
	}

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  addTaskAnnotation(vfxShotDataNodeName, NodePurpose.Product);   
	}
	
	String approveNodeName = pShotNamer.getPlateApproveNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(undistorted1kQuickTimeNodeName);
	  sources.add(vfxRefNodeName);
	  sources.add(vfxShotDataNodeName);

	  TargetStage stage = 
	    new TargetStage(pStageInfo, pContext, pClient, 
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
  
  public final static String aLocation        = "Location";
  public final static String aReferenceImages = "ReferenceImages";
  public final static String aBackgroundPlate = "BackgroundPlate";

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;

  /**
   * Provides project-wide names of nodes and node directories.
   */ 
  private ProjectNamer pProjectNamer;

  /**
   * Provides the names of nodes and node directories which are shot specific.
   */
  private ShotNamer pShotNamer;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of nodes required to exist for this builder to run. 
   */ 
  private TreeSet<String> pRequiredNodeNames;

  /**
   * The stages which require running their finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 


  /*-- PLATE PREREQUISITES -----------------------------------------------------------------*/

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
   * The fully resolved name of the original undistored grid node.
   */ 
  private String pPlatesOriginalGridNodeName; 

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
