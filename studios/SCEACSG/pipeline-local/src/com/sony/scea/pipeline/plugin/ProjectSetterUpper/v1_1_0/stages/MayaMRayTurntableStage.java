package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class MayaMRayTurntableStage 
extends MayaMRayRenderStage
{

protected MayaMRayTurntableStage 
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    int length,
    int padding,
    String mayaScene,
    String globals//,
    //Renderer renderer
  )
  throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          new FrameRange(1, length, 1), 
          padding, 
          suffix, 
          mayaScene);
    setPreExportMel(globals);
    //setRenderer(renderer);
  }
	private static final long serialVersionUID = 6622143704246570326L;
}
