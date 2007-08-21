package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


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
          null, 
          new PluginContext(action));
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.ScriptFile.toString();
  }

  private static final long serialVersionUID = 9094243771431113273L;
}
