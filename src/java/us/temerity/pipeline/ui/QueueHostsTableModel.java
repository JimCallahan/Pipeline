// $Id: QueueHostsTableModel.java,v 1.3 2004/08/23 04:29:11 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.text.*;
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
  extends SortableTableModel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a table model.
   * 
   * @param parent
   *   The parent dialog. 
   */
  public 
  QueueHostsTableModel
  (
   JManageJobServersDialog parent
  ) 
  {
    super();
    
    pParent = parent;

    /* initialize the columns */ 
    { 
      pNumColumns = 8;

      {
	Class classes[] = { 
	  String.class, String.class, String.class, 
	  QueueHost.class, QueueHost.class, QueueHost.class, 	  
	  QueueHost.class, Integer.class 
	}; 
	pColumnClasses = classes;
      }

      {
	String names[] = {
	  "Hostname", "Status", "Reservation", 
	  "System Load", "Free Memory", "Free Disk Space", 
	  "Jobs", "Slots" 
	};
	pColumnNames = names;
      }

      {
	int widths[] = { 200, 120, 120, 135, 135, 135, 135, 60 };
	pColumnWidths = widths;
      }

      {
	JSimpleTableCellRenderer slotsRenderer = new JSimpleTableCellRenderer(JLabel.CENTER);
	slotsRenderer.setName("GreenTableCellRenderer");

	TableCellRenderer renderers[] = {
	  new JSimpleTableCellRenderer(JLabel.CENTER), 
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
	  slotsRenderer
	};
	pRenderers = renderers;
      }

      {
	JCollectionTableCellEditor editor = 
	  new JCollectionTableCellEditor(QueueHost.Status.titles(), 120);

	TableCellEditor editors[] = {
	  null, 
	  editor, 
	  new JIdentifierTableCellEditor(120, JLabel.CENTER), 
	  null, 
	  null, 
	  null, 
	  null, 
	  new JJobSlotsTableCellEditor()
	};
	pEditors = editors;
      }
    }

    pQueueHosts    = new ArrayList<QueueHost>();
    pSelectionKeys = new ArrayList<String>();

    pEditedStatusIndices  = new TreeSet<Integer>();
    pEditedReserveIndices = new TreeSet<Integer>();
    pEditedSlotsIndices   = new TreeSet<Integer>();
    pEditedBiasesIndices  = new TreeSet<Integer>();    
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
    ArrayList<String> values = new ArrayList<String>();
    ArrayList<Integer> indices = new ArrayList<Integer>();
    int idx = 0;
    for(QueueHost host : pQueueHosts) {
      String value = null;
      switch(pSortColumn) {
      case 0:
	value = host.getName();
	break;

      case 1:
	value = host.getStatus().toString();
	break;

      case 2:
	value = host.getReservation();
	if(value == null)
	  value = "";
	break;

      case 3:
      case 4:
      case 5:
      case 6:
	{
	  ResourceSample sample = host.getLatestSample();
	  if(sample == null) 
	    value = "";
	  else {
	    switch(pSortColumn) {
	    case 3:
	      value = String.format("%1$.1f", sample.getLoad());
	      break;

	    case 4:
	      value = String.format("%1$.1f", ((double) sample.getMemory()) / 1073741824.0);
	      break;

	    case 5:
	      value = String.format("%1$.1f", ((double) sample.getDisk()) / 1073741824.0);
	      break;
	      
	    case 6:
	      value = String.valueOf(sample.getNumJobs());
	    }
	  }
	}
	break;

      case 7:
	value = String.valueOf(host.getJobSlots());
	break;
	
      default:
	{
	  value = "";
	  String kname = pSelectionKeys.get(pSortColumn-8);
	  if(kname != null) {
	    Integer bias = host.getSelectionBias(kname);
	    if(bias != null) 
	      value = bias.toString();
	  }
	}
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
  /*   S O R T A B L E   T A B L E   M O D E L   O V E R R I D E S                          */
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
    if(col < 8)
      return pColumnClasses[col];
    else 
      return Integer.class;
  }
  
  /**
   * Returns the number of columns in the model.
   */ 
  public int
  getColumnCount()
  {
    return (pNumColumns + pSelectionKeys.size());
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
    if(col < 8)
      return pColumnNames[col];
    else 
      return pSelectionKeys.get(col-8);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set table data.
   */ 
  public void
  setQueueHosts
  (
   TreeMap<String,QueueHost> hosts, 
   TreeSet<String> keys, 
   boolean isPrivileged
  ) 
  {
    pQueueHosts.clear();
    if(hosts != null)
      pQueueHosts.addAll(hosts.values());

    pSelectionKeys.clear();
    if(keys != null) 
      pSelectionKeys.addAll(keys);

    pIsPrivileged = isPrivileged;

    sort();
  }

  
  /*----------------------------------------------------------------------------------------*/
  
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
   * Get the changes to host selection key biases.
   */ 
  public TreeMap<String,TreeMap<String,Integer>>
  getHostBiases() 
  {
    TreeMap<String,TreeMap<String,Integer>> table = 
      new TreeMap<String,TreeMap<String,Integer>>();
    for(Integer idx : pEditedBiasesIndices) {
      QueueHost host = pQueueHosts.get(idx);
      if(host != null) {
	TreeMap<String,Integer> biases = new TreeMap<String,Integer>();
	for(String kname : host.getSelectionKeys()) 
	  biases.put(kname, host.getSelectionBias(kname));

	table.put(host.getName(), biases); 
      }
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
    if(!pIsPrivileged) 
      return false;

    switch(col) {
    case 1: 
    case 2: 
    case 7:
      return true;

    default:
      return (col > 7); 
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
      return host.getName();

    case 1:
      return host.getStatus().toString();

    case 2:
      return host.getReservation();

    case 3:
    case 4:
    case 5:
    case 6:
      return host;
      
    case 7:
      return host.getJobSlots();

    default:
      {
	String kname = pSelectionKeys.get(col-8);
	if(kname != null) 
	  return host.getSelectionBias(kname);
	else {
	  assert(false);
	  return null;
	}
      }
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
    int idx = pRowToIndex[row];
    QueueHost host = pQueueHosts.get(idx);
    switch(col) {
    case 1:
      {
	host.setStatus(QueueHost.Status.valueOf(QueueHost.Status.class, (String) value));

	pEditedStatusIndices.add(idx);
	pParent.doEdited(); 
      }
      break;

    case 2:
      {
	String author = (String) value;
	if((author != null) && (author.length() == 0)) 
	  author = null;
	host.setReservation(author);

	pEditedReserveIndices.add(idx);
	pParent.doEdited(); 
      }
      break;

    case 7:
      {
	Integer slots = (Integer) value;
	if((slots != null) && (slots >= 0)) 
	  host.setJobSlots(slots);

	pEditedSlotsIndices.add(idx);
	pParent.doEdited(); 
      }
      break;

      
    default:
      if(col > 7) {
	String kname = pSelectionKeys.get(col-8);
	if(kname != null) {
	  Integer bias = (Integer) value;
	  if(bias == null) {
	    host.removeSelectionKey(kname); 
	    pEditedBiasesIndices.add(idx);
	    pParent.doEdited(); 
	  }
	  else if((bias >= -100) && (bias <= 100)) {
	    host.addSelectionKey(kname, bias);
	    pEditedBiasesIndices.add(idx);
	    pParent.doEdited(); 
	  }	  
	}
      }
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
   * Does the current user have privileged status?
   */ 
  private boolean  pIsPrivileged;

  /**
   * The underlying set of editors.
   */ 
  private ArrayList<QueueHost> pQueueHosts;

  /**
   * The names of the valid selection keys.
   */ 
  private ArrayList<String>  pSelectionKeys; 

  /**
   * The parent dialog.
   */ 
  private JManageJobServersDialog  pParent;


  /**
   * The indices of host which have had their status edited.
   */ 
  private TreeSet<Integer>  pEditedStatusIndices; 

  /**
   * The indices of host which have had their user reservations edited.
   */ 
  private TreeSet<Integer>  pEditedReserveIndices; 

  /**
   * The indices of host which have had their job slots edited.
   */ 
  private TreeSet<Integer>  pEditedSlotsIndices; 

  /**
   * The indices of host which have had their selection key biases edited.
   */ 
  private TreeSet<Integer>  pEditedBiasesIndices; 
}
