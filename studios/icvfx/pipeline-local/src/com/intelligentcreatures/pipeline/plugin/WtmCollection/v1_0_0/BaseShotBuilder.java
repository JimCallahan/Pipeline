// $Id: BaseShotBuilder.java,v 1.10 2008/03/23 05:09:58 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.*; 

import java.util.*; 

/*------------------------------------------------------------------------------------------*/
/*   B A S E   S H O T   B U I L D E R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Abstract builder base class that provides the functionality common to all shot
 * related builders. 
 */
public abstract 
class BaseShotBuilder
  extends BaseTaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A protected constructor used by subclasses. 
   * 
   * @param name
   *   Name of the builder.
   * 
   * @param desc 
   *   A short description of the builder. 
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
   * @param taskType
   *   The type of task this builder is constructing.
   */
  protected 
  BaseShotBuilder
  (
    String name,
    String desc,
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInfo, 
    StudioDefinitions studioDefs,
    ProjectNamer projectNamer, 
    ShotNamer shotNamer, 
    TaskType taskType
  )
    throws PipelineException
  {
    super(name, desc, mclient, qclient, builderInfo);

    /* initialize fields */ 
    {
      pStudioDefs = studioDefs;
      pProjectNamer = projectNamer;
      if(pProjectNamer == null) 
	pProjectNamer = new ProjectNamer(mclient, qclient, pStudioDefs);	
      pShotNamer = shotNamer;
      pTaskType = taskType; 

      pRequiredNodeNames = new TreeSet<String>(); 
    }

    /* whether to checkin the newly created nodes */ 
    addCheckinWhenDoneParam();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A R A M E T E R S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper method for adding the Location parameter which includes project, sequence and 
   * shot names.
   */ 
  protected final void
  addLocationParam() 
    throws PipelineException
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
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the project namer.
   */ 
  protected final void 
  initProjectNamer() 
    throws PipelineException
  {
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
  }

   
  /*----------------------------------------------------------------------------------------*/
  /*   D E F A U L T   E D I T O R S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Setup the default editors common to all builders.
   */ 
  protected void 
  setCommonDefaultEditors() 
  {
    setDefaultEditor(ICStageFunction.aNone,          null);
    setDefaultEditor(ICStageFunction.aMayaScene,     new PluginContext("MayaProject"));
    setDefaultEditor(ICStageFunction.aTextFile,      new PluginContext("NEdit"));
    setDefaultEditor(ICStageFunction.aScriptFile,    new PluginContext("NEdit"));
    setDefaultEditor(ICStageFunction.aRenderedImage, new PluginContext("NukeViewer"));
    setDefaultEditor(ICStageFunction.aSourceImage,   new PluginContext("NukeViewer"));
    setDefaultEditor(ICStageFunction.aNukeScript,    new PluginContext("Nuke"));
    setDefaultEditor(ICStageFunction.aHDRImage,      new PluginContext("XImage")); 
    setDefaultEditor(ICStageFunction.aQuickTime,     new PluginContext("DjvView")); 
    setDefaultEditor(ICStageFunction.aQuickTimeSound, 
		     new PluginContext("smplayer", "ICVFX")); 
    setDefaultEditor(ICStageFunction.aObjModel,      new PluginContext("GPlay")); 
    setDefaultEditor(ICStageFunction.aIgesModel,     new PluginContext("GPlay")); 
    setDefaultEditor(ICStageFunction.aPFTrackScene,  new PluginContext("PFTrack", "ICVFX"));
  }


  /*----------------------------------------------------------------------------------------*/
  /*  T A S K   A N N O T A T I O N S                                                       */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Adds an ApproveTask with a specific approval builder to the set of annotation 
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
			     pTaskType.toString(), builderID);
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
                      pTaskType.toString()); 
  }

  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins on the given node. 
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
                      pTaskType.toString()); 
  }

  
  /** 
   * Adds a SubmitTask, ApproveTask or CommonTask annotation to the set of annotation 
   * plugins on the given node if it doesn't already exist. <P> 
   */ 
  protected void 
  addMissingTaskAnnotation
  (
   String nodeName, 
   NodePurpose purpose
  ) 
    throws PipelineException
  {
    addMissingTaskAnnotation(nodeName, purpose, 
			     pShotNamer.getProjectName(), pShotNamer.getFullShotName(),
			     pTaskType.toString()); 
  }
   
  

  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  protected 
  class BaseSetupShotEssentials
    extends SetupPass
  {
    public 
    BaseSetupShotEssentials()
    {
      super("Setup Shot Essentials", 
            "Setup the common builder properties as well as essential stuff for all shots" + 
            "like project, sequence and shot names.");
    }
   
    public 
    BaseSetupShotEssentials
    (
     String name, 
     String description
    )
    {
      super(name, description); 
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

      /* turn on the DoAnnotations flag for the StageInformation shared by all 
         of the Stages created by this builder since we always want task annotations */
      getStageInformation().setDoAnnotations(true);
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
  /*   C O N S T R U C T   P A S S   H E L P E R S                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Lock the latest version of all of the prerequisite nodes.
   */
  protected void 
  lockNodePrerequisites() 
    throws PipelineException
  {
    for(String name : pRequiredNodeNames) {
      if(!nodeExists(name)) 
	throw new PipelineException
	  ("The required prerequisite node (" + name + ") does not exist!"); 
      lockLatest(name); 
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*  P U B L I C   S T A T I C S                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aLocation = "Location";

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  protected StudioDefinitions pStudioDefs;

  /**
   * Provides project-wide names of nodes and node directories.
   */ 
  protected ProjectNamer pProjectNamer;

  /**
   * Provides the names of nodes and node directories which are shot specific.
   */
  protected ShotNamer pShotNamer;

  /**
   * The type of task this builder is constructing.
   */ 
  protected TaskType pTaskType; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of nodes required to exist for this builder to run. 
   */ 
  protected TreeSet<String> pRequiredNodeNames;

}
