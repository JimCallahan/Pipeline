// $Id: ReleasedNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L E A S E D   N O D E    E V E N T                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the release of the working version of a node.
 */
public
class ReleasedNodeEvent
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
  ReleasedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  public
  ReleasedNodeEvent
  ( 
   NodeID nodeID
  ) 
  {
    super(NodeEventOp.Released, nodeID); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7145692348603713205L;

}

