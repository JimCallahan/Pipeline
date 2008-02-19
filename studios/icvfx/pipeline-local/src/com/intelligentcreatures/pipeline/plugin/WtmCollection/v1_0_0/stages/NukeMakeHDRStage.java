// $Id: NukeMakeHDRStage.java,v 1.1 2008/02/19 09:26:36 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   M A K E   H D R   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which HDR images using NukeMakeHDR action.
 */ 
public 
class NukeMakeHDRStage 
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
   * @param expTimesName
   *   The name of the (optional) exposure times node. 
   * 
   * @param rawNames
   *   The name of the A, B and C series raw exposures node. 
   */
  public
  NukeMakeHDRStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String expTimesName, 
   ArrayList<String> rawNames 
  )
    throws PipelineException
  {
    super("NukeMakeHDR", 
	  "Creates a node which generates HDR images using the NukeMakeHDR action.", 
	  stageInfo, context, client, 
	  nodeName, "hdr", 
	  null, 
	  new PluginContext("NukeMakeHDR")); 

    if(rawNames.size() != 3) 
      throw new PipelineException
	("Exactly (3) raw exposure nodes are required!"); 

    addSingleParamValue("OutputFormat", "LatLon");
    addSingleParamValue("OutputSize", 256);
    addSingleParamValue("MissingExposure", 394.8);
    addSingleParamValue("BlackPoint", 0.07);
    addSingleParamValue("WhitePoint", 0.95);

    if(expTimesName != null) {
      addLink(new LinkMod(expTimesName, LinkPolicy.Dependency));
      addSingleParamValue("ExposureTimes", expTimesName);
    }

    int order = 100; 
    for(String rname : rawNames) {
      Path path = new Path(rname);
      String prefix = path.getName();
      addSecondarySequence(new FileSeq(prefix + "_combo", "hdr"));
      addSecondarySequence(new FileSeq(prefix + "_fix", "hdr"));
      addSecondarySequence(new FileSeq(prefix + "_nofix", "hdr"));
      addSecondarySequence(new FileSeq(prefix + "_wts", "hdr"));

      addLink(new LinkMod(rname, LinkPolicy.Dependency));
      addSourceParamValue(rname, "Order", order);
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
    return ICStageFunction.aHDRImage;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -1900200318984798412L;

}
