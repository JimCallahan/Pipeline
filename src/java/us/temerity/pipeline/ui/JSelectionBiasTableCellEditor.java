// $Id: JSelectionBiasTableCellEditor.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   B I A S   T A B L E   C E L L   E D I T O R                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells selection biases. 
 */ 
public
class JSelectionBiasTableCellEditor
  extends JIntegerTableCellEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   */ 
  public 
  JSelectionBiasTableCellEditor() 
  {
    super(135, JLabel.CENTER);
    pField.setName("PurpleEditableTextField");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1509229509476352325L;

}
