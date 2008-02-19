// $Id: PrimaryHDRStage.java,v 1.1 2008/02/19 09:26:36 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I M A R Y   H D R   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which extracts the primary HDR image from the HDR diagnostic node.
 */ 
public 
class PrimaryHDRStage 
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
   * @param diagnosticName
   *   The name of the diagnostic HDR image node. 
   */
  public
  PrimaryHDRStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String diagnosticName
  )
    throws PipelineException
  {
    super("PrimaryHDR", 
	  "Creates a node which extracts the primary HDR image from the HDR diagnostic node.",
	  stageInfo, context, client, 
	  nodeName, "hdr", 
	  null, 
	  new PluginContext("CatFiles")); 

    addLink(new LinkMod(diagnosticName, LinkPolicy.Dependency));
    addSourceParamValue(diagnosticName, "Order", 100);
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
    return ICStageFunction.aHDRImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 5172183234272927379L;

}
