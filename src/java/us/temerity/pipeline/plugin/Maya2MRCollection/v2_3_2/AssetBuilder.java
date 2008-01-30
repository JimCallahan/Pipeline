// $Id: AssetBuilder.java,v 1.1 2008/01/30 09:28:46 jim Exp $

package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultProjectNames.GlobalsType;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/

public
class AssetBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  AssetBuilder
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
      new DefaultAssetNames(mclient, qclient),
      new DefaultProjectNames(mclient, qclient),
      info);
  }
  
  public
  AssetBuilder
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
    super("Asset",
          "The Temerity Asset Builder that works with the basic Temerity Names class.",
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
      
    // Globabl parameters
    {
      ArrayList<String> projects = pBuilderQueries.getProjectList();
      UtilityParam param = 
        new OptionalEnumUtilityParam
        (aProjectName,
         "The name of the project to build the asset in.", 
         projects.get(0), 
         projects); 
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

    //Model Parameters
    {
      String each[] = {"Import", "Export"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each));
      UtilityParam param = 
        new EnumUtilityParam
        (aModelDelivery, 
         "How is model data being passed from the edit to the verify stage.  In the Import" +
         "method, the entire model scene is imported.  In the Export setup a top level group is" +
         "selected and only geometry under that group is exported in to the verify scene.", 
         "Import",
         choices); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aModelTTForApproval, 
         "Should there be a Turntable for model verification.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyModelMEL, 
         "Is there a verification MEL to check the model before it is submitted", 
         true); 
      addParam(param);
    }
    
    //Rig Parameters
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aReRigSetup,
         "Will this asset use a basic autorig setup?", 
         false); 
      addParam(param);
    }    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aHasBlendShapes,
         "Does the node have blendshapes that need to be attached to the rig?", 
         false); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aRigAnimForApproval, 
         "Should there be an animation verification render for rig approval.", 
         true); 
      addParam(param);
    }
    {
      String each[] = {"None", "FBX", "Curves", "dkAnim"};
      ArrayList<String> choices = new ArrayList<String>(Arrays.asList(each));
      UtilityParam param = 
        new EnumUtilityParam
        (aAnimationFormat,
         "What format is the animation for the rig verification being presented in? " +
         "If this is set to FBX, then a FBX node and a curves node will be created, " +
         "with the curve animation being brought into the verification scene.  If " +
         "curves is selected, a single curves node will be create.  dkAnim support " +
         "is not complete and selecting that option will cause an exception to be thrown.", 
         "Curves",
         choices); 
      addParam(param);
    }
    
    //Material Parameters
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildTextureNode, 
         "Build a texture node", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aMaterialTTForApproval, 
         "Should there be a Turntable for shader verification and approval.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aSeparateAnimTextures,
         "Build a separate node for textures just for the animators.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyShaderMEL, 
         "Is there a verification MEL to check the shaders before they are submitted", 
         true); 
      addParam(param);
    }
    
    if (!assetNames.isGenerated())
      addSubBuilder(assetNames);
    if (!projectNames.isGenerated())
      addSubBuilder(projectNames);
    configNamer(assetNames, projectNames);
    
    setDefaultEditors();
    
    pAssetNames = (BuildsAssetNames) assetNames;
    pProjectNames = (BuildsProjectNames) projectNames;
    
    addSetupPasses();
    addConstructPasses();

    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic pStageInformation about the asset is collected " +
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
      
      layout.addEntry(2, aDoAnnotations);
      layout.addEntry(2, aBuildThumbnails);
      
      LayoutGroup mayaGroup = 
        new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);
      
      mayaGroup.addEntry(aMayaContext);

      //layout.addEntry(2, aBuildLowRez);
      layout.addSubGroup(2, mayaGroup);
      
      {
        LayoutGroup modelGroup = 
          new LayoutGroup("ModelSettings", "Settings related to the model part of the asset process.", true);
        modelGroup.addEntry(aModelDelivery);
        modelGroup.addEntry(aModelTTForApproval);
        modelGroup.addEntry(aVerifyModelMEL);
        layout.addSubGroup(2, modelGroup);
      }
      {
        LayoutGroup rigGroup = 
          new LayoutGroup("RigSettings", "Settings related to the rig part of the asset process.", true);
        rigGroup.addEntry(aReRigSetup);  
        rigGroup.addEntry(aHasBlendShapes);
        rigGroup.addEntry(aRigAnimForApproval);
        rigGroup.addEntry(aAnimationFormat);
        layout.addSubGroup(2, rigGroup);
      }
      {
        LayoutGroup matGroup = 
          new LayoutGroup("MaterialSettings", "Settings related to the material/shading part of the asset process.", true);
        matGroup.addEntry(aMaterialTTForApproval);
        matGroup.addEntry(aBuildTextureNode);
        matGroup.addEntry(aSeparateAnimTextures);
        matGroup.addEntry(aVerifyShaderMEL);
        layout.addSubGroup(2, matGroup);
      }

      LayoutGroup skGroup =
        new LayoutGroup("SelectionKeys", "List of default selection keys", true);
      skGroup.addEntry(aSelectionKeys);
      layout.addSubGroup(1, skGroup);
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      setLayout(finalLayout);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R   M A P P I N G                                                */
  /*----------------------------------------------------------------------------------------*/
  
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
  
  protected void
  addSetupPasses()
    throws PipelineException
  {
    addSetupPass(new InformationPass());
  }
  
  protected void
  addConstructPasses()
    throws PipelineException
  {
    ConstructPass build = new BuildPass();
    addConstructPass(build);
    ConstructPass end = new FinalizePass();
    addConstructPass(end);
    addPassDependency(build, end);
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
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected LinkedList<String>
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  @Override
  protected boolean 
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
  
  /* Model */
  protected boolean pModelTT;
  protected boolean pImportModel;
  
  /* Rig */
  protected boolean pReRigSetup;
  protected boolean pHasBlendShapes;
  protected boolean pMakeFBX;
  protected boolean pMakeCurves;
  protected boolean pMakeDkAnim;
  protected boolean pRigTT;
  
  /* Material */
  protected boolean pBuildTextureNode;
  protected boolean pSeparateAnimTextures;
  protected boolean pShadeTT;
  
  // builder conditions
  protected boolean pCheckInWhenDone;
  protected boolean pBuildThumbnails;
  
  // Mel Scripts
  protected String pFinalizeMEL;
  protected String pLRFinalizeMEL;
  protected String pPlaceHolderMEL;
  protected String pMRInitMEL;
  protected String pVerifyModelMEL;
  protected String pVerifyShaderMEL;

  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
    new ArrayList<EmptyMayaAsciiStage>();
  
  protected ArrayList<EmptyFileStage> pEmptyFileStages = 
    new ArrayList<EmptyFileStage>();
  
  protected TreeSet<String> pRequiredNodes;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  public final static String aBuildThumbnails = "BuildThumbnails";
  
  //Model Parameters
  //public final static String aSeparateModelPieces = "SeparateModelPieces";
  public final static String aModelTTForApproval = "ModelTTForApproval";
  public final static String aVerifyModelMEL = "VerifyModelMEL";
  public final static String aModelDelivery   = "ModelDelivery";
  
  //Rig Parameters
  public final static String aHasBlendShapes = "HasBlendShapes";
  public final static String aReRigSetup = "ReRigSetup";
  public final static String aRigAnimForApproval = "RigAnimForApproval";
  public final static String aAnimationFormat = "AnimationFormat";
  
  //Material Parameters
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aMaterialTTForApproval = "MaterialTTForApproval";
  public final static String aSeparateAnimTextures = "SeparateAnimTextures";
  public final static String aVerifyShaderMEL = "VerifyShaderMEL";
  
  public final static String aProjectName = "ProjectName";
  
  private static final long serialVersionUID = -1692414484571590368L;

  
  
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
            "Information pass for the AssetBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderQueries.setContext(pContext);
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pReRigSetup = getBooleanParamValue(new ParamMapping(aReRigSetup));
      pModelTT = getBooleanParamValue(new ParamMapping(aModelTTForApproval));
      pRigTT = getBooleanParamValue(new ParamMapping(aRigAnimForApproval));
      pShadeTT = getBooleanParamValue(new ParamMapping(aMaterialTTForApproval));
      pBuildThumbnails = getBooleanParamValue(new ParamMapping(aBuildThumbnails));
      pHasBlendShapes = getBooleanParamValue(new ParamMapping(aHasBlendShapes));
      pImportModel = 
	getStringParamValue(new ParamMapping(aModelDelivery)).equals("Import") ? true : false;
      
      { //String each[] = {"None", "FBX", "Curves", "dkAnim"};
	pMakeFBX = false;
	pMakeCurves = false;
	pMakeDkAnim = false;
	
	String animFormat = getStringParamValue(new ParamMapping(aAnimationFormat));
	if (animFormat.equals("FBX")) {
	  pMakeFBX = true;
	  pMakeCurves = true;
	}
	else if (animFormat.equals("Curves"))
	  pMakeCurves = true;
	else if (animFormat.equals("dkAnim"))
	  pMakeDkAnim = true;
      }
      
      if (pMakeDkAnim)
	throw new PipelineException
	  ("The dkAnim support in Pipeline is not finished.  " +
	   "This option will be activated once it is");
      
      pAssetName = pAssetNames.getAssetName();
      pAssetType = pAssetNames.getAssetType();
      
      pRequiredNodes = new TreeSet<String>();

      pFinalizeMEL = pProjectNames.getFinalizeScriptName(null, pAssetType);
      addNonNullValue(pFinalizeMEL, pRequiredNodes);
      
      pLRFinalizeMEL = pProjectNames.getLowRezFinalizeScriptName(null, pAssetType);
      addNonNullValue(pLRFinalizeMEL, pRequiredNodes);

      pMRInitMEL = pProjectNames.getMRayInitScriptName();
      addNonNullValue(pMRInitMEL, pRequiredNodes);

      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();
      addNonNullValue(pPlaceHolderMEL, pRequiredNodes);
      
      addNonNullValue(pProjectNames.getPlaceholderSkelScriptName(), pRequiredNodes);
      
      pVerifyModelMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyModelMEL)))
        pVerifyModelMEL = pProjectNames.getModelVerificationScriptName();
      addNonNullValue(pVerifyModelMEL, pRequiredNodes);

      pVerifyShaderMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyShaderMEL)))
        pVerifyShaderMEL = pProjectNames.getShaderVerificationScriptName();
      addNonNullValue(pVerifyShaderMEL, pRequiredNodes);
      
      if (pModelTT) {
	addNonNullValue(pProjectNames.getAssetModelTTSetup(pAssetName, pAssetType), pRequiredNodes);
	addNonNullValue(pProjectNames.getAssetModelTTGlobals(), pRequiredNodes);
      }
      
      if (pReRigSetup)
	addNonNullValue(pProjectNames.getFinalRigScriptName(), pRequiredNodes);
      
      if (pRigTT) {
	addNonNullValue(pProjectNames.getAssetRigAnimSetup(pAssetName, pAssetType), pRequiredNodes);
	addNonNullValue(pProjectNames.getAssetRigAnimGlobals(), pRequiredNodes);
      }
      
      if (pShadeTT) {
	addNonNullValue(pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType), pRequiredNodes);
        addNonNullValue(pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR), pRequiredNodes);
      }
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);

      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      pStageInfo.setDefaultSelectionKeys(keys);
      pStageInfo.setUseDefaultSelectionKeys(true);
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      pStageInfo.setDoAnnotations(annot);
      
      pTaskName = pProjectNames.getTaskName(pAssetName, pAssetType);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    
    private static final long serialVersionUID = 826916688187120841L;
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
            "The AssetBuilder Pass which actually constructs the node networks.");
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
      doModel();
      doRig();
      doMaterials();
    }

    protected void 
    doModel()
      throws PipelineException
    {
      String taskType = pProjectNames.getModelingTaskName();
      
      String editModel = pAssetNames.getModelEditNodeName();
      String verifyModel = pAssetNames.getModelVerifyNodeName();
      {
	AssetBuilderModelStage stage = 
	  new AssetBuilderModelStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext, 
	   editModel,
	   pPlaceHolderMEL);
	isEditNode(stage, taskType);
	stage.build();
	pModelStages.add(stage);
      }
      if (pImportModel) {
	TreeMap<String, String> edit = new TreeMap<String, String>();
	edit.put("mod", editModel);
	ModelPiecesVerifyStage stage =
	  new ModelPiecesVerifyStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   pMayaContext,
	   verifyModel, 
	   edit,
	   pVerifyModelMEL);
	if (pModelTT)
	  isPrepareNode(stage, taskType);
	else
	  isFocusNode(stage, taskType);
	stage.build();
      }
      else {
	AssetModelExportStage stage = 
	  new AssetModelExportStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   verifyModel,
	   editModel,
	   "GEO",
	   pVerifyModelMEL);
	if (pModelTT)
	  isPrepareNode(stage, taskType);
	else
	  isFocusNode(stage, taskType);
	stage.build();
      }
      String modelTT = pAssetNames.getModelTTNodeName();
      String modelTTImg = pAssetNames.getModelTTImagesNodeName();
      String thumb = null;
      if(pModelTT) {
        {
          String modelTTSetup = 
            pProjectNames.getAssetModelTTSetup(pAssetName, pAssetType);
          AdvAssetBuilderTTStage stage =
            new AdvAssetBuilderTTStage
            (pStageInfo,
             pContext,
             pClient,
             pMayaContext,
             modelTT,
             verifyModel,
             modelTTSetup);
          isPrepareNode(stage, taskType);
          stage.build();
          addToDisableList(modelTT);
        }
        {
          String globals = pProjectNames.getAssetModelTTGlobals();
          AdvAssetBuilderTTImgStage stage =
            new AdvAssetBuilderTTImgStage
            (pStageInfo, 
             pContext, 
             pClient, 
             modelTTImg, 
             modelTT, 
             globals,
             Renderer.Software);
          isFocusNode(stage, taskType);
          stage.build();
        }
        if (pBuildThumbnails) {
          thumb = pAssetNames.getModelThumbNodeName();
          {
            ThumbnailStage stage = 
              new ThumbnailStage(pStageInfo, pContext, pClient, thumb, "png", modelTTImg, 160);
            isThumbnailNode(stage, taskType);
            stage.build();
          }
        }
      }
      String modelSubmit = pAssetNames.getModelSubmitNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        if (pBuildThumbnails)
          sources.add(thumb);
        else if (pModelTT)
          sources.add(modelTTImg);
        else
          sources.add(verifyModel);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(modelSubmit);
        addToCheckInList(modelSubmit);
      }
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String modelApprove = pAssetNames.getModelApproveNodeName();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, modelFinal, "ma", verifyModel, StageFunction.aMayaScene.toString());
        isProductNode(stage, taskType);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(modelFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, modelApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(modelApprove);
        addToCheckInList(modelApprove);
      }
    }
    
    protected void
    doRig()
      throws PipelineException
    {
      LockBundle bundle = new LockBundle();
      String taskType = pProjectNames.getRiggingTaskName();
      
      String modelFinal = pAssetNames.getModelFinalNodeName();
      bundle.addNodeToLock(modelFinal);
      String blendShapes = null; 
      if (pHasBlendShapes) {
        blendShapes = pAssetNames.getBlendShapeModelNodeName();
        {
          EmptyMayaAsciiStage stage = 
            new EmptyMayaAsciiStage
            (pStageInfo,
             pContext,
             pClient,
             pMayaContext,
             blendShapes);
          isEditNode(stage, taskType);
          stage.build();
          pEmptyMayaScenes.add(stage);
        }
      }
      String skeleton = pAssetNames.getSkeletonNodeName();
      String skelMel = pProjectNames.getPlaceholderSkelScriptName();
      if (skeleton != null) {
	if (skelMel == null) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage(pStageInfo, pContext, pClient, pMayaContext, skeleton);
	  isEditNode(stage, taskType);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}
	else {
	  AssetBuilderModelStage stage =
	    new AssetBuilderModelStage(pStageInfo, pContext, pClient, pMayaContext, skeleton, skelMel);
	  isEditNode(stage, taskType);
	  stage.build();
	  pModelStages.add(stage);
	}
      }
      
      String texNode = null;
      if (pBuildTextureNode && pSeparateAnimTextures) {
	texNode = pAssetNames.getAnimTextureNodeName();
	{
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(pStageInfo, pContext, pClient, pMayaContext, texNode, true);
	  isEditNode(stage, taskType);
	  stage.build();
	}
      }
      
      String rigEdit = pAssetNames.getRigEditNodeName();
      {
        NewAssetBuilderRigStage stage = 
          new NewAssetBuilderRigStage
          (pStageInfo,
           pContext, 
           pClient,
           pMayaContext,
           rigEdit,
           modelFinal, null, blendShapes,
           skeleton, null, null,
           texNode);
        isEditNode(stage, taskType);
        stage.build();
      }
      
      String rigMatExp = null; 
      
      String rigSource = rigEdit;
      
      if (pReRigSetup) {
	
	rigMatExp = pAssetNames.getRigMatExportNodeName();
	{
	  NewAssetBuilderMaterialExportStage stage = 
	    new NewAssetBuilderMaterialExportStage
	    (pStageInfo, 
	     pContext,
	     pClient,
	     rigMatExp, 
	     rigEdit);
	  isPrepareNode(stage, taskType);
	  stage.build();
	}
	
	String reRigNode = pAssetNames.getReRigNodeName();
	String finalRigScript = pProjectNames.getFinalRigScriptName();
	{
	  AdvAssetBuilderReRigStage stage = 
	    new AdvAssetBuilderReRigStage
	    (pStageInfo, 
	      pContext, 
	      pClient, 
	      pMayaContext, 
	      reRigNode, 
	      modelFinal, 
	      rigEdit, 
	      blendShapes, 
	      skeleton, 
	      finalRigScript,
	      pReRigSetup);
	  isPrepareNode(stage, taskType);
	  stage.build();
	}
	rigSource = reRigNode;
      }
      
      String rigFinal = pAssetNames.getRigFinalNodeName();
      {
	if (rigSource.equals(rigEdit))
	  rigEdit = null;
	if (!pReRigSetup && !pImportModel) {
	  AssetModelExportStage stage = 
	    new AssetModelExportStage
	    (pStageInfo,
	     pContext,
	     pClient,
	     rigFinal,
	     rigSource,
	     "SELECT",
	     pLRFinalizeMEL);
	  if (pRigTT)
	    isPrepareNode(stage, taskType);
	  else
	    isFocusNode(stage, taskType);
	  stage.build();
	}
	else {
	  NewAssetBuilderFinalStage stage = 
	    new NewAssetBuilderFinalStage
	    (pStageInfo,
	     pContext, 
	     pClient,
	     pMayaContext,
	     rigFinal, 
	     rigSource, rigEdit, rigMatExp,
	     pLRFinalizeMEL);
	  if (pRigTT)
	    isPrepareNode(stage, taskType);
	  else
	    isFocusNode(stage, taskType);
	  stage.build();
	}
      }
      
      //Submit Fun Time
      String animFBX = pAssetNames.getRigAnimFBXNodeName();
      String animCurves = pAssetNames.getRigAnimCurvesNodeName();
      String rigAnim = pAssetNames.getRigAnimNodeName();
      String rigImages = pAssetNames.getRigAnimImagesNodeName();
      String thumb = pAssetNames.getRigThumbNodeName();
      if (pRigTT) {
	if (pMakeFBX) {
	  EmptyFBXStage stage = new EmptyFBXStage(pStageInfo, pContext, pClient, animFBX);
	  stage.build();
	}
	if (pMakeCurves ) {
	  StandardStage stage = null;
	  if (pMakeFBX) {
	    stage = 
	      new AdvAssetBuilderCurvesStage
	      (pStageInfo, 
	       pContext, 
	       pClient, 
	       pMayaContext, 
	       animCurves, 
	       skeleton, 
	       animFBX);
	  }
	  else {
	    stage = 
	      new EmptyMayaAsciiStage(pStageInfo, pContext, pClient, pMayaContext, animCurves);
	    pEmptyMayaScenes.add((EmptyMayaAsciiStage) stage);
	  }
	  stage.build();
	}
	if (pMakeDkAnim ) {
	  EmptyFileStage stage = 
	    new EmptyFileStage(pStageInfo, pContext, pClient, animCurves, "dkAnim");
	  stage.build();
	  pEmptyFileStages.add(stage);
	}
	{
	  String setup = 
	    pProjectNames.getAssetRigAnimSetup(pAssetName, pAssetType);
	  StandardStage stage = null;
	  if (pMakeCurves) {
	    stage = 
	      new AdvAssetBuilderAnimStage
	      (pStageInfo,
	       pContext, 
	       pClient,
	       pMayaContext,
	       rigAnim,
	       animCurves,
	       rigFinal,
	       pAssetNames.getNameSpace(),
	       setup);
	  }
	  else if (pMakeDkAnim) {
	    // do stuff here
	  }
	  else {
	    stage = new AdvAssetBuilderTTStage
	      (pStageInfo, 
	       pContext, 
	       pClient, 
	       pMayaContext, 
	       rigAnim, 
	       rigFinal, 
	       setup);
	  }
	  isPrepareNode(stage, taskType);
	  stage.build();
	  addToDisableList(rigAnim);
	}
	{
	  String globals = 
	    pProjectNames.getAssetRigAnimGlobals();
	  AdvAssetBuilderTTImgStage stage =
	    new AdvAssetBuilderTTImgStage
	    (pStageInfo, 
	     pContext, 
	     pClient, 
	     rigImages, 
	     rigAnim, 
	     globals,
	     Renderer.Software);
	  isFocusNode(stage, taskType);
	  stage.build();
	}
	if (pBuildThumbnails) {
	  ThumbnailStage stage = 
	    new ThumbnailStage(pStageInfo, pContext, pClient, thumb, "png", rigImages, 160);
	  isThumbnailNode(stage, taskType);
	  stage.build();
        }
      }
      String rigSubmit = pAssetNames.getRigSubmitNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        if (pBuildThumbnails)
          sources.add(thumb);
        else if (pRigTT)
          sources.add(rigImages);
        else
          sources.add(rigFinal);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(rigSubmit);
        addToCheckInList(rigSubmit);
        bundle.addNodeToCheckin(rigSubmit);
      }
      String assetFinal = pAssetNames.getAnimFinalNodeName();
      String rigApprove = pAssetNames.getRigApproveNodeName();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, assetFinal, "ma", rigFinal, StageFunction.aMayaScene.toString());
        isProductNode(stage, taskType);
        stage.build();
      }
      String texFinalNode = null;
      if (pBuildTextureNode && pSeparateAnimTextures) {
	texFinalNode = pAssetNames.getAnimTextureFinalNodeName();
        {
          TreeSet<String> sources = new TreeSet<String>();
          sources.add(texNode);
          TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, texFinalNode, sources);
          isProductNode(stage, taskType);
          stage.build();
        }
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(assetFinal);
        addNonNullValue(texFinalNode, sources);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(rigApprove);
        addToCheckInList(rigApprove);
        bundle.addNodeToCheckin(rigApprove);
      }
      addLockBundle(bundle);
    }
    
    protected void
    doMaterials()
      throws PipelineException
    {
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String taskType = pProjectNames.getShadingTaskName();
      
      LockBundle bundle = new LockBundle();
      
      String texNode = null;
      if (pBuildTextureNode) {
	texNode = pAssetNames.getTextureNodeName();
	{
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(pStageInfo, pContext, pClient, pMayaContext, texNode, true);
	  isEditNode(stage, taskType);
	  stage.build();
	}
      }
      
      String matName = pAssetNames.getMaterialNodeName();
      {
        AdvAssetMaterialStage stage =
          new AdvAssetMaterialStage
          (pStageInfo,
           pContext, 
           pClient,
           pMayaContext,
           matName,
           modelFinal,
           texNode);
        isEditNode(stage, taskType);
        stage.build();
        addToDisableList(matName);
      }

      String matExportName = pAssetNames.getMaterialExportNodeName();
      {
        AssetBuilderShaderExportStage stage = 
          new AssetBuilderShaderExportStage
          (pStageInfo, 
           pContext,
           pClient,
           matExportName, 
           matName,
           pVerifyShaderMEL,
           "");
        isPrepareNode(stage, taskType);
        stage.build();
      }

      String rigSource = pAssetNames.getAnimFinalNodeName();
      
      bundle.addNodeToLock(rigSource);
      
      String matVerify = pAssetNames.getMaterialVerifyNodeName();
      {
        NewAssetBuilderFinalStage stage = 
          new NewAssetBuilderFinalStage
          (pStageInfo,
           pContext, 
           pClient,
           pMayaContext,
           matVerify, 
           rigSource, matName, matExportName,
           pFinalizeMEL);
        if (pShadeTT)
          isPrepareNode(stage, taskType);
        else
          isFocusNode(stage, taskType);
        stage.build();
      }
      String matTT = pAssetNames.getMaterialTTNodeName();
      String matTTImg = pAssetNames.getMaterialRenderNodeName();
      String thumb = null;
      if(pShadeTT) {
        {
          String matTTSetup = 
            pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType);
          AdvAssetBuilderTTStage stage =
            new AdvAssetBuilderTTStage
            (pStageInfo,
             pContext,
             pClient,
             pMayaContext,
             matTT,
             matVerify,
             matTTSetup);
          isPrepareNode(stage, taskType);
          stage.build();
          addToDisableList(matTT);
        }
        {
          String globals = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR);
          AdvAssetBuilderTTImgStage stage =
            new AdvAssetBuilderTTImgStage
            (pStageInfo, 
             pContext, 
             pClient, 
             matTTImg, 
             matTT, 
             globals,
             Renderer.MentalRay);
          isFocusNode(stage, taskType);
          stage.build();
        }
        if (pBuildThumbnails) {
          thumb = pAssetNames.getMaterialThumbNodeName();
          ThumbnailStage stage = 
            new ThumbnailStage(pStageInfo, pContext, pClient, thumb, "png", matTTImg, 160);
          isThumbnailNode(stage, taskType);
          stage.build();
        }
      }
      String matSubmit = pAssetNames.getMaterialSubmitNodeName();
      {
        TreeSet<String> sources = new TreeSet<String>();
        if (pBuildThumbnails)
          sources.add(thumb);
        else if (pShadeTT)
          sources.add(matTTImg);
        else
          sources.add(matVerify);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, matSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(matSubmit);
        addToCheckInList(matSubmit);
        bundle.addNodeToCheckin(matSubmit);
      }

      String finalTex = null;
      if (pBuildTextureNode) {
	finalTex = pAssetNames.getTextureFinalNodeName();
	EmptyFileStage stage = new EmptyFileStage(pStageInfo, pContext, pClient, finalTex);
	isProductNode(stage, taskType);
	stage.build();
	pEmptyFileStages.add(stage);
      }
      String matFinal = pAssetNames.getRenderFinalNodeName();
      String matApprove = pAssetNames.getMaterialApproveNodeName();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, matFinal, "ma", matVerify, StageFunction.aMayaScene.toString());
        isProductNode(stage, taskType);
        stage.build();
      }
      {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(matFinal);
	if (pBuildTextureNode)
	  sources.add(finalTex);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, matApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(matApprove);
        addToCheckInList(matApprove);
        bundle.addNodeToCheckin(matApprove);
      }
      addLockBundle(bundle);
    }
    private static final long serialVersionUID = 8287285172355006675L;
  }
  
  
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
    private static final long serialVersionUID = -2030732485658168909L;
  }
}
