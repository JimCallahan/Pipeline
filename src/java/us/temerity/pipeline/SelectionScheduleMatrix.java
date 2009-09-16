// $Id: SelectionScheduleMatrix.java,v 1.2 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   S C H E D U L E   M A T R I X                                      */
/*------------------------------------------------------------------------------------------*/

/**
 *  Generates a matrix of all the value mappings of all the selection schedules at
 *  a given point in time.
 */
public 
class SelectionScheduleMatrix
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  SelectionScheduleMatrix
  (
    TreeMap<String, SelectionSchedule> schedules,
    long now
  )
  {
    pNow = now;
    pSchedules = new TreeMap<String, SelectionSchedule>(schedules);
    recompute();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   M O D I F I C A T I O N                                                              */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * @return 
   *   The time the schedules were matrixed at.
   */
  public long 
  getNow()
  {
    return pNow;
  }
  
  /**
   * @param now 
   *   The time to matrix the schedules.
   */
  public void 
  setNow
  (
    long now
  )
  {
    pNow = now;
    recompute();
  }
  
  /**
   * @return a copy of the scheduledGroups
   */
  public TreeMap<String, String> 
  getScheduledGroups()
  {
    return new TreeMap<String, String>(pScheduledGroups);
  }
  
  /**
   * @param scheduledGroups The list of schedules to matrixize.
   */
  public void 
  setScheduledGroups
  (
    TreeMap<String, String> scheduledGroups
  )
  {
    pScheduledGroups = new TreeMap<String, String>(scheduledGroups);
    recompute();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the selection group for the given schedule.
   */
  public String
  getScheduledGroup
  (
    String sname  
  )
  {
    return pScheduledGroups.get(sname);
  }
  
  /**
   * Gets the editable state of the selection group field for the given schedule.
   */
  public EditableState
  getScheduledGroupState
  (
    String sname  
  )
  {
    return pScheduledGroupsState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Gets the order for the given schedule.
   */
  public Integer
  getScheduledOrder
  (
    String sname  
  )
  {
    return pScheduledOrder.get(sname);
  }
  
  /**
   * Gets the editable state of the order field for the given schedule.
   */
  public EditableState
  getScheduledOrderState
  (
    String sname  
  )
  {
    return pScheduledOrderState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the job slots for the given schedule.
   */
  public Integer
  getScheduledSlots
  (
    String sname  
  )
  {
    return pScheduledSlots.get(sname);
  }
  
  /**
   * Gets the editable state of the job slots field for the given schedule.
   */
  public EditableState
  getScheduledSlotsState
  (
    String sname  
  )
  {
    return pScheduledSlotsState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the server status for the given schedule.
   */
  public QueueHostStatus
  getScheduledStatus
  (
    String sname  
  )
  {
    return pScheduledStatus.get(sname);
  }
  
  /**
   * Gets the editable state of the server status field for the given schedule.
   */
  public EditableState
  getScheduledStatusState
  (
    String sname  
  )
  {
    return pScheduledStatusState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the remove reservation setting for the given schedule.
   */
  public boolean
  getScheduledReservation
  (
    String sname  
  )
  {
    return pScheduledReservation.get(sname);
  }
  
  /**
   * Gets the editable state of the reservation field for the given schedule.
   */
  public EditableState
  getScheduledReservationState
  (
    String sname  
  )
  {
    return pScheduledReservationState.get(sname);
  }
  
/*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the dispatch control setting for the given schedule.
   */
  public String
  getScheduledDispatchControl
  (
    String sname  
  )
  {
    return pScheduledDispatchControl.get(sname);
  }
  
  /**
   * Gets the editable state of the dispatch control field for the given schedule.
   */
  public EditableState
  getScheduledDispatchControlState
  (
    String sname  
  )
  {
    return pScheduledDispatchControlState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the user balance group setting for the given schedule.
   */
  public String
  getScheduledUserBalance
  (
    String sname  
  )
  {
    return pScheduledUserBalance.get(sname);
  }
  
  /**
   * Gets the editable state of the user balance group field for the given schedule.
   */
  public EditableState
  getScheduledUserBalanceState
  (
    String sname  
  )
  {
    return pScheduledUserBalanceState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the favor method setting for the given schedule.
   */
  public JobGroupFavorMethod
  getScheduledFavorMethod
  (
    String sname  
  )
  {
    return pScheduledFavorMethod.get(sname);
  }
  
  /**
   * Gets the editable state of the favor method field for the given schedule.
   */
  public EditableState
  getScheduledFavorMethodState
  (
    String sname  
  )
  {
    return pScheduledFavorMethodState.get(sname);
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets a list of all the schedules contained in this matrix.
   */
  public Set<String>
  getScheduleNames()
  {
    return Collections.unmodifiableSet(pSchedules.keySet());
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R I V A T E   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/

  private void
  recompute()
  {
    pScheduledGroups               = new TreeMap<String, String>();
    pScheduledGroupsState          = new TreeMap<String, EditableState>();
    
    pScheduledOrder                = new TreeMap<String, Integer>();
    pScheduledOrderState           = new TreeMap<String, EditableState>();
    
    pScheduledSlots                = new TreeMap<String, Integer>();
    pScheduledSlotsState           = new TreeMap<String, EditableState>();
    
    pScheduledStatus               = new TreeMap<String, QueueHostStatus>();
    pScheduledStatusState          = new TreeMap<String, EditableState>();
    
    pScheduledReservation          = new TreeMap<String, Boolean>();
    pScheduledReservationState     = new TreeMap<String, EditableState>();
    
    pScheduledDispatchControl      = new TreeMap<String, String>();
    pScheduledDispatchControlState = new TreeMap<String, EditableState>();
    
    pScheduledUserBalance          = new TreeMap<String, String>();
    pScheduledUserBalanceState     = new TreeMap<String, EditableState>();
    
    pScheduledFavorMethod          = new TreeMap<String, JobGroupFavorMethod>();
    pScheduledFavorMethodState     = new TreeMap<String, EditableState>();
    
    
    for(SelectionSchedule sched : pSchedules.values()) {
      String name = sched.getName();
      pScheduledGroups.put(name, sched.activeGroup(pNow));
      pScheduledGroupsState.put(name, sched.getGroupEditState(pNow));
      pScheduledStatus.put(name, sched.activeServerStatus(pNow));
      pScheduledStatusState.put(name, sched.getServerStatusEditState(pNow));
      pScheduledReservation.put(name, sched.activeReservationStatus(pNow));
      pScheduledReservationState.put(name, sched.getReservationEditState(pNow));
      pScheduledOrder.put(name, sched.activeOrder(pNow));
      pScheduledOrderState.put(name, sched.getOrderEditState(pNow));
      pScheduledSlots.put(name, sched.activeSlots(pNow));
      pScheduledSlotsState.put(name, sched.getSlotsEditState(pNow));
      pScheduledDispatchControl.put(name, sched.activeDispatchControl(pNow));
      pScheduledDispatchControlState.put(name, sched.getDispatchControlEditState(pNow));
      pScheduledUserBalance.put(name, sched.activeUserBalance(pNow));
      pScheduledUserBalanceState.put(name, sched.getUserBalanceEditState(pNow));
      pScheduledFavorMethod.put(name, sched.activeFavorMethod(pNow));
      pScheduledFavorMethodState.put(name, sched.getFavorMethodEditState(pNow));
    }
  }

  
  private long pNow;
  
  private TreeMap<String, SelectionSchedule> pSchedules;
  
  private TreeMap<String,String> pScheduledGroups;
  private TreeMap<String, EditableState> pScheduledGroupsState;

  private TreeMap<String, QueueHostStatus> pScheduledStatus;
  private TreeMap<String, EditableState> pScheduledStatusState;
  
  private TreeMap<String, Boolean> pScheduledReservation;
  private TreeMap<String, EditableState> pScheduledReservationState;
  
  private TreeMap<String, Integer> pScheduledOrder;
  private TreeMap<String, EditableState> pScheduledOrderState;
  
  private TreeMap<String, Integer> pScheduledSlots;
  private TreeMap<String, EditableState> pScheduledSlotsState;
  
  private TreeMap<String, String> pScheduledDispatchControl;
  private TreeMap<String, EditableState> pScheduledDispatchControlState;
  
  private TreeMap<String, String> pScheduledUserBalance;
  private TreeMap<String, EditableState> pScheduledUserBalanceState;
  
  private TreeMap<String, JobGroupFavorMethod> pScheduledFavorMethod;
  private TreeMap<String, EditableState> pScheduledFavorMethodState;
}
