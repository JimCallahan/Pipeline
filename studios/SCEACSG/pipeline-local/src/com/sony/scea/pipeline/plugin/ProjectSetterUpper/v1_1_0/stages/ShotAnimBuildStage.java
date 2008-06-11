package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;


public 
class ShotAnimBuildStage
extends MayaAnimInstanceBuildStage
{
	public ShotAnimBuildStage
	(
			StageInformation stageInformation,
			UtilContext context,
			MasterMgrClient client,
			MayaContext mayaContext,
			String nodeName,
			TreeMap<String, String> assets,
			TreeMap<String, String> anim,
			TreeMap<String, Integer> assetCounts,
			String buildMEL,
			FrameRange range
	)
	throws PipelineException
	{
		super("ShotAnimBuild",
				"Stage for building an animation scene from models and anim curves",
				stageInformation,
				context,
				client,
				mayaContext,
				nodeName,
				true);
		if (range != null)
			setFrameRange(range);
		for (String namespace : assets.keySet()) {
			String node = assets.get(namespace);
			Integer count = assetCounts.get(namespace);

			if (count == null || count <= 1)
				setupLink(node, namespace, getReference(), getModel(), 0, 0);
			else
				setupLink(node, namespace, getReference(), getModel(), count, 0);
		}
		for (String namespace : anim.keySet()) {
			String node = anim.get(namespace);
			setupLink(node, namespace, getReference(), getAnimation(), 0 ,0);
		}
		setAnimMel(buildMEL);
	}
	private static final long serialVersionUID = 7400008406120638337L;
}
