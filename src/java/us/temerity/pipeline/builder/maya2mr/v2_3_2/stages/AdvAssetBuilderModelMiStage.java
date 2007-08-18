// $Id: AdvAssetBuilderModelMiStage.java,v 1.3 2007/08/18 18:14:49 jesse Exp $

package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaMiExportStage;

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
          sourceScene,
          "GEO");
    setGeoAllExport();
  }
  private static final long serialVersionUID = -2280488424689859000L;
}
