/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

/**
 * A leaf stage used in the NewAssetBuilder that builds the rig node.
 * <p>
 * This node is the rig stage of the asset process. It may only have one source, the model
 * node, but it can also have a separate head model and a separate blendshape node as well, if
 * the <code>BuildSeparateHead</code> option is selected in the builder. It uses the
 * MayaBuild Action to import the models. This Action will need to be disabled before the node
 * is used for rigging work. Once it is disabled, the model file will have to be imported by
 * hand into this scene, every time it is changed.
 * <P>
 * Alternatively, this node can be used with an autorig setup. There are three nodes that can
 * be passed in for the autorigger. A mel script that performs the rigging actions, a skeleton
 * file that contains the rigging setup, and an info file that should contain information that
 * the rigging mel script can use to attach the geometry to the skeleton. One or more of these
 * can be set to <code>null</code> if they're not needed. For example, if the autorig mel
 * script builds the skeleton, then the skelName param could be <code>null</code>.
 * 
 */
public 
class NewAssetBuilderRigStage
  extends MayaBuildStage
  {
  /**
   * This constructor will initialize the stage to generate the rig node.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param modelName
   *        The name of the model node to link to the rig node.
   * @param headName
   *        The name of the separate head model in link to the rig node.
   * @param blendName
   *        The name of the separate blend shape models to link to the rig node.
   * @param skelName
   *        The name of the separate skeleton file is an autorig mel script is being used.
   * @param autoRigMel
   *        The name of the mel script that does the autorigging.
   * @param rigInfoName
   *        The name of the file that contains information for the autorigger.
   * @throws PipelineException
   */
  public 
  NewAssetBuilderRigStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String modelName,
    String headName,
    String blendName,
    String skelName,
    String autoRigMel,
    String rigInfoName,
    String animTextures
  )
    throws PipelineException
  {
    super("NewAssetBuilderRig", 
          "Stage to build the rig scene", 
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName,
          true);
    setupLink(modelName, "mod", MayaBuildStage.getImport(), false);
    if (headName != null)
      setupLink(headName, "head", MayaBuildStage.getImport(), false);
    if (blendName != null)
      setupLink(blendName, "blend", MayaBuildStage.getImport(), false);
    if (skelName != null)
      setupLink(skelName, "skel", MayaBuildStage.getImport(), false);
    if (rigInfoName != null) {
      LinkMod mod = new LinkMod(rigInfoName, LinkPolicy.Dependency);
      addLink(mod);
    }
    if (animTextures != null)
      setupLink(animTextures, "tex", MayaBuildStage.getReference(), true);
    if (autoRigMel != null)
      setModelMel(autoRigMel);
  }
  private static final long serialVersionUID = 798760775428376736L;
}
