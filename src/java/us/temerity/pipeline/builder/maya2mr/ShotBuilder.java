package us.temerity.pipeline.builder.maya2mr;

import java.util.TreeSet;

import us.temerity.pipeline.PipelineException;
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
  ShotBuilder()
    throws PipelineException
  {
    this(new DefaultBuilderAnswers(BaseUtil.getDefaultUtilContext()));
  }
  
  public 
  ShotBuilder
  (
    AnswersBuilderQueries builderInfo
  ) 
    throws PipelineException
  {
    super("ShotBuilder", 
	  "The basic Temerity Shot Builder that works with the basic Temerity Names class.");
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

}
