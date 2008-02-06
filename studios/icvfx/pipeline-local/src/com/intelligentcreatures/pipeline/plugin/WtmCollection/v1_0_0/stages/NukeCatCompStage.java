// $Id: NukeCatCompStage.java,v 1.2 2008/02/06 21:33:22 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   C A T   C O M P   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which concatenates a Write node onto the end of a script which will 
 * generate the target imagess. 
 */ 
public 
class NukeCatCompStage 
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
   * @param suffix
   *   The suffix for the created node.
   * 
   * @param sources
   *   The name of source Nuke scripts. 
   */
  public
  NukeCatCompStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String suffix,
   LinkedList<String> sources
  )
    throws PipelineException
  {
    super("NukeCatComp", 
	  "Creates a node which concatenates a Write node onto the end of a script " + 
	  "which will generate the target imagess.", 
	  stageInfo, context, client, 
	  nodeName, suffix, 
	  null, 
	  new PluginContext("NukeCatComp")); 

    int order = 100;
    for(String source : sources) {
      addLink(new LinkMod(source, LinkPolicy.Dependency));
      addSourceParamValue(source, "Order", order);
      order += 50;
    }
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
   * @param sources
   *   The name of source Nuke scripts. 
   */
  public
  NukeCatCompStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range,
   Integer padding,
   String suffix,
   LinkedList<String> sources
  )
    throws PipelineException
  {
    super("NukeCatComp", 
	  "Creates a node which concatenates a Write node onto the end of a script " + 
	  "which will generate the target imagess.", 
	  stageInfo, context, client, 
	  nodeName, range, padding, suffix, 
	  null, 
	  new PluginContext("NukeCatComp")); 

    int order = 100;
    for(String source : sources) {
      addLink(new LinkMod(source, LinkPolicy.Dependency));
      addSourceParamValue(source, "Order", order);
      order += 50;
    }
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
 
  private static final long serialVersionUID = -9051902390137200075L;

}
