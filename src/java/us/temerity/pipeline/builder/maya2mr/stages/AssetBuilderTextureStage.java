/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.builders
 */
package us.temerity.pipeline.builder.maya2mr.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.PluginContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.stages.NoFrameNumStage;

/**
 * A leaf stage used in the AssetBuilder that builds the texture node.
 * <P>
 * This node is the texture stage of the asset process. It has no sources initially, but
 * does have the ListSources Action assigned to it. Whenever textures are attached to the
 * node, that Action will generate a list of all the textures that are attached. The texture
 * node stage needs to be told its parent node, to which it will attach itself.
 * 
 * @author Jesse Clemens
 */
public 
class AssetBuilderTextureStage 
  extends NoFrameNumStage
{
  /**
   * This constructor will initialize the stage and then runs build to generate the
   * texture node.
   * <p>
   * 
   * @param context
   *            The {@link UtilContext} that this stage acts in.
   * @param nodeName
   *            The name of the node that is to be created.
   * @param parentName
   *            The name of the node that the texture node will be a source of.
   * @throws PipelineException
   */
  public 
  AssetBuilderTextureStage
  (
    UtilContext context, 
    String nodeName, 
    String parentName
  )
    throws PipelineException
  {
    super("AssetBuilderTexture", 
          "Stage to build the texture node", 
          context, 
          nodeName, 
          null, 
          new PluginContext("Emacs"), 
          new PluginContext("ListSources"));
    pParentName = parentName;
  }

  @Override
  public boolean build() 
    throws PipelineException
  {
    boolean toReturn = super.build();
    if(pParentName != null)
      sClient.link(getAuthor(), getView(), pParentName, getNodeName(),
	LinkPolicy.Reference, LinkRelationship.All, null);
    return toReturn;
  }
  
  private String pParentName;
  
}
