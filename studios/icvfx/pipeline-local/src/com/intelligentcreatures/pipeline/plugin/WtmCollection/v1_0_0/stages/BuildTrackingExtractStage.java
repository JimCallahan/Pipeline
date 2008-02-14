// $Id: BuildTrackingExtractStage.java,v 1.2 2008/02/14 14:22:51 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D   T R A C K I N G   E X T R A C T   S T A G E                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates the baked camera or tracking locator data scenes.
 */ 
public 
class BuildTrackingExtractStage
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

   * @param extractMEL
   *   The name of the MEL script which bakes and extracts the camera/locators.
   * 
   * @param range
   *   The frame range to give the newly created scene.
   */
  public
  BuildTrackingExtractStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String trackName, 
    String extractMEL, 
    FrameRange range
  ) 
    throws PipelineException
  {
    super("BuildTrackingExtract", 
      	  "Creates the baked camera or tracking locator data scenes.", 
      	  stageInfo, context, client,
          new MayaContext(), nodeName, true);
    
    if(range != null)
      setFrameRange(range);

    setUnits();

    addLink(new LinkMod(trackName, LinkPolicy.Dependency)); 
    addSourceParamValue(trackName, "PrefixName", "track");
    addSourceParamValue(trackName, "BuildType", "Reference");
    addSourceParamValue(trackName, "NameSpace", true);

    addLink(new LinkMod(extractMEL, LinkPolicy.Dependency)); 
    addSingleParamValue("ModelMEL", extractMEL);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 2668988706480167666L;

}
