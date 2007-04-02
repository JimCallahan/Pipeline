/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.stages;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;

/**
 * A leaf stage used in the AssetBuilder that builds the final node.
 * <P>
 * This node is the final stage of the asset process. It has two source nodes, a material
 * scene and a finalize mel scene. It uses the MayaBuild Action to import that material
 * scene and then runs the finalize script to do any and all clean-up on the model to
 * prepare it for later use. It is suggested that the finalize mel imports the references
 * and possibly cleans up namespaces.
 * 
 * @author Jesse Clemens
 */
public 
class AssetBuilderFinalStage 
  extends MayaBuildStage
{
  /**
   * This constructor will initialize the stage and then runs build to generate the final
   * node.
   * <p>
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param materialName
   *            The name of the material node to link to the final node.
   * @param finalizeMel
   *            The name of the finalize mel script to be run.
   * @throws PipelineException
   */
  public 
  AssetBuilderFinalStage
  (
    UtilContext context, 
    MayaContext mayaContext,
    String nodeName, 
    String materialName, 
    String finalizeMel
  )
    throws PipelineException
  {
    super("AssetBuilderFinal", "Stage to build the final character", context, 
          mayaContext, nodeName, true);
    setupLink(materialName, "mat", MayaBuildStage.getImport(), false);
    setModelMel(finalizeMel);
  }
}
