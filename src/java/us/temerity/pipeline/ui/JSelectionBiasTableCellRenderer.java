// $Id: JSelectionBiasTableCellRenderer.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   S E L E C T I O N   B I A S   T A B L E   C E L L   R E N D E R E R                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cell containing selection biases. 
 */ 
public
class JSelectionBiasTableCellRenderer
  extends JSimpleTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */ 
  public 
  JSelectionBiasTableCellRenderer() 
  {
    super(JLabel.CENTER);

    setName("PurpleTableCellRenderer");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -780227815814676657L;

}
