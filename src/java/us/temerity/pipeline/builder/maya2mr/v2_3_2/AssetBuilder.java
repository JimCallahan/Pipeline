package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.stages.StageInformation;

/*------------------------------------------------------------------------------------------*/
/*   A S S E T   B U I L D E R                                                              */
/*------------------------------------------------------------------------------------------*/

public
class AssetBuilder
  extends BaseBuilder
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
    AnswersBuilderQueries builderInfo,
    BaseNames assetNames,
    BaseNames projectNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("AssetBuilder", 
          new VersionID("2.3.1"),
          "Temerity", 
      	  "The basic Temerity Asset Builder that works with the basic Temerity Names class.",
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
	(aBuildAdvancedShadingNetwork,
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
    configNamer(assetNames, projectNames);
    pAssetNames = (BuildsAssetNames) assetNames;
    pProjectNames = (BuildsProjectNames) projectNames;
    addSetupPass(new InformationPass());
    addConstuctPass(new BuildPass());
    addConstuctPass(new FinalizePass());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R   M A P P I N G                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected void 
  configNamer 
  (
    BaseNames names,
    BaseNames projectNames
  )
    throws PipelineException
  {
    addMappedParam(names.getName(), DefaultAssetNames.aProjectName, aProjectName);
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

  // Mel Scripts
  protected String pFinalizeMEL;

  protected String pPlaceHolderMEL;

  protected String pMRInitMEL;

  // builder conditions
  protected boolean pBuildLowRez;

  protected boolean pBuildTextureNode;

  protected boolean pBuildAdvancedShadingNetwork;

  protected boolean pCheckInWhenDone;

  // private variables for tracking things.

  protected ArrayList<AssetBuilderModelStage> pModelStages = new ArrayList<AssetBuilderModelStage>();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "MayaContext";
  public final static String aBuildLowRez = "BuildLowRez";
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aBuildAdvancedShadingNetwork = "BuildAdvancedShadingNetwork";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aProjectName = "ProjectName";
  public final static String aSelectionKeys = "SelectionKeys";
  
  private static final long serialVersionUID = -8898612001759637874L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected class 
  InformationPass
    extends SetupPass
  {
    public 
    InformationPass()
    {
      super("Information Pass", "Information pass for the AssetBuilder");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvancedShadingNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));

      pFinalizeMEL = pProjectNames.getFinalizeScriptName(null, pAssetNames.getAssetType());

      pMRInitMEL = pProjectNames.getMRInitScriptName();

      pPlaceHolderMEL = pProjectNames.getPlaceholderScriptName();

      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      StageInformation stageInfo = pBuilderInformation.getStageInformation();
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      stageInfo.setDefaultSelectionKeys(keys);
      stageInfo.setUseDefaultSelectionKeys(true);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -1539635589668134156L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected class
  BuildPass
    extends ConstructPass
  {
    public 
    BuildPass()
    {
      super("Build Pass", "The AssetBuilder Pass which actually constructs the node networks.");
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the build phase in the Build Pass");
      buildAssetTree
      (pAssetNames.getFinalNodeName(), 
       pAssetNames.getMaterialNodeName(),
       pAssetNames.getRigNodeName(), 
       pAssetNames.getModelNodeName());
      if(pBuildLowRez)
	buildAssetTree
	(pAssetNames.getLowRezFinalNodeName(), 
	 pAssetNames.getLowRezMaterialNodeName(), 
	 pAssetNames.getLowRezRigNodeName(), 
	 pAssetNames.getLowRezModelNodeName());
      if(pBuildAdvancedShadingNetwork)
	buildShadingNetwork();
      if(pBuildTextureNode)
	buildTextureNode();
    }
    
    protected void 
    buildAssetTree
    (
      String finalName, 
      String matName, 
      String rigName, 
      String modName
    ) 
      throws PipelineException
    {
      StageInformation info = pBuilderInformation.getStageInformation();
      if(!checkExistance(modName)) {
        AssetBuilderModelStage stage = 
          new AssetBuilderModelStage
          (info,
           pContext,
           pClient,
           pMayaContext, 
           modName,
           pPlaceHolderMEL);
        stage.build();
        pModelStages.add(stage);
      }
      if(!checkExistance(rigName)) {
        new AssetBuilderRigStage(info, pContext, pClient, pMayaContext, rigName, modName).build();
      }
      if(!checkExistance(matName)) {
        new AssetBuilderMaterialStage(info, pContext, pClient, pMayaContext, matName, rigName).build();
        addToDisableList(matName);
      }
      if(!checkExistance(finalName)) {
        new AssetBuilderFinalStage
          (info, pContext, pClient, pMayaContext, finalName, matName, pFinalizeMEL).build();
        addToQueueList(finalName);
      }
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
        new AssetBuilderShaderStage(info, pContext, pClient,  pMayaContext, pAssetNames.getShaderNodeName(),
          pAssetNames.getFinalNodeName(), pAssetNames.getShaderIncludeNodeName(), pMRInitMEL).build();
        addToDisableList(pAssetNames.getShaderNodeName());
      }
      if(!checkExistance(pAssetNames.getShaderExportNodeName())) {
        new AssetBuilderShaderExportStage(info, pContext, pClient,  pMayaContext, pAssetNames
          .getShaderExportNodeName(), pAssetNames.getShaderNodeName(), pAssetNames.getAssetName()).build();
        addToQueueList(pAssetNames.getShaderExportNodeName());
        removeFromQueueList(pAssetNames.getFinalNodeName());
      }
    }
    private static final long serialVersionUID = 4847241152514088650L;
  }
   
  protected class
  FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("Finalize Pass", "The AssetBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the prebuild phase in the Finalize Pass");
      return getDisableList();
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the build phase in the Finalize Pass");
      for (AssetBuilderModelStage stage : pModelStages)
	stage.finalizeStage();
      disableActions();
    }
    private static final long serialVersionUID = 3776473936564046625L;
  }
}
