// $Id: JLinkParamTableCellEditor.java,v 1.3 2005/01/03 06:56:23 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;


/*------------------------------------------------------------------------------------------*/
/*   L I N K   P A R A M   T A B L E   C E L L   E D I T O R                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The editor for {@link JTable JTable} cells containing 
 * {@link LinkActionParam LinkActionParam} data.
 */ 
public
class JLinkParamTableCellEditor
  extends AbstractCellEditor
  implements TableCellEditor, ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new editor.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   * 
   * @param stitles
   *   The short names of the upstream nodes.
   * 
   * @param snames
   *   The fully resolved node names of the upstream nodes.
   */
  public 
  JLinkParamTableCellEditor
  (
   int width,
   ArrayList<String> stitles, 
   ArrayList<String> snames
  ) 
  {
    this(null, width, stitles, snames);
  }

  /**
   * Construct a new editor.
   * 
   * @param parent
   *   The parent dialog or <CODE>null</CODE> the field is not a child of a dialog.
   * 
   * @param width
   *   The minimum and preferred width of the field.
   * 
   * @param stitles
   *   The short names of the upstream nodes.
   * 
   * @param snames
   *   The fully resolved node names of the upstream nodes.
   */
  public 
  JLinkParamTableCellEditor
  (
   JDialog parent, 
   int width,
   ArrayList<String> stitles, 
   ArrayList<String> snames
  ) 
  {
    pNames = new ArrayList<String>(snames);
    pNames.add(null);

    {
      ArrayList<String> values = new ArrayList<String>(stitles);
      values.add("-");
    
      pField = UIFactory.createCollectionField(values, parent, width);
      pField.addActionListener(this);
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
    return pNames.get(pField.getSelectedIndex());
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
    LinkActionParam param = (LinkActionParam) value;
    
    String source = (String) param.getValue();
    int idx = pNames.indexOf(source);
    if(idx != -1) 
      pField.setSelectedIndex(idx);
    else 
      pField.setSelected("-");
        
    return pField;
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
    fireEditingStopped();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5520694713663486443L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved named of the upstream nodes which correspond to the items displayed
   * by the field.
   */ 
  private ArrayList<String>  pNames;

  /**
   * The link field editor.
   */ 
  private JCollectionField  pField;

}
