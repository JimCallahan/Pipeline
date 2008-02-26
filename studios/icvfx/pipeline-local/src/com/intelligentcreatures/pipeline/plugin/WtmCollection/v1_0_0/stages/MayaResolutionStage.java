// $Id: MayaResolutionStage.java,v 1.1 2008/02/26 09:00:16 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*; 

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E S O L U T I O N   S T A G E                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which uses the MayaResolution action
 */ 
public 
class MayaResolutionStage 
  extends MELFileStage
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
   * @param imageSource
   *   The name of source image node. 
   * 
   * @param ratio 
   *   The pixel aspect ratio of the source image.
   */
  public
  MayaResolutionStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client, 
   String nodeName, 
   String imageSource, 
   Double ratio
  )
    throws PipelineException
  {
    super("MayaResolution", 
	  "Creates a node which uses the MayaResolution action.", 
	  stageInfo, context, client, 
	  nodeName, 
	  null, 
	  new PluginContext("MayaResolution")); 

    addLink(new LinkMod(imageSource, LinkPolicy.Dependency));

    addSingleParamValue("ImageSource", imageSource); 
    addSingleParamValue("PixelAspectRatio", ratio);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  //  private static final long serialVersionUID = 

}
