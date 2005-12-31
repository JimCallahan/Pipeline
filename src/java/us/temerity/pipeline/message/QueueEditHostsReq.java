// $Id: QueueEditHostsReq.java,v 1.3 2005/12/31 20:42:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   H O S T S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the editable properties of the given hosts. <P> 
 */
public
class QueueEditHostsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param status
   *   The new host status indexed by fully resolved names of the hosts.
   * 
   * @param reservations
   *   The names of reserving users indexed by fully resolved names of the hosts.
   * 
   * @param orders
   *   The server dispatch order indexed by fully resolved names of the hosts.
   * 
   * @param slots 
   *   The number of job slots indexed by fully resolved names of the hosts.
   * 
   * @param groups
   *   The names of the selection groups indexed by fully resolved names of the hosts.
   * 
   * @param schedules
   *   The names of the selection schedules indexed by fully resolved names of the hosts.
   */
  public
  QueueEditHostsReq
  (
   TreeMap<String,QueueHost.Status> status, 
   TreeMap<String,String> reservations, 
   TreeMap<String,Integer> orders, 
   TreeMap<String,Integer> slots, 
   TreeMap<String,String> groups,
   TreeMap<String,String> schedules
  )
  { 
    pStatus       = status;
    pReservations = reservations;
    pJobOrders    = orders;
    pJobSlots     = slots; 
    pGroups       = groups; 
    pSchedules    = schedules;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the new host status indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,QueueHost.Status>
  getStatus() 
  {
    return pStatus;
  }

  /**
   * Gets the names of reserving users indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,String>
  getReservations() 
  {
    return pReservations;
  }

  /**
   * Gets the server dispatch order indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,Integer>
  getJobOrders() 
  {
    return pJobOrders; 
  }

  /**
   * Gets the number of job slots indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,Integer>
  getJobSlots() 
  {
    return pJobSlots;
  }

  /**
   * Gets the names of the selection groups indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,String>
  getSelectionGroups() 
  {
    return pGroups;
  }

  /**
   * Gets the names of the selection schedules indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,String>
  getSelectionSchedules() 
  {
    return pSchedules;
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5116575566988831720L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The new host status indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,QueueHost.Status>  pStatus;

  /**
   * The names of reserving users indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,String>  pReservations;

  /**
   * The server dispatch order indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,Integer>  pJobOrders;

  /**
   * The number of job slots indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,Integer>  pJobSlots;

  /**
   * The names of the selection groups indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,String>  pGroups;

  /**
   * The names of the selection schedules indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,String>  pSchedules;

}
  
