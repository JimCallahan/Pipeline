//$Id: ProjectBuilder.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BaseBuilder.ConstructPass;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.builder.v2_4_1.NodePurpose;
import us.temerity.pipeline.builder.v2_4_1.TaskType;
import us.temerity.pipeline.stages.*;

import com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   B U I L D E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Builder to create the initial set of scripts and scene files that projects being done with
 * the Nathan Love Base Collection need to function.
 */
public 
class ProjectBuilder
extends BaseBuilder
{
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Default constructor that allows for standalone invocation.
	 */
	public
	ProjectBuilder
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
	ProjectBuilder
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation builderInformation,
			StudioDefinitions studioDefinitions
	) 
	throws PipelineException
	{
		super("Project",
				"The Project Builder that works with the basic Temerity Project Names class.",
				mclient,
				qclient,
				builderInformation);

		pStudioDefs = studioDefinitions;

		{
			UtilityParam param = 
				new StringUtilityParam
				(ParamNames.aProjectName,
						"The name of the project that is being setup.", 
				"projects"); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new BooleanUtilityParam
				(aModifyExisting,
						"If the named project already exists, should this builder go ahead and run anyway." +
						"This can change behavior of existing networks, so should be used carefully.", 
						false); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new MayaContextUtilityParam
				(ParamNames.aMayaContext,
						"The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
						new MayaContext()); 
			addParam(param);
		}

		/* whether to checkin the newly created nodes */ 
		addCheckinWhenDoneParam();


		pProjectNamer = new ProjectNamer(mclient, qclient);
		pStageInfo = getStageInformation();
		addSubBuilder(pProjectNamer);
		addMappedParam(pProjectNamer.getName(), ParamNames.aProjectName, ParamNames.aProjectName);    

		//pFinalizeStages = new LinkedList<FinalizableStage>();

		//setDefaultEditors();

		addSetupPass(new SetupEssentialsPass());
		addConstructPass(new MakeNodesPass());
		addConstructPass(new FinalizePass());

		{
			AdvancedLayoutGroup layout = 
				new AdvancedLayoutGroup
				("Project Information", 
						"The pass where all the basic stageInformation about the asset is collected " +
						"from the user.", 
						"ProjectInformation", 
						true);
			layout.addEntry(1, aUtilContext);
			layout.addEntry(1, null);
			layout.addEntry(1, aActionOnExistence);
			layout.addEntry(1, aReleaseOnError);
			layout.addEntry(1, aCheckinWhenDone);
			layout.addEntry(1, null);
			layout.addEntry(1, ParamNames.aProjectName);
			layout.addEntry(1, aModifyExisting);
			layout.addEntry(1, null);
			layout.addEntry(1, ParamNames.aMayaContext);

			PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
			setLayout(finalLayout);
		}
	}



	/*----------------------------------------------------------------------------------------*/
	/*  V E R I F I C A T I O N                                                               */
	/*----------------------------------------------------------------------------------------*/

	@Override
	public MappedArrayList<String, PluginContext> 
	getNeededActions()
	{
		MappedArrayList<String, PluginContext> toReturn = 
			new MappedArrayList<String, PluginContext>();

		String toolset = getToolset();

		toReturn.put(toolset, new PluginContext("MayaRemoveRefMEL"));
		toReturn.put(toolset, new PluginContext("MayaShdCopyMEL"));
		toReturn.put(toolset, new PluginContext("Touch"));
		toReturn.put(toolset, new PluginContext("CatFiles"));


		return toReturn;
	}



	/*----------------------------------------------------------------------------------------*/
	/*   S E T U P   P A S S E S                                                              */
	/*----------------------------------------------------------------------------------------*/

	private
	class SetupEssentialsPass
	extends SetupPass
	{

		private
		SetupEssentialsPass()
		{
			super("Setup Essentials", 
			"Setup the common builder properties.");
		}

		/**
		 * Phase in which parameter values should be extracted from parameters and checked
		 * for consistency and applicability.
		 */
		@Override
		public void 
		validatePhase()
		throws PipelineException
		{
			/* sets up the built-in parameters common to all builders */ 
			validateBuiltInParams();

			/* setup the StudioDefinitions version of the UtilContext */ 
			pStudioDefs.setContext(pContext);  

			pMayaContext = (MayaContext) getParamValue(ParamNames.aMayaContext);

			pProjectName = getStringParamValue(new ParamMapping(ParamNames.aProjectName));
			if (!getBooleanParamValue(new ParamMapping(aModifyExisting))) {
				ArrayList<String> projects = pStudioDefs.getProjectList();
				if (projects.contains(pProjectName))
					throw new PipelineException
					("The project (" + pProjectName + ") already exists.  " +
							"Cannot use the project builder to create it when the " +
					"Modify Existing flag is not turned on.");
			}

			pEmptyFiles = new LinkedList<BaseStage>();
			pPreBuildStages  = new LinkedList<BaseStage>();
		}

		private static final long serialVersionUID = 2453800092783810792L;
	}



	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T   P A S S E S                                                      */
	/*----------------------------------------------------------------------------------------*/

	private
	class MakeNodesPass
	extends ConstructPass
	{
		private 
		MakeNodesPass()
		{
			super("Make Nodes", 
			"Creates the node networks.");
		}

		@Override
		public void 
		buildPhase()
		throws PipelineException
		{
			//wct - need to collect all the nodes that need to be finalized...
			pFinalizeStages = new LinkedList<FinalizableStage>();

			buildGeneralNodes();
			buildModelNodes();
			buildRigNodes();
			buildShaderNodes();
			//wct - maybe should add a param for whether to do this...
			buildTTNodes();
			buildAnimSetupNodes();
			buildLgtSetupNodes();

		}

		private void 
		buildGeneralNodes()
		throws PipelineException
		{
			//StageInformation stageInfo = getStageInformation();

			/* Circle Turntable Setup Script */
			{
				String script = pProjectNamer.getCircleTurntableMEL();
				PlaceholderTTStage stage = 
					new PlaceholderTTStage(pStageInfo, pContext, pClient, script, 
							PlaceholderTTStage.TTType.Circle);
				if (stage.build())
					addToCheckInList(script);
			}

			/* Center Turntable Setup Script */
			{
				String script = pProjectNamer.getCenterTurntableMEL();
				PlaceholderTTStage stage = 
					new PlaceholderTTStage(pStageInfo, pContext, pClient, script, 
							PlaceholderTTStage.TTType.Center);
				if (stage.build())
					addToCheckInList(script);
			}

			/* Remove Reference MEL */
			{
				String script = pProjectNamer.getRemoveReferenceMEL();
				MELFileStage stage = 
					new MELFileStage(pStageInfo, pContext, pClient, script, "MayaRemoveRefMEL");
				if (stage.build()) {
					addToCheckInList(script);
					//addToQueueList(script);
					pPreBuildStages.add(stage);
				}
			}

			/* Asset verification*/
			{
				String script = pProjectNamer.getAssetVerificationMEL();
				EmptyFileStage stage = 
					new EmptyFileStage(pStageInfo, pContext, pClient, script, "mel");
				if (stage.build()) {
					addToCheckInList(script);
					//addToQueueList(script);
					pEmptyFiles.add(stage);
					pPreBuildStages.add(stage);
				}
			}

			String cameraMel = pProjectNamer.getPlaceholderCameraScriptName();
			{
				PlaceholderCameraStage stage = 
					new PlaceholderCameraStage(pStageInfo, pContext, pClient, cameraMel);
				if (stage.build())
					addToCheckInList(cameraMel);
			}

			//build the renderCam too why don't we...
			{
				AssetNamer cameraNames = new AssetNamer(pClient, pQueue);
				cameraNames.setParamValue(ParamNames.aProjectName, pProjectName);
				cameraNames.setParamValue(ParamNames.aAssetName, "renderCam");
				cameraNames.setParamValue(ParamNames.aAssetType, "cam");
				cameraNames.generateNames();

				String renderCam = cameraNames.getAssetProductScene();
				AssetBuilderModelStage stage = 
					new AssetBuilderModelStage
					(pStageInfo,
							pContext,
							pClient,
							pMayaContext,
							renderCam,
							cameraMel);
				if (stage.build())
				{
					pFinalizeStages.add(stage);
					addToCheckInList(renderCam);
				}
			}

			String skelMel = pProjectNamer.getPlaceholderSkelScriptName();
			{
				PlaceholderSkelMELStage stage = 
					new PlaceholderSkelMELStage(pStageInfo, pContext, pClient, skelMel);
				if (stage.build())
					addToCheckInList(skelMel);
			}
		}

		private void 
		buildModelNodes()
		throws PipelineException
		{
			//  StageInformation stageInfo = getStageInformation();

			/* Model placeholder*/
			{
				String script = pProjectNamer.getModelPlaceholderMEL();
				PlaceholderMELStage stage = 
					new PlaceholderMELStage(pStageInfo, pContext, pClient, script);
				if (stage.build())
					addToCheckInList(script);
			}

			/* Model verification*/
			{
				String script = pProjectNamer.getModelVerificationMEL();
				VerifyModelMELStage stage = 
					new VerifyModelMELStage(pStageInfo, pContext, pClient, script);
				boolean build = stage.build();
				if (build)
					addToCheckInList(script);
			}
		}

		private void 
		buildRigNodes()
		throws PipelineException
		{
			//StageInformation stageInfo = getStageInformation();
			LinkedList<String> collectedScripts = new LinkedList<String>();

			/* Rig Verification */
			{
				String script = pProjectNamer.getRigVerificationMEL();
				EmptyFileStage stage = 
					new EmptyFileStage(pStageInfo, pContext, pClient, script, "mel");
				if (stage.build()) {
					addToCheckInList(script);
					//addToQueueList(script);
					pEmptyFiles.add(stage);
					pPreBuildStages.add(stage);
				}
				collectedScripts.add(script);
			}

			/* Rig Finalize*/
			for (AssetType type : AssetType.values()) {
				String script = pProjectNamer.getRigFinalizeMEL(type);
				CatFilesStage stage = 
					new CatFilesStage(pStageInfo, pContext, pClient, script, "mel", collectedScripts);
				boolean build = stage.build();
				if (build) {
					addToCheckInList(script);
					addToQueueList(script);
				}
			}
		}

		private void 
		buildShaderNodes()
		throws PipelineException
		{
			//StageInformation stageInfo = getStageInformation();
			LinkedList<String> collectedScripts = new LinkedList<String>();

			/* Shader Copy*/
			{
				String script = pProjectNamer.getShaderCopyMEL();
				MELFileStage stage = 
					new MELFileStage(pStageInfo, pContext, pClient, script, "MayaShdCopyMEL");
				if (stage.build()) {
					addToCheckInList(script);
					//addToQueueList(script);
					pPreBuildStages.add(stage);
				}
				collectedScripts.add(script);
			}

			/* Shader Verification */
			{
				String script = pProjectNamer.getShadeVerificationMEL();
				EmptyFileStage stage = 
					new EmptyFileStage(pStageInfo, pContext, pClient, script, "mel");
				if (stage.build()) {
					addToCheckInList(script);
					//addToQueueList(script);
					pEmptyFiles.add(stage);
					pPreBuildStages.add(stage);
				}
			}

			collectedScripts.add(pProjectNamer.getRemoveReferenceMEL());

			/* Shader Finalize */
			for (AssetType type : AssetType.values()) {
				String script = pProjectNamer.getShadeFinalizeMEL(type);
				CatFilesStage stage = 
					new CatFilesStage(pStageInfo, pContext, pClient, script, "mel", collectedScripts);
				boolean build = stage.build();
				if (build) {
					addToCheckInList(script);
					addToQueueList(script);
				}
			}
		}

		private void 
		buildTTNodes()
		throws PipelineException
		{
			//wct - need to add center tt for sets... also hdr setups should be included later!!!
			//StageInformation stageInfo = getStageInformation();
			String ttSetup = pProjectNamer.getModelTTSetup();
			String modelGlobals = pProjectNamer.getModelGlobalsMEL();
			String shadeGlobals = pProjectNamer.getShadeGlobalsMEL();

			//String modSetup = pProjectNamer.getModelTTSetup();
			//String modSetSetup = pProjectNames.getAssetModelTTSetup(null, "set");
			//String rigSetup = pProjectNamer.getAssetRigAnimSetup();
			//String rigSetSetup = pProjectNames.getAssetRigAnimSetup(null, "set");
			//String shdSetup = pProjectNamer.getShaderTTSetup();
			//String shdSetSetup = pProjectNames.getAssetShaderTTSetup(null, "set");

			//String globalHdrSetup = pProjectNames.getGlobalMatLightRigNodeName("character");
			//String globalHdrSetSetup = pProjectNames.getGlobalMatLightRigNodeName("set");
			//String hdrSetup = pProjectNames.getAssetHdrShaderTTSetup(null, "character");
			//String hdrSetSetup = pProjectNames.getAssetHdrShaderTTSetup(null, "set");

			//TreeSet<String> pCircles = new TreeSet<String>();
			//pCircles.add(modSetup);
			//pCircles.add(rigSetup);
			//pCircles.add(shdSetup);
			//TreeSet<String> pCenter = new TreeSet<String>();
			//pCenter.add(modSetSetup);
			//pCenter.add(rigSetSetup);
			//pCenter.add(shdSetSetup);
			{
				ModelEditStage stage = 
					new ModelEditStage(pStageInfo, pContext, pClient, pMayaContext, ttSetup, pProjectNamer.getCircleTurntableMEL());
				if (stage.build()) {
					addToCheckInList(ttSetup);
					//addToQueueList(ttSetup);
					//addToDisableList(ttSetup);
					//stage.finalizeStage();
					pFinalizeStages.add(stage);
				}
			}
			{
				MELFileStage stage =
					new MELFileStage(pStageInfo, pContext, pClient, modelGlobals, "MayaRenderGlobals");
				boolean build = stage.build();
				if (build) {
					addToCheckInList(modelGlobals);
					addToQueueList(modelGlobals);
				}
			}

			{
				MELFileStage stage =
					new MELFileStage(pStageInfo, pContext, pClient, shadeGlobals, "MRayRenderGlobals");
				boolean build = stage.build();
				if (build) {
					addToCheckInList(shadeGlobals);
					addToQueueList(shadeGlobals);
				}
			}
		}

		private void 
		buildAnimSetupNodes()
		throws PipelineException
		{
			String animGlobals = pProjectNamer.getAnimGlobals();

			{
				MELFileStage stage =
					new MELFileStage(pStageInfo, pContext, pClient, animGlobals, "MayaRenderGlobals");
				boolean build = stage.build();
				if (build) {
					addToCheckInList(animGlobals);
					addToQueueList(animGlobals);
				}
			}
		}

		private void 
		buildLgtSetupNodes()
		throws PipelineException
		{
			String lgtGlobals = pProjectNamer.getLgtGlobals();
			String lgtMayaGlobals = pProjectNamer.getMayaLgtGlobals();

			{
				MELFileStage stage =
					new MELFileStage(pStageInfo, pContext, pClient, lgtMayaGlobals, "MayaRenderGlobals");
				boolean build = stage.build();
				if (build) {
					addToCheckInList(lgtMayaGlobals);
					addToQueueList(lgtMayaGlobals);
				}
			}

			{
				MELFileStage stage =
					new MELFileStage(pStageInfo, pContext, pClient, lgtGlobals, "MRayRenderGlobals");
				boolean build = stage.build();
				if (build) {
					addToCheckInList(lgtGlobals);
					addToQueueList(lgtGlobals);
				}
			}
		}

		private static final long serialVersionUID = -3538851156066717973L;
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S E C O N D   L O O P                                                                */
	/*----------------------------------------------------------------------------------------*/

	protected
	class FinalizePass
	extends ConstructPass
	{
		public 
		FinalizePass()
		{
			super("FinalizePass", 
			"The AssetBuilder pass that cleans everything up.");
		}

		@Override
		public LinkedList<String> 
		preBuildPhase()
		{
			LinkedList<String> regenerate = new LinkedList<String>();

			regenerate.addAll(getDisableList());
			for(FinalizableStage stage : pFinalizeStages) 
				regenerate.add(stage.getNodeName());
			for (BaseStage s : pPreBuildStages)
				regenerate.add(s.getNodeName());

			return regenerate;
		}

		@Override
		public void 
		buildPhase() 
		throws PipelineException
		{
			MasterMgrClient mclient = new MasterMgrClient();	

			for(FinalizableStage stage : pFinalizeStages) 
			{
				stage.finalizeStage();
				mclient.vouch(getAuthor(), getView(), stage.getNodeName());
			}

			disableActions();
		}    
		private static final long serialVersionUID = -7068969470683956727L;
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * new
	 */
	private static final long serialVersionUID = -6716887847852990391L;

	private static final String aModifyExisting = "ModifyExisting";



	/*----------------------------------------------------------------------------------------*/
	/*   I N T E R N A L S                                                                    */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Provides a set of studio-wide helpers for project, sequence and shot naming.
	 */ 
	private StudioDefinitions pStudioDefs;

	/**
	 * Provides project-wide names of nodes and node directories.
	 */ 
	private ProjectNamer pProjectNamer;

	private String pProjectName;

	private MayaContext pMayaContext;

	private LinkedList<BaseStage> pEmptyFiles; 
	private LinkedList<BaseStage> pPreBuildStages; 
	protected LinkedList<FinalizableStage> pFinalizeStages;
	StageInformation pStageInfo;
}
