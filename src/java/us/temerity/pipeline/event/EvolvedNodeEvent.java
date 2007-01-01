// $Id: EvolvedNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E V O L V E D   N O D E    E V E N T                                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the changing the checked-in version upon which the working version is based 
 * without modifying the working version properties, links or associated files.
 */
public
class EvolvedNodeEvent
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
  EvolvedNodeEvent()
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   * 
   * @param vid
   *   The revision number of the checked-in version.
   */
  public
  EvolvedNodeEvent
  ( 
   NodeID nodeID, 
   VersionID vid
  ) 
  {
    super(NodeEventOp.Evolved, nodeID, vid);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8136190709652270541L;

}

