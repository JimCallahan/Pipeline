package us.temerity.pipeline.builder.stages;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;


public 
class ShotBuilderAnimExportStage
  extends NoFrameNumStage
{
  public ShotBuilderAnimExportStage
  (
    UtilContext context, 
    String nodeName,
    String srcName,
    TreeSet<String> assetNames,
    FrameRange range
  ) 
    throws PipelineException
  {
    super("ShotBuilderAnimExport", 
          "The stage for the ShotBuilder that exports the animation from a given scene.", 
          context, nodeName, null, 
          new PluginContext("Emacs"), new PluginContext("AnimExport"));
    for (String assetName : assetNames) {
      addSecondarySequence(new FileSeq(assetName, "anim"));
    }
    LinkMod mod = new LinkMod(srcName, LinkPolicy.Dependency);
    addLink(mod);
    addSingleParam("Method", "Simulate");
    addSingleParam("ExportSet", "SELECT");
    addSingleParam("FirstFrame", range.getStart());
    addSingleParam("LastFrame", range.getEnd());
    addSingleParam("MayaScene", srcName);
  }
}
