// $Id: BuildPreMatchStage.java,v 1.1 2008/02/22 09:22:29 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   P R E - M A T C H   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the Maya scene containing the full match animation rig to be animated.
 */ 
public 
class BuildPreMatchStage
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
   * @param constrainMEL
   *   The name of the node contaning MEL script to add the rig constraints.
   * 
   * @param rigName
   *   The name of the node contaning the match rig Maya scene.
   * 
   * @param cameraName
   *   The name of the node containing the extracted camera Maya scene.
   * 
   * @param trackName
   *   The name of the node containing the extracted tracking locators Maya scene.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildPreMatchStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String constrainMEL, 
    String rigName, 
    String cameraName, 
    String trackName, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildPreMatch", 
      	  "Stage to build the Maya scene containing the full match animation rig to " + 
	  "be animated.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    setupLink(rigName, "rig");
    setupLink(cameraName, "cam");
    setupLink(trackName, "track");

    addLink(new LinkMod(constrainMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", constrainMEL);
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
 
  private static final long serialVersionUID = -2849794323217858794L;

}
