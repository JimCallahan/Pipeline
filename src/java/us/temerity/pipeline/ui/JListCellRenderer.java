// $Id: JListCellRenderer.java,v 1.1 2004/05/08 15:13:09 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   C E L L   R E N D E R E R                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JList JList} cells.
 */ 
public
class JListCellRenderer
  extends JLabel 
  implements ListCellRenderer 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new renderer.
   */
  public 
  JListCellRenderer() 
  {
    setOpaque(true);
    setBackground(new Color(0.45f, 0.45f, 0.45f));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R E N D E R I N G                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generates the component to be displayed for both popup list and combo box field. 
   */ 
  public Component 
  getListCellRendererComponent
  (
   JList list,
   Object value,
   int index,
   boolean isSelected,
   boolean cellHasFocus
  )
  {
    assert(value != null);
    setText(value.toString());
    
    setHorizontalAlignment(JLabel.LEFT);
    if(isSelected) {
      setForeground(Color.yellow);
      setIcon(sSelectedIcon);
    }
    else {
      setIcon(sNormalIcon);
      setForeground(Color.white);
    }
    
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9144374790563937303L;


  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
