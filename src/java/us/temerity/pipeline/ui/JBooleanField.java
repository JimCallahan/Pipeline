// $Id: JBooleanField.java,v 1.7 2005/01/09 17:31:42 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

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

    {
      setName("BooleanField");
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      addMouseListener(this);

      {
	add(Box.createRigidArea(new Dimension(18, 0)));
	add(Box.createHorizontalGlue());
	
	{
	  JLabel label = new JLabel();
	  pLabel = label;
	  
	  label.setName("BooleanValueTextField");
	  label.setHorizontalAlignment(JLabel.CENTER);
	  label.addMouseListener(this);
	  
	  add(label);
	}
	
	add(Box.createHorizontalGlue());
	add(Box.createRigidArea(new Dimension(3, 0)));

	{
	  JLabel label = new JLabel(sEnabledIcon);
	  pIconLabel = label;
	  add(label);
	}
	
	add(Box.createRigidArea(new Dimension(3, 0)));
      }
    }

    pListenerList  = new EventListenerList();
    pActionCommand = "value-changed";

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
   Boolean value
  ) 
  {
    pValue = value;
    if(pValue != null) 
      pLabel.setText(pValue ? "YES" : "no");
    else 
      pLabel.setText("-");

    fireActionPerformed();
  }

  /**
   * Get the boolean value.
   */ 
  public Boolean
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
    return pLabel.getText();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds the specified action listener to receive action events from this field.
   */ 
  public void
  addActionListener
  (
   ActionListener l
  )
  {
    pListenerList.add(ActionListener.class, l);
  }

  /**
   * Removes the specified action listener so that it no longer receives action events
   * from this field.
   */ 
  public void 	
  removeActionListener
  (
   ActionListener l
  )
  {
    pListenerList.remove(ActionListener.class, l);
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
    pActionCommand = command; 
  }

  /**
   * Notifies all listeners that have registered interest for notification of action events.
   */
  protected void 
  fireActionPerformed() 
  {
    ActionEvent event = null;

    Object[] listeners = pListenerList.getListenerList();
    int i;
    for(i=listeners.length-2; i>=0; i-=2) {
      if(listeners[i]==ActionListener.class) {
	if(event == null) 
	  event = new ActionEvent(this, pEventID++, pActionCommand);

	((ActionListener)listeners[i+1]).actionPerformed(event);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J C O M P O N E N T   O V E R R I D E S                                              */
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
      pLabel.addMouseListener(this);

      pIconLabel.setIcon(sEnabledIcon);
    }
    else if(!enabled && isEnabled()) {
      removeMouseListener(this);
      pLabel.removeMouseListener(this);

      pIconLabel.setIcon(sDisabledIcon);
    }

    super.setEnabled(enabled);
  }

  /**
   * Sets the foreground color of this component.
   */ 
  public void 
  setForeground
  (
   Color fg
  )
  {
    if(pLabel != null) 
      pLabel.setForeground(fg);
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
    if(pValue != null) 
      setValue(!pValue);
    else 
      setValue(true);
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
  private Boolean  pValue;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The value.
   */ 
  private JLabel  pLabel; 

  /**
   * The icon.
   */ 
  private JLabel  pIconLabel; 


  /*----------------------------------------------------------------------------------------*/

  /**
   * The action listeners registered to this object.
   */ 
  private EventListenerList pListenerList;

  /**
   * The command string passed to generated action events. 
   */ 
  private String  pActionCommand; 

  /**
   * The unique event ID.
   */ 
  private int pEventID; 
  
}
