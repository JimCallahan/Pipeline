// $Id: AnimCurveExportStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaCurvesExportStage;

/*------------------------------------------------------------------------------------------*/
/*   A N I M   C U R V E   E X P O R T   S T A G E                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds scene where artist animation is exported to as curves
 */
public 
class AnimCurveExportStage
  extends MayaCurvesExportStage
{
  public
  AnimCurveExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String sourceName,
    String exportSet
  )
    throws PipelineException
  {
    super("AnimCurveExport",
          "Builds scene where artist animation is exported to as curves",
          stageInformation,
          context,
          client,
          nodeName,
          true,
          null,
          sourceName,
          exportSet);
    addSingleParamValue("CleanUpNamespace", true);
    addSingleParamValue("BakeAnimation", true);
  }
  private static final long serialVersionUID = -2733067174443264038L;
}
