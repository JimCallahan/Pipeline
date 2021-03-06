package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;


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
      setNodeFrameRange(range);
    setUnits();
    setupLink(sourceName, sourcePrefix, getReference(), true);
    if (lgtRigName != null)
      setupLink(lgtRigName, "lights", getReference(), true);
  }
  private static final long serialVersionUID = 4463317246082802738L;
}
