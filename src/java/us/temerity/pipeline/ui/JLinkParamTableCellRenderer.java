// $Id: JLinkParamTableCellRenderer.java,v 1.2 2004/06/28 23:03:47 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   P A R A M   T A B L E   C E L L   R E N D E R E R                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer for {@link JTable JTable} cells containing 
 * {@link LinkActionParam LinkActionParam} data.
 */ 
public
class JLinkParamTableCellRenderer
  extends JLabel 
  implements TableCellRenderer
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   * 
   * @param stitles
   *   The short names of the upstream nodes.
   * 
   * @param snames
   *   The fully resolved node names of the upstream nodes.
   */
  public 
  JLinkParamTableCellRenderer
  (
   ArrayList<String> stitles, 
   ArrayList<String> snames
  ) 
  {
    pTitles = stitles; 
    pNames  = snames;

    setOpaque(true);
    setName("SimpleTableCellRenderer");

    setHorizontalAlignment(JLabel.CENTER);
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
    int idx = -1;
    if(value != null) {
      LinkActionParam param = (LinkActionParam) value;
      idx = pNames.indexOf(param.getStringValue());
    }

    if(idx == -1) 
      setText("-");
    else 
      setText(pTitles.get(idx));

    setForeground(isSelected ? Color.yellow : Color.white);

    return this;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3708385337158752099L;
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the upstream nodes.
   */ 
  private ArrayList<String>  pNames;

  /**
   * The short names of the upstream nodes.
   */ 
  private ArrayList<String>  pTitles;


}
