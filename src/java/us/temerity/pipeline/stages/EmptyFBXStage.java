package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class EmptyFBXStage 
  extends StandardStage
  implements FinalizableStage
{
  public
  EmptyFBXStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName
  )
    throws PipelineException
  {
    super("EmptyFBX", 
      "Stage to make an empty FBX scene from an empty Maya scene.", 
      stageInformation, 
      context, 
      client, 
      nodeName, 
      "fbx", 
      null, 
      new PluginContext("EmptyFBX"));
  }
  
  public void
  finalizeStage()
    throws PipelineException
  {
    removeAction();
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aMotionBuilderScene;
  }

  
  private static final long serialVersionUID = 7956888902881045916L;
}
