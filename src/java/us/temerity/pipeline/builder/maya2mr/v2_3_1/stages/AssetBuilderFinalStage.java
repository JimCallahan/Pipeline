package us.temerity.pipeline.builder.maya2mr.v2_3_1.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

/**
 * A leaf stage used in the AssetBuilder that builds the final node.
 * <P>
 * This node is the final stage of the asset process. It has two source nodes, a material
 * scene and a finalize mel scene. It uses the MayaBuild Action to import that material
 * scene and then runs the finalize script to do any and all clean-up on the model to
 * prepare it for later use. It is suggested that the finalize mel imports the references
 * and possibly cleans up namespaces.
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
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String materialName, 
    String finalizeMel
  )
    throws PipelineException
  {
    super("AssetBuilderFinal", 
          "Stage to build the final character", 
          stageInformation,
          context,
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(materialName, "mat", MayaBuildStage.getImport(), false);
    setModelMel(finalizeMel);
  }
  private static final long serialVersionUID = 7832981809977190282L;

}
