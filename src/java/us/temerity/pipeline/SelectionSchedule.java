// $Id: SelectionSchedule.java,v 1.6 2007/11/30 20:14:24 jesse Exp $

package us.temerity.pipeline;

import java.util.LinkedList;
import java.util.Set;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   S C H E D U L E                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The schedule of automatic selection groups changes.
 */
public
class SelectionSchedule
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  SelectionSchedule()
  { 
    init();
  }

  /**
   * Construct a new selection schedule.
   * 
   * @param name
   *   The name of the schedule.
   */ 
  public
  SelectionSchedule
  (
   String name
  ) 
  {
    super(name);
    init();
  }

  /**
   * Construct a new selection schedule with the same set of rules as the given schedule.
   * 
   * @param name
   *   The name of the new schedule.
   * 
   * @param schedule
   *   Copy selection rules from this schedule.
   */ 
  public
  SelectionSchedule
  (
   String name, 
   SelectionSchedule schedule
  ) 
  {
    super(name);
    init();
    setRules(schedule.pRules);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the instance.
   */ 
  private void 
  init() 
  {
    pRules = new LinkedList<SelectionRule>();
    pRules.add(new SelectionRule());
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the selection rules.
   */ 
  public LinkedList<SelectionRule> 
  getRules() 
  {
    LinkedList<SelectionRule> rules = new LinkedList<SelectionRule>();
    for(SelectionRule rule : pRules) 
      rules.add((SelectionRule) rule.clone()); 
    return rules;
  }
  
  /**
   * Set the selection rules.
   */ 
  public void
  setRules
  (
   LinkedList<SelectionRule> rules
  ) 
  {
    pRules.clear();
    for(SelectionRule rule : rules) 
      pRules.add((SelectionRule) rule.clone()); 
  }
  
  /**
   * Add a new (lowest priority) selection rule.
   */ 
  public void
  addRule
  (
   SelectionRule rule
  ) 
  {
    pRules.add((SelectionRule) rule.clone()); 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Clear selection group specified by any rule which is not one of the following valid 
   * selection groups.
   * 
   * @param groups
   *   The names of the valid selection groups.
   * 
   * @return 
   *   Whether any rules where modified.
   */ 
  public boolean
  validateGroups
  (
   Set<String> groups
  ) 
  {
    boolean modified = false;
    for(SelectionRule rule : pRules) {
      String gname = rule.getGroup();
      if((gname != null) && !groups.contains(gname)) {
	rule.setGroup(null);
	modified = true;
      }
    }

    return modified;
  }

  /**
   * Clear selection group specified by any rule using one of the following invalidated 
   * selection groups.
   * 
   * @param groups
   *   The names of the invalid selection groups.
   * 
   * @return 
   *   Whether any rules where modified.
   */ 
  public boolean
  clearInvalidGroups
  (
   Set<String> groups
  ) 
  {
    boolean modified = false;
    for(SelectionRule rule : pRules) {
      String gname = rule.getGroup();
      if((gname != null) && groups.contains(gname)) {
	rule.setGroup(null);
	modified = true;
      }
    }

    return modified;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S C H E D U L I N G                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection rule active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * 
   * @return
   *   The name of the selection group or <CODE>null</CODE> if none is active.
   */ 
  public SelectionRule
  activeRule
  (
    long stamp  
  )
  {
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) 
	return rule;
    }
    return null;
  }
  
  /**
   * Get the name of the selection group active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * <p>
   * @return
   *   The name of the selection group or <CODE>null</CODE> if none is active.
   */ 
  public String
  activeGroup
  (
   long stamp 
  )
  {
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) 
	return rule.getGroup();
    }

    return null;
  }
  
  /**
   * Get the server Status active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * 
   * @return
   *   The server Status or <code>null</code> if none is set.
   */ 
  public QueueHostStatus
  activeServerStatus
  (
    long stamp  
  )
  {
    QueueHostStatus toReturn = null;
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) { 
	toReturn = rule.getServerStatus();
	break;
      }
    }
    return toReturn;
  }
  
  /**
   * Get the remove reservation status active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * 
   * @return
   *   The reservation status.
   */ 
  public boolean
  activeReservationStatus
  (
    long stamp  
  )
  {
    boolean toReturn = false;
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) { 
	toReturn = rule.getRemoveReservation();
	break;
      }
    }
    return toReturn;
  }
  
  /**
   * Get the order value active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * 
   * @return
   *   The order.
   */ 
  public Integer
  activeOrder
  (
    long stamp  
  )
  {
    Integer toReturn = null;
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) { 
	toReturn = rule.getOrder();
	break;
      }
    }
    return toReturn;
  }
  
  /**
   * Get the slots value active for the given point in time (milliseconds 
   * since midnight, January 1, 1970 UTC).
   * 
   * @return
   *   The number of slots.
   */ 
  public Integer
  activeSlots
  (
    long stamp  
  )
  {
    Integer toReturn = null;
    for(SelectionRule rule : pRules) {
      if(rule.isActive(stamp)) { 
	toReturn = rule.getSlots();
	break;
      }
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   E D I T   S T A T E                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the {@link EditableState} that indicates when the selection group of a host with this
   * schedule will be editable.
   */
  public EditableState
  getGroupEditState
  (
    long stamp  
  )
  {
    SelectionRule currentRule = activeRule(stamp);
    boolean active = false;
    boolean any = false;
    for(SelectionRule rule : pRules) {
      String value = rule.getGroup(); 
      if (value != null)
	any = true;
      if(currentRule == rule && value != null)
	active = true;
      if (active && any)
	return EditableState.Automatic;
    }
    if (!active && any)
      return EditableState.SemiAutomatic;
    return EditableState.Manual;
  }
  
  /**
   * Returns the {@link EditableState} that indicates when the server status of a host with this
   * schedule will be editable.
   */
  public EditableState
  getServerStatusEditState
  (
    long stamp  
  )
  {
    SelectionRule currentRule = activeRule(stamp);
    boolean active = false;
    boolean any = false;
    for(SelectionRule rule : pRules) {
      QueueHostStatus value = rule.getServerStatus();
      if (value != null)
	any = true;
      if(currentRule == rule && value != null)
	active = true;
      if (active && any)
	return EditableState.Automatic;
    }
    if (!active && any)
      return EditableState.SemiAutomatic;
    return EditableState.Manual;
  }
  
  /**
   * Returns the {@link EditableState} that indicates when the reservation status of a host with this
   * schedule will be editable.
   */
  public EditableState
  getReservationEditState
  (
    long stamp  
  )
  {
    SelectionRule currentRule = activeRule(stamp);
    boolean active = false;
    boolean any = false;
    for(SelectionRule rule : pRules) {
      boolean value = rule.getRemoveReservation();
      if (value)
	any = true;
      if(currentRule == rule && value)
	active = true;
      if (active && any)
	return EditableState.Automatic;
    }
    if (!active && any)
      return EditableState.SemiAutomatic;
    return EditableState.Manual;
  }
  
  /**
   * Returns the {@link EditableState} that indicates when the order of a host with this
   * schedule will be editable.
   */
  public EditableState
  getOrderEditState
  (
    long stamp  
  )
  {
    SelectionRule currentRule = activeRule(stamp);
    boolean active = false;
    boolean any = false;
    for(SelectionRule rule : pRules) {
      Integer value = rule.getOrder();
      if (value != null)
	any = true;
      if(currentRule == rule && value != null)
	active = true;
      if (active && any)
	return EditableState.Automatic;
    }
    if (!active && any)
      return EditableState.SemiAutomatic;
    return EditableState.Manual;
  }
  
  /**
   * Returns the {@link EditableState} that indicates when the slots of a host with this
   * schedule will be editable.
   */
  public EditableState
  getSlotsEditState
  (
    long stamp  
  )
  {
    SelectionRule currentRule = activeRule(stamp);
    boolean active = false;
    boolean any = false;
    for(SelectionRule rule : pRules) {
      Integer value = rule.getSlots();
      if (value != null)
	any = true;
      if(currentRule == rule && value != null)
	active = true;
      if (active && any)
	return EditableState.Automatic;
    }
    if (!active && any)
      return EditableState.SemiAutomatic;
    return EditableState.Manual;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 
    
    if(!pRules.isEmpty()) 
      encoder.encode("Rules", pRules);    
  }

  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    LinkedList<SelectionRule> rules = (LinkedList<SelectionRule>) decoder.decode("Rules"); 
    if(rules != null)
      pRules = rules;
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8053140768771401564L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The selection rules in order of precidence. <P> 
   * 
   * The first rule to be active during a given point in time will determine the selection
   * group for that time.  All following selection rules will be ignored, even if they are
   * also active for the time in question.
   */ 
  private LinkedList<SelectionRule> pRules; 

}
