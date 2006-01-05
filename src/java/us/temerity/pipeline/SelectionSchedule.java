// $Id: SelectionSchedule.java,v 1.2 2006/01/05 16:54:43 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

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
   * This method should only be used by the GLUE parser to initialize the class.
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
   *   The names of the valid seleciton groups.
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
   *   The names of the invalid seleciton groups.
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
   * Get the name of the selection group active for the given point in time.
   * 
   * @return
   *   The name of the selection group or <CODE>null</CODE> if none is active.
   */ 
  public String
  activeGroup
  (
   Date date
  )
  {
    for(SelectionRule rule : pRules) {
      if(rule.isActive(date)) 
	return rule.getGroup();
    }

    return null;
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
