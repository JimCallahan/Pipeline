package us.temerity.pipeline.builder.maya2mr.v2_3_2;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S I C   S H O T   B U I L D E R                                                    */
/*------------------------------------------------------------------------------------------*/

public 
class BasicShotBuilder
  extends BaseBuilder
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  BasicShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    BuilderInformation info
  )
    throws PipelineException
  {
    this(mclient, qclient, new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         info);
  }
  
  public 
  BasicShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("BasicShotBuilder", 
          new VersionID("2.1.3"),
          "Temerity", 
	  "A simple Temerity Shot Builder that works with the basic Temerity Names class.",
	  mclient,
	  qclient,
	  builderInformation);
    pBuilderInfo = builderInfo;
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
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public final static String aMayaContext = "Maya";
  
  public final static String aCheckinWhenDone = "CheckinWhenDone";
  public final static String aProjectName = "ProjectName";
  public final static String aStartFrame = "StartFrame";
  public final static String aEndFrame = "EndFrame";
  
  public final static String aBuildLightingScene = "BuildLightingScene";
  public final static String aBuildImages = "BuildImages";
  
  public final static String aCharacters = "Characters";
  public final static String aProps = "Props";
  public final static String aSets = "Sets";
  
  public final static String aSelectionKeys = "SelectionKeys";
  
  private static final long serialVersionUID = 2017077506125779699L;

}
