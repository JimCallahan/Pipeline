package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultProjectNames.GlobalsType;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   A S S E T   B U I L D E R                                                */
/*------------------------------------------------------------------------------------------*/

public 
class SimpleAssetBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  SimpleAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient,
         qclient,
         new DefaultBuilderAnswers(mclient, qclient, 
           UtilContext.getDefaultUtilContext(mclient), info.getLoggerName()), 
         new DefaultAssetNames(mclient, qclient, info),
         new DefaultProjectNames(mclient, qclient, info),
         info);
  }
  
  @SuppressWarnings("deprecation")
  public
  SimpleAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderQueries,
    BaseNames assetNames,
    BaseNames projectNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("SimpleAsset",
          "The Simple Temerity Asset Builder that works with the basic Temerity Names class.",
          mclient,
          qclient,
          builderInformation);
    pBuilderQueries = builderQueries;
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The asset naming class that was passed in does not implement " +
         "the BuildsAssetNames interface");
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
    
    // Globals parameters
    {
      ArrayList<String> projects = pBuilderQueries.getProjectList();
      if (projects != null && projects.size() > 0) {
        UtilityParam param = 
          new OptionalEnumUtilityParam
          (aProjectName,
           "The name of the project to build the asset in.", 
           projects.get(0), 
           projects); 
        addParam(param);
      }
      else {
        UtilityParam param = 
          new StringUtilityParam
          (aProjectName,
           "The name of the project to build the asset in.",
           null);
        addParam(param);
      }
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
        (aBuildThumbnail, 
         "Is a Thumbnail node needed.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildTurntable, 
         "Are Turntable nodes needed.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildTextureNode, 
         "Build a texture node", 
         true); 
      addParam(param);
    }
    
    {
      String each[] = {"Import", "Export"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each));
      UtilityParam param = 
        new EnumUtilityParam
        (aModelDelivery, 
         "How is model data being passed from the edit to the verify stage.  In the Import" +
         "method, the entire model scene is imported.  In the Export setup a top level " + 
         "group is selected and only geometry under that group is exported in to the " +
         "verify scene.", 
         "Import",
         choices); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyMEL, 
         "Is there a verification MEL to check the asset before it is submitted", 
         true); 
      addParam(param);
    }
    
    if (!assetNames.isGenerated())
      addSubBuilder(assetNames);
    if (!projectNames.isGenerated())
      addSubBuilder(projectNames);
    configNamer(assetNames, projectNames);
    
    pAssetNames = (BuildsAssetNames) assetNames;
    pProjectNames = (BuildsProjectNames) projectNames;
    
    setDefaultEditors();
    
    addSetupPasses();
    addConstructPasses();
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic information about the asset is collected " +
           "from the user.", 
           "BuilderSettings", 
           true);
      layout.addColumn("Asset Information", true);
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
      layout.addEntry(2, aBuildThumbnail);
      layout.addEntry(2, aBuildTurntable);
      layout.addEntry(2, aBuildTextureNode);
      layout.addSeparator(2);
      layout.addEntry(2, aModelDelivery);
      layout.addEntry(2, aVerifyMEL);
      
      LayoutGroup mayaGroup = 
        new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);
      
      mayaGroup.addEntry(aMayaContext);
      
      layout.addSubGroup(2, mayaGroup);
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P A S S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Override to change setup passes
   */
  protected void
  addSetupPasses()
    throws PipelineException
  {
    addSetupPass(new InformationPass());
  }
  
  /**
   * Override to change construct passes
   */
  protected void
  addConstructPasses()
    throws PipelineException
  {
    ConstructPass build = new BuildPass();
    addConstructPass(build);
    ConstructPass end = new FinalizePass();
    addConstructPass(end);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R   M A P P I N G                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Override this method to set different parameter mappings for passed in Namers.
   */
  protected void 
  configNamer 
  (
    BaseNames assetNames,
    BaseNames projectNames
  )
    throws PipelineException
  {
    if (!assetNames.isGenerated())
      addMappedParam(assetNames.getName(), DefaultAssetNames.aProjectName, aProjectName);
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
    setDefaultEditor(StageFunction.aMayaScene, new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.aNone, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aTextFile, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aScriptFile, new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("Shake"));
    setDefaultEditor(StageFunction.aSourceImage, new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.aMotionBuilderScene, null);
  }

  @Override
  public LinkedList<String> 
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  @Override
  public boolean 
  performCheckIn()
  {
    return pCheckInWhenDone;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  // Context
  protected MayaContext pMayaContext;

  // Names
  protected BuildsAssetNames pAssetNames;
  protected BuildsProjectNames pProjectNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderQueries;
  
  protected String pAssetName;
  protected String pAssetType;
  
  // conditions on what is being built
  
  
  // builder conditions
  protected boolean pCheckInWhenDone;
  protected boolean pBuildThumbnail;
  protected boolean pBuildTurntable;
  protected boolean pBuildTextureNode;
  protected boolean pImportModel;
  
  // Mel Scripts
  protected String pPlaceHolderMEL;
  protected String pVerifyMEL;
  
  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected TreeSet<String> pRequiredNodes;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  public final static String aBuildThumbnail   = "BuildThumbnail";
  public final static String aBuildTurntable   = "BuildTurntable";
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aModelDelivery    = "ModelDelivery";
  public final static String aVerifyMEL        = "VerifyMEL";
  
  public final static String aProjectName = "ProjectName";
  
  private static final long serialVersionUID = 3854094814560543747L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected 
  class InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", 
            "Information pass for the SimpleAssetBuilder");
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderQueries.setContext(pContext);
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildThumbnail = getBooleanParamValue(new ParamMapping(aBuildThumbnail));
      pBuildTurntable = getBooleanParamValue(new ParamMapping(aBuildTurntable));
      pImportModel = 
	getStringParamValue(new ParamMapping(aModelDelivery)).equals("Import") ? true : false;
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      pAssetName = pAssetNames.getAssetName();
      pAssetType = pAssetNames.getAssetType();
      
      pRequiredNodes = new TreeSet<String>();
      
      if (pBuildTurntable) {
	addNonNullValue(pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType), pRequiredNodes);
      }
      
      pVerifyMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyMEL)))
	pVerifyMEL = pProjectNames.getAssetVerificationScriptName();
      addNonNullValue(pVerifyMEL, pRequiredNodes);
      
      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();
      addNonNullValue(pPlaceHolderMEL, pRequiredNodes);
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);

      StageInformation stageInfo = getStageInformation();
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      stageInfo.setDefaultSelectionKeys(keys);
      stageInfo.setUseDefaultSelectionKeys(true);
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      stageInfo.setDoAnnotations(annot);
      
      pTaskName = pProjectNames.getTaskName(pAssetName, pAssetType);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = 4439191096809456824L;
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
            "The SimpleAssetBuilder Pass which constructs the node networks.");
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      return pRequiredNodes;
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      String taskType = pProjectNames.getModelingTaskName();
      
      if (pBuildTextureNode) {
	
      }
      
      String editAsset = pAssetNames.getAssetEditNodeName();
      String verifyAsset = pAssetNames.getAssetVerifyNodeName();
      {
	AssetBuilderModelStage stage = 
	  new AssetBuilderModelStage
	  (stageInfo,
	   pContext,
	   pClient,
	   pMayaContext, 
	   editAsset,
	   pPlaceHolderMEL);
	addEditAnnotation(stage, taskType);
	stage.build();
	pModelStages.add(stage);
      }
      if (pImportModel) {
	TreeMap<String, String> edit = new TreeMap<String, String>();
	edit.put("mod", editAsset);
	ModelPiecesVerifyStage stage =
	  new ModelPiecesVerifyStage
	  (stageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   verifyAsset, 
	   edit,
	   pVerifyMEL);
	if (pBuildTurntable)
	  addPrepareAnnotation(stage, taskType);
	else
	  addFocusAnnotation(stage, taskType);
	stage.build();
      }
      else {
	AssetModelExportStage stage = 
	  new AssetModelExportStage
	  (stageInfo,
	   pContext,
	   pClient,
	   verifyAsset,
	   editAsset,
	   "SELECT",
	   pVerifyMEL);
	if (pBuildTurntable)
	  addPrepareAnnotation(stage, taskType);
	else
	  addFocusAnnotation(stage, taskType);
	stage.build();
      }
      String assetTT = pAssetNames.getAssetTTNodeName();
      String assetTTImg = pAssetNames.getAssetTTImagesNodeName();
      String thumb = null;
      if(pBuildTurntable) {
        {
          String assetTTSetup = 
            pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType);
          AdvAssetBuilderTTStage stage =
            new AdvAssetBuilderTTStage
            (stageInfo,
             pContext,
             pClient,
             pMayaContext,
             assetTT,
             verifyAsset,
             assetTTSetup);
          addPrepareAnnotation(stage, taskType);
          stage.build();
          addToDisableList(assetTT);
        }
        {
          String globals = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR);
          AdvAssetBuilderTTImgStage stage =
            new AdvAssetBuilderTTImgStage
            (stageInfo, 
             pContext, 
             pClient, 
             assetTTImg, 
             assetTT, 
             globals,
             Renderer.MentalRay);
          addFocusAnnotation(stage, taskType);
          stage.build();
        }
        if (pBuildThumbnail) {
          thumb = pAssetNames.getAssetThumbNodeName();
          ThumbnailStage stage = 
            new ThumbnailStage(stageInfo, pContext, pClient, thumb, "png", assetTTImg, 160);
          addThumbnailAnnotation(stage, taskType);
          stage.build();
        }
      }
      String assetSubmit = pAssetNames.getAssetSubmitNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        if (pBuildThumbnail)
          sources.add(thumb);
        else if (pBuildTurntable)
          sources.add(assetTTImg);
        else
          sources.add(verifyAsset);
        TargetStage stage = new TargetStage(stageInfo, pContext, pClient, assetSubmit, sources);
        addSubmitAnnotation(stage, taskType);
        stage.build();
        addToQueueList(assetSubmit);
        addToCheckInList(assetSubmit);
      }
      String assetFinal = pAssetNames.getAssetFinalNodeName();
      String assetApprove = pAssetNames.getAssetApproveNodeName();
      {
        ProductStage stage = 
          new ProductStage(stageInfo, pContext, pClient, assetFinal, "ma", verifyAsset, StageFunction.aMayaScene);
        addProductAnnotation(stage, taskType);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(assetFinal);
        TargetStage stage = new TargetStage(stageInfo, pContext, pClient, assetApprove, sources);
        addApproveAnnotation(stage, taskType);
        stage.build();
        addToQueueList(assetApprove);
        addToCheckInList(assetApprove);
      }
    }
    private static final long serialVersionUID = -8881566912838162426L;
  }
  
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("FinalizePass", 
	    "The SimpleAssetBuilder pass that cleans everything up.");
    }
    
    @Override
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> toReturn = new LinkedList<String>(getDisableList());
      toReturn.addAll(getDisableList());
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
      disableActions();
    }
    
    private static final long serialVersionUID = 2815380698824865655L;
  }

}
