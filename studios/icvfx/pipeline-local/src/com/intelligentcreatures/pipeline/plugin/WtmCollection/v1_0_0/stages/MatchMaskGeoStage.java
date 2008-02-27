// $Id: MatchMaskGeoStage.java,v 1.1 2008/02/27 20:22:22 jim Exp $

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
   * @param exportMEL
   *   The name of the node containing the MEL script to export the OBJ files.
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
    String exportMEL, 
    FrameRange range 
  ) 
    throws PipelineException
  {
    super("MatchMaskGeo", 
      	  "Exports per-frame baked OBJ files of the match animation.", 
      	  stageInfo, context, client,
	  nodeName, range, 4, "obj", null, new PluginContext("MayaMEL")); 

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

    addLink(new LinkMod(exportMEL, LinkPolicy.Dependency));
    addSourceParamValue(exportMEL, "Order", 100);
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
