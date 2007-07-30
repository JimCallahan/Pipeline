package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


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
          getDefaultEditor(client, suffix),
          new PluginContext("Thumbnail"));
    addLink(new LinkMod(source, LinkPolicy.Dependency));
    addSingleParamValue("ThumbnailSize", size);
  }
  private static final long serialVersionUID = 6947901517583279702L;
}
