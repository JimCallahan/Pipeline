// $Id: LightingEditStage.java,v 1.1 2008/05/26 03:19:51 jesse Exp $

package com.nathanlove.pipeline.plugin.BaseCollection.v1_0_0.stages;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaBuildStage;

/*------------------------------------------------------------------------------------------*/
/*   L I G H T I N G   E D I T   S T A G E                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Stage to build the scene the artist lights in.
 */
public 
class LightingEditStage
  extends MayaBuildStage
{
  public
  LightingEditStage
  (
    StageInformation stageInformation,
    UtilContext context,
    MasterMgrClient client, 
    MayaContext mayaContext,
    String nodeName,
    String preLightName,
    FrameRange range
  ) 
    throws PipelineException
  {
    super("LightingEdit", 
      	  "Stage to build the scene the artist lights in.",
      	  stageInformation,
          context,
          client,
          mayaContext, 
          nodeName, true);
    if (range != null)
      setFrameRange(range);
    setUnits();
    setupLink(preLightName, "pre", getReference(), true);
  }
  private static final long serialVersionUID = 7757340125934950966L;
}
