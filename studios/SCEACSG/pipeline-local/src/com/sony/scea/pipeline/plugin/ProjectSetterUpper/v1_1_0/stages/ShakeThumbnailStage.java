package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
//import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;

public 
class ShakeThumbnailStage
  extends StandardStage
{
  public
  ShakeThumbnailStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName, 
    String suffix,
    String source,
    int size
  )
    throws PipelineException
  {
    super("ShakeThumbnailStage",
          "Stage to build a thumbnail image from a sequence",
          stageInformation,
          context,
          client,
          nodeName,
          suffix,
          null,
          new PluginContext("ShakeThumbnail"));
    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("ThumbnailSize", size);
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  /*
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aRenderedImage;
  }
*/
  
  private static final long serialVersionUID = 6947901517583279702L;
}
