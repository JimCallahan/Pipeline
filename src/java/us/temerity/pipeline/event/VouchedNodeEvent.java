// $Id: VouchedNodeEvent.java,v 1.1 2008/05/04 00:40:18 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   V O U C H E D   N O D E    E V E N T                                                   */
/*------------------------------------------------------------------------------------------*/

/**
 * An event recording that a user vouched for the up-to-date status of the files associated 
 * with a working version of a node.
 */
public
class VouchedNodeEvent
  extends BaseWorkingNodeEvent
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
  VouchedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  public
  VouchedNodeEvent
  ( 
   NodeID nodeID
  ) 
  {
    super(NodeEventOp.Vouched, nodeID); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 159371332541637635L;

}

