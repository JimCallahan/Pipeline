// $Id: QueueGetHostsRsp.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T S   R S P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the current state of the hosts capable of executing jobs for the Pipeline queue.
 */
public
class QueueGetHostsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param hosts
   *   The per-host information indexed by fully resolved host name.
   */ 
  public
  QueueGetHostsRsp
  (
   TaskTimer timer, 
   TreeMap<String,QueueHost> hosts
  )
  { 
    super(timer);

    if(hosts == null) 
      throw new IllegalArgumentException("The hosts cannot be (null)!");
    pHosts = hosts;

    Logs.net.finest("QueueMgr.getHosts():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the per-host information indexed by fully resolved host name.
   */
  public TreeMap<String,QueueHost>
  getHosts() 
  {
    return pHosts;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -983824158769415406L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-host information indexed by fully resolved host name.
   */ 
  private TreeMap<String,QueueHost>  pHosts;

}
  
