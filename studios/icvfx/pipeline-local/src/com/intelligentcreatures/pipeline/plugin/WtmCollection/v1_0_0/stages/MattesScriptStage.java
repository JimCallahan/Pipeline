// $Id: MattesScriptStage.java,v 1.1 2008/03/13 16:26:27 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T T E S   S C R I P T   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the editable mattes Nukes script from placeholder components.
 */ 
public 
class MattesScriptStage 
  extends StandardStage
  implements FinalizableStage
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage.
   * 
   * @param name
   *   The name of the stage.
   * 
   * @param desc
   *   A description of what the stage should do.
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
   * @param masterNuke
   *   The name of source master Nuke script.
   * 
   * @param platesNuke
   *   The name of source Nuke script fragment which reads in the plates images.
   * 
   * @param platesImages
   *   The name of the source node containing the raw cineon plate images.
   */
  @SuppressWarnings("unchecked")
  public
  MattesScriptStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String masterNuke,
   String platesNuke,
   String platesImages
  )
    throws PipelineException
  {
    super("MattesScript", 
	  "Creates the editable mattes Nukes script from placeholder components.", 
          stageInfo, context, client, 
          nodeName, "nk", 
          null, 
	  new PluginContext("NukeSubstComp", "Temerity", 
			    new Range<VersionID>(new VersionID("2.4.2"), null)));  

    addSingleParamValue("Mode", "Substitute"); 

    pMasterNuke = masterNuke;
    addLink(new LinkMod(masterNuke, LinkPolicy.Dependency));
    addSingleParamValue("MasterScript", masterNuke); 

    pPlatesNuke = platesNuke;
    addLink(new LinkMod(platesNuke, LinkPolicy.Dependency));
    addSourceParamValue(platesNuke, "ReplaceName", "Plates"); 

    addLink(new LinkMod(platesImages, LinkPolicy.Reference));
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
    return ICStageFunction.aNukeScript;
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
    pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pMasterNuke);
    pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pPlatesNuke); 
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7163479618872477607L;
 


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of source master Nuke script.
   */ 
  private String pMasterNuke; 

  /**
   * The name of source Nuke script fragment which reads in the plates images.
   */ 
  private String pPlatesNuke; 

}
