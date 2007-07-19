package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


public 
class MELFileStage
  extends StandardStage
{
  protected
  MELFileStage
  (
    String name, 
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    PluginContext editor, 
    PluginContext action
  )
    throws PipelineException
  {
    super(name, 
          desc,
          stageInformation,
          context,
          client,
          nodeName, 
          "mel", 
          editor, 
          action);
  }
  
  public
  MELFileStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String action
  )
    throws PipelineException
  {
    super("MELFileStage", 
          "Quick version of this stage which can be used to build simple MEL scripts.",
          stageInformation,
          context,
          client,
          nodeName, 
          "mel", 
          BaseStage.getDefaultEditor(client, "mel"), 
          new PluginContext(action));
  }
  private static final long serialVersionUID = 9094243771431113273L;
}
