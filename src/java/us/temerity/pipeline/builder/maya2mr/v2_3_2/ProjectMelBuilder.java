package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

public 
class ProjectMelBuilder
  extends BaseBuilder
{
  public 
  ProjectMelBuilder
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
  ProjectMelBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BaseNames projectNames,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ProjectMELBuilder",
          new VersionID("2.3.2"),
          "Temerity", 
          "The Advanced Temerity Asset Builder that works with the basic Temerity Names class.",
          mclient,
          qclient,
          builderInformation);
    
    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
      
    addSubBuilder(projectNames);
    
    // Globabl parameters
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
        new BooleanUtilityParam
        (aCheckinWhenDone,
         "Automatically check-in all the nodes when building is finished.", 
         false); 
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
    
    

  }
  
  @Override
  protected TreeSet<String> 
  getNodesToCheckIn()
  {
    return null;
  }
  
  
  protected BuildsProjectNames pProjectNames;
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aSelectionKeys = "SelectionKeys";
  public final static String aProjectName = "ProjectName";
  
  public final static String aFinalizeCharacter = "FinalizeAssets";
  public final static String aFinalizeCharacterLR = "FinalizeAssetsLR";
  public final static String aCopyShaders = "CopyShaders";
  public final static String aCopyRigging = "CopyRigging";
  public final static String aAssetGlobals = "AssetGlobals";
  public final static String aAnimGlobals = "AnimGlobals";
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;
}
