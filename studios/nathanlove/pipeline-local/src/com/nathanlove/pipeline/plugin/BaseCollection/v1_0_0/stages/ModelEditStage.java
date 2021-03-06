// $Id: ModelEditStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   M O D E L   E D I T   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A leaf stage used in the AssetBuilder that builds the model node.
 * <p>
 * This node is the model stage of the asset process. It has the potential to have one
 * source, a mel script that will generate a placeholder model in the scene. It uses the
 * MayaBuild Action to create the scene, whether it is empty or uses the placeholder mel
 * script. This class also has a finishModel method which can be run after the node has been
 * built which will detach the linked mel script and remove the Action.
 */
public 
class ModelEditStage
  extends MayaBuildStage
  implements FinalizableStage
{
  /**
   * This constructor initializes the stage and then runs build to generate
   * the model node.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * @param mayaContext
   *   The {@link MayaContext} that this stage acts in.
   * @param nodeName
   *   The name of the node that is to be created.
   * @param placeHolderMel
   *   The name of the place holder mel script to be run.
   */
  public 
  ModelEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName, 
    String placeHolderMel
  ) 
    throws PipelineException
  {
    super("ModelEdit", 
          "Stage to build the model", 
          stageInformation,
          context, 
          client,
          mayaContext,
          nodeName, 
          true);
    pPlaceHolderMel = placeHolderMel;
    setInitialMel(pPlaceHolderMel);
  }

  
  public void 
  finalizeStage() 
    throws PipelineException
  {
    removeAction();
    if(pPlaceHolderMel != null)
      if (pRegisteredNodeMod.getSourceNames().contains(pPlaceHolderMel))
	pClient.unlink(getAuthor(), getView(), getNodeName(), pPlaceHolderMel);
    vouch();
  }
  
  private String pPlaceHolderMel;
  
  private static final long serialVersionUID = -4169512077820526304L;
}
