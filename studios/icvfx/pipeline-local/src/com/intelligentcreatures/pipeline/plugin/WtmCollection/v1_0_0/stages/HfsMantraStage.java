package com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.stages;

import com.intelligentcreatures.pipeline.plugin.WtmCollection.v1_0_0.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   M A N T R A   S T A G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a node which Renders a Houdini scene.
 */
public
class HfsMantraStage
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
   */
  public
  HfsMantraStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   String suffix,
   String inputNode,
   Integer processors,
   Integer width,
   Integer height,
   String ouputFormat,
   String colorDepth,
   String renderFields,
   String extraOptions,
   String renderMode,
   boolean antiAliasing,
   String motionBlur,
   boolean depthOfField,
   boolean globalIllumination,
   Double shadingQuality,
   String aaThreshold,
   Double jitterScale,
   String covingMethod,
   Integer mPolyCacheSize,
   Integer mPolyMaxSplits,
   Integer rayMeshCacheSize,
   Double rayShadingRate,
   String verbosity,
   String profiling
  )
    throws PipelineException
  {
    super("HfsMantra",
	  "Creates a node which renders a Houdini scene.",
	  stageInfo, context, client,
	  nodeName, suffix,
	  null,
	  new PluginContext("HfsMantra", "Temerity",
			    new Range<VersionID>(new VersionID("2.4.3"), null)));

    initStage(inputNode, processors, width, height, ouputFormat,
    		colorDepth, renderFields, extraOptions, renderMode, antiAliasing,
    		motionBlur, depthOfField, globalIllumination, shadingQuality,
    		aaThreshold, jitterScale, covingMethod, mPolyCacheSize, mPolyMaxSplits,
    		rayMeshCacheSize, rayShadingRate, verbosity, profiling);

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
   */
  public
  HfsMantraStage
  (
   StageInformation stageInfo,
   UtilContext context,
   MasterMgrClient client,
   String nodeName,
   FrameRange range,
   Integer padding,
   String suffix,
   String inputNode,
   Integer processors,
   Integer width,
   Integer height,
   String ouputFormat,
   String colorDepth,
   String renderFields,
   String extraOptions,
   String renderMode,
   boolean antiAliasing,
   String motionBlur,
   boolean depthOfField,
   boolean globalIllumination,
   Double shadingQuality,
   String aaThreshold,
   Double jitterScale,
   String covingMethod,
   Integer mPolyCacheSize,
   Integer mPolyMaxSplits,
   Integer rayMeshCacheSize,
   Double rayShadingRate,
   String verbosity,
   String profiling
  )
    throws PipelineException
  {
    super("HfsMantra",
	  "Creates a node which generates a Houdini scene description file.",
	  stageInfo, context, client,
	  nodeName, range, padding, suffix,
	  null,
	  new PluginContext("HfsMantra"));

    initStage(inputNode, processors, width, height, ouputFormat,
    		colorDepth, renderFields, extraOptions, renderMode, antiAliasing,
    		motionBlur, depthOfField, globalIllumination, shadingQuality,
    		aaThreshold, jitterScale, covingMethod, mPolyCacheSize, mPolyMaxSplits,
    		rayMeshCacheSize, rayShadingRate, verbosity, profiling);

    setExecutionMethod(ExecutionMethod.Subdivided);
  }

  private void
  initStage
  (
	  String inputNode,
	  Integer processors,
	  Integer width,
	  Integer height,
	  String ouputFormat,
	  String colorDepth,
	  String renderFields,
	  String extraOptions,
	  String renderMode,
	  boolean antiAliasing,
	  String motionBlur,
	  boolean depthOfField,
	  boolean globalIllumination,
	  Double shadingQuality,
	  String aaThreshold,
	  Double jitterScale,
	  String covingMethod,
	  Integer mPolyCacheSize,
	  Integer mPolyMaxSplits,
	  Integer rayMeshCacheSize,
	  Double rayShadingRate,
	  String verbosity,
	  String profiling
  )
   throws PipelineException
  {
	addLink(new LinkMod(inputNode, LinkPolicy.Dependency, LinkRelationship.OneToOne, 0));
	addSingleParamValue("InputFiles", inputNode);

	if(processors != null)
		addSingleParamValue("Processors", processors);

	if(width != null)
		addSingleParamValue("ImageWidth", width);

	if(height != null)
		addSingleParamValue("ImageHeight", height);

	if(ouputFormat != null)
		addSingleParamValue("OutputFormat", ouputFormat);

	if(colorDepth != null)
		addSingleParamValue("ColorDepth", colorDepth);

	if(colorDepth != null)
		addSingleParamValue("ColorDepth", colorDepth);

	if(renderFields != null)
		addSingleParamValue("RenderFields", renderFields);

	if(extraOptions != null)
		addSingleParamValue("ExtraOptions", extraOptions);

	if(renderMode != null)
		addSingleParamValue("RenderMode", renderMode);

	addSingleParamValue("AntiAliasing", antiAliasing);

	if(motionBlur != null)
		addSingleParamValue("MotionBlur", motionBlur);

	if(colorDepth != null)
		addSingleParamValue("ColorDepth", colorDepth);

	if(colorDepth != null)
		addSingleParamValue("ColorDepth", colorDepth);

	addSingleParamValue("DepthOfField", depthOfField);
	addSingleParamValue("GlobalIllumination", globalIllumination);

	if(shadingQuality != null)
		addSingleParamValue("ShadingQuality", shadingQuality);

	if(aaThreshold != null)
		addSingleParamValue("AntiAliasingThreshold", aaThreshold);

	if(jitterScale != null)
		addSingleParamValue("JitterScale", jitterScale);

	if(covingMethod != null)
		addSingleParamValue("CovingMethod", covingMethod);

	if(mPolyCacheSize != null)
		addSingleParamValue("MicroPolygonCacheSize", mPolyCacheSize);

	if(mPolyMaxSplits != null)
		addSingleParamValue("MicroPolygonMaxSplits", mPolyMaxSplits);

	if(rayShadingRate != null)
		addSingleParamValue("RayShadingRate", rayShadingRate);

	if(verbosity != null)
		addSingleParamValue("Verbosity", verbosity);

	if(profiling != null)
		addSingleParamValue("Profiling", profiling);
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

  private static final long serialVersionUID = 6922382382755257796L;

}
