package us.temerity.pipeline.builder.maya2mr.v2_3_1.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaFileStage;


/**
 * A leaf stage used in the NewAssetBuilder that builds the material export node.
 * <P>
 * This node is the material export stage of the asset process. It has one source, the
 * material stage. It uses the MayaShaderExport Action to export all the shaders from the
 * scene.
 * 
 */
public 
class NewAssetBuilderMaterialExportStage
  extends MayaFileStage
{

  /**
   * This constructor will initialize the stage.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param matName
   *        The name of the material node to link to the material export node.
   */
  public 
  NewAssetBuilderMaterialExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String matName
  )
    throws PipelineException
  {
    super("NewAssetBuilderMaterialExport", 
          "Stage to build the material export scene",
          stageInformation,
          context,
          client,
          null, 
          nodeName, 
          true,
          null,
          new PluginContext("MayaShaderExport"));
    addSingleParamValue("SelectionPrefix", "");
    setMayaScene(matName);
  }
  private static final long serialVersionUID = -8913667114103669134L;
}
