package com.nathanlove.pipeline.plugin.InternalCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_28.*;
import us.temerity.pipeline.builder.v2_4_28.TaskBuilder;
import us.temerity.pipeline.stages.*;

public 
class NewTaskTestBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  NewTaskTestBuilder 
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation
  )
    throws PipelineException
  {
    super("NewTaskTest", "Test Builder for the new task system", mclient, qclient, 
          builderInformation, EntityType.Asset);
    
    addSetupPass(new InfoPass());
    addConstructPass(new BuildPass());

    addCheckinWhenDoneParam();
    
    AdvancedLayoutGroup layout = 
      new AdvancedLayoutGroup
      ("Builder Information", 
       "The pass where all the basic information about the asset is collected " +
       "from the user.", 
       "BuilderSettings", 
       true);
    layout.addEntry(1, aUtilContext);
    layout.addEntry(1, null);
    layout.addEntry(1, aCheckinWhenDone);
    layout.addEntry(1, aActionOnExistence);
    layout.addEntry(1, aReleaseOnError);
  
    PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
    setLayout(finalLayout);
  }
  

  private 
  class InfoPass
    extends SetupPass
  {
    public 
    InfoPass()
    {
      super("Info Pass", 
            "Info pass for the new Task Test Builder");
      
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      
      setTaskInformation("newTask", "char", "bob", TaskType.Asset.toString());
      
      pStartPath = new Path(new Path(new Path(new Path(
        new Path("/projects"), getProjectName()), "assets"), getTaskIdent1()), 
                  getTaskIdent2());
      
    }
    
    private static final long serialVersionUID = -1858360649024397969L;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/

  
  private
  class BuildPass
    extends ConstructPass
  {
    public 
    BuildPass()
    {
      super("Build Pass", 
            "The Pass which constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      
      {
        String runVerifyNode = getDefaultVerifyBuilderNodeName();
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, runVerifyNode, "txt");
        addTaskAnnotation(stage, NodePurpose.Execution);
        stage.build();
        addToQueueList(runVerifyNode);
        addToCheckInList(runVerifyNode);
      }
      

      {
        String runPublishNode = getDefaultPublishBuilderNodeName();
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, runPublishNode, "txt");
        addTaskAnnotation(stage, NodePurpose.Execution);
        stage.build();
        addToQueueList(runPublishNode);
        addToCheckInList(runPublishNode);
      }
      
      
      String namePrefix = getTaskIdent1() + "_" + getTaskIdent2();
      
      String editNode = 
        new Path(new Path(pStartPath, "work"), namePrefix + "_edit").toString();
      
      {
        EmptyFileStage stage = 
          new EmptyFileStage(stageInfo, pContext, pClient, editNode, "ma");
        addTaskAnnotation(stage, NodePurpose.Edit);
        stage.build();
      }
      
      String submitNode = 
        new Path(pStartPath, namePrefix + "_submit").toString();
      {
        TargetStage stage = 
          new TargetStage(stageInfo, pContext, pClient, submitNode, editNode);
        addSubmitTaskAnnotation(stage);
        stage.build();
        addToQueueList(submitNode);
        addToCheckInList(submitNode);
      }

      String verifyEditNode = 
        new Path(new Path(new Path(getDefaultTemerityPath(), "verify"), "scenes"), 
                                   namePrefix + "_verified").toString();
      {
        ProductStage stage = 
          new ProductStage(stageInfo, pContext, pClient, verifyEditNode, "ma", editNode, StageFunction.aMayaScene);
        addTaskAnnotation(stage, NodePurpose.Prepare);
        stage.build();
      }
      
      String verifyNode = getDefaultVerifyNodeName();
      {
        TargetStage stage = 
          new TargetStage(stageInfo, pContext, pClient, verifyNode, verifyEditNode);
        addVerifyTaskAnnotation(stage);
        stage.build();
        addToQueueList(verifyNode);
        addToCheckInList(verifyNode);  
      }
      
      String publishedNode = 
        new Path(new Path(getDefaultTemerityPath(), "products"), namePrefix).toString();
      
      {
        ProductStage stage = 
          new ProductStage(stageInfo, pContext, pClient, publishedNode, "ma", verifyEditNode, StageFunction.aMayaScene);
        addTaskAnnotation(stage, NodePurpose.Product);
        stage.build();
      }
      
      String publishNode = getDefaultPublishNodeName();
      {
        TargetStage stage = 
          new TargetStage(stageInfo, pContext, pClient, publishNode, publishedNode);
        addPublishTaskAnnotation(stage);
        stage.build();
        addToQueueList(publishNode);
        addToCheckInList(publishNode);
      }
    }
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3674652865912230608L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private Path pStartPath;
}
