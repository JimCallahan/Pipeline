package us.temerity.pipeline.builder.maya2mr.v2_3_2;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   A D V   A S S E T   B U I L D E R                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class AdvAssetBuilder 
  extends ApprovalBuilder
{
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
    AnswersBuilderQueries builderInfo,
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
    pBuilderInfo = builderInfo;
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The asset naming class that was passed in does not implement " +
         "the BuildsAssetNames interface");
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
      
    addSubBuilder(assetNames);
    addSubBuilder(projectNames);
    
    // Globabl parameters
    {
      ArrayList<String> projects = pBuilderInfo.getProjectList();
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
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aCheckinWhenDone,
         "Automatically check-in all the nodes when building is finished.", 
         false); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildLowRez, 
         "Build a Low-Rez model network.", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        ListUtilityParam.createSelectionKeyParam
        (aSelectionKeys, 
         "Which Selection Keys Should be assigned to the constructred nodes", 
         null,
         qclient);
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
         "Is there a verification MEL to check the model before it is approved", 
         true); 
      addParam(param);
    }
    
    //Rig Parameters
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aAutoRigSetup,
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
        (aVerifyRigMEL, 
         "Is there a verification MEL to check the rig before it is approved", 
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
         false); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aVerifyShaderMEL, 
         "Is there a verification MEL to check the shaders before it is approved", 
         true); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aSeparateAnimTextures,
         "Build a separate node for textures just for the animators.", 
         false); 
      addParam(param);
    }
    
    configNamer(assetNames, projectNames);
    pAssetNames = (BuildsAssetNames) assetNames;
    pProjectNames = (BuildsProjectNames) projectNames;

    addSetupPass(new InformationPass());
    
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
      layout.addEntry(1, aActionOnExistance);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, null);
      layout.addEntry(1, aProjectName);
      
      
      LayoutGroup mayaGroup = 
        new LayoutGroup("MayaGroup", "Parameters related to Maya scenes", true);
      
      mayaGroup.addEntry(aMayaContext);

      layout.addEntry(2, aBuildLowRez);
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
        rigGroup.addEntry(aAutoRigSetup);  
        rigGroup.addEntry(aHasBlendShapes);
        rigGroup.addEntry(aVerifyRigMEL);
        layout.addSubGroup(2, rigGroup);
      }
      {
        LayoutGroup matGroup = 
          new LayoutGroup("MaterialSettings", "Settings related to the material/shading part of the asset process.", true);
        matGroup.addEntry(aBuildMiShadeNetwork);
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
    addMappedParam(assetNames.getName(), DefaultAssetNames.aProjectName, aProjectName);
    addMappedParam(projectNames.getName(), DefaultProjectNames.aProjectName, aProjectName);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected TreeSet<String>
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
  protected AnswersBuilderQueries pBuilderInfo;
  
  // conditions on what is being built
  
  /* Model */
  protected boolean pMultipleModels;
  protected int pNumberOfModels;
  protected boolean pModelTT;
  
  /* Rig */
  protected boolean pAutoRigSetup;
  protected boolean pHasBlendShapes;
  
  /* Material */
  protected boolean pBuildTextureNode;
  protected boolean pBuildMiShadingNetwork;
  protected boolean pSeparateAnimTextures;
  
  protected boolean pBuildLowRez;
  
  
  // builder conditions
  protected boolean pCheckInWhenDone;
  
  // Mel Scripts
  protected String pFinalizeMEL;
  protected String pLRFinalizeMEL;
  protected String pPlaceHolderMEL;
  protected String pMRInitMEL;
  protected String pVerifyModelMEL;
  protected String pVerifyRigMEL;
  protected String pVerifyShaderMEL;
  
  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
    new ArrayList<EmptyMayaAsciiStage>();
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  //Model Parameters
  public final static String aSeparateModelPieces = "SeparateModelPieces";
  public final static String aModelTTForApproval = "ModelTTForApproval";
  public final static String aVerifyModelMEL = "VerifyModelMEL";
  
  //Rig Parameters
  public final static String aHasBlendShapes = "HasBlendShapes";
  public final static String aAutoRigSetup = "AutoRigSetup"; 
  public final static String aVerifyRigMEL = "VerifyRigMEL";
  
  //Material Parameters
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aBuildMiShadeNetwork = "BuildMiShadeNetwork";
  public final static String aVerifyShaderMEL = "VerifyShaderMEL";
  public final static String aSeparateAnimTextures = "SeparateAnimTextures";
  
  public final static String aBuildLowRez = "BuildLowRez";
  
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aSelectionKeys = "SelectionKeys";
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
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildMiShadingNetwork = 
        getBooleanParamValue(new ParamMapping(aBuildMiShadeNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pAutoRigSetup = getBooleanParamValue(new ParamMapping(aAutoRigSetup));
      pModelTT = getBooleanParamValue(new ParamMapping(aModelTTForApproval));

      pFinalizeMEL = pProjectNames.getFinalizeScriptName(null, pAssetNames.getAssetType());
      
      pLRFinalizeMEL = pProjectNames.getLowRezFinalizeScriptName(null, pAssetNames.getAssetType());

      pMRInitMEL = pProjectNames.getMRayInitScriptName();

      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();
      
      pVerifyModelMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyModelMEL)))
        pVerifyModelMEL = pProjectNames.getModelVerificationScriptName();

      pVerifyRigMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyRigMEL)))
        pVerifyRigMEL = pProjectNames.getRigVerificationScriptName();
        
      pVerifyShaderMEL = null;
      if (getBooleanParamValue(new ParamMapping(aVerifyShaderMEL)))
        pVerifyShaderMEL = pProjectNames.getShaderVerificationScriptName();
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);

      pMultipleModels = false;
      pNumberOfModels = getIntegerParamValue(new ParamMapping(aSeparateModelPieces), 0);
      if (pNumberOfModels > 0)
        pMultipleModels = true;
      
      StageInformation info = pBuilderInformation.getStageInformation();
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      info.setDefaultSelectionKeys(keys);
      info.setUseDefaultSelectionKeys(true);
      
      pTaskName = pProjectNames.getTaskName(pAssetNames.getAssetName(), pAssetNames.getAssetType());
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the init phase in the Information Pass.");
      if (pMultipleModels) {
        ModelPiecesBuilder builder = 
          new ModelPiecesBuilder(pClient, pQueue, pAssetNames, pProjectNames, pBuilderInformation, pNumberOfModels);
        addSubBuilder(builder);
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
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      doModel();
      doRig();
    }

    protected void 
    doModel()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String taskType = pProjectNames.getModelingTaskName();
      
      String editModel = pAssetNames.getModelEditNodeName();
      String verifyModel = pAssetNames.getModelVerifyNodeName();
      if (!pMultipleModels) {
        if(!checkExistance(editModel)) {
          AssetBuilderModelStage stage = 
            new AssetBuilderModelStage
            (info,
             pContext,
             pClient,
             pMayaContext, 
             editModel,
             pPlaceHolderMEL);
          isEditNode(stage, taskType);
          stage.build();
          pModelStages.add(stage);
        }
        if(!checkExistance(verifyModel)) {
          TreeMap<String, String> edit = new TreeMap<String, String>();
          edit.put("mod", editModel);
          ModelPiecesVerifyStage stage =
            new ModelPiecesVerifyStage
            (info,
             pContext,
             pClient,
             pMayaContext,
             verifyModel, 
             edit,
             pVerifyModelMEL);
          isIntermediateNode(stage, taskType);
          stage.build();
        }
      }
      String modelTT = pAssetNames.getModelTTNodeName();
      String modelTTImg = pAssetNames.getModelTTImagesNodeName();
      if(pModelTT) {
        if (!checkExistance(modelTT)) {
          String modelTTSetup = 
            pProjectNames.getAssetModelTTSetup(pAssetNames.getAssetName(), pAssetNames.getAssetType());
          AdvAssetBuilderTTStage stage =
            new AdvAssetBuilderTTStage
            (info,
             pContext,
             pClient,
             pMayaContext,
             modelTT,
             verifyModel,
             modelTTSetup);
          isIntermediateNode(stage, taskType);
          stage.build();
          addToDisableList(modelTT);
        }
        if (checkExistance(modelTTImg)) {
          String globals = 
            pProjectNames.getAssetModelTTGlobals(pAssetNames.getAssetName(), pAssetNames.getAssetType());
          AdvAssetBuilderTTImgStage stage =
            new AdvAssetBuilderTTImgStage
            (info, 
             pContext, 
             pClient, 
             modelTTImg, 
             modelTT, 
             globals);
          isFocusNode(stage, taskType);
          stage.build();
        }
      }
      String modelSubmit = pAssetNames.getModelSubmitNodeName();
      if (!checkExistance(modelSubmit)) {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(modelTTImg);
        TargetStage stage = new TargetStage(info, pContext, pClient, modelSubmit, sources);
        stage.build();
        isSubmitNode(stage, taskType, pAssetNames.getModelApproveNodeName());
      }
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String modelApprove = pAssetNames.getModelApproveNodeName();
      if (!checkExistance(modelFinal)) {
        ProductStage stage = new ProductStage(info, pContext, pClient, modelFinal, "ma", verifyModel );
        isProductNode(stage, taskType);
        stage.build();
      }
      if (!checkExistance(modelApprove)) {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(modelFinal);
        TargetStage stage = new TargetStage(info, pContext, pClient, modelApprove, sources);
        //TODO add the classPath
        isApprovalNode(stage, taskType, null);
        stage.build();
      }
    }
    
    protected void
    doRig()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String taskType = pProjectNames.getRiggingTaskName();
      
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String blendShapes = null; 
      if (pHasBlendShapes) {
        blendShapes = pAssetNames.getBlendShapeModelNodeName();
        if (!checkExistance(blendShapes)) {
          EmptyMayaAsciiStage stage = 
            new EmptyMayaAsciiStage
            (info,
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
      if (skeleton != null && !checkExistance(skeleton)) {
        EmptyMayaAsciiStage stage = 
          new EmptyMayaAsciiStage
          (info,
           pContext,
           pClient,
           pMayaContext,
           skeleton);
        isEditNode(stage, taskType);
        stage.build();
        pEmptyMayaScenes.add(stage);
      }
      String rigEdit = pAssetNames.getRigEditNodeName();
      if (!checkExistance(rigEdit)) {
        NewAssetBuilderRigStage stage = 
          new NewAssetBuilderRigStage
          (info,
           pContext, 
           pClient,
           pMayaContext,
           rigEdit,
           modelFinal, null, blendShapes,
           skeleton, null, null);
        isEditNode(stage, taskType);
        stage.build();
      }
      String targetMEL = pVerifyRigMEL;
      if (pAutoRigSetup) {
        String rigMEL = pAssetNames.getRigMELNodeName();
        String reRigMEL = pAssetNames.getReRigMELNodeName();
        if (!checkExistance(reRigMEL)) {
          AdvAssetBuilderReRigMELStage stage = 
            new AdvAssetBuilderReRigMELStage(info, pContext, pClient, reRigMEL);
          stage.build();
        }
        if (!checkExistance(rigMEL)) {
          AdvAssetBuilderRigMELStage stage = 
            new AdvAssetBuilderRigMELStage(info, pContext, pClient, rigMEL, reRigMEL, pVerifyRigMEL);
          isIntermediateNode(stage, taskType);
          stage.build();
          targetMEL = rigMEL;
        }
      }
      String reRigNode = pAssetNames.getReRigNodeName();
      if (!checkExistance(reRigNode)) {
        AdvAssetBuilderReRigStage stage = 
          new AdvAssetBuilderReRigStage
          (info, 
           pContext, 
           pClient, 
           pMayaContext, 
           reRigNode, 
           modelFinal, 
           rigEdit, 
           blendShapes, 
           skeleton, 
           targetMEL,
           pAutoRigSetup);
        isIntermediateNode(stage, taskType);
        stage.build();
      }
      
      doMaterials();
      String matName = pAssetNames.getMaterialNodeName();
      String matExportName = pAssetNames.getMaterialExportNodeName();
      
      String rigFinal = pAssetNames.getRigFinalNodeName();
      if (!checkExistance(rigFinal)) {
        NewAssetBuilderFinalStage stage = 
          new NewAssetBuilderFinalStage
          (info,
           pContext, 
           pClient,
           pMayaContext,
           rigFinal, 
           reRigNode, matName, matExportName,
           pFinalizeMEL);
        isIntermediateNode(stage, taskType);
        stage.build();
      }
      
      //Submit Fun Time
      String animFBX = pAssetNames.getRigAnimFBXNodeName();
      String animCurves = pAssetNames.getRigAnimCurvesNodeName();
      String rigAnim = pAssetNames.getRigAnimNodeName();
      String rigImages = pAssetNames.getRigAnimImagesNodeName();
      if (!checkExistance(animFBX)) {
        EmptyFBXStage stage = new EmptyFBXStage(info, pContext, pClient, animFBX);
        stage.build();
      }
      if (!checkExistance(animCurves)) {
        AdvAssetBuilderCurvesStage stage = 
          new AdvAssetBuilderCurvesStage
          (info, 
           pContext, 
           pClient, 
           pMayaContext, 
           animCurves, 
           skeleton, 
           animFBX);
        stage.build();
      }
      if (!checkExistance(rigAnim)) {
        String setup = pProjectNames.getAssetRigAnimSetup(pAssetNames.getAssetName(), pAssetNames.getAssetType());
        AdvAssetBuilderAnimStage stage = 
          new AdvAssetBuilderAnimStage
          (info,
           pContext, 
           pClient,
           pMayaContext,
           rigAnim,
           animCurves,
           rigFinal,
           pAssetNames.getNameSpace(),
           setup);
        isIntermediateNode(stage, taskType);
        stage.build();
      }
      if (!checkExistance(rigImages)) {
        String globals = 
          pProjectNames.getAssetRigAnimGlobals(pAssetNames.getAssetName(), pAssetNames.getAssetType());
        AdvAssetBuilderTTImgStage stage =
          new AdvAssetBuilderTTImgStage
          (info, 
           pContext, 
           pClient, 
           rigImages, 
           rigAnim, 
           globals);
        isFocusNode(stage, taskType);
        stage.build();
      }
      String rigSubmit = pAssetNames.getRigSubmmitNodeName();
      if (!checkExistance(rigSubmit)) {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(rigImages);
        TargetStage stage = new TargetStage(info, pContext, pClient, rigSubmit, sources);
        isSubmitNode(stage, taskType, pAssetNames.getRigApproveNodeName());
        stage.build();
      }
      String assetFinal = pAssetNames.getFinalNodeName();
      String rigApprove = pAssetNames.getRigApproveNodeName();
      if (!checkExistance(assetFinal)) {
        ProductStage stage = new ProductStage(info, pContext, pClient, assetFinal, "ma", rigFinal);
        isProductNode(stage, taskType);
        stage.build();
      }
      String miFile = pAssetNames.getModelMiNodeName();
      if (pBuildMiShadingNetwork) {
        if (!checkExistance(miFile)) {
          AdvAssetBuilderModelMiStage stage = 
            new AdvAssetBuilderModelMiStage
            (info,
             pContext,
             pClient,
             miFile,
             assetFinal);
          isProductNode(stage, taskType);
          stage.build();
        }
      }
      if (!checkExistance(rigApprove)) {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(assetFinal);
        TargetStage stage = new TargetStage(info, pContext, pClient, rigApprove, sources);
        isApprovalNode(stage, taskType, null);
        //TODO put in class path
        stage.build();
      }
      doShaders();
      doTextures();
    }
    
    protected void
    doMaterials()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String modelFinal = pAssetNames.getModelFinalNodeName();
      
      String matName = pAssetNames.getMaterialNodeName();
      if (!checkExistance(matName)) {
        NewAssetBuilderMaterialStage stage =
          new NewAssetBuilderMaterialStage
          (info,
           pContext, 
           pClient,
           pMayaContext,
           matName,
           modelFinal,
           null);
        stage.build();
        addToDisableList(matName);
      }

      String matExportName = pAssetNames.getMaterialExportNodeName();
      if (!checkExistance(matExportName)) {
        NewAssetBuilderMaterialExportStage stage = 
          new NewAssetBuilderMaterialExportStage
          (info, 
           pContext,
           pClient,
           matExportName, 
           matName);
        stage.build();
      }
    }
    
    protected void
    doShaders()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String assetFinal = pAssetNames.getFinalNodeName();
    }
    
    protected void
    doTextures()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
    }

    
    private static final long serialVersionUID = 9147958678499662058L;
  }
}
