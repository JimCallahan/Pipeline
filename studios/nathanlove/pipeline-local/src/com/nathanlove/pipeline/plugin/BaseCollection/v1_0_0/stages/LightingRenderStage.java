// $Id: LightingRenderStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaRenderStage;

/**
 * Stage to do a test render of the lighting scene.
 */
public 
class LightingRenderStage
  extends MayaRenderStage
{
  public
  LightingRenderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String mayaScene,
    String globals,
    String renderer,
    FrameRange range
  )
    throws PipelineException
  {
    super("LightingRender",
          "Stage to do a test render of the lighting scene.",
          stageInformation,
          context,
          client,
          nodeName,
          range,
          4,
          "iff",
          mayaScene);
    setPreRenderMel(globals);
    Renderer r = Renderer.fromString(renderer);
    if (r != null)
      setRenderer(r);
  }
  private static final long serialVersionUID = 3131285833474766594L;
}
