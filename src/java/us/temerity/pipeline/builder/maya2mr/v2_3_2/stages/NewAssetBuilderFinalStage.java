/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.MayaBuildStage;
import us.temerity.pipeline.stages.StageInformation;

/**
 * A leaf stage used in the NewAssetBuilder that builds the final node.
 * <P>
 * This node is the final stage of the asset process. It takes the rig scene, the material
 * scene, and the material export scene as its sources. It also takes a finalize mel script.
 * The Finalize Mel script is responsible for taking all the shaders from the Material Export
 * node and applying them to the rig scene, using the Material scene as a guideline. It should
 * also remove the reference of the material scene once it is finished running.
 * <p>
 * The Rig node and the Material Export node are imported into this scene without namespaces.
 * The Material scene is referenced in with the namespace "mat".
 * <p>
 * Only the rig name is required for this stage to work.  The other two values can be passed in
 * as <code>null</code>, in which case they will be ignored.  This allows the stage to build
 * both low-rez and high-rez final scene.
 */
public 
class NewAssetBuilderFinalStage 
  extends MayaBuildStage
{
  /**
   * This constructor will initialize the stage.
   * <p>
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param rigName
   *        The name of the rig node to link to the final node.
   * @param matName
   *        The name of the material node to link to the final node.
   * @param matExpName
   *        The name of the material export node to link to the final node.
   * @param finalizeMel
   *        The name of the finalize mel script to be run.
   */
  public 
  NewAssetBuilderFinalStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName,
    String rigName,
    String matName,
    String matExpName,
    String finalizeMel
  )
    throws PipelineException
  {
    super("NewAssetBuilderFinal", 
          "Stage to build the final character",
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(rigName, "", getImport(), false);
    if (matName != null)
      setupLink(matName, "mat", getReference(), true);
    if (matExpName != null)
      setupLink(matExpName, "", getImport(), false);
    setModelMel(finalizeMel);
  }
  private static final long serialVersionUID = -4382811401473487876L;
}
