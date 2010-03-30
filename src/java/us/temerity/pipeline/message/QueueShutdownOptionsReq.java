// $Id: QueueShutdownOptionsReq.java,v 1.1 2005/01/15 21:16:08 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S H U T D O W N   O P T I O N S                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to shutdown the plqueuemgr(1) daemon with additional options.
 * 
 * @see MasterMgr
 */
public
class QueueShutdownOptionsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param shutdownJobMgrs
   *   Whether to shutdown all job servers before exiting.
   */
  public
  QueueShutdownOptionsReq
  (
   boolean shutdownJobMgrs
  )
  {
    pShutdownJobMgrs = shutdownJobMgrs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to shutdown all job servers before exiting.
   */ 
  public boolean
  shutdownJobMgrs()
  {
    return pShutdownJobMgrs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4380120796784173732L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to shutdown all job servers before exiting.
   */
  private boolean pShutdownJobMgrs;

}
  
