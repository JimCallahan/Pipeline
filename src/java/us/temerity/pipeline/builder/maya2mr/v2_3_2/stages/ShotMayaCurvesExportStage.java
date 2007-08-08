package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaCurvesExportStage;
import us.temerity.pipeline.stages.StageInformation;


public 
class ShotMayaCurvesExportStage
  extends MayaCurvesExportStage
{
  public
  ShotMayaCurvesExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String sourceName,
    String exportSet,
    boolean bakeAnimation
  )
    throws PipelineException
  {
    super("ShotMayaCurvesExport",
          "Shot Builder Stage to export animation to curves",
          stageInformation,
          context,
          client,
          nodeName,
          true,
          null,
          sourceName,
          exportSet);
    addSingleParamValue("CleanUpNamespace", true);
    addSingleParamValue("BakeAnimation", bakeAnimation);
  
  }
}
