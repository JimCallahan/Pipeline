package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

/**
 * A leaf stage used in the AdvAssetBuilder that builds the shader node.
 * <P>
 * This node is the shader stage of the asset process. The final node is referenced into the
 * shader scene using the MayaBuild Action. The optional source is a mentral ray
 * initialization script, which will load the mental ray plugin in the shader scene, if that
 * is not being done by default.
 * 
 * @author Jesse Clemens
 */
public 
class AdvAssetShaderStage 
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
   * @param texName
   *            The name of the texture node to link to the shader node.
   * @param mrInitMel
   *            The name of the mrInit mel script to be run.
   * @throws PipelineException
   */
  public 
  AdvAssetShaderStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName, 
    String finalName, 
    String texName, 
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
    if (texName != null)
      setupLink(texName, "tex", MayaBuildStage.getReference(), true);
    setModelMel(mrInitMel);
  }
  private static final long serialVersionUID = -8982938006640768310L;
}
