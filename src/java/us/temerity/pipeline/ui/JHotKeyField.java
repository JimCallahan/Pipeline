// $Id: JHotKeyField.java,v 1.6 2005/01/09 23:12:05 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   H O T   K E Y   F I E L D                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An editor of {@link HotKey HotKey} values.
 */
public 
class JHotKeyField
  extends JLabel
  implements FocusListener, MouseListener, KeyListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JHotKeyField()
  {
    super();  

    {
      setHorizontalAlignment(JLabel.CENTER);
      setAlignmentX(0.5f);
      
      setFocusable(true);
      addFocusListener(this);
      addMouseListener(this);
      addKeyListener(this);

      setText("-"); 
    }

    pListenerList = new EventListenerList();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Set the hot key to display.
   */ 
  public void 
  setHotKey
  (
   HotKey key
  ) 
  {
    setHotKeyNoAction(key);
    fireActionPerformed(); 
  }

  /**
   * Set the hot key to display without firing an action event.
   */ 
  public void 
  setHotKeyNoAction
  (
   HotKey key
  ) 
  {
    pHotKey = key;
    setText((key != null) ? key.toString() : "-");
  }

  /**
   * Get the hot key displayed.
   * 
   * @return 
   *   The hot key or <CODE>null</CODE> if unset.
   */ 
  public HotKey
  getHotKey() 
  {
    return pHotKey;
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
	  event = new ActionEvent(this, pEventID++, "hot-key-changed"); 

	((ActionListener)listeners[i+1]).actionPerformed(event);
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- FOCUS LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when a component gains the keyboard focus.
   */ 
  public void 	
  focusGained
  (
   FocusEvent e
  )
  {
    repaint();
  }

  /**
   * Invoked when a component loses the keyboard focus.
   */ 
  public void 	
  focusLost
  (
   FocusEvent e
  )
  {
    repaint();
  }


  /*-- MOUSE LISTENER METHODS --------------------------------------------------------------*/

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on a component. 
   */ 
  public void 
  mouseClicked(MouseEvent e) {}
    
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
  mousePressed
  (
   MouseEvent e
  )
  {
    requestFocusInWindow();
  }

  /**
   * Invoked when a mouse button has been released on a component. 
   */ 
  public void 
  mouseReleased(MouseEvent e) {}


  /*-- KEY LISTENER METHODS ----------------------------------------------------------------*/

  /**
   * voked when a key has been pressed.
   */   
  public void 
  keyPressed
  (
   KeyEvent e
  )
  {
    switch(e.getKeyCode()) {
    case KeyEvent.VK_SHIFT:
    case KeyEvent.VK_ALT:
    case KeyEvent.VK_CONTROL:
      break;

    case KeyEvent.VK_DELETE:
      setHotKey(null);
      KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
      break;
      
    default:
      setHotKey(new HotKey(e));    
    }
  }

  /**
   * Invoked when a key has been released.
   */ 
  public void 	
  keyReleased(KeyEvent e) {}

  /**
   * Invoked when a key has been typed.
   */ 
  public void 	
  keyTyped(KeyEvent e) {} 

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2977925171510698518L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hot key being displayed.
   */ 
  private HotKey  pHotKey;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The action listeners registered to this object.
   */ 
  private EventListenerList pListenerList;

  /**
   * The unique event ID.
   */ 
  private int pEventID; 

}
