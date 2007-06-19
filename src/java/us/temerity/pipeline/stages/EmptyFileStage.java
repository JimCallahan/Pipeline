package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;

/**
 * A stage to create a node that is just an empty file.
 * <p>
 * Has a finalizeStage method that will remove the action, allowing it to be editing, if
 * this behavior is desired. 
 *
 */
public class EmptyFileStage
  extends NoFrameNumStage
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
          new PluginContext("Emacs"), 
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
  
  private static final long serialVersionUID = -4188429817735845652L;
}
