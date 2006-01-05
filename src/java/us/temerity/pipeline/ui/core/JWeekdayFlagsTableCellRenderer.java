// $Id: JWeekdayFlagsTableCellRenderer.java,v 1.1 2006/01/05 16:54:44 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.laf.LookAndFeelLoader;
import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   W E E K D A Y   F L A G S   T A B L E   C E L L   R E N D E R E R                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A renderer for {@link JTable JTable} cells containing boolean flags for each day of 
 * the week. 
 */ 
public
class JWeekdayFlagsTableCellRenderer
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JWeekdayFlagsTableCellRenderer() 
  {
    {
      JPanel panel = new JPanel();
      pPanel = panel;
      
      panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
      
      pLabels = new JLabel[7];
      int wk;
      for(wk=0; wk<pLabels.length; wk++) {
	if(wk > 0) 
	  panel.add(Box.createRigidArea(new Dimension(3, 0)));
	
	JLabel label = new JLabel();
	pLabels[wk] = label;
      
	label.setHorizontalAlignment(JLabel.CENTER);

	Dimension size = new Dimension(19, 19); 
	label.setMinimumSize(size);
	label.setMaximumSize(size);
	label.setPreferredSize(size);	  

	panel.add(label);
      }
    }

    {
      JLabel label = new JLabel("-");
      pNullLabel = label;
      
      label.setOpaque(true);
      label.setName("SimpleTableCellRenderer");
      
      label.setHorizontalAlignment(JLabel.CENTER);
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
   int col
  )
  {
    boolean flags[] = (boolean[]) value; 
    if(flags != null) {
      int wk;
      for(wk=0; wk<pLabels.length; wk++) {
	if((flags != null) && flags[wk]) 
	  pLabels[wk].setIcon(sWeeklySelectedIcon[wk]); 
	else 
	  pLabels[wk].setIcon(sWeeklyIcon[wk]);
      }
      return pPanel;
    }
    else {
      pNullLabel.setForeground(isSelected ? Color.yellow : Color.white);
      return pNullLabel;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

  private static final Icon[] sWeeklyIcon = {
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon0.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon1.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon2.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon3.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon4.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon5.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklyIcon0.png"))
  };

  private static final Icon[] sWeeklySelectedIcon = {
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon0.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon1.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon2.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon3.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon4.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon5.png")), 
    new ImageIcon(LookAndFeelLoader.class.getResource("WeeklySelectedIcon0.png"))
  };



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The weekday labels.
   */ 
  private JLabel[]  pLabels;

  /**
   * The containing panel.
   */ 
  private JPanel  pPanel; 


  /**
   * The label for <CODE>null</CODE> values.
   */ 
  private JLabel  pNullLabel; 

}
