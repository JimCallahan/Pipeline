// $Id: QueueHostStatus.java,v 1.1 2006/07/02 00:27:49 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T   S T A T U S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The operational status of a job server host.
 */
public
enum QueueHostStatus
{  
  /**
   * Indicates that the <B>pljobmgr</B>(1) daemon is in the process of being enabled.
   */
  Enabling, 

  /**
   * A <B>pljobmgr</B>(1) daemon is currently running on the host and is available to 
   * run new jobs which meet the selection criteria for the host.
   */ 
  Enabled, 


  /**
   * Indicates that the <B>pljobmgr</B>(1) daemon is in the process of being disabled.
   */
  Disabling, 

  /**
   * A <B>pljobmgr</B>(1) daemon is currently running on the host, but the host has 
   * been temporarily disabled. <P> 
   * 
   * Jobs previously assigned to the host may continue running until they complete, but no 
   * new jobs will be assigned to this host.  The host will respond to requests to kill 
   * jobs currently running on the host. 
   */ 
  Disabled, 


  /**
   * Indicates that a shutdown of the <B>pljobmgr</B>(1) daemon is underway.
   */
  Terminating, 

  /**
   * No <B>pljobmgr</B>(1) daemon is currently running on the host.  <P> 
   * 
   * No jobs will be assigned to this host until the <B>pljobmgr</B>(1) daemon is restarted.
   */ 
  Shutdown,


  /*----------------------------------------------------------------------------------------*/

  /**
   * A <B>pljobmgr</B>(1) daemon is currently running on the host, but is not responding
   * to network connections from clients. <P> 
   * 
   * This is probably an indication that something has gone wrong with the daemon or the 
   * host running the daemon.  No jobs will be assigned to this host while it is in this 
   * state.
   */ 
  Hung;



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the list of all possible states.
   */ 
  public static ArrayList<QueueHostStatus> 
  all() 
  {
    QueueHostStatus values[] = values();
    ArrayList<QueueHostStatus> all = new ArrayList<QueueHostStatus>(values.length);
    int wk;
    for(wk=0; wk<values.length; wk++)
      all.add(values[wk]);
    return all;
  }

  /**
   * Get the list of human friendly string representation for all possible values.
   */ 
  public static ArrayList<String>
  titles() 
  {
    ArrayList<String> titles = new ArrayList<String>();
    for(QueueHostStatus status : QueueHostStatus.all()) 
      titles.add(status.toTitle());
    return titles;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert to a more human friendly string representation.
   */ 
  public String
  toTitle() 
  {
    return toString();
  }


}
