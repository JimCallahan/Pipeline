//$Id: AssetBuilder.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.LinkedList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.StageInformation;
import us.temerity.pipeline.builder.v2_4_1.NodePurpose;
import us.temerity.pipeline.builder.v2_4_1.TaskType;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;

import com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/


/**
 * Standard Asset Builder for the Nathan Love Base Collection.
 */
public 
class AssetBuilder
extends BaseAssetBuilder
{
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Default constructor that allows for standalone invocation.
	 */
	public
	AssetBuilder
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation builderInformation
	)
	throws PipelineException
	{
		this(mclient, qclient, builderInformation, 
				new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)));
	}

	public 
	AssetBuilder
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation builderInformation,
			StudioDefinitions studioDefinitions
	)
	throws PipelineException
	{
		super("AssetBuilder",
				"Standard Asset Builder for the Temerity Base Collection.",
				mclient, qclient, builderInformation, studioDefinitions);

		pStudioDefinitions = studioDefinitions;

		addSetupPass(new InformationPass());
		addConstructPass(new BuildPass());
		addConstructPass(new FinalizePass());

		setLayout(getBuilderLayout());
	}



	/*----------------------------------------------------------------------------------------*/
	/*  V E R I F I C A T I O N                                                               */
	/*----------------------------------------------------------------------------------------*/

	@SuppressWarnings("unchecked")
	@Override
	public MappedArrayList<String, PluginContext> 
	getNeededActions()
	{
		MappedArrayList<String, PluginContext> toReturn = 
			new MappedArrayList<String, PluginContext>();

		String toolset = getToolset();

		{
			Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.4"), null);
			toReturn.put(toolset, new PluginContext("MayaBuild", "Temerity", range));
		}
		{
			Range<VersionID> range = new Range<VersionID>(new VersionID("2.2.1"), null);
			toReturn.put(toolset, new PluginContext("Touch", "Temerity", range));
		}
		{
			Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.15"), null);
			toReturn.put(toolset, new PluginContext("Copy", "Temerity", range));
		}
		{
			Range<VersionID> range = new Range<VersionID>(new VersionID("2.4.1"), null);
			toReturn.put(toolset, new PluginContext("MayaFTNBuild", "Temerity", range));
		}
		{
			Range<VersionID> range = new Range<VersionID>(new VersionID("2.3.1"), null);
			toReturn.put(toolset, new PluginContext("MayaShaderExport", "Temerity", range));
		}

		return toReturn;
	}



	/*----------------------------------------------------------------------------------------*/
	/*   F I R S T   L O O P                                                                  */
	/*----------------------------------------------------------------------------------------*/

	private 
	class InformationPass
	extends SetupPass
	{
		public 
		InformationPass()
		{
			super("Information Pass", 
			"Information pass for the AssetBuilder");
		}

		@SuppressWarnings("unchecked")
		@Override
		public void 
		validatePhase()
		throws PipelineException
		{
			validateBuiltInParams();
			pStudioDefinitions.setContext(pContext);
			getStageInformation().setDoAnnotations(true);

			pMayaContext = (MayaContext) getParamValue(ParamNames.aMayaContext);

			pAssetType = pAssetNames.getAssetType();

			pProjectName = pProjectNames.getProjectName();
			pTaskName = pAssetNames.getTaskName();

			pRequiredNodes = new TreeSet<String>();
			pRequiredNodes.add(pProjectNames.getModelPlaceholderMEL());
			pRequiredNodes.add(pProjectNames.getModelVerificationMEL());
			pRequiredNodes.add(pProjectNames.getRigVerificationMEL());
			pRequiredNodes.add(pProjectNames.getRigFinalizeMEL(pAssetType));
			pRequiredNodes.add(pProjectNames.getShadeFinalizeMEL(pAssetType));
			pRequiredNodes.add(pProjectNames.getShadeVerificationMEL());

		}

		private static final long serialVersionUID = 7409773506214092503L;
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S E C O N D   L O O P                                                                */
	/*----------------------------------------------------------------------------------------*/


	private
	class BuildPass
	extends ConstructPass
	{
		public 
		BuildPass()
		{
			super("Build Pass", 
			"The Pass which constructs the node networks.");
		}

		@Override
		public void 
		buildPhase()
		throws PipelineException
		{
			pFinalizeStages = new LinkedList<FinalizableStage>();
			pVouchStages = new LinkedList<FinalizableStage>();
			pStageInfo = getStageInformation();
			buildModel();
			buildRig();
			buildTexture();
			buildShade();
		}

		private void
		buildModel()
		throws PipelineException
		{
			String type = TaskType.Modeling.toTitle();
			String editModel = pAssetNames.getModelEditScene();
			String verifyModel = pAssetNames.getModelVerifyScene();
			String modelTT = pAssetNames.getModelTTScene();
			String modelTTImg = pAssetNames.getModelTTRender();
			String modelTTMov = pAssetNames.getModelTTMovie();
			String modelTTSetup = pProjectNames.getModelTTSetup();
			String modelTTThumb = pAssetNames.getModelTTThumbnail();
			
			LockBundle bundle = new LockBundle();

			{
				ModelEditStage stage = 
					new ModelEditStage
					(pStageInfo, pContext, pClient, pMayaContext, 
							editModel, pProjectNames.getModelPlaceholderMEL());
				addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
				if (stage.build())
				{
					pFinalizeStages.add(stage);
					pVouchStages.add(stage);
				}
			}
			{
				AssetVerifyStage stage =
					new AssetVerifyStage
					(pStageInfo, pContext, pClient, pMayaContext,
							verifyModel, editModel, pProjectNames.getModelVerificationMEL());
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
			}

			{
				AssetBuilderTTStage stage =
					new AssetBuilderTTStage
					(pStageInfo,
							pContext,
							pClient,
							pMayaContext,
							modelTT,
							verifyModel,
							modelTTSetup);
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
				addToDisableList(modelTT);
			}
			{
				String globals = pProjectNames.getModelGlobalsMEL();
				AssetBuilderTTImgStage stage =
					new AssetBuilderTTImgStage
					(pStageInfo, 
							pContext, 
							pClient, 
							modelTTImg, 
							modelTT, 
							globals,
							Renderer.Software);
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
			}

			{
				ShakeQuickTimeStage stage =
					new ShakeQuickTimeStage
					(pStageInfo, 
							pContext, 
							pClient,
							modelTTMov,
							640,
							480,
							modelTTImg);
				addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
				stage.build();
			}

			{
				QuickTimeThumbnailStage stage = 
					new QuickTimeThumbnailStage(pStageInfo, pContext, pClient, modelTTThumb, "png", modelTTMov, 160, 120, 1);
				addTaskAnnotation(stage, NodePurpose.Thumbnail, pProjectName, pTaskName, type);
				stage.build();
			}

			String modelApprove = pAssetNames.getModelApproveNode();
			{
				String modelSubmit = pAssetNames.getModelSubmitNode();
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(modelTTThumb);
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelSubmit, sources);
				addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
				if (stage.build()) {
					addToQueueList(modelSubmit);
					addToCheckInList(modelSubmit);
				}
			}

			String modelFinal = pAssetNames.getModelProductScene();
			{
				ProductStage stage = 
					new ProductStage(pStageInfo, pContext, pClient, modelFinal, "ma", verifyModel, StageFunction.aMayaScene.toString());
				addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
				stage.build();
				bundle.addNodeToLock(modelFinal);
			}

			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(modelFinal);
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelApprove, sources);
				addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
						new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask"));
				if (stage.build()) {
					addToQueueList(modelApprove);
					addToCheckInList(modelApprove);
				}
			}
		}

		private void
		buildRig()
		throws PipelineException
		{
			String type = TaskType.Rigging.toTitle();
			LockBundle bundle = new LockBundle();

			String modelProduct = pAssetNames.getModelProductScene();
			bundle.addNodeToLock(modelProduct);

			String skeleton = pAssetNames.getSkeletonNodeName();
			String skelMel = pProjectNames.getPlaceholderSkelScriptName();
			{
				AssetBuilderModelStage stage =
					new AssetBuilderModelStage(pStageInfo, pContext, pClient, pMayaContext, skeleton, skelMel);
				addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
				if (stage.build()) 
				{
					pFinalizeStages.add(stage);
					pVouchStages.add(stage);
					//addToDisableList(rigEdit);
				}
			}

			String rigEdit = pAssetNames.getRigEditScene();
			{
				FancyRigEditStage stage = 
					new FancyRigEditStage
					(pStageInfo, pContext, pClient, pMayaContext,
							rigEdit,
							modelProduct, null, null, skeleton, null, null, null);
				addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
				//stage.build();
				if (stage.build()) 
				{
					pFinalizeStages.add(stage);
					//pVouchStages.add(stage);
					//addToDisableList(rigEdit);
				}
			}

			String rigVerify = pAssetNames.getRigVerifyScene();
			{
				AssetVerifyStage stage =
					new AssetVerifyStage
					(pStageInfo, pContext, pClient, pMayaContext,
							rigVerify, rigEdit, pProjectNames.getRigFinalizeMEL(pAssetType));
				addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
				stage.build();
			}
			String rigApprove = pAssetNames.getRigApproveNode();
			{
				String rigSubmit = pAssetNames.getRigSubmitNode();
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(rigVerify);
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigSubmit, sources);
				addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
				if (stage.build()) {
					addToQueueList(rigSubmit);
					addToCheckInList(rigSubmit);
					bundle.addNodeToCheckin(rigSubmit);
				}
			}
			String rigFinal = pAssetNames.getRigProductScene();
			{
				ProductStage stage = 
					new ProductStage(pStageInfo, pContext, pClient, rigFinal, "ma", rigVerify, StageFunction.aMayaScene.toString());
				addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
				stage.build();
			}
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(rigFinal);
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigApprove, sources);
				addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
						new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask"));
				if (stage.build()) {
					addToQueueList(rigApprove);
					addToCheckInList(rigApprove);
					bundle.addNodeToCheckin(rigApprove);
				}
			}
			if (!bundle.getNodesToCheckin().isEmpty())
				addLockBundle(bundle);
		}

		private void
		buildTexture()
		throws PipelineException
		{
			String type = TaskType.Shading.toTitle();

			String texNode = pAssetNames.getTextureEditNode();
			{
				MayaFTNBuildStage stage = 
					new MayaFTNBuildStage(pStageInfo, pContext, pClient, pMayaContext, texNode, true);
				addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
				stage.build();
			}

			String finalTex = pAssetNames.getTextureProductNode();
			{
				TargetStage stage = 
					new TargetStage(pStageInfo, pContext, pClient, finalTex, new TreeSet<String>());
				addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
				stage.build();
			}
		}

		private void
		buildShade()
		throws PipelineException
		{
			String type = TaskType.Shading.toTitle();

			String modelFinal = pAssetNames.getModelProductScene();

			LockBundle bundle = new LockBundle();

			String shadeTT = pAssetNames.getShadeTTScene();
			String shadeTTImg = pAssetNames.getShadeTTRender();
			String shadeTTMov = pAssetNames.getShadeTTMovie();
			String shadeTTSetup = pProjectNames.getModelTTSetup();
			String shadeTTThumb = pAssetNames.getShadeTTThumbnail();

			String shdName = pAssetNames.getShadeEditScene();
			{
				ShadeEditStage stage =
					new ShadeEditStage
					(pStageInfo, pContext, pClient, pMayaContext,
							shdName, modelFinal,
							pAssetNames.getTextureEditNode());
				addTaskAnnotation(stage, NodePurpose.Edit, pProjectName, pTaskName, type);
				if (stage.build())
					addToDisableList(shdName);
			}

			String shadeExport = pAssetNames.getShaderExportScene();
			{
				ShaderExportStage stage = 
					new ShaderExportStage
					(pStageInfo, pContext, pClient, 
							shadeExport, shdName,
							pProjectNames.getShadeVerificationMEL());
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
			}

			String rigProduct = pAssetNames.getRigProductScene();
			bundle.addNodeToLock(rigProduct);

			String shdFinal = pAssetNames.getShadeFinalScene();
			{
				ShadeFinalStage stage = 
					new ShadeFinalStage
					(pStageInfo,
							pContext, 
							pClient,
							pMayaContext,
							shdFinal, 
							rigProduct, shdName, shadeExport,
							pProjectNames.getShadeFinalizeMEL(pAssetType));
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
			}
			{
				AssetBuilderTTStage stage =
					new AssetBuilderTTStage
					(pStageInfo,
							pContext,
							pClient,
							pMayaContext,
							shadeTT,
							shdFinal,
							shadeTTSetup);
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
				addToDisableList(shadeTT);
			}

			{
				String globals = pProjectNames.getShadeGlobalsMEL();
				AssetBuilderMRayTTImgStage stage =
					new AssetBuilderMRayTTImgStage
					(pStageInfo, 
							pContext, 
							pClient, 
							shadeTTImg, 
							shadeTT, 
							globals);
				addTaskAnnotation(stage, NodePurpose.Prepare, pProjectName, pTaskName, type);
				stage.build();
			}

			{
				ShakeQuickTimeStage stage =
					new ShakeQuickTimeStage
					(pStageInfo, 
							pContext, 
							pClient,
							shadeTTMov,
							640,
							480,
							shadeTTImg);
				addTaskAnnotation(stage, NodePurpose.Focus, pProjectName, pTaskName, type);
				stage.build();
			}

			{
				QuickTimeThumbnailStage stage = 
					new QuickTimeThumbnailStage(pStageInfo, pContext, pClient, shadeTTThumb, "png", shadeTTMov, 160, 120, 1);
				addTaskAnnotation(stage, NodePurpose.Thumbnail, pProjectName, pTaskName, type);
				stage.build();
			}

			String shdApprove = pAssetNames.getShadeApproveNode();
			{
				String shdSubmit = pAssetNames.getShadeSubmitNode();
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(shadeTTThumb);
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdSubmit, sources);
				addSubmitTaskAnnotation(stage, pProjectName, pTaskName, type);
				if (stage.build()) {
					addToQueueList(shdSubmit);
					addToCheckInList(shdSubmit);
					bundle.addNodeToCheckin(shdSubmit);
				}
			}
			String shdProduct = pAssetNames.getShadeProductScene();
			{
				ProductStage stage = 
					new ProductStage(pStageInfo, pContext, pClient, shdProduct, "ma", shdFinal, StageFunction.aMayaScene.toString());
				addTaskAnnotation(stage, NodePurpose.Product, pProjectName, pTaskName, type);
				stage.build();
			}
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(shdProduct);
				sources.add(pAssetNames.getTextureProductNode());
				TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdApprove, sources);
				addApproveTaskAnnotation(stage, pProjectName, pTaskName, type, 
						new BuilderID("Approval", new VersionID("2.4.1"), "Temerity", "ApproveTask"));
				if (stage.build()) {
					addToQueueList(shdApprove);
					addToCheckInList(shdApprove);
					bundle.addNodeToCheckin(shdApprove);

				}
			}
			if (!bundle.getNodesToCheckin().isEmpty())
				addLockBundle(bundle);
		}

		@Override
		public TreeSet<String> 
		nodesDependedOn()
		{
			return pRequiredNodes;
		}

		private static final long serialVersionUID = 1476189272491840367L;

		private StageInformation pStageInfo;
	}



	/*----------------------------------------------------------------------------------------*/
	/*  S T A T I C   I N T E R N A L S                                                       */
	/*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = 211203648424077939L;
}
