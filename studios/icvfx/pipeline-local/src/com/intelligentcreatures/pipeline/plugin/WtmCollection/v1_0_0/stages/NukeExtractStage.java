// $Id: NukeExtractStage.java,v 1.1 2008/02/07 14:14:33 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E   E X T R A C T   S T A G E                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Extracts script fragments containing Nuke nodes from a larger Nuke script.
 */ 
public 
class NukeExtractStage
  extends StandardStage
{
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
   * @param nukeScript
   *   The source node which contains the Nuke script to scan.
   * 
   * @param typePattern
   *   The regular expression to use to match the types of Nuke nodes to extract.
   * 
   * @param namePattern
   *   The regular expression to use to match the names of Nuke nodes to extract.  
   * 
   * @param matchUnnamed
   *   Whether to match node's which do not have a name. 
   */
  public
  NukeExtractStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName, 
   String nukeScript,
   String typePattern,
   String namePattern, 
   boolean matchUnnamed
  )
    throws PipelineException
  {
    super("NukeExtractStage",
          "Extracts script fragments containing Nuke nodes from a larger Nuke script.", 
          stageInfo, context, client,
          nodeName, "nk",
          null,
          new PluginContext("NukeExtract"));

    addLink(new LinkMod(nukeScript, LinkPolicy.Dependency));
    addSingleParamValue("NukeScript", nukeScript); 
    addSingleParamValue("TypePattern", typePattern);
    addSingleParamValue("NamePattern", namePattern);
    addSingleParamValue("MatchUnnamed", matchUnnamed);
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
 
  private static final long serialVersionUID = 1635196799864718863L;

}
