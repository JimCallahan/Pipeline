// $Id: CopyTrackingMarkersStage.java,v 1.1 2008/03/19 22:38:46 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O P Y   T R A C K I N G   M A R K E R S   S T A G E                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Make an approved copy of the original tracking markers file.
 */ 
public 
class CopyTrackingMarkersStage 
  extends CopyStage
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
   * @param source
   *   The name of the source images node.
   */
  public 
  CopyTrackingMarkersStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String source
  )
    throws PipelineException
  {
    super("CopyTrackingMarkers", 
          "Make an approved copy of the original tracking markers file.", 
          stageInfo, context, client, 
          nodeName, "2dt", source);
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
    return StageFunction.aTextFile;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7015154340970153420L;

}
