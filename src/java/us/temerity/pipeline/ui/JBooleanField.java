// $Id: JBooleanField.java,v 1.4 2004/06/19 00:31:57 jim Exp $

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
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
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
	JValueField field = new JValueField(this);
	pTextField = field;

	field.setName("BooleanValueTextField");
	field.setHorizontalAlignment(JLabel.CENTER);
	field.setEditable(false);
	field.addMouseListener(this);

	add(field);
      }
      
      add(Box.createHorizontalGlue());

      {
	JLabel label = new JLabel(sEnabledIcon);
	pIconLabel = label;
	add(label);
      }

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
    pTextField.setText(pValue ? "YES" : "no");

    pTextField.fireActionPerformed2();
  }

  /**
   * Get the boolean value.
   */ 
  public boolean
  getValue() 
  {
    return pValue;
  }


  /**
   * Get the text value.
   */ 
  public String
  getText() 
  {
    return pTextField.getText();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets whether or not this component is enabled.
   */ 
  public void 
  setEnabled
  (
   boolean enabled
  )
  {
    if(enabled && !isEnabled()) {
      addMouseListener(this);
      pTextField.addMouseListener(this);
      
      pTextField.setText(pValue ? "YES" : "no");
      pIconLabel.setIcon(sEnabledIcon);
    }
    else if(!enabled && isEnabled()) {
      removeMouseListener(this);
      pTextField.removeMouseListener(this);

      pTextField.setText("-");
      pIconLabel.setIcon(sDisabledIcon);
    }

    super.setEnabled(enabled);
  }


  /**
   * Adds the specified action listener to receive action events from this collection field.
   */ 
  public void
  addActionListener
  (
   ActionListener l
  )
  {
    pTextField.addActionListener(l);
  }

  /**
   * Removes the specified action listener so that it no longer receives action events
   * from this collection field.
   */ 
  public void 	
  removeActionListener
  (
   ActionListener l
  )
  {
    pTextField.removeActionListener(l);
  }
          
  /**
   * Sets the command string used for action events.
   */ 
  public void 	
  setActionCommand
  (
   String command
  )
  {
    pTextField.setActionCommand(command);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sets the foreground color of this component.
   */ 
  public void 
  setForeground
  (
   Color fg
  )
  {
    if(pTextField != null) 
      pTextField.setForeground(fg);
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  public
  class JValueField
    extends JTextField
  {
    public 
    JValueField
    (
     JBooleanField parent
    ) 
    {
      pParent = parent;
    }    

    public JBooleanField
    getParent()
    {
      return pParent;
    }

    public void 
    fireActionPerformed2()
    {
      fireActionPerformed();
    }

    private static final long serialVersionUID = 133532305819177444L;

    private JBooleanField  pParent;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2376760029398653726L;


  private static Icon sEnabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("BooleanIcon.png"));

  private static Icon sDisabledIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("BooleanIconDisabled.png"));


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The underlying boolean value.
   */ 
  private boolean  pValue;


  /**
   * The value text field.
   */ 
  private JValueField  pTextField;

  /**
   * The icon.
   */ 
  private JLabel  pIconLabel; 



}
