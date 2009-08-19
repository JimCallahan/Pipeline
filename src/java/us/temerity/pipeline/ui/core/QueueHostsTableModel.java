// $Id: QueueHostsTableModel.java,v 1.25 2009/08/19 23:42:47 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
import us.temerity.pipeline.ui.*;

import java.util.*;
import javax.swing.JLabel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   H O S T S   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains a set of 
 * {@link QueueHostInfo QueueHostInfo} instances.
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

      pQueueHostnames = new ArrayList<String>(); 
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
      pNumColumns = 13;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, 
	  ResourceSampleCache.class, ResourceSampleCache.class, ResourceSampleCache.class,
	  ResourceSampleCache.class, Integer.class,
	  String.class, Integer.class, String.class, String.class, String.class
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Hostname", "Status", "OS", 
	  "System Load", "Free Memory", "Free Disk Space", 
	  "Jobs", "Slots", 
	  "Reservation", "Order", "Selection", "Schedule", "Hardware"
	};
	pColumnNames = names;
      }

      {
	String colors[] = {
	  "", "", "", 
	  "Blue", "Blue", "Blue", 
	  "Green", "Green", 
	  "Purple", "Purple", "Purple", "Purple", "Purple"
	};
	pColumnColorPrefix = colors; 
      }
      
      {
	String desc[] = {
          "The fully resolved host name.", 
	  "The current status of the job server.", 
	  "The operating system type of the job server.", 
	  "The system load of the server.", 
	  "The amount of unused system memory (in GB).", 
	  "The amount of available temporary disk space (in GB).", 
	  "The number of job running on the server.", 
	  "The maximum number of simultaneous jobs allowed to on the server.",
	  "The name of the user holding the server reservation.", 
	  "The order in which jobs are dispatched to the servers.", 
	  "The name of the selection group.", 
	  "The name of the selection schedule.",
	  "The name of the hardware group."
	};
	pColumnDescriptions = desc;
      }

      {
        Vector3i ranges[] = {
          new Vector3i(120, 200, Integer.MAX_VALUE), 
          new Vector3i(120),
          new Vector3i(90),
          new Vector3i(135),
          new Vector3i(135),
          new Vector3i(135),
          new Vector3i(135),
          new Vector3i(70),
          new Vector3i(120, 120, Integer.MAX_VALUE), 
          new Vector3i(90),
          new Vector3i(120, 120, Integer.MAX_VALUE), 
          new Vector3i(120, 120, Integer.MAX_VALUE), 
          new Vector3i(120, 120, Integer.MAX_VALUE)
        };
        pColumnWidthRanges = ranges;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
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
          null, 
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
   TreeMap<String,String> names, 
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
    pQueueHostnames.clear(); 
    pQueueHosts.clear();
    if(hosts != null) {
      for(Map.Entry<String,QueueHostInfo> entry : hosts.entrySet()) {
        String hname = names.get(entry.getKey()); 
        if(hname != null) {
          pQueueHostnames.add(hname); 
          pQueueHosts.add(entry.getValue()); 
        }
      }
    }
    
    pNumRows = pQueueHostnames.size(); 

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
  /*   S O R T I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sort the rows by the values in the current sort column and direction.
   */ 
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx;
    for(idx=0; idx<pNumRows; idx++) {
      QueueHostInfo host = pQueueHosts.get(idx); 
      Comparable value = null;
      switch(pSortColumn) {
      case 0:
        value = pQueueHostnames.get(idx);
        break;

      case 1:
	{
	  QueueHostStatusChange change = pQueueHostStatusChanges.get(idx);
	  if(change != null) 
	    value = change.toString();
	  else 
	    value = host.getStatus().toString();
	}
	break;

      case 2:
        value = host.getOsType();
	break;

      case 3:
      case 4:
      case 5:
      case 6:
	{
	  ResourceSample sample = host.getLatestSample();
	  if(sample != null) {
	    switch(pSortColumn) {
	    case 3:
	      value = new Float(sample.getLoad());
	      break;

	    case 4:
	      value = new Long(sample.getMemory());
	      break;

	    case 5:
	      value = new Long(sample.getDisk());
	      break;
	      
	    case 6:
	      value = new Integer(sample.getNumJobs());
	    }
	  }
	}
	break;

      case 7:
	value = new Integer(host.getJobSlots());
	break;
	
      case 8:
	value = host.getReservation();
	if((value != null) && pWorkGroups.contains(value)) 
	  value = ("[" + value + "]");
	break;
	
      case 9:
	value = new Integer(host.getOrder());
	break;

      case 10:
	value = host.getSelectionGroup();
	break;
	
      case 11:
	value = host.getSelectionSchedule();
	break;
	
      case 12:
	value = host.getHardwareGroup();
	break;

      default:
	assert(true) : ("Invalid column index (" + pSortColumn + ")!");
      }
      
      cells[idx] = new IndexValue(idx, value); 
    }

    Comparator<IndexValue> comp = 
      pSortAscending ? new AscendingIndexValue() : new DescendingIndexValue(); 
    Arrays.sort(cells, comp);

    pRowToIndex = new int[pNumRows];
    int row; 
    for(row=0; row<pNumRows; row++) 
      pRowToIndex[row] = cells[row].getIndex();       

    fireTableDataChanged();
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
    case 8: /* Reservation */
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

    case 10: /* Selection Group */
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionGroups);

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }

    case 11: /* Schedule */
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionSchedules); 

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }
      
    case 12: /* Hardware Group */ 
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
    case 7: /* Slots */
      return (editable && 
	      ((host != null) && (host.getSlotsState() != EditableState.Automatic)));

    case 9: /* Order */
      return (editable && 
	      ((host != null) && (host.getOrderState() != EditableState.Automatic)));
    
    case 1:  /* Status */
    case 11: /* Schedule */
    case 12: /* Hardware Group */ 
      return editable;
    
    case 8:  /* Reservation */
      return (editable && 
	      ((host != null) && (host.getReservationState() != EditableState.Automatic)));
      
    case 10: /* Selection Group */
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
      return pQueueHostnames.get(srow);

    case 1:
      {
	QueueHostStatusChange change = pQueueHostStatusChanges.get(srow);
	if(change != null) 
	  return change.toString();
	else 
	  return host.getStatus().toString();
      }

    case 2:
      return host.getOsType();

    case 3:
    case 4:
    case 5:
    case 6:
      return pSamples.get(host.getName());
      
    case 7:
      return host.getJobSlots();

    case 8:
      {
	String res = host.getReservation();
	if(res == null) 
	  return "-"; 
	if(pWorkGroups.contains(res)) 
	  return ("[" + res + "]");
	return res;
      }

    case 9:
      return host.getOrder();

    case 10:
      {
	String group = host.getSelectionGroup();
	if(group == null) 
	  group = "-";
	return group;
      }

    case 11:
      {
	String sched = host.getSelectionSchedule();
	if(sched == null) 
	  sched = "-";
	return sched;
      }

    case 12:
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
	  if(setValueAtHelper(value, srow, col))
	    edited = true;
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
    case 1:
      return setStatus(host, srow, hostname, value, null, false);

    case 7:
      return setSlots(host, srow, hostname, (Integer) value, null, false);
      
    case 8:
      return setReservation(host, srow, hostname, (String) value, null, false);

    case 9:
      return setOrder(host, srow, hostname, (Integer) value, null, false);

    case 10:
      return setGroup(host, srow, hostname, (String) value, null, false);

    case 11:
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
	else {
	  clearState(host);
        }

	return true; 
      }
    
    case 12:
      return setHardwareGroup(host, srow, hostname, (String) value);
      
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
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      if(group.equals("-")) 
	group = null;
      host.setSelectionGroup(group);
      pEditedGroupIndices.add(srow);
      pParent.unsavedChange("Selection Group: " + hostname);
    }

    if(state != null)
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
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      if((order != null) && (order >= 0)) {
	host.setOrder(order);
	pEditedOrderIndices.add(srow);
	pParent.unsavedChange("Order: " + hostname);
      }
    }

    if(state != null)
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
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      if((slots != null) && (slots >= 0)) {
	host.setJobSlots(slots);
	pEditedSlotsIndices.add(srow);
	pParent.unsavedChange("Slots: " + hostname);
      }
    }

    if(state != null)
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
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      if(QueueHostStatusChange.titles().contains(value)) {
	QueueHostStatusChange change = 
	  QueueHostStatusChange.valueOf(QueueHostStatusChange.class, (String) value);
	pQueueHostStatusChanges.set(srow, change);
	pParent.unsavedChange("Status: " + hostname);
	toReturn = true;
      }
    }

    if(state != null)
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
    if(res != null) {
      if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
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

    if(state != null)
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
   * The displayed hostname. 
   */ 
  private ArrayList<String> pQueueHostnames;
  
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
