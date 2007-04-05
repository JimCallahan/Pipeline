package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;


/**
 * A leaf stage used in the AssetBuilder that builds the material node.
 * <P>
 * This node is the material stage of the asset process. It has one source, the rig stage.
 * It uses the MayaBuild Action to reference the rig node. This Action will need to be
 * disabled before the node is used for shading work.
 *
 * @author jesse
 */
public 
class NewAssetBuilderMaterialStage
  extends MayaBuildStage
{

  /**
   * This constructor will initialize the stage.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param modName
   *        The name of the model node to link to the material node.
   * @param headName
   *        The name of the head model to link to the material node.
   * @throws PipelineException
   */
  public 
  NewAssetBuilderMaterialStage
  (
    UtilContext context,
    MayaContext mayaContext,
    String nodeName,
    String modName,
    String headName
  )
    throws PipelineException
  {
    super("NewAssetBuilderMaterial", "Stage to build the material scene", context,
          mayaContext, nodeName, true);
    setupLink(modName, "mod", MayaBuildStage.getReference(), true);
    if (headName != null)
      setupLink(headName, "head", MayaBuildStage.getReference(), true);
  }
}