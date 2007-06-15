package us.temerity.pipeline.builder.maya2mr;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.stages.*;
import us.temerity.pipeline.stages.*;

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
  AssetBuilder()
    throws PipelineException
  {
    this(new DefaultBuilderAnswers(BaseUtil.getDefaultUtilContext()), 
         new DefaultAssetNames());
  }
  
  public 
  AssetBuilder
  (
    AnswersBuilderQueries builderInfo,
    BaseNames assetNames
  ) 
    throws PipelineException
  {
    super("AssetBuilder", 
      	  "The basic Temerity Asset Builder that works with the basic Temerity Names class.");
    pBuilderInfo = builderInfo;
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The naming class that was passed in does not implement the BuildsAssetNames interface");
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
	 null);
      addParam(param);
    }
    configNamer(assetNames);
    pAssetNames = (BuildsAssetNames) assetNames;
    addSetupPass(new InformationLoop());
    addConstuctPass(new BuildLoop());
    addConstuctPass(new FinalizeLoop());
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
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected class 
  InformationLoop
    extends SetupPass
  {
    public 
    InformationLoop()
    {
      super("Information Pass", "Information pass for the AssetBuilder");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvancedShadingNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));

      pFinalizeMEL = pAssetNames.getFinalizeScriptName();

      pMRInitMEL = pAssetNames.getMRInitScriptName();

      pPlaceHolderMEL = pAssetNames.getPlaceholderScriptName();

      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      BaseStage.setDefaultSelectionKeys(keys);
      BaseStage.useDefaultSelectionKeys(true);
      
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -1539635589668134156L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected class
  BuildLoop
    extends ConstructPass
  {
    public 
    BuildLoop()
    {
      super("Build Pass", "The AssetBuilder Pass which actually constructs the node networks.");
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the build phase in the Build Pass");
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
      if(!checkExistance(modName)) {
        AssetBuilderModelStage stage = 
          new AssetBuilderModelStage
          (pContext, 
           pMayaContext, 
           modName,
           pPlaceHolderMEL);
        stage.build();
        pModelStages.add(stage);
      }
      if(!checkExistance(rigName)) {
        new AssetBuilderRigStage(pContext, pMayaContext, rigName, modName).build();
      }
      if(!checkExistance(matName)) {
        new AssetBuilderMaterialStage(pContext, pMayaContext, matName, rigName).build();
        addToDisableList(matName);
      }
      if(!checkExistance(finalName)) {
        new AssetBuilderFinalStage
          (pContext, pMayaContext, finalName, matName, pFinalizeMEL).build();
        addToQueueList(finalName);
      }
    }

    protected void 
    buildTextureNode() 
      throws PipelineException
    {
      String textureNodeName = pAssetNames.getTextureNodeName();
      String parentName = pBuildAdvancedShadingNetwork ? pAssetNames.getShaderNodeName() : pAssetNames
        .getMaterialNodeName();
      if(!checkExistance(textureNodeName)) {
        new AssetBuilderTextureStage(pContext, textureNodeName, parentName).build();
      }
    }

    protected void 
    buildShadingNetwork() 
      throws PipelineException
    {
      if(!checkExistance(pAssetNames.getShaderIncludeNodeName())) {
        new AssetBuilderShaderIncludeStage(pContext, pAssetNames.getShaderIncludeNodeName(),
          pAssetNames.getShaderIncludeGroupSecSeq()).build();
      }
      if(!checkExistance(pAssetNames.getShaderNodeName())) {
        new AssetBuilderShaderStage(pContext, pMayaContext, pAssetNames.getShaderNodeName(),
          pAssetNames.getFinalNodeName(), pAssetNames.getShaderIncludeNodeName(), pMRInitMEL).build();
        addToDisableList(pAssetNames.getShaderNodeName());
      }
      if(!checkExistance(pAssetNames.getShaderExportNodeName())) {
        new AssetBuilderShaderExportStage(pContext, pMayaContext, pAssetNames
          .getShaderExportNodeName(), pAssetNames.getShaderNodeName(), pAssetNames.getAssetName()).build();
        addToQueueList(pAssetNames.getShaderExportNodeName());
        removeFromQueueList(pAssetNames.getFinalNodeName());
      }
    }
    private static final long serialVersionUID = 4847241152514088650L;
  }
   
  protected class
  FinalizeLoop
    extends ConstructPass
  {
    public 
    FinalizeLoop()
    {
      super("Finalize Pass", "The AssetBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the prebuild phase in the Finalize Pass");
      return getDisableList();
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the build phase in the Finalize Pass");
      for (AssetBuilderModelStage stage : pModelStages)
	stage.finalizeStage();
      disableActions();
    }
    private static final long serialVersionUID = 3776473936564046625L;
  }
}
