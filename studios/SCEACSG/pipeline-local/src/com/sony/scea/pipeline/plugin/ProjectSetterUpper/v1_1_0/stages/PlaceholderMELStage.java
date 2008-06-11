// $Id: PlaceholderMELStage.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.*;


public 
class PlaceholderMELStage
  extends FileWriterStage
{
  public 
  PlaceholderMELStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("PlaceholderMEL",
      "Stage to make a placeholder MEL that creates geometry with the right names for the Adv Asset Builder",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "string $fileName = `file -q -sn -shn` ;\n" + 
    	       "string $buffer[];\n" + 
    	       "tokenize($fileName, \".\", $buffer);\n" + 
    	       "string $geo = ($buffer[0] + \"_geo\");\n" + 
    	       "polySphere -r 1 -sx 20 -sy 20 -ax 0 1 0 -tx 2 -ch 0 -n $geo;\n";
    setFileContents(s);
  }
  
  
  
  private static final long serialVersionUID = 5723243852662925026L;
}
