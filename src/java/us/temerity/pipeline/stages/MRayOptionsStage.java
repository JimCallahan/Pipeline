package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


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
          new PluginContext("Emacs"),
          new PluginContext("MRayOptions"));
  }
  private static final long serialVersionUID = 167310982141538453L;
}
