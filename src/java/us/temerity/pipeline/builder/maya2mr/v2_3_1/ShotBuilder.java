package us.temerity.pipeline.builder.maya2mr.v2_3_1;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O T   B U I L D E R                                                                */
/*------------------------------------------------------------------------------------------*/

public 
class ShotBuilder
  extends BaseBuilder
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
    this(mclient, qclient, new DefaultBuilderAnswers(mclient, qclient, UtilContext.getDefaultUtilContext(mclient)),
         info);
  }
  
  public 
  ShotBuilder
  (
    MasterMgrClient mclient,
    QueueMgrClient qclient,
    AnswersBuilderQueries builderInfo,
    BuilderInformation builderInformation
  ) 
    throws PipelineException
  {
    super("ShotBuilder", 
      	  new VersionID("2.3.1"), 
      	  "Temerity",
	  "The basic Temerity Shot Builder that works with the basic Temerity Names class.",
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

  private static final long serialVersionUID = -4118587763338751379L;

}
