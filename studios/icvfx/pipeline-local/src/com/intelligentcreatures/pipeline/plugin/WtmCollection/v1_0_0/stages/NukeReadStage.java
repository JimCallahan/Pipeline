// $Id: NukeReadStage.java,v 1.5 2008/03/23 05:09:58 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   R E A D   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the NukeRead action.
 */ 
public 
class NukeReadStage 
  extends StandardStage
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
   * @param imageName
   *   The name of source image node.
   */
  protected
  NukeReadStage
  (
   String name, 
   String desc,
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String imageName,
   PluginContext action
  )
    throws PipelineException
  {
    super(name, desc,
          stageInfo, context, client, 
          nodeName, "nk", 
          null, 
	  action); 

    addLink(new LinkMod(imageName, LinkPolicy.Dependency));
    addSingleParamValue("ImageSource", imageName); 
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
   * @param imageName
   *   The name of source image node.
   */
  public
  NukeReadStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String imageName
  )
    throws PipelineException
  {
    this("NukeRead", 
	 "Creates a node which uses the NukeRead action.", 
	 stageInfo, context, client, 
	 nodeName, imageName, 
	 new PluginContext("NukeRead"));
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
   * @param imageName
   *   The name of source image node.
   * 
   * @param missingFrames
   *   How to handle missing frames. 
   */
  public
  NukeReadStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String imageName, 
   String missingFrames 
  )
    throws PipelineException
  {
    this(stageInfo, context, client, 
	 nodeName, imageName); 
    
    if(missingFrames != null) 
      addSingleParamValue("MissingFrames", missingFrames); 
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7286056856278936588L;

}
