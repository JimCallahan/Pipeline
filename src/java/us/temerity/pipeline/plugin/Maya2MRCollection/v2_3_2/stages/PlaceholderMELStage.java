// $Id: PlaceholderMELStage.java,v 1.2 2008/02/06 05:11:29 jesse Exp $

package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.StageFunction;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.FileWriterStage;


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
    super("PlaceholderMELStage",
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
