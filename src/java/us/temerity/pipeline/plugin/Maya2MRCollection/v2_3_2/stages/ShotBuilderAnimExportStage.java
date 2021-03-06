package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.StandardStage;


public 
class ShotBuilderAnimExportStage
  extends StandardStage
{
  public 
  ShotBuilderAnimExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String srcName,
    TreeSet<String> assetNames,
    FrameRange range
  ) 
    throws PipelineException
  {
    super("ShotBuilderAnimExport", 
          "The stage for the ShotBuilder that exports the animation from a given scene.",
          stageInformation,
          context, 
          client,
          nodeName, null, 
          null, 
          new PluginContext("AnimExport"));
    for (String assetName : assetNames) {
      addSecondarySequence(new FileSeq(assetName, "anim"));
    }
    LinkMod mod = new LinkMod(srcName, LinkPolicy.Dependency);
    addLink(mod);
    addSingleParamValue("Method", "Simulate");
    addSingleParamValue("ExportSet", "SELECT");
    addSingleParamValue("FirstFrame", range.getStart());
    addSingleParamValue("LastFrame", range.getEnd());
    addSingleParamValue("MayaScene", srcName);
  }
  private static final long serialVersionUID = -34779414650608573L;
}
