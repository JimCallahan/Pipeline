// $Id: JWeekdayFlagsTableCellEditor.java,v 1.1 2006/01/05 16:54:44 jim Exp $

package us.temerity.pipeline.ui.core;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   W E E K D A Y   F L A G S   T A B L E   C E L L   E D I T O R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing boolean flags for each day of 
 * the week. 
 */ 
public
class JWeekdayFlagsTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   */
  public 
  JWeekdayFlagsTableCellEditor() 
  {
    JPanel panel = new JPanel();
    pPanel = panel;
    
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

    pChecks = new JCheckBox[7];
    int wk;
    for(wk=0; wk<pChecks.length; wk++) {
      if(wk > 0) 
	panel.add(Box.createRigidArea(new Dimension(3, 0)));

      JCheckBox check = new JCheckBox();
      pChecks[wk] = check; 
    
      check.setName("WeeklyCheck" + wk);
    
      check.addActionListener(this);
    
      Dimension size = new Dimension(19, 19); 
      check.setMinimumSize(size);
      check.setMaximumSize(size);
      check.setPreferredSize(size);	  

      panel.add(check);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the value contained in the editor.
   */ 
  public Object 
  getCellEditorValue() 
  {
    boolean[] flags = new boolean[7];
    int wk;
    for(wk=0; wk<pChecks.length; wk++) 
      flags[wk] = pChecks[wk].isSelected();

    return flags;
  }

  /**
   * Sets an initial value for the editor.
   */ 
  public Component 	
  getTableCellEditorComponent
  (
   JTable table, 
   Object value, 
   boolean isSelected, 
   int row, 
   int column
  )
  {
    boolean[] flags = (boolean[]) value;
    
    int wk;
    for(wk=0; wk<pChecks.length; wk++) 
      pChecks[wk].setSelected((flags != null) && flags[wk]); 

    return pPanel;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    fireEditingStopped();  // replace this with a mouse exit event listener on pPanel... 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7843036967645716646L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The check boxes. 
   */ 
  private JCheckBox[]  pChecks;

  /**
   * The containing panel.
   */ 
  private JPanel  pPanel; 

}
