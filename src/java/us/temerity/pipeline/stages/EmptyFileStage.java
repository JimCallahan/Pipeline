package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

/**
 * A stage to create a node that is just an empty file.
 * <p>
 * Has a finalizeStage method that will remove the action, allowing it to be editing, if
 * this behavior is desired. 
 *
 */
public class EmptyFileStage
  extends StandardStage
  implements FinalizableStage
{
  public
  EmptyFileStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName
  )
    throws PipelineException
  {
    super("EmptyFile", 
          "Stage to build an Empty File",
          stageInformation,
          context,
          client,
          nodeName, 
          null, 
          null, 
          new PluginContext("Touch"));
  }
  
  public
  EmptyFileStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix
  )
    throws PipelineException
  {
    super("EmptyFile", 
          "Stage to build an Empty File",
          stageInformation,
          context,
          client,
          nodeName, 
          suffix, 
          null, 
          new PluginContext("Touch"));
  }
  
  public
  EmptyFileStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix
  )
    throws PipelineException
  {
    super("EmptyFile", 
          "Stage to build an Empty File",
          stageInformation,
          context,
          client,
          nodeName, 
          range,
          padding,
          suffix, 
          null, 
          new PluginContext("Touch"));
  }
  
  /**
   * Finishes off the work of the stage after it has been queued.
   * <p>
   * Removes the action from the node. This only needs to be called if it is desirous to
   * remove the Action.
   * 
   */
  public void 
  finalizeStage() 
    throws PipelineException
  {
    removeAction(pRegisteredNodeName);
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aTextFile;
  }

  
  private static final long serialVersionUID = -4188429817735845652L;
}
