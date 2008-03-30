// $Id: PlaceholderStage.java,v 1.1 2008/03/30 01:43:10 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P L A C E H O L D E R   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Copies a placeholder node.<P> 
 */ 
public 
class PlaceholderStage 
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
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param placeholder
   *   The name of the placeholder node. 
   */
  protected
  PlaceholderStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String suffix, 
   String placeholder
  )
    throws PipelineException
  {
    super("Placeholder", 
          "Creates a node which is a copy of an existing placeholder node.", 
          stageInfo,  context, client, 
          nodeName, suffix, 
	  placeholder);

    pPlaceholderNodeName = placeholder;
  }

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
   * @param range
   *   The frame range for the node.
   * 
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   * 
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param placeholder
   *   The name of the placeholder node. 
   */
  protected
  PlaceholderStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range,
   Integer padding,
   String suffix, 
   String placeholder
  )
    throws PipelineException
  {
    super("Placeholder", 
          "Creates a node which is a copy of an existing placeholder node.", 
          stageInfo,  context, client, 
          nodeName, range, padding, suffix, 
	  placeholder);

    pPlaceholderNodeName = placeholder;
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
 
  private static final long serialVersionUID = 9171565769902270856L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * The name of the placeholder Maya scene node. 
   */ 
  private String pPlaceholderNodeName; 


}
