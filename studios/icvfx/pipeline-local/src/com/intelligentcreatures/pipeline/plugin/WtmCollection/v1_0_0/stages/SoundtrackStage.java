// $Id: SoundtrackStage.java,v 1.2 2008/05/12 16:45:25 jesse Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S O U N D T R A C K   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a copy of the placeholder soundtrack node. 
 */ 
public 
class SoundtrackStage 
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
   *   The name of the placeholder soundtrack node.
   */
  public
  SoundtrackStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String placeholder
  )
    throws PipelineException
  {
    super("Soundtrack", 
          "Creates a copy of the placeholder soundtrack audio file node." , 
          stageInfo,  context, client, 
          nodeName, "aiff", 
	  placeholder);

    pPlaceholderNodeName = placeholder;
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
    return ICStageFunction.aSoundFile;
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
    removeAction();
    if(pRegisteredNodeMod.getSourceNames().contains(pPlaceholderNodeName)) 
      pClient.unlink(getAuthor(), getView(), pRegisteredNodeName, pPlaceholderNodeName); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -2393245859940777550L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of the placeholder Maya scene node. 
   */ 
  private String pPlaceholderNodeName; 


}
