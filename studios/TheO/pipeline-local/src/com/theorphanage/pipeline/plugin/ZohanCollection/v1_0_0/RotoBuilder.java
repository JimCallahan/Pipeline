package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/**
 * Roto Builder
 * <P>
 * <h2> Parameters</h2>
 * <ul>
 * <li> Location - The Project, Sequence, and Shot name, or [[NEW]] to create something new. 
 * <li> Plates - which existing plates should be used to build this roto.
 */
public 
class RotoBuilder 
  extends TaskBuilder 
{
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
        new StudioDefinitions(mclient, qclient),
        new ProjectNames(mclient, qclient),
        null);
  }
  
  public 
  RotoBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation builderInformation, 
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
        builderInformation);
    
    pDefs = defs;
    pProjectNamer = projectNamer;
    pShotNamer = shotNamer;

    {
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
    
    addCheckinWhenDoneParam();
    addDoAnnotationParam();
    
    if (!projectNamer.isGenerated()) {
      addSubBuilder(projectNamer);
      ParamMapping mapping = 
        new ParamMapping(aLocation, aProjectName);
      addMappedParam(projectNamer.getName(), new ParamMapping(ProjectNames.aProjectName), mapping);
    }
    
    addSetupPass(new FirstInfoPass());
    addSetupPass(new SecondInfoPass());
    addSetupPass(new ThirdInfoPass());
    
    {
      AdvancedLayoutGroup layout = 
        new AdvancedLayoutGroup
          ("Builder Information", 
           "The pass where all the basic StageInformation about the shot is collected " +
           "from the user.", 
           "BuilderSettings", 
           true);
      {
        layout.addEntry(1, aUtilContext);
        layout.addEntry(1, null);
        layout.addEntry(1, aCheckinWhenDone);
        layout.addEntry(1, aActionOnExistence);
        layout.addEntry(1, aReleaseOnError);
        layout.addEntry(1, aDoAnnotations);
        layout.addEntry(1, null);
        layout.addEntry(1, aLocation);
        
      }
      
      PassLayoutGroup finalLayout = new PassLayoutGroup(layout.getName(), layout);
      {
        AdvancedLayoutGroup empty = new AdvancedLayoutGroup("None", true);
        finalLayout.addPass(empty.getName(), empty);
      }
      
      {
        AdvancedLayoutGroup group = new AdvancedLayoutGroup("Plates", true);
        group.addEntry(1, aPlates);
        finalLayout.addPass(group.getName(), group);
      }
      setLayout(finalLayout);
    }
  }
  
  
  @Override
  protected LinkedList<String> 
  getNodesToCheckIn() 
  {
    return getCheckInList();
  }
  
  protected
  class FirstInfoPass
    extends SetupPass
  {
    public 
    FirstInfoPass()
    {
      super("First Info Pass", 
            "The First Information pass for the RotoBuilder");
    }
    
    @Override
    public void 
    validatePhase()
      throws PipelineException
    {
      validateBuiltInParams();
      pDefs.setContext(pContext);
      
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
      
      boolean annot = getBooleanParamValue(new ParamMapping(aDoAnnotations));
      pStageInfo.setDoAnnotations(annot);
    }
    
    @Override
    public void 
    initPhase() 
      throws PipelineException 
    {
      if (pShotNamer == null) {
        pShotNamer = new ShotNames(pClient, pQueue, pDefs);
      }
      
      if (!pShotNamer.isGenerated()) {
        addSubBuilder(pShotNamer);

        if (!pProjectName.equals(StudioDefinitions.aNEW))  {
          addMappedParam(pShotNamer.getName(), 
                         new ParamMapping(ShotNames.aProjectName),
                         new ParamMapping(aLocation,aProjectName));
        }
        if (!pSequenceName.equals(StudioDefinitions.aNEW))  {
          addMappedParam(pShotNamer.getName(), 
                         new ParamMapping(ShotNames.aSequenceName),
                         new ParamMapping(aLocation, aSequenceName));
        }
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
  
  protected
  class SecondInfoPass
    extends SetupPass
  {
    public 
    SecondInfoPass()
    {
      super("Second Info Pass", 
      "Get name info for the RotoBuilder");
    }

    @Override
    public void 
    initPhase()
      throws PipelineException
    {
      TreeSet<String> plates = new TreeSet<String>(findChildNodeNames(pShotNamer.getPlatePath()));
      {
        ListUtilityParam param =
          new ListUtilityParam(aPlates, "A list of plates to potentially include in the roto", null, plates, null, null);
        
        replaceParam(param);
      }
    }
    private static final long serialVersionUID = 8499139999589586806L;
  }
  
  protected
  class ThirdInfoPass
    extends SetupPass
  {
    public 
    ThirdInfoPass()
    {
      super("Third Info Pass", 
            "Get plate info for the RotoBuilder");
    }
    
    @Override
    public void 
    validatePhase() 
      throws PipelineException 
    {
      ComparableTreeSet<String> plates = (ComparableTreeSet<String>) getParamValue(aPlates);
      pPlatePaths = new ArrayList<String>();
      Path plateStart = pShotNamer.getPlatePath();
      for (String plate : plates) {
        pPlatePaths.add(new Path(plateStart, plate).toString());
      }
    }
    private static final long serialVersionUID = 6265724310033372952L;
  }
  
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
      return new TreeSet<String>(pPlatePaths);
    }
    
  }

  private static final long serialVersionUID = 6680062424812172450L;
  
  public final static String aLocation = "Location";
  public final static String aProjectName = "ProjectName";
  public final static String aSequenceName = "SequenceName";
  public final static String aShotName = "ShotName";
  public final static String aPlates  = "Plates";

  
  private StudioDefinitions pDefs;
  private ShotNames pShotNamer;
  private ProjectNames pProjectNamer;
  private ArrayList<String> pPlatePaths;
  
}
