// $Id: HfsReadCmdStage.java,v 1.1 2008/03/23 05:09:58 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   R E A D   C M D   S T A G E                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the HfsReadCmd action.
 */ 
public 
class HfsReadCmdStage 
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
   * @param fileSource
   *   The name of the node to read in.
   * 
   * @param operatorName
   *   The name of the Houdini operator who's parameter which will be set by the generated
   *   command file.
   * 
   * @param parameterName
   *   The name of the Houdini operator's parameter which will be set by the generated
   *   command file.
   */
  public
  HfsReadCmdStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String fileSource, 
   String operatorName,
   String parameterName
  )
    throws PipelineException
  {
    super("HfsReadCmd", 
	 "Creates a node which uses the HfsReadCmd action.", 
          stageInfo, context, client, 
          nodeName, "cmd", 
          null, 
	  new PluginContext("HfsReadCmd"));

    addLink(new LinkMod(fileSource, LinkPolicy.Dependency));
    addSingleParamValue("FileSource", fileSource); 
    
    addSingleParamValue("OperatorName", operatorName); 
    addSingleParamValue("ParameterName", parameterName); 
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
    return StageFunction.aScriptFile;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -6641623862137648357L;

}
