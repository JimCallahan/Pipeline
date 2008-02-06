// $Id: NukeReadReformatStage.java,v 1.4 2008/02/07 14:14:33 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   R E A D   R E F O R M A T   S T A G E                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the NukeReformat action to read one set of images and reformat
 * them to match the resolution of a second set of images.
 */ 
public 
class NukeReadReformatStage 
  extends NukeReadStage
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
   * @param sourceImageName
   *   The name of source image node.
   * 
   * @param resImageName
   *   The name of resolution source image node.
   */
  public
  NukeReadReformatStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String sourceImageName, 
   String resImageName
  )
    throws PipelineException
  {
    super("NukeReadReformat", 
          "Creates a node which uses the NukeRead action.", 
          stageInfo, context, client, 
          nodeName, sourceImageName, 
	  new PluginContext("NukeReformat"));

    addLink(new LinkMod(resImageName, LinkPolicy.Dependency));

    addSingleParamValue("Mode", "Read & Reformat"); 
    addSingleParamValue("ResizeType", "None"); 
    if(resImageName != null) {
      addSingleParamValue("OutputResSource", resImageName); 
      addSingleParamValue("ProxyResSource", resImageName); 
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
    return ICStageFunction.aNukeScript;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -2470341421601450474L;

}
