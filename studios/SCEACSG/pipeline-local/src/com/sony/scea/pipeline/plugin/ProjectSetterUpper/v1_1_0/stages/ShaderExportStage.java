// $Id: ShaderExportStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/**
 * A leaf stage used in the AssetBuilder that builds the shader export node.
 * <P>
 * This node is the shader export stage of the asset process. It has one source node, the
 * shading scene. It uses the MayaShaderExport Action to select all the shaders with the
 * given prefix from the shading scene and export them.
 * 
 */
public 
class ShaderExportStage 
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
  ShaderExportStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String shadeName,
    String verifyMel
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
    addSingleParamValue("SelectionPrefix", "");
    if (verifyMel != null) {
      addLink(new LinkMod(verifyMel, LinkPolicy.Dependency));
      addSingleParamValue("NewSceneMEL", verifyMel);
    }
  }
  private static final long serialVersionUID = 6585570838244402249L;
}
