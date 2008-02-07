// $Id: NukeRescaleStage.java,v 1.2 2008/02/07 15:48:21 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   R E S C A L E   S T A G E                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the NukeRescale action.
 */ 
public 
class NukeRescaleStage 
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
   * @param imageSource
   *   The name of source images node. 
   * 
   * @param scale
   */
  public
  NukeRescaleStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String suffix,
   String imageSource, 
   Double scale
  )
    throws PipelineException
  {
    super("NukeRescale", 
	  "Creates a node which uses the NukeRescale action.", 
	  stageInfo, context, client, 
	  nodeName, suffix, 
	  null, 
	  new PluginContext("NukeRescale")); 

    addLink(new LinkMod(imageSource, LinkPolicy.Dependency));

    addSingleParamValue("ImageSource", imageSource); 
    addSingleParamValue("Scale", scale);    
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
   * @param imageSource
   *   The name of source images node. 
   * 
   * @param scale
   */
  public
  NukeRescaleStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   FrameRange range,
   Integer padding,
   String suffix,
   String imageSource, 
   Double scale
  )
    throws PipelineException
  {
    super("NukeRescale", 
	  "Creates a node which uses the NukeRescale action.", 
	  stageInfo, context, client, 
	  nodeName, range, padding, suffix, 
	  null, 
	  new PluginContext("NukeRescale")); 

    addLink(new LinkMod(imageSource, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));

    addSingleParamValue("ImageSource", imageSource); 
    addSingleParamValue("Scale", scale);  

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);   
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
 
  private static final long serialVersionUID = -4167005876301019960L;

}
