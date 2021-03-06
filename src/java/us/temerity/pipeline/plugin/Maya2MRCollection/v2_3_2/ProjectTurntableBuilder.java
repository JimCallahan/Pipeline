package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.*;
import us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2.stages.PlaceholderTTStage.TTType;
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
      new DefaultProjectNames(mclient, qclient, info),
      new DefaultBuilderAnswers(mclient, qclient, 
        UtilContext.getDefaultUtilContext(mclient), info.getLoggerName()), 
      info);
  }
  
  @SuppressWarnings("deprecation")
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
    super("ProjectTurntable",
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
	   "The name of the project to build the turntables in.", 
	   projects.get(0), 
	   projects); 
	addParam(param);
      }
      else {
	UtilityParam param = 
	  new StringUtilityParam
	  (aProjectName,
	   "The name of the project to build the turntables in.", 
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
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aCheckinWhenDone);
      layout.addEntry(1, aActionOnExistence);
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
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      
      pMakeSetups = getBooleanParamValue(new ParamMapping(aMakeSetups));
      
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      pMayaContext = (MayaContext) getParamValue(aMayaContext);
      
      StageInformation stageInfo = getStageInformation();
      
      TreeSet<String> keys = (TreeSet<String>) getParamValue(aSelectionKeys);
      stageInfo.setDefaultSelectionKeys(keys);
      stageInfo.setUseDefaultSelectionKeys(true);
      
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
      StageInformation stageInfo = getStageInformation();
      
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
	    new EmptyMayaAsciiStage(stageInfo, pContext, pClient, pMayaContext, node);
	  stage.build();
	  pEmptyMayaStages.add(stage);
	  addToCheckInList(node);
	}
      }
      else {
	String circleMel = pProjectNames.getPlaceholderTTCircleScriptName();
	{
	  PlaceholderTTStage stage = 
	    new PlaceholderTTStage(stageInfo, pContext, pClient, circleMel, TTType.Circle);
	  stage.build();
	  addToCheckInList(circleMel);
	}
	for (String circles : pCircles) {
	  AssetBuilderModelStage stage = 
	    new AssetBuilderModelStage(stageInfo, pContext, pClient, pMayaContext, circles, circleMel);
	  stage.build();
	  pModelStages.add(stage);
	  addToCheckInList(circles);
	}
	String centerMel = pProjectNames.getPlaceholderTTCenterScriptName();
	{
	  PlaceholderTTStage stage = 
	    new PlaceholderTTStage(stageInfo, pContext, pClient, centerMel, TTType.Center);
	  stage.build();
	  addToCheckInList(centerMel);
	}
	for (String centers : pCenter) {
	  AssetBuilderModelStage stage = 
	    new AssetBuilderModelStage(stageInfo, pContext, pClient, pMayaContext, centers, centerMel);
	  stage.build();
	  pModelStages.add(stage);
	  addToCheckInList(centers);
	}
      }
      String cameraMel = pProjectNames.getPlaceholderCameraScriptName();
      {
	PlaceholderCameraStage stage = 
	  new PlaceholderCameraStage(stageInfo, pContext, pClient, cameraMel);
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
    public LinkedList<String> 
    preBuildPhase()
    {
      LinkedList<String> toReturn = new LinkedList<String>();
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
      for (AssetBuilderModelStage stage : pModelStages)
        stage.finalizeStage();
      for (EmptyMayaAsciiStage stage : pEmptyMayaStages)
	stage.finalizeStage();
    }
    private static final long serialVersionUID = 1321703133564996170L;
  }
}
