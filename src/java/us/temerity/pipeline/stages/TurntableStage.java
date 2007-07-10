package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaRenderStage;
import us.temerity.pipeline.stages.StageInformation;

public 
class TurntableStage 
  extends MayaRenderStage
  {
    protected TurntableStage
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
      String globals
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
      setPreRenderMel(globals);
    }
    private static final long serialVersionUID = 3240152577221732496L;
}
