// $Id: QtEncodeStage.java,v 1.1 2008/04/03 01:34:28 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q T   E N C O D E   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the QtEncode action.
 */ 
public 
class QtEncodeStage 
  extends StandardStage
{ 
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new stage which generates a Nuke script using the "Substitute" mode.
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
   * @param sourceMovie
   *   The name of the source QuickTime movie node.
   * 
   * @param codecSettings
   *   The name of the QuickTime codec export settings node.
   */ 
  @SuppressWarnings("unchecked")
  public
  QtEncodeStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String sourceMovie,
   String codecSettings
  )
    throws PipelineException
  {
    super("QtEncode", 
	  "Creates a node which uses the QtEncode action.", 
          stageInfo, context, client, 
          nodeName, "qt", 
	  null, 
	  new PluginContext("QtEncode", "ICVFX")); 

    addLink(new LinkMod(sourceMovie, LinkPolicy.Dependency));
    addSingleParamValue("SourceMovie", sourceMovie); 

    addLink(new LinkMod(codecSettings, LinkPolicy.Dependency));
    addSingleParamValue("CodecSettings", codecSettings); 
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
    return ICStageFunction.aQuickTime;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 394431910036121238L;

}
