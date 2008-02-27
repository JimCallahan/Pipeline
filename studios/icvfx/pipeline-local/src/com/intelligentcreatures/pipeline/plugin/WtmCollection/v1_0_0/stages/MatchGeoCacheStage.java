// $Id: MatchGeoCacheStage.java,v 1.1 2008/02/27 20:22:22 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T C H   G E O   C A C H E   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the match animation Maya geometry cache.
 */ 
public 
class MatchGeoCacheStage
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
   * @param prebakeName
   *   The name of the node containing the pre-baked Maya scene.
   * 
   * @param bakeMEL
   *   The name of the node containing the geo-cache baking MEL script.
   */
  public
  MatchGeoCacheStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String prebakeName, 
    String bakeMEL
  ) 
    throws PipelineException
  {
    super("MatchGeoCache", 
      	  "Creates the match animation Maya geometry cache.", 
      	  stageInfo, context, client,
	  nodeName, "mc", null, new PluginContext("MayaMEL")); 

    addLink(new LinkMod(prebakeName, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", prebakeName); 

    addSingleParamValue("SaveResult", false); 
    
    MayaContext mayaContext = new MayaContext();
    if(mayaContext.getLinearUnit() != null)
      addSingleParamValue("LinearUnits", mayaContext.getLinearUnit());
    if(mayaContext.getAngularUnit() != null)
      addSingleParamValue("AngularUnits", mayaContext.getAngularUnit());
    if(mayaContext.getTimeUnit() != null)
      addSingleParamValue("TimeUnits", mayaContext.getTimeUnit());

    addLink(new LinkMod(bakeMEL, LinkPolicy.Dependency));
    addSourceParamValue(bakeMEL, "Order", 100);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 484783363550959037L;

}
