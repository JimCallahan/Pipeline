package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.FrameRange;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;


public 
class ShotBuilderLightStage
  extends MayaBuildStage
{
  public
  ShotBuilderLightStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName,
    String sourceName,
    String sourcePrefix,
    String lgtRigName,
    FrameRange range
  ) 
    throws PipelineException
  {
    super("ShotBuilderLight", "Stage to build the lighting scene", 
          context, mayaContext, 
          nodeName, true);
    if (range != null)
      setFrameRange(range);
    setUnits();
    setupLink(sourceName, sourcePrefix, getReference(), true);
    setupLink(lgtRigName, "lights", getReference(), true);
  }
}
