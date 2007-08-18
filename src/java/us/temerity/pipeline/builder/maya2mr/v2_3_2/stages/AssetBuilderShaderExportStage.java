package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaFileStage;

/**
 * A leaf stage used in the AssetBuilder that builds the shader export node.
 * <P>
 * This node is the shader export stage of the asset process. It has one source node, the
 * shading scene. It uses the MayaShaderExport Action to select all the shaders with the
 * given prefix from the shading scene and export them.
 * 
 * @author Jesse Clemens
 */
public 
class AssetBuilderShaderExportStage 
  extends MayaFileStage
{
  
  /**
   * This constructor will initialize the stage and then runs build to generate the shader
   * export node.
   * <p>
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param shadeName
   *            The name of the shade node to link to the final node.
   * @param prefix
   *            The prefix that will be used by the MayaShaderExport Action to search for
   *            shaders.
   * @throws PipelineException
   */
  public 
  AssetBuilderShaderExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String shadeName,
    String verifyMel,
    String prefix
  ) 
    throws PipelineException
  {
    super("AssetBuilderShaderExport", 
          "Stage to build the shader export scene", 
          stageInformation,
          context, 
          client,
          null, 
          nodeName, 
          true, 
          null,
          new PluginContext("MayaShaderExport"));
    setMayaScene(shadeName);
    addSingleParamValue("SelectionPrefix", prefix);
    if (verifyMel != null) {
      addLink(new LinkMod(verifyMel, LinkPolicy.Dependency));
      addSingleParamValue("NewSceneMEL", verifyMel);
    }
  }
  private static final long serialVersionUID = -384787874256066913L;
}
