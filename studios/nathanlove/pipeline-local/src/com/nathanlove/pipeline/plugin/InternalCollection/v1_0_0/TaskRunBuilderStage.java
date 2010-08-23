package com.nathanlove.pipeline.plugin.InternalCollection.v1_0_0;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/**
 * Stage to assign the TaskRunBuilder Action to a node. <p>
 * 
 * This stage extends the {@link TouchFilesStage}, so that it will not actually attempt to
 * run the Verify or Publish builder during builder instantiation.
 */
public 
class TaskRunBuilderStage
  extends TouchFilesStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructor
   * 
   * @param stageInformation
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   *   
   * @param bid
   *   The {@link BuilderID} representing the builder that this node is going to run.
   */
  public
  TaskRunBuilderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    BuilderID bid
  )
    throws PipelineException
  {
    super("TaskRunBuilderStage", 
          "A node which will run a task's verify builder", 
          stageInformation, context, client, nodeName, "txt", 
          null, new PluginContext("TaskRunBuilder"), StageFunction.aTextFile);
    
    addSingleParamValue(aBuilderID, bid);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7042733097096270119L;
  
  private static final String aBuilderID     = "BuilderID";
}
