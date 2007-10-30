package us.temerity.pipeline.builder.maya2mr.v2_3_2.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.FileWriterStage;


public 
class PlaceholderSkelMELStage
  extends FileWriterStage
{
  public 
  PlaceholderSkelMELStage
  (
    StageInformation info,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("PlaceholderSkelMELStage",
      "Stage to make a skeleton placeholder MEL that creates geometry with " +
      "the right names for the Adv Asset Builder",
      info,
      context,
      client,
      nodeName,
      "mel",
      StageFunction.aScriptFile);
    
    String s = "spaceLocator -p 0 0 0 -n Reference;\n" + 
    	       "joint -p 0 0 0  -n Root;\n" + 
    	       "select -r Reference;\n" + 
    	       "select -hi;\n" + 
    	       "sets -n SELECT;\n";
    setFileContents(s);
  }
  
  
  
  private static final long serialVersionUID = 5723243852662925026L;
}
