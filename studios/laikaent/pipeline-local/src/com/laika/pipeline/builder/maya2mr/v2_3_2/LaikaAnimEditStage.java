package com.laika.pipeline.builder.maya2mr.v2_3_2;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;


public 
class LaikaAnimEditStage
  extends MayaAnimBuildStage
{
  public LaikaAnimEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    TreeMap<String, String> assets,
    String cameraName,
    String cameraAnim,
    FrameRange range
  )
    throws PipelineException
  {
    super("LaikaAnimEdit",
          "Stage to make an animation scene with camera animation",
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName,
          true);
    if (range != null)
      setFrameRange(range);
    for (String namespace : assets.keySet()) {
      String node = assets.get(namespace);
      setupLink(node, namespace, getReference(), getModel());
    }
    if (cameraName != null) {
      setupLink(cameraName, "renderCam", getReference(), getModel());;
    }
    if (cameraAnim != null)
      setupLink(cameraAnim, "renderCam", getImport(), getAnimation());;
  }
  private static final long serialVersionUID = 6849588321995492102L;
}
