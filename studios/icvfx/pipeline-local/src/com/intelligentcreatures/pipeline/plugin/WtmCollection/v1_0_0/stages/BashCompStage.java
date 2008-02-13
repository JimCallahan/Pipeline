// $Id: BashCompStage.java,v 1.1 2008/02/13 18:56:27 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S H   C O M P   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a simple A over B composite.
 */ 
public 
class BashCompStage
  extends StandardStage
{
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
   *   The frame range to composite.
   * 
   * @param foreground
   *   The name of foreground source images
   * 
   * @param background
   *   The name of background source images.
   */
  public
  BashCompStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   FrameRange range, 
   String foreground,  
   String background
  )
    throws PipelineException
  {
    super("BashCompStage",
          "Generates a simple A over B composite.", 
          stageInfo, context, client,
          nodeName, range, 4, "tif",
          null,
          new PluginContext("Composite"));

    addLink(new LinkMod(background, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));
    addSingleParamValue("Background", background); 
    
    addLink(new LinkMod(foreground, LinkPolicy.Dependency,LinkRelationship.OneToOne, 0)); 
    addSourceParamValue(foreground, "Order", 100); 

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(15);
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 710863347182206757L;

}
