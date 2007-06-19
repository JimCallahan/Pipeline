/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_1.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.stages.MayaFileStage;
import us.temerity.pipeline.stages.StageInformation;

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
   * @param mayaContext
   *            The {@link MayaContext} that this stage acts in.
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
    MayaContext mayaContext,
    String nodeName,
    String shadeName,
    String prefix
  ) 
    throws PipelineException
  {
    super("AssetBuilderShaderExport", 
          "Stage to build the shader export scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true, 
          null,
          new PluginContext("MayaShaderExport"));
    setMayaScene(shadeName);
    addSingleParamValue("SelectionPrefix", prefix);
  }
  private static final long serialVersionUID = 7424624983972003621L;
}
