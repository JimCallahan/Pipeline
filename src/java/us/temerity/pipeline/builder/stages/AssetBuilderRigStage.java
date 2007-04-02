/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;

/**
 * A leaf stage used in the AssetBuilder that builds the rig node.
 * <p>
 * This node is the rig stage of the asset process. It has one source, the model node. It
 * uses the MayaBuild Action to import the model node. This Action will need to be disabled
 * before the node is used for rigging work. Once it is disabled, the model file will have
 * to be imported by hand into this scene, every time it is changed.
 * 
 * @author jesse
 */
public 
class AssetBuilderRigStage 
  extends MayaBuildStage
{
  /**
   * This constructor will initialize the stage.
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param modelName
   *            The name of the model node to link to the rig node.
   * @throws PipelineException
   */
  public 
  AssetBuilderRigStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName, 
    String modelName
  ) 
    throws PipelineException
  {
    super("AssetBuilderRig", "Stage to build the rig scene", context, mayaContext, nodeName, true);
    setupLink(modelName, "mod", MayaBuildStage.getImport(), false);
  }
}
