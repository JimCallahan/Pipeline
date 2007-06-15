/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.maya2mr.stages;

import us.temerity.pipeline.FileSeq;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.NoFrameNumStage;

/**
 * A leaf stage used in the AssetBuilder that builds the shader include node.
 * <P>
 * This node is the shader include stage of the asset process. It has no sources, but does
 * have a secondary sequence. The files of this node are generated with the
 * MRayShaderInclude Action. The primary file of the node is an include list that the
 * standalone mental ray renderer can use to load the custom shaders at render time. The
 * secondary file is for Maya to use with the MI_CUSTOM_SHADER environment variable to allow
 * the shaders to be loaded when Maya starts.
 * 
 * @author Jesse Clemens
 */
public 
class AssetBuilderShaderIncludeStage 
  extends NoFrameNumStage
{
  /**
   * This constructor will initialize the stage and then runs build to generate the shader
   * include node.
   * <p>
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param secSeq
   *            The secondary sequence that is going to be added to the node. The name of
   *            this sequence needs to match the name that Maya will be looking for using
   *            MI_CUSTOM_SHADER.
   * @throws PipelineException
   */
  public 
  AssetBuilderShaderIncludeStage
  (
    UtilContext context, 
    String nodeName,
    FileSeq secSeq
  ) 
    throws PipelineException
  {
    super("AssetBuilderShaderInclude", "Stage to build the shader include node", context, nodeName, "mi", new PluginContext("Emacs"), new PluginContext("MRayShaderInclude"));
    addSecondarySequence(secSeq);
  }
}
