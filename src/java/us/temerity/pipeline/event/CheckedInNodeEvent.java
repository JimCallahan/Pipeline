// $Id: CheckedInNodeEvent.java,v 1.2 2007/01/04 08:44:17 jim Exp $

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
   *   The revision number component level incremented or
   *   <CODE>null</CODE> for the initial revision.
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

    pLevel = level; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the revision number component level incremented or
   * <CODE>null</CODE> for the initial revision.
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

    if(pLevel != null) 
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

    pLevel = (VersionID.Level) decoder.decode("Level");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6076671489862187452L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The revision number component level incremented or 
   * <CODE>null</CODE> for the initial revision.
   */
  private VersionID.Level  pLevel; 

}

