// $Id: CheckedInNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C H E C K E D - I N   N O D E    E V E N T                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the check-in of a working version into the repository. 
 */
public
class CheckedInNodeEvent
  extends BaseRepoNodeEvent
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  CheckedInNodeEvent()
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version created.
   * 
   * @param level  
   *   The revision number component level incremented.
   */
  public
  CheckedInNodeEvent
  ( 
   NodeID nodeID, 
   VersionID vid, 
   VersionID.Level level
  ) 
  {
    super(NodeEventOp.CheckedIn, nodeID, vid);

    if(level == null) 
      throw new IllegalArgumentException("The level cannot be (null)!");
    pLevel = level; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the revision number component level incremented.
   */
  public VersionID.Level
  getLevel() 
  {
    return pLevel;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 

    encoder.encode("Level", pLevel); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder); 

    VersionID.Level level = (VersionID.Level) decoder.decode("Level");
    if(level == null) 
      throw new GlueException("The \"Level\" was missing!");
    pLevel = level; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6076671489862187452L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The revision number component level incremented.
   */
  private VersionID.Level  pLevel; 

}

