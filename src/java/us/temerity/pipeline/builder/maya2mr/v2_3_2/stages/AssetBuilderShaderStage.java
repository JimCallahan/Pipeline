package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaBuildStage;
import us.temerity.pipeline.stages.StageInformation;

/**
 * A leaf stage used in the AssetBuilder that builds the shader node.
 * <P>
 * This node is the shader stage of the asset process. It must have at least two source
 * nodes and may have three. The manditory sources nodes are the final node of the asset
 * process and the shader include node. The final node is referenced into the shader scene
 * using the MayaBuild Action. The shader include node is a Reference link, which provides
 * the custom shader includes necessary to load the shader scene. The optional source is a
 * mentral ray initialization script, which will load the mental ray plugin in the shader
 * scene, if that is not being done by default.
 * 
 * @author Jesse Clemens
 */
public 
class AssetBuilderShaderStage 
  extends MayaBuildStage
  {
  /**
   * This constructor will initialize the stage and then runs build to generate the shader
   * node.
   * <p>
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param finalName
   *            The name of the final node to link to the shader node.
   * @param shaderIncludeName
   *            The name of the shader include node to link to the shader node.
   * @param mrInitMel
   *            The name of the mrInit mel script to be run.
   * @throws PipelineException
   */
  public 
  AssetBuilderShaderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String finalName, 
    String shaderIncludeName, 
    String mrInitMel
  )
    throws PipelineException
  {
    super("AssetBuilderShader", 
          "Stage to build the shader scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(finalName, "final", MayaBuildStage.getReference(), true);
    addLink(new LinkMod(shaderIncludeName, LinkPolicy.Reference));
    setModelMel(mrInitMel);
  }
  private static final long serialVersionUID = 45623374952378348L;
}
