package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaMiShaderStage;


public 
class AdvAssetShdMiStage
  extends MayaMiShaderStage
{
  public 
  AdvAssetShdMiStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String mayaScene
  )
    throws PipelineException
  {
    super("AdvAssetShdMi", 
      "Stage to build shader mi files for shader tt rendering",
      stageInformation,
      context,
      client,
      nodeName,
      new FrameRange(1, 90, 1),
      4,
      mayaScene);
    setMayaLightLinkingValue(2);
    setRenderPassSuffix("beau");
    setNamespaces("final", null, null);
  }
  private static final long serialVersionUID = 3791432716459428991L;
}
