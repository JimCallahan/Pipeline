// $Id: JResourceSamplesTableCellRenderer.java,v 1.5 2006/11/21 19:55:51 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   R E S O U R C E   S A M P L E S   T A B L E   C E L L   R E N D E R E R                */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells displaying graphs of 
 * {@link ResourceSample ResourceSample} data associated with a 
 * {@link QueueHost QueueHost}. <P> 
 * 
 * This class displays one of the four resources contained in a <CODE>ResourceSample</CODE>:
 * System Load, Free Memory, Free Disk Space or Running Jobs. <P> 
 * 
 * The latest value is displayed in textual form.  All of the samples within a 1-hour window
 * are displayed in graphical form with the latest value highlighed. <P> 
 */ 
public
class JResourceSamplesTableCellRenderer
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JResourceSamplesTableCellRenderer
  (
   QueueHostsTableModel parent, 
   SampleType type
  ) 
  {
    pParent = parent; 
    pSampleType = type; 

    String prefix = null;
    switch(pSampleType) {
    case Jobs:
      {
	Color fg[] = {
	  new Color(0.0f,  1.0f,  0.0f),
	  new Color(0.56f, 0.75f, 0.56f)
	};
	pBarForeground = fg;
	
	Color hl[] = {
	  new Color(1.0f, 0.0f,  0.0f),
	  new Color(0.7f, 0.52f, 0.52f)
	};
	pBarHighlight = hl;
	
	prefix = "Green";
      }
      break;

    default:
      {
	Color fg[] = {
	  new Color(0.0f,  0.59f, 1.0f),
	  new Color(0.56f, 0.67f, 0.75f)
	};
	pBarForeground = fg;
	
	Color hl[] = {
	  new Color(1.0f, 1.0f, 0.0f), 
	  new Color(1.0f, 1.0f, 0.75f)      
	};
	pBarHighlight = hl;

	prefix = "Blue";
      }
    }

    {
      JLabel label = new JLabel("-");
      pNullLabel = label;

      label.setOpaque(true);
      label.setName(prefix + "TableCellRenderer");    
      label.setHorizontalAlignment(JLabel.CENTER);
    }

    {
      JPanel panel = new JPanel();
      pPanel = panel;


      panel.setName(prefix + "ValuePanel");  
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

      {
	JLabel label = new JLabel();
	pLabel = label;
      
	label.setHorizontalAlignment(JLabel.CENTER);

	Dimension size = new Dimension(35, 19); 
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);	  

	panel.add(label);
      }

      {
	JBarGraph bar = new JBarGraph();
	pBarGraph = bar;

	panel.add(bar);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/
          
  /**
   * Returns the component used for drawing the cell.
   */ 
  public Component 	
  getTableCellRendererComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   boolean hasFocus, 
   int row, 
   int column
  )
  {
    ResourceSampleCache cache = (ResourceSampleCache) value; 
    QueueHostInfo host = pParent.getHostInfo(row);
    
    Color fg = new Color(0.75f, 0.75f, 0.75f);
    if(host != null) {
      switch(host.getStatus()) {
      case Enabled:
	fg = (isSelected ? Color.yellow : Color.white);
	break;

      case Disabled:
	break;

      default:
	cache = null;
      }
    }

    /* display the graph */ 
    if((host != null) &&
       (cache != null) && cache.hasSamples()) {

      /* update the numeric label */ 
      {
	ResourceSample latest = cache.getLatestSample(); 
	switch(pSampleType) {
	case Load:
	  pLabel.setText(String.format("%1$.1f", latest.getLoad()));
	  break;		       
	  
	case Memory:
	  pLabel.setText(String.format("%1$.1f", 
				       ((double) latest.getMemory()) / 1073741824.0));
	  break; 
	  
	case Disk:
	  pLabel.setText(String.format("%1$.1f", 
				       ((double) latest.getDisk()) / 1073741824.0));
	  break;
	  
	case Jobs:
	  pLabel.setText(String.valueOf(latest.getNumJobs()));
	}
	
	pLabel.setForeground(fg);
      }

      /* update the value graph */ 
      {
	int size = cache.getNumSamples();
	
	float vs[] = new float[size];
	int i, wk;
	for(i=size-1, wk=0; wk<size; i--, wk++) {
	  float v = 0.0f;
	  switch(pSampleType) {
	  case Load:
	    {
	      Integer numProcs = host.getNumProcessors();
	      if(numProcs != null) 
		v = cache.getLoad(i) / ((float) numProcs);
	    }
	    break;

	  case Memory:
	    {
	      Long totalMem =  host.getTotalMemory();
	      if(totalMem != null) 
		v = ((float) cache.getMemory(i)) / ((float) totalMem); 
	    }
	    break; 
	    
	  case Disk:
	    {
	      Long totalDisk = host.getTotalDisk();
	      if(totalDisk != null) 
		v = ((float) cache.getDisk(i)) / ((float) totalDisk);
	    }
	    break;
	    
	  case Jobs:
	    v = ((float) cache.getNumJobs(i)) / ((float) host.getJobSlots());
	  }

	  vs[wk] = Math.max(0.0f, Math.min(1.0f, v));
	}
	
	pBarGraph.setValues(vs, true);	
	
	switch(host.getStatus()) {
	case Enabled:
	  pBarGraph.setForeground(pBarForeground[0]);
	  pBarGraph.setHighlight(pBarHighlight[0]);
	  break;
	  
	default:
	  pBarGraph.setForeground(pBarForeground[1]);
	  pBarGraph.setHighlight(pBarHighlight[1]);
	}	  
      }
    
      return pPanel;
    }
    else {
      pNullLabel.setForeground(fg);

      return pNullLabel;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S   C L A S S E S                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  enum SampleType
  {
    /**
     * Display system load.
     */ 
    Load, 

    /**
     * Display available free memory (in GB) on the host.
     */ 
    Memory, 

    /**
     * Display available free temporary disk space (in GB) on the host.
     */ 
    Disk, 
    
    /**
     * Display the number of currently running jobs.
     */ 
    Jobs;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = ;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent table model.
   */ 
  private QueueHostsTableModel  pParent;
  
  /**
   * The type of sample being displayed.
   */ 
  private SampleType  pSampleType; 
  
  /**
   * The label to use when the value is <CODE>null</CODE>.
   */ 
  private JLabel  pNullLabel;

  /**
   * The panel containing the value label and graph. 
   */ 
  private JPanel pPanel; 

  /**
   * The label to use when the value is not <CODE>null</CODE>.
   */ 
  private JLabel  pLabel;

  /**
   * The graph of the values.
   */ 
  private JBarGraph  pBarGraph; 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Bar colors: [enabled, disabled]
   */ 
  private Color[]  pBarForeground;
  private Color[]  pBarHighlight;

}
