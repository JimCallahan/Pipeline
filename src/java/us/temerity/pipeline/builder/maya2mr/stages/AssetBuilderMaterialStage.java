package us.temerity.pipeline.builder.maya2mr.stages;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaBuildStage;


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
class AssetBuilderMaterialStage
  extends MayaBuildStage
{

  /**
   * This constructor will initialize the stage and then run build to generate the
   * material node.
   *
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param rigName
   *            The name of the rig node to link to the material node.
   * @throws PipelineException
   */
  public 
  AssetBuilderMaterialStage
  (
    UtilContext context,
    MayaContext mayaContext,
    String nodeName,
    String rigName
  )
    throws PipelineException
  {
    super("AssetBuilderMaterial", "Stage to build the material scene", context,
          mayaContext, nodeName, true);
    setupLink(rigName, "rig", MayaBuildStage.getReference(), true);
  }
}
