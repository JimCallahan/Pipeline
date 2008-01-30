/*
 * Created on Jul 8, 2007
 * Created by jesse
 * For Use in us.temerity.pipeline.stages
 * 
 */
package us.temerity.pipeline.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;

public 
class MayaAnimBuildStage 
  extends MayaFileStage
{
  public MayaAnimBuildStage
  (
    String name,
    String desc,
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    boolean isAscii
  )
    throws PipelineException
  {
    super(name, 
          desc, 
          stageInformation, 
          context, 
          client, 
          mayaContext, 
          nodeName, 
          isAscii, 
          null, 
          new PluginContext("MayaAnimBuild"));
    setUnits();
  }
  
  /**
   * Utility method for adding a link to this stage.
   * <p>
   * This method combines two things into one function call. It creates the LinkMod and then
   * sets the correct source parameters. The values that are set depend upon the
   * {@link MayaBuildType} that is passed in. This enum represents the two different cases
   * in which links are typcially made to a MayaBuild node and set the {@link LinkPolicy}
   * and the Source Params to correctly match the selected case.
   * 
   * @param sourceName
   *        The name of the source to be linked to the node.
   * @param namespace
   *        The namespace to be used when the source is imported or referenced.
   * @param buildType
   *        The sort of Maya connection that this link is going to represent. This value
   *        will effect the {@link LinkPolicy} and the source parameters that are set.
   * @param sceneType
   *        Is this file an Animation file (containing curves) or a model file (containing a
   *        rig).
   */
  public void 
  setupLink
  (
    String sourceName, 
    String namespace, 
    MayaBuildType buildType,
    MayaSceneType sceneType
  )
    throws PipelineException
  {
    LinkPolicy policy = null;
    String sourceParamValue = null;
    switch (buildType) {
    case IMPORT:
      policy = LinkPolicy.Dependency;
      sourceParamValue = "Import";
      break;
    case REFERENCE:
      policy = LinkPolicy.Reference;
      sourceParamValue = "Reference";
      break;
    default:
      throw new PipelineException(
        "You must specify a MayaBuildType when you call linkNode()");
    }

    LinkMod link = new LinkMod(sourceName, policy, LinkRelationship.All, null);
    addLink(link);
    addSourceParamValue(sourceName, "PrefixName", namespace);
    addSourceParamValue(sourceName, "BuildType", sourceParamValue);
    switch (sceneType) {
    case ANIMATION:
      addSourceParamValue(sourceName, "SceneType", "Animation");
      break;
    case MODEL:
      addSourceParamValue(sourceName, "SceneType", "Model");
    }
  }

  
  private static final long serialVersionUID = -7074698626849138011L;
  
  /**
   * Utility method to be used with the {@link #setupLink(String, String, MayaBuildType, MayaSceneType)}
   * method.
   * 
   * @return The REFERENCE value of {@link MayaBuildType}.
   */
  public static MayaBuildType 
  getReference()
  {
      return MayaBuildType.REFERENCE;
  }

  /**
   * Utility method to be used with the {@link #setupLink(String, String, MayaBuildType, MayaSceneType)}
   * method.
   * 
   * @return The IMPORT value of {@link MayaBuildType}.
   */
  public static MayaBuildType 
  getImport()
  {
      return MayaBuildType.IMPORT;
  }
  
  public static MayaSceneType
  getModel()
  {
    return MayaSceneType.MODEL;
  }
  
  public static MayaSceneType
  getAnimation()
  {
    return MayaSceneType.ANIMATION;
  }
}
