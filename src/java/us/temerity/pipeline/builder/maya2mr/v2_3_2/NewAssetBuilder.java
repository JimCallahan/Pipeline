package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   N E W   A S S E T   B U I L D E R                                                      */
/*------------------------------------------------------------------------------------------*/

public 
class NewAssetBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NewAssetBuilder
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
         info);
  }
  
  public 
  NewAssetBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderInfo,
    BaseNames assetNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("NewAssetBuilder",
      	  new VersionID("2.3.1"),
      	  "Temerity", 
      	  "The Revised Temerity Asset Builder that works with the basic Temerity Names class.",
      	  mclient,
      	  qclient,
      	  builderInformation);
    pBuilderInfo = builderInfo;
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The naming class that was passed in does not implement " +
         "the BuildsAssetNames interface");
    addSubBuilder(assetNames);
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
	(aBuildLowRez, 
	 "Build a Low-Rez model network.", 
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
      UtilityParam param = 
	new BooleanUtilityParam
	(aBuildAdvShadeNetwork,
         "Build an advanced shading setup for mental ray standalone rendering", 
         false); 
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
	(aBuildSeparateHead,
         "Does the asset need to have a separate head file.  " +
         "Will also create a scene for blend shapes", 
         false); 
      addParam(param);
    }
    {
      UtilityParam param = 
	new BooleanUtilityParam
	(aAutoRigSetup,
         "Will this asset use a basic autorig setup?", 
         false); 
      addParam(param);
    }
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
	ListUtilityParam.createSelectionKeyParam
	(aSelectionKeys, 
	 "Which Selection Keys Should be assigned to the constructred nodes", 
	 null,
	 qclient);
      addParam(param);
    }

    configNamer(assetNames);
    pAssetNames = (BuildsAssetNames) assetNames;
    addSetupPass(new InformationPass());
    ConstructPass build = new BuildPass();
    ConstructPass finalize = new FinalizeLoop();
    addConstuctPass(finalize);
    addConstuctPass(build);
    addPassDependency(build, finalize);
    
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
      LayoutGroup skGroup =
	new LayoutGroup("SelectionKeys", "List of default selection keys", true);
      
      mayaGroup.addEntry(aMayaContext);

      layout.addSubGroup(2, mayaGroup);
      
      layout.addEntry(2, aBuildLowRez);
      layout.addEntry(2, null);
      layout.addEntry(2, aBuildAdvShadeNetwork);
      layout.addEntry(2, aBuildTextureNode);
      layout.addEntry(2, null);
      layout.addEntry(2, aAutoRigSetup);
      layout.addEntry(2, aBuildSeparateHead);
      
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
    BaseNames names
  )
    throws PipelineException
  {
    addMappedParam(names.getName(), DefaultAssetNames.aProjectName, aProjectName);
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
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;

  // Mel Scripts
  protected String pFinalizeMEL;
  
  protected String pLRFinalizeMEL;

  protected String pPlaceHolderMEL;

  protected String pMRInitMEL;
  
  protected String pAutoRigMEL;

  // conditions on what is being built
  protected boolean pBuildLowRez;
  protected boolean pBuildTextureNode;
  protected boolean pBuildAdvancedShadingNetwork;
  protected boolean pBuildSeparateHead;
  protected boolean pAutoRigSetup;
  
  // builder conditions
  protected boolean pCheckInWhenDone;

  // variables for tracking things.

  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected ArrayList<EmptyFileStage> pRigInfoStages = 
    new ArrayList<EmptyFileStage>();
  
  protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
    new ArrayList<EmptyMayaAsciiStage>();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  public final static String aBuildLowRez = "BuildLowRez";
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aBuildAdvShadeNetwork = "BuildAdvShadeNetwork";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aBuildSeparateHead = "BuildSeparateHead";
  public final static String aSelectionKeys = "SelectionKeys";
  public final static String aProjectName = "ProjectName";
  public final static String aAutoRigSetup = "AutoRigSetup";
  
  private static final long serialVersionUID = -6941538298185257158L;

  
  
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
	    "Information pass for the NewAssetBuilder");
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
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvShadeNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pBuildSeparateHead = getBooleanParamValue(new ParamMapping(aBuildSeparateHead));
      pAutoRigSetup = getBooleanParamValue(new ParamMapping(aAutoRigSetup));

      pFinalizeMEL = pAssetNames.getFinalizeScriptName();
      
      pLRFinalizeMEL = pAssetNames.getLowRezFinalizeScriptName();

      pMRInitMEL = pAssetNames.getMRInitScriptName();

      pPlaceHolderMEL = pAssetNames.getPlaceholderScriptName();
      
      pAutoRigMEL = pAssetNames.getAutoRigScriptName();
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      StageInformation info = pBuilderInformation.getStageInformation();
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      info.setDefaultSelectionKeys(keys);
      info.setUseDefaultSelectionKeys(true);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = 3094875433886937402L;
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
	    "The NewAssetBuilder Pass which actually constructs the node networks.");
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the build phase in the Build Pass");
      
      String modelName = pAssetNames.getModelNodeName(); 
      if(!checkExistance(modelName)) {
	AssetBuilderModelStage stage = 
	  new AssetBuilderModelStage
	  (info,
	   pContext,
	   pClient,
	   pMayaContext, 
	   modelName,
	   pPlaceHolderMEL);
	stage.build();
	pModelStages.add(stage);
      }

      String headName = null;
      String blendName = null;
      if (pBuildSeparateHead) {
	headName = pAssetNames.getHeadModelNodeName();
	blendName = pAssetNames.getBlendShapeModelNodeName();

	if (!checkExistance(headName)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (info,
	     pContext,
	     pClient, 
	     pMayaContext,
	     headName);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	if (!checkExistance(blendName)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (info,
	     pContext,
	     pClient,
	     pMayaContext,
	     blendName);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}
      }
      String skeleton = null;
      String rigInfo = null;
      String autoRigMEL = null;

      if (pAutoRigSetup) {
	skeleton = pAssetNames.getSkeletonNodeName();
	rigInfo = pAssetNames.getRigInfoNodeName();
	autoRigMEL = pAssetNames.getAutoRigScriptName();

	if (skeleton != null && !checkExistance(skeleton)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (info,
	     pContext,
	     pClient,
	     pMayaContext,
	     skeleton);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	if (rigInfo != null && !checkExistance(rigInfo)) {
	  EmptyFileStage stage = 
	    new EmptyFileStage
	    (info,
	     pContext,
	     pClient,
	     rigInfo);
	  stage.build();
	  pRigInfoStages.add(stage);
	}
      }

      String rigName = pAssetNames.getRigNodeName();
      if (!checkExistance(rigName)) {
	NewAssetBuilderRigStage stage = 
	  new NewAssetBuilderRigStage
	  (info,
	   pContext, 
	   pClient,
	   pMayaContext,
	   rigName,
	   modelName, headName, blendName,
	   skeleton, autoRigMEL, rigInfo);
	stage.build();
      }

      String matName = pAssetNames.getMaterialNodeName();
      if (!checkExistance(matName)) {
	NewAssetBuilderMaterialStage stage =
	  new NewAssetBuilderMaterialStage
	  (info,
	   pContext, 
	   pClient,
	   pMayaContext,
	   matName,
	   modelName, headName);
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

      String finalName = pAssetNames.getFinalNodeName();
      if (!checkExistance(finalName)) {
	NewAssetBuilderFinalStage stage = 
	  new NewAssetBuilderFinalStage
	  (info,
	   pContext, 
	   pClient,
	   pMayaContext,
	   finalName, 
	   rigName, matName, matExportName,
	   pFinalizeMEL);
	stage.build();
	addToQueueList(finalName);
	addToCheckInList(finalName);
      }

      String lrFinalName = pAssetNames.getLowRezFinalNodeName();
      if (pBuildLowRez && !checkExistance(lrFinalName)) {
	NewAssetBuilderFinalStage stage =
	  new NewAssetBuilderFinalStage
	  (info,
	   pContext, 
	   pClient,
	   pMayaContext,
	   lrFinalName,
	   rigName, null, null,
	   pLRFinalizeMEL);
	stage.build();
	addToQueueList(lrFinalName);
	addToCheckInList(lrFinalName);
      }
	if(pBuildAdvancedShadingNetwork)
	  buildShadingNetwork();
	if(pBuildTextureNode)
	  buildTextureNode();
    }
    
    protected void 
    buildTextureNode() 
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      String textureNodeName = pAssetNames.getTextureNodeName();
      String parentName = pBuildAdvancedShadingNetwork ? pAssetNames.getShaderNodeName() : pAssetNames
        .getMaterialNodeName();
      if(!checkExistance(textureNodeName)) {
        new AssetBuilderTextureStage(info, pContext, pClient, textureNodeName, parentName).build();
      }
    }

    protected void 
    buildShadingNetwork() 
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      if(!checkExistance(pAssetNames.getShaderIncludeNodeName())) {
        new AssetBuilderShaderIncludeStage(info, pContext, pClient, pAssetNames.getShaderIncludeNodeName(),
          pAssetNames.getShaderIncludeGroupSecSeq()).build();
      }
      if(!checkExistance(pAssetNames.getShaderNodeName())) {
        new AssetBuilderShaderStage(info, pContext, pClient, pMayaContext, pAssetNames.getShaderNodeName(),
          pAssetNames.getFinalNodeName(), pAssetNames.getShaderIncludeNodeName(), pMRInitMEL).build();
        addToDisableList(pAssetNames.getShaderNodeName());
      }
      if(!checkExistance(pAssetNames.getShaderExportNodeName())) {
        new AssetBuilderShaderExportStage(info, pContext, pClient, pMayaContext, pAssetNames
          .getShaderExportNodeName(), pAssetNames.getShaderNodeName(), pAssetNames.getAssetName()).build();
        addToQueueList(pAssetNames.getShaderExportNodeName());
        removeFromQueueList(pAssetNames.getFinalNodeName());
        addToCheckInList(pAssetNames.getShaderExportNodeName());
        removeFromCheckInList(pAssetNames.getFinalNodeName());
      }
    }

    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> list = new TreeSet<String>();
      addNonNullValue(pFinalizeMEL, list);
      addNonNullValue(pPlaceHolderMEL, list);
      if (pBuildLowRez)
	addNonNullValue(pLRFinalizeMEL, list);
      if (pBuildAdvancedShadingNetwork)
	addNonNullValue(pMRInitMEL, list);
      if (pAutoRigSetup)
	addNonNullValue(pAutoRigMEL, list);
      return list;
    }
    private static final long serialVersionUID = -2455380248604721406L;
  }
   
  protected 
  class FinalizeLoop
    extends ConstructPass
  {
    public 
    FinalizeLoop()
    {
      super("Finalize Pass", 
	    "The NewAssetBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the prebuild phase in the Finalize Pass");
      TreeSet<String> toReturn = new TreeSet<String>(getDisableList());
      if (pBuildSeparateHead)
	toReturn.add(pAssetNames.getBlendShapeModelNodeName());
      if (pAutoRigSetup) {
	toReturn.add(pAssetNames.getRigInfoNodeName());
	toReturn.add(pAssetNames.getSkeletonNodeName());
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
      for (EmptyFileStage stage : pRigInfoStages)
	stage.finalizeStage();
      for (EmptyMayaAsciiStage stage : pEmptyMayaScenes)
	stage.finalizeStage();
      disableActions();
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> list = new TreeSet<String>();
      
      list.addAll(getDisableList());
      addNonNullValue(pAssetNames.getModelNodeName(), list);
      if (pAutoRigSetup) {
	addNonNullValue(pAssetNames.getRigInfoNodeName(), list);
	addNonNullValue(pAssetNames.getSkeletonNodeName(), list);
      }
      if (pBuildSeparateHead) {
	addNonNullValue(pAssetNames.getBlendShapeModelNodeName(), list);
	addNonNullValue(pAssetNames.getHeadModelNodeName(), list);
      }
      return list;
    }
    private static final long serialVersionUID = 3776473936564046625L;
  }
}