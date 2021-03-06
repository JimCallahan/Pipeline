// $Id: ShadeFinalStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/**
 * A leaf stage used in the AssetBuilder that builds the final node.
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
class ShadeFinalStage 
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
  ShadeFinalStage
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
    super("ShadeFinal", 
          "Stage to build the final character",
          stageInformation,
          context, 
          client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(rigName, "", getImport(), false);
    if (matName != null)
      setupLink(matName, "source", getReference(), true);
    if (matExpName != null)
      setupLink(matExpName, "", getImport(), false);
    setModelMel(finalizeMel);
  }
  private static final long serialVersionUID = 7015358477535830300L;
}
