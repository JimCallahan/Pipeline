// $Id: AdvAssetBuilder.java,v 1.4 2007/07/23 20:02:51 jesse Exp $

package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.DefaultProjectNames.GlobalsType;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;
import us.temerity.pipeline.stages.MayaRenderStage.Renderer;

/*------------------------------------------------------------------------------------------*/
/*   A D V   A S S E T   B U I L D E R                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class AdvAssetBuilder 
  extends ApprovalBuilder
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
    addCheckinWhenDoneParam();
    addSelectionKeyParam();
    addDoAnnotationParam();
    
//    {
//      UtilityParam param = 
//        new BooleanUtilityParam
//        (aBuildLowRez, 
//         "Build a Low-Rez model network.", 
//         true); 
//      addParam(param);
//    }
    
    

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
         "is not complete and selecting that option will cause an exception to be " +
         "thrown.", 
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
         false); 
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
         false); 
      addParam(param);
    }
    
    if (!assetNames.isGenerated())
      addSubBuilder(assetNames);
    if (!projectNames.isGenerated())
      addSubBuilder(projectNames);
    configNamer(assetNames, projectNames);
    
    pAssetNames = (BuildsAssetNames) assetNames;
    pProjectNames = (BuildsProjectNames) projectNames;

    addSetupPass(new InformationPass());
    ConstructPass build = new BuildPass();
    addConstuctPass(build);
    ConstructPass end = new FinalizePass();
    addConstuctPass(end);
    addPassDependency(build, end);
    
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
      layout.addEntry(1, aDoAnnotations);
      
      
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C H E C K - I N                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
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
  protected boolean pBuildTextureNode;
  protected boolean pBuildMiShadeNetwork;
  protected boolean pSeparateAnimTextures;
  protected boolean pShadeTT;
  
  //protected boolean pBuildLowRez;
  
  
  // builder conditions
  protected boolean pCheckInWhenDone;
  
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
  public final static String aShaderTTForApproval = "aShaderTTForApproval";
  public final static String aSeparateAnimTextures = "SeparateAnimTextures";
  
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
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      //pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildMiShadeNetwork = 
        getBooleanParamValue(new ParamMapping(aBuildMiShadeNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pReRigSetup = getBooleanParamValue(new ParamMapping(aReRigSetup));
      pModelTT = getBooleanParamValue(new ParamMapping(aModelTTForApproval));
      pRigTT = getBooleanParamValue(new ParamMapping(aRigAnimForApproval));
      pShadeTT = getBooleanParamValue(new ParamMapping(aShaderTTForApproval));
      
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
      
      //pLRFinalizeMEL = pProjectNames.getLowRezFinalizeScriptName(null, pAssetNames.getAssetType());

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
      pNumberOfModels = getIntegerParamValue(new ParamMapping(aSeparateModelPieces), 0);
      if (pNumberOfModels > 0)
        pMultipleModels = true;
      
      StageInformation info = pBuilderInformation.getStageInformation();
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      info.setDefaultSelectionKeys(keys);
      info.setUseDefaultSelectionKeys(true);
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      info.setDoAnnotations(annot);
      
      pTaskName = pProjectNames.getTaskName(pAssetName, pAssetType);
      
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
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      doModel();
      doRig();
      doShaders();
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
          if (pModelTT)
            isPrepareNode(stage, taskType);
          else
            isFocusNode(stage, taskType);
          stage.build();
        }
      }
      String modelTT = pAssetNames.getModelTTNodeName();
      String modelTTImg = pAssetNames.getModelTTImagesNodeName();
      if(pModelTT) {
        if (!checkExistance(modelTT)) {
          String modelTTSetup = 
            pProjectNames.getAssetModelTTSetup(pAssetName, pAssetType);
          AdvAssetBuilderTTStage stage =
            new AdvAssetBuilderTTStage
            (info,
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
        if (!checkExistance(modelTTImg)) {
          String globals = pProjectNames.getAssetModelTTGlobals();
          AdvAssetBuilderTTImgStage stage =
            new AdvAssetBuilderTTImgStage
            (info, 
             pContext, 
             pClient, 
             modelTTImg, 
             modelTT, 
             globals,
             Renderer.Software);
          isFocusNode(stage, taskType);
          stage.build();
        }
      }
      String modelSubmit = pAssetNames.getModelSubmitNodeName();
      if (!checkExistance(modelSubmit)) {
        TreeSet<String> sources = new TreeSet<String>();
        if (pModelTT)
          sources.add(modelTTImg);
        else
          sources.add(verifyModel);
        TargetStage stage = new TargetStage(info, pContext, pClient, modelSubmit, sources);
        stage.build();
        isSubmitNode(stage, taskType);
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
        isApproveNode(stage, taskType);
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
      String skelMel = pProjectNames.getPlaceholderSkelScriptName();
      if (skeleton != null && !checkExistance(skeleton)) {
	if (skelMel == null) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage(info, pContext, pClient, pMayaContext, skeleton);
	  isEditNode(stage, taskType);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}
	else {
	  AssetBuilderModelStage stage =
	    new AssetBuilderModelStage(info, pContext, pClient, pMayaContext, skeleton, skelMel);
	  isEditNode(stage, taskType);
	  stage.build();
	  pModelStages.add(stage);
	}
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
      
      String reRigNode = pAssetNames.getReRigNodeName();
      String finalRigScript = pProjectNames.getFinalRigScriptName();
      if (!pReRigSetup)
	finalRigScript = pVerifyRigMEL;
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
      if (pRigTT) {
	if (pMakeFBX) {
	  if (!checkExistance(animFBX)) {
	    EmptyFBXStage stage = new EmptyFBXStage(info, pContext, pClient, animFBX);
	    stage.build();
	  }
	}
	if (pMakeCurves ) {
	  if (!checkExistance(animCurves)) {
	    StandardStage stage = null;
	    if (pMakeFBX) {
	      stage = 
		new AdvAssetBuilderCurvesStage
		(info, 
		 pContext, 
		 pClient, 
		 pMayaContext, 
		 animCurves, 
		 skeleton, 
		 animFBX);
	    }
	    else {
	      stage = 
		new EmptyMayaAsciiStage(info, pContext, pClient, pMayaContext, animCurves);
	      pEmptyMayaScenes.add((EmptyMayaAsciiStage) stage);
	    }
	    stage.build();
	  }
	}
	if (pMakeDkAnim ) {
	  if (!checkExistance(animCurves)) {
	    EmptyFileStage stage = 
	      new EmptyFileStage(info, pContext, pClient, animCurves, "dkAnim");
	    stage.build();
	    pEmptyFileStages.add(stage);
	  }
	}
	if (!checkExistance(rigAnim)) {
	  String setup = 
	    pProjectNames.getAssetRigAnimSetup(pAssetName, pAssetType);
	  StandardStage stage = null;
	  if (pMakeCurves) {
	    stage = 
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
	  }
	  else if (pMakeDkAnim) {
	    // do stuff here
	  }
	  else {
	    stage = 
	      new AdvAssetBuilderTTStage
	      (info, 
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
	if (!checkExistance(rigImages)) {
	  String globals = 
	    pProjectNames.getAssetRigAnimGlobals();
	  AdvAssetBuilderTTImgStage stage =
	    new AdvAssetBuilderTTImgStage
	    (info, 
	     pContext, 
	     pClient, 
	     rigImages, 
	     rigAnim, 
	     globals,
	     Renderer.Software);
	  isFocusNode(stage, taskType);
	  stage.build();
	}
      }
      String rigSubmit = pAssetNames.getRigSubmitNodeName();
      if (!checkExistance(rigSubmit)) {
        TreeSet<String> sources = new TreeSet<String>();
        if (pRigTT)
          sources.add(rigImages);
        else
          sources.add(rigFinal);
        TargetStage stage = new TargetStage(info, pContext, pClient, rigSubmit, sources);
        isSubmitNode(stage, taskType);
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
      if (pBuildMiShadeNetwork) {
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
      String texFinalNode = null;
      if (pBuildTextureNode && pSeparateAnimTextures) {
	texFinalNode = pAssetNames.getAnimTextureFinalNodeName();
        if (!checkExistance(texFinalNode)) {
          TreeSet<String> sources = new TreeSet<String>();
          sources.add(texNode);
          TargetStage stage = new TargetStage(info, pContext, pClient, texFinalNode, sources);
          isProductNode(stage, taskType);
          stage.build();
        }
      }
      if (!checkExistance(rigApprove)) {
        TreeSet<String> sources = new TreeSet<String>();
        sources.add(assetFinal);
        if (pBuildMiShadeNetwork)
          sources.add(miFile);
        addNonNullValue(texFinalNode, sources);
        TargetStage stage = new TargetStage(info, pContext, pClient, rigApprove, sources);
        isApproveNode(stage, taskType);
        stage.build();
      }
    }
    
    protected void
    doMaterials()
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String modelFinal = pAssetNames.getModelFinalNodeName();
      String taskType = pProjectNames.getRiggingTaskName();
      
      String texNode = null;
      if (pBuildTextureNode && pSeparateAnimTextures) {
	texNode = pAssetNames.getAnimTextureNodeName();
	if (!checkExistance(texNode)) {
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(info, pContext, pClient, pMayaContext, texNode, true);
	  isEditNode(stage, taskType);
	  stage.build();
	}
      }
      
      String matName = pAssetNames.getMaterialNodeName();
      if (!checkExistance(matName)) {
        AdvAssetMaterialStage stage =
          new AdvAssetMaterialStage
          (info,
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
      if (!checkExistance(matExportName)) {
        NewAssetBuilderMaterialExportStage stage = 
          new NewAssetBuilderMaterialExportStage
          (info, 
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
      StageInformation info = pBuilderInformation.getStageInformation();
      String assetFinal = pAssetNames.getFinalNodeName();
      String taskType = pProjectNames.getShadingTaskName();

      String texNode = null;
      if (pBuildTextureNode) {
	texNode = pAssetNames.getTextureNodeName();
	if (!checkExistance(texNode)) {
	  MayaFTNBuildStage stage = 
	    new MayaFTNBuildStage(info, pContext, pClient, pMayaContext, texNode, true);
	  isEditNode(stage, taskType);
	  stage.build();
	}
      }
      
      String shdNode = pAssetNames.getShaderNodeName();
      if (!checkExistance(shdNode)) {
	AdvAssetShaderStage stage = 
	  new AdvAssetShaderStage
	  (info, 
	   pContext, 
	   pClient, 
	   pMayaContext, 
	   shdNode, 
	   assetFinal, texNode, pMRInitMEL);
	if (pShadeTT)
	  isEditNode(stage, taskType);
	stage.build();
	addToDisableList(shdNode);
      }
      
      String shdExport = pAssetNames.getShaderExportNodeName();
      if (!checkExistance(shdExport)) {
	AssetBuilderShaderExportStage stage =
	  new AssetBuilderShaderExportStage
	  (info, 
	   pContext, 
	   pClient,
	   pMayaContext, 
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
      if (pShadeTT) {
	String shdTT = pAssetNames.getShaderTTNodeName();
	if (!checkExistance(shdTT)) {
	  String setup = 
	    pProjectNames.getAssetShaderTTSetup(pAssetName, pAssetType);
	  AdvAssetBuilderTTStage stage = 
	    new AdvAssetBuilderTTStage
	    (info, 
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
	  if (!checkExistance(cam)) {
	    AdvAssetCamMiStage stage = 
	      new AdvAssetCamMiStage(info, pContext, pClient, cam, shdTT);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  if (!checkExistance(lgt)) {
	    AdvAssetLgtMiStage stage = 
	      new AdvAssetLgtMiStage(info, pContext, pClient, lgt, shdTT);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  if (!checkExistance(shd)) {
	    AdvAssetShdMiStage stage =
	      new AdvAssetShdMiStage(info, pContext, pClient, shd, shdNode);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  if (!checkExistance(camOver)) {
	    MRayCamOverrideStage stage =
	      new MRayCamOverrideStage(info, pContext, pClient, camOver);
	    isPrepareNode(stage, taskType);
	    stage.build();
	  }
	  String options = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Standalone);
	  String model = pAssetNames.getModelMiNodeName();
	  if (!checkExistance(shdImg)) {
	    AdvAssetShdImgStage stage = 
	      new AdvAssetShdImgStage
	      (info,
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
	  if (!checkExistance(shdImg)) {
	    AdvAssetBuilderTTImgStage stage =
	      new AdvAssetBuilderTTImgStage
	      (info, 
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
      }
      String shdSubmit = pAssetNames.getShaderSubmitNode();
      if (!checkExistance(shdSubmit)) {
        TreeSet<String> sources = new TreeSet<String>();
        if (pShadeTT)
          sources.add(shdImg);
        else
          sources.add(shdExport);
        TargetStage stage = new TargetStage(info, pContext, pClient, shdSubmit, sources);
        isSubmitNode(stage, taskType);
        stage.build();
      }
      
      //Product Nodes
      String finalTex = null;
      String finalShd = pAssetNames.getShaderExportFinalNodeName();
      if (pBuildTextureNode) {
	finalTex = pAssetNames.getTextureFinalNodeName();
	if (!checkExistance(finalTex)) {
	  EmptyFileStage stage = new EmptyFileStage(info, pContext, pClient, finalTex);
	  isProductNode(stage, taskType);
	  stage.build();
	  pEmptyFileStages.add(stage);
	}
      }
      if (!checkExistance(finalShd)) {
	ProductStage stage = 
	  new ProductStage(info, pContext, pClient, finalShd, "ma", shdExport);
	isPrepareNode(stage, taskType);
	stage.build();
      }
      String shdApprove = pAssetNames.getShaderApproveNode();
      if (!checkExistance(shdApprove)) {
	TreeSet<String> sources = new TreeSet<String>();
	sources.add(finalShd);
	if (pBuildTextureNode)
	  sources.add(finalTex);
	TargetStage stage = new TargetStage(info, pContext, pClient, shdApprove, sources);
	isApproveNode(stage, taskType);
	stage.build();
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
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the prebuild phase in the Finalize Pass");
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
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the build phase in the Finalize Pass");
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
