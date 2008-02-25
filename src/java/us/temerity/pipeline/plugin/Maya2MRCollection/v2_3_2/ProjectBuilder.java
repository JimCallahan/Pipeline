package us.temerity.pipeline.plugin.Maya2MRCollection.v2_3_2;

import java.util.ArrayList;
import java.util.LinkedList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.BuilderInformation.*;
import us.temerity.pipeline.stages.EmptyFileStage;

/*------------------------------------------------------------------------------------------*/
/*   P R O J E C T   B U I L D E R                                                          */
/*------------------------------------------------------------------------------------------*/

public 
class ProjectBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public 
  ProjectBuilder
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
  ProjectBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BaseNames projectNames,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("Project",
          "The Project Builder that works with the basic Temerity Project Names class.",
          mclient,
          qclient,
          builderInformation);
    
    pBuilderInfo = builderInfo;
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
      
    {
      UtilityParam param = 
        new StringUtilityParam
        (aProjectName,
         "The name of the project to build the asset in.", 
         "projects"); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildMELScripts,
         "Run the MEL script builder.", 
         true); 
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildTurntableSetups,
         "Run the Turntable builder.", 
         true); 
      addParam(param);
    }
    
    addSelectionKeyParam();
    
    if (!projectNames.isGenerated()) {
      addSubBuilder(projectNames);
      configNamer(projectNames);
    }
    
    setDefaultEditors();
    
    pProjectNames = (BuildsProjectNames) projectNames;

    
    addSetupPass(new InformationPass());
    addConstructPass(new BuildPass());
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Project Information", 
           "The pass where all the basic stageInformation about the asset is collected " +
           "from the user.", 
           "BuilderSettings", 
           true);
      layout.addEntry(1, aUtilContext);
      layout.addEntry(1, null);
      layout.addEntry(1, aActionOnExistence);
      layout.addEntry(1, aReleaseOnError);
      layout.addEntry(1, null);
      layout.addEntry(1, aProjectName);
      layout.addEntry(1, aBuildMELScripts);
      layout.addEntry(1, aBuildTurntableSetups);
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
  
  
  protected String pProjectName;
  
  protected BuildsProjectNames pProjectNames;
  
  protected AnswersBuilderQueries pBuilderInfo;
  
  protected boolean pBuildMelScripts;
  protected boolean pBuildTurntables;
  protected boolean pBuildDummy;
  

  private static final long serialVersionUID = -2587344381236695139L;
  public static final String aProjectName = "ProjectName";
  public static final String aBuildMELScripts = "BuildMELScripts";
  public static final String aBuildTurntableSetups = "BuildTurntableSetups";
  
  
  
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
            "Information pass for the ProjectBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pBuilderInfo.setContext(pContext);
      
      pProjectName = getStringParamValue(new ParamMapping(aProjectName));
      ArrayList<String> projects = pBuilderInfo.getProjectList();
      if (projects.contains(pProjectName))
	throw new PipelineException
	  ("The project (" + pProjectName + ") already exists.  " +
	   "Cannot use the project builder to create it.");
      
      pBuildMelScripts = getBooleanParamValue(new ParamMapping(aBuildMELScripts));
      pBuildTurntables = getBooleanParamValue(new ParamMapping(aBuildTurntableSetups));
      if (pBuildMelScripts || pBuildTurntables)
	pBuildDummy = false;
      else
	pBuildDummy = true;
      
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, "Validation complete.");
    }
    
    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      pLog.log(LogMgr.Kind.Ops,LogMgr.Level.Fine, 
        "Starting the init phase in the Information Pass.");
      
      if (pBuildMelScripts) {
	ProjectScriptBuilder builder = 
	  new ProjectScriptBuilder(pClient, pQueue, (BaseNames) pProjectNames, 
	                           pBuilderInfo, getBuilderInformation());
	addSubBuilder(builder);
	addMappedParam(builder.getName(), DefaultProjectNames.aProjectName, aProjectName);
	addMappedParam(builder.getName(), aSelectionKeys, aSelectionKeys);
      }
      if (pBuildTurntables) {
	ProjectTurntableBuilder builder = 
	  new ProjectTurntableBuilder(pClient, pQueue, (BaseNames) pProjectNames, 
	                              pBuilderInfo, getBuilderInformation());
	addSubBuilder(builder);
	addMappedParam(builder.getName(), DefaultProjectNames.aProjectName, aProjectName);
	addMappedParam(builder.getName(), aSelectionKeys, aSelectionKeys);
      }
    }
    private static final long serialVersionUID = -59511509631672300L;
  } 
  
  protected 
  class BuildPass
    extends ConstructPass
  {

    public 
    BuildPass()
    {
      super("Build Pass", 
            "The ProjectBuilder Pass which actually constructs the node networks.");
    }
    
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      StageInformation stageInfo = getStageInformation();
      
      if (pBuildDummy) {
	String node = pProjectNames.getDummyFile();
	EmptyFileStage stage = new EmptyFileStage(stageInfo, pContext, pClient, node);
	stage.build();
	addToCheckInList(node);
      }
    }
    private static final long serialVersionUID = 6671865047898426873L;
  }
}
