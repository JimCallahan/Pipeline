package us.temerity.pipeline.builder.maya2mr;

import java.util.TreeSet;

import us.temerity.pipeline.PipelineException;
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
  BasicShotBuilder()
    throws PipelineException
  {
    this(new DefaultBuilderAnswers(BaseUtil.getDefaultUtilContext()));
  }
  
  public 
  BasicShotBuilder
  (
    AnswersBuilderQueries builderInfo
  ) 
    throws PipelineException
  {
    super("BasicShotBuilder", 
	  "A simple Temerity Shot Builder that works with the basic Temerity Names class.");
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
  

}
