// $Id: PFTrackBuildStage.java,v 1.7 2008/02/13 21:31:57 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P F T R A C K   B U I L D   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the PFTrackBuild action.<P> 
 * 
 * Actually, right now it just touches the file since we don't yet have a working 
 * PFTrackBuild action.
 */ 
public 
class PFTrackBuildStage 
  extends StandardStage
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
   * @param platesName
   *   The name of the scanned plates node.
   * 
   * @param vfxDataName
   *   The name of VFX lens data node. 
   */
  public
  PFTrackBuildStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String platesName, 
   String vfxDataName 
  )
    throws PipelineException
  {
    super("PFTrackBuild", 
          "Creates a node which uses the PFTrackBuild action.", 
          stageInfo, 
          context, 
          client, 
          nodeName, 
          "pts", 
          null, 
          new PluginContext("Touch"));  

    addLink(new LinkMod(platesName, LinkPolicy.Association, LinkRelationship.None, null));
    addLink(new LinkMod(vfxDataName, LinkPolicy.Association, LinkRelationship.None, null));
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
    return ICStageFunction.aPFTrackScene;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -3670238520255972952L;

}
