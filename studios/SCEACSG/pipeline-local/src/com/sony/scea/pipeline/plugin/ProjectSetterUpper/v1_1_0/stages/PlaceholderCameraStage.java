// $Id: PlaceholderCameraStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.StageFunction;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.FileWriterStage;


public 
class PlaceholderCameraStage
  extends FileWriterStage
{
  public 
  PlaceholderCameraStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("PlaceholderCameraStage",
      "Stage to make a placeholder MEL that creates a camera setup",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "string $cams[] = `camera -focalLength 50`;\n" + 
    	       "rename $cams[0] \"renderCam\";\n" + 
    	       "setAttr \"renderCamShape.farClipPlane\" 100000;\n" + 
    	       "\n"; 
    
    s+= "\n" + 
        "select -r renderCam;\n" + 
        "select -add renderCamShape;\n" + 
        "sets -n CAMERA;\n";
    s+= "\n" + 
    "select -r renderCam;\n" + 
    "select -add renderCamShape;\n" + 
    "sets -n SELECT;\n"; 

    setFileContents(s);
  }
  private static final long serialVersionUID = 1462477996398925073L;
}
