/*
 * Created on Sep 18, 2006 Created by jesse For Use in us.temerity.pipeline.stages
 */
package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages;

import us.temerity.pipeline.MasterMgrClient;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.MayaContext;
import us.temerity.pipeline.builder.UtilContext;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.stages.FinalizableStage;
import us.temerity.pipeline.stages.MayaBuildStage;

/**
 * A leaf stage used in the AssetBuilder that builds the model node.
 * <p>
 * This node is the model stage of the asset process. It has the potential to have one
 * source, a mel script that will generate a placeholder model in the scene. It uses the
 * MayaBuild Action to create the scene, whether it is empty or uses the placeholder mel
 * script. This class also has a finishModel method which can be run after the node has been
 * built which will detach the linked mel script and remove the Action.
 * 
 * @author jesse
 */
public 
class AssetBuilderModelStage
extends MayaBuildStage
implements FinalizableStage
{
	/**
	 * This constructor initializes the stage and then runs build to generate
	 * the model node.
	 * 
	 * @param context
	 *            The {@link UtilContext} that this stage acts in.
	 * @param mayaContext
	 *            The {@link MayaContext} that this stage acts in.
	 * @param nodeName
	 *            The name of the node that is to be created.
	 * @param placeHolderMel
	 *            The name of the place holder mel script to be run.
	 * @throws PipelineException
	 */
	public 
	AssetBuilderModelStage
	(
			StageInformation stageInformation,
			UtilContext context,
			MasterMgrClient client,
			MayaContext mayaContext,
			String nodeName, 
			String placeHolderMel
	) 
	throws PipelineException
	{
		super("AssetBuilderModel", 
				"Stage to build the model", 
				stageInformation,
				context, 
				client,
				mayaContext,
				nodeName, 
				true);
		pPlaceHolderMel = placeHolderMel;
		setInitialMel(pPlaceHolderMel);
	}

	/**
	 * Finishes off the work of the stage after it has been queued.
	 * <p>
	 * Necessary to clean up the model node after it has been queued at least once. This
	 * will remove the Action that was used to create the node and will disconnect the
	 * placeholder Mel if one was used.
	 * 
	 * @throws PipelineException
	 */
	public void 
	finalizeStage() 
	throws PipelineException
	{
		removeAction();
		if(pPlaceHolderMel != null)
			if (pRegisteredNodeMod.getSourceNames().contains(pPlaceHolderMel))
                          pClient.unlink(getAuthor(), getView(), getNodeName(), pPlaceHolderMel);
	}

	private String pPlaceHolderMel;

	private static final long serialVersionUID = -4169512077820526304L;
}
