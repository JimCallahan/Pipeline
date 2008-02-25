package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.stages.EmptyFileStage;
import us.temerity.pipeline.stages.EmptyMayaAsciiStage;

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
         new DefaultProjectNames(mclient, qclient),
         info);
  }
  
  public 
  NewAssetBuilder
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
    super("NewAsset",
      	  "The Revised Temerity Asset Builder that works with the basic Temerity Names class.",
      	  mclient,
      	  qclient,
      	  builderInformation);
    pBuilderInfo = builderInfo;
 
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The naming class that was passed in does not implement the BuildsAssetNames interface");
    
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
      
    addSubBuilder(projectNames);
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
    addCheckinWhenDoneParam();
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
    addSelectionKeyParam();

    configNamer(assetNames);
    setDefaultEditors();
    
    pAssetNames = (BuildsAssetNames) assetNames;
    addSetupPasses();
    addConstructPasses();
    
    {
      AdvancedLayoutGroup layout = 
	new AdvancedLayoutGroup
	  ("Builder Information", 
	   "The pass where all the basic stageInformation about the asset is collected " +
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
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  // Context
  protected MayaContext pMayaContext;

  // Names
  protected BuildsAssetNames pAssetNames;
  
  protected BuildsProjectNames pProjectNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;

  // Mel Scripts
  protected String pFinalizeMEL;
  
  protected String pLRFinalizeMEL;

  protected String pPlaceHolderMEL;

  protected String pMRInitMEL;
  
  protected String pFinalRigMEL;

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
  public final static String aBuildSeparateHead = "BuildSeparateHead";
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
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvShadeNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pBuildSeparateHead = getBooleanParamValue(new ParamMapping(aBuildSeparateHead));
      pAutoRigSetup = getBooleanParamValue(new ParamMapping(aAutoRigSetup));

      pFinalizeMEL = pProjectNames.getFinalizeScriptName(null, pAssetNames.getAssetType());
      
      pLRFinalizeMEL = pProjectNames.getLowRezFinalizeScriptName(null, pAssetNames.getAssetType());

      pMRInitMEL = pProjectNames.getMRayInitScriptName();

      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();
      
      pFinalRigMEL = pProjectNames.getFinalRigScriptName();
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      StageInformation stageInfo = getStageInformation();
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      stageInfo.setDefaultSelectionKeys(keys);
      stageInfo.setUseDefaultSelectionKeys(true);
      
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
      StageInformation stageInfo = getStageInformation();
      String modelName = pAssetNames.getModelNodeName(); 
      {
	AssetBuilderModelStage stage = 
	  new AssetBuilderModelStage
	  (stageInfo,
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

	{
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (stageInfo,
	     pContext,
	     pClient, 
	     pMayaContext,
	     headName);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	{
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (stageInfo,
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
	autoRigMEL = pProjectNames.getFinalRigScriptName();

	if (skeleton != null) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (stageInfo,
	     pContext,
	     pClient,
	     pMayaContext,
	     skeleton);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	if (rigInfo != null) {
	  EmptyFileStage stage = 
	    new EmptyFileStage
	    (stageInfo,
	     pContext,
	     pClient,
	     rigInfo);
	  stage.build();
	  pRigInfoStages.add(stage);
	}
      }

      String rigName = pAssetNames.getRigNodeName();
      {
	NewAssetBuilderRigStage stage = 
	  new NewAssetBuilderRigStage
	  (stageInfo,
	   pContext, 
	   pClient,
	   pMayaContext,
	   rigName,
	   modelName, headName, blendName,
	   skeleton, autoRigMEL, rigInfo, null);
	stage.build();
      }

      String matName = pAssetNames.getMaterialNodeName();
      {
	NewAssetBuilderMaterialStage stage =
	  new NewAssetBuilderMaterialStage
	  (stageInfo,
	   pContext, 
	   pClient,
	   pMayaContext,
	   matName,
	   modelName, headName);
	stage.build();
	addToDisableList(matName);
      }

      String matExportName = pAssetNames.getMaterialExportNodeName();
      {
	NewAssetBuilderMaterialExportStage stage = 
	  new NewAssetBuilderMaterialExportStage
	  (stageInfo, 
	   pContext,
	   pClient,
	   matExportName, 
	   matName);
	stage.build();
      }

      String finalName = pAssetNames.getFinalNodeName();
      {
	NewAssetBuilderFinalStage stage = 
	  new NewAssetBuilderFinalStage
	  (stageInfo,
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
      if (pBuildLowRez) {
	NewAssetBuilderFinalStage stage =
	  new NewAssetBuilderFinalStage
	  (stageInfo,
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
      StageInformation stageInfo = getStageInformation();
      String textureNodeName = pAssetNames.getTextureNodeName();
      String parentName = pBuildAdvancedShadingNetwork ? pAssetNames.getShaderNodeName() : pAssetNames
        .getMaterialNodeName();
      new AssetBuilderTextureStage(stageInfo, pContext, pClient, textureNodeName, parentName).build();
    }

    protected void 
    buildShadingNetwork() 
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      {
        new AssetBuilderShaderIncludeStage(stageInfo, pContext, pClient, pAssetNames.getShaderIncludeNodeName(),
          pAssetNames.getShaderIncludeGroupSecSeq()).build();
      }
      {
        new AssetBuilderShaderStage(stageInfo, pContext, pClient, pMayaContext, pAssetNames.getShaderNodeName(),
          pAssetNames.getFinalNodeName(), pAssetNames.getShaderIncludeNodeName(), pMRInitMEL).build();
        addToDisableList(pAssetNames.getShaderNodeName());
      }
      {
        new AssetBuilderShaderExportStage
        (stageInfo, 
         pContext, 
         pClient, 
         pAssetNames.getShaderExportNodeName(), 
         pAssetNames.getShaderNodeName(), 
         null, 
         pAssetNames.getAssetName()).build();
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
	addNonNullValue(pFinalRigMEL, list);
      return list;
    }
    private static final long serialVersionUID = -2455380248604721406L;
  }
   
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("Finalize Pass", 
	    "The NewAssetBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
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
