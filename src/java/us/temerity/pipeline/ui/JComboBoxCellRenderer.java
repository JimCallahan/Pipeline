// $Id: JComboBoxCellRenderer.java,v 1.4 2004/05/02 12:12:23 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import javax.swing.*;
import javax.swing.border.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M B O   B O X   C E L L   R E N D E R E R                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The renderer used for the {@link JComboBox JComboBox} field and popup menu items.
 */ 
public
class JComboBoxCellRenderer
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
  JComboBoxCellRenderer() 
  {
    setOpaque(true);
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
    
    if(index == -1) {
      setHorizontalAlignment(JLabel.CENTER);
      setForeground(new Color(1.0f, 1.0f, 0.0f));
      setBackground(new Color(0.45f, 0.45f, 0.45f));
      setIcon(null);
    }
    else {
      setHorizontalAlignment(JLabel.LEFT);
      setBackground(new Color(0.5f, 0.5f, 0.5f));
      if(isSelected) {
	setForeground(new Color(1.0f, 1.0f, 0.0f));
	setIcon(sSelectedIcon);
      }
      else {
	setIcon(sNormalIcon);
	setForeground(new Color(1.0f, 1.0f, 1.0f));
      }
    }    
    
    return this;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C    I N T E R N A L S                                                     */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 584004318062788314L;


  private static Icon sNormalIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellNormalIcon.png"));

  private static Icon sSelectedIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("ListCellSelectedIcon.png"));

}
