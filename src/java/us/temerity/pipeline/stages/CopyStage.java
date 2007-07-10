package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.StageInformation;
import us.temerity.pipeline.stages.StandardStage;

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
    String source
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
          new PluginContext("Emacs"), 
          new PluginContext("Copy"));
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
    String source
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
          new PluginContext("Emacs"), 
          new PluginContext("Copy"));
    addLink(new LinkMod(source, LinkPolicy.Dependency));
  }
  private static final long serialVersionUID = 5430807656489635181L;
}
