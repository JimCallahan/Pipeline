package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaCurvesExportStage;


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
  private static final long serialVersionUID = 5010175295350788309L;
}
