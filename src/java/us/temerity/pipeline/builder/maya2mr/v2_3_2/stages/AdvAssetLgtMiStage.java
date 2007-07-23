package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaMiExportStage;
import us.temerity.pipeline.stages.StageInformation;


public 
class AdvAssetLgtMiStage
  extends MayaMiExportStage
{
  public 
  AdvAssetLgtMiStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String mayaScene
  )
    throws PipelineException
  {
    super("AdvAssetLgtMi", 
          "Stage to build light mi files for shader tt rendering",
          stageInformation,
          context,
          client,
          nodeName,
          new FrameRange(1, 90, 1),
          4,
          mayaScene,
          "tt:LIGHTS");
    setLightsExport();
  }
  private static final long serialVersionUID = 8764592010619645165L;
}
