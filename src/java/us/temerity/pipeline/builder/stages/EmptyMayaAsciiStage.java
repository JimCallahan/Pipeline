package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.NodeMod;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;


public class EmptyMayaAsciiStage
  extends MayaBuildStage
{
  public EmptyMayaAsciiStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName
  )
    throws PipelineException
  {
    super("EmptyMayaAscii", "Stage to build the model", context, mayaContext,
      nodeName, true);
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
