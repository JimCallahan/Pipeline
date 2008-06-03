// $Id: JLockedVersionTableCellEditor.java,v 1.1 2008/06/03 17:47:01 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   L O C K E D  V E R S I O N   T A B L E   C E L L   E D I T O R                         */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells who's {@link String String} value can 
 * only be one of the members of a {@link Collection Collection<String>}.
 */ 
public
class JLockedVersionTableCellEditor
  extends JCollectionTableCellEditor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param values
   *   The choice values.
   * 
   * @param model
   *   The table model for the cell being edited.
   * 
   * @param width
   *   The horizontal size.
   */
  public 
  JLockedVersionTableCellEditor
  (
   Collection<String> values,
   PackedNodeTableModel model, 
   int width
  ) 
  {
    super(values, width); 
    
    pTableModel = model; 
  }




  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
    ArrayList<String> vstrs = new ArrayList<String>();
    TreeSet<VersionID> vids = pTableModel.getLockableVersionIDs(row);
    if((vids != null) && (!vids.isEmpty())) {
      for(VersionID vid : vids) 
        vstrs.add(vid.toString());

      Collections.reverse(vstrs);
    }
    else {
      vstrs.add("-"); 
    }
    pField.setValues(vstrs);

    return super.getTableCellEditorComponent(table, value, isSelected, row, column); 
  }




  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1544385368855890596L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table model for the cell being edited.
   */ 
  private PackedNodeTableModel pTableModel;

}
