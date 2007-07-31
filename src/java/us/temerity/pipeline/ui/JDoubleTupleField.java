// $Id: JDoubleTupleField.java,v 1.1 2007/07/31 14:58:40 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   T U P L E   F I E L D                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A field which represents a Tuple*d value.
 */
public 
class JDoubleTupleField
  extends JPanel
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  protected
  JDoubleTupleField
  (
   int size
  ) 
  {
    super();  
    setName("JDoubleField");

    setAlignmentY(0.5f);
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    pSize = size; 
    pFields = new JDoubleField[pSize];

    int i;
    for(i=0; i<pSize; i++) {
      if(i > 0) 
        add(Box.createRigidArea(new Dimension(4, 0)));

      pFields[i] = UIFactory.createDoubleField(0.0, 35, JLabel.CENTER);
      pFields[i].addActionListener(this);
      add(pFields[i]); 
    }

    pListenerList  = new EventListenerList();
    pActionCommand = "value-changed";
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
      int i; 
      for(i=0; i<pSize; i++) {
        pFields[i].addActionListener(this);
        pFields[i].setEnabled(true);
      }
    }
    else if(!enabled && isEnabled()) {
      int i; 
      for(i=0; i<pSize; i++) {
        pFields[i].removeActionListener(this);
        pFields[i].setEnabled(false);
      }
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
    int i; 
    for(i=0; i<pSize; i++) 
      pFields[i].setWarningColor(fg);

    super.setForeground(fg);
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
    fireActionPerformed();    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6423731446744588574L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of tuple components.
   */
  protected int  pSize; 

  /**
   * The component fields.
   */ 
  protected JDoubleField[]  pFields; 


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
