// $Id: QueueSlotsTableModel.java,v 1.2 2004/11/02 23:06:44 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

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
  extends SortableTableModel
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
   JQueueJobBrowserPanel parent, 
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

      pLocalHostnames = localHostnames;
    }

    /* initialize the columns */ 
    { 
      pNumColumns = 7;

      {
	Class classes[] = { 
	  String.class, Long.class, String.class, String.class, String.class, 
	  String.class, String.class
	};
	pColumnClasses = classes;
      }

      {
	String names[] = { 
	  "Hostname", "Job ID", "Target Files", "Started", "Duration", 
	  "Target Node", "Owner|View"
	};
	pColumnNames = names;
      }

      {
	int widths[] = { 200, 80, 180, 180, 120, 360, 180 };
	pColumnWidths = widths;
      }

      {
	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.CENTER),
	  new JSimpleTableCellRenderer(JLabel.LEFT), 
	  new JSimpleTableCellRenderer(JLabel.CENTER)
	};
	pRenderers = renderers;
      }

      {
	TableCellEditor editors[] = { null, null, null, null, null, null, null };
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
  protected void 
  sort()
  {
    Date now = new Date();

    ArrayList<Comparable> values = new ArrayList<Comparable>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx;
    for(idx=0; idx<pHostnames.length; idx++) {
      Comparable value = null;

      QueueJobInfo info = pJobInfo[idx];
      JobStatus status = pJobStatus[idx]; 

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
	if(info != null) 
	  value = info.getStartedStamp();
	else 
	  value = new Date(0L);
	break;

      case 4:
	if(info != null) 
	  value = new Long(now.getTime() - info.getStartedStamp().getTime());
	else 
	  value = new Long(Long.MAX_VALUE);
	break;

      case 5:
	if(status != null) 
	  value = status.getNodeID().getName();
	else 
	  value = "";
	
      case 6:
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
   * 
   * @param isPrivileged
   *   Does the current user have privileged status?
   */ 
  public void
  setSlots
  (
   TreeMap<String,QueueHost> hosts, 
   TreeMap<Long,JobStatus> jobStatus, 
   TreeMap<Long,QueueJobInfo> jobInfo,
   boolean isPrivileged
  ) 
  {
    int cnt = 0;
    for(QueueHost host : hosts.values()) {
      switch(host.getStatus()) {
      case Enabled:
	cnt += host.getJobSlots();
      }
    }

    pHostnames = new String[cnt];
    pJobInfo   = new QueueJobInfo[cnt];
    pJobStatus = new JobStatus[cnt];

    int wk = 0;
    for(String hostname : hosts.keySet()) {
      QueueHost host = hosts.get(hostname);
      switch(host.getStatus()) {
      case Enabled:
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
	  
	  int sk;
	  for(sk=0; sk<host.getJobSlots(); sk++) {
	    pHostnames[wk] = hostname;
	    
	    if(sk < hinfo.size()) {
	      QueueJobInfo info = hinfo.get(sk);
	      JobStatus status  = hstatus.get(sk);
	      if((info != null) && (status != null)) {
		pJobInfo[wk]   = info;
		pJobStatus[wk] = status;
	      }
	    }
	    
	    wk++;
	  }
	}
      }
    }

    pIsPrivileged = isPrivileged;
    
    sort();
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
    Date now = new Date();
    int irow = pRowToIndex[row];

    String hostname = pHostnames[irow];
    QueueJobInfo info = pJobInfo[irow];
    JobStatus status = pJobStatus[irow];

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
      if(info != null) 
	return Dates.format(info.getStartedStamp());  
      else 
	return null;

    case 4:
      if(info != null) 
 	return Dates.formatInterval(now.getTime() - info.getStartedStamp().getTime());
      else 
	return null;
      
    case 5:
      if(status != null) 
	return status.getNodeID().getName();
      else 
	return "";
	
    case 6:
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
  private JQueueJobBrowserPanel pParent;


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

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
 

  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}
