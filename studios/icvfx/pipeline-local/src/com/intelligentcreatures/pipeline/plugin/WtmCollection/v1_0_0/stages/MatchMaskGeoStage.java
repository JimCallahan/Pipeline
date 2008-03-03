// $Id: MatchMaskGeoStage.java,v 1.2 2008/03/03 11:18:59 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T C H   M A S K   G E O   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports per-frame baked OBJ files of the match animation.
 */ 
public 
class MatchMaskGeoStage
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
   * @param exportSet
   *   The name of the Maya Set (or geometry node) to be exported. 
   * 
   * @param range
   *   The frame range of the exported OBJs.
   */
  public
  MatchMaskGeoStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String prebakeName, 
    String exportSet, 
    FrameRange range 
  ) 
    throws PipelineException
  {
    super("MatchMaskGeo", 
      	  "Exports per-frame baked OBJ files of the match animation.", 
      	  stageInfo, context, client,
	  nodeName, range, 4, "obj", null, new PluginContext("MayaObjExport")); 

    addLink(new LinkMod(prebakeName, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", prebakeName); 

    addSingleParamValue("ExportSet", exportSet); 
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
    return ICStageFunction.aObjModel;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -7802179090859510643L;

}
