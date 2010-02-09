// $Id: ProjectBuilder.java,v 1.2 2008/06/26 20:45:55 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

import com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   B U I L D E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder to create the initial set of scripts and scene files that projects being done with
 * the Nathan Love Base Collection need to function.
 */
public 
class ProjectBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Default constructor that allows for standalone invocation.
   */
  public
  ProjectBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
   this(mclient, qclient, builderInformation, 
        new StudioDefinitions(mclient, qclient, 
          UtilContext.getDefaultUtilContext(mclient), builderInformation.getLoggerName()));
  }
  
  public
  ProjectBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation,
    StudioDefinitions studioDefinitions
  ) 
    throws PipelineException
  {
    super("Project",
          "The Project Builder that works with the basic Temerity Project Names class.",
          mclient,
          qclient,
          builderInformation);
    
    pStudioDefs = studioDefinitions;
    
    {
      UtilityParam param = 
        new StringUtilityParam
        (ParamNames.aProjectName,
         "The name of the project that is being setup.", 
         "projects"); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aModifyExisting,
         "If the named project already exists, should this builder go ahead and run anyway." +
         "This can change behavior of existing networks, so should be used carefully.", 
         false); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new MayaContextUtilityParam
        (ParamNames.aMayaContext,
         "The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
         new MayaContext()); 
      addParam(param);
    }
    
    /* whether to checkin the newly created nodes */ 
    addCheckinWhenDoneParam();
    
    
    pProjectNamer = new ProjectNamer(mclient, qclient, builderInformation); 
    addSubBuilder(pProjectNamer);
    addMappedParam(pProjectNamer.getName(), ParamNames.aProjectName, ParamNames.aProjectName);    
    
    //setDefaultEditors();
    
    addSetupPass(new SetupEssentialsPass());
    addConstructPass(new MakeNodesPass());
    
    setDefaultEditors(StudioDefinitions.getDefaultEditors());
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Project Information", 
           "The pass where all the basic stageInformation about the asset is collected " +
           "from the user.", 
           "ProjectInformation", 
           true);
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aActionOnExistence);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, aCheckinWhenDone);
      layout.addEntry(1, null);
      layout.addEntry(1, ParamNames.aProjectName);
      layout.addEntry(1, aModifyExisting);
      layout.addEntry(1, null);
      layout.addEntry(1, ParamNames.aMayaContext);
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  V E R I F I C A T I O N                                                               */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    MappedArrayList<String, PluginContext> toReturn = 
      new MappedArrayList<String, PluginContext>();
    
    String toolset = getToolset();
    
    toReturn.put(toolset, new PluginContext("MayaRemoveRefMEL"));
    toReturn.put(toolset, new PluginContext("MayaShdCopyMEL"));
    toReturn.put(toolset, new PluginContext("Touch"));
    toReturn.put(toolset, new PluginContext("CatFiles"));
    toReturn.put(toolset, new PluginContext("MayaRenderGlobals"));
    toReturn.put(toolset, new PluginContext("MRayRenderGlobals"));
    
    
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  private
  class SetupEssentialsPass
    extends SetupPass
  {

    private
    SetupEssentialsPass()
    {
      super("Setup Essentials", 
            "Setup the common builder properties.");
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
      
      pMayaContext = (MayaContext) getParamValue(ParamNames.aMayaContext);

      pProjectName = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
      if (!getBooleanParamValue(new ParamMapping(aModifyExisting))) {
        ArrayList<String> projects = pStudioDefs.getProjectList();
        if (projects.contains(pProjectName))
          throw new PipelineException
          ("The project (" + pProjectName + ") already exists.  " +
           "Cannot use the project builder to create it when the " +
           "Modify Existing flag is not turned on.");
      }
      
      pEmptyFiles = new LinkedList<BaseStage>();
    }
    
    private static final long serialVersionUID = 2453800092783810792L;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private
  class MakeNodesPass
    extends ConstructPass
  {
    private 
    MakeNodesPass()
    {
      super("Make Nodes", 
            "Creates the node networks.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      buildGeneralNodes();
      buildModelNodes();
      buildRigNodes();
      buildShaderNodes();
      buildLightingNodes();
    }

    private void 
    buildGeneralNodes()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      
      /* Circle Turntable Setup Script */
      {
        String script = pProjectNamer.getCircleTurntableMEL();
        PlaceholderTTStage stage = 
          new PlaceholderTTStage(stageInfo, pContext, pClient, script, 
                                 PlaceholderTTStage.TTType.Circle);
        if (stage.build())
          addToCheckInList(script);
      }
      
      /* Center Turntable Setup Script */
      {
        String script = pProjectNamer.getCenterTurntableMEL();
        PlaceholderTTStage stage = 
          new PlaceholderTTStage(stageInfo, pContext, pClient, script, 
                                 PlaceholderTTStage.TTType.Center);
        if (stage.build())
          addToCheckInList(script);
      }
      
      /* Remove Reference MEL */
      {
        String script = pProjectNamer.getRemoveReferenceMEL();
        MELFileStage stage = 
          new MELFileStage(stageInfo, pContext, pClient, script, "MayaRemoveRefMEL");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
        }
      }
      
      /* Asset verification*/
      {
        String script = pProjectNamer.getAssetVerificationMEL();
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
          pEmptyFiles.add(stage);
        }
      }
      
      /* Camera Placeholder*/
      {
        String script = pProjectNamer.getCameraPlaceholderMEL();
        CameraPlaceholderMELStage stage = 
          new CameraPlaceholderMELStage(stageInfo, pContext, pClient, script);
        if (stage.build())
          addToCheckInList(script);
      }
    }
    
    private void 
    buildModelNodes()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      
      /* Model placeholder*/
      {
        String script = pProjectNamer.getModelPlaceholderMEL();
        PlaceholderMELStage stage = 
          new PlaceholderMELStage(stageInfo, pContext, pClient, script);
        if (stage.build())
          addToCheckInList(script);
      }
      
     /* Model verification*/
      {
        String script = pProjectNamer.getModelVerificationMEL();
        VerifyModelMELStage stage = 
          new VerifyModelMELStage(stageInfo, pContext, pClient, script);
        boolean build = stage.build();
        if (build)
          addToCheckInList(script);
      }
    }
    
    private void 
    buildRigNodes()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      LinkedList<String> collectedScripts = new LinkedList<String>();
      
      /* Rig Verification */
      {
        String script = pProjectNamer.getRigVerificationMEL();
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
          pEmptyFiles.add(stage);
        }
        collectedScripts.add(script);
      }
      
      /* Rig Finalize*/
      for (AssetType type : AssetType.values()) {
        String script = pProjectNamer.getRigFinalizeMEL(type);
        CatFilesStage stage = 
          new CatFilesStage(stageInfo, pContext, pClient, script, "mel", collectedScripts);
        boolean build = stage.build();
        if (build) {
          addToCheckInList(script);
          addToQueueList(script);
        }
      }
    }
    
    private void 
    buildShaderNodes()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      LinkedList<String> collectedScripts = new LinkedList<String>();
      
      /* Shader Copy*/
      {
        String script = pProjectNamer.getShaderCopyMEL();
        MELFileStage stage = 
          new MELFileStage(stageInfo, pContext, pClient, script, "MayaShdCopyMEL");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
        }
        collectedScripts.add(script);
      }
      
      /* Shader Verification */
      {
        String script = pProjectNamer.getShadeVerificationMEL();
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
          pEmptyFiles.add(stage);
        }
      }
      
      collectedScripts.add(pProjectNamer.getRemoveReferenceMEL());
      
      /* Shader Finalize */
      for (AssetType type : AssetType.values()) {
        String script = pProjectNamer.getShadeFinalizeMEL(type);
        CatFilesStage stage = 
          new CatFilesStage(stageInfo, pContext, pClient, script, "mel", collectedScripts);
        boolean build = stage.build();
        if (build) {
          addToCheckInList(script);
          addToQueueList(script);
        }
      }
    }
    
    private void 
    buildLightingNodes()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      
      {
        String script = pProjectNamer.getLightingMayaGlobalsMEL();
        MELFileStage stage =
          new MELFileStage(stageInfo, pContext, pClient, script, "MayaRenderGlobals");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
        }
      }
      
      {
        String script = pProjectNamer.getLightingMRayGlobalsMEL();
        MELFileStage stage =
          new MELFileStage(stageInfo, pContext, pClient, script, "MRayRenderGlobals");
        if (stage.build()) {
          addToCheckInList(script);
          addToQueueList(script);
        }
      }
      
      {
        String script = pProjectNamer.getLightingProductMEL();
        LightingProductMELStage stage =
          new LightingProductMELStage(stageInfo, pContext, pClient, script);
        if (stage.build()) {
          addToCheckInList(script);
        }
      }
    }
    
    private static final long serialVersionUID = -3538851156066717973L;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * new
   */
  private static final long serialVersionUID = -6716887847852990391L;
  
  private static final String aModifyExisting = "ModifyExisting";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Provides a set of studio-wide helpers for project, sequence and shot naming.
   */ 
  private StudioDefinitions pStudioDefs;
  
  /**
   * Provides project-wide names of nodes and node directories.
   */ 
  private ProjectNamer pProjectNamer;
  
  private String pProjectName;
  
  private MayaContext pMayaContext;
  
  private LinkedList<BaseStage> pEmptyFiles; 
}
