// $Id: QueueEditHostsReq.java,v 1.5 2006/07/02 00:27:49 jim Exp $

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
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param changes
   *   The changes in host status indexed by fully resolved names of the hosts.
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
   * 
   * @param hostnames
   *   The canonical names of the host from which the request was made.
   */
  public
  QueueEditHostsReq
  (
   TreeMap<String,QueueHostStatusChange> changes, 
   TreeMap<String,String> reservations, 
   TreeMap<String,Integer> orders, 
   TreeMap<String,Integer> slots, 
   TreeMap<String,String> groups,
   TreeMap<String,String> schedules,
   TreeSet<String> hostnames
  )
  { 
    super();

    pStatusChanges = changes;
    pReservations  = reservations;
    pJobOrders     = orders;
    pJobSlots      = slots; 
    pGroups        = groups; 
    pSchedules     = schedules;

    pLocalHostnames = hostnames; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets fully resolved names of all hosts being edited.
   */
  public TreeSet<String> 
  getEditedHostnames()
  {
    TreeSet<String> hosts = new TreeSet<String>();

    if(pStatusChanges != null) 
      hosts.addAll(pStatusChanges.keySet());
    
    if(pReservations != null) 
      hosts.addAll(pReservations.keySet());

    if(pJobOrders != null)
      hosts.addAll(pJobOrders.keySet());

    if(pJobSlots != null)
      hosts.addAll(pJobSlots.keySet());

    if(pGroups != null) 
      hosts.addAll(pGroups.keySet());

    if(pSchedules != null)
      hosts.addAll(pSchedules.keySet());    

    return hosts; 
  }
  
  /**
   * Gets changes in host status changes indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,QueueHostStatusChange>
  getStatusChanges() 
  {
    return pStatusChanges;
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

  /**
   * Gets the canonical names of the host from which the request was made.
   */ 
  public TreeSet<String> 
  getLocalHostnames()
  {
    return pLocalHostnames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5116575566988831720L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The changes in host status changes indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,QueueHostStatusChange>  pStatusChanges;

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

  /**
   * The canonical names of the host from which the request was made.
   */ 
  private TreeSet<String>  pLocalHostnames;
}
  
