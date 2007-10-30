package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class MRayOptionsStage
  extends StandardStage
{
  public
  MRayOptionsStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("MRayOptionsStage",
          "Stage to make a mental ray options file",
          stageInformation,
          context,
          client,
          nodeName,
          "mi",
          null,
          new PluginContext("MRayOptions"));
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
  
  private static final long serialVersionUID = 167310982141538453L;
}
