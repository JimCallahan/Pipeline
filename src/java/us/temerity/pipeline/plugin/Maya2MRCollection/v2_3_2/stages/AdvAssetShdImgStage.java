package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MRayRenderStage;


public 
class AdvAssetShdImgStage
  extends MRayRenderStage
{
  public 
  AdvAssetShdImgStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    String nodeName,
    String modelName,
    String lightsName,
    String shaderName,
    String cameraName,
    String camOverrideName,
    String optionName
  )
    throws PipelineException
  {
    super("AdvAssetShdImg",
          "Stage to create a mental ray standalone render node for shader viewing",
          stageInformation,
          context,
          client,
          nodeName,
          new FrameRange(1, 90, 1),
          4,
          "tga");
    setSource(modelName, SourceType.Geometry, LinkRelationship.All);
    setSource(lightsName, SourceType.Lights, LinkRelationship.OneToOne);
    setSource(shaderName, SourceType.Shaders, LinkRelationship.OneToOne);
    setSource(cameraName, SourceType.Cameras, LinkRelationship.OneToOne);
    setSource(camOverrideName, SourceType.CamOverride, LinkRelationship.All);
    setSource(optionName, SourceType.Options, LinkRelationship.All);
    addSingleParamValue("CameraName", "tt:renderCamShape");
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
      break;
    case OneToOne:
      addLink(new LinkMod(sourceName, LinkPolicy.Dependency, relationship, 0));
      break;
    case None:
      throw new PipelineException
        ("Cannot have a Link Relationship of (None) with a MRayRender Stage.");
    }
    addSourceParamValue(sourceName, "Contains", type.toString());
  }
  private static final long serialVersionUID = 5099001800033780443L;
}
