// $Id: RenderTrackingVerifyStage.java,v 1.1 2008/02/13 18:56:27 jim Exp $

package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R E N D E R   T R A C K I N G   V E R I F Y   S T A G E                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Renders the tracking verification images using Maya.
 */ 
public 
class RenderTrackingVerifyStage
  extends MayaRenderStage
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
   * @param range
   *   The frame range to render.
   * 
   * @param mayaScene
   *   The name of the node containing the Maya scene to render.
   * 
   * @param preRenderMEL
   *   The name of the MEL script to use as the PreRenderMel parameter.
   */
  public
  RenderTrackingVerifyStage
  (
    StageInformation stageInfo,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range, 
    String mayaScene, 
    String cameraOverride, 
    String preRenderMEL
  ) 
    throws PipelineException
  {
    super("RenderTrackingVerify", 
      	  "Renders the tracking verification images using Maya.", 
      	  stageInfo, context, client,
          nodeName, range, 4, "tif", 
	  mayaScene); 

    if(cameraOverride != null) 
      addSingleParamValue("CameraOverride", cameraOverride); 

    if(preRenderMEL != null) 
      setPreRenderMel(preRenderMEL);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = -4952867921495925004L;

}
