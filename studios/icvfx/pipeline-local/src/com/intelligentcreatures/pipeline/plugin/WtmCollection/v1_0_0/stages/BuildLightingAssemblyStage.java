package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C A T   S C R I P T   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which concatenates several scripts together.
 */
public
class BuildLightingAssemblyStage
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
   * @param suffix
   *   The target script file suffix.
   *
   * @param sources
   *   The name of source MEL scripts.
   */
  public
  BuildLightingAssemblyStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   ArrayList<String> orderedSources,
   ArrayList<String> unorderedSources,
   boolean useGraphicalLicense
  )
    throws PipelineException
  {
	    super("HfsScript",
	    		  "Creates a node which is a Houdini scene.",
	    		  stageInfo, context, client,
	    		  nodeName, "hip",
	    		  null,
	    		  new PluginContext("HfsScript"));

	    addSingleParamValue("UseGraphicalLicense", useGraphicalLicense);

	    if(orderedSources.size() < 1)
	        throw new PipelineException
	  	("At least one source script is required for a HfsScript Action!");

	    // Add the scripts
	    int order = 100;
	    for (String source : orderedSources)
	    {
	    	addLink(new LinkMod(source, LinkPolicy.Dependency));
	    	addSourceParamValue(source, "Order", order);
	    	order += 50;
	    }

	    for (String source : unorderedSources)
	    	addLink(new LinkMod(source, LinkPolicy.Dependency));

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
    return StageFunction.aHoudiniScene;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3615496839409116231L;

}
