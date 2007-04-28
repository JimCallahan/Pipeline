// $Id: QueueSlotsTableModel.java,v 1.10 2007/04/28 22:43:21 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   S L O T S   T A B L E   M O D E L                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link SortableTableModel SortableTableModel} which contains information about the 
 * enabled job server slots. 
 */ 
public
class QueueSlotsTableModel 
  extends AbstractSortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   */
  public 
  QueueSlotsTableModel
  (
   JQueueJobSlotsPanel parent, 
   TreeSet<String> localHostnames
  ) 
  {
    super();
    
    /* initialize the fields */ 
    {
      pParent = parent;
      
      pHostnames = new String[0];
      pJobInfo   = new QueueJobInfo[0];
      pJobStatus = new JobStatus[0];
      pOnHold    = new Long[0];

      pLocalHostnames = localHostnames;
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 8;

      {
	Class classes[] = { 
	  String.class, Long.class, String.class, String.class, String.class, String.class, 
	  String.class, String.class
	};
	pColumnClasses = classes;
      }

      {
	String names[] = { 
	  "Hostname", "Job ID", "Target Files", "On Hold", "Started", "Duration", 
	  "Target Node", "Owner|View"
	};
	pColumnNames = names;
      }

      {
	String desc[] = {
	  "The fully resolved host name.", 
	  "The unique job identifier.", 
	  "The file pattern of the files generated by the job.", 
	  "How long the server is on hold due to job ramp-up.",
	  "When the job began execution.", 
	  "How long the job has been running so far.", 
	  "The name of the node associated with the jobs.", 
	  "The working area where the target files are created."
	};
	pColumnDescriptions = desc;
      }

      {
	int widths[] = { 200, 80, 270, 120, 180, 120, 540, 180 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSlotsTableCellRenderer(this, JLabel.CENTER), 
	  new JSlotsTableCellRenderer(this, JLabel.CENTER), 
	  new JSlotsTableCellRenderer(this, JLabel.CENTER), 
	  new JSlotsTableCellRenderer(this, JLabel.CENTER), 
	  new JSlotsTableCellRenderer(this, JLabel.CENTER),
	  new JSlotsTableCellRenderer(this, JLabel.CENTER),
	  new JSlotsTableCellRenderer(this, JLabel.LEFT), 
	  new JSlotsTableCellRenderer(this, JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { null, null, null, null, null, null, null, null };
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
    long now = System.currentTimeMillis();

    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx;
    for(idx=0; idx<pHostnames.length; idx++) {
      Comparable value = null;

      QueueJobInfo info = pJobInfo[idx];
      JobStatus status = pJobStatus[idx]; 
      Long onHold = pOnHold[idx];

      switch(pSortColumn) {
      case 0:
	value = pHostnames[idx];
	break;

      case 1:
	if(info != null) 
	  value = new Long(info.getJobID());
	else 
	  value = new Long(Long.MAX_VALUE);
	break;

      case 2:
	if(status != null) 
	  value = status.getTargetSequence().toString();
	else 
	  value = "-";
	break;

      case 3:
	if(onHold != null) 
	  value = onHold;
	else 
	  value = 0L;
	break;
	
      case 4:
	if(info != null) 
	  value = info.getStartedStamp();
	else 
	  value = new Long(0L);
	break;

      case 5:
	if(info != null) 
	  value = new Long(now - info.getStartedStamp());
	else 
	  value = new Long(Long.MAX_VALUE);
	break;

      case 6:
	if(status != null) 
	  value = status.getNodeID().getName();
	else 
	  value = "";
	
      case 7:
	if(status != null) 
	  value = (status.getNodeID().getAuthor() + "|" + status.getNodeID().getView());
	else 
	  value = "-";
      }
      
      int wk;
      for(wk=0; wk<values.size(); wk++) {
	if(value.compareTo(values.get(wk)) > 0) 
	  break;
      }
      values.add(wk, value);
      indices.add(wk, idx);
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
   * Get the ID of the job at the given row.
   */
  public Long
  getJobID
  (
   int row
  ) 
  {
    QueueJobInfo info = pJobInfo[pRowToIndex[row]];
    if(info != null) 
      return info.getJobID();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   * 
   * @param hosts
   *   The job servers indexed by fully resolved hostname.
   * 
   * @param jobStatus
   *   The status of all running jobs indexed by job ID.
   * 
   * @param jobInfo
   *   The information about all running jobs indexed by job ID.
   */ 
  public void
  setSlots
  (
   TreeMap<String,QueueHostInfo> hosts, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo
  ) 
  { 
    UserPrefs prefs = UserPrefs.getInstance();

    int cnt = 0;
    for(QueueHostInfo host : hosts.values()) {
      switch(host.getStatus()) {
      case Enabled:
      case Disabled:
	cnt += host.getJobSlots();
      }
    }

    pIsEnabled = new boolean[cnt];
    pHostnames = new String[cnt];
    pJobInfo   = new QueueJobInfo[cnt];
    pJobStatus = new JobStatus[cnt];
    pOnHold    = new Long[cnt];
    
    long now = TimeStamps.now();

    int wk = 0;
    for(String hostname : hosts.keySet()) {
      QueueHostInfo host = hosts.get(hostname);
      switch(host.getStatus()) {
      case Enabled:
      case Disabled:
	{
	  int slots = host.getJobSlots();

	  ArrayList<QueueJobInfo> hinfo = new ArrayList<QueueJobInfo>();
	  for(QueueJobInfo info : jobInfo.values()) {
	    if(hostname.equals(info.getHostname())) 
	      hinfo.add(info);
	  }
	  
	  ArrayList<JobStatus> hstatus = new ArrayList<JobStatus>();
	  for(QueueJobInfo info : hinfo) 
	    hstatus.add(jobStatus.get(info.getJobID()));

	  Long onHold = null;
	  {
	    long stamp = host.getHold();
	    if(stamp > now)
	      onHold = stamp - now;
	  }
	  
	  int sk;
	  for(sk=0; sk<host.getJobSlots(); sk++) {
	    pIsEnabled[wk] = (host.getStatus() == QueueHostStatus.Enabled);

            if(prefs.getShowFullHostnames())   
              pHostnames[wk] = hostname;
            else
              pHostnames[wk] = host.getShortName(); 
	    
	    if(sk < hinfo.size()) {
	      QueueJobInfo info = hinfo.get(sk);
	      JobStatus status  = hstatus.get(sk);
	      if((info != null) && (status != null)) {
		pJobInfo[wk]   = info;
		pJobStatus[wk] = status;
	      }
	    }
	    else {
	      pOnHold[wk] = onHold;
	    }
	    
	    wk++;
	  }
	}
      }
    }
    
    sort();
  }
  
  /**
   * Whether the host owning the slot on the given row is enabled.
   */ 
  public boolean 
  isSlotEnabled
  (
   int row
  ) 
  {
    return pIsEnabled[pRowToIndex[row]];
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
    return pHostnames.length;
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
    long now = System.currentTimeMillis();
    int irow = pRowToIndex[row];

    String hostname = pHostnames[irow];
    QueueJobInfo info = pJobInfo[irow];
    JobStatus status = pJobStatus[irow];
    Long onHold = pOnHold[irow];

    switch(col) {
    case 0:
      return hostname;

    case 1:
      if(info != null) 
	return info.getJobID();
      else 
	return null;

    case 2:
      if(status != null) 
	return status.getTargetSequence().toString();
      else 
	return null;
      
    case 3:
      if(onHold != null) 
	return TimeStamps.formatInterval(onHold);
      else 
	return null;

    case 4:
      if(info != null) 
	return TimeStamps.format(info.getStartedStamp());  
      else 
	return null;

    case 5:
      if(info != null) 
 	return TimeStamps.formatInterval(now - info.getStartedStamp());
      else 
	return null;
      
    case 6:
      if(status != null) 
	return status.getNodeID().getName();
      else 
	return "";
	
    case 7:
      if(status != null) 
	return (status.getNodeID().getAuthor() + "|" + status.getNodeID().getView());
      else 
	return null;

    default:
      assert(false);
      return null;
    }    
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 100489263612074719L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent panel.
   */ 
  private JQueueJobSlotsPanel pParent;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the host owning each slot is Enabled.
   */ 
  private boolean[] pIsEnabled; 

  /**
   * The per-slot hostnames.
   */ 
  private String[] pHostnames;

  /**
   * The job information for jobs associated with each slot. <P>
   * 
   * If an entry is <CODE>null</CODE>, then no job is running on the slot.
   */ 
  private QueueJobInfo[]  pJobInfo;

  /**
   * The job status for jobs associated with each slot. <P>
   * 
   * If an entry is <CODE>null</CODE>, then no job is running on the slot.
   */ 
  private JobStatus[]  pJobStatus;
 
  /**
   * The on hold duration for each slot. <P> 
   * 
   * If an entry is <CODE>null</CODE>, then the slot is either active or ready.
   */ 
  private Long[] pOnHold;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}
