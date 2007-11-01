// $Id: AdvAssetBuilder.java,v 1.18 2007/11/01 19:08:53 jesse Exp $

package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.DefaultProjectNames.GlobalsType;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;

/*------------------------------------------------------------------------------------------*/
/*   A D V   A S S E T   B U I L D E R                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class AdvAssetBuilder 
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  AdvAssetBuilder
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
  AdvAssetBuilder
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
    super("AdvAssetBuilder",
          new VersionID("2.3.2"),
          "Temerity", 
          "The Advanced Temerity Asset Builder that works with the basic Temerity Names class.",
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
      UtilityParam param = 
        new IntegerUtilityParam
        (aSeparateModelPieces,
         "How many different pieces of geometry will the model be built in?  " +
         "Set this to zero if there only needs to be one model file.", 
         0); 
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
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyRigMEL, 
         "Is there a verification MEL to check the rig before it is submitted", 
         true); 
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
        (aBuildMiShadeNetwork,
         "Build an advanced shading setup for mental ray standalone rendering", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aShaderTTForApproval, 
         "Should there be a Turntable for shader verification and approval.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyShaderMEL, 
         "Is there a verification MEL to check the shaders before it is submitted", 
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
      
      layout.addEntry(2, aDoAnnotations);
      layout.addEntry(2, aBuildThumbnails);
      layout.addSeparator(2);
      //layout.addEntry(2, aBuildLowRez);
      
      LayoutGroup mayaGroup = 
        new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);
      
      mayaGroup.addEntry(aMayaContext);

      //layout.addEntry(2, aBuildLowRez);
      layout.addSubGroup(2, mayaGroup);
      
      {
        LayoutGroup modelGroup = 
          new LayoutGroup("ModelSettings", "Settings related to the model part of the asset process.", true);
        modelGroup.addEntry(aSeparateModelPieces);
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
        rigGroup.addEntry(aVerifyRigMEL);
        layout.addSubGroup(2, rigGroup);
      }
      {
        LayoutGroup matGroup = 
          new LayoutGroup("MaterialSettings", "Settings related to the material/shading part of the asset process.", true);
        //matGroup.addEntry(aSeparateShade);
        matGroup.addEntry(aBuildMiShadeNetwork);
        matGroup.addEntry(aShaderTTForApproval);
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
    addPassDependency(build, end);
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
  protected boolean performCheckIn()
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
  protected boolean pMultipleModels;
  protected int pNumberOfModels;
  protected boolean pModelTT;
  
  /* Rig */
  protected boolean pReRigSetup;
  protected boolean pHasBlendShapes;
  protected boolean pMakeFBX;
  protected boolean pMakeCurves;
  protected boolean pMakeDkAnim;
  protected boolean pRigTT;
  
  /* Material */
  //protected boolean pSeparateShade;
  protected boolean pBuildTextureNode;
  protected boolean pBuildMiShadeNetwork;
  protected boolean pSeparateAnimTextures;
  protected boolean pShadeTT;
  
  protected boolean pBuildLowRez;
  
  
  // builder conditions
  protected boolean pCheckInWhenDone;
  protected boolean pBuildThumbnails;
  
  // Mel Scripts
  protected String pFinalizeMEL;
  //protected String pLRFinalizeMEL;
  protected String pPlaceHolderMEL;
  protected String pMRInitMEL;
  protected String pVerifyModelMEL;
  protected String pVerifyRigMEL;
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
  public final static String aSeparateModelPieces = "SeparateModelPieces";
  public final static String aModelTTForApproval = "ModelTTForApproval";
  public final static String aVerifyModelMEL = "VerifyModelMEL";
  
  //Rig Parameters
  public final static String aHasBlendShapes = "HasBlendShapes";
  public final static String aReRigSetup = "ReRigSetup";
  public final static String aRigAnimForApproval = "RigAnimForApproval";
  public final static String aAnimationFormat = "AnimationFormat";
  public final static String aVerifyRigMEL = "VerifyRigMEL";
  
  //Material Parameters
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aBuildMiShadeNetwork = "BuildMiShadeNetwork";
  public final static String aVerifyShaderMEL = "VerifyShaderMEL";
  public final static String aShaderTTForApproval = "ShaderTTForApproval";
  public final static String aSeparateAnimTextures = "SeparateAnimTextures";
  //public final static String aSeparateShade = "SeparateShade";
  
  //public final static String aBuildLowRez = "BuildLowRez";
  
  public final static String aProjectName = "ProjectName";
  
  private static final long serialVersionUID = -720208594406378352L;
  
  
  
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
            "Information pass for the AdvAssetBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderQueries.setContext(pContext);
      //pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildMiShadeNetwork = 
        getBooleanParamValue(new ParamMapping(aBuildMiShadeNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pReRigSetup = getBooleanParamValue(new ParamMapping(aReRigSetup));
      pModelTT = getBooleanParamValue(new ParamMapping(aModelTTForApproval));
      pRigTT = getBooleanParamValue(new ParamMapping(aRigAnimForApproval));
      pShadeTT = getBooleanParamValue(new ParamMapping(aShaderTTForApproval));
      pBuildThumbnails = getBooleanParamValue(new ParamMapping(aBuildThumbnails));
      //pSeparateShade = getBooleanParamValue(new ParamMapping(aSeparateShade));
      
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
      
      //pLRFinalizeMEL = pProjectNames.getLowRezFinalizeScriptName(null, pAssetType);
      //addNonNullValue(pLRFinalizeMEL, pRequiredNodes);

      pMRInitMEL = pProjectNames.getMRayInitScriptName();
      addNonNullValue(pMRInitMEL, pRequiredNodes);

      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();
      addNonNullValue(pPlaceHolderMEL, pRequiredNodes);
      
      pVerifyModelMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyModelMEL)))
        pVerifyModelMEL = pProjectNames.getModelVerificationScriptName();
      addNonNullValue(pVerifyModelMEL, pRequiredNodes);

      pVerifyShaderMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyShaderMEL)))
        pVerifyShaderMEL = pProjectNames.getShaderVerificationScriptName();
      addNonNullValue(pVerifyShaderMEL, pRequiredNodes);
      
      pVerifyRigMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyRigMEL)))
        pVerifyRigMEL = pProjectNames.getRigVerificationScriptName();
      if (!pReRigSetup)
	addNonNullValue(pVerifyRigMEL, pRequiredNodes);
      
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
	if (pBuildMiShadeNetwork)
	  addNonNullValue(pProjectNames.getAssetShaderTTGlobals(GlobalsType.Standalone), pRequiredNodes);
	else
	  addNonNullValue(pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR), pRequiredNodes);
      }
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);

      pMultipleModels = false;
      pNumberOfModels = getIntegerParamValue(new ParamMapping(aSeparateModelPieces), new Range<Integer>(0, null));
      if (pNumberOfModels > 0)
        pMultipleModels = true;
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      pStageInfo.setDefaultSelectionKeys(keys);
      pStageInfo.setUseDefaultSelectionKeys(true);
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      pStageInfo.setDoAnnotations(annot);
      
      pTaskName = pProjectNames.getTaskName(pAssetName, pAssetType);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException
    {
      if (pMultipleModels) {
        ModelPiecesBuilder builder = 
          new ModelPiecesBuilder(pClient, pQueue, pAssetNames, pProjectNames, pBuilderInformation, pNumberOfModels, pModelTT);
        addSubBuilder(builder);
        addMappedParam(builder.getName(), ModelPiecesBuilder.aMayaContext, aMayaContext);
        addPassDependency(builder.getConstructPass("Build Pass"), getConstructPass("Build Pass")); 
      }
    }

    private static final long serialVersionUID = -8377745269730692702L;
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
            "The AdvAssetBuilder Pass which actually constructs the node networks.");
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
      doShaders();
    }

    protected void 
    doModel()
      throws PipelineException
    {
      String taskType = pProjectNames.getModelingTaskName();
      
      String editModel = pAssetNames.getModelEditNodeName();
      String verifyModel = pAssetNames.getModelVerifyNodeName();
      if (!pMultipleModels) {
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
        {
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
      }
    }
    
    protected void
    doRig()
      throws PipelineException
    {
      String taskType = pProjectNames.getRiggingTaskName();
      
      String modelFinal = pAssetNames.getModelFinalNodeName();
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
           skeleton, null, null, null);
        isEditNode(stage, taskType);
        stage.build();
      }
      
      String reRigNode = pAssetNames.getReRigNodeName();
      String finalRigScript = pProjectNames.getFinalRigScriptName();
      if (!pReRigSetup)
	finalRigScript = pVerifyRigMEL;
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
      
      doMaterials();
      String matName = pAssetNames.getMaterialNodeName();
      String matExportName = pAssetNames.getMaterialExportNodeName();
      String texNode = pAssetNames.getAnimTextureNodeName();
      
      String rigFinal = pAssetNames.getRigFinalNodeName();
      {
        NewAssetBuilderFinalStage stage = 
          new NewAssetBuilderFinalStage
          (pStageInfo,
           pContext, 
           pClient,
           pMayaContext,
           rigFinal, 
           reRigNode, matName, matExportName,
           pFinalizeMEL);
        if (pRigTT)
          isPrepareNode(stage, taskType);
        else
          isFocusNode(stage, taskType);
        stage.build();
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
	  {
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
	}
	if (pMakeDkAnim ) {
	  {
	    EmptyFileStage stage = 
	      new EmptyFileStage(pStageInfo, pContext, pClient, animCurves, "dkAnim");
	    stage.build();
	    pEmptyFileStages.add(stage);
	  }
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
	    stage = 
	      new AdvAssetBuilderTTStage
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
      }
      String assetFinal = pAssetNames.getFinalNodeName();
      String rigApprove = pAssetNames.getRigApproveNodeName();
      {
        ProductStage stage = 
          new ProductStage(pStageInfo, pContext, pClient, assetFinal, "ma", rigFinal, StageFunction.aMayaScene.toString());
        isProductNode(stage, taskType);
        stage.build();
      }
      String miFile = pAssetNames.getModelMiNodeName();
      if (pBuildMiShadeNetwork) {
	AdvAssetBuilderModelMiStage stage = 
	  new AdvAssetBuilderModelMiStage
	  (pStageInfo,
	   pContext,
	   pClient,
	   miFile,
	   assetFinal);
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
        if (pBuildMiShadeNetwork)
          sources.add(miFile);
        addNonNullValue(texFinalNode, sources);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, rigApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
        addToQueueList(rigApprove);
      }
    }
    
    protected void
    doMaterials()
      throws PipelineException
    {
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String taskType = pProjectNames.getRiggingTaskName();
      
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
        NewAssetBuilderMaterialExportStage stage = 
          new NewAssetBuilderMaterialExportStage
          (pStageInfo, 
           pContext,
           pClient,
           matExportName, 
           matName);
        isPrepareNode(stage, taskType);
        stage.build();
      }
    }
    
    protected void
    doShaders()
      throws PipelineException
    {
      String assetFinal = pAssetNames.getFinalNodeName();
      String taskType = pProjectNames.getShadingTaskName();

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
      
      String shdNode = pAssetNames.getShaderNodeName();
      {
	AdvAssetShaderStage stage = 
	  new AdvAssetShaderStage
	  (pStageInfo, 
	   pContext, 
	   pClient, 
	   pMayaContext, 
	   shdNode, 
	   assetFinal, texNode, pMRInitMEL);
	isEditNode(stage, taskType);
	stage.build();
	addToDisableList(shdNode);
      }
      
      String shdExport = pAssetNames.getShaderExportNodeName();
      {
	AssetBuilderShaderExportStage stage =
	  new AssetBuilderShaderExportStage
	  (pStageInfo, 
	   pContext, 
	   pClient,
	   shdExport, 
	   shdNode, 
	   pVerifyShaderMEL, 
	   pAssetNames.getAssetName());
	if (pShadeTT)
	  isPrepareNode(stage, taskType);
	else
	  isFocusNode(stage, taskType);
	stage.build();
      }

      String shdImg = pAssetNames.getShaderRenderNodeName();
      String thumb = pAssetNames.getShaderThumbNodeName();
      if (pShadeTT) {
	String shdTT = pAssetNames.getShaderTTNodeName();
	{
	  String setup = 
	    pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType);
	  AdvAssetBuilderTTStage stage = 
	    new AdvAssetBuilderTTStage
	    (pStageInfo, 
	     pContext, 
	     pClient,
	     pMayaContext, 
	     shdTT, 
	     pAssetNames.getModelFinalNodeName(), 
	     setup);
	  isEditNode(stage, taskType);
	  stage.build();
	  addToDisableList(shdTT);
	}

	if (pBuildMiShadeNetwork) {
	  String cam = pAssetNames.getShaderCamMiNodeName();
	  String camOver = pAssetNames.getShaderCamOverMiNodeName();
	  String lgt = pAssetNames.getShaderLgtMiNodeName();
	  String shd = pAssetNames.getShaderShdMiNodeName();
	  {
	    AdvAssetCamMiStage stage = 
	      new AdvAssetCamMiStage(pStageInfo, pContext, pClient, cam, shdTT);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  {
	    AdvAssetLgtMiStage stage = 
	      new AdvAssetLgtMiStage(pStageInfo, pContext, pClient, lgt, shdTT);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  {
	    AdvAssetShdMiStage stage =
	      new AdvAssetShdMiStage(pStageInfo, pContext, pClient, shd, shdNode);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  {
	    MRayCamOverrideStage stage =
	      new MRayCamOverrideStage(pStageInfo, pContext, pClient, camOver);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  String options = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Standalone);
	  String model = pAssetNames.getModelMiNodeName();
	  {
	    AdvAssetShdImgStage stage = 
	      new AdvAssetShdImgStage
	      (pStageInfo,
	       pContext,
	       pClient,
	       shdImg,
	       model,
	       lgt,
	       shd,
	       cam,
	       camOver,
	       options);
	    isFocusNode(stage, taskType);
	    stage.build();
	  }
	}
	else {
	  String globals = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR);
	  {
	    AdvAssetBuilderTTImgStage stage =
	      new AdvAssetBuilderTTImgStage
	      (pStageInfo, 
	       pContext, 
	       pClient, 
	       shdImg, 
	       shdTT, 
	       globals,
	       Renderer.MentalRay);
	    isFocusNode(stage, taskType);
	    stage.build();
	  }
	}
	if (pBuildThumbnails) {
	  ThumbnailStage stage = 
	    new ThumbnailStage(pStageInfo, pContext, pClient, thumb, "png", shdImg, 160);
	  isThumbnailNode(stage, taskType);
	  stage.build();
        }
      }
      String shdSubmit = pAssetNames.getShaderSubmitNode();
      {
        TreeSet<String> sources = new TreeSet<String>();
        if (pBuildThumbnails)
          sources.add(thumb);
        else if(pShadeTT)
          sources.add(shdImg);
        else
          sources.add(shdExport);
        TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
        addToQueueList(shdSubmit);
      }
      
      //Product Nodes
      String finalTex = null;
      String finalShd = pAssetNames.getShaderExportFinalNodeName();
      if (pBuildTextureNode) {
	finalTex = pAssetNames.getTextureFinalNodeName();
	{
	  EmptyFileStage stage = new EmptyFileStage(pStageInfo, pContext, pClient, finalTex);
	  isProductNode(stage, taskType);
	  stage.build();
	  pEmptyFileStages.add(stage);
	}
      }
      {
	ProductStage stage = 
	  new ProductStage(pStageInfo, pContext, pClient, finalShd, "ma", shdExport, StageFunction.aMayaScene.toString());
	isProductNode(stage, taskType);
	stage.build();
      }
      String shdApprove = pAssetNames.getShaderApproveNode();
      {
	TreeSet<String> sources = new TreeSet<String>();
	sources.add(finalShd);
	if (pBuildTextureNode)
	  sources.add(finalTex);
	TargetStage stage = new TargetStage(pStageInfo, pContext, pClient, shdApprove, sources);
	isApproveNode(stage, taskType);
	stage.build();
	addToQueueList(shdApprove);
      }
    }
    private static final long serialVersionUID = 9147958678499662058L;
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
    private static final long serialVersionUID = -6423722443630330292L;
  }
}
