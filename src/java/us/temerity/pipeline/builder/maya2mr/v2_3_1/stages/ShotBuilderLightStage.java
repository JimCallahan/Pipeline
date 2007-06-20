package us.temerity.pipeline.builder.maya2mr.v2_3_1.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaBuildStage;
import us.temerity.pipeline.stages.StageInformation;


public 
class ShotBuilderLightStage
  extends MayaBuildStage
{
 

  public
  ShotBuilderLightStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName,
    String sourceName,
    String sourcePrefix,
    String lgtRigName,
    FrameRange range
  ) 
    throws PipelineException
  {
    super("ShotBuilderLight", 
      	  "Stage to build the lighting scene",
      	  stageInformation,
          context,
          client,
          mayaContext, 
          nodeName, true);
    if (range != null)
      setFrameRange(range);
    setUnits();
    setupLink(sourceName, sourcePrefix, getReference(), true);
    setupLink(lgtRigName, "lights", getReference(), true);
  }
  private static final long serialVersionUID = 4463317246082802738L;
}