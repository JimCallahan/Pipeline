// $Id: DistortedGridStage.java,v 1.1 2008/02/06 16:29:48 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D I S T O R T E D   G R I D   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a placeholder image for the post-PFTrack distored grid by copying the original
 * grid image. 
 */ 
public 
class DistortedGridStage 
  extends StandardStage
  implements FinalizableStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage.
   * 
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   * 
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   * 
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   * 
   * @param nodeName
   *   The name of the node that is to be created.
   * 
   * @param gridName
   *   The name of the original grid node.
   * 
   * @param pftrackName
   *   The name of the PFTrack scene node.
   */
  public
  DistortedGridStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String gridName, 
   String pftrackName
  )
    throws PipelineException
  {
    super("PFTrackBuild", 
          "Creates a node which uses the PFTrackBuild action.", 
          stageInfo, 
          context, 
          client, 
          nodeName, 
          "tif", 
          null, 
          new PluginContext("Copy"));   

    pOriginalGridNodeName = gridName;
    addLink(new LinkMod(gridName, LinkPolicy.Dependency));
    addLink(new LinkMod(pftrackName, LinkPolicy.Association, LinkRelationship.None, null));
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return ICStageFunction.aRenderedImage;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I N A L I Z A B L E   S T A G E                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Finishes off the work of the stage after it has been queued.
   */
  public void 
  finalizeStage() 
    throws PipelineException
  {
    removeAction(pRegisteredNodeName);
    if(pRegisteredNodeMod.getSourceNames().contains(pOriginalGridNodeName)) 
      pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pOriginalGridNodeName);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7065661211422289862L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of the original grid node.
   */ 
  private String pOriginalGridNodeName; 

}
