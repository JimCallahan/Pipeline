package us.temerity.pipeline.builder.maya2mr.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
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
    UtilContext context,
    String nodeName,
    String matName
  )
    throws PipelineException
  {
    super("NewAssetBuilderMaterialExport", 
          "Stage to build the material export scene", 
          context,
          null, 
          nodeName, 
          true,
          null,
          new PluginContext("MayaShaderExport"));
    addSingleParam("SelectionPrefix", "");
    setMayaScene(matName);
  }
}
