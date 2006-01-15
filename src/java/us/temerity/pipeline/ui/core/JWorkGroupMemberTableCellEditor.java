// $Id: JWorkGroupMemberTableCellEditor.java,v 1.1 2006/01/15 06:29:26 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.ui.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K   G R O U P   M E M B E R   T A B L E   C E L L   E D I T O R                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing {@link Boolean Boolean} data.
 */ 
public
class JWorkGroupMemberTableCellEditor
  extends JBooleanTableCellEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   */
  public 
  JWorkGroupMemberTableCellEditor
  (
   int width
  ) 
  {
    super(width, JLabel.CENTER, true);

    ArrayList<String> values = new ArrayList<String>();
    values.add("MANAGER");
    values.add("Member");
    values.add("-");
    
    pField.setValues(values);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7634655698546872411L;

}
