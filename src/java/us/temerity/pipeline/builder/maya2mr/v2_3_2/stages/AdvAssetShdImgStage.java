package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MRayRenderStage;
import us.temerity.pipeline.stages.StageInformation;


public 
class AdvAssetShdImgStage
  extends MRayRenderStage
{
  public 
  AdvAssetShdImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String modelName,
    String lightsName,
    String shaderName,
    String cameraName,
    String camOverrideName,
    String optionName
  )
    throws PipelineException
  {
    super("AdvAssetShdImg",
          "Stage to create a mental ray standalone render node for shader viewing",
          stageInformation,
          context,
          client,
          nodeName,
          new FrameRange(1, 90, 1),
          4,
          "iff");
    setSource(modelName, SourceType.Geometry);
    setSource(lightsName, SourceType.Lights);
    setSource(shaderName, SourceType.Shaders);
    setSource(cameraName, SourceType.Cameras);
    setSource(camOverrideName, SourceType.CamOverride);
    setSource(optionName, SourceType.Options);
    addSingleParamValue("CameraName", "tt:renderCamShape");
  }
  private static final long serialVersionUID = 5099001800033780443L;
}
