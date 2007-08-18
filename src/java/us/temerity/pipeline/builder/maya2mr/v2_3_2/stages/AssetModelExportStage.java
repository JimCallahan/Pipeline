package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaExportStage;


public 
class AssetModelExportStage
  extends MayaExportStage
{
  public
  AssetModelExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String exportName,
    String verifyMEL
  )
    throws PipelineException
  {
    super("AssetModelExport", 
          "Stage to be run to export a model from a working scene",
          stageInformation,
          context,
          client,
          nodeName,
          true,
          null,
          exportName,
          true);
    setNewSceneMel(verifyMEL);
  }
  private static final long serialVersionUID = -5363352825953199560L;
}
