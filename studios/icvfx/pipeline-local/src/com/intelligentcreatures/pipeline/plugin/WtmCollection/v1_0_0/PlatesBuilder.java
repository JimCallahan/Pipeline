// $Id: PlatesBuilder.java,v 1.5 2008/02/06 16:29:48 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
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
  public PlatesBuilder
  (
   MasterMgrClient mclient,
   QueueMgrClient qclient,
   BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, qclient, info, 
         new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         null, null);
  }
  
  /**
   * Provided to allow parent builders to create instances and share.
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
   * 
   */ 
  public PlatesBuilder
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

      pFinalStages = new ArrayList<FinalizableStage>(); 

      // ... 
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
           "Select the name of the shot sequence or [[NEW]] to create a new sequence.",
           StudioDefinitions.aShotName,
           "Select the name of the shot or [[NEW]] to create a new shot.",
           pStudioDefs.getAllProjectsAllNamesForParam());
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

      /* add the ProjectNamer as a sub-builder */  
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

      setDefaultEditor(ICStageFunction.aPFTrackScene, new PluginContext("PFTrack", "ICVFX"));
    }

    /* create the construct passes */ 
    {
      ConstructPass build = new BuildPass();
      addConstructPass(build);
      
      ConstructPass end = new FinalizePass();
      addConstructPass(end);
      
      addPassDependency(build, end);
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
   * Returns the names of nodes that this Builder will check-in.
   */
  @Override
  protected LinkedList<String> 
  getNodesToCheckIn() 
  {
    return getCheckInList();
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
      /* sets up the built-in parameters common to all builders,
           TODO: shouldn't this be in BaseBuilder itself?! */ 
      validateBuiltInParams();

      /**
       * NOTE: This is commented because the UtilContext is not actually needed by 
       * StudioDefinitions as currently written.  However, if StudioDefinitions did require 
       * access to the UtilContext it will have to be initialized here! 
       */ 
      pStudioDefs.setContext(pContext);  
      
      /* lookup the name of the selected project, sequence and shot from the 
           builder's Location parameter */ 
      {
        ParamMapping mapping = 
          new ParamMapping(aLocation, StudioDefinitions.aSequenceName);
        pSequenceName = getStringParamValue(mapping);
      }
      
      {
        ParamMapping mapping = 
          new ParamMapping(aLocation, StudioDefinitions.aShotName);
        pShotName = getStringParamValue(mapping);
      }

      /* add any required placeholders or other common shared stuff here.. */ 
      {
        //pRequiredNodes.add(/* full node name */); 
      }

      /* lookup whether we should check-in the nodes we've created at the end */ 
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
    
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

      /* if no parent builder as already generated the shot names... */ 
      if(!pShotNamer.isGenerated()) {

        /* add the ShotNamer as a sub-builder */ 
        addSubBuilder(pShotNamer);
        
        /* always link the nested ProjectName parameter inside the complex parameter 
             Location (of this builder) with the simple ProjectName parameter of the 
             ShotNamer */
        addMappedParam
          (pShotNamer.getName(), 
           new ParamMapping(StudioDefinitions.aProjectName), 
           new ParamMapping(aLocation, StudioDefinitions.aProjectName));

        /* if we are not creating a new shot, 
             then link the nested SequenceName parameter inside the complex parameter 
             Location (of this builder) with the simple SequenceName parameter of the 
             ShotNamer */ 
        if(!pSequenceName.equals(StudioDefinitions.aNEW))  {
          addMappedParam
            (pShotNamer.getName(), 
             new ParamMapping(StudioDefinitions.aSequenceName), 
             new ParamMapping(aLocation, StudioDefinitions.aSequenceName));
        }

        /* if we are not creating a new shot, 
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
        pBackgroundPlateNodeName = path.toString(); 
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

      /* the original undistored grid node */ 
      pPlatesOriginalGridNodeName = pProjectNamer.getPlatesOriginalGridNode();
      pRequiredNodeNames.add(pPlatesOriginalGridNodeName); 

      /* misc Nuke script fragements */ 
      {
	pGridGradeWarpNodeName = pProjectNamer.getGridGradeWarpNode();
	pRequiredNodeNames.add(pGridGradeWarpNodeName); 
	
	pGridGradeDiffNodeName = pProjectNamer.getGridGradeDiffNode();
	pRequiredNodeNames.add(pGridGradeDiffNodeName); 
      }
    }

    private static final long serialVersionUID = 7830642124642481656L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   *
   */ 
  protected 
  class BuildPass
    extends ConstructPass
  {
    public 
    BuildPass()
    {
      super("Build Pass", 
            "Creates the nodes which make up the Plates task."); 
    }
    
    /**
     * 
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
      

      LockBundle bundle = new LockBundle();
      
      /* the submit network */ 
      {
	String vfxRefNodeName = buildVfxReference(NodePurpose.Prepare); 

	String vfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	{
	  LensInfoStage stage = 
	    new LensInfoStage(pStageInfo, pContext, pClient, vfxShotDataNodeName); 
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
	  addToQueueList(solveDistortionNodeName);
	  addToDisableList(solveDistortionNodeName);
	}
	
	String distortedGridNodeName = pShotNamer.getDistortedGridNode(); 
	{
	  DistortedGridStage stage = 
	    new DistortedGridStage(pStageInfo, pContext, pClient, 
				   distortedGridNodeName, pPlatesOriginalGridNodeName, 
				   solveDistortionNodeName);
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	  pFinalStages.add(stage);
	}

	String readDistortedNodeName = pShotNamer.getReadDistortedNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(pStageInfo, pContext, pClient, 
			      readDistortedNodeName, distortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	String reformatOriginalNodeName = pShotNamer.getReformatOriginalNode(); 
	{
	  NukeReadReformatStage stage = 
	    new NukeReadReformatStage(pStageInfo, pContext, pClient, 
				      reformatOriginalNodeName, 
				      pPlatesOriginalGridNodeName, distortedGridNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String gridAlignNodeName = pShotNamer.getGridAlignNode();
	{
	  LinkedList<String> sources = new LinkedList<String>(); 
	  sources.add(reformatOriginalNodeName); 
	  sources.add(pGridGradeWarpNodeName); 
	  sources.add(readDistortedNodeName); 
	  sources.add(pGridGradeDiffNodeName); 
 
	  NukeCatStage stage = 
	    new NukeCatStage(pStageInfo, pContext, pClient, gridAlignNodeName, sources); 
	  stage.addLink(new LinkMod(pPlatesOriginalGridNodeName, LinkPolicy.Reference));
	  stage.addLink(new LinkMod(distortedGridNodeName, LinkPolicy.Reference));
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build();  
	  addToQueueList(solveDistortionNodeName);
	  addToDisableList(solveDistortionNodeName);
	}



	// /prod/wtm/ri/120/plates/focus/ri120_grid_align

        /* 

        Stage stage = new Stage(...  rigSource); 
        addPrepareAnnotation(stage, taskType);
        stage.build();
        addToQueueList(rigSource);
        addToCheckInList(rigSource);
        bundle.addNodeToLock(rigSource);
        
        
        Stage stage = new Stage(...  rigSubmit); 
        addSubmitAnnotation(stage, taskType);
        stage.build();
        addToQueueList(rigSubmit);
        addToCheckInList(rigSubmit);
        bundle.addNodeToCheckin(rigSubmit);
        
        
        ....

        */ 


      }

      /* the approve network */ 
      {
	String vfxRefNodeName = buildVfxReference(NodePurpose.Product); 

        


      }

      addLockBundle(bundle);
    }


    /*-- STAGE HELPERS ---------------------------------------------------------------------*/

    private String
    buildVfxReference
    (
     NodePurpose purpose
    ) 
      throws PipelineException
    {
      String name = pShotNamer.getVfxReferenceNode(purpose); 

      TargetStage stage = 
	new TargetStage(pStageInfo, pContext, pClient, name, pMiscReferenceNodeNames); 
      addTaskAnnotation(stage, purpose); 
      stage.build(); 

      return name;
    }


    private static final long serialVersionUID = -5216068758078265108L;
  }
  
  /** 
   *
   */ 
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("Finalize Pass", 
	    "The SimpleAssetBuilder pass that cleans everything up.");
    }
    
    /**
     * Returns a set of nodes that have to be in a Finished queue state before the
     * buildPhase() method is called.
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
     * Remove any placeholders and temporary settings used during construction and 
     * prepare the nodes for final check-in. 
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
    
    private static final long serialVersionUID = 2931899251798393266L;
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
   * The stages which require running a finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 

  /**
   * Whether all nodes should be checked-in at the end of execution. <P> 
   * 
   * If set to (false), node locking will not be peformed.
   */ 
  private boolean pCheckInWhenDone;


  /*-- PLATE PREREQUISITES -----------------------------------------------------------------*/

  /**
   * The fully resolved name of background plates node.
   */ 
  private String pBackgroundPlateNodeName; 
  
  /**
   * The fully resolved names of all reference image nodes.
   */ 
  private TreeSet<String> pMiscReferenceNodeNames; 

  /**
   * The fully resolved name of the original undistored grid node.
   */ 
  private String pPlatesOriginalGridNodeName; 

  /**
   * Miscellaneous Nuke script fragements used in the undistort process.
   */ 
  private String pGridGradeWarpNodeName; 
  private String pGridGradeDiffNodeName; 


  // ... 


}
