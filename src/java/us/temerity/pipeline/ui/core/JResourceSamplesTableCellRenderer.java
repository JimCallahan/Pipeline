// $Id: JResourceSamplesTableCellRenderer.java,v 1.1 2005/01/03 06:56:24 jim Exp $

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
   SampleType type
  ) 
  {
    pSampleType = type; 

    Color fg = new Color(0.0f, 0.59f, 1.0f);
    Color hl = new Color(1.0f, 1.0f, 0.0f);
    String prefix = "Blue";
    switch(pSampleType) {
    case Jobs:
      fg = new Color(0.0f, 1.0f, 0.0f);
      hl = new Color(1.0f, 0.0f, 0.0f);
      prefix = "Green";
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

	bar.setForeground(fg);
	bar.setHighlight(hl);

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
    QueueHost host = (QueueHost) value; 
    List<ResourceSample> samples = host.getSamples();
    if((samples != null) && !samples.isEmpty()) { 

      /* update the numeric label */ 
      {
	ResourceSample first = samples.get(0);
	switch(pSampleType) {
	case Load:
	  pLabel.setText(String.format("%1$.1f", first.getLoad()));
	  break;		       
	  
	case Memory:
	  pLabel.setText(String.format("%1$.1f", 
				       ((double) first.getMemory()) / 1073741824.0));
	  break; 
	  
	case Disk:
	  pLabel.setText(String.format("%1$.1f", 
				       ((double) first.getDisk()) / 1073741824.0));
	  break;
	
	case Jobs:
	  pLabel.setText(String.valueOf(first.getNumJobs()));
	}

	pLabel.setForeground(isSelected ? Color.yellow : Color.white);
      }

      /* update the value graph */ 
      {
	float vs[] = new float[samples.size()];
	switch(pSampleType) {
	case Load:
	  {
	    int wk = 0;
	    for(ResourceSample sample : samples) {
	      float v = sample.getLoad() / ((float) host.getNumProcessors());
	      vs[wk] = Math.max(0.0f, Math.min(1.0f, v));
	      wk++;
	    }
	  }
	  break;		       
	  
	case Memory:
	  {
	    int wk = 0;
	    for(ResourceSample sample : samples) {
	      float v = ((float) sample.getMemory()) / ((float) host.getTotalMemory());
	      vs[wk] = Math.max(0.0f, Math.min(1.0f, v));
	      wk++;
	    }
	  }
	  break; 
	  
	case Disk:
	  {
	    int wk = 0;
	    for(ResourceSample sample : samples) {
	      float v = ((float) sample.getDisk()) / ((float) host.getTotalDisk());
	      vs[wk] = Math.max(0.0f, Math.min(1.0f, v));
	      wk++;
	    }
	  }
	  break;
	  
	case Jobs:
	  {
	    int wk = 0;
	    for(ResourceSample sample : samples) {
	      float v = ((float) sample.getNumJobs()) / ((float) host.getJobSlots());
	      vs[wk] = Math.max(0.0f, Math.min(1.0f, v));
	      wk++;
	    }
	  }
	}
	
	pBarGraph.setValues(vs, true);
      }

      return pPanel;
    }
    else {
      pNullLabel.setForeground(isSelected ? Color.yellow : Color.white);

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

}
