package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.NodeMod;
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
    UtilContext context, 
    String nodeName
  )
    throws PipelineException
  {
    super("EmptyFile", 
          "Stage to build an Empty File", 
           context, 
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
    NodeMod mod = sClient.getWorkingVersion(getAuthor(), getView(), pRegisteredNodeName);
    mod.setAction(null);
    sClient.modifyProperties(getAuthor(), getView(), mod);
  }
}
