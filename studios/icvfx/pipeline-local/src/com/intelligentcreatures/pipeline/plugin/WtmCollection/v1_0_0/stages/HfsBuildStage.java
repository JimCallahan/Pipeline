package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   HfsBuildStage		                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which concatenates several scripts together.
 */
public
class HfsBuildStage
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
  HfsBuildStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   ArrayList<String> orderedSources,
   ArrayList<String> unorderedSources,
   boolean useGraphicalLicense,
   String preBuildScript,
   String postBuildScript,
   String preSceneScript,
   String postSceneScript
  )
    throws PipelineException
  {
	    super("HfsBuild",
	    		  "Creates a node which is a Houdini scene.",
	    		  stageInfo, context, client,
	    		  nodeName, "hip",
	    		  null,
	    		  new PluginContext("HfsBuild"));

	    addSingleParamValue("UseGraphicalLicense", useGraphicalLicense);

	    if(orderedSources.size() < 1 && unorderedSources.size() < 1)
	        throw new PipelineException
	  	("At least one source is required for a HfsBuild Action!");

	    addSourceParamValue(orderedSources.get(0), "MergePattern", "*");

	    // Add the scripts
	    if (orderedSources != null)
	    {
		    int order = 100;
		    for (String source : orderedSources)
		    {
		    	addLink(new LinkMod(source, LinkPolicy.Dependency));
		    	addSourceParamValue(source, "Order", order);
		    	order += 50;
		    }
	    }

	    if (unorderedSources != null)
	    {
	    	for (String source : unorderedSources)
	    		addLink(new LinkMod(source, LinkPolicy.Dependency));
	    }

	    if (preBuildScript != null)
	    {
	    	addLink(new LinkMod(preBuildScript, LinkPolicy.Dependency));
	    	addSingleParamValue("PreBuildScript", preBuildScript);
	    }

	    if (postBuildScript != null)
	    {
	    	addLink(new LinkMod(postBuildScript, LinkPolicy.Dependency));
	    	addSingleParamValue("PostBuildScript", postBuildScript);
	    }

	    if (preSceneScript != null)
	    {
	    	addLink(new LinkMod(preSceneScript, LinkPolicy.Dependency));
	    	addSingleParamValue("PreSceneScript", preSceneScript);
	    }

	    if (postSceneScript != null)
	    {
	    	addLink(new LinkMod(postSceneScript, LinkPolicy.Dependency));
	    	addSingleParamValue("PostSceneScript", postSceneScript);
	    }
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

  private static final long serialVersionUID = -3615496839409116201L;

}
