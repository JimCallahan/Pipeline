// $Id: LensInfoStage.java,v 1.6 2008/02/07 14:14:33 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L E N S   D A T A   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the LensInfo action.
 */ 
public 
class LensInfoStage 
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
   */
  public
  LensInfoStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName
  )
    throws PipelineException
  {
    super("LensInfo", 
          "Creates a node which uses the LensInfo action.", 
          stageInfo, context, client, 
          nodeName, null, 
          null, 
          new PluginContext("LensInfo", "ICVFX"));
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
    return ICStageFunction.aNone;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -1458573679042030943L;

}
