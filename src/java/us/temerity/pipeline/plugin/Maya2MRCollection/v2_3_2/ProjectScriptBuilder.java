package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.DefaultProjectNames.GlobalsType;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.stages.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   S C R I P T   B U I L D E R                                            */
/*------------------------------------------------------------------------------------------*/

public 
class ProjectScriptBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  ProjectScriptBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient,
      qclient,
      new DefaultProjectNames(mclient, qclient),
      new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)), 
      info);
  }
  
  public
  ProjectScriptBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BaseNames projectNames,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ProjectScript",
          "The Project Script Builder that works with the basic Temerity Project Names class.",
          mclient,
          qclient,
          builderInformation);
    pBuilderInfo = builderInfo;
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
    
    // Global parameters
    {
      ArrayList<String> projects = pBuilderInfo.getProjectList();
      if (projects.size() > 0) {
	UtilityParam param = 
	  new OptionalEnumUtilityParam
	  (aProjectName,
	   "The name of the project to build the asset in.", 
	   projects.get(0), 
	   projects); 
	addParam(param);
      }
      else {
	UtilityParam param = 
	  new StringUtilityParam
	  (aProjectName,
	   "The name of the project to build the asset in.", 
	   "projects"); 
	addParam(param);
      }
    }
    
    addCheckinWhenDoneParam();
    addSelectionKeyParam();
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aFinalizeAssets,
         "Build finalize scripts for the different asset types.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aFinalizeAssetsLR,
         "Build low rez finalize scripts for the different asset types.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aCopyShading,
         "Build a copy shading MEL script and use it in the finalize scripts.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aCopyRigging,
         "Build a copy rigging MEL script.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aGlobals,
         "Build render globals for all asset turntables.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aMentalRayInit,
         "Build a Mental Ray initialization script.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aPlaceholder,
         "Build placeholder MEL scripts for each asset type.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aAssetVerification,
         "Build verification mel scripts for each step in the asset process.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aDefaultScripts,
         "There are several scripts that the script builder can create default values for." +
         "These are scripts for which no Action exists in Pipeline.  If you are planning to " +
         "create your own versions of these scripts, you can leave this set to false.  If you" +
         "need to quickly create asset networks that will build and can be checked in, without" +
         "having to create your own mel scripts, set this to (YES)", 
         true); 
      addParam(param);
    }
    
    if (!projectNames.isGenerated()) {  
      addSubBuilder(projectNames);
      configNamer(projectNames);
    }
    
    pProjectNames = (BuildsProjectNames) projectNames;
    
    setDefaultEditors();
    
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
      layout.addColumn("MEL Scripts", true);
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aCheckinWhenDone);
      layout.addEntry(1, aActionOnExistence);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, null);
      layout.addEntry(1, aProjectName);
      LayoutGroup skGroup =
        new LayoutGroup("SelectionKeys", "List of default selection keys", true);
      skGroup.addEntry(aSelectionKeys);
      layout.addSubGroup(1, skGroup);
      
      layout.addEntry(2, aDefaultScripts);
      layout.addSeparator(2);
      layout.addEntry(2, aFinalizeAssets);
      layout.addEntry(2, aFinalizeAssetsLR);
      layout.addEntry(2, aAssetVerification);
      layout.addEntry(2, aCopyShading);
      layout.addEntry(2, aCopyRigging);
      layout.addEntry(2, aPlaceholder);
      layout.addSeparator(2);
      layout.addEntry(2, aGlobals);
      layout.addSeparator(2);
      layout.addEntry(2, aMentalRayInit);
      
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
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S U B - B U I L D E R   M A P P I N G                                                */
  /*----------------------------------------------------------------------------------------*/
  
  protected void 
  configNamer 
  (
    BaseNames projectNames
  )
    throws PipelineException
  {
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
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  protected BuildsProjectNames pProjectNames;
  
  protected boolean pCheckInWhenDone;
  
  protected boolean pFinalizeAssets;
  protected boolean pFinalizeAssetsLR;
  protected boolean pCopyShading;
  protected boolean pCopyRigging;
  protected boolean pGlobals;
  protected boolean pMRayInit;
  protected boolean pPlaceholder;
  protected boolean pAssetVerification;
  
  protected boolean pDefaultScripts;
  
  protected ArrayList<EmptyFileStage> pEmptyFiles = 
    new ArrayList<EmptyFileStage>();
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";
  
  public final static String aFinalizeAssets = "FinalizeAssets";
  public final static String aFinalizeAssetsLR = "FinalizeAssetsLR";
  public final static String aCopyShading = "CopyShading";
  public final static String aCopyRigging = "CopyRigging";
  public final static String aGlobals = "Globals";
  public final static String aMentalRayInit = "MentalRayInit";
  public final static String aPlaceholder = "Placeholder";
  public final static String aAssetVerification = "AssetVerification";
  public final static String aDefaultScripts = "DefaultScripts";

  
  private static final long serialVersionUID = -8870111744415216596L;
  
  
  
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
            "Information pass for the ProjectMELBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      
      pDefaultScripts = getBooleanParamValue(new ParamMapping(aDefaultScripts));
      
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      pFinalizeAssets = getBooleanParamValue(new ParamMapping(aFinalizeAssets));
      pFinalizeAssetsLR = getBooleanParamValue(new ParamMapping(aFinalizeAssetsLR));
      pCopyShading = getBooleanParamValue(new ParamMapping(aCopyShading));
      pCopyRigging = getBooleanParamValue(new ParamMapping(aCopyRigging));
      pGlobals = getBooleanParamValue(new ParamMapping(aGlobals));
      pMRayInit = getBooleanParamValue(new ParamMapping(aMentalRayInit));
      pPlaceholder = getBooleanParamValue(new ParamMapping(aPlaceholder));
      pAssetVerification = getBooleanParamValue(new ParamMapping(aAssetVerification));
      
      StageInformation stageInfo = getStageInformation();
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      stageInfo.setDefaultSelectionKeys(keys);
      stageInfo.setUseDefaultSelectionKeys(true);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -5273508737055512461L;
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
            "The ProjectMELBuilder Pass which actually constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      if (pCopyShading) {
	String script = pProjectNames.getShaderCopyScriptName();
	MELFileStage stage = 
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaShdCopyMEL");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pCopyRigging) {
	String script = pProjectNames.getRigCopyScriptName();
	MELFileStage stage = 
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaReRigMEL");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      {
	String script = pProjectNames.getRemoveReferenceScriptName();
	MELFileStage stage = 
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaRemoveRefMEL");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pMRayInit) {
	String script = pProjectNames.getMRayInitScriptName();
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaMRayInitMEL");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pPlaceholder) {
	String script = pProjectNames.getPlaceholderScriptName();
	{
	  boolean build = false;
	  if (!pDefaultScripts) {
	    EmptyFileStage stage = 
	      new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
	    stage.build();
	    build = stage.build();
	    if (build) {
	      pEmptyFiles.add(stage);
	      addToQueueList(script);
	    }
	  }
	  else {
	    PlaceholderMELStage stage = 
	      new PlaceholderMELStage(stageInfo, pContext, pClient, script);
	    build = stage.build();
	  }
	  if (build)
	    addToCheckInList(script);
	}
	script = pProjectNames.getPlaceholderSkelScriptName();
	{
	  boolean build = false;
	  if (!pDefaultScripts) {
	    EmptyFileStage stage = 
	      new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
	    build = stage.build();
	    if (build) {
	      pEmptyFiles.add(stage);
	      addToQueueList(script);
	    }
	  }
	  else {
	    PlaceholderSkelMELStage stage = 
	      new PlaceholderSkelMELStage(stageInfo, pContext, pClient, script);
	    build = stage.build();
	  }
	  if (build)
	    addToCheckInList(script);
	}
    }
      String types[] = {"character", "prop", "set"};
      if (pFinalizeAssets) {
	LinkedList<String> collectedScripts = new LinkedList<String>();
	if (pCopyShading)
	  collectedScripts.add(pProjectNames.getShaderCopyScriptName());
	collectedScripts.add(pProjectNames.getRemoveReferenceScriptName());
	for (String type : types) {
	  String script = pProjectNames.getFinalizeScriptName(null, type);
	  CatFilesStage stage = 
	    new CatFilesStage(stageInfo, pContext, pClient, script, "mel", collectedScripts);
	  boolean build = stage.build();
	  if (build) {
	    addToCheckInList(script);
	    addToQueueList(script);
	  }
	}
      }
      if (pFinalizeAssetsLR) {
	//LinkedList<String> collectedScripts = new LinkedList<String>();
	for (String type : types) {
	  String script = pProjectNames.getLowRezFinalizeScriptName(null, type);
	  EmptyFileStage stage = 
	    new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
	  boolean build = stage.build();
	  if (build) {
	    addToCheckInList(script);
	    addToQueueList(script);
	  }
	}
      }
      if (pAssetVerification) {
	LinkedList<String> scripts = new LinkedList<String>();
	scripts.add(pProjectNames.getShaderVerificationScriptName());
	if (!pDefaultScripts)
	  scripts.add(pProjectNames.getModelVerificationScriptName());
	scripts.add(pProjectNames.getRigVerificationScriptName());
	scripts.add(pProjectNames.getAssetVerificationScriptName());
	for (String script : scripts) {
	  EmptyFileStage stage = 
	    new EmptyFileStage(stageInfo, pContext, pClient, script, "mel");
	  boolean build = stage.build();
	  if (build) {
	    addToCheckInList(script);
	    addToQueueList(script);
	    pEmptyFiles.add(stage);
	  }
	}
	if (pDefaultScripts) {
	  String script = pProjectNames.getModelVerificationScriptName();
	  VerifyModelMELStage stage = new VerifyModelMELStage(stageInfo, pContext, pClient, script);
	  boolean build = stage.build();
	  if (build)
	   addToCheckInList(script);
	}
      }
      if (pFinalizeAssets) {
	String script = pProjectNames.getFinalRigScriptName();
	LinkedList<String> collectedScripts = new LinkedList<String>();
	if (pCopyRigging)
	  collectedScripts.add(pProjectNames.getRigCopyScriptName());
	collectedScripts.add(pProjectNames.getRemoveReferenceScriptName());
	if (pAssetVerification)
	  collectedScripts.add(pProjectNames.getRigVerificationScriptName());
	CatFilesStage stage = 
	  new CatFilesStage(stageInfo, pContext, pClient, script, "mel", collectedScripts);
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getAssetModelTTGlobals();
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaRenderGlobals");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getAssetRigAnimGlobals();
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaRenderGlobals");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Maya2MR);
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MRayRenderGlobals");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getAssetShaderTTGlobals(GlobalsType.Standalone);
	MRayOptionsStage stage =
	  new MRayOptionsStage(stageInfo, pContext, pClient, script);
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getAnimGlobals();
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MayaRenderGlobals");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
      if (pGlobals) {
	String script = pProjectNames.getLgtGlobals();
	MELFileStage stage =
	  new MELFileStage(stageInfo, pContext, pClient, script, "MRayRenderGlobals");
	boolean build = stage.build();
	if (build) {
	  addToCheckInList(script);
	  addToQueueList(script);
	}
      }
    } // End of buildPhase()
    private static final long serialVersionUID = -4520437729968377615L;
  }
  
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("Finalize Pass", 
	    "The Project MEL Builder pass that removes unnecessary actions.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      TreeSet<String> toReturn = new TreeSet<String>(getDisableList());
      for (EmptyFileStage stage : pEmptyFiles) 
	toReturn.add(stage.getNodeName());
      return toReturn;
    }

    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for (EmptyFileStage stage : pEmptyFiles)
	stage.finalizeStage();
    }
    private static final long serialVersionUID = 2805188757043354868L;
  }
}
