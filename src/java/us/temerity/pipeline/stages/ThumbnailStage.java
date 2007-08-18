package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class ThumbnailStage
  extends StandardStage
{
  public
  ThumbnailStage
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
    super("ThumbnailStage",
          "Stage to build a thumbnail image from a sequence",
          stageInformation,
          context,
          client,
          nodeName,
          suffix,
          null,
          new PluginContext("Thumbnail"));
    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("ThumbnailSize", size);
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.RenderedImage.toString();
  }

  
  private static final long serialVersionUID = 6947901517583279702L;
}
