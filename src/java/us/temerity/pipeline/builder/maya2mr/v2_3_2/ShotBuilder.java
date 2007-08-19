package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.ShotBuilderAnimStage;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.ShotMayaCurvesExportStage;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

public 
class ShotBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, 
         qclient, 
         new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         new DefaultProjectNames(mclient, qclient),
         info);
  }
  
  public 
  ShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderQueries,
    BaseNames projectNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ShotBuilder", 
      	  new VersionID("2.3.2"), 
      	  "Temerity",
	  "The basic Temerity Shot Builder that works with the basic Temerity Names class.",
	  mclient,
	  qclient,
	  builderInformation);
    pBuilderQueries = builderQueries;

    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
    
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
        new BooleanUtilityParam
        (aMovieFormat,
         "Does the layout of your project contain movie names as well as sequence and shot names.", 
         true); 
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
        new BooleanUtilityParam
        (aBuildThumbnails, 
         "Are Thumbnail nodes needed.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildTestImages, 
         "Are renders/playblasts needed at each stage for scene verification.", 
         true); 
      addParam(param);
    }
    
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
    
    if (!projectNames.isGenerated())
      addSubBuilder(projectNames);
    
    configNamer(projectNames);
    pProjectNames = (BuildsProjectNames) projectNames;
    
    addSetupPass(new FirstInfoPass());
    addSetupPass(new AssetInfoPass());
    ConstructPass build = new BuildPass();
    addConstuctPass(build);
    ConstructPass end = new FinalizePass();
    addConstuctPass(end);
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
	layout.addEntry(1, aActionOnExistance);
	layout.addEntry(1, aReleaseOnError);
	layout.addEntry(1, null);
	layout.addEntry(1, aProjectName);
	
	 LayoutGroup skGroup =
	   new LayoutGroup("SelectionKeys", "List of default selection keys", true);
	 skGroup.addEntry(aSelectionKeys);
	 layout.addSubGroup(1, skGroup);

	layout.addEntry(2, aDoAnnotations);
	layout.addEntry(2, aBuildThumbnails);
	layout.addEntry(2, aBuildTestImages);
	layout.addSeparator(2);
	layout.addEntry(2, aNewSequence);
	layout.addEntry(2, aMovieFormat);
	layout.addSeparator(2);
	
	layout.addEntry(2, aStartFrame);
	layout.addEntry(2, aEndFrame);
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
	 layout.addSubGroup(1, charGroup);
	 
	 LayoutGroup propGroup =
	   new LayoutGroup("Props", "List of props in the shot", true);
	 propGroup.addEntry(aProps);
	 layout.addSubGroup(1, propGroup);
	 
	 LayoutGroup setGroup =
	   new LayoutGroup("Sets", "List of sets in the shot", true);
	 setGroup.addEntry(aSets);
	 layout.addSubGroup(1, setGroup);
	 
	 finalLayout.addPass(layout2.getName(), layout2);
      }
    }

  }
  
  @Override
  protected TreeSet<String> getNodesToCheckIn()
  {
    return null;
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   D E F A U L T   E D I T O R S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Override this to change the default editors.
   */
  protected void
  setDefaultEditors()
  {
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("Shake"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), null);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  // Context
  protected MayaContext pMayaContext;
  
  protected String pProject;

  // Names
  protected BuildsShotNames pShotNames;
  protected BuildsProjectNames pProjectNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderQueries;
  
  protected FrameRange pFrameRange;
  
  protected Boolean pBuildThumbnails;
  protected Boolean pBuildTestImages;
  
//  TreeMap<String, String> pChars;
//  TreeMap<String, String> pSets;
//  TreeMap<String, String> pProps;
//  TreeMap<String, String> pNamespaces;
  
  ArrayList<BuildsAssetNames> pAssets;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  public final static String aProjectName = "ProjectName";
  public final static String aNewSequence = "NewSequence";
  public final static String aMovieFormat = "MovieFormat";
  public final static String aBuildThumbnails = "BuildThumbnails";

  public final static String aStartFrame = "StartFrame";
  public final static String aEndFrame = "EndFrame";
  
  public final static String aAnimFormat = "AnimFormat";
  public final static String aExternalAnimOnly = "ExternalAnimOnly";
  
  public final static String aBuildLayout  = "BuildLayout";
  public final static String aBuildTestImages = "BuildTestImages";
  public final static String aBuildAnimImages = "BuildAnimImages";
  
  public final static String aChars = "Chars";
  public final static String aProps = "Props";
  public final static String aSets = "Sets";

  private static final long serialVersionUID = -4118587763338751379L;

  
  
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
            "The First Information pass for the ShotBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Asset Info Pass.");
      validateBuiltInParams();
      pBuilderQueries.setContext(pContext);
      
      pMovieFormat = getBooleanParamValue(new ParamMapping(aMovieFormat));
      pNewSequence = getBooleanParamValue(new ParamMapping(aNewSequence));
      
      pProject = getStringParamValue(new ParamMapping(aProjectName));
      
      pBuildThumbnails = getBooleanParamValue(new ParamMapping(aBuildThumbnails));
      pBuildTestImages = getBooleanParamValue(new ParamMapping(aBuildTestImages));
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the init phase in the First Info Pass.");
      DefaultShotNames names = 
	new DefaultShotNames(pProject, pMovieFormat, !pNewSequence, pClient, pQueue, pBuilderQueries);
      addSubBuilder(names);
      pShotNames = names;
      
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
    
    private static final long serialVersionUID = 3924566232585200969L;
    
    protected boolean pMovieFormat;
    protected boolean pNewSequence;
  }
  
  protected 
  class AssetInfoPass
    extends SetupPass
  {
    public 
    AssetInfoPass()
    {
      super("Asset Info Pass", 
            "The Asset Information pass for the ShotBuilder");
      
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Asset Info Pass.");
      
      pAssets = new ArrayList<BuildsAssetNames>();
      
      TreeSet<String> chars = (TreeSet<String>) getParamValue(aChars);
      for (String each : chars) {
	DefaultAssetNames names = new DefaultAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "character");
	names.generateNames();
	pAssets.add(names);
      }
      
      TreeSet<String> props = (TreeSet<String>) getParamValue(aProps);
      for (String each : props) {
	DefaultAssetNames names = new DefaultAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "prop");
	names.generateNames();
	pAssets.add(names);
      }
      
      TreeSet<String> sets = (TreeSet<String>) getParamValue(aProps);
      for (String each : sets) {
	DefaultAssetNames names = new DefaultAssetNames(pClient, pQueue);
	names.setParamValue(DefaultAssetNames.aProjectName, pProject);
	names.setParamValue(DefaultAssetNames.aAssetName, each);
	names.setParamValue(DefaultAssetNames.aAssetType, "set");
	names.generateNames();
	pAssets.add(names);
      }
    }
    private static final long serialVersionUID = 8371820302516003252L;
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
            "The ShotBuilder Pass which actually constructs the node networks.");
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> toReturn = new TreeSet<String>();
      for (BuildsAssetNames asset : pAssets) {
	toReturn.add(asset.getFinalNodeName());
      }
      return toReturn;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      doLayout();
      doAnim();
      doLighting();
    }

    private void 
    doLayout()
      throws PipelineException
    {
      String taskType = pProjectNames.getLayoutTaskName();
      
      String layoutScene = pShotNames.getLayoutEditNodeName();
      {
	ShotBuilderAnimStage stage = 
	  new ShotBuilderAnimStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   layoutScene, 
	   pAssets,
	   null,
	   pFrameRange,
	   false);
	isEditNode(stage, taskType);
        stage.build();
        addToDisableList(layoutScene);
      }
      
      pLayoutAnims = new TreeSet<String>();
      for (BuildsAssetNames asset : pAssets) {
	pShotNames.getLayoutExportPrepareNodeName(asset.getAssetName());
      }
      
      TreeSet<String> anims = new TreeSet<String>();
      for (BuildsAssetNames asset : pAssets) {
	String anim = pShotNames.getLayoutExportPrepareNodeName(asset.getAssetName());
	String exportSet = asset.getNameSpace() + ":SELECT";
	{
	  ShotMayaCurvesExportStage stage = 
	    new ShotMayaCurvesExportStage
	    (pStageInfo,
	     pContext,
	     pClient,
	     anim,
	     layoutScene,
	     exportSet,
	     false);
	  isPrepareNode(stage, taskType);
	  stage.build();
	  anims.add(exportSet);
	}
      }
    }
    
    private void 
    doAnim()
    {
    }

    private void 
    doLighting()
    {
    }

    private TreeSet<String> pLayoutAnims;
  }
  
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("FinalizePass", 
	    "The AdvAssetBuilder pass that cleans everything up.");
    }
  }
}



