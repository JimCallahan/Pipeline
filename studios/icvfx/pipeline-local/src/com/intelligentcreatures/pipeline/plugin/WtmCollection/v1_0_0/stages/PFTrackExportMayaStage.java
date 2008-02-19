// $Id: PFTrackExportMayaStage.java,v 1.2 2008/02/19 03:34:22 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P F T R A C K   E X P O R T   M A Y A   S T A G E                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the PFTrackExportMaya action.<P> 
 *
 * Actually, right now it just copies a placeholder Maya scene since we don't yet have a
 * working PFTrackExportMaya action.
 */ 
public 
class PFTrackExportMayaStage 
  extends CopyStage
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
   * @param placeholder
   *   The name of the placeholder Maya scene node. 
   * 
   * @param pftrackName
   *   The name of the PFTRack scene node.
   * 
   * @param platesName
   *   The name of the undistorted 1k plates node.
   */
  public
  PFTrackExportMayaStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String placeholder, 
   String pftrackName, 
   String platesName
  )
    throws PipelineException
  {
    super("PFTrackExportMaya", 
          "Creates a node which uses the PFTrackExportMaya action.", 
          stageInfo,  context, client, 
          nodeName, "ma", 
	  placeholder);

    pPlaceholderNodeName = placeholder;

    addLink(new LinkMod(pftrackName, LinkPolicy.Association, LinkRelationship.None, null));
    addLink(new LinkMod(platesName,  LinkPolicy.Association, LinkRelationship.None, null));
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
    return ICStageFunction.aMayaScene;
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
    if(pRegisteredNodeMod.getSourceNames().contains(pPlaceholderNodeName)) 
      pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pPlaceholderNodeName); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 5803693365565122861L; 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of the placeholder Maya scene node. 
   */ 
  private String pPlaceholderNodeName; 


}
