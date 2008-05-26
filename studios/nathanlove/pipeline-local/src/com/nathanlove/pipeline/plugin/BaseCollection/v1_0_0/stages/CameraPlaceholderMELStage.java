// $Id: CameraPlaceholderMELStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   C A M E R A   P L A C E H O L D E R   M E L   S T A G E                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a MEL script that makes a simple camera node.
 */
public 
class CameraPlaceholderMELStage
  extends FileWriterStage
{
  public 
  CameraPlaceholderMELStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("CameraPlaceholderMEL",
      "Stage to make a placeholder MEL that creates a simple camera",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = 
      "string $cam[] = `camera -centerOfInterest 5 -focalLength 35 -lensSqueezeRatio 1 " +
      "-cameraScale 1 -horizontalFilmAperture 1.41732 -horizontalFilmOffset 0 " +
      "-verticalFilmAperture 0.94488 -verticalFilmOffset 0 -filmFit Fill -overscan 1 " +
      "-motionBlur 0 -shutterAngle 144 -nearClipPlane 0.01 -farClipPlane 1000 " +
      "-orthographic 0 -orthographicWidth 30`; \n" + 
      "rename $cam[0] \"renderCam\";\n" + 
      "cameraMakeNode 1 \"\";\n" ; 
    setFileContents(s);
  }
  private static final long serialVersionUID = -4309432007145683118L;
}
