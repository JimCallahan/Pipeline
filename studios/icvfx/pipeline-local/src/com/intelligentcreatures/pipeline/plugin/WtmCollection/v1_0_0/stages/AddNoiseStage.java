// $Id: AddNoiseStage.java,v 1.1 2008/03/08 12:27:12 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D   N O I S E   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the noised up blot textures. 
 */ 
public 
class AddNoiseStage
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
   * @param addNoiseName
   *   The name of the node containing the Nuke script which adds the noise.
   * 
   * @param blotNukeName
   *   The name of the node containing the Nuke script to read the blot textures.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  AddNoiseStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String addNoiseName,
    String blotNukeName, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("AddNoise", 
      	  "Creates the noised up blot textures.", 
      	  stageInfo, context, client,
	  nodeName, range, 4, "tif", null, new PluginContext("NukeSubstComp")); 

    addLink(new LinkMod(addNoiseName, LinkPolicy.Dependency));
    addSingleParamValue("MasterScript", addNoiseName); 

    addLink(new LinkMod(blotNukeName, LinkPolicy.Dependency));
    addSourceParamValue(blotNukeName, "ReplaceName", "BlotAnim"); 
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
 
  private static final long serialVersionUID = 4815632206291739539L;

}
