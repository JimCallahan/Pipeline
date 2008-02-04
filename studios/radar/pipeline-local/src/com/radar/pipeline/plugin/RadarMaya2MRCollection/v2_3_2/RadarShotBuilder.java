package com.radar.pipeline.plugin.RadarMaya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;


public 
class RadarShotBuilder
  extends TaskBuilder
{
  
  public
  RadarShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, 
         qclient, 
         new RadarBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         new RadarProjectNames(mclient, qclient),
         info);
  }
  
  public 
  RadarShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderQueries,
    RadarProjectNames projectNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("RadarShotBuilder", 
	  "The basic Radar Shot Builder that works with the basic Radar Names class.",
	  mclient,
	  qclient,
	  builderInformation);
    pBuilderQueries = builderQueries;
    
    // Global parameters

    {
      ArrayList<String> projects = pBuilderQueries.getProjectList();
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
    addSelectionKeyParam();
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
    
    setDefaultEditors();
    
    if (!projectNames.isGenerated())
      addSubBuilder(projectNames);

    configNamer(projectNames);
    pProjectNames = projectNames;
    
    addSetupPass(new FirstInfoPass());
    addSetupPass(new AssetInfoPass());
    ConstructPass build = new BuildPass();
    addConstructPass(build);
    ConstructPass end = new FinalizePass();
    addConstructPass(end);
    addPassDependency(build, end);

    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic pStageInformation about the shot is collected " +
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
	
	 LayoutGroup skGroup =
	   new LayoutGroup("SelectionKeys", "List of default selection keys", true);
	 skGroup.addEntry(aSelectionKeys);
	 layout.addSubGroup(1, skGroup);

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
	   "The pass where all the basic pStageInformation about what assets are in the shot" +
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
    
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R   M A P P I N G                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected void 
  configNamer 
  (
    BaseNames projectNames
  )
    throws PipelineException
  {
    if (!projectNames.isGenerated())
      addMappedParam(projectNames.getName(), DefaultProjectNames.aProjectName, aProjectName);
  }
  
  @Override
  protected LinkedList<String> 
  getNodesToCheckIn()
  {
    return null;
  }

  /**
   * Overriden to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.aNone, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aTextFile, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Jedit", "Radar"));
    setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("ImfDisp"));
    setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.aMotionBuilderScene, new PluginContext("Jedit", "Radar"));
  }

  
  
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
  
  private static final long serialVersionUID = 5239818357053986422L;

  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  // Context
  protected MayaContext pMayaContext;
  
  protected String pProject;
  
  protected RadarAssetNames pCameraNames;

  // Names
  protected RadarShotNames pShotNames;
  protected BuildsProjectNames pProjectNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderQueries;
  
  protected FrameRange pFrameRange;
  
  private TreeMap<String, AssetBundle> pAssets;
  
  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
    new ArrayList<EmptyMayaAsciiStage>();
  
  protected ArrayList<EmptyFileStage> pEmptyFileStages = 
    new ArrayList<EmptyFileStage>();
  
  protected String pImgSuffix = "tga";
  
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
            "The First Information pass for the RadarShotBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderQueries.setContext(pContext);
      
      pNewSequence = getBooleanParamValue(new ParamMapping(aNewSequence));
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      pStageInfo.setDefaultSelectionKeys(keys);
      pStageInfo.setUseDefaultSelectionKeys(true);
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      pStageInfo.setDoAnnotations(annot);
      
      pProject = getStringParamValue(new ParamMapping(aProjectName));
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException
    {
      RadarShotNames names = 
	new RadarShotNames(pProject, !pNewSequence, pClient, pQueue, pBuilderQueries);
      addSubBuilder(names);
      pShotNames = names;
      
      int start = getIntegerParamValue(new ParamMapping(aStartFrame), new Range<Integer>(0, null));
      int end = getIntegerParamValue(new ParamMapping(aEndFrame), new Range<Integer>(start + 1, null));
      pFrameRange = new FrameRange(start, end, 1);
      
      {
        ArrayList<String> chars = pBuilderQueries.getListOfAssets(pProject, "character");
        UtilityParam param =
          new ListUtilityParam
          (aChars, 
           "Which characters are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(chars),
           null,
           null);
        replaceParam(param);
      }
      
      {
        ArrayList<String> props = pBuilderQueries.getListOfAssets(pProject, "prop");
        UtilityParam param =
          new ListUtilityParam
          (aProps, 
           "Which props are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(props),
           null,
           null);
        replaceParam(param);
      }
      
      {
        ArrayList<String> props = pBuilderQueries.getListOfAssets(pProject, "prop");
        UtilityParam param =
          new ListUtilityParam
          (aProps, 
           "Which props are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(props),
           null,
           null);
        replaceParam(param);
      }
      
      {
        ArrayList<String> sets = pBuilderQueries.getListOfAssets(pProject, "set");
        UtilityParam param =
          new ListUtilityParam
          (aSets, 
           "Which sets are included in the shot.",
           new TreeSet<String>(),
           new TreeSet<String>(sets),
           null,
           null);
        replaceParam(param);
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
            "The Asset Information pass for the RadarShotBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pAssets = new TreeMap<String, AssetBundle>();
      
      {
	pCameraNames = new RadarAssetNames(pClient, pQueue);
	pCameraNames.setParamValue(DefaultAssetNames.aProjectName, pProject);
	pCameraNames.setParamValue(DefaultAssetNames.aAssetName, "renderCam");
	pCameraNames.setParamValue(DefaultAssetNames.aAssetType, "cam");
	pCameraNames.generateNames();
      }
      
      TreeSet<String> chars = (TreeSet<String>) getParamValue(aChars);
      for (String each : chars) {
	RadarAssetNames names = new RadarAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "character");
	names.generateNames();
	pAssets.put(names.getNameSpace(), new AssetBundle(names));
      }
      
      TreeSet<String> props = (TreeSet<String>) getParamValue(aProps);
      for (String each : props) {
	RadarAssetNames names = new RadarAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "prop");
	names.generateNames();
	pAssets.put(names.getNameSpace(), new AssetBundle(names));
      }
      
      TreeSet<String> sets = (TreeSet<String>) getParamValue(aSets);
      for (String each : sets) {
	RadarAssetNames names = new RadarAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "set");
	names.generateNames();
	pAssets.put(names.getNameSpace(), new AssetBundle(names));
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
            "The RadarShotBuilder Pass which constructs the node networks.");
    }
    
    @Override
    public void buildPhase()
      throws PipelineException
    {
      doRenderCam();
      doLayout();
      doAnimation();
      doLighting();
    }
    
    protected void
    doRenderCam()
      throws PipelineException
    {
      String camMel = pProjectNames.getPlaceholderCameraScriptName();
      String renderCam = pCameraNames.getAssetFinalNodeName();
      AssetBuilderModelStage stage = 
	new AssetBuilderModelStage
	(pStageInfo,
	 pContext,
	 pClient,
	 pMayaContext,
	 renderCam,
	 camMel);
      	stage.build();
      	
      	pModelStages.add(stage);
      	addToCheckInList(renderCam);
    }
    
    protected void
    doLayout()
      throws PipelineException
    {
      @SuppressWarnings("unused")
      String taskType = pProjectNames.getLayoutTaskName();
      
      String layoutCameraAnimation = pShotNames.getLayoutExportPrepareNodeName("cam"); 
      {
	EmptyMayaAsciiStage stage =
	  new EmptyMayaAsciiStage
	  (pStageInfo, pContext, pClient, pMayaContext, layoutCameraAnimation);
	stage.build();
	pEmptyMayaScenes.add(stage);
      }
    }
    
    protected void
    doAnimation()
      throws PipelineException
    {
      String taskType = pProjectNames.getAnimTaskName();
      
      String layoutCameraAnimation = pShotNames.getLayoutExportPrepareNodeName("cam");
      String cameraName = pCameraNames.getAssetFinalNodeName();
      
      TreeMap<String, String> assets = new TreeMap<String, String>();
      {
	for (String namespace : pAssets.keySet()) {
	  AssetBundle bundle = pAssets.get(namespace);
	  RadarAssetType type = bundle.pType;
	  String nodename;
	  switch(type) {
	  case Asset:
	    nodename = bundle.pNames.getAnimFinalNodeName();
	    break;
	  case SimpleAsset:
	    nodename = bundle.pNames.getAssetFinalNodeName();
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
	RadarAnimEditStage stage = 
	  new RadarAnimEditStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   animEdit,
	   assets,
	   cameraName,
	   layoutCameraAnimation,
	   pFrameRange);
	isEditNode(stage, taskType);
	stage.build();
	addToDisableList(animEdit);
      }
      
      assets.put(pCameraNames.getNameSpace(), cameraName);
      TreeMap<String, AssetBundle> assetsWithCam = new TreeMap<String, AssetBundle>(pAssets);
      assetsWithCam.put(pCameraNames.getNameSpace(), new AssetBundle(pCameraNames));
      
      TreeMap<String, String> animProductFiles = new TreeMap<String, String>();
      TreeMap<String, String> animPrepareFiles = new TreeMap<String, String>();
      
      for (String nameSpace : assetsWithCam.keySet()) {
	RadarAssetNames names = assetsWithCam.get(nameSpace).pNames;
	String assetName = names.getAssetName();
	String animPrepare = pShotNames.getAnimExportPrepareNodeName(assetName);
	String animProduct = pShotNames.getAnimExportProductNodeName(assetName);
	{
	  ShotMayaCurvesExportStage stage = 
	    new ShotMayaCurvesExportStage
	    (pStageInfo,
	     pContext,
	     pClient,
	     animPrepare,
	     animEdit,
	     nameSpace + ":SELECT",
	     true);
	  isPrepareNode(stage, taskType);
	  stage.build();
	}
	{
	  ProductStage stage = 
	    new ProductStage
	    (pStageInfo, 
	     pContext, 
	     pClient, 
	     animProduct, 
	     "ma", 
	     animPrepare, 
	     StageFunction.aMayaScene);
	  isProductNode(stage, taskType);
	  stage.build();
	}
	animPrepareFiles.put(nameSpace, animPrepare);
	animProductFiles.put(nameSpace, animProduct);
      }
      String animBuild = pShotNames.getAnimBuildNodeName();
      {
	ShotAnimBuildStage stage = 
	  new ShotAnimBuildStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   animBuild,
	   assets,
	   animPrepareFiles,
	   null,
	   pFrameRange);
	isPrepareNode(stage, taskType);
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
	    (pStageInfo, 
	     pContext, 
	     pClient, 
	     animRender,
	     pFrameRange,
	     pImgSuffix,
	     animBuild, 
	     globals,
	     Renderer.Software);
	  isFocusNode(stage, taskType);
	  stage.build();
      }
      {
	ThumbnailStage stage = 
	  new ThumbnailStage(pStageInfo, pContext, pClient, animThumb, "png", animRender, 160);
	isThumbnailNode(stage, taskType);
	stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(animThumb);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, animSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(animSubmit);
        addToCheckInList(animSubmit);
      }
      
      //Time to do Product stuff (or more product stuff, I should say)
      String preLight = pShotNames.getPreLightNodeName();
      String preMEL = pShotNames.getPreLightMELNodeName();
      {
	EmptyFileStage stage = new EmptyFileStage(pStageInfo, pContext, pClient, preMEL, "mel");
	stage.build();
      }
      TreeMap<String, String> finalAssets = new TreeMap<String, String>();
      {
	for (String namespace : pAssets.keySet()) {
	  AssetBundle bundle = pAssets.get(namespace);
	  RadarAssetType type = bundle.pType;
	  String nodename;
	  switch(type) {
	  case Asset:
	    nodename = bundle.pNames.getRenderFinalNodeName();
	    break;
	  case SimpleAsset:
	    nodename = bundle.pNames.getAssetFinalNodeName();
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
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   preLight,
	   finalAssets,
	   animProductFiles,
	   preMEL,
	   pFrameRange);
	stage.build();
      }
      String animApprove = pShotNames.getAnimApproveNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(preLight);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, animApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(animApprove);
        addToCheckInList(animApprove);
      }
    }
    
    protected void
    doLighting()
      throws PipelineException
    {
      String taskType = pProjectNames.getLightingTaskName();
      
      String preLight = pShotNames.getPreLightNodeName();
      String lighting = pShotNames.getLightEditNodeName();
      {
	ShotBuilderLightStage stage = 
	  new ShotBuilderLightStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   lighting,
	   preLight,
	   "pre",
	   null,
	   pFrameRange);
	isEditNode(stage, taskType);
	stage.build();
	addToDisableList(lighting);
      }
      String lgtRender = pShotNames.getLightImagesNodeName();
      String lgtThumb = pShotNames.getLightThumbNodeName();
      String lightSubmit = pShotNames.getLightSubmitNodeName();
      {
	String globals = 
	    pProjectNames.getLgtGlobals();
	ShotImgStage stage =
	    new ShotImgStage
	    (pStageInfo, 
	     pContext, 
	     pClient, 
	     lgtRender,
	     pFrameRange,
	     pImgSuffix,
	     lighting, 
	     globals,
	     Renderer.MentalRay);
	  isFocusNode(stage, taskType);
	  stage.build();
      }
      {
	ThumbnailStage stage = 
	  new ThumbnailStage(pStageInfo, pContext, pClient, lgtThumb, "png", lgtRender, 160);
	isThumbnailNode(stage, taskType);
	stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(lgtThumb);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, lightSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(lightSubmit);
        addToCheckInList(lightSubmit);
      }
      
      //Approve Time
      String lightFinal = pShotNames.getFinalLightNodeName();
      {
	ProductStage stage = 
	  new ProductStage
	  (pStageInfo, 
	   pContext, 
	   pClient, 
	   lightFinal, 
	   "ma", 
	   lighting, 
	   StageFunction.aMayaScene);
	isProductNode(stage, taskType);
	stage.build();
      }
      String lgtApprove = pShotNames.getLightApproveNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(lightFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, lgtApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(lgtApprove);
        addToCheckInList(lgtApprove);
      }
    }
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
    public TreeSet<String> 
    preBuildPhase()
    {
      TreeSet<String> toReturn = new TreeSet<String>(getDisableList());
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
      RadarAssetNames names  
    ) 
      throws PipelineException
    {
      pNames = names;
      
      boolean good = false;
      
      String simpleFinal = pNames.getAssetFinalNodeName();
      if (nodeExists(simpleFinal)) {
	pType = RadarAssetType.SimpleAsset;
	good = true;
      }
      
      String assetFinal = pNames.getRenderFinalNodeName();
      boolean nodeExists = nodeExists(assetFinal);
      
      if ( nodeExists && !good) {
	pType = RadarAssetType.Asset;
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
    
    public RadarAssetNames
    names()
    {
      return pNames;
    }
    
    public RadarAssetType
    type()
    {
      return pType;
    }
    
    private RadarAssetNames pNames;
    private RadarAssetType pType;
  }
  
  public static enum
  RadarAssetType
  {
    SimpleAsset, Asset
  }
}