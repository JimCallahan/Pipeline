package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.stages.*;

import com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0.stages.*;

/**
 * Roto Builder
 * <P>
 * <h2> Parameters</h2>
 * <ul>
 * <li> Location - The Project, Sequence, and Shot name, or [[NEW]] to create something new. 
 * <li> Plates - which existing plates should be used to build this roto.
 * <li> NumOfMattes - how many matte passes need to be created.
 * <li> Mattes - The names of the mattes that are going to be created.
 */
public 
class RotoBuilder 
  extends TaskBuilder 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Required constructor for to launch the builder.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param info
   *   Information that is shared among all builders in a given invocation.
   */ 
  public 
  RotoBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, qclient, info, 
        new StudioDefinitions(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
        new ProjectNames(mclient, qclient),
        null);
  }
  
  /**
   * Provided to allow parent builders to create instances and share.
   * 
   * @param mclient 
   *   The master manager connection.
   * 
   * @param qclient 
   *   The queue manager connection.
   * 
   * @param info
   *   Information that is shared among all builders in a given invocation.
   * 
   * @param defs 
   *   Provides a set of studio-wide helpers for project, sequence and shot naming.
   * 
   * @param projectNamer
   *   Provides project-wide names of nodes and node directories.
   * 
   * @param shotNamer
   *   Provides the names of nodes and node directories which are shot specific.
   * 
   */ 
  public 
  RotoBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info, 
    StudioDefinitions defs,
    ProjectNames projectNamer,
    ShotNames shotNamer
  )
    throws PipelineException
  {
    super("RotoBuilder",
        "Builder to construct basic roto network.",
        mclient,
        qclient,
        info);
    
    /* initialize fields */ 
    pDefs = defs;
    pProjectNamer = projectNamer;
    pShotNamer = shotNamer;
    pFinalizeStages = new ArrayList<FinalizableStage>();

    /* setup builder parameters */ 
    {
      /* select the project, sequence and shot for the task */ 
      UtilityParam param = 
        new DoubleMapUtilityParam(
            aLocation, 
            "The Project, Sequence, and Shot to put the Roto in.",
            aProjectName,
            "Select the name of the project",
            aSequenceName,
            "Select the name of the sequence or [[NEW]] to create a new sequence",
            aShotName,
            "Select the name of the shot or [[NEW]] to create a new shot",
            pDefs.getAllProjectsAllNamesForParam());
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new PlaceholderUtilityParam(aPlates, "Which existing plates should be used.");
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new PlaceholderUtilityParam(aMattes, "What mattes should be created.");
      addParam(param);
    }
    
    {
      UtilityParam param = 
        new IntegerUtilityParam
          (aNumOfMattes, 
           "How many mattes should be created?",
           1);
      addParam(param);
    }
    
    addCheckinWhenDoneParam();
    addDoAnnotationParam();
    
    /* if no parent builder has already generated the names for ProjectNamer, 
    this builder should take over control of naming the project */ 
    if (!projectNamer.isGenerated()) {
      
      /* add the ProjectNamer as a sub-builder */  
      addSubBuilder(projectNamer);

      /* link the nested ProjectName parameter inside the complex parameter Location
      (of this builder) with the simple parameter ProjectName of the ProjectNamer */
      addMappedParam(projectNamer.getName(), 
                     new ParamMapping(ProjectNames.aProjectName), 
                     new ParamMapping(aLocation, aProjectName));
    }
    
    {
      /* create the setup passes */ 
      addSetupPass(new FirstInfoPass());
      addSetupPass(new SecondInfoPass());
      addSetupPass(new ThirdInfoPass());

      /* create the construct passes */ 
      ConstructPass first = new FirstConstructPass();
      ConstructPass second = new SecondConstructPass();
      addConstructPass(first);
      addConstructPass(second);

      /* makes the second pass dependent on the first*/ 
      addPassDependency(first, second);
    }
    
    /* setup the default editors */ 
    {
      setDefaultEditor(StageFunction.aNone, new PluginContext("WordPad"));
      setDefaultEditor(StageFunction.aAfterFXScene, new PluginContext("AfterFX"));
      setDefaultEditor(StageFunction.aTextFile, new PluginContext("WordPad"));
      setDefaultEditor(StageFunction.aScriptFile, new PluginContext("WordPad"));
      setDefaultEditor(StageFunction.aSilhouetteScene, new PluginContext("Silhouette", "TheO"));
      setDefaultEditor(StageFunction.aRenderedImage, new PluginContext("FrameCycler", "TheO"));
      setDefaultEditor(StageFunction.aSourceImage, new PluginContext("NukeViewer"));
    }

    /* specify the layout of the parameters for each pass in the UI */ 
    PassLayoutGroup finalLayout = 
      new PassLayoutGroup("Pass Layout", "Layout for all the passes");
    {
      {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup("BuilderSettings", true);
        layout.addEntry(1, aUtilContext);
        layout.addEntry(1, null);
        layout.addEntry(1, aCheckinWhenDone);
        layout.addEntry(1, aActionOnExistence);
        layout.addEntry(1, aReleaseOnError);
        layout.addEntry(1, aDoAnnotations);
        layout.addEntry(1, null);
        layout.addEntry(1, aLocation);
        layout.addEntry(1, aNumOfMattes);
        finalLayout.addPass(layout.getName(), layout);
        
      }
      
      {
        AdvancedLayoutGroup empty = new AdvancedLayoutGroup("None", true);
        finalLayout.addPass(empty.getName(), empty);
      }
      
      {
        AdvancedLayoutGroup group = new AdvancedLayoutGroup("Plates", true);
        group.addEntry(1, aPlates);
        group.addSeparator(1);
        group.addEntry(1, aMattes);
        finalLayout.addPass(group.getName(), group);
      }
      setLayout(finalLayout);
    }
  }
  
 
  
  /*----------------------------------------------------------------------------------------*/
  /*  O V E R R I D E S                                                                     */
  /*----------------------------------------------------------------------------------------*/
 
  @Override
  protected boolean 
  performCheckIn()
  {
    return pCheckInWhenDone;
  }
  
  @Override
  protected LinkedList<String> 
  getNodesToCheckIn() 
  {
    return getCheckInList();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  protected MappedArrayList<String, PluginContext> 
  getNeededActions()
  {
    ArrayList<PluginContext> plugins = new ArrayList<PluginContext>();
    plugins.add(new PluginContext("AfterFXTemplate", "TheO"));
    plugins.add(new PluginContext("SilhouetteBuild"));
    plugins.add(new PluginContext("AfterFXRenderImg"));
    plugins.add(new PluginContext("Touch"));
    plugins.add(new PluginContext("Copy"));
    
    MappedArrayList<String, PluginContext> toReturn = new MappedArrayList<String, PluginContext>();
    toReturn.put(getToolset(), plugins);
    return toReturn;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   S E T U P   P A S S E S                                                              */
  /*----------------------------------------------------------------------------------------*/

  
  protected
  class FirstInfoPass
    extends SetupPass
  {
    public 
    FirstInfoPass()
    {
      super("FirstInfoPass", 
            "The First Information pass for the RotoBuilder");
    }
    
    /**
     * Phase in which parameter values should be extracted from parameters and checked
     * for consistency and applicability.
     */
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      /* sets up the built-in parameters common to all builders */
      validateBuiltInParams();
      /* Now that we know what the correct Context is, tell the Studio Definitions. */
      pDefs.setContext(pContext);
      
      /* lookup the name of the selected project, sequence and shot from the 
          builder's Location parameter */ 
      {
        ParamMapping mapping = 
          new ParamMapping(aLocation, aProjectName);
        pProjectName = getStringParamValue(mapping);
      }
      
      {
        ParamMapping mapping = 
          new ParamMapping(aLocation, aSequenceName);
        pSequenceName = getStringParamValue(mapping);
      }
      
      {
        ParamMapping mapping = 
          new ParamMapping(aLocation, aShotName);
        pShotName = getStringParamValue(mapping);
      }
      
      /* lookup whether we should check-in the nodes we've created at the end */ 
      pCheckInWhenDone = getBooleanParamValue(new ParamMapping(aCheckinWhenDone));
      
      /* turn on the DoAnnotations flag for the StageInformation shared by all 
          of the Stages created by this builder since we always want task annotations */
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      pStageInfo.setDoAnnotations(annot);
    }
    
    /**
     * Phase in which new Sub-Builders should be created and added to the current Builder.
     */ 
    @Override
    public void 
    initPhase() 
      throws PipelineException 
    {
      /* if we haven't been passed in a ShotNamer from a parent builder, make one now */
      if (pShotNamer == null) {
        pShotNamer = new ShotNames(pClient, pQueue, pDefs);
      }
      
      /* if no parent builder has already generated the shot names... */ 
      if (!pShotNamer.isGenerated()) {
        addSubBuilder(pShotNamer);

        if (!pProjectName.equals(StudioDefinitions.aNEW))  {
          addMappedParam(pShotNamer.getName(), 
                         new ParamMapping(ShotNames.aProjectName),
                         new ParamMapping(aLocation,aProjectName));
        }
        /* if we are not creating a new sequence, 
            then link the nested SequenceName parameter inside the complex parameter 
            Location (of this builder) with the simple SequenceName parameter of the 
            ShotNamer */ 
        if (!pSequenceName.equals(StudioDefinitions.aNEW))  {
          addMappedParam(pShotNamer.getName(), 
                         new ParamMapping(ShotNames.aSequenceName),
                         new ParamMapping(aLocation, aSequenceName));
        }
        /* if we are not creating a new shot, 
            then link the nested ShotName parameter inside the complex parameter 
            Location (of this builder) with the simple ShotName parameter of the 
            ShotNamer */ 
        if (!pShotName.equals(StudioDefinitions.aNEW))  {
          addMappedParam(pShotNamer.getName(), 
                         new ParamMapping(ShotNames.aShotName),
                         new ParamMapping(aLocation, aShotName));
        }
      }
    }
    
    private static final long serialVersionUID = 2415207563183079720L;

    private String pProjectName;
    private String pSequenceName;
    private String pShotName;

  }
  
  /*----------------------------------------------------------------------------------------*/
  
  protected
  class SecondInfoPass
    extends SetupPass
  {
    public 
    SecondInfoPass()
    {
      super("SecondInfoPass", 
      "Get name info for the RotoBuilder");
    }

    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TreeSet<String> plates = new TreeSet<String>(findChildNodeNames(pShotNamer.getPlatePath()));
      if (plates.isEmpty())
        throw new PipelineException
          ("At least one plate must exist for the Roto Builder " +
           "to actually make nodes.");
      
      pPlateMapping = new TreeMap<String, String>();
      for (String plate : plates) {
        StringBuffer newString = new StringBuffer();
        for (char c : plate.toCharArray()) {
          if (Character.isLetterOrDigit(c))
            newString.append(c);
        }
        pPlateMapping.put(newString.toString(), plate);
      }
      {
        ListUtilityParam param =
          new ListUtilityParam(aPlates, "A list of plates to potentially include in the roto", new TreeSet<String>(), pPlateMapping.keySet(), null, null);
        
        replaceParam(param);
      }
      pNumMattes = getIntegerParamValue(new ParamMapping(aNumOfMattes), 
                                        new Range<Integer>(0, null));
      if (pNumMattes < 1)
        disableParam(new ParamMapping(aMattes));
      else {
        StringListUtilityParam param =
          new StringListUtilityParam(aMattes, "The name of each Matte to create", pNumMattes, null);
        replaceParam(param);
      }
    }
    private static final long serialVersionUID = 8499139999589586806L;
  }
  
  
  /*----------------------------------------------------------------------------------------*/

  
  protected
  class ThirdInfoPass
    extends SetupPass
  {
    public 
    ThirdInfoPass()
    {
      super("ThirdInfoPass", 
            "Get plate info for the RotoBuilder");
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void 
    validatePhase() 
      throws PipelineException 
    {
      ComparableTreeSet<String> plates = (ComparableTreeSet<String>) getParamValue(aPlates);
      if (plates.isEmpty())
        throw new PipelineException
          ("At least one plate must be selected for the Roto Builder " +
           "to actually make nodes.");
      pPlatePaths = new ArrayList<String>();
      for (String plate : plates) {
        pPlatePaths.add(new Path(pShotNamer.getPlatePath(), pPlateMapping.get(plate)).toString());
      }
    }
    private static final long serialVersionUID = 6265724310033372952L;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T   P A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  
  protected
  class FirstConstructPass
    extends ConstructPass
  {

    public 
    FirstConstructPass()
    {
      super("FirstConstructPass",
            "Makes the RotoBuilder nodes.");
    }
    
    @Override
    public TreeSet<String> 
    nodesDependedOn()
    {
      TreeSet<String> depends = new TreeSet<String>();
      depends.add(pProjectNamer.getRotoCompTemplateName());
      return depends;
    }
    
    @Override
    public void 
    buildPhase()
      throws PipelineException
    {
      pTaskName = pShotNamer.getTaskName();
      String taskType = "Roto";
      
      FrameRange range = null;
      
      for (String plate : pPlatePaths) {
        lockLatest(plate);
        NodeMod mod = pClient.getWorkingVersion(getAuthor(), getView(), plate);
        range = mod.getPrimarySequence().getFrameRange();
      }
      
      String rotoScene = pShotNamer.getRotoScene();
      {
        SilhouetteBuildStage stage = 
          new SilhouetteBuildStage(pStageInfo, pContext, pClient, rotoScene, "HDTV 24p (1920x1080)", pPlatePaths );
        stage.build();
        addEditAnnotation(stage, taskType);
        addToDisableList(rotoScene);
      }
      
      TreeSet<String> submitSources = new TreeSet<String>();
      if (pNumMattes == 0)
        submitSources.add(rotoScene);
      else {
        StringListUtilityParam param = (StringListUtilityParam) getParam(aMattes);
        String template = pProjectNamer.getRotoCompTemplateName();
        for (String matte : param.getStringValues()) {
          String matteRender = pShotNamer.getMatteName(matte);
          String matteTest = pShotNamer.getMatteTestCompScene(matte);
          String matteTestRender = pShotNamer.getMatteTestRenderName(matte);
          {
            BaseStage stage =
              new MatteStage(pStageInfo, pContext, pClient, 
                             matteRender, range, rotoScene);
            addPrepareAnnotation(stage, taskType);
            stage.build();
            pFinalizeStages.add((FinalizableStage) stage);
          }
          {
            BaseStage stage = 
              new MatteTestStage(pStageInfo, pContext, pClient, 
                                 matteTest, template, pPlatePaths, matteRender);
            addPrepareAnnotation(stage, taskType);
            stage.build();
          }
          {
            ArrayList<String> sources = new ArrayList<String>(pPlatePaths);
            sources.add(matteRender);
            BaseStage stage = 
              new MatteTestRenderStage(pStageInfo, pContext, pClient, 
                                       matteTestRender, range, matteTest, sources );
            addFocusAnnotation(stage, taskType);
            stage.build();
            submitSources.add(matteTestRender);
          }
        }
      }
      String submitNode = pShotNamer.getRotoSubmitName();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, submitNode, submitSources);
        addSubmitAnnotation(stage, taskType);
        stage.build();
        addToQueueList(submitNode);
        addToCheckInList(submitNode);
      }
      TreeSet<String> approveSources = new TreeSet<String>();
      if (pNumMattes > 0) {
        StringListUtilityParam param = (StringListUtilityParam) getParam(aMattes);
        for (String matte : param.getStringValues()) {
          String matteRender = pShotNamer.getMatteName(matte);
          String matteFinal = pShotNamer.getFinalMatteName(matte);
          {
            ProductStage stage = 
              new ProductStage
              (pStageInfo, pContext, pClient, 
               matteFinal, range, 4, "exr", 
               matteRender, StageFunction.aRenderedImage);
            addProductAnnotation(stage, taskType);
            stage.build();
          }
          {
            String eachApproval = pShotNamer.getRotoApprovalName(matte);
            TargetStage stage = 
              new TargetStage(pStageInfo, pContext, pClient, eachApproval, matteFinal);
            addApproveAnnotation(stage, taskType);
            stage.build();
            approveSources.add(eachApproval);
          }
        }
      }
      String rotoApprove = pShotNamer.getRotoApprovalName();
      {
        TargetStage stage = 
          new TargetStage(pStageInfo, pContext, pClient, rotoApprove, approveSources);
        addApproveAnnotation(stage, taskType);
        stage.build();
        addToQueueList(rotoApprove);
        addToCheckInList(rotoApprove);
      }
    }
    
    private static final long serialVersionUID = 2650600966182944088L;
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  protected
  class SecondConstructPass
    extends ConstructPass
  {
    public 
    SecondConstructPass()
    {
      super("SecondConstructPass",
            "Finalizes the RotoBuilder nodes.");
    }
    
    /**
     * Returns a set of nodes that have to be in a Finished queue state before the
     * buildPhase() method is called.
     */ 
    @SuppressWarnings("unused")
    @Override
    public TreeSet<String> 
    preBuildPhase()
      throws PipelineException
    {
      TreeSet<String> regenerate = new TreeSet<String>();

      regenerate.addAll(getDisableList());
      for(FinalizableStage stage : pFinalizeStages) 
        regenerate.add(stage.getNodeName());

      return regenerate;
    }
    
    /**
     * Fix all the links and temporary settings used during construction, disable the
     * appropriate actions and prepare the nodes for final check-in. 
     */ 
    @Override
    public void 
    buildPhase() 
      throws PipelineException
    {
      for (FinalizableStage stage : pFinalizeStages)
        stage.finalizeStage();
      disableActions();
    }
    private static final long serialVersionUID = -5276816389494648592L;
  }

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6680062424812172450L;
  
  public final static String aLocation = "Location";
  public final static String aProjectName = "ProjectName";
  public final static String aSequenceName = "SequenceName";
  public final static String aShotName = "ShotName";
  public final static String aPlates  = "Plates";
  public final static String aMattes  = "Mattes";
  public final static String aNumOfMattes  = "NumOfMattes";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private int pNumMattes;
  private boolean pCheckInWhenDone;
  private StudioDefinitions pDefs;
  private ShotNames pShotNamer;
  private ProjectNames pProjectNamer;
  private ArrayList<String> pPlatePaths;
  private TreeMap<String, String> pPlateMapping;
  private ArrayList<FinalizableStage> pFinalizeStages;
  
 
}
