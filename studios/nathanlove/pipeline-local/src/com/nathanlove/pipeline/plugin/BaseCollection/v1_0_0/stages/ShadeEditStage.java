// $Id: ShadeEditStage.java,v 1.1 2008/05/26 03:19:52 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;


/**
 * A leaf stage used in the AssetBuilder that builds the material node.
 * <P>
 * This node is the material stage of the asset process. It has two sources, the model scene
 * and the texture node.
 * It uses the MayaBuild Action to reference both nodes. This Action will need to be
 * disabled before the node is used for shading work.
 *
 */
public 
class ShadeEditStage
  extends MayaBuildStage
{
  /**
   * This constructor will initialize the stage.
   * 
   * @param context
   *        The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *        The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *        The name of the node that is to be created.
   * @param modName
   *        The name of the model node to link to the material node.
   * @param texName
   *        The name of the texture model to link to the material node.
   * @throws PipelineException
   */
  public 
  ShadeEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    String modName,
    String texName
  )
    throws PipelineException
  {
    super("ShadeEdit", 
      	  "Stage to build the shading scene", 
      	  stageInformation,
      	  context,
      	  client,
          mayaContext, 
          nodeName, 
          true);
    setupLink(modName, "mod", MayaBuildStage.getReference(), true);
    if (texName != null)
      setupLink(texName, "tex", MayaBuildStage.getReference(), true);
  }
  private static final long serialVersionUID = 1567317326416445021L;
}
