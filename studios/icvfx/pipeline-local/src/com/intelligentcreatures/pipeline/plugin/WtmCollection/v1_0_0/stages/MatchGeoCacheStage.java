// $Id: MatchGeoCacheStage.java,v 1.2 2008/03/03 11:18:59 jim Exp $

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
   * @param geometryName
   *   The name of the geometry shape node to cache. 
   */
  public
  MatchGeoCacheStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String prebakeName, 
    String geometryName
  ) 
    throws PipelineException
  {
    super("MatchGeoCache", 
      	  "Creates the match animation Maya geometry cache.", 
      	  stageInfo, context, client,
	  nodeName, "mc", null, new PluginContext("MayaMakeGeoCache")); 

    addLink(new LinkMod(prebakeName, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", prebakeName); 
    
    addSingleParamValue("GeometryName", geometryName); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 484783363550959037L;

}
