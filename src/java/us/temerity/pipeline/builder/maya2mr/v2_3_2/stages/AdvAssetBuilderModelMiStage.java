/*
 * Created on Jul 9, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.builder.maya2mr.v2_3_2.stages
 * 
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaMiExportStage;
import us.temerity.pipeline.stages.StageInformation;

public 
class AdvAssetBuilderModelMiStage 
  extends MayaMiExportStage
{
  public 
  AdvAssetBuilderModelMiStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String sourceScene
  )
    throws PipelineException
  {
    super("AdvAssetBuilderModelMI", 
          "Stage to build a model mi file to use for shader verification",
          stageInformation,
          context,
          client,
          nodeName,
          "ma",
          sourceScene,
          "GEO");
    setGeoAllExport();
  }
  private static final long serialVersionUID = -2280488424689859000L;
}
