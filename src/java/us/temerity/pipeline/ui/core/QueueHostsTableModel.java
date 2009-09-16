// $Id: QueueHostsTableModel.java,v 1.26 2009/09/16 03:54:40 jesse Exp $

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
 * 
 * <ol start=0>
 * <li> Hostname
 * <li> Status
 * <li> OS
 * <li> System Load
 * <li> Free Memory'''
 * <li> Free Disk Space
 * <li> Jobs
 * <li> Slots
 * <li> Order
 * <li> Reservation
 * <li> User Balance Group
 * <li> Favor Method
 * <li> Selection Group
 * <li> Dispatch Control
 * <li> Queue Schedules
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

      pQueueHostnames = new ArrayList<String>(); 
      pQueueHosts = new ArrayList<QueueHostInfo>();
      pQueueHostStatusChanges = new ArrayList<QueueHostStatusChange>();

      pSamples = new TreeMap<String,ResourceSampleCache>();

      pWorkGroups = new TreeSet<String>();
      pWorkUsers  = new TreeSet<String>();
      
      pSelectionGroups    = new TreeSet<String>();
      pSelectionSchedules = new TreeSet<String>();
      pHardwareGroups     = new TreeSet<String>();
      pDispatchControls   = new TreeSet<String>();
      pUserBalanceGroups  = new TreeSet<String>();
      
      pEditedStatusIndices      = new TreeSet<Integer>();
      pEditedReserveIndices     = new TreeSet<Integer>();
      pEditedOrderIndices       = new TreeSet<Integer>();
      pEditedSlotsIndices       = new TreeSet<Integer>();
      pEditedGroupIndices       = new TreeSet<Integer>();  
      pEditedScheduleIndices    = new TreeSet<Integer>();  
      pEditedHardwareIndices    = new TreeSet<Integer>();
      pEditedDispatchIndices    = new TreeSet<Integer>();
      pEditedFavorGroupIndices  = new TreeSet<Integer>();
      pEditedUserBalanceIndices = new TreeSet<Integer>();

      pLocalHostnames = localHostnames;
    }

    /* initialize the columns */ 
    { 
      pNumColumns = TableDefs.values().length;
      pColumnClasses = new Class[pNumColumns];
      pColumnNames = new String[pNumColumns];
      pColumnColorPrefix = new String[pNumColumns];
      pColumnDescriptions = new String[pNumColumns];
      pColumnWidthRanges = new Vector3i[pNumColumns];
      pRenderers = new TableCellRenderer[pNumColumns];
      pEditors = new TableCellEditor[pNumColumns];

      for (TableDefs def : TableDefs.values()) {
        int ord = def.ordinal();
        pColumnClasses[ord] = def.getColumnClass();
        pColumnNames[ord] = def.getColumnName();
        pColumnColorPrefix[ord] = def.getColumnColorPrefix();
        pColumnDescriptions[ord] = def.getColumnDescription();
        pColumnWidthRanges[ord] = def.getColumnWidthRange();
        pRenderers[ord] = def.getRenderer(this);
        pEditors[ord] = def.getEditor(this);
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
      toReturn = (String) getValueAt(row, TableDefs.SCHEDULE.ordinal());
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
   * @param selectionGroups
   *   The valid selection group names. 
   * 
   * @param selectionSchedules
   *   The valid selection schedule names.
   *   
   * @param names
   *   The short names of all the hosts
   *   
   * @param hardwareGroups
   *   The valid hardware group names.
   *   
   * @param dispatchControls
   *   The valid dispatch control names.
   *   
   * @param userBalanceGroups
   *   The valid user balance group names.
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
    TreeSet<String> dispatchControls,
    TreeSet<String> userBalanceGroups,
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
    
    pDispatchControls.clear();
    if (dispatchControls != null)
      pDispatchControls.addAll(dispatchControls);
    
    pUserBalanceGroups.clear();
    if (userBalanceGroups != null)
      pUserBalanceGroups.addAll(userBalanceGroups);

    pPrivilegeDetails = privileges; 
    
    pEditedStatusIndices.clear();
    pEditedReserveIndices.clear();
    pEditedOrderIndices.clear();
    pEditedSlotsIndices.clear();
    pEditedGroupIndices.clear();
    pEditedHardwareIndices.clear();
    pEditedScheduleIndices.clear();
    pEditedDispatchIndices.clear();
    pEditedFavorGroupIndices.clear();
    pEditedUserBalanceIndices.clear();

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
	
	String dispatch = null;
	boolean dispatchModified = false;
	if (pEditedDispatchIndices.contains(idx)) {
	  dispatch = host.getDispatchControl();
	  dispatchModified = true;
	}
	
	String userBalance = null;
	boolean userBalanceModified = false;
	if (pEditedUserBalanceIndices.contains(idx)) {
	  userBalance = host.getUserBalanceGroup();
	  userBalanceModified = true;
	}
	
	JobGroupFavorMethod favorMethod = null;
	if (pEditedFavorGroupIndices.contains(idx)) {
	  favorMethod = host.getFavorMethod();
	}

	if((change != null) || reservationModified || 
	   (order != null) || (slots != null) ||
	   groupModified || scheduleModified || hardwareModified ||
	   dispatchModified || userBalanceModified || favorMethod != null) {

	  QueueHostMod qmod = 
	    new QueueHostMod(change, reservation, reservationModified, order, slots, 
			     group, groupModified, schedule, scheduleModified,
			     hardware, hardwareModified, dispatch, dispatchModified,
			     userBalance, userBalanceModified, favorMethod);

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
  @Override
  public void 
  sort()
  {
    IndexValue cells[] = new IndexValue[pNumRows]; 
    int idx;
    for(idx=0; idx<pNumRows; idx++) {
      QueueHostInfo host = pQueueHosts.get(idx); 
      Comparable value = null;
      TableDefs swtch = TableDefs.values()[pSortColumn];
      switch(swtch) {
      case HOSTNAME:
        value = pQueueHostnames.get(idx);
        break;

      case STATUS:
	{
	  QueueHostStatusChange change = pQueueHostStatusChanges.get(idx);
	  if(change != null) 
	    value = change.toString();
	  else 
	    value = host.getStatus().toString();
	}
	break;

      case OS:
        value = host.getOsType();
	break;

      case LOAD:
      case MEMORY:
      case DISK:
      case JOBS:
	{
	  ResourceSample sample = host.getLatestSample();
	  if(sample != null) {
	    switch(swtch) {
	    case LOAD:
	      value = new Float(sample.getLoad());
	      break;

	    case MEMORY:
	      value = new Long(sample.getMemory());
	      break;

	    case DISK:
	      value = new Long(sample.getDisk());
	      break;
	      
	    case JOBS:
	      value = new Integer(sample.getNumJobs());
	    }
	  }
	}
	break;

      case SLOTS:
	value = new Integer(host.getJobSlots());
	break;
	
      case RESERVATION:
	value = host.getReservation();
	if((value != null) && pWorkGroups.contains(value)) 
	  value = ("[" + value + "]");
	break;
	
      case ORDER:
	value = new Integer(host.getOrder());
	break;

      case SELECTION: 
	value = host.getSelectionGroup();
	break;
	
      case DISPATCH:
        value = host.getDispatchControl();
        break;
	
      case SCHEDULE:
	value = host.getSelectionSchedule();
	break;
	
      case HARDWARE:
	value = host.getHardwareGroup();
	break;
	
      case FAVORMETHOD:
        value = host.getFavorMethod();
        break;
        
      case USERBALANCE:
        value = host.getUserBalanceGroup();
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
  @Override
  public TableCellEditor
  getEditor
  (
    int col   
  )
  {
    TableDefs swtch = TableDefs.values()[col];
    switch(swtch) {
    case RESERVATION:
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

    case SELECTION:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionGroups);

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }
      
    case DISPATCH:
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("-");
        choices.addAll(pDispatchControls);
  
        JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
        editor.setSynthPrefix("Purple");
  
        return editor;
      }

    case SCHEDULE:
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pSelectionSchedules); 

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }
      
    case HARDWARE: 
      {
	ArrayList<String> choices = new ArrayList<String>();
	choices.add("-");
	choices.addAll(pHardwareGroups);

	JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
	editor.setSynthPrefix("Purple");

	return editor;
      }
      
    case FAVORMETHOD:
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add(JobGroupFavorMethod.None.toTitle());
        choices.add(JobGroupFavorMethod.MostEngaged.toTitle());
        choices.add(JobGroupFavorMethod.MostPending.toTitle());
        
        JCollectionTableCellEditor editor = new JCollectionTableCellEditor(choices, 120);
        editor.setSynthPrefix("Purple");

        return editor;
      }
      
    case USERBALANCE:
      {
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("-");
        choices.addAll(pUserBalanceGroups);
  
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
  @Override
  public boolean 	
  isCellEditable
  (
    int row, 
    int col
  ) 
  {
    QueueHostInfo host = pQueueHosts.get(pRowToIndex[row]);
    if (host == null)
      return false;
    boolean editable = isHostEditable(host.getName());
    
    TableDefs swtch = TableDefs.values()[col];
    switch(swtch) {
    case SLOTS:
      return (editable && 
	      (host.getSlotsState() != EditableState.Automatic));

    case ORDER:
      return (editable && 
	      (host.getOrderState() != EditableState.Automatic));
    
    case STATUS:
    case SCHEDULE:
    case HARDWARE: 
      return editable;
    
    case RESERVATION:
      return (editable && 
	      (host.getReservationState() != EditableState.Automatic));
      
    case SELECTION:
      return (editable && 
	      (host.getGroupState() != EditableState.Automatic));
      
    case DISPATCH:
      return (editable && 
             (host.getDispatchState() != EditableState.Automatic));
  
    case FAVORMETHOD:
      return (editable && 
             (host.getFavorState() != EditableState.Automatic));
      
    case USERBALANCE:
      return (editable && 
             (host.getUserBalanceState() != EditableState.Automatic));
      
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
    
    TableDefs swtch = TableDefs.values()[col];
    switch(swtch) {
    case HOSTNAME:
      return pQueueHostnames.get(srow);

    case STATUS:
      {
	QueueHostStatusChange change = pQueueHostStatusChanges.get(srow);
	if(change != null) 
	  return change.toString();
	else 
	  return host.getStatus().toString();
      }

    case OS:
      return host.getOsType();

    case LOAD:
    case MEMORY:
    case DISK:
    case JOBS:
      return pSamples.get(host.getName());
      
    case SLOTS:
      return host.getJobSlots();

    case RESERVATION:
      {
	String res = host.getReservation();
	if(res == null) 
	  return "-"; 
	if(pWorkGroups.contains(res)) 
	  return ("[" + res + "]");
	return res;
      }

    case ORDER:
      return host.getOrder();

    case SELECTION:
      {
	String group = host.getSelectionGroup();
	if(group == null) 
	  group = "-";
	return group;
      }

    case SCHEDULE:
      {
	String sched = host.getSelectionSchedule();
	if(sched == null) 
	  sched = "-";
	return sched;
      }

    case HARDWARE:
      {
	String group = host.getHardwareGroup();
	if(group == null) 
	  group = "-";
	return group;
      }
      
    case DISPATCH:
      {
        String control = host.getDispatchControl();
        if (control == null)
          control = "-";
        return control;
      }
      
    case USERBALANCE:
      {
        String group = host.getUserBalanceGroup();
        if(group == null) 
          group = "-";
        return group;
      }
      
    case FAVORMETHOD:
      return host.getFavorMethod().toTitle();

    default:
      assert(true) : ("Invalid column index (" + col + ")!");
      return null;
    }    
  }

  /**
   * Sets the value in the cell at columnIndex and rowIndex to aValue.
   */ 
  @Override
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

    TableDefs swtch = TableDefs.values()[col];
    switch(swtch) {
    case STATUS:
      return setStatus(host, srow, hostname, value, null, false);

    case SLOTS:
      return setSlots(host, srow, hostname, (Integer) value, null, false);
      
    case RESERVATION:
      return setReservation(host, srow, hostname, (String) value, null, false);

    case ORDER:
      return setOrder(host, srow, hostname, (Integer) value, null, false);

    case SELECTION:
      return setSelectionGroup(host, srow, hostname, (String) value, null, false);

    case DISPATCH:
      return setDispatchControl(host, srow, hostname, (String) value, null, false);
      
    case USERBALANCE:
      return setUserBalanceGroup(host, srow, hostname, (String) value , null, false);
      
    case FAVORMETHOD:
      return setFavorMethod(host, srow, hostname, (String) value , null, false);
    
    case SCHEDULE:
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
	      setSelectionGroup(host, srow, hostname, matrix.getScheduledGroup(sname), 
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
    
    case HARDWARE:
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
    host.setDispatchState(EditableState.Manual);
  }
  
  /**
   * Helper method to set the group of the current host.
   */
  private boolean
  setSelectionGroup
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
   * Helper method to set the uesr balance group of the current host.
   */
  private boolean
  setUserBalanceGroup
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
      host.setUserBalanceGroup(group);
      pEditedUserBalanceIndices.add(srow);
      pParent.unsavedChange("User Balance Group: " + hostname);
    }

    if(state != null)
      host.setUserBalanceState(state);

    return true; 
  }
  
  /**
   * Helper method to set the uesr balance group of the current host.
   */
  private boolean
  setFavorMethod
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    String method,
    EditableState state,
    boolean sched
  )
  {
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      int i = 0;
      for (String title : JobGroupFavorMethod.titles()) {
        if (title.equals(method))
          break;
        i++;
      }
      host.setFavorMethod(JobGroupFavorMethod.values()[i]);
      pEditedFavorGroupIndices.add(srow);
      pParent.unsavedChange("Favor Method: " + hostname);
    }

    if(state != null)
      host.setFavorState(state);

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
   * Helper method to set the dispatch control of the current host.
   */
  private boolean
  setDispatchControl
  (
    QueueHostInfo host,
    int srow,
    String hostname,
    String control,
    EditableState state,
    boolean sched
  )
  {
    if((sched && ((state == null) || (state == EditableState.Automatic))) || !sched) {
      if(control.equals("-")) 
        control = null;
      host.setDispatchControl(control);
      pEditedDispatchIndices.add(srow);
      pParent.unsavedChange("Dispatch Control: " + hostname);
    }

    if(state != null)
      host.setDispatchState(state);

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
  /*   T A B L E   D E F I N I T I O N S                                                    */
  /*----------------------------------------------------------------------------------------*/

  private
  enum TableDefs
  {
    HOSTNAME,
    STATUS,
    OS,
    LOAD,
    MEMORY,
    DISK,
    JOBS,
    SLOTS,
    ORDER,
    RESERVATION,
    USERBALANCE,
    FAVORMETHOD,
    SELECTION,
    DISPATCH,
    SCHEDULE,
    HARDWARE;
    
    @SuppressWarnings("unchecked")
    public Class
    getColumnClass()
    {
      switch (this) {
      case HOSTNAME:
      case STATUS:
      case OS:
      case SELECTION:
      case DISPATCH:
      case USERBALANCE:
      case SCHEDULE:
      case RESERVATION:
      case HARDWARE:
      case FAVORMETHOD:
        return String.class;
      case LOAD:
      case MEMORY:
      case DISK:
      case JOBS:
        return ResourceSampleCache.class;
      case SLOTS:
      case ORDER:
        return Integer.class;
      default:
        throw new IllegalStateException("Invalid enum!");
      }
    }
    
    public String
    getColumnName()
    {
      switch(this) {
      case HOSTNAME:
        return "Hostname";
      case STATUS:
        return "Status";
      case OS:
        return "OS";
      case LOAD:
        return "System Load";
      case MEMORY:
        return "Free Memory";
      case DISK:
        return "Free Disk Space";
      case JOBS:
        return "Jobs";
      case SLOTS:
        return "Slots";
      case RESERVATION:
        return "Reservation";
      case ORDER:
        return "Order";
      case SELECTION:
        return "Selection";
      case DISPATCH:
        return "Dispatch";
      case SCHEDULE:
        return "Schedule";
      case HARDWARE:
        return "Hardware";
      case FAVORMETHOD:
        return "FavorMethod";
      case USERBALANCE:
        return "UserBalance";
      default:
        throw new IllegalStateException("Invalid enum!");
      }
    }
    
    public String
    getColumnColorPrefix()
    {
      switch(this) {
      case HOSTNAME:
      case STATUS:
      case OS:
        return "";
      case LOAD:
      case MEMORY:
      case DISK:
        return "Blue";
      case JOBS:
      case SLOTS:
        return "Green";
      default:
        return "Purple";
      }
    }
    
    public String
    getColumnDescription()
    {
      switch(this) {
      case HOSTNAME:
        return "The fully resolved host name.";
      case STATUS:
        return "The current status of the job server.";
      case OS:
        return "The operating system type of the job server.";
      case LOAD:
        return "The system load of the server.";
      case MEMORY:
        return "The amount of unused system memory (in GB).";
      case DISK:
        return "The amount of available temporary disk space (in GB).";
      case JOBS:
        return "The number of job running on the server.";
      case SLOTS:
        return "The maximum number of simultaneous jobs allowed to on the server.";
      case RESERVATION:
        return "The name of the user or group holding the server reservation.";
      case ORDER:
        return "The order in which servers are considered for job dispatch.";
      case SELECTION:
        return "The name of the selection group.";
      case DISPATCH:
        return "The name of the dispatch control.";
      case SCHEDULE:
        return "The name of the queue schedule.";
      case HARDWARE:
        return "The name of the hardware group.";
      case FAVORMETHOD:
        return "The job group favor method.";
      case USERBALANCE:
        return "The name of the user balance group.";
      default:
        throw new IllegalStateException("Invalid enum!");
      }
    }
    
    public Vector3i
    getColumnWidthRange()
    {
      switch(this) {
      case HOSTNAME:
        return new Vector3i(120, 200, Integer.MAX_VALUE);
      case STATUS:
        return new Vector3i(120);
      case OS:
      case ORDER:
        return new Vector3i(90);
      case LOAD:
      case MEMORY:
      case DISK:
      case JOBS:
        return new Vector3i(135);
      case SLOTS:
        return new Vector3i(70);
      default:
        return new Vector3i(120, 120, Integer.MAX_VALUE);
      }
    }
    
    public TableCellRenderer
    getRenderer
    (
      QueueHostsTableModel model  
    )
    {
      switch (this) {
      case HOSTNAME:
      case OS:
        return new JSimpleTableCellRenderer(JLabel.CENTER);
      case STATUS:
        return new JQHostStatusTableCellRenderer(model);
      case LOAD:
        return new JResourceSamplesTableCellRenderer
          (model, JResourceSamplesTableCellRenderer.SampleType.Load);
      case MEMORY:
        return new JResourceSamplesTableCellRenderer
          (model, JResourceSamplesTableCellRenderer.SampleType.Memory);
      case DISK:
        return new JResourceSamplesTableCellRenderer
          (model, JResourceSamplesTableCellRenderer.SampleType.Disk);
      case JOBS:
        return new JResourceSamplesTableCellRenderer
          (model, JResourceSamplesTableCellRenderer.SampleType.Jobs); 
      case SLOTS:
        return new JQHostSlotsTableCellRenderer(model);
      case RESERVATION:
        return new JQHostReservationTableCellRenderer(model);
      case ORDER:
        return new JQHostOrderTableCellRenderer(model);
      case SELECTION:
        return new JQHostSGroupTableCellRenderer(model);
      case DISPATCH:
        return new JQHostDispatchTableCellRenderer(model);
      case SCHEDULE:
        return new JQHostSSchedTableCellRenderer(model);
      case HARDWARE:
        return new JQHostHGroupTableCellRenderer(model);
      case FAVORMETHOD:
        return new JQHostFavorTableCellRenderer(model);
      case USERBALANCE:
        return new JQHostUserTableCellRenderer(model);
      default:
        throw new IllegalStateException("Invalid enum!");
      }
    }
    
    public TableCellEditor
    getEditor
    (
      QueueHostsTableModel model  
    )
    {
      switch(this) {
      case STATUS:
       return new JStatusTableCellEditor(model, 120);
      case SLOTS:
        JIntegerTableCellEditor slots = 
          new JIntegerTableCellEditor(70, JLabel.CENTER);
        slots.setName("GreenEditableTextField");
        return slots;
      case ORDER:
        JIntegerTableCellEditor order = 
          new JIntegerTableCellEditor(90, JLabel.CENTER);
        order.setName("PurpleEditableTextField");
        return order;
      default:
        return null;
      }
    }
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
  
  /**
   * The valid dispatch control names.
   */
  private TreeSet<String> pDispatchControls;
  
  /**
   * The valid user balance group names.
   */
 private TreeSet<String> pUserBalanceGroups;

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

  /**
   * The indices of hosts which have had their dispatch control edited.
   */ 
  private TreeSet<Integer>  pEditedDispatchIndices;
  
  /**
   * The indices of hosts which have had their user balance group edited.
   */ 
  private TreeSet<Integer>  pEditedUserBalanceIndices;
  
  /**
   * The indices of hosts which have had their favor group edited.
   */ 
  private TreeSet<Integer>  pEditedFavorGroupIndices;

  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}