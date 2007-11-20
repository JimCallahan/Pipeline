// $Id: SelectionRulesTableModel.java,v 1.3 2007/11/20 05:42:07 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   R U L E S   T A B L E   M O D E L                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link SelectionRule SelectionRule} instances.
 */ 
public
class SelectionRulesTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  SelectionRulesTableModel
  (
   JManageSelectionKeysDialog parent
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;
      
      pPrivilegeDetails = new PrivilegeDetails();    
      
      pRules = new ArrayList<SelectionRule>();
      pOrder = new ArrayList<Integer>();
      pSelectionGroups = new TreeSet<String>();
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 11;

      {
	Class classes[] = { 
	  String.class, QueueHostStatus.class, Boolean.class, Integer.class, Integer.class,
	  Integer.class, String.class, String.class, 
	  new boolean[1].getClass(), 
	  String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Group", "Status", "RemoveReserv", "NewOrder", "NewSlots", 
	  "Order", "Rule", "Date", "Weekdays", "Begins", "Ends"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The selection group used when the rule is active.",
	  "What to do with the server's status when the rule is active",
	  "Should the server's reservation be removed when the rule is active?",
	  "How to change the server's order when the rule is active",
	  "How to change the server's number of job slots when the rule is active",
	  "The order in which rules are applied.", 
	  "The type of selection rule.", 
	  "The specific date when the rule begins.", 
	  "Whether the rule is active on each of the days of the week.", 
	  "The time when the rule begins.", 
	  "The time when the rule ends (on following day if earlier than Begins)."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 
	  120, 90, 100, 80, 80, 80, 80, 120, 154, 60, 60
	};
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),   
 	  new JWeekdayFlagsTableCellRenderer(),
	  new JSimpleTableCellRenderer(JLabel.CENTER),   
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	ArrayList<String> rules = new ArrayList<String>();
	rules.add("Default");
	rules.add("Specific");
	rules.add("Daily");

	TableCellEditor editors[] = {
	  null,
	  null,
	  new JBooleanTableCellEditor(120, JLabel.CENTER),
	  new JIntegerTableCellEditor(120, JLabel.CENTER),
	  new JIntegerTableCellEditor(120, JLabel.CENTER),
	  new JIntegerTableCellEditor(120, JLabel.CENTER),
	  new JCollectionTableCellEditor(rules, 120), 
	  new JStringTableCellEditor(120, JLabel.CENTER),
 	  new JWeekdayFlagsTableCellEditor(),
	  new JStringTableCellEditor(60, JLabel.CENTER),
	  new JStringTableCellEditor(60, JLabel.CENTER)
	};
	pEditors = editors;
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  public void 
  sort()
  {
    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(SelectionRule rule : pRules) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = rule.getGroup();
	if(value == null) 
	  value = "";
	break;
	
      case 1:
	value = rule.getServerStatus();
	if (value == null)
	  value = QueueHostStatus.Hung;
	break;
	
      case 2:
	value = rule.getRemoveReservation();
	break;
	
      case 3:
	value = rule.getOrder();
	if (value == null)
	  value = -1;
	
      case 4:
	value = rule.getSlots();
	if (value == null)
	  value = -1;
	
      case 5:
	value = pOrder.get(idx);
	break;
	
      case 6:
	if(rule instanceof DailySelectionRule) 
	  value = "Daily"; 
	else if(rule instanceof SpecificSelectionRule) 
	  value = "Specific"; 
	else
	  value = "Default";
	break;
	
      case 7:
	value = "";
	if(rule instanceof SpecificSelectionRule) {
	  SpecificSelectionRule srule = (SpecificSelectionRule) rule;
	  value = srule.getStartDateString();
	}	    
	break;
	
      case 8:
	value = "";
	if(rule instanceof DailySelectionRule) {
	  DailySelectionRule drule = (DailySelectionRule) rule;
	  boolean flags[] = drule.getActiveFlags();
	  value = ((flags[0] ? "1" : "0") +
		   (flags[1] ? "1" : "0") +
		   (flags[2] ? "1" : "0") +
		   (flags[3] ? "1" : "0") +
		   (flags[4] ? "1" : "0") +
		   (flags[5] ? "1" : "0") +
		   (flags[6] ? "1" : "0"));   
	}
	break;
	
      case 9:
	value = "";
	if(rule instanceof IntervalSelectionRule) {
	  IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	  value = irule.getStartTimeString();
	}
	break;

      case 10:
	value = "";
	if(rule instanceof IntervalSelectionRule) {
	  IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	  value = irule.getEndTimeString();
	}

      default:
	assert(false);
      }
      
      int wk;
      for(wk=0; wk<values.size(); wk++) {
	if(value.compareTo(values.get(wk)) > 0) 
	  break;
      }
      values.add(wk, value);
      indices.add(wk, idx);
	
      idx++;
    }
      
    pRowToIndex = new int[indices.size()];
    int wk; 
    if(pSortAscending) {
      for(wk=0; wk<pRowToIndex.length; wk++) 
	pRowToIndex[wk] = indices.get(wk);
    }
    else {
      for(wk=0, idx=indices.size()-1; wk<pRowToIndex.length; wk++, idx--) 
	pRowToIndex[wk] = indices.get(idx);
    }

    fireTableDataChanged();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the selection rules in order of precedence.
   */
  public LinkedList<SelectionRule> 
  getSelectionRules() 
  {
    return getSelectionRules(null);
  }

  /**
   * Get the selection rules in order of precedence except the rules on the given rows.
   */
  public LinkedList<SelectionRule> 
  getSelectionRules
  (
   int exclude[]
  ) 
  {
    ArrayList<Integer> indices = new ArrayList<Integer>();
    {
      ArrayList<Integer> orders = new ArrayList<Integer>();
      int idx = 0;
      for(Integer order : pOrder) {
	int wk;
	for(wk=0; wk<orders.size(); wk++) {
	  if(order.compareTo(orders.get(wk)) < 0) 
	    break;
	}
	orders.add(wk, order);
	indices.add(wk, idx);
	
	idx++;
      }
    }

    LinkedList<SelectionRule> rules = new LinkedList<SelectionRule>();
    for(Integer idx : indices) {
      boolean skip = false;
      if(exclude != null) {
	int wk;
	for(wk=0; wk<exclude.length; wk++) {
	  if(pRowToIndex[exclude[wk]] == idx) {
	    skip = true;
	    break;
	  }
	}
      }

      if(!skip) 
	rules.add(pRules.get(idx));
    }

    return rules;
  }
  /**
   * Get the selection rule on the given row.
   */ 
  public SelectionRule
  getSelectionRule
  (
   int row
  )
  {
    return pRules.get(pRowToIndex[row]);
  }

  /**
   * Set the selection rules and valid selection group names.
   */ 
  public void
  setSelectionRules
  (
   LinkedList<SelectionRule> rules, 
   TreeSet<String> groups, 
   PrivilegeDetails privileges
  ) 
  {
    pRules.clear();
    pRules.addAll(rules);

    pOrder.clear();
    int order = 100;
    for(SelectionRule rule : pRules) {
      pOrder.add(order);
      order += 100;
    }
    
    pSelectionGroups.clear();
    pSelectionGroups.addAll(groups);

    pPrivilegeDetails = privileges; 

    sort();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the editor for the given column. 
   */ 
  public TableCellEditor
  getEditor
  (
   int col   
  )
  {
    switch(col) {
    case 0:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionGroups);

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);

	return editor;
      }
    case 1:
      {
	ArrayList<String> values = new ArrayList<String>();
	values.add("-");
	values.add(QueueHostStatus.Enabled.toString());
	values.add(QueueHostStatus.Disabled.toString());
	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(values, 120);

	return editor;
      }

    default:
      return pEditors[col];
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   T A B L E   M O D E L   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the number of rows in the model.
   */ 
  public int 
  getRowCount()
  {
    return pRules.size();
  }

  /**
   * Returns true if the cell at rowIndex and columnIndex is editable.
   */ 
  public boolean 	
  isCellEditable
  (
   int row, 
   int col
  ) 
  {
    if(!pPrivilegeDetails.isQueueAdmin()) 
      return false;
    
    SelectionRule rule = pRules.get(pRowToIndex[row]);
    switch(col) {
    case 0: 
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 6:
      return true;
      
    case 7:
      return (rule instanceof SpecificSelectionRule);
      
    case 8:
      return (rule instanceof DailySelectionRule); 
      
    case 9:
    case 10:
      return (rule instanceof IntervalSelectionRule);
    }

    return false; 
  }

  /**
   * Returns the value for the cell at columnIndex and rowIndex.
   */ 
  public Object 	
  getValueAt
  (
   int row, 
   int col
  )
  {
    int srow = pRowToIndex[row];
    SelectionRule rule = pRules.get(srow);
    Integer order = pOrder.get(srow);
    switch(col) {
    case 0:
      {
	String group = rule.getGroup();
	if(group == null) 
	  group = "-";
	return group;
      }
    
    case 1:
      {
	QueueHostStatus stat = rule.getServerStatus();
	if (stat == null)
	  return "-";
	else
	  return stat.toString();
      }
      
    case 2:
      return rule.getRemoveReservation();
	
    case 3:
      return rule.getOrder();
      
    case 4:
      return rule.getSlots();
	
    case 5:
      return pOrder.get(srow);
	
    case 6:
      if(rule instanceof DailySelectionRule) 
	return "Daily"; 
      else if(rule instanceof SpecificSelectionRule) 
	return "Specific"; 
      else
	return "Default";
	
    case 7:
      if(rule instanceof SpecificSelectionRule) {
	SpecificSelectionRule srule = (SpecificSelectionRule) rule;
	return srule.getStartDateString();
      }	    
      else {
	return null; 
      }
	
    case 8:
      if(rule instanceof DailySelectionRule) {
	DailySelectionRule drule = (DailySelectionRule) rule;
	return drule.getActiveFlags();
      }
      else {
	return null;
      }
	
    case 9:
      if(rule instanceof IntervalSelectionRule) {
	IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	return irule.getStartTimeString();
      }
      else {
	return null;
      }
      
    case 10:
      if(rule instanceof IntervalSelectionRule) {
	IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	return irule.getEndTimeString();
      }
      else {
	return null;
      }

    default:
      assert(false);
      return null;
    }    
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  public void 
  setValueAt
  (
   Object value, 
   int row, 
   int col
  ) 
  {
    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col);
    
    if(pPrivilegeDetails.isQueueAdmin()) {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  setValueAtHelper(value, srow, col);
      }
    }
      
    if(edited) {
      fireTableDataChanged();
      pParent.doEdited(); 
    }
  }

  public boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {
    SelectionRule rule = pRules.get(srow);
    switch(col) {
    case 0:
      {
	String group = (String) value;
	if(group.equals("-")) 
	  group = null;
	rule.setGroup(group);
	return true;
      }
      
    case 1:
      {
	String temp = (String) value;
	if (temp.equals("-")) {
	  rule.setServerStatus(null);
	  return true;
	}
	QueueHostStatus status = QueueHostStatus.valueOf(temp);
	rule.setServerStatus(status);
	return true;
      }
    case 2:
      {
	rule.setRemoveReservation((Boolean) value);
	return true;
      }
    
    case 3:
      {
	rule.setOrder((Integer) value);
	return true;
      }
      
    case 4:
      {
	rule.setSlots((Integer) value);
	return true;
      }
	
    case 5:
      pOrder.set(srow, (Integer) value);
      return true;
	
    case 6:
      {
	String rname = (String) value;
	if(rname.equals("Daily") && !(rule instanceof DailySelectionRule)) {
	  pRules.set(srow, new DailySelectionRule(rule));
	  return true;
	}
	else if(rname.equals("Specific") && !(rule instanceof SpecificSelectionRule)) {
	  pRules.set(srow, new SpecificSelectionRule(rule));
	  return true;
	}
	else if(rname.equals("Default") && 
		((rule instanceof SpecificSelectionRule) || 
		 (rule instanceof DailySelectionRule))) {
	  pRules.set(srow, new SelectionRule(rule));
	  return true;
	}
      }
	
    case 7:
      if(rule instanceof SpecificSelectionRule) {
	SpecificSelectionRule srule = (SpecificSelectionRule) rule;
	String str = (String) value;
	String parts[] = str.split("-"); 
	if(parts.length == 3) {
	  try {
	    srule.setStartDate(Integer.parseInt(parts[0]), 
			       Integer.parseInt(parts[1])-1, 
			       Integer.parseInt(parts[2]));
	    return true;
	  }
	  catch(Exception ex) {
	  }
	}
      }
      break;
	
    case 8:
      if(rule instanceof DailySelectionRule) {
	DailySelectionRule drule = (DailySelectionRule) rule;
	boolean[] flags = (boolean[]) value;
	if(flags != null) {
	  drule.setActiveFlags(flags);
	  return true;
	}
      }
      break;

    case 9:
      if(rule instanceof IntervalSelectionRule) {
	IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	String str = (String) value;
	String parts[] = str.split("\\:"); 
	if(parts.length == 2) {
	  try {
	    irule.setStartTime(Integer.parseInt(parts[0]), 
			       Integer.parseInt(parts[1]));
	    return true;
	  }
	  catch(Exception ex) {
	  }
	}
      }
      break;
      
    case 10:
      if(rule instanceof IntervalSelectionRule) {
	IntervalSelectionRule irule = (IntervalSelectionRule) rule;
	String str = (String) value;
	String parts[] = str.split("\\:"); 
	if(parts.length == 2) {
	  try {
	    irule.setEndTime(Integer.parseInt(parts[0]), 
			     Integer.parseInt(parts[1]));
	    return true;
	  }
	  catch(Exception ex) {
	  }
	}
      }
      break;

    default:
      assert(false);
    }
    
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2539851335329633322L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The underlying selection rules.
   */ 
  private ArrayList<SelectionRule>  pRules; 

  /**
   * The order that rules should be applied.
   */ 
  private ArrayList<Integer>  pOrder; 

  /**
   * The valid selection group names. 
   */ 
  private TreeSet<String>  pSelectionGroups; 

  /**
   * The parent dialog.
   */ 
  private JManageSelectionKeysDialog  pParent;

}
