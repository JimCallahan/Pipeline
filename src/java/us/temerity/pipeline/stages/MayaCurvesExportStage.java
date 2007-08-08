package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.MayaFileStage;


public 
class MayaCurvesExportStage
  extends MayaFileStage
{
  protected
  MayaCurvesExportStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    boolean isAscii,
    PluginContext editor,
    String sourceName,
    String exportSet
  )
    throws PipelineException
  {
    super(name,
          desc,
          stageInformation,
          context,
          client,
          null,
          nodeName,
          isAscii,
          editor,
          new PluginContext("MayaCurvesExport"));
    setMayaScene(sourceName);
    addSingleParamValue("ExportSet", exportSet);
  }
  private static final long serialVersionUID = -2493960986436834697L;
}
