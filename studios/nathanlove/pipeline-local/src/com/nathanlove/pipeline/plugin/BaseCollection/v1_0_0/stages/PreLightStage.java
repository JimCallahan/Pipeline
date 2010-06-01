// $Id: PreLightStage.java,v 1.1 2008/05/26 03:19:52 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;

/*------------------------------------------------------------------------------------------*/
/*   P R E   L I G H T   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds the node where the animation is applied to the lighting assets
 * for the lighters to work with.
 */
public 
class PreLightStage
  extends MayaAnimBuildStage
{
  public 
  PreLightStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    TreeMap<String, String> assets,
    TreeMap<String, String> anim,
    String textureNode,
    String verifyMel,
    FrameRange range
  )
    throws PipelineException
  {
    super("PreLight",
          "Builds the node where the animation is applied to the lighting assets " +
          "for the lighters to work with.",
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName,
          true);
    if (range != null)
      setMayaFrameRange(range);
    for (String namespace : assets.keySet()) {
      String node = assets.get(namespace);
      String animNode = anim.get(namespace);
      setupLink(node, namespace, getReference(), getModel());
      setupLink(animNode, namespace, getReference(), getAnimation());
    }
    
    LinkMod mod = new LinkMod(textureNode, LinkPolicy.Association);
    addLink(mod);
    
    setAnimMel(verifyMel);
  }
  private static final long serialVersionUID = 8466199613681903206L;
}
