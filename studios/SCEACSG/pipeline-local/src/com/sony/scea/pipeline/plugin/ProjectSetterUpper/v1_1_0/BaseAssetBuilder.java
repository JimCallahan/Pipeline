//$Id: BaseAssetBuilder.java,v 1.1 2008/06/11 12:48:23 jim Exp $

package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.builder.v2_4_1.EntityType;
import us.temerity.pipeline.stages.FinalizableStage;


public 
class BaseAssetBuilder
extends TaskBuilder
{

	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	/**
	 * Constructor.
	 * 
	 * @param name
	 *   The name of the Builder.
	 * @param desc
	 *   A brief description of what the Builder is supposed to do.
	 * @param mclient
	 *   The instance of the Master Manager that the Builder is going to use.
	 * @param qclient
	 *   The instance of the Queue Manager that the Builder is going to use
	 * @param builderInformation
	 *   The instance of the global information class used to share information between all the
	 *   Builders that are invoked.
	 */
	protected 
	BaseAssetBuilder
	(
			String name,
			String desc,
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation builderInformation,
			StudioDefinitions studioDefinitions
	)
	throws PipelineException
	{
		super(name, desc, mclient, qclient, builderInformation, EntityType.Asset);

		pStudioDefinitions = studioDefinitions;
		pMclient = mclient;

		/* Params  */
		{
			ArrayList<String> projects = studioDefinitions.getProjectList();
			if (projects.isEmpty())
				throw new PipelineException("Please create a project before running the asset builder.");
			UtilityParam param = 
				new OptionalEnumUtilityParam
				(ParamNames.aProjectName,
						"The name of the project to build the asset in.", 
						projects.get(0), 
						projects); 
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
		addCheckinWhenDoneParam();

		setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
		setDefaultEditor(StageFunction.aNone, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aTextFile, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("Shake"));
		setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));


		pProjectNames = new ProjectNamer(mclient, qclient);
		addSubBuilder(pProjectNames);
		addMappedParam(pProjectNames.getName(), ParamNames.aProjectName, ParamNames.aProjectName);

		pAssetNames = new AssetNamer(mclient, qclient);
		addSubBuilder(pAssetNames);
		addMappedParam(pAssetNames.getName(), ParamNames.aProjectName, ParamNames.aProjectName);

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

			return regenerate;
		}

		@Override
		public void 
		buildPhase() 
		throws PipelineException
		{
			for(FinalizableStage stage : pFinalizeStages)
			{
				stage.finalizeStage();
			}
			
			disableActions();
			clearDisableList();

			//vouch shit...
			for(FinalizableStage stage : pVouchStages)
			{
				pMclient.vouch(getAuthor(), getView(), stage.getNodeName());
				pLog.log
				(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
						"Vouch: " + stage.getNodeName());
			}

		}    
		private static final long serialVersionUID = -7068969470683956727L;
	}


	protected PassLayoutGroup
	getBuilderLayout()
	{
		AdvancedLayoutGroup layout = 
			new AdvancedLayoutGroup
			("Builder Information", 
					"The pass where all the basic information about the asset is collected " +
					"from the user.", 
					"BuilderSettings", 
					true);
		layout.addEntry(1, aUtilContext);
		layout.addEntry(1, null);
		layout.addEntry(1, aCheckinWhenDone);
		layout.addEntry(1, aActionOnExistence);
		layout.addEntry(1, aReleaseOnError);
		layout.addEntry(1, null);
		layout.addEntry(1, ParamNames.aProjectName);


		LayoutGroup mayaGroup = 
			new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);

		mayaGroup.addEntry(ParamNames.aMayaContext);

		layout.addSubGroup(1, mayaGroup);
		PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
		return finalLayout;
	}

	/*----------------------------------------------------------------------------------------*/
	/*  I N T E R N A L S                                                                     */
	/*----------------------------------------------------------------------------------------*/

	protected ProjectNamer pProjectNames;
	protected AssetNamer pAssetNames;
	protected StudioDefinitions pStudioDefinitions;

	protected MayaContext pMayaContext;

	protected TreeSet<String> pRequiredNodes;

	protected AssetType pAssetType;

	protected String pProjectName;

	protected String pTaskName;

	protected LinkedList<FinalizableStage> pFinalizeStages;
	protected LinkedList<FinalizableStage> pVouchStages;

	private MasterMgrClient pMclient;

	private static final long serialVersionUID = 211203648424077939L;
}
