package us.temerity.pipeline.builder.builders;

import java.util.TreeSet;

import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.*;
import us.temerity.pipeline.builder.interfaces.AnswersBuilderQueries;
import us.temerity.pipeline.builder.names.BuildsAssetNames;
import us.temerity.pipeline.builder.names.BuildsShotNames;

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
    AnswersBuilderQueries builderInfo,
    BaseNames shotNames
  ) 
    throws PipelineException
  {
    super("ShotBuilder", 
	  "The basic Temerity Shot Builder that works with the basic Temerity Names class.");
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
  protected BuildsShotNames pAssetNames;
  
  // Question Answering
  protected AnswersBuilderQueries pBuilderInfo;

}
