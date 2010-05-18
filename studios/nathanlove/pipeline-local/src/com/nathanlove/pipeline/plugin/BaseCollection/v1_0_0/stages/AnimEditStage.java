// $Id: AnimEditStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;

/*------------------------------------------------------------------------------------------*/
/*   A N I M   E D I T   S T A G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage to make the animation scene the artist works in.
 */
public 
class AnimEditStage
  extends MayaAnimBuildStage
{
  public AnimEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client,
    MayaContext mayaContext,
    String nodeName,
    TreeMap<String, String> assets,
    FrameRange range
  )
    throws PipelineException
  {
    super("AnimEdit",
          "Stage to make the animation scene the artist works in.",
          stageInformation,
          context,
          client,
          mayaContext,
          nodeName,
          true);
    if (range != null)
      setNodeFrameRange(range);
    for (String namespace : assets.keySet()) {
      String node = assets.get(namespace);
      setupLink(node, namespace, getReference(), getModel());
    }
  }
  private static final long serialVersionUID = -7245300798400386323L;

}
