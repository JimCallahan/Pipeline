package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaRenderStage;


public 
class ShotImgStage
  extends MayaRenderStage
{
  public
  ShotImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    String suffix,
    String mayaScene,
    String globals,
    Renderer renderer
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
    setPreRenderMel(globals);
    setRenderer(renderer);
  }
  private static final long serialVersionUID = 1572901247647526193L;
}
