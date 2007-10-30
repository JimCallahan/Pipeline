package us.temerity.pipeline.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


public 
class MRayCamOverrideStage
  extends StandardStage
{
  protected 
  MRayCamOverrideStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super(name, 
         desc, 
         stageInformation, 
         context, 
         client, 
         nodeName, 
         "mi", 
         null, 
         new PluginContext("MRayCamOverride"));
  }
  
  public
  MRayCamOverrideStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName
  )
    throws PipelineException
  {
    super("MRayCamOverride", 
          "Quick version of stage for making a basic cam override node", 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          "mi", 
          null, 
          new PluginContext("MRayCamOverride"));
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.aTextFile;
  }

  
  private static final long serialVersionUID = 4457874075415364187L;
}
