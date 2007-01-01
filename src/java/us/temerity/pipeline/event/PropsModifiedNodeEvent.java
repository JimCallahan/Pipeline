// $Id: PropsModifiedNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P R O P S   M O D I F I E D   N O D E    E V E N T                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the change in one or more properties of a working version of a node.
 */
public
class PropsModifiedNodeEvent
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
  PropsModifiedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  public
  PropsModifiedNodeEvent
  ( 
   NodeID nodeID
  ) 
  {
    super(NodeEventOp.PropsModified, nodeID); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2574826084559985516L;

}

