// $Id: BuildMatchVerifyStage.java,v 1.1 2008/02/22 09:22:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   M A T C H   V E R I F Y   S T A G E                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the match animation verification render Maya scene.
 */ 
public 
class BuildMatchVerifyStage
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
   * @param matchName
   *   The name of the node containg the match animation Maya scene.
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
  BuildMatchVerifyStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String matchName, 
    String shaderName, 
    String modelMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildMatchVerify", 
      	  "Stage to build the match animation verification render Maya scene.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    setupLink(matchName, "match");
    setupLink(shaderName, "shd");

    addLink(new LinkMod(modelMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", modelMEL);
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
 
  private static final long serialVersionUID = 6798280165759488033L;

}
