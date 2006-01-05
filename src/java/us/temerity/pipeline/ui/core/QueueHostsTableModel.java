// $Id: QueueHostsTableModel.java,v 1.8 2006/01/05 16:54:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.text.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T S   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link QueueHost QueueHost} instances.
 */ 
public
class QueueHostsTableModel
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  QueueHostsTableModel
  (
   JQueueJobBrowserPanel parent, 
   TreeSet<String> localHostnames
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pQueueHosts = new ArrayList<QueueHost>();

      pSelectionGroups    = new TreeSet<String>();
      pSelectionSchedules = new TreeSet<String>();
      
      pEditedStatusIndices   = new TreeSet<Integer>();
      pEditedReserveIndices  = new TreeSet<Integer>();
      pEditedOrderIndices    = new TreeSet<Integer>();
      pEditedSlotsIndices    = new TreeSet<Integer>();
      pEditedGroupIndices    = new TreeSet<Integer>();  
      pEditedScheduleIndices = new TreeSet<Integer>();  

      pLocalHostnames = localHostnames;
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 11;

      {
	Class classes[] = { 
	  String.class, String.class, 
	  QueueHost.class, QueueHost.class, QueueHost.class,
	  QueueHost.class, Integer.class,
	  String.class, Integer.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Status", "OS", 
	  "System Load", "Free Memory", "Free Disk Space", 
	  "Jobs", "Slots", 
	  "Reservation", "Order", "Group", "Schedule"
	};
	pColumnNames = names;
      }

      {
	String colors[] = {
	  "", "", 
	  "Blue", "Blue", "Blue", 
	  "Green", "Green", 
	  "Purple", "Purple", "Purple", "Purple"
	};
	pColumnColorPrefix = colors; 
      }
      
      {
	String desc[] = {
	  "The current status of the job server.", 
	  "The operating system type of the job server.", 
	  "The system load of the server.", 
	  "The amount of unused system memory (in GB).", 
	  "The amount of available temporary disk space (in GB).", 
	  "The number of job running on the server.", 
	  "The maximum number of simultaneous jobs allowed to on the server.",
	  "The name of the user holding the server reservation.", 
	  "The order in which jobs are dispatched to the servers.", 
	  "The name of the selection bias group.", 
	  "The name of the selection schedule."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 120, 90, 135, 135, 135, 135, 60, 120, 90, 120, 120 };
	pColumnWidths = widths;
      }

      {
	JSimpleTableCellRenderer green = new JSimpleTableCellRenderer(JLabel.CENTER);
	green.setName("GreenTableCellRenderer");

	JSimpleTableCellRenderer purple = new JSimpleTableCellRenderer(JLabel.CENTER);
	purple.setName("PurpleTableCellRenderer");

	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JResourceSamplesTableCellRenderer
	        (JResourceSamplesTableCellRenderer.SampleType.Load), 
	  new JResourceSamplesTableCellRenderer
                (JResourceSamplesTableCellRenderer.SampleType.Memory), 
	  new JResourceSamplesTableCellRenderer
                (JResourceSamplesTableCellRenderer.SampleType.Disk), 
	  new JResourceSamplesTableCellRenderer
                (JResourceSamplesTableCellRenderer.SampleType.Jobs), 
	  green, 
	  purple, 
	  purple, 
	  purple,
 	  purple
	};

	pRenderers = renderers;
      }

      {
	JCollectionTableCellEditor status = 
	  new JCollectionTableCellEditor(QueueHost.Status.titles(), 120);

	JIntegerTableCellEditor slots = 
	  new JIntegerTableCellEditor(60, JLabel.CENTER);
	slots.setName("GreenEditableTextField");

	JIdentifierTableCellEditor reservation = 
	  new JIdentifierTableCellEditor(120, JLabel.CENTER);
	reservation.setName("PurpleEditableTextField");

	JIntegerTableCellEditor order = 
	  new JIntegerTableCellEditor(90, JLabel.CENTER);
	order.setName("PurpleEditableTextField");

	TableCellEditor editors[] = {
	  status, 
	  null,
	  null, 
	  null, 
	  null, 
	  null, 
	  slots, 
	  reservation, 
	  order
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
    for(QueueHost host : pQueueHosts) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	value = host.getStatus().toString();
	break;

      case 1:
	{
	  OsType os = host.getOsType();
	  if(os != null) 
	    value = os.toString();
	  else 
	    value = "";
	}
	break;

      case 2:
      case 3:
      case 4:
      case 5:
	{
	  ResourceSample sample = host.getLatestSample();
	  if(sample == null) {
	    switch(pSortColumn) {
	    case 2:
	      value = new Float(0.0f);
	      break;

	    case 3:
	    case 4:
	      value = new Long(0);
	      break;
	      
	    case 5:
	      value = new Integer(0);
	    }
	  }
	  else {
	    switch(pSortColumn) {
	    case 2:
	      value = new Float(sample.getLoad());
	      break;

	    case 3:
	      value = new Long(sample.getMemory());
	      break;

	    case 4:
	      value = new Long(sample.getDisk());
	      break;
	      
	    case 5:
	      value = new Integer(sample.getNumJobs());
	    }
	  }
	}
	break;

      case 6:
	value = new Integer(host.getJobSlots());
	break;
	
      case 7:
	value = host.getReservation();
	if(value == null)
	  value = "";
	break;
	
      case 8:
	value = new Integer(host.getOrder());
	break;

      case 9:
	value = host.getSelectionGroup();
	if(value == null)
	  value = "";
	break;
	
      case 10:
	value = host.getReservation();
	if(value == null)
	  value = "";
	break;

      default:
	assert(true) : ("Invalid column index (" + pSortColumn + ")!");
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
    
    pParent.sortHostnamesTable(pRowToIndex);
  }

  /**
   * Copy the row sort order from another table model with the same number of rows.
   */ 
  public void
  externalSort
  (
   int[] rowToIndex
  ) 
  {
    pRowToIndex = rowToIndex.clone();
    fireTableDataChanged();     
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   C O L U M N   V I S I B I L I T Y                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Notifies the model that columns visible to the user have changed.
   */ 
  public void 
  columnVisiblityChanged()
  {
    pParent.updateHostsHeaderButtons();
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
    case 9:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionGroups);

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }

    case 10:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionSchedules); 

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

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
   * Returns the most specific superclass for all the cell values in the column.
   */
  public Class 	
  getColumnClass
  (
   int col
  )
  {
    return pColumnClasses[col];
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return pNumColumns; 
  }

  /**
   * Returns the name of the column at columnIndex.
   */ 
  public String 	
  getColumnName
  (
   int col
  ) 
  {
    return pColumnNames[col];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   *
   * @param hosts
   *   Current job server hosts indexed by fully resolved hostname.
   * 
   * @param groups
   *   The valid selection group names. 
   * 
   * @param schedules
   *   The valid selection schedule names. 
   * 
   * @param isPrivileged
   *   Whether the current user is has privileged status.
   */ 
  public void
  setQueueHosts
  (
   TreeMap<String,QueueHost> hosts, 
   TreeSet<String> groups, 
   TreeSet<String> schedules, 
   boolean isPrivileged
  ) 
  {
    pQueueHosts.clear();
    if(hosts != null)
      pQueueHosts.addAll(hosts.values());

    pSelectionGroups.clear();
    if(groups != null) 
      pSelectionGroups.addAll(groups);

    pSelectionSchedules.clear();
    if(schedules != null) 
      pSelectionSchedules.addAll(schedules);

    pIsPrivileged = isPrivileged;
    
    pEditedStatusIndices.clear();
    pEditedReserveIndices.clear();
    pEditedOrderIndices.clear();
    pEditedSlotsIndices.clear();
    pEditedGroupIndices.clear();
    pEditedScheduleIndices.clear();

    sort();
  }

  
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the name of the host on the given row.
   */
  public String
  getHostname
  (
   int row
  ) 
  { 
    QueueHost host = pQueueHosts.get(pRowToIndex[row]);
    if(host != null) 
      return host.getName();
    return null;
  }

  /** 
   * Get the names of the hosts in the current sorted order.
   */
  public ArrayList<String>
  getHostnames() 
  { 
    ArrayList<String> names = new ArrayList<String>();

    int row;
    for(row=0; row<pQueueHosts.size(); row++) 
      names.add(pQueueHosts.get(pRowToIndex[row]).getName());

    return names;
  }

  
  /**
   * Get the changes to host state. 
   */ 
  public TreeMap<String,QueueHost.Status> 
  getHostStatus() 
  {
    TreeMap<String,QueueHost.Status> table = new TreeMap<String,QueueHost.Status>();
    for(Integer idx : pEditedStatusIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getStatus());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
  }

  /**
   * Get the changes to host user reservations. 
   */ 
  public TreeMap<String,String>
  getHostReservations() 
  {
    TreeMap<String,String> table = new TreeMap<String,String>();
    for(Integer idx : pEditedReserveIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getReservation());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
  }

  /**
   * Get the changes to host dispatch order. 
   */ 
  public TreeMap<String,Integer>
  getHostOrders() 
  {
    TreeMap<String,Integer> table = new TreeMap<String,Integer>();
    for(Integer idx : pEditedOrderIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getOrder());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
  }

  /**
   * Get the changes to host user reservations. 
   */ 
  public TreeMap<String,Integer>
  getHostSlots() 
  {
    TreeMap<String,Integer> table = new TreeMap<String,Integer>();
    for(Integer idx : pEditedSlotsIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getJobSlots());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
  }

  /**
   * Get the changes to host selection groups.
   */ 
  public TreeMap<String,String>
  getHostSelectionGroups() 
  {
    TreeMap<String,String> table = new TreeMap<String,String>();
    for(Integer idx : pEditedGroupIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getSelectionGroup());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
  }
  
  /**
   * Get the changes to host selection groups.
   */ 
  public TreeMap<String,String>
  getHostSelectionSchedules() 
  {
    TreeMap<String,String> table = new TreeMap<String,String>();
    for(Integer idx : pEditedScheduleIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) 
	table.put(host.getName(), host.getSelectionSchedule());
    }
    
    if(!table.isEmpty()) 
      return table;

    return null;
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
    return pQueueHosts.size();
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
    boolean editable = false;
    QueueHost host = pQueueHosts.get(pRowToIndex[row]);
    if(pIsPrivileged) 
      editable = true;
    else 
      editable = pLocalHostnames.contains(host.getName());
      
    switch(col) {
    case 0:
    case 6: 
    case 7: 
    case 8:
    case 10:
      return editable;
 
    case 9:
      return ((host != null) && (host.getSelectionSchedule() == null));

    default:
      return false;
    }
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
    QueueHost host = pQueueHosts.get(pRowToIndex[row]);
    switch(col) {
    case 0:
      return host.getStatus().toString();

    case 1:
      return host.getOsType();

    case 2:
    case 3:
    case 4:
    case 5:
      return host;
      
    case 6:
      return host.getJobSlots();

    case 7:
      return host.getReservation();

    case 8:
      return host.getOrder();

    case 9:
      {
	String group = host.getSelectionGroup();
	if(group == null) 
	  group = "-";
	return group;
      }

    case 10:
      {
	String sched = host.getSelectionSchedule();
	if(sched == null) 
	  sched = "-";
	return sched;
      }

    default:
      assert(true) : ("Invalid column index (" + col + ")!");
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
    String newGroup = null;
    boolean modifyGroup = false;
    if(col == 10) {
      UIMaster master = UIMaster.getInstance();
      QueueMgrClient client = master.getQueueMgrClient();
      try {
	String sname = (String) value;
	if(!sname.equals("-")) {
	  TreeMap<String,SelectionSchedule> schedules = client.getSelectionSchedules();
	  if(schedules != null) {
	    SelectionSchedule sched = schedules.get(sname);
	    if(sched != null) {
	      newGroup = sched.activeGroup(new Date());
	      modifyGroup = true;
	    }
	  }
	}
      }
      catch(PipelineException ex) {
	master.showErrorDialog(ex);
      }
    }

    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col, newGroup, modifyGroup);
    
    if(pIsPrivileged) {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  setValueAtHelper(value, srow, col, newGroup, modifyGroup);
      }
    }
      
    if(edited) {
      fireTableDataChanged();
      pParent.doHostsEdited(); 
    }
  }

  public boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col, 
   String newGroup, 
   boolean modifyGroup 
  ) 
  {
    QueueHost host = pQueueHosts.get(srow);
    switch(col) {
    case 0:
      {
	host.setStatus(QueueHost.Status.valueOf(QueueHost.Status.class, (String) value));

	pEditedStatusIndices.add(srow);
	return true;
      }

    case 6:
      {
	Integer slots = (Integer) value;
	if((slots != null) && (slots >= 0)) 
	  host.setJobSlots(slots);

	pEditedSlotsIndices.add(srow);
	return true; 
      }
      
    case 7:
      {
	String author = (String) value;
	if((author != null) && (author.length() == 0)) 
	  author = null;
	host.setReservation(author);

	pEditedReserveIndices.add(srow);
	return true;
      }

    case 8:
      {
	Integer order = (Integer) value;
	if((order != null) && (order >= 0)) 
	  host.setOrder(order);

	pEditedOrderIndices.add(srow);
	return true; 
      }

    case 9:
      {
	String group = (String) value;
	if(group.equals("-")) 
	  group = null;
	host.setSelectionGroup(group);

	pEditedGroupIndices.add(srow);
	return true; 
      }

    case 10:
      {
	String sched = (String) value;
	if(sched.equals("-")) 
	  sched = null;
	host.setSelectionSchedule(sched);

	pEditedScheduleIndices.add(srow);

	if(modifyGroup) {
	  host.setSelectionGroup(newGroup);
	  pEditedGroupIndices.add(srow);
	}

	return true; 
      }

    default:
      return false;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8796631808173092560L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent panel.
   */ 
  private JQueueJobBrowserPanel pParent;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<QueueHost> pQueueHosts;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The valid selection group names. 
   */ 
  private TreeSet<String>  pSelectionGroups; 

  /**
   * The valid selection schedule names. 
   */ 
  private TreeSet<String>  pSelectionSchedules; 



  /*----------------------------------------------------------------------------------------*/

  /**
   * The indices of hosts which have had their status edited.
   */ 
  private TreeSet<Integer>  pEditedStatusIndices; 

  /**
   * The indices of hosts which have had their user reservations edited.
   */ 
  private TreeSet<Integer>  pEditedReserveIndices; 

  /**
   * The indices of hosts which have had their dispatch order edited.
   */ 
  private TreeSet<Integer>  pEditedOrderIndices; 

  /**
   * The indices of hosts which have had their job slots edited.
   */ 
  private TreeSet<Integer>  pEditedSlotsIndices; 

  /**
   * The indices of hosts which have had their selection group edited.
   */ 
  private TreeSet<Integer>  pEditedGroupIndices; 

  /**
   * The indices of hosts which have had their selection schedule edited.
   */ 
  private TreeSet<Integer>  pEditedScheduleIndices; 



  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}
