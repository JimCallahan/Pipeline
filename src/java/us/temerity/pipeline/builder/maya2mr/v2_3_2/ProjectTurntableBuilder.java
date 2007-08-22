package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.*;
import us.temerity.pipeline.builder.maya2mr.v2_3_2.stages.PlaceholderTTStage.TTType;
import us.temerity.pipeline.stages.EmptyMayaAsciiStage;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   T U R N T A B L E   B U I L D E R                                      */
/*------------------------------------------------------------------------------------------*/

public 
class ProjectTurntableBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  ProjectTurntableBuilder
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
  ProjectTurntableBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BaseNames projectNames,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ProjectTurntableBuilder",
          new VersionID("2.3.2"),
          "Temerity", 
          "The Project Turntable Setup Builder that works with the basic Temerity Project Names class.",
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
    {
      UtilityParam param = 
        new MayaContextUtilityParam
        (aMayaContext,
         "The Linear, Angular, and Time Units to assign to all constructed Maya scenes.",
         new MayaContext()); 
      addParam(param);
    }
    
    addCheckinWhenDoneParam();
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aMakeSetups,
         "Should be builder create actual turntable setups.  " +
         "If this is set to (no) then empty scenes will be made", 
         true); 
      addParam(param);
    }
    
    addSelectionKeyParam();
    
    if (!projectNames.isGenerated()) {  
      addSubBuilder(projectNames);
      configNamer(projectNames);
    }
    
    pProjectNames = (BuildsProjectNames) projectNames;
    
    setDefaultEditors();
    
    addSetupPass(new InformationPass());
    ConstructPass build = new BuildPass();
    ConstructPass end = new FinalizePass();
    addConstuctPass(build);
    addConstuctPass(end);
    addPassDependency(build, end);
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic pStageInformation about the asset is collected " +
           "from the user.", 
           "BuilderSettings", 
           true);
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aCheckinWhenDone);
      layout.addEntry(1, aActionOnExistance);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, null);
      layout.addEntry(1, aProjectName);
      layout.addEntry(1, aMayaContext);
      layout.addEntry(1, aMakeSetups);
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
    setDefaultEditor(StageFunction.MayaScene.toString(), new PluginContext("MayaProject"));
    setDefaultEditor(StageFunction.None.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.TextFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.ScriptFile.toString(), new PluginContext("Emacs"));
    setDefaultEditor(StageFunction.RenderedImage.toString(), new PluginContext("Shake"));
    setDefaultEditor(StageFunction.SourceImage.toString(), new PluginContext("Gimp"));
    setDefaultEditor(StageFunction.MotionBuilderScene.toString(), null);
  }
  
  
  @Override
  protected TreeSet<String> 
  getNodesToCheckIn()
  {
    return getCheckInList();
  }
  
  @Override
  protected boolean 
  performCheckIn()
  {
    return pCheckInWhenDone;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  protected BuildsProjectNames pProjectNames;
  
  protected boolean pCheckInWhenDone;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;
  
  protected boolean pMakeSetups;
  
  // Context
  protected MayaContext pMayaContext;
  
  private ArrayList<EmptyMayaAsciiStage> pEmptyMayaStages = 
    new ArrayList<EmptyMayaAsciiStage>();
  
  private ArrayList<AssetBuilderModelStage> pModelStages =
    new ArrayList<AssetBuilderModelStage>();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aProjectName = "ProjectName";
  public final static String aMakeSetups = "MakeSetups";
  public final static String aMayaContext = "Maya";

  private static final long serialVersionUID = 4853829289454923898L;

  
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
            "Information pass for the ProjectTurntableBuilder");
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
      
      pMakeSetups = getBooleanParamValue(new ParamMapping(aMakeSetups));
      
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      pStageInfo.setDefaultSelectionKeys(keys);
      pStageInfo.setUseDefaultSelectionKeys(true);
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    private static final long serialVersionUID = -3818051689036065649L;
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
            "The ProjectTurntableBuilder Pass which actually constructs the node networks.");
    }
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the build phase in the Build Pass");
      String modSetup = pProjectNames.getAssetModelTTSetup(null, "character");
      String modSetSetup = pProjectNames.getAssetModelTTSetup(null, "set");
      String rigSetup = pProjectNames.getAssetRigAnimSetup(null, "character");
      String rigSetSetup = pProjectNames.getAssetRigAnimSetup(null, "set");
      String shdSetup = pProjectNames.getAssetShaderTTSetup(null, "character");
      String shdSetSetup = pProjectNames.getAssetShaderTTSetup(null, "set");
      TreeSet<String> pCircles = new TreeSet<String>();
      pCircles.add(modSetup);
      pCircles.add(rigSetup);
      pCircles.add(shdSetup);
      TreeSet<String> pCenter = new TreeSet<String>();
      pCenter.add(modSetSetup);
      pCenter.add(rigSetSetup);
      pCenter.add(shdSetSetup);
      if (!pMakeSetups) {
	TreeSet<String> all = new TreeSet<String>(pCircles);
	all.addAll(pCenter);
	for (String node : all ) {
	  EmptyMayaAsciiStage stage = 
	    new EmptyMayaAsciiStage(pStageInfo, pContext, pClient, pMayaContext, node);
	  stage.build();
	  pEmptyMayaStages.add(stage);
	  addToCheckInList(node);
	}
      }
      else {
	String circleMel = pProjectNames.getPlaceholderTTCircleScriptName();
	{
	  PlaceholderTTStage stage = 
	    new PlaceholderTTStage(pStageInfo, pContext, pClient, circleMel, TTType.Circle);
	  stage.build();
	  addToCheckInList(circleMel);
	}
	for (String circles : pCircles) {
	  AssetBuilderModelStage stage = 
	    new AssetBuilderModelStage(pStageInfo, pContext, pClient, pMayaContext, circles, circleMel);
	  stage.build();
	  pModelStages.add(stage);
	  addToCheckInList(circles);
	}
	String centerMel = pProjectNames.getPlaceholderTTCenterScriptName();
	{
	  PlaceholderTTStage stage = 
	    new PlaceholderTTStage(pStageInfo, pContext, pClient, centerMel, TTType.Center);
	  stage.build();
	  addToCheckInList(centerMel);
	}
	for (String centers : pCenter) {
	  AssetBuilderModelStage stage = 
	    new AssetBuilderModelStage(pStageInfo, pContext, pClient, pMayaContext, centers, centerMel);
	  stage.build();
	  pModelStages.add(stage);
	  addToCheckInList(centers);
	}
      }
      String cameraMel = pProjectNames.getPlaceholderCameraScriptName();
      {
	PlaceholderCameraStage stage = 
	  new PlaceholderCameraStage(pStageInfo, pContext, pClient, cameraMel);
	stage.build();
	addToCheckInList(cameraMel);
      }
    }
    private static final long serialVersionUID = -1362555982315848091L;
  }
  
  protected 
  class FinalizePass
    extends ConstructPass
  {
    public 
    FinalizePass()
    {
      super("Finalize Pass", 
            "The ProjectTurntableBuilder pass that disconnects placeholder MEL scripts.");
    }
    
    @Override
    public TreeSet<String> 
    preBuildPhase()
    {
      pLog.log(LogMgr.Kind.Ops, LogMgr.Level.Fine, 
        "Starting the prebuild phase in the Finalize Pass");
      TreeSet<String> toReturn = new TreeSet<String>();
      for (EmptyMayaAsciiStage stage : pEmptyMayaStages)
	toReturn.add(stage.getNodeName());
      for (AssetBuilderModelStage stage : pModelStages)
	toReturn.add(stage.getNodeName());
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
      for (EmptyMayaAsciiStage stage : pEmptyMayaStages)
	stage.finalizeStage();
    }
    private static final long serialVersionUID = 1321703133564996170L;
  }
}
