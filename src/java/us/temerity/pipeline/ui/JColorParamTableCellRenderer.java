// $Id: JColorParamTableCellRenderer.java,v 1.2 2008/01/20 01:38:06 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L O R   P A R A M   T A B L E   C E L L   R E N D E R E R                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable} cells containing TupleParam<Color3d> data. 
 */ 
public
class JColorParamTableCellRenderer
  extends JColorTableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param owner
   *   The owning parent dialog.
   */
  public 
  JColorParamTableCellRenderer
  (
   Dialog owner
  ) 
  {
    super(owner); 
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
    Color3dActionParam param = (Color3dActionParam) value; 

    Color3d color = null; 
    if(param != null) 
      color = param.getTupleValue();
    
    return super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1265269653718975631L;

}
