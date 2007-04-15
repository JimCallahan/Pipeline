// $Id: QueueNodeJobsReq.java,v 1.1 2007/04/15 20:27:07 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   N O D E   J O B S   R E Q                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to perform an operation all jobs associated with the given working version.<P> 
 */
public
class QueueNodeJobsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   */
  public
  QueueNodeJobsReq
  (
   NodeID nodeID
  )
  { 
    super();

    if(nodeID == null) 
      throw new IllegalArgumentException("The node IDs cannot be (null)!");
    pNodeID = nodeID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique working version identifier. 
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 659479051550245241L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier. 
   */ 
  private NodeID pNodeID;

}

  
