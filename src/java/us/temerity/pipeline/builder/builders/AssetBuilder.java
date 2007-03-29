package us.temerity.pipeline.builder.builders;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.interfaces.AnswersBuilderQueries;
import us.temerity.pipeline.builder.interfaces.DefaultBuilderAnswers;
import us.temerity.pipeline.builder.names.BuildsAssetNames;
import us.temerity.pipeline.builder.names.DefaultAssetNames;
import us.temerity.pipeline.builder.stages.*;

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
      	  "The basic Temerity Asset Builder that works with the basic Temerity Names class.", 
      	  true);
    pBuilderInfo = builderInfo;
    if (!(assetNames instanceof BuildsAssetNames))
      throw new PipelineException
        ("The naming class that was passed in does not implement the BuildsAssetNames interface");
    addSubBuilder(assetNames);
    {
      BuilderParam param = 
	new MayaContextBuilderParam
	(aMayaContext,
         "The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
         new MayaContext()); 
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aBuildLowRez, 
	 "Build a Low-Rez model network.", 
	 true); 
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aBuildTextureNode, 
	 "Build a texture node", 
	 true); 
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aBuildAdvancedShadingNetwork,
         "Build an advanced shading setup for mental ray standalone rendering", 
         false); 
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aCheckinWhenDone,
         "Automatically check-in all the nodes when building is finished.", 
         false); 
      addParam(param);
    }
    {
      ArrayList<String> projects = pBuilderInfo.getProjectList();
      BuilderParam param = 
	new OptionalEnumBuilderParam
	(aProjectName,
	 "The name of the project to build the asset in.", 
	 projects.get(0), 
	 projects); 
      addParam(param);
    }
    configNamer(assetNames);
    pAssetNames = (BuildsAssetNames) assetNames;
    addFirstLoopPass(new InformationLoop());
    addSecondLoopPass(new BuildLoop());
    addSecondLoopPass(new FinalizeLoop());
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
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   F I R S T   L O O P                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  protected class 
  InformationLoop
    extends FirstLoop
  {
    public 
    InformationLoop()
    {
      super("Information Pass", "Information pass for the AssetBuilder");
    }

    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvancedShadingNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));

      pFinalizeMEL = pAssetNames.getFinalizeScriptName();

      pMRInitMEL = pAssetNames.getMRInitScriptName();

      pPlaceHolderMEL = pAssetNames.getPlaceholderScriptName();

      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -1539635589668134156L;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E C O N D   L O O P                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected class
  BuildLoop
    extends SecondLoop
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
      if(!nodeExists(modName)) {
        AssetBuilderModelStage stage = 
          new AssetBuilderModelStage
          (pContext, 
           pMayaContext, 
           modName,
           pPlaceHolderMEL);
        stage.build();
        pModelStages.add(stage);
      }
      if(!nodeExists(rigName)) {
        new AssetBuilderRigStage(pContext, pMayaContext, rigName, modName).build();
      }
      if(!nodeExists(matName)) {
        new AssetBuilderMaterialStage(pContext, pMayaContext, matName, rigName).build();
        addToDisableList(matName);
      }
      if(!nodeExists(finalName)) {
        new AssetBuilderFinalStage(pContext, pMayaContext, finalName, matName, pFinalizeMEL).build();
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
      if(!nodeExists(textureNodeName)) {
        new AssetBuilderTextureStage(pContext, textureNodeName, parentName).build();
      }
    }

    protected void 
    buildShadingNetwork() 
      throws PipelineException
    {
      if(!nodeExists(pAssetNames.getShaderIncludeNodeName())) {
        new AssetBuilderShaderIncludeStage(pContext, pAssetNames.getShaderIncludeNodeName(),
          pAssetNames.getShaderIncludeGroupSecSeq()).build();
      }
      if(!nodeExists(pAssetNames.getShaderNodeName())) {
        new AssetBuilderShaderStage(pContext, pMayaContext, pAssetNames.getShaderNodeName(),
          pAssetNames.getFinalNodeName(), pAssetNames.getShaderIncludeNodeName(), pMRInitMEL).build();
      }
      if(!nodeExists(pAssetNames.getShaderExportNodeName())) {
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
    extends SecondLoop
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
      TreeSet<String> toReturn = new TreeSet<String>();
      for (AssetBuilderModelStage stage : pModelStages)
	toReturn.add(stage.getNodeName());
      return toReturn;
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, "Starting the build phase in the Finalize Pass");
      for (AssetBuilderModelStage stage : pModelStages)
	stage.finishModel();
      disableActions();
    }
    private static final long serialVersionUID = 3776473936564046625L;
  }
}
