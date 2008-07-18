package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   G E N E R A T E   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which Renders a Houdini scene.
 */
public
class HfsGeoStage
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
   *   The suffix for the created node.
   *
   * @param houdiniScene
   *   The name of the Houdini scene to render.
   *
   * @param outputOperator
   *   The name of the render output operator.
   *
   * @param cameraOverride
   *   Overrides the render camera (if set).
   *
   * @param useGraphicalLicense
   *   Whether to use an interactive graphical Houdini license when running hbatch(1).
   *   Normally, hbatch(1) is run using a non-graphical license (-R option).
   *
   * @param extraOptions
   *   Additional command-line options.
   */
  public
  HfsGeoStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String suffix,
   String houdiniScene,
   String outputOperator,
   boolean useGraphicalLicense,
   String extraOptions,
   String preRenderScript,
   String postRenderScript,
   String preFrameScript,
   String postFrameScript
  )
    throws PipelineException
  {
    super("HfsGEO",
      "Creates a node which generates a Houdini geometry files.",
	  stageInfo, context, client,
	  nodeName, suffix,
	  null,
	  new PluginContext("HfsGEO"));

    initStage(houdiniScene, outputOperator, useGraphicalLicense, extraOptions,
    		preRenderScript, postRenderScript, preFrameScript, postFrameScript);

  }

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
   * @param range
   *   The frame range for the node.
   *
   * @param padding
   *   The padding for the file numbers. If this is set to <code>null</code>, a
   *   padding of 4 will be used.
   *
   * @param suffix
   *   The suffix for the created node.
   *
   * @param houdiniScene
   *   The name of the Houdini scene to render.
   *
   * @param outputOperator
   *   The name of the render output operator.
   *
   * @param cameraOverride
   *   Overrides the render camera (if set).
   *
   * @param useGraphicalLicense
   *   Whether to use an interactive graphical Houdini license when running hbatch(1).
   *   Normally, hbatch(1) is run using a non-graphical license (-R option).
   *HfsGeoStage
   * @param extraOptions
   *   Additional command-line options.
   */
  public
  HfsGeoStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   FrameRange range,
   Integer padding,
   String suffix,
   String houdiniScene,
   String outputOperator,
   boolean useGraphicalLicense,
   String extraOptions,
   String preRenderScript,
   String postRenderScript,
   String preFrameScript,
   String postFrameScript
  )
    throws PipelineException
  {
    super("HfsGEO",
	  "Creates a node which generates a Houdini geometry files.",
	  stageInfo, context, client,
	  nodeName, range, padding, suffix,
	  null,
	  new PluginContext("HfsGEO"));

    initStage(houdiniScene, outputOperator, useGraphicalLicense, extraOptions,
    		preRenderScript, postRenderScript, preFrameScript, postFrameScript);

    setExecutionMethod(ExecutionMethod.Parallel);
    setBatchSize(5);
  }

  private void
  initStage
  (
   String houdiniScene,
   String outputOperator,
   boolean useGraphicalLicense,
   String extraOptions,
   String preRenderScript,
   String postRenderScript,
   String preFrameScript,
   String postFrameScript
  )
  throws PipelineException
  {
	addLink(new LinkMod(houdiniScene, LinkPolicy.Dependency));
	addSingleParamValue("HoudiniScene", houdiniScene);

	if(outputOperator != null)
	  addSingleParamValue("OutputOperator", outputOperator);

	addSingleParamValue("UseGraphicalLicense", useGraphicalLicense);

	if(extraOptions != null)
	  addSingleParamValue("ExtraOptions", extraOptions);

	if(preRenderScript != null)
	{
		addSingleParamValue("PreRenderScript", preRenderScript);
		addLink(new LinkMod(preRenderScript, LinkPolicy.Dependency));
	}

	if(postRenderScript != null)
	{
		addSingleParamValue("PostRenderScript", postRenderScript);
		addLink(new LinkMod(postRenderScript, LinkPolicy.Dependency));
	}

	if(preFrameScript != null)
	{
		addSingleParamValue("PreFrameScript", preFrameScript);
		addLink(new LinkMod(preFrameScript, LinkPolicy.Dependency));
	}

	if(postFrameScript != null)
	{
		addSingleParamValue("PostFrameScript", postFrameScript);
		addLink(new LinkMod(postFrameScript, LinkPolicy.Dependency));
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
    return ICStageFunction.aHoudiniScene;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6922282382755257796L;

}
