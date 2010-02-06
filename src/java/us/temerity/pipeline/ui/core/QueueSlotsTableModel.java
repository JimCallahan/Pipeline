// $Id: QueueSlotsTableModel.java,v 1.14 2009/11/06 00:48:55 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;
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
        Vector3i ranges[] = {
          new Vector3i(120, 200, Integer.MAX_VALUE), 
          new Vector3i(80),
          new Vector3i(120, 240, Integer.MAX_VALUE), 
          new Vector3i(120), 
          new Vector3i(180), 
          new Vector3i(120), 
          new Vector3i(180, 360, Integer.MAX_VALUE), 
          new Vector3i(180)
        };
        pColumnWidthRanges = ranges;
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
  /*   A C C E S S                                                                          */
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

    IntegerOpMap<String> activeJobs = new IntegerOpMap<String>();
    MappedSet<String,Long> hostJobIDs = new MappedSet<String,Long>();
    for(QueueJobInfo info : jobInfo.values()) {
      switch(info.getState()) {
      case Running:
      case Limbo:  
        {
          String hname = info.getHostname();
          if(hname != null) {
            activeJobs.apply(hname, 1); 
            hostJobIDs.put(hname, info.getJobID());
          }
        }
      }
    }

    pNumRows = 0; 
    for(Map.Entry<String,QueueHostInfo> entry : hosts.entrySet()) {
      String hname = entry.getKey();
      QueueHostInfo host = entry.getValue(); 

      activeJobs.apply(hname, host.getJobSlots(), BaseOpMap.Op.Max);

      switch(host.getStatus()) {
      case Enabled:
      case Disabled:
      case Limbo:
	pNumRows += activeJobs.get(hname);
      }
    }

    pHostStatus = new QueueHostStatus[pNumRows];
    pHostnames  = new String[pNumRows];
    pJobInfo    = new QueueJobInfo[pNumRows];
    pJobStatus  = new JobStatus[pNumRows];
    pOnHold     = new Long[pNumRows];
    
    long now = TimeStamps.now();

    int wk = 0;
    for(Map.Entry<String,QueueHostInfo> entry : hosts.entrySet()) {      
      String hname = entry.getKey();
      QueueHostInfo host = entry.getValue(); 

      switch(host.getStatus()) {
      case Enabled:
      case Disabled:
      case Limbo:
	{
	  ArrayList<QueueJobInfo> hinfo = new ArrayList<QueueJobInfo>();
	  ArrayList<JobStatus> hstatus = new ArrayList<JobStatus>();
          {
            TreeSet<Long> jobIDs = hostJobIDs.get(hname);
            if(jobIDs != null) {
              for(Long jobID : jobIDs) {
                QueueJobInfo info = jobInfo.get(jobID);
                JobStatus js = jobStatus.get(jobID); 
                if((info != null) && (js != null)) {
                  hinfo.add(info);
                  hstatus.add(js);
                }
              }
            }
          }
	  
	  Long onHold = null;
	  {
	    long stamp = host.getHold();
	    if(stamp > now)
	      onHold = stamp - now;
	  }

          int slots = activeJobs.get(hname);

	  int sk;
	  for(sk=0; sk<slots; sk++) {
	    pHostStatus[wk] = host.getStatus(); 

            if(prefs.getShowFullHostnames())   
              pHostnames[wk] = hname;
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
    return (pHostStatus[pRowToIndex[row]] == QueueHostStatus.Enabled); 
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
    IndexValue cells[] = new IndexValue[pNumRows]; 
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
	break;

      case 2:
	if(status != null) 
	  value = status.getTargetSequence().toString();
	break;

      case 3:
	if(onHold != null) 
	  value = onHold;
	break;
	
      case 4:
	if(info != null) 
	  value = info.getStartedStamp();
	break;

      case 5:
	if(info != null) 
	  value = new Long(now - info.getStartedStamp());
	break;

      case 6:
	if(status != null) 
	  value = status.getNodeID().getName();
	break;

      case 7:
	if(status != null) 
	  value = (status.getNodeID().getAuthor() + "|" + status.getNodeID().getView());
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

    boolean isLive = false;
    switch(pHostStatus[irow]) {
    case Enabled:
    case Disabled:
      isLive = true;
    }

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
      if((info != null) && isLive) 
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
   * The status of the host owning the slots.
   */ 
  private QueueHostStatus[] pHostStatus;

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
