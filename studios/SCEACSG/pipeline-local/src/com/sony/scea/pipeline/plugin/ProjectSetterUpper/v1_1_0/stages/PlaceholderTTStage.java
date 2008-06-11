// $Id: PlaceholderTTStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;


public 
class PlaceholderTTStage
  extends FileWriterStage
{
  public 
  PlaceholderTTStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    TTType type
  )
    throws PipelineException
  {
    super("PlaceholderTT",
      "Stage to make a placeholder MEL that creates a turntable setup",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "string $cams[] = `camera -focalLength 50`;\n" + 
    	       "rename $cams[0] \"renderCam\";\n" + 
    	       "setAttr \"renderCamShape.farClipPlane\" 100000;\n" + 
    	       "defaultDirectionalLight(1, 1,1,1, \"0\", 0,0,0,0);\n" + 
    	       "setAttr \"directionalLight1.rotateX\" -35;\n" + 
    	       "setAttr \"directionalLight1.rotateY\" -20;\n" + 
    	       "parent directionalLight1 renderCam;\n" + 
    	       "\n"; 
    if (type.equals(TTType.Circle))
      s+= "string $curves[] = `circle -c 0 0 0 -nr 0 1 0 -sw 360 -r 1 -d 3 -ut 0 -tol 0.01 -s 90 -ch 0`;\n" + 
    	  "string $curve = $curves[0];\n" + 
    	  "setAttr ($curve + \".scaleX\") 275;\n" + 
    	  "setAttr ($curve + \".scaleY\") 275;\n" + 
    	  "setAttr ($curve +\" .scaleZ\") 275;\n" + 
    	  "\n" + 
    	  "select -r renderCam ;\n" + 
    	  "select -tgl $curve ;\n" + 
    	  "pathAnimation -fractionMode true -follow true -followAxis x -upAxis y -worldUpType \"vector\" -worldUpVector 0 1 0 -inverseUp false -inverseFront false -bank false -startTimeU 1 -endTimeU 90;\n";
    else if (type.equals(TTType.Center))
      s+= "setKeyframe \"renderCam.ry\";\n" + 
      	  "currentTime 90 ;\n" + 
      	  "setAttr \"renderCam.rotateY\" 360;\n" + 
      	  "setKeyframe \"renderCam.ry\";";
    
    s+= "\n" + 
        "select -r renderCam;\n" + 
        "select -add renderCamShape;\n" + 
        "sets -n CAMERA;\n" + 
        "select -r directionalLight1;\n" + 
        "sets -n LIGHTS;\n";
    setFileContents(s);
  }
  
  public static enum TTType{ Circle, Center};
  
  private static final long serialVersionUID = 5723243852662925026L;
}
