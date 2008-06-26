package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M E L   S T A G E                                                  			*/
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which runs Maya MEL scripts on the provided scene.
 */
public
class MayaMelStage
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
   * @param mayaScene
   *   The Maya scene to run the scripts on.
   *
   * @param scripts
   *   The scripts to run (run in the order they appear in the list).
   */
  public
  MayaMelStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String mayaScene,
   ArrayList<String> scripts,
   boolean saveResult,
   String linearUnits,
   String angularUnits,
   String timeUnits,
   String suffix
  )
    throws PipelineException
  {
    super("MayaMEL",
	  "Creates a node which runs Maya MEL scripts on the provided scene.",
	  stageInfo, context, client,
	  nodeName, suffix,
	  null,
	  new PluginContext("MayaMEL"));

    if(scripts.size() < 1)
      throw new PipelineException
	("At least one source script is required for a MayaMEL Action!");

    if (mayaScene == null)
        throw new PipelineException
    ("A Maya scene is required for a MayaMEL Action!");

    addSingleParamValue("MayaScene", mayaScene);
    addSingleParamValue("SaveResult", saveResult);

    if (linearUnits != null)
    	addSingleParamValue("LinearUnits", linearUnits);

    if (angularUnits != null)
    	addSingleParamValue("AngularUnits", angularUnits);

    if (timeUnits != null)
    	addSingleParamValue("TimeUnits", timeUnits);

    // Add the Maya scene
    addLink(new LinkMod(mayaScene, LinkPolicy.Dependency));

    // Add the scripts
    int order = 100;
    for (String script : scripts)
    {
    	addLink(new LinkMod(script, LinkPolicy.Dependency));
    	addSourceParamValue(script, "Order", order);
    	order += 50;
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
    return ICStageFunction.aTextFile;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1900200318984798413L;

}
