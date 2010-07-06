package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import java.util.TreeMap;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.MayaAnimBuildStage;


public 
class AnimEditStage
extends MayaAnimInstanceBuildStage
{
	public AnimEditStage
	(
			StageInformation stageInformation,
			UtilContext context,
			MasterMgrClient client,
			MayaContext mayaContext,
			String nodeName,
			TreeMap<String, String> assets,
			TreeMap<String, Integer> counts,
			String cameraName,
			String cameraAnim,
			FrameRange range
	)
	throws PipelineException
	{
		super("AnimEdit",
				"Stage to make an animation scene with camera animation",
				stageInformation,
				context,
				client,
				mayaContext,
				nodeName,
				true);
		if (range != null)
			setNodeFrameRange(range);
		for (String namespace : assets.keySet()) {
			String node = assets.get(namespace);
			setupLink(node, namespace, getReference(), getModel(), counts.get(namespace), 0);
		}
		if (cameraName != null) {
			setupLink(cameraName, "renderCam", getReference(), getModel(), 0, 0);
		}
		if (cameraAnim != null)
			setupLink(cameraAnim, "renderCam", getImport(), getAnimation(), 0, 0);
	}
	private static final long serialVersionUID = -7245300798400386323L;

}
