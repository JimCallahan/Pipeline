// $Id: InternalTrackingBuilder.java,v 1.4 2008/03/04 08:15:16 jesse Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E R N A L   T R A C K I N G   B U I L D E R                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A builder for constructing the nodes required to generate the tracked camera/model 
 * Maya scene internally at IC required by the Tracking task.<P> 
 * 
 * When the Maya tracking scene will be generated internally at IC using PFTrack instead of 
 * being supplied by an outsourcer, this builder should be used to create the small node 
 * network required to generate this tracking scene.  The network created includes the 
 * automatically generated target Maya scene, the PFTrack scene used to do the tracking and
 * the source undistorted plate images and on-set data needed by PFTrack.<P> 
 * 
 * When the Maya tracking scene is created by an outsourcer, this builder does not need to 
 * be run.  Instead, you merely need to register and check-in this outsourcer provided 
 * tracking scene before running the main Tracking builder.<P> 
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
class InternalTrackingBuilder 
  extends BaseTrackingBuilder 
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
  InternalTrackingBuilder
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
  InternalTrackingBuilder
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
    super("InternalTracking",
	  "A builder for constructing the nodes required to generate the tracked " + 
	  "camera/model Maya scene internally at IC required by the Tracking task.", 
          mclient, qclient, builderInfo, studioDefs, projectNamer, shotNamer);

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
    //plugins.add(new PluginContext("PFTrackExportMaya", "ICVFX"));	
    plugins.add(new PluginContext("Touch")); 		

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
      pRorschachTrackPlaceholderNodeName = pProjectNamer.getRorschachTrackPlaceholderNode(); 
      pRequiredNodeNames.add(pRorschachTrackPlaceholderNodeName); 
    }
    
    private static final long serialVersionUID = 2837229513090774291L;
  }

    
  /*----------------------------------------------------------------------------------------*/

  protected
  class SetupTrackingEssentials
    extends BaseSetupTrackingEssentials
  {
    public 
    SetupTrackingEssentials()
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
	pVfxShotDataNodeName = pShotNamer.getVfxShotDataNode(); 
	pRequiredNodeNames.add(pVfxShotDataNodeName); 
	
	pUndistorted1kPlateNodeName = pShotNamer.getUndistorted1kPlateNode(); 
	pRequiredNodeNames.add(pUndistorted1kPlateNodeName); 
      }
    }
    
    private static final long serialVersionUID = 5839033369301514231L;
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
            "Creates the nodes required for the Tracking task."); 
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

	String prereqNodeName = pShotNamer.getInternalTrackingPrereqNode();
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

      /* the node network */
      {
	String pftrackNodeName = pShotNamer.getPFTrackNode(); 
	{
	  PFTrackBuildStage stage = 
	    new PFTrackBuildStage(stageInfo, pContext, pClient, 
				  pftrackNodeName, pUndistorted2kPlateNodeName, 
				  pVfxShotDataNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build();  
	  addToDisableList(pftrackNodeName);
	}
	
	String trackNodeName = pShotNamer.getTrackNode(); 
	{
	  PFTrackExportMayaStage stage = 
	    new PFTrackExportMayaStage(stageInfo, pContext, pClient, 
				       trackNodeName, pRorschachTrackPlaceholderNodeName, 
				       pftrackNodeName, pUndistorted1kPlateNodeName); 
	  addTaskAnnotation(stage, NodePurpose.Edit); 
	  stage.build(); 
	  pFinalStages.add(stage);
	  addToQueueList(trackNodeName);
	  addToCheckInList(trackNodeName); 
	}
      }
    }

    private static final long serialVersionUID = -2330684399337736948L; 
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
    
    private static final long serialVersionUID = 2333465496302551900L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6859395400895692118L; 
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The stages which require running their finalizeStage() method before check-in.
   */ 
  private ArrayList<FinalizableStage> pFinalStages; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the node containing a placeholder Maya scene which will 
   * eventually contain the camera/model tracking data exported from PFTrack.
   */ 
  private String pRorschachTrackPlaceholderNodeName; 

  /**
   * The fully resolved name of the grouping node for all existing miscellanous 
   * on-set reference images. 
   */ 
  private String pVfxShotDataNodeName; 
  
  /**
   * The fully resolved name of the node containing the undistorted/linearized
   * ~2k plate images.
   */ 
  private String pUndistorted1kPlateNodeName; 

}
