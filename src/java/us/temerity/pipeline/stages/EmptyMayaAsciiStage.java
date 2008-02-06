package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public class EmptyMayaAsciiStage
  extends MayaBuildStage
{
  public EmptyMayaAsciiStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName
  )
    throws PipelineException
  {
    super("EmptyMayaAscii", 
          "Stage to build the model",
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName, 
          true);
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
    return StageFunction.aMayaScene;
  }

  
  private static final long serialVersionUID = 1710441722495211817L;

}
