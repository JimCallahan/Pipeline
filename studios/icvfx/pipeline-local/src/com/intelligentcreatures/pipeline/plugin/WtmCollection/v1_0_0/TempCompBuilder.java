// $Id: TempCompBuilder.java,v 1.2 2008/04/03 10:30:47 jim Exp $

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
 * A builder for constructing the nodes associated with the TempComp task.<P> 
 * 
 * If temp renders have been enabled for the Tracking task, then the following node will
 * will be used as the source of rendered elements:<P> 
 * 
 * /projects/PROJECT/shots/SEQ/SHOT/tracking/product/render/SEQSHOT_inkblot<P> 
 * 
 * Otherwise, the rendered elements must be supplied as a previously checked-in edit node 
 * of this task called: 
 * 
 * /projects/PROJECT/shots/SEQ/SHOT/temp_comp/edit/SEQSHOT_inkblot<P> 
 * 
 * If neither node exists, then the builder will generate an error.<P> 
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
class TempCompBuilder 
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
  TempCompBuilder
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
  TempCompBuilder
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
    super("TempComp",
          "A builder for constructing the nodes associated with the TempComp task.", 
          mclient, qclient, builderInfo, studioDefs, 
	  projectNamer, shotNamer, TaskType.TempComp); 

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
      addSetupPass(new TempCompSetupShotEssentials());
      addSetupPass(new SetupImageParams());
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
    plugins.add(new PluginContext("NukeRead"));		
    plugins.add(new PluginContext("NukeSubstComp", "Temerity", 
				  new Range<VersionID>(new VersionID("2.4.3"), null)));
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
  class TempCompSetupShotEssentials
    extends BaseSetupShotEssentials
  {
    public 
    TempCompSetupShotEssentials()
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
	/* temp_comp assets */ 
	pTempCompRedistortNukeNodeName = pProjectNamer.getTempCompRedistortNukeNode();
	pRequiredNodeNames.add(pTempCompRedistortNukeNodeName); 

	pTempCompNukeNodeName = pProjectNamer.getTempCompNukeNode();
	pRequiredNodeNames.add(pTempCompNukeNodeName); 
      }
    }

    private static final long serialVersionUID = 253728508933041108L;
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

    private static final long serialVersionUID = -3556086189831350144L;
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

      /* redistort UV map */ 
      pRedistortUvImageNodeName = pShotNamer.getRedistortUvImageNode();
      pRequiredNodeNames.add(pRedistortUvImageNodeName); 

      /* rendered inkblot */ 
      {      
	String inkblotName = null;
	String trackingInkblot = pShotNamer.getTrackingInkblotNode(); 
	String tempCompInkblot = pShotNamer.getTempCompInkblotNode(); 
	if(nodeExists(trackingInkblot)) 
	  inkblotName = trackingInkblot;
	else if(nodeExists(tempCompInkblot)) 
	  inkblotName = tempCompInkblot;
	else 
	  throw new PipelineException
	    ("Unable to find the temporary inkblot rendered images node in either " + 
	     "(" + trackingInkblot + ") or (" + tempCompInkblot + ") locations!"); 

	FrameRange range = null;
	try {
	  NodeVersion vsn = pClient.getCheckedInVersion(inkblotName, null); 
	  range = vsn.getPrimarySequence().getFrameRange(); 
	}
	catch(PipelineException ex) {
	  throw new PipelineException
	    ("Somehow no checked-in version of the temporary render node " + 
	     "(" + inkblotName + ") exists!"); 
	} 
	
	if(!pFrameRange.equals(range)) 
	  throw new PipelineException
	    ("Somehow the frame range (" + range + ") of the temporary inkblot " + 
	     "rendered images node (" + inkblotName + ") did not match the frame range " + 
	     "(" + pFrameRange + ") of the selected background plates node " + 
	     "(" + pBackgroundPlateNodeName + ")!");
	
	pInkblotNodeName = inkblotName;
	pRequiredNodeNames.add(pInkblotNodeName); 
      }
    }

    private static final long serialVersionUID = 8936598986610560244L;
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
            "Creates the nodes which make up the TempComp task."); 
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

	String prereqNodeName = pShotNamer.getTempCompPrereqNode();
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

      /* add Edit annotations to the inkblot if local to the TempComp task */ 
      if(pInkblotNodeName.equals(pShotNamer.getTempCompInkblotNode())) 
	addMissingTaskAnnotation(pInkblotNodeName, NodePurpose.Edit); 

      /* the submit network */
      {	
	String tempCompReadInkblotNodeName = pShotNamer.getTempCompReadInkblotNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(stageInfo, pContext, pClient, 
			      tempCompReadInkblotNodeName, pInkblotNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String tempCompReadRedistortNodeName = pShotNamer.getTempCompReadRedistortNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage(stageInfo, pContext, pClient, 
			      tempCompReadRedistortNodeName, pRedistortUvImageNodeName, 
			      "Nearest Frame");  
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String tempCompInkblotRedistortedNodeName = 
	  pShotNamer.getTempCompInkblotRedistortedNode();
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>(); 
	  subst.put(tempCompReadInkblotNodeName, "Inkblot"); 
	  subst.put(tempCompReadRedistortNodeName, "UV_RD_Image"); 

	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	    (stageInfo, pContext, pClient, 
	     tempCompInkblotRedistortedNodeName, pFrameRange, 4, "sgi", 
	     "Process", pTempCompRedistortNukeNodeName, 
	     subst, new PluginContext("NukeFrameCycler"));
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build(); 
	}

	String tempCompReadInkblotRedistortedNodeName = 
	  pShotNamer.getTempCompReadInkblotRedistortedNode(); 
	{
	  NukeReadStage stage = 
	    new NukeReadStage
	      (stageInfo, pContext, pClient, 
	       tempCompReadInkblotRedistortedNodeName, tempCompInkblotRedistortedNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}
	
	String tempCompReadPlatesNodeName = pShotNamer.getTempCompReadPlatesNode();
	{
	  NukeReadStage stage = 
	    new NukeReadStage
	      (stageInfo, pContext, pClient, 
	       tempCompReadPlatesNodeName, pBackgroundPlateNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Prepare); 
	  stage.build();  
	}

	String tempCompNukeEditNodeName = pShotNamer.getTempCompNukeEditNode(); 
	{
	  TreeMap<String,String> subst = new TreeMap<String,String>(); 
	  subst.put(tempCompReadInkblotRedistortedNodeName, "Foreground"); 
	  subst.put(tempCompReadPlatesNodeName, "Plates"); 

	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	      (stageInfo, pContext, pClient, 
	       tempCompNukeEditNodeName, pTempCompNukeNodeName, 
	       subst);
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  addToDisableList(tempCompNukeEditNodeName); 
	}
	
	pTempCompNodeName = pShotNamer.getTempCompNode();
	{
	  NukeSubstCompStage stage = 
	    new NukeSubstCompStage
	    (stageInfo, pContext, pClient, 
	     pTempCompNodeName, pFrameRange, 6, "cin", 
	     "Process", tempCompNukeEditNodeName, 
	     new TreeMap<String,String>(), new PluginContext("DjvView"));
	  addTaskAnnotation(stage, NodePurpose.Focus); 
	  stage.build(); 
	}

	String tempCompThumbNodeName = pShotNamer.getTempCompThumbNode();
	{
	  NukeThumbnailStage stage = 
	    new NukeThumbnailStage(stageInfo, pContext, pClient,
				   tempCompThumbNodeName, "tif", pTempCompNodeName, 
				   1, 150, 1.0, true, true, new Color3d()); 
	  addTaskAnnotation(stage, NodePurpose.Thumbnail); 
	  stage.build(); 
	}
	
	String submitNodeName = pShotNamer.getTempCompSubmitNode();
	{
	  TreeSet<String> sources = new TreeSet<String>();
	  sources.add(tempCompThumbNodeName);

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
 	String approveNodeName = pShotNamer.getTempCompApproveNode();
 	{
 	  TreeSet<String> sources = new TreeSet<String>();
 	  sources.add(pTempCompNodeName);

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

    private static final long serialVersionUID = -7616286859313337411L;
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
 
    private static final long serialVersionUID = 8379170004405281872L;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2778378863778159912L;

  public final static String aBackgroundPlate = "BackgroundPlate";
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing Nuke script used to redistort
   * CG elements to match the original plates.
   */ 
  private String pTempCompRedistortNukeNodeName; 

  /**
   * The fully resolved name of the node containing Nuke script used to perform
   * the temp comp.
   */ 
  private String pTempCompNukeNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a thumbnail image extracted
   * from the temp composited images.
   */ 
  private String pTempCompNodeName; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name and frame range of background plates node. 
   */ 
  private String pBackgroundPlateNodeName;
  private FrameRange pFrameRange;

  /**
   * The fully resolved name of the temporary inkblot rendered images node.
   */ 
  private String pInkblotNodeName; 

  /**
   * The fully resolved name of the node containing redistorted UV image rendered
   * by Houdni.
   */
  private String pRedistortUvImageNodeName; 

}
