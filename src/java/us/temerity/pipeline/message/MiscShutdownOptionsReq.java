// $Id: MiscShutdownOptionsReq.java,v 1.1 2005/01/15 21:16:08 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S H U T D O W N   O P T I O N S                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to shutdown the plmaster(1) daemon with additional options.
 * 
 * @see MasterMgr
 */
public
class MiscShutdownOptionsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param shutdownJobMgrs
   *   Whether to command the queue manager to shutdown all job servers before exiting.
   * 
   * @param shutdownPluginMgr
   *   Whether to shutdown the plugin manager before exiting.
   */
  public
  MiscShutdownOptionsReq
  (
   boolean shutdownJobMgrs, 
   boolean shutdownPluginMgr
  )
  {
    pShutdownJobMgrs   = shutdownJobMgrs;
    pShutdownPluginMgr = shutdownPluginMgr;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to command the queue manager to shutdown all job servers before exiting.
   */ 
  public boolean
  shutdownJobMgrs()
  {
    return pShutdownJobMgrs;
  }

  /**
   * Whether to shutdown the plugin manager before exiting.
   */ 
  public boolean
  shutdownPluginMgr()
  {
    return pShutdownPluginMgr;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5116811990770989573L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether to command the queue manager to shutdown all job servers before exiting.
   */
  private boolean pShutdownJobMgrs;

  /**
   * Whether to shutdown the plugin manager before exiting.
   */
  private boolean pShutdownPluginMgr;

}
  
