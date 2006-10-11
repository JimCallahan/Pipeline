// $Id: JJobStatesTableCellRenderer.java,v 1.2 2006/10/11 06:09:39 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

import java.awt.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S T A T E S   T A B L E   C E L L   R E N D E R E R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells displaying horizontal bar graphs of the 
 * breakdown of @{link JobState JobState} counts associated with a @{link JobGroup JobGroup}. 
 * <P> 
 */ 
public
class JJobStatesTableCellRenderer
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JJobStatesTableCellRenderer() 
  {
    JPanel panel = new JPanel();
    pPanel = panel;

    panel.setLayout(new BorderLayout());
    panel.setName("JobStatesTableCellRenderer");  
      
    {
      JProportionGraph graph = new JProportionGraph();
      pGraph = graph;

      panel.add(graph);
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
    int[] counts = (int[]) value; 
    
    int wk;
    float[] props = new float[counts.length];
    for(wk=0; wk<props.length; wk++) 
      props[wk] = (float) counts[wk];
    
    float total = 0.0f;
    for(wk=0; wk<props.length; wk++) 
      total += props[wk];
    
    if(total > 0.0f) {
      for(wk=0; wk<props.length; wk++)
	props[wk] /= total;
      
      pGraph.setValues(props, NodeStyles.getJobColors());
    }
    else {
      pGraph.setValues(null, null);
    }
    
    return pPanel;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = ;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The wrapping panel. 
   */ 
  private JPanel  pPanel; 

  /**
   * The horizontal graph showing state colors bars in proportion to the state counts.
   */ 
  private JProportionGraph pGraph; 

}
