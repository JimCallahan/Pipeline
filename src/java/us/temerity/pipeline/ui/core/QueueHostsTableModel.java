// $Id: QueueHostsTableModel.java,v 1.21 2007/12/15 07:51:30 jesse Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;

import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T S   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link QueueHostInfo QueueHostInfo} instances.
 * 
 * <ol "start = 0">
 * <li> Status
 * <li> OS
 * <li> System Load
 * <li> Free Memory
 * <li> Free Disk Space
 * <li> Jobs
 * <li> Slots
 * <li> Reservation
 * <li> Order
 * <li> Group
 * <li> Schedule
 * <li> Hardware Group
 * </ol>
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
   JQueueJobServersPanel parent, 
   TreeSet<String> localHostnames
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;

      pPrivilegeDetails = new PrivilegeDetails();  
      pQueueHosts = new ArrayList<QueueHostInfo>();
      pQueueHostStatusChanges = new ArrayList<QueueHostStatusChange>();

      pSamples = new TreeMap<String,ResourceSampleCache>();

      pWorkGroups = new TreeSet<String>();
      pWorkUsers  = new TreeSet<String>();
      
      pSelectionGroups    = new TreeSet<String>();
      pSelectionSchedules = new TreeSet<String>();
      pHardwareGroups     = new TreeSet<String>();
      
      pEditedStatusIndices   = new TreeSet<Integer>();
      pEditedReserveIndices  = new TreeSet<Integer>();
      pEditedOrderIndices    = new TreeSet<Integer>();
      pEditedSlotsIndices    = new TreeSet<Integer>();
      pEditedGroupIndices    = new TreeSet<Integer>();  
      pEditedScheduleIndices = new TreeSet<Integer>();  
      pEditedHardwareIndices = new TreeSet<Integer>();

      pLocalHostnames = localHostnames;
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 12;

      {
	Class classes[] = { 
	  String.class, String.class, 
	  ResourceSampleCache.class, ResourceSampleCache.class, ResourceSampleCache.class,
	  ResourceSampleCache.class, Integer.class,
	  String.class, Integer.class, String.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Status", "OS", 
	  "System Load", "Free Memory", "Free Disk Space", 
	  "Jobs", "Slots", 
	  "Reservation", "Order", "Group", "Schedule", "Hardware"
	};
	pColumnNames = names;
      }

      {
	String colors[] = {
	  "", "", 
	  "Blue", "Blue", "Blue", 
	  "Green", "Green", 
	  "Purple", "Purple", "Purple", "Purple", "Purple"
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
	  "The name of the selection schedule.",
	  "The name of the hardware value group."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 120, 90, 135, 135, 135, 135, 70, 120, 90, 120, 120, 120 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JQHostStatusTableCellRenderer(this), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JResourceSamplesTableCellRenderer
	        (this, JResourceSamplesTableCellRenderer.SampleType.Load), 
	  new JResourceSamplesTableCellRenderer
                (this, JResourceSamplesTableCellRenderer.SampleType.Memory), 
	  new JResourceSamplesTableCellRenderer
                (this, JResourceSamplesTableCellRenderer.SampleType.Disk), 
	  new JResourceSamplesTableCellRenderer
                (this, JResourceSamplesTableCellRenderer.SampleType.Jobs), 
	  new JQHostSlotsTableCellRenderer(this), 
	  new JQHostReservationTableCellRenderer(this), 
	  new JQHostOrderTableCellRenderer(this), 
	  new JQHostSGroupTableCellRenderer(this), 
	  new JQHostSSchedTableCellRenderer(this),
	  new JQHostHGroupTableCellRenderer(this),
	};

	pRenderers = renderers;
      }

      {
	JStatusTableCellEditor status = 
	  new JStatusTableCellEditor(this, 120);

	JIntegerTableCellEditor slots = 
	  new JIntegerTableCellEditor(70, JLabel.CENTER);
	slots.setName("GreenEditableTextField");

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
	  null, 
	  order
	};
	pEditors = editors;
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of the schedule that is current assigned to a row, including the current 
   * value if it is in the process of being edited (even if the edit has not been applied).
   */
  public String
  getCurrentScheduleName
  (
    int row  
  )
  {
    QueueHostInfo qinfo = getHostInfo(row);
    String toReturn = qinfo.getSelectionSchedule();
    if (pEditedScheduleIndices.contains(row)) {
      toReturn = (String) getValueAt(row, 10);
    }
    return toReturn;
  }
  
  /**
   * Get a matrix of all the values for all the {@link SelectionSchedule SelectionSchedules}
   * at the time of the last update.
   */
  public SelectionScheduleMatrix
  getSelectionScheduleMatrix()
  {
    return pParent.getSelectionScheduleMatrix();
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
    for(QueueHostInfo host : pQueueHosts) {
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
	{
	  QueueHostStatusChange change = pQueueHostStatusChanges.get(idx);
	  if(change != null) 
	    value = change.toString();
	  else 
	    value = host.getStatus().toString();
	}
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
	else if(pWorkGroups.contains(value)) 
	  value = ("[" + value + "]");
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
	value = host.getSelectionSchedule();
	if(value == null)
	  value = "";
	break;
	
      case 11:
	value = host.getHardwareGroup();
	if (value == null)
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
    case 7:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	for(String group : pWorkGroups) 
	  choices.add("[" + group + "]");
	choices.addAll(pWorkUsers); 

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }

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
      
    case 11:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pHardwareGroups);

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
   * @param samples
   *   The latest resource samples indexed by fully resolved hostname.
   * 
   * @param workGroups
   *   The names of the user work groups.
   * 
   * @param workUsers
   *   The names of the work group members.
   * 
   * @param groups
   *   The valid selection group names. 
   * 
   * @param schedules
   *   The valid selection schedule names. 
   * 
   * @param privileges
   *   The details of the administrative privileges granted to the current user. 
   */ 
  public void
  setQueueHosts
  (
   TreeMap<String,QueueHostInfo> hosts, 
   TreeMap<String,ResourceSampleCache> samples, 
   Set<String> workGroups, 
   Set<String> workUsers,
   TreeSet<String> selectionGroups, 
   TreeSet<String> selectionSchedules, 
   TreeSet<String> hardwareGroups,
   PrivilegeDetails privileges
  ) 
  {
    pQueueHosts.clear();
    if(hosts != null)
      pQueueHosts.addAll(hosts.values());

    if(samples != null) {
      for(String hname : samples.keySet()) {
	ResourceSampleCache ncache = samples.get(hname);
	if(ncache != null) {
	  ResourceSampleCache ocache = pSamples.get(hname); 
	  if(ocache == null) {
	    ocache = new ResourceSampleCache(sCacheSize);
	    pSamples.put(hname, ocache);
	  }
	  
	  ocache.addAllSamples(ncache);
	  
	  Long latest = ocache.getLastTimeStamp(); 
	  if(latest != null) 
	    ocache.pruneSamplesBefore(latest - sCacheInterval);
	}
      }
    }

    pQueueHostStatusChanges.clear();
    for(String hname : hosts.keySet()) 
      pQueueHostStatusChanges.add(null);

    pWorkGroups.clear();
    if(workGroups != null) 
      pWorkGroups.addAll(workGroups);

    pWorkUsers.clear();
    if(workUsers != null) 
      pWorkUsers.addAll(workUsers);

    pSelectionGroups.clear();
    if(selectionGroups != null) 
      pSelectionGroups.addAll(selectionGroups);
    
    pHardwareGroups.clear();
    if(hardwareGroups != null) 
      pHardwareGroups.addAll(hardwareGroups);

    pSelectionSchedules.clear();
    if(selectionSchedules != null) 
      pSelectionSchedules.addAll(selectionSchedules);

    pPrivilegeDetails = privileges; 
    
    pEditedStatusIndices.clear();
    pEditedReserveIndices.clear();
    pEditedOrderIndices.clear();
    pEditedSlotsIndices.clear();
    pEditedGroupIndices.clear();
    pEditedHardwareIndices.clear();
    pEditedScheduleIndices.clear();

    sort();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Whether changes are pending for host on the given row. 
   */
  public boolean 
  isHostPending
  (
   int row
  ) 
  { 
    int srow = pRowToIndex[row];

    QueueHostInfo qinfo = pQueueHosts.get(srow);
    if((qinfo != null) && qinfo.isPending()) 
      return true;

    return (pQueueHostStatusChanges.get(srow) != null);
  }

  /** 
   * Whether status changes are pending for host on the given row. 
   */
  public boolean 
  isHostStatusPending
  (
   int row
  ) 
  { 
    int srow = pRowToIndex[row];

    QueueHostInfo qinfo = pQueueHosts.get(srow);
    if((qinfo != null) && qinfo.isStatusPending()) 
      return true;

    return (pQueueHostStatusChanges.get(srow) != null);
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
    QueueHostInfo host = pQueueHosts.get(pRowToIndex[row]);
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
   * Get the host info for the given row.
   */
  public QueueHostInfo
  getHostInfo
  (
   int row
  ) 
  { 
    return pQueueHosts.get(pRowToIndex[row]);
  }
  
  /**
   * Is the host editable by the current user?
   * <p>
   * This method is meant to be used by Renderers that need to color themselves
   * based on the editable state of a host.
   * 
   * @param hostName
   *   The name of the host.
   */
  public boolean
  isHostEditable
  (
    String hostName  
  )
  {
    if(pPrivilegeDetails.isQueueAdmin()) 
      return true;
    return pLocalHostnames.contains(hostName);
  }
  
 
 
 /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the changes to host state or other properties.
   */ 
  public TreeMap<String,QueueHostMod> 
  getHostChanges() 
  {
    TreeMap<String,QueueHostMod> table = new TreeMap<String,QueueHostMod>();
    
    int idx; 
    for(idx=0; idx<pQueueHosts.size(); idx++) {
      QueueHostInfo host = pQueueHosts.get(idx);
      if(host != null) {
	QueueHostStatusChange change = pQueueHostStatusChanges.get(idx);
	
	String reservation = null;
	boolean reservationModified = false;
	if(pEditedReserveIndices.contains(idx)) {
	  reservation = host.getReservation();
	  reservationModified = true;
	}
	
	Integer order = null;
	if(pEditedOrderIndices.contains(idx)) 
	  order = host.getOrder();
	
	Integer slots = null;
	if(pEditedSlotsIndices.contains(idx)) 
	  slots = host.getJobSlots();
	
	String group = null;
	boolean groupModified = false;
	if(pEditedGroupIndices.contains(idx)) {
	  group = host.getSelectionGroup(); 
	  groupModified = true;
	}
	
	String hardware= null;
	boolean hardwareModified = false;
	if(pEditedHardwareIndices.contains(idx)) {
	  hardware = host.getHardwareGroup(); 
	  hardwareModified = true;
	}

	String schedule = null;
	boolean scheduleModified = false;
	if(pEditedScheduleIndices.contains(idx)) {
	  schedule = host.getSelectionSchedule(); 
	  scheduleModified = true;
	}

	if((change != null) || reservationModified || 
	   (order != null) || (slots != null) ||
	   groupModified || scheduleModified || hardwareModified) {

	  QueueHostMod qmod = 
	    new QueueHostMod(change, reservation, reservationModified, order, slots, 
			     group, groupModified, schedule, scheduleModified,
			     hardware, hardwareModified);

	  table.put(host.getName(), qmod);	
	}
      }
    }

    if(!table.isEmpty()) 
      return table;

    return null;
  }



  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the intervals of time not currently included in the resource samples cache.
   */ 
  public TreeMap<String,TimeInterval>
  getSampleIntervals() 
  {
    long now    = System.currentTimeMillis();
    long oldest = now - sCacheInterval;
    
    TreeMap<String,TimeInterval> intervals = new TreeMap<String,TimeInterval>();

    for(String hname : pSamples.keySet()) {   
      ResourceSampleCache cache = pSamples.get(hname); 
      if((cache != null) && (cache.getLastTimeStamp() != null)) {
	long latest = cache.getLastTimeStamp();
	if(latest > oldest) 
	  intervals.put(hname, new TimeInterval(latest, now)); 
      }
    }
      
    return intervals;
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
    QueueHostInfo host = pQueueHosts.get(pRowToIndex[row]);
    boolean editable = isHostEditable(host.getName());
      
    switch(col) {
    /* Slots */
    case 6:
      return (editable && 
	      ((host != null) && (host.getSlotsState() != EditableState.Automatic)));
    /* Order */
    case 8:
      return (editable && 
	      ((host != null) && (host.getOrderState() != EditableState.Automatic)));
    /* Status */
    case 0:
    /* Schedule */
    case 10:
    /* Hardware Group */ 
    case 11:
      return editable;

    /* Reservation */
    case 7:
      return (editable && 
	      ((host != null) && (host.getReservationState() != EditableState.Automatic)));
    /* Selection Group */
    case 9:
      return (editable && 
	      ((host != null) && (host.getGroupState() != EditableState.Automatic)));
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
    int srow = pRowToIndex[row];
    QueueHostInfo host = pQueueHosts.get(srow);
    switch(col) {
    case 0:
      {
	QueueHostStatusChange change = pQueueHostStatusChanges.get(srow);
	if(change != null) 
	  return change.toString();
	else 
	  return host.getStatus().toString();
      }

    case 1:
      return host.getOsType();

    case 2:
    case 3:
    case 4:
    case 5:
      return pSamples.get(host.getName());
      
    case 6:
      return host.getJobSlots();

    case 7:
      {
	String res = host.getReservation();
	if(res == null) 
	  return "-"; 
	if(pWorkGroups.contains(res)) 
	  return ("[" + res + "]");
	return res;
      }

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
    case 11:
      {
	String group = host.getHardwareGroup();
	if(group == null) 
	  group = "-";
	return group;
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
    int vrow = pRowToIndex[row];
    boolean edited = setValueAtHelper(value, vrow, col);

    {
      int[] selected = pTable.getSelectedRows(); 
      int wk;
      for(wk=0; wk<selected.length; wk++) {
	int srow = pRowToIndex[selected[wk]];
	if(srow != vrow)
	  setValueAtHelper(value, srow, col);
      }
    }
      
    if(edited) 
      fireTableDataChanged();
  }


  public boolean 
  setValueAtHelper
  (
   Object value, 
   int srow, 
   int col
  ) 
  {
    QueueHostInfo host = pQueueHosts.get(srow);

    UserPrefs prefs = UserPrefs.getInstance();
    String hostname = prefs.getShowFullHostnames() ? host.getName() : host.getShortName(); 

    switch(col) {
    case 0:
      {
	return setStatus(host, srow, hostname, value, null, false);
      }
    case 6:
      {
	return setSlots(host, srow, hostname, (Integer) value, null, false);
      }
      
    case 7:
      {
	return setReservation(host, srow, hostname, (String) value, null, false);
      }

    case 8:
      {
	return setOrder(host, srow, hostname, (Integer) value, null, false);
      }

    case 9:
      {
	return setGroup(host, srow, hostname, (String) value, null, false);
      }

    case 10:
      {
	String sname = (String) value;
	if(sname.equals("-")) 
	  sname = null;
	host.setSelectionSchedule(sname);

	pEditedScheduleIndices.add(srow);
	pParent.unsavedChange("Selection Schedule: " + hostname);

	if (sname != null) {
	  SelectionScheduleMatrix matrix = pParent.getSelectionScheduleMatrix();
	  if(matrix != null) {
	    Set<String> schedules = matrix.getScheduleNames();
	    if(schedules.contains(sname)) {
	      setGroup(host, srow, hostname, matrix.getScheduledGroup(sname), 
		matrix.getScheduledGroupState(sname), true);
	      setSlots(host, srow, hostname, matrix.getScheduledSlots(sname), 
		matrix.getScheduledSlotsState(sname), true);
	      setStatus(host, srow, hostname, matrix.getScheduledStatus(sname), 
		matrix.getScheduledStatusState(sname), true);
	      if (matrix.getScheduledReservation(sname) == true)
		setReservation(host, srow, hostname, "-", 
		  matrix.getScheduledReservationState(sname), true);
	      else
		setReservation(host, srow, hostname, null, 
		  matrix.getScheduledReservationState(sname), true);
	      setOrder(host, srow, hostname, matrix.getScheduledOrder(sname), 
		matrix.getScheduledOrderState(sname), true);
	    }
	    else 
	      clearState(host);
	  }
	}
	else
	  clearState(host);
	return true; 
      }
    
    case 11:
      {
	return setHardwareGroup(host, srow, hostname, (String) value);
      }
      
    default:
      return false;
    }
  }
  
  private void
  clearState
  (
    QueueHostInfo host  
  )
  {
    host.setGroupState(EditableState.Manual);
    host.setOrderState(EditableState.Manual);
    host.setStatusState(EditableState.Manual);
    host.setSlotsState(EditableState.Manual);
    host.setReservationState(EditableState.Manual);
  }
  
  /**
   * Helper method to set the group of the current host.
   */
  private boolean
  setGroup
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    String group,
    EditableState state,
    boolean sched
  )
  {
    if ( (sched && (state == null || state == EditableState.Automatic)  ) || !sched  ) {
      if(group.equals("-")) 
	group = null;
      host.setSelectionGroup(group);
      pEditedGroupIndices.add(srow);
      pParent.unsavedChange("Selection Group: " + hostname);
    }
    if (state != null)
      host.setGroupState(state);

    return true; 
  }
  
  /**
   * Helper method to set the hardware group of the current host.
   */
  private boolean
  setHardwareGroup
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    String group
  )
  {
    if(group.equals("-")) 
      group = null;
    host.setHardwareGroup(group);

    pEditedHardwareIndices.add(srow);
    pParent.unsavedChange("Hardware Group: " + hostname);
    return true; 
  }
  
  /**
   * Helper method to set the order of the current host.
   */
  private boolean
  setOrder
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    Integer order,
    EditableState state,
    boolean sched
  )
  {
    if ( (sched && (state == null || state == EditableState.Automatic)  ) || !sched  ) { 
      if((order != null) && (order >= 0)) {
	host.setOrder(order);
	pEditedOrderIndices.add(srow);
	pParent.unsavedChange("Order: " + hostname);
      }
    }
    if (state != null)
      host.setOrderState(state);

    return true; 
  }
  
  /**
   * Helper method to set the slots of the current host.
   */
  private boolean
  setSlots
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    Integer slots,
    EditableState state,
    boolean sched
  )
  {
    if ( (sched && (state == null || state == EditableState.Automatic)  ) || !sched  ) {
      if((slots != null) && (slots >= 0)) {
	host.setJobSlots(slots);
	pEditedSlotsIndices.add(srow);
	pParent.unsavedChange("Slots: " + hostname);
      }
    }
    if (state != null)
      host.setSlotsState(state);

    return true; 
  }
  
  /**
   * Helper method to set the status of the current host.
   */
  private boolean
  setStatus
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    Object value,
    EditableState state,
    boolean sched
  )
  {
    boolean toReturn = false;
    if ( (sched && (state == null || state == EditableState.Automatic)  ) || !sched  ) {
      if(QueueHostStatusChange.titles().contains(value)) {
	QueueHostStatusChange change = 
	  QueueHostStatusChange.valueOf(QueueHostStatusChange.class, (String) value);
	pQueueHostStatusChanges.set(srow, change);
	pParent.unsavedChange("Status: " + hostname);
	toReturn = true;
      }

    }
    if (state != null)
      host.setStatusState(state);
    return toReturn;
  }

  /**
   * Helper method to set the reservation of the current host.
   */
  private boolean
  setReservation
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    String res,
    EditableState state,
    boolean sched
  )
  {
    if (res != null) {
      if ( (sched && (state == null || state == EditableState.Automatic)  ) || !sched  ) {
	if(res.equals("-")) 
	  host.setReservation(null); 
	else if(res.startsWith("[") && res.endsWith("]"))
	  host.setReservation(res.substring(1, res.length()-1));
	else 
	  host.setReservation(res);
	pEditedReserveIndices.add(srow);
	pParent.unsavedChange("Reservation: " + hostname);
      }
    }
    if (state != null)
      host.setReservationState(state);

    return true;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8796631808173092560L;

  /**
   * The number of samples to cache (at 15-second intervals). 
   */ 
  private static final int sCacheSize = 120;  

  /**
   * The interval of time displayed by the bar graphs 
   */ 
  private static final long sCacheInterval = 1800000L;   /* 30-minutes */ 
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent panel.
   */ 
  private JQueueJobServersPanel pParent;
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The details of the administrative privileges granted to the current user. 
   */ 
  private PrivilegeDetails  pPrivilegeDetails; 

  /**
   * The underlying set of hosts.
   */ 
  private ArrayList<QueueHostInfo> pQueueHosts;
  
  /**
   * The underlying set of host status changes.  Entries which are <CODE>null</CODE> should
   * use the status from the pQueueHost.getStatus() method instead.
   */ 
  private ArrayList<QueueHostStatusChange> pQueueHostStatusChanges;

  /**
   * The cached resource samples for each host.
   */ 
  private TreeMap<String,ResourceSampleCache> pSamples;
  


  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the user work groups.
   */
  private TreeSet<String>  pWorkGroups; 
  
  /**
   * The names of the work group members.
   */
  private TreeSet<String> pWorkUsers;

  /**
   * The valid selection group names. 
   */ 
  private TreeSet<String>  pSelectionGroups; 
  
  /**
   * The valid selection group names. 
   */ 
  private TreeSet<String>  pHardwareGroups; 

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
  
  /**
   * The indices of hosts which have had their hardware group edited.
   */ 
  private TreeSet<Integer>  pEditedHardwareIndices; 




  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}
