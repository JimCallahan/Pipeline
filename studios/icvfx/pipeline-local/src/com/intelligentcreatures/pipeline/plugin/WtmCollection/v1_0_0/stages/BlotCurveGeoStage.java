// $Id: BlotCurveGeoStage.java,v 1.1 2008/03/06 14:06:48 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B L O T   C U R V E   G E O   S T A G E                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports per-frame baked IGES files of the match animation.
 */ 
public 
class BlotCurveGeoStage
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
   * @param sourceName
   *   The name of the node containing the source Maya scene.
   * 
   * @param exportSet
   *   The name of the Maya Set (or geometry node) to be exported. 
   * 
   * @param range
   *   The frame range of the exported IGESs.
   */
  public
  BlotCurveGeoStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String sourceName, 
    String exportSet, 
    FrameRange range 
  ) 
    throws PipelineException
  {
    super("BlotCurveGeo", 
      	  "Exports per-frame baked IGES files of the match animation.", 
      	  stageInfo, context, client,
	  nodeName, range, 4, "iges", null, new PluginContext("MayaIgesExport")); 

    addLink(new LinkMod(sourceName, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", sourceName); 

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
    return ICStageFunction.aIgesModel;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 2122583952403280346L;

}
