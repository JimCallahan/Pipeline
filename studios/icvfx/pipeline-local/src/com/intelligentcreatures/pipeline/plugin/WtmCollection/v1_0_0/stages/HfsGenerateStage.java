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
class HfsGenerateStage
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
  HfsGenerateStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String suffix,
   String houdiniScene,
   String outputOperator,
   String cameraOverride,
   boolean useGraphicalLicense,
   String extraOptions
  )
    throws PipelineException
  {
    super("HfsGenerate",
	  "Creates a node which generates a Houdini scene description file.",
	  stageInfo, context, client,
	  nodeName, suffix,
	  null,
	  new PluginContext("HfsGenerate", "Temerity",
			    new Range<VersionID>(new VersionID("2.4.3"), null)));

    initStage(houdiniScene, outputOperator, cameraOverride, useGraphicalLicense, extraOptions);

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
   *
   * @param extraOptions
   *   Additional command-line options.
   */
  public
  HfsGenerateStage
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
   String cameraOverride,
   boolean useGraphicalLicense,
   String extraOptions
  )
    throws PipelineException
  {
    super("HfsGenerate",
	  "Creates a node which generates a Houdini scene description file.",
	  stageInfo, context, client,
	  nodeName, range, padding, suffix,
	  null,
	  new PluginContext("HfsGenerate", "Temerity",
			    new Range<VersionID>(new VersionID("2.4.3"), null)));

    initStage(houdiniScene, outputOperator, cameraOverride, useGraphicalLicense, extraOptions);

    setExecutionMethod(ExecutionMethod.Serial);
    setBatchSize(5);
  }

  private void
  initStage
  (
   String houdiniScene,
   String outputOperator,
   String cameraOverride,
   boolean useGraphicalLicense,
   String extraOptions
  )
  throws PipelineException
  {

	addLink(new LinkMod(houdiniScene, LinkPolicy.Dependency));
	addSingleParamValue("HoudiniScene", houdiniScene);

	if(outputOperator != null)
	  addSingleParamValue("OutputOperator", outputOperator);

	if(cameraOverride != null)
	  addSingleParamValue("CameraOverride", cameraOverride);

	addSingleParamValue("UseGraphicalLicense", useGraphicalLicense);

	if(extraOptions != null)
	  addSingleParamValue("ExtraOptions", extraOptions);

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
