// $Id: BuildTrackingVerifyStage.java,v 1.3 2008/02/19 09:37:11 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   T R A C K I N G   V E R I F Y   S T A G E                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the tracking verification render Maya scene.
 */ 
public 
class BuildTrackingVerifyStage
  extends MayaBuildStage
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
   * @param trackName
   *   The name of the node containg the tracking data Maya scene.
   * 
   * @param modelName
   *   The name of the node containg the test model Maya scene.
   * 
   * @param shaderName
   *   The name of the node containg the test shaders Maya scene.
   * 
   * @param modelMEL
   *   The name of the MEL script to use as the ModelMel parameter.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildTrackingVerifyStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String trackName, 
    String modelName, 
    String shaderName, 
    String modelMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildTrackingVerify", 
      	  "Stage to build the tracking verification render Maya scene.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    setupLink(trackName, "track");
    setupLink(modelName, "mdl");
    setupLink(shaderName, "shd");

    if(modelMEL != null) {
      addLink(new LinkMod(modelMEL, LinkPolicy.Dependency)); 
      addSingleParamValue("ModelMEL", modelMEL);
    }    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Setup a link.
   */ 
  private void 
  setupLink
  (
   String sourceName, 
   String nspace
  )
    throws PipelineException
  {
    addLink(new LinkMod(sourceName, LinkPolicy.Dependency)); 

    addSourceParamValue(sourceName, "PrefixName", nspace);
    addSourceParamValue(sourceName, "BuildType", "Reference");
    addSourceParamValue(sourceName, "NameSpace", true);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 7089145821834900454L;

}
