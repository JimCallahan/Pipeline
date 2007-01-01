// $Id: SeqsModifiedNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S E Q S   M O D I F I E D   N O D E    E V E N T                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the addition, removal or renumbering of the file sequences assoicated with 
 * a working version of a node.
 */
public
class SeqsModifiedNodeEvent
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
  SeqsModifiedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  public
  SeqsModifiedNodeEvent
  ( 
   NodeID nodeID
  ) 
  {
    super(NodeEventOp.SeqsModified, nodeID); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4639620283895132589L;

}

