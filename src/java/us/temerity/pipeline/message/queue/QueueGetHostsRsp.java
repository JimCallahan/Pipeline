// $Id: QueueGetHostsRsp.java,v 1.5 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

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
   TreeMap<String,QueueHostInfo> hosts
  )
  { 
    super(timer);

    if(hosts == null) 
      throw new IllegalArgumentException("The hosts cannot be (null)!");
    pHosts = hosts;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHosts():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the per-host information indexed by fully resolved host name.
   */
  public TreeMap<String,QueueHostInfo>
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
  private TreeMap<String,QueueHostInfo>  pHosts;

}
  
