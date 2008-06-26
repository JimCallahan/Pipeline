package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Do Copy Stage                                                 			*/
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which runs Maya MEL scripts on the provided scene.
 */
public
class DoCopyStage
  extends CopyStage
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new stage.
   *
   * @param stageInfo
   *   Class containing basic information shared among all stages.
   *
   * @param context
   *   The {@link UtilContext} that this stage acts in.
   *
   * @param client
   *   The instance of Master Manager that the stage performs all its actions in.
   *
   * @param nodeName
   *   The name of the node that is to be created.
   */
  public
  DoCopyStage
  (
   String name,
   String desc,
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String suffix,
   String source
  )
    throws PipelineException
  {
	  super(name, desc,
			  stageInfo, context, client,
			  nodeName, suffix, source);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1900200318984798400L;

}
