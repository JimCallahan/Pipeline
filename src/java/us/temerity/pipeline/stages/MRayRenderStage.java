package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;


public 
class MRayRenderStage
  extends StandardStage
{
  protected 
  MRayRenderStage
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
    String mayaScene
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
      new PluginContext("FCheck"), 
      new PluginContext("MRayRender"));
    setMayaScene(mayaScene);
  }
  
  protected 
  MRayRenderStage
  (
    String name,
    String desc, 
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String suffix,
    String mayaScene
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
      new PluginContext("FCheck"), 
      new PluginContext("MRayRender"));
    setMayaScene(mayaScene);
  }
  
  private void 
  setMayaScene
  (
   String mayaScene 
  )
    throws PipelineException
  {
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));
    addSingleParamValue("MayaScene", mayaScene);
  }
  
  public void
  setSource
  (
    String sourceName,
    SourceType type
  )
    throws PipelineException
  {
    addLink(new LinkMod(sourceName, LinkPolicy.Dependency));
    addSourceParamValue(sourceName, "Contains", type.toString());
  }
  
  enum SourceType 
  {
    ShaderIncs, Lights, Shaders, Geometry, InstGroups, Cameras, CamOverride, Options
  }
  
  private static final long serialVersionUID = -3636358465186210569L;

}
