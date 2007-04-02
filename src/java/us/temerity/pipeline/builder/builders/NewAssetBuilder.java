package us.temerity.pipeline.builder.builders;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
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
class NewAssetBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  NewAssetBuilder()
    throws PipelineException
  {
    this(new DefaultBuilderAnswers(BaseUtil.getDefaultUtilContext()), 
         new DefaultAssetNames());
  }
  
  public 
  NewAssetBuilder
  (
    AnswersBuilderQueries builderInfo,
    BaseNames assetNames
  ) 
    throws PipelineException
  {
    super("NewAssetBuilder", 
      	  "The Revised Temerity Asset Builder that works with the basic Temerity Names class.", 
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
      BuilderParam param = 
	new BooleanBuilderParam
	(aBuildSeparateHead,
         "Does the asset need to have a separate head file.  " +
         "Will also create a scene for blend shapes", 
         false); 
      addParam(param);
    }
    {
      BuilderParam param = 
	new BooleanBuilderParam
	(aAutoRigSetup,
         "Will this asset use a basic autorig setup?", 
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
    {
      BuilderParam param = 
	ListBuilderParam.createSelectionKeyParam
	(aSelectionKeys, 
	 "Which Selection Keys Should be assigned to the constructred nodes", 
	 null);
      addParam(param);
    }
    {
      
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
  
  protected String pFinalizeMelLR;

  protected String pPlaceHolderMEL;

  protected String pMRInitMEL;

  // builder conditions
  protected boolean pBuildLowRez;

  protected boolean pBuildTextureNode;

  protected boolean pBuildAdvancedShadingNetwork;

  protected boolean pCheckInWhenDone;
  
  protected boolean pBuildSeparateHead;
  
  protected boolean pAutoRigSetup;


  // private variables for tracking things.

  protected ArrayList<AssetBuilderModelStage> pModelStages = 
    new ArrayList<AssetBuilderModelStage>();
  
  protected ArrayList<EmptyFileStage> pRigInfoStages = 
    new ArrayList<EmptyFileStage>();
  
  protected ArrayList<EmptyMayaAsciiStage> pEmptyMayaScenes = 
    new ArrayList<EmptyMayaAsciiStage>();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "MayaContext";
  public final static String aBuildLowRez = "BuildLowRez";
  public final static String aBuildTextureNode = "BuildTextureNode";
  public final static String aBuildAdvancedShadingNetwork = "BuildAdvancedShadingNetwork";
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aBuildSeparateHead = "BuildSeparateHead";
  public final static String aSelectionKeys = "SelectionKeys";
  public final static String aProjectName = "ProjectName";
  public final static String aAutoRigSetup = "AutoRigSetup";
  
  
  
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
      super("Information Pass", 
	    "Information pass for the AssetBuilder");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
	"Starting the validate phase in the Information Pass.");
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      pBuildLowRez = getBooleanParamValue(new ParamMapping(aBuildLowRez));
      pBuildTextureNode = getBooleanParamValue(new ParamMapping(aBuildTextureNode));
      pBuildAdvancedShadingNetwork = 
	getBooleanParamValue(new ParamMapping(aBuildAdvancedShadingNetwork));
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      pBuildSeparateHead = getBooleanParamValue(new ParamMapping(aBuildSeparateHead));
      pAutoRigSetup = getBooleanParamValue(new ParamMapping(aAutoRigSetup));

      pFinalizeMEL = pAssetNames.getFinalizeScriptName();
      
      pFinalizeMelLR = pAssetNames.getLowRezFinalizeScriptName();

      pMRInitMEL = pAssetNames.getMRInitScriptName();

      pPlaceHolderMEL = pAssetNames.getPlaceholderScriptName();
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      BaseStage.setDefaultSelectionKeys(keys);
      BaseStage.useDefaultSelectionKeys(true);
      
      sLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = 3094875433886937402L;
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
      super("Build Pass", 
	    "The AssetBuilder Pass which actually constructs the node networks.");
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the build phase in the Build Pass");
      
      String modelName = pAssetNames.getModelNodeName(); 
      if(!nodeExists(modelName)) {
	AssetBuilderModelStage stage = 
	  new AssetBuilderModelStage
	  (pContext, 
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

	if (!nodeExists(headName)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (pContext, 
	     pMayaContext,
	     headName);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	if (!nodeExists(blendName)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (pContext, 
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

	if (skeleton != null && !nodeExists(skeleton)) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage
	    (pContext, 
	     pMayaContext,
	     skeleton);
	  stage.build();
	  pEmptyMayaScenes.add(stage);
	}

	if (rigInfo != null && !nodeExists(rigInfo)) {
	  EmptyFileStage stage = 
	    new EmptyFileStage
	    (pContext,
	     rigInfo);
	  stage.build();
	  pRigInfoStages.add(stage);
	}
      }

      String rigName = pAssetNames.getRigNodeName();
      if (!nodeExists(rigName)) {
	NewAssetBuilderRigStage stage = 
	  new NewAssetBuilderRigStage
	  (pContext, pMayaContext,
	   rigName,
	   modelName, headName, blendName,
	   skeleton, autoRigMEL, rigInfo);
	stage.build();
      }

      String matName = pAssetNames.getMaterialNodeName();
      if (!nodeExists(matName)) {
	NewAssetBuilderMaterialStage stage =
	  new NewAssetBuilderMaterialStage
	  (pContext, pMayaContext,
	   matName,
	   modelName, headName);
	stage.build();
	addToDisableList(matName);
      }

      String matExportName = pAssetNames.getMaterialExportNodeName();
      if (!nodeExists(matExportName)) {
	NewAssetBuilderMaterialExportStage stage = 
	  new NewAssetBuilderMaterialExportStage
	  (pContext, 
	   matExportName, 
	   matName);
	stage.build();
      }

      String finalName = pAssetNames.getFinalNodeName();
      if (!nodeExists(finalName)) {
	NewAssetBuilderFinalStage stage = 
	  new NewAssetBuilderFinalStage
	  (pContext, pMayaContext,
	   finalName, 
	   rigName, matName, matExportName,
	   pFinalizeMEL);
	stage.build();
	addToQueueList(finalName);
      }

      String lrFinalName = pAssetNames.getLowRezFinalNodeName();
      if (pBuildLowRez && !nodeExists(lrFinalName)) {
	NewAssetBuilderFinalStage stage =
	  new NewAssetBuilderFinalStage
	  (pContext, pMayaContext,
	   lrFinalName,
	   rigName, null, null,
	   pFinalizeMelLR);
	stage.build();
	addToQueueList(lrFinalName);
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
        addToDisableList(pAssetNames.getShaderNodeName());
      }
      if(!nodeExists(pAssetNames.getShaderExportNodeName())) {
        new AssetBuilderShaderExportStage(pContext, pMayaContext, pAssetNames
          .getShaderExportNodeName(), pAssetNames.getShaderNodeName(), pAssetNames.getAssetName()).build();
        addToQueueList(pAssetNames.getShaderExportNodeName());
        removeFromQueueList(pAssetNames.getFinalNodeName());
      }
    }
    private static final long serialVersionUID = -2455380248604721406L;
  }
   
  protected class
  FinalizeLoop
    extends SecondLoop
  {
    public 
    FinalizeLoop()
    {
      super("Finalize Pass", 
	    "The AssetBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the prebuild phase in the Finalize Pass");
      return getDisableList();
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      sLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
	"Starting the build phase in the Finalize Pass");
      for (AssetBuilderModelStage stage : pModelStages)
	stage.finalizeStage();
      for (EmptyFileStage stage : pRigInfoStages)
	stage.finalizeStage();
      for (EmptyMayaAsciiStage stage : pEmptyMayaScenes)
	stage.finalizeStage();
      disableActions();
    }
    private static final long serialVersionUID = 3776473936564046625L;
  }
}
