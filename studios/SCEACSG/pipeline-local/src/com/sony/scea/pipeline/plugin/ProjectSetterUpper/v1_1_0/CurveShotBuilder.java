package com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0;

import java.text.DecimalFormat;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;

import com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.stages.*;
import com.sony.scea.pipeline.plugin.ProjectSetterUpper.v1_1_0.IntegerListUtilityParam;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;
import us.temerity.pipeline.builder.v2_4_1.NodePurpose;
import us.temerity.pipeline.builder.v2_4_1.TaskType;
import us.temerity.pipeline.builder.v2_4_1.TaskBuilder;
import us.temerity.pipeline.builder.v2_4_1.EntityType;

public 
class CurveShotBuilder
extends TaskBuilder
{

	public
	CurveShotBuilder
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation info
	)
	throws PipelineException
	{
		this(mclient, qclient, info, 
		     new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)));
	}

	public
	CurveShotBuilder
	(
			MasterMgrClient mclient,
			QueueMgrClient qclient,
			BuilderInformation builderInformation,
			StudioDefinitions studioDefinitions
	) 
	throws PipelineException
	{
		super("CurveShot",
		      "The Shot Builder that works with the basic Temerity Project Names class.",
		      mclient,
		      qclient,
		      builderInformation,
		      EntityType.Shot);

		pStudioDefinitions = studioDefinitions;
		pMclient = mclient;

		// Global parameters

		{
			ArrayList<String> projects = studioDefinitions.getProjectList();
			UtilityParam param = 
				new OptionalEnumUtilityParam
				(aProjectName,
						"The name of the project to build the shot in.", 
						projects.get(0), 
						projects); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new BooleanUtilityParam
				(aNewSequence,
						"Are you building a new sequence or creating a shot in an existing sequence.", 
						true); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new IntegerUtilityParam
				(aStartFrame,
						"The first frame of the shot.", 
						1); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new IntegerUtilityParam
				(aEndFrame,
						"The last frame of the shot.", 
						24); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new MayaContextUtilityParam
				(aMayaContext,
						"The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
						new MayaContext()); 
			addParam(param);
		}

		addCheckinWhenDoneParam();
		//addSelectionKeyParam();
		addDoAnnotationParam();

		{
			UtilityParam param = 
				new PlaceholderUtilityParam
				(aChars, 
				"Which characters are included in the shot."); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new PlaceholderUtilityParam
				(aSets, 
				"Which sets are included in the shot."); 
			addParam(param);
		}

		{
			UtilityParam param = 
				new PlaceholderUtilityParam
				(aProps, 
				"Which props are included in the shot."); 
			addParam(param);
		}

		//if (!projectNames.isGenerated())
		//	addSubBuilder(projectNames);

		//configNamer(projectNames);
		//pProjectNames = projectNames;

		addSetupPass(new FirstInfoPass());
		addSetupPass(new AssetInfoPass());
		ConstructPass build = new BuildPass();
		addConstructPass(build);
		ConstructPass end = new FinalizePass();
		addConstructPass(end);

		{
			AdvancedLayoutGroup layout = 
				new AdvancedLayoutGroup
				("Builder Information", 
						"The pass where all the basic stageInformation about the shot is collected " +
						"from the user.", 
						"BuilderSettings", 
						true);
			{
				layout.addColumn("Shot Information", true);
				layout.addEntry(1, aUtilContext);
				layout.addEntry(1, null);
				layout.addEntry(1, aCheckinWhenDone);
				layout.addEntry(1, aActionOnExistence);
				layout.addEntry(1, aReleaseOnError);
				layout.addEntry(1, null);
				layout.addEntry(1, aProjectName);

				//LayoutGroup skGroup =
				//	new LayoutGroup("SelectionKeys", "List of default selection keys", true);
				//skGroup.addEntry(aSelectionKeys);
				//layout.addSubGroup(1, skGroup);

				layout.addEntry(2, aDoAnnotations);
				layout.addEntry(2, aNewSequence);
				layout.addSeparator(2);
				layout.addEntry(2, aStartFrame);
				layout.addEntry(2, aEndFrame);

				LayoutGroup mayaGroup = 
					new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);

				mayaGroup.addEntry(aMayaContext);

				layout.addSubGroup(2, mayaGroup);

			}

			PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);

			{
				AdvancedLayoutGroup layout2 = 
					new AdvancedLayoutGroup
					("Asset Information", 
							"The pass where all the basic stageInformation about what assets are in the shot" +
							"is collected from the user.", 
							"Assets", 
							true);

				LayoutGroup charGroup =
					new LayoutGroup("Characters", "List of characters in the shot", true);
				charGroup.addEntry(aChars);
				layout2.addSubGroup(1, charGroup);

				LayoutGroup propGroup =
					new LayoutGroup("Props", "List of props in the shot", true);
				propGroup.addEntry(aProps);
				layout2.addSubGroup(1, propGroup);

				LayoutGroup setGroup =
					new LayoutGroup("Sets", "List of sets in the shot", true);
				setGroup.addEntry(aSets);
				layout2.addSubGroup(1, setGroup);

				finalLayout.addPass(layout2.getName(), layout2);
				setLayout(finalLayout);
			}
		}

		pProjectNames = new ProjectNamer(mclient, qclient);
		//pAssetNames = new AssetNamer(mclient, qclient);
		setDefaultEditors();
	}


	protected void
	setDefaultEditors()
	{
		setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
		setDefaultEditor(StageFunction.aNone, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aTextFile, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Emacs"));
		setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("Shake"));
		setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
		setDefaultEditor(StageFunction.aMotionBuilderScene, new PluginContext("Emacs"));
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S U B - B U I L D E R   M A P P I N G                                                */
	/*----------------------------------------------------------------------------------------*/

//	protected void 
//	configNamer 
//	(
//	BaseNames projectNames
//	)
//	throws PipelineException
//	{
//	if (!projectNames.isGenerated())
//	addMappedParam(projectNames.getName(), DefaultProjectNames.aProjectName, aProjectName);
//	}



	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/

	public final static String aMayaContext = "Maya";

	public final static String aProjectName = "ProjectName";
	public final static String aNewSequence = "NewSequence";

	public final static String aStartFrame = "StartFrame";
	public final static String aEndFrame = "EndFrame";

	public final static String aChars = "Chars";
	public final static String aProps = "Props";
	public final static String aSets = "Sets";

	private static final long serialVersionUID = 4214839314468065088L;



	/*----------------------------------------------------------------------------------------*/
	/*  I N T E R N A L S                                                                     */
	/*----------------------------------------------------------------------------------------*/
	/**
	 * Provides a set of studio-wide helpers for project, sequence and shot naming.
	 */ 
	private StudioDefinitions pStudioDefinitions;

	private MasterMgrClient pMclient;
	/**
	 * Provides project-wide names of nodes and node directories.
	 */ 
	private ProjectNamer pProjectNames;

	// Context
	protected MayaContext pMayaContext;

	protected String pProject;

	//private AssetNamer pAssetNames;

	// Names
	protected ShotNames pShotNames;

	protected AssetNamer pCameraNames;

	// Question Answering
	//protected AnswersBuilderQueries pBuilderQueries;

	protected FrameRange pFrameRange;

	private TreeMap<String, AssetBundle> pAssets;
	private TreeMap<String, Integer> pAssetCounts;

	protected ArrayList<AssetBuilderModelStage> pModelStages = 
		new ArrayList<AssetBuilderModelStage>();

	protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
		new ArrayList<EmptyMayaAsciiStage>();

	protected ArrayList<FinalizableStage> pVouchStages = new ArrayList<FinalizableStage>();

	protected ArrayList<EmptyFileStage> pEmptyFileStages = 
		new ArrayList<EmptyFileStage>();

	protected String pImgSuffix = "iff";

	private Boolean pHasProps;
	private Boolean pHasSets;

	/*----------------------------------------------------------------------------------------*/
	/*   F I R S T   L O O P                                                                  */
	/*----------------------------------------------------------------------------------------*/

	protected 
	class FirstInfoPass
	extends SetupPass
	{
		public 
		FirstInfoPass()
		{
			super("First Info Pass", 
			"The First Information pass for the CurveShotBuilder");
		}

		@SuppressWarnings("unchecked")
		@Override
		public void 
		validatePhase()
		throws PipelineException
		{
			validateBuiltInParams();
			//pBuilderQueries.setContext(pContext);

			pNewSequence = getBooleanParamValue(new ParamMapping(aNewSequence));

			pMayaContext = (MayaContext) getParamValue(aMayaContext);

			StageInformation stageInfo = getStageInformation();

			//TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
			//stageInfo.setDefaultSelectionKeys(keys);
			//stageInfo.setUseDefaultSelectionKeys(true);

			boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
			stageInfo.setDoAnnotations(annot);

			pProject = getStringParamValue(new ParamMapping(aProjectName));
			pProjectNames.setParamValue(ParamNames.aProjectName, pProject);
			pProjectNames.generateNames();

//			need to tell default project names what the selected project is...

			//pProjectNames.updateProjectName(pProject);
		}

		@Override
		public void 
		initPhase() 
		throws PipelineException
		{
			ShotNames names = 
				new ShotNames(pClient, pQueue);
			names.setParamValue(ParamNames.aProjectName, pProject);
			addSubBuilder(names);
			pShotNames = names;

			int start = getIntegerParamValue(new ParamMapping(aStartFrame), new Range<Integer>(0, null));
			int end = getIntegerParamValue(new ParamMapping(aEndFrame), new Range<Integer>(start + 1, null));
			pFrameRange = new FrameRange(start, end, 1);

			{
				ArrayList<String> chars = pStudioDefinitions.getAssetList(pProject, AssetType.character.toString());
//				ArrayList<String> chars2 = new ArrayList<String>();

//				for (String guy: chars)
//				{
//				if (!guy.contains("_"))
//				chars2.add(guy);
//				}
				/*
				UtilityParam param =
					new ListUtilityParam
					(aChars, 
							"Which characters are included in the shot.",
							new TreeSet<String>(),
							new TreeSet<String>(chars2),
							null,
							null);
				 */
				UtilityParam param =
					new IntegerListUtilityParam
					(aChars, 
							"Which characters are included in the shot.",
							new TreeSet<String>(chars),
							0);
				replaceParam(param);
			}

			{
				ArrayList<String> props = pStudioDefinitions.getAssetList(pProject, AssetType.prop.toString());

				if (props != null)
				{
					pHasProps = true;
					/*
					UtilityParam param =
						new ListUtilityParam
						(aProps, 
								"Which props are included in the shot.",
								new TreeSet<String>(),
								new TreeSet<String>(props),
								null,
								null);
					 */
					UtilityParam param =
						new IntegerListUtilityParam
						(aProps, 
								"Which props are included in the shot.",
								new TreeSet<String>(props),
								0);
					replaceParam(param);
				}
				else
				{
					pHasProps = false;

					UtilityParam param =
						new StringUtilityParam
						(aProps, 
								"Which props are included in the shot.",
						"N/A");	
					replaceParam(param);
				}
			}

			{
				ArrayList<String> sets = pStudioDefinitions.getAssetList(pProject, AssetType.env.toString());

				if (sets != null)
				{
					pHasSets = true;
					/*
					UtilityParam param =
						new ListUtilityParam
						(aSets, 
								"Which sets are included in the shot.",
								new TreeSet<String>(),
								new TreeSet<String>(sets),
								null,
								null);
					 */
					UtilityParam param =
						new IntegerListUtilityParam
						(aSets, 
								"Which sets are included in the shot.",
								new TreeSet<String>(sets),
								0);
					replaceParam(param);
				}
				else
				{
					pHasSets = false;
					UtilityParam param =
						new StringUtilityParam
						(aSets, 
								"Which sets are included in the shot.",
						"N/A");	
					replaceParam(param);
				}
			}
		}
		protected boolean pNewSequence;

		private static final long serialVersionUID = -8034836560600484096L;
	}

	protected 
	class AssetInfoPass
	extends SetupPass
	{
		public 
		AssetInfoPass()
		{
			super("Asset Info Pass", 
			"The Asset Information pass for the CurveShotBuilder");
		}

		@SuppressWarnings("unchecked")
		@Override
		public void 
		validatePhase()
		throws PipelineException
		{
			pAssets = new TreeMap<String, AssetBundle>();
			pAssetCounts = new TreeMap<String, Integer>();

			{
				pCameraNames = new AssetNamer(pClient, pQueue);
				pCameraNames.setParamValue(ParamNames.aProjectName, pProject);
				pCameraNames.setParamValue(ParamNames.aAssetName, "renderCam");
				pCameraNames.setParamValue(ParamNames.aAssetType, "cam");
				pCameraNames.generateNames();
			}

			//TreeSet<String> chars = (TreeSet<String>) getParamValue(aChars);
			IntegerListUtilityParam ilup = (IntegerListUtilityParam)getParam(aChars);
			TreeMap <String, UtilityParam> params = ilup.getParams();

			for (String each : params.keySet()) {
				Integer count = ((IntegerUtilityParam)params.get(each)).getIntegerValue();
				if (count == null)
					count = 0;

				if (count > 0)
				{
					AssetNamer names = new AssetNamer(pClient, pQueue);
					names.setParamValue(ParamNames.aProjectName, pProject);
					names.setParamValue(ParamNames.aAssetName, each);
					names.setParamValue(ParamNames.aAssetType, "char");
					names.generateNames();
					pAssets.put(names.getNameSpace(), new AssetBundle(names)); //namespace is the same as asset's name for now...
					pAssetCounts.put(names.getNameSpace(), count);
				}
			}

			if (pHasProps)
			{
				ilup = (IntegerListUtilityParam)getParam(aProps);
				params = ilup.getParams();

				//TreeSet<String> props = (TreeSet<String>) getParamValue(aProps);
				for (String each : params.keySet()) {

					Integer count = ((IntegerUtilityParam)params.get(each)).getIntegerValue();
					if (count == null)
						count = 0;

					if (count > 0)
					{
						AssetNamer names = new AssetNamer(pClient, pQueue);
						names.setParamValue(ParamNames.aProjectName, pProject);
						names.setParamValue(ParamNames.aAssetName, each);
						names.setParamValue(ParamNames.aAssetType, "prop");
						names.generateNames();
						
						pAssets.put(names.getNameSpace(), new AssetBundle(names));
						pAssetCounts.put(names.getNameSpace(), count);
					}
				}
			}

			if (pHasSets)
			{
				ilup = (IntegerListUtilityParam)getParam(aSets);
				params = ilup.getParams();

				//TreeSet<String> sets = (TreeSet<String>) getParamValue(aSets);
				for (String each : params.keySet()) {
					Integer count = ((IntegerUtilityParam)params.get(each)).getIntegerValue();
					if (count == null)
						count = 0;

					if (count > 0)
					{
						AssetNamer names = new AssetNamer(pClient, pQueue);
						names.setParamValue(ParamNames.aProjectName, pProject);
						names.setParamValue(ParamNames.aAssetName, each);
						names.setParamValue(ParamNames.aAssetType, "env");
						names.generateNames();
						
						pAssets.put(names.getNameSpace(), new AssetBundle(names));
						pAssetCounts.put(names.getNameSpace(), count);
					}
				}
			}

			TreeSet<String> names = new TreeSet<String>();
			for (AssetBundle asset : pAssets.values()) {
				String name = asset.names().getAssetName();
				if (names.contains(name))
					throw new PipelineException
					("Two assets with the name (" + name + ") were specified for the Shot Builder.  " +
					"You cannot have a shot with two identically named assets.");

			}
		}
		private static final long serialVersionUID = 5373677273885431026L;
	}


	/*----------------------------------------------------------------------------------------*/
	/*   S E C O N D   L O O P                                                                */
	/*----------------------------------------------------------------------------------------*/

	protected 
	class BuildPass
	extends ConstructPass
	{
		public 
		BuildPass()
		{
			super("Build Pass", 
			"The CurveShotBuilder Pass which constructs the node networks.");
			pRenderCamExists = false;
		}

		@Override
		public TreeSet<String> 
		nodesDependedOn()
		{
			TreeSet<String> toReturn = new TreeSet<String>();
			addNonNullValue(pProjectNames.getPlaceholderCameraScriptName(), toReturn);

			//String renderCam = pCameraNames.getAssetFinalNodeName();
			String renderCam = pCameraNames.getAssetName();
			try {
				if (nodeExists(renderCam)) {
					pRenderCamExists = true;
					addNonNullValue(renderCam, toReturn);
				}
			}
			catch (PipelineException ex) {
			}
			addNonNullValue(pProjectNames.getAnimGlobals(), toReturn);
			addNonNullValue(pProjectNames.getLgtGlobals(), toReturn);
			return toReturn;
		}

		@Override
		public void buildPhase()
		throws PipelineException
		{
			if (!pRenderCamExists)
				doRenderCam();
			//wct come back here homes
			doLayout();
			doAnimation();
			doLighting();
		}

		protected void
		doRenderCam()
		throws PipelineException
		{
			StageInformation stageInfo = getStageInformation();
			String camMel = pProjectNames.getPlaceholderCameraScriptName();
			//String renderCam = pCameraNames.getAssetFinalNodeName();
			String renderCam = pCameraNames.getAssetProductScene();
			AssetBuilderModelStage stage = 
				new AssetBuilderModelStage
				(stageInfo,
						pContext,
						pClient,
						pMayaContext,
						renderCam,
						camMel);

			//addTaskAnnotation(stage, NodePurpose.Prereq, pProject, pCameraNames.getTaskName(), TaskType.Asset.toTitle());

			if (stage.build())
			{
				pModelStages.add(stage);
				pVouchStages.add(stage);
				addToCheckInList(renderCam);
			}
		}

		protected void
		doLayout()
		throws PipelineException
		{
			StageInformation stageInfo = getStageInformation();
			@SuppressWarnings("unused")
			String taskType = TaskType.Layout.toTitle();

			String layoutCameraAnimation = pShotNames.getLayoutExportPrepareNodeName("cam"); 
			{
				EmptyMayaAsciiStage stage =
					new EmptyMayaAsciiStage
					(stageInfo, pContext, pClient, pMayaContext, layoutCameraAnimation);
				//use animation as the task type for now...
				addTaskAnnotation(stage, NodePurpose.Prepare, pProject, pShotNames.getTaskName(TaskType.Animation.toTitle()), taskType);
				if (stage.build())
				{
					pEmptyMayaScenes.add(stage);
				}
			}
		}


		protected void
		doAnimation()
		throws PipelineException
		{
			StageInformation stageInfo = getStageInformation();
			String taskType = TaskType.Animation.toString();

			String layoutCameraAnimation = pShotNames.getLayoutExportPrepareNodeName("cam");
			//String cameraName = pCameraNames.getAssetFinalNodeName();
			String cameraName = pCameraNames.getAssetProductScene();

			TreeMap<String, String> assets = new TreeMap<String, String>();
			{
				for (String namespace : pAssets.keySet()) {
					AssetBundle bundle = pAssets.get(namespace);
					AssetComplexity type = bundle.pType;
					String nodename;
					switch(type) {
					case Asset:
						//nodename = bundle.pNames.getAnimFinalNodeName();
						nodename = bundle.pNames.getRigProductScene();
						break;
					case SimpleAsset:
						nodename = bundle.pNames.getAssetProductScene();
						break;
					default:
						throw new PipelineException("Somehow there is an asset with an invalid type");
					}
					assets.put(namespace, nodename);
					lockLatest(nodename);
				}
			}
			String animEdit = pShotNames.getAnimEditNodeName();
			{
				AnimEditStage stage = 
					new AnimEditStage
					(stageInfo,
							pContext,
							pClient,
							pMayaContext,
							animEdit,
							assets,
							pAssetCounts,
							cameraName,
							layoutCameraAnimation,
							pFrameRange);
				//addEditAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Edit, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
				addToDisableList(animEdit);
			}

			assets.put(pCameraNames.getNameSpace(), cameraName);
			TreeMap<String, AssetBundle> assetsWithCam = new TreeMap<String, AssetBundle>(pAssets);
			assetsWithCam.put(pCameraNames.getNameSpace(), new AssetBundle(pCameraNames));

			TreeMap<String, String> animProductFiles = new TreeMap<String, String>();
			TreeMap<String, String> animPrepareFiles = new TreeMap<String, String>();

			for (String nameSpace : assetsWithCam.keySet()) {
				AssetNamer names = assetsWithCam.get(nameSpace).pNames;
				String assetName = names.getAssetName();
				String animPrepare = pShotNames.getAnimExportPrepareNodeName(assetName);
				String animProduct = pShotNames.getAnimExportProductNodeName(assetName);

				if (pAssetCounts.get(assetName) == null || pAssetCounts.get(assetName) == 1)
				{
					{
						ShotMayaCurvesExportStage stage = 
							new ShotMayaCurvesExportStage
							(stageInfo,
									pContext,
									pClient,
									animPrepare,
									animEdit,
									nameSpace + ":SELECT",
									true);
						//addPrepareAnnotation(stage, taskType);
						addTaskAnnotation(stage, NodePurpose.Prepare, pProject, pShotNames.getTaskName(taskType), taskType);
						stage.build();
					}
					{
						ProductStage stage = 
							new ProductStage
							(stageInfo, 
									pContext, 
									pClient, 
									animProduct, 
									"ma", 
									animPrepare, 
									StageFunction.aMayaScene.toString());
						//addProductAnnotation(stage, taskType);
						addTaskAnnotation(stage, NodePurpose.Product, pProject, pShotNames.getTaskName(taskType), taskType);
						stage.build();
					}
					animPrepareFiles.put(nameSpace, animPrepare);
					animProductFiles.put(nameSpace, animProduct);
				}
				else
				{					
					for (int i = 0; i < pAssetCounts.get(assetName); i++)
					{
						DecimalFormat df = new DecimalFormat("0000");

						animPrepare = pShotNames.getAnimExportPrepareNodeName(assetName, i);
						animProduct = pShotNames.getAnimExportProductNodeName(assetName, i);

						{
							ShotMayaCurvesExportStage stage = 
								new ShotMayaCurvesExportStage
								(stageInfo,
										pContext,
										pClient,
										animPrepare,
										animEdit,
										nameSpace + df.format(i) + ":SELECT",
										true);
							//addPrepareAnnotation(stage, taskType);
							addTaskAnnotation(stage, NodePurpose.Prepare, pProject, pShotNames.getTaskName(taskType), taskType);
							stage.build();
						}
						{
							ProductStage stage = 
								new ProductStage
								(stageInfo, 
										pContext, 
										pClient, 
										animProduct, 
										"ma", 
										animPrepare, 
										StageFunction.aMayaScene.toString());
							//addProductAnnotation(stage, taskType);
							addTaskAnnotation(stage, NodePurpose.Product, pProject, pShotNames.getTaskName(taskType), taskType);
							stage.build();
						}
						animPrepareFiles.put(nameSpace + df.format(i), animPrepare);
						animProductFiles.put(nameSpace + df.format(i), animProduct);
					}
				}
			}
			String animBuild = pShotNames.getAnimBuildNodeName();
			{
				ShotAnimBuildStage stage = 
					new ShotAnimBuildStage
					(stageInfo,
							pContext,
							pClient,
							pMayaContext,
							animBuild,
							assets,
							animPrepareFiles,
							pAssetCounts,
							null,
							pFrameRange);
				//addPrepareAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Prepare, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			String animRender = pShotNames.getAnimImgNodeName();
			String animThumb = pShotNames.getAnimThumbNodeName();
			String animSubmit = pShotNames.getAnimSubmitNodeName();
			{
				String globals = 
					pProjectNames.getAnimGlobals();
				ShotImgStage stage =
					new ShotImgStage
					(stageInfo, 
							pContext, 
							pClient, 
							animRender,
							pFrameRange,
							pImgSuffix,
							animBuild, 
							globals,
							Renderer.Software);
				//addFocusAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Focus, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			{
				ShakeThumbnailStage stage = 
					new ShakeThumbnailStage(stageInfo, pContext, pClient, animThumb, "png", animRender, 160);
				//addThumbnailAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Thumbnail, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(animThumb);
				TargetStage stage = new TargetStage(stageInfo, pContext, pClient, animSubmit, sources);
				//addSubmitAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Submit, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
				addToQueueList(animSubmit);
				addToCheckInList(animSubmit);
			}

			//Time to do Product stuff (or more product stuff, I should say)
			String preLight = pShotNames.getPreLightNodeName();
			String preMEL = pShotNames.getPreLightMELNodeName();
			{
				EmptyFileStage stage = new EmptyFileStage(stageInfo, pContext, pClient, preMEL, "mel");
				addTaskAnnotation(stage, NodePurpose.Prereq, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			TreeMap<String, String> finalAssets = new TreeMap<String, String>();
			{
				for (String namespace : pAssets.keySet()) {
					AssetBundle bundle = pAssets.get(namespace);
					AssetComplexity type = bundle.pType;
					String nodename;
					switch(type) {
					case Asset:
						//nodename = bundle.pNames.getRenderFinalNodeName();
						nodename = bundle.pNames.getShadeProductScene();
						break;
					case SimpleAsset:
						//nodename = bundle.pNames.getAssetFinalNodeName();
						nodename = bundle.pNames.getAssetProductScene();
						break;
					default:
						throw new PipelineException("Somehow there is an asset with an invalid type");
					}
					finalAssets.put(namespace, nodename);
					lockLatest(nodename);
				}
				finalAssets.put(pCameraNames.getNameSpace(), cameraName);
			}
			/* 
			 * This cannot be a product node because the lighters may need to build it on their
			 * own as newer versions of the high rez models be come availible.  So it is built
			 * as part of the animation approval process (so that new animation is always pushed
			 * out to lighting as soon as possible, but it is not considered a node that belongs
			 * to any one task.
			 */
			{
				ShotAnimBuildStage stage = 
					new ShotAnimBuildStage
					(stageInfo,
							pContext,
							pClient,
							pMayaContext,
							preLight,
							finalAssets,
							animProductFiles,
							pAssetCounts,
							preMEL,
							pFrameRange);
				addTaskAnnotation(stage, NodePurpose.Product, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			String animApprove = pShotNames.getAnimApproveNodeName();
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(preLight);
				TargetStage stage = new TargetStage(stageInfo, pContext, pClient, animApprove, sources);
				//addApproveAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Approve, pProject, pShotNames.getTaskName(taskType), taskType);
				if (stage.build())
				{
					addToQueueList(animApprove);
					addToCheckInList(animApprove);
				}
			}
		}

		protected void
		doLighting()
		throws PipelineException
		{
			StageInformation stageInfo = getStageInformation();
			String taskType = TaskType.Lighting.toTitle();
			String preLight = pShotNames.getPreLightNodeName();
			String lighting = pShotNames.getLightEditNodeName();
			{
				ShotBuilderLightStage stage = 
					new ShotBuilderLightStage
					(stageInfo,
							pContext,
							pClient,
							pMayaContext,
							lighting,
							preLight,
							"pre",
							null,
							pFrameRange);
				//addEditAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Edit, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
				addToDisableList(lighting);
			}
			String lgtRender = pShotNames.getLightImagesNodeName();
			String lgtThumb = pShotNames.getLightThumbNodeName();
			String lightSubmit = pShotNames.getLightSubmitNodeName();
			{
				String globals = 
					pProjectNames.getLgtGlobals();
				MRayShotImgStage stage =
					new MRayShotImgStage
					(stageInfo, 
							pContext, 
							pClient, 
							lgtRender,
							pFrameRange,
							pImgSuffix,
							lighting, 
							globals);
				//addFocusAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Focus, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			{
				ShakeThumbnailStage stage = 
					new ShakeThumbnailStage(stageInfo, pContext, pClient, lgtThumb, "png", lgtRender, 160);
				//addThumbnailAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Thumbnail, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(lgtThumb);
				TargetStage stage = new TargetStage(stageInfo, pContext, pClient, lightSubmit, sources);
				//addSubmitAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Submit, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
				addToQueueList(lightSubmit);
				addToCheckInList(lightSubmit);
			}

			//Approve Time
			String lightFinal = pShotNames.getFinalLightNodeName();
			{
				ProductStage stage = 
					new ProductStage
					(stageInfo, 
							pContext, 
							pClient, 
							lightFinal, 
							"ma", 
							lighting, 
							StageFunction.aMayaScene.toString());
				//addProductAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Product, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
			}
			String lgtApprove = pShotNames.getLightApproveNodeName();
			{
				TreeSet<String> sources = new TreeSet<String>();
				sources.add(lightFinal);
				TargetStage stage = new TargetStage(stageInfo, pContext, pClient, lgtApprove, sources);
				//addApproveAnnotation(stage, taskType);
				addTaskAnnotation(stage, NodePurpose.Approve, pProject, pShotNames.getTaskName(taskType), taskType);
				stage.build();
				addToQueueList(lgtApprove);
				addToCheckInList(lgtApprove);
			}
		}

		private boolean pRenderCamExists;
		private static final long serialVersionUID = 7481221384924916509L;
	}

	protected 
	class FinalizePass
	extends ConstructPass
	{
		public 
		FinalizePass()
		{
			super("Finalize Pass", 
			"The ShotBuilder Pass which cleans everything up.");
		}

		@Override
		public LinkedList<String> 
		preBuildPhase()
		{
			LinkedList<String> toReturn = new LinkedList<String>(getDisableList());
			toReturn.addAll(getDisableList());
			for (EmptyMayaAsciiStage stage : pEmptyMayaScenes) {
				toReturn.add(stage.getNodeName());
			}
			for (EmptyFileStage stage : pEmptyFileStages) {
				toReturn.add(stage.getNodeName());
			}
			for (AssetBuilderModelStage stage : pModelStages) {
				toReturn.add(stage.getNodeName());
			}
			return toReturn;
		}

		@Override
		public void 
		buildPhase() 
		throws PipelineException
		{
			for (AssetBuilderModelStage stage : pModelStages)
				stage.finalizeStage();
			for (EmptyFileStage stage : pEmptyFileStages)
				stage.finalizeStage();
			for (EmptyMayaAsciiStage stage : pEmptyMayaScenes)
				stage.finalizeStage();
			for (FinalizableStage stage : pVouchStages)
			{
				pMclient.vouch(getAuthor(), getView(), stage.getNodeName());
				pLog.log
				(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
						"Vouch: " + stage.getNodeName());
			}
			disableActions();

		}

		private static final long serialVersionUID = 4592076017313180015L;
	}

	/*----------------------------------------------------------------------------------------*/
	/*   S U B   C L A S S E S                                                                */
	/*----------------------------------------------------------------------------------------*/

	protected
	class AssetBundle
	{
		protected 
		AssetBundle
		(
				AssetNamer names  
		) 
		throws PipelineException
		{
			pNames = names;

			boolean good = false;

			//String simpleFinal = pNames.getAssetFinalNodeName();
			String simpleFinal = pNames.getAssetProductScene();
			if (nodeExists(simpleFinal)) {
				pType = AssetComplexity.SimpleAsset;
				good = true;
			}

			//String assetFinal = pNames.getRenderFinalNodeName();
			String assetFinal = pNames.getShadeProductScene();
			boolean nodeExists = nodeExists(assetFinal);

			if ( nodeExists && !good) {
				pType = AssetComplexity.Asset;
				good = true;
			}
			else if (nodeExists && good)
				throw new PipelineException
				("Cannot determine the type of the asset.  " +
						"Both (" + simpleFinal + ") and (" + assetFinal + ") should not exist.");

			if (!good)
				throw new PipelineException
				("Cannot determine the type of the asset.  " +
						"Neither (" + simpleFinal + ") or (" + assetFinal + ") exists.");
		}

		public AssetNamer
		names()
		{
			return pNames;
		}

		public AssetComplexity
		type()
		{
			return pType;
		}

		private AssetNamer pNames;
		private AssetComplexity pType;
	}

	public static enum
	AssetComplexity
	{
		SimpleAsset, Asset
	}

}
