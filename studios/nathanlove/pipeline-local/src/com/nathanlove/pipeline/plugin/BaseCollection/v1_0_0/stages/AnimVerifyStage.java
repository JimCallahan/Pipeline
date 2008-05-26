// $Id: AnimVerifyStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;

/*------------------------------------------------------------------------------------------*/
/*   A N I M   V E R I F Y   S T A G E                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Builds the node where the animation is reapplied to the rigged assets to make sure it works.
 */
public 
class AnimVerifyStage
  extends MayaAnimBuildStage
{
  public 
  AnimVerifyStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    TreeMap<String, String> assets,
    TreeMap<String, String> anim,
    String verifyMel,
    FrameRange range
  )
    throws PipelineException
  {
    super("AnimVerify",
          "Builds the node where the animation is reapplied to the rigged assets " +
          "to make sure it works.",
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName,
          true);
    if (range != null)
      setFrameRange(range);
    for (String namespace : assets.keySet()) {
      String node = assets.get(namespace);
      String animNode = anim.get(namespace);
      setupLink(node, namespace, getReference(), getModel());
      setupLink(animNode, namespace, getReference(), getAnimation());
    }
    
    setAnimMel(verifyMel);
  }

  private static final long serialVersionUID = -2651535408706492915L;
}
