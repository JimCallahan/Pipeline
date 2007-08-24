package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;


public 
class ShotAnimBuildStage
  extends MayaAnimBuildStage
{
  public ShotAnimBuildStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    TreeMap<String, String> assets,
    TreeMap<String, String> anim,
    String buildMEL,
    FrameRange range
  )
    throws PipelineException
  {
    super("ShotAnimBuild",
          "Stage for building an animation scene from models and anim curves",
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
    for (String namespace : anim.keySet()) {
      String node = anim.get(namespace);
      setupLink(node, namespace, getReference(), getAnimation());
    }
    setAnimMel(buildMEL);
  }
  private static final long serialVersionUID = 7400008406120638337L;
}
