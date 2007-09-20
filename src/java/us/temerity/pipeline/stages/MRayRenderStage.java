package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BaseBuilder.StageFunction;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;


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
    String suffix
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
          new PluginContext("MRayRender"));
    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);
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
    String suffix
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
      new PluginContext("MRayRender"));
    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);
  }
  
  public void
  setSource
  (
    String sourceName,
    SourceType type,
    LinkRelationship relationship
  )
    throws PipelineException
  {
    switch(relationship) {
    case All:
      addLink(new LinkMod(sourceName, LinkPolicy.Dependency));  
    case OneToOne:
      addLink(new LinkMod(sourceName, LinkPolicy.Dependency, relationship, 0));
    case None:
      throw new PipelineException
        ("Cannot have a Link Relationship of (None) with a MRayRender Stage.");
    }
    addSourceParamValue(sourceName, "Contains", type.toString());
  }
  
  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String 
  getStageFunction()
  {
    return StageFunction.RenderedImage.toString();
  }

  
  public 
  enum SourceType 
  {
    ShaderIncs, Lights, Shaders, Geometry, InstGroups, Cameras, CamOverride, Options
  }
  
  private static final long serialVersionUID = -3636358465186210569L;

}
