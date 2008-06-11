package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaRenderStage;


public 
class MRayShotImgStage
  extends MayaMRayRenderStage
{
  public
  MRayShotImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    String suffix,
    String mayaScene,
    String globals
  )
    throws PipelineException
  {
    super("ShotImg",
          "Stage to create some rendered images.",
          stageInformation,
          context,
          client,
          nodeName,
          range,
          4,
          suffix,
          mayaScene);
    setPreExportMel(globals);
  }
  private static final long serialVersionUID = 1572901247647526193L;
}
