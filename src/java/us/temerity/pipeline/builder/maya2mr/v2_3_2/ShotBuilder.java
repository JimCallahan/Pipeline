package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

public 
class ShotBuilder
  extends TaskBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, 
         qclient, 
         new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         new DefaultProjectNames(mclient, qclient),
         info);
  }
  
  public 
  ShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderQueries,
    BaseNames projectNames,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ShotBuilder", 
      	  new VersionID("2.3.2"), 
      	  "Temerity",
	  "The basic Temerity Shot Builder that works with the basic Temerity Names class.",
	  mclient,
	  qclient,
	  builderInformation);
    pBuilderQueries = builderQueries;

    if (!(projectNames instanceof BuildsProjectNames))
      throw new PipelineException
        ("The project naming class that was passed in does not implement " +
         "the BuildsProjectNames interface");
    
    // Global parameters
    {
      ArrayList<String> projects = pBuilderQueries.getProjectList();
      UtilityParam param = 
        new OptionalEnumUtilityParam
        (aProjectName,
         "The name of the project to build the shot in.", 
         projects.get(0), 
         projects); 
      addParam(param);
    }
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aNewSequence,
         "Are you building a new sequence or creating a shot in an existing sequence.", 
         true); 
      addParam(param);
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
    addSelectionKeyParam();
    addDoAnnotationParam();
    
    {
      UtilityParam param = 
        new BooleanUtilityParam
        (aBuildThumbnails, 
         "Are Thumbnail nodes needed.", 
         true); 
      addParam(param);
    }
  }
  
  @Override
  protected TreeSet<String> getNodesToCheckIn()
  {
    return null;
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/
  
  // Context
  protected MayaContext pMayaContext;

  // Names
  protected BuildsShotNames pShotNames;
  protected BuildsProjectNames pProjectNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderQueries;
  
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  public final static String aProjectName = "ProjectName";
  public final static String aNewSequence = "NewSequence";
  public final static String aBuildThumbnails = "BuildThumbnails";

  public final static String aStartFrame = "StartFrame";
  public final static String aEndFrame = "EndFrame";
  
  public final static String aAnimFormat = "AnimFormat";
  public final static String aExternalAnimOnly = "ExternalAnimOnly";
  
  public final static String aBuildLayout  = "BuildLayout";
  public final static String aBuildTestImages = "BuildTestImages";
  public final static String aBuildAnimImages = "BuildAnimImages";
  
  public final static String aChars = "Chars";
  public final static String aProps = "Props";
  public final static String aSets = "Sets";

  private static final long serialVersionUID = -4118587763338751379L;

}
