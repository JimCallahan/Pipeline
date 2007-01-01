// $Id: LinksModifiedNodeEvent.java,v 1.1 2007/01/01 16:09:51 jim Exp $

package us.temerity.pipeline.event;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K S   M O D I F I E D   N O D E    E V E N T                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * An event for the addition, removal or modification of the properties of existing upstream 
 * links of a working version of a node.
 */
public
class LinksModifiedNodeEvent
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
  LinksModifiedNodeEvent() 
  {}

  /** 
   * Create the event.
   * 
   * @param nodeID 
   *   The unique working version identifier. 
   */
  public
  LinksModifiedNodeEvent
  ( 
   NodeID nodeID
  ) 
  {
    super(NodeEventOp.LinksModified, nodeID); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8625143033235229708L;

}

