// $Id: JBooleanField.java,v 1.1 2004/05/16 19:21:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   F I E L D                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a boolean value.  
 */
public 
class JBooleanField
  extends JPanel
  implements MouseListener
{
  public 
  JBooleanField()
  {
    super();  
    setName("BooleanField");

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    addMouseListener(this);

    {
      add(Box.createHorizontalGlue());
      
      {
	JLabel label = new JLabel();
	pLabel = label;
	
	add(label);
      }
      
      add(Box.createHorizontalGlue());
      add(new JLabel(sBooleanIcon));
      add(Box.createRigidArea(new Dimension(8, 0)));
    }
      
    setValue(false);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the boolean value.
   */ 
  public void 
  setValue
  (
   boolean tf
  ) 
  {
    pValue = tf;
    pLabel.setText(pValue ? "YES" : "no");
  }

  /**
   * Get the boolean value.
   */ 
  public boolean
  getValue() 
  {
    return pValue;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) 
  {
    setValue(!pValue);
  }    

  /**
   * Invoked when the mouse enters a component. 
   */
  public void 
  mouseEntered(MouseEvent e) {}

  /**
   * Invoked when the mouse exits a component. 
   */ 
  public void 
  mouseExited(MouseEvent e) {}

  /**
   * Invoked when a mouse button has been pressed on a component. 
   */
  public void 
  mousePressed(MouseEvent e) {} 

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2376760029398653726L;


  private static Icon sBooleanIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("BooleanIcon.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The text label.
   */ 
  private JLabel  pLabel;

  /**
   * The underlying boolean value.
   */ 
  private boolean  pValue;

}
