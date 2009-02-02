package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class CopyStage 
  extends StandardStage
{
  protected
  CopyStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    String nodeName,
    String suffix,
    String source,
    String stageFunction
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
          new PluginContext("Copy"),
          stageFunction);
    addLink(new LinkMod(source, LinkPolicy.Dependency));
  }
  
  protected
  CopyStage
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
    String source,
    String stageFunction
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
          new PluginContext("Copy"),
          stageFunction);
    addLink(new LinkMod(source, LinkPolicy.Dependency));
  }
  
  
  private static final long serialVersionUID = 5430807656489635181L;
}
