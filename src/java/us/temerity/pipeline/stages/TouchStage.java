package us.temerity.pipeline.stages;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class TouchStage 
  extends StandardStage
{
  public
  TouchStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    TreeSet<String> sources
  )
    throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          suffix, 
          null, 
          new PluginContext("Touch"));
    for (String source : sources)
      addLink(new LinkMod(source, LinkPolicy.Dependency));
  }
  
  public
  TouchStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    FrameRange range,
    Integer padding,
    String suffix,
    TreeSet<String> sources
  )
    throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          nodeName, 
          range,
          padding, 
          suffix, 
          null, 
          new PluginContext("Touch"));
    for (String source : sources)
      addLink(new LinkMod(source, LinkPolicy.Dependency));
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

  
  private static final long serialVersionUID = -324541956598968880L;
}
