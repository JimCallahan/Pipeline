// $Id: UnreachableServers.java,v 1.2 2009/12/12 23:12:50 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   U N R E A C H A B L E   S E R V E R S                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A table containing the timestamps of when a server has become unreachable.
 */ 
class UnreachableServers
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  UnreachableServers()
  {
    pStamps = new TreeMap<String,Long>();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Mark the given server as having become unreachable.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   */   
  public synchronized void 
  unreachable
  (
   String hostname
  ) 
  {
    pStamps.put(hostname, System.currentTimeMillis());
  }
     
  /**
   * Get the timestamp of the most recent time the given server became unreachable.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the server has never been unreachable.
   */  
  public synchronized Long
  lastUnreachable
  (
   String hostname
  ) 
  {
    return pStamps.get(hostname);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when a long transaction was started.
   */ 
  private TreeMap<String,Long>  pStamps;

}
