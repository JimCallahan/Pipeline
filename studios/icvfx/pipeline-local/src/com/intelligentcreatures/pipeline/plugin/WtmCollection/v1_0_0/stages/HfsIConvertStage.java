
package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   HfsGConvert Stage			                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which converts the OBJ format to the BGEO format.
 */
public
class HfsIConvertStage
  extends StandardStage
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
   *
   */
  public
  HfsIConvertStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String sourceNode,
   FrameRange frameRange,
   Integer padding,
   String suffix,
   String colorDepth
  )
    throws PipelineException
  {
	  // TODO: This should probably take a suffix as a parameter ..
	  super("HfsIConvert",
	    	"Creates a node which converts an .rat sequence into a .jpg sequence.",
	    	stageInfo, context, client,
	    	nodeName, frameRange, padding, suffix,
	    	null,
	    	new PluginContext("HfsIConvert"));

	  addSingleParamValue("ImageSource", sourceNode);
	  addSingleParamValue("ColorDepth", colorDepth);
	  addLink(new LinkMod(sourceNode, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * See {@link BaseStage#getStageFunction()}
   */
  @Override
  public String
  getStageFunction()
  {
    return ICStageFunction.aRenderedImage;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2306937786460396875L;

}
