// $Id: JJobSlotsTableCellEditor.java,v 1.1 2005/01/03 06:56:24 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   S L O T S   T A B L E   C E L L   E D I T O R                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells job slots.
 */ 
public
class JJobSlotsTableCellEditor
  extends JIntegerTableCellEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   */ 
  public 
  JJobSlotsTableCellEditor() 
  {
    super(60, JLabel.CENTER);
    pField.setName("GreenEditableTextField");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1990773279186031109L;

}
