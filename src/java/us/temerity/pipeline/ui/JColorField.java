// $Id: JColorField.java,v 1.6 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L O R   F I E L D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The field which displays an editable color swatch.
 */ 
public 
class JColorField
  extends JPanel
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with an initial color.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param color
   *   The initial color. 
   */ 
  public 
  JColorField
  (
   Frame owner,  
   Color3d color
  ) 
  {
    super();
    pOwnerFrame = owner;
    initColor(color);
  }

  /**
   * Construct with an initial color.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JColorField
  (
   Dialog owner,    
   Color3d color
  ) 
  {
    super();
    pOwnerDialog = owner;
    initColor(color);
  }
  
  /**
   * Construct with an initial color.
   * 
   * @param owner
   *   The parent frame.
   * 
   * @param color
   *   The initial color. 
   */ 
  public 
  JColorField
  (
   Frame owner,  
   Color color
  ) 
  {
    super();
    pOwnerFrame = owner;
    initColor(color);
  }

  /**
   * Construct with an initial color.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JColorField
  (
   Dialog owner,    
   Color color
  ) 
  {
    super();
    pOwnerDialog = owner;
    initColor(color);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  private void 
  initColor
  (
   Color3d color
  ) 
  {
    initUI();

    pValue = new Color3d();
    setValue(color);
  }

  private void 
  initColor
  (
   Color color
  ) 
  {
    initUI();

    pValue = new Color3d();
    setColor(color);
  }

  private void 
  initUI()
  {
    pTitle = "Color Editor:";

    setLayout(new BorderLayout());
    setAlignmentX(0.5f);
    setOpaque(true);

    {
      JButton btn = new JButton();
      pButton = btn;
      btn.setName("ColorButton");
      btn.setOpaque(false);
   
      btn.addActionListener(this);
      btn.setActionCommand("edit-color");
   
      btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
   
      add(btn);
    }

    pListenerList  = new EventListenerList();
    pActionCommand = "value-changed";
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set header title to use for the color editor dialog.
   */ 
  public void 
  setDialogTitle
  (
   String title
  ) 
  {
    pTitle = title;
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the color value.
   */ 
  public Color3d
  getValue()
  {
    return new Color3d(pValue);
  }

  /**
   * Set the color value.
   * 
   * @param color
   *   The new color value.
   */ 
  public void 
  setValue
  (
   Color3d color
  )
  {
    if(color != null) 
      pValue.set(color);
    else 
      pValue.set(0.5, 0.5, 0.5);

    setBackground(getColor());

    fireActionPerformed();   
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the color value as an AWT color.
   */ 
  public Color
  getColor()
  {
    return (new Color((float) pValue.r(), (float) pValue.g(), (float) pValue.b()));
  }

  /**
   * Set the color value from an AWT color.
   * 
   * @param color
   *   The new color value.
   */ 
  public void 
  setColor
  (
   Color color
  )
  {
    if(color != null) {
      float[] c = color.getColorComponents(null);
      pValue.set((double) c[0], (double) c[1], (double) c[2]); 
    }
    else {
      pValue.set(0.5, 0.5, 0.5);
    }

    setBackground(color);
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
      pButton.addActionListener(this);
      pButton.setEnabled(true);
    }
    else if(!enabled && isEnabled()) {
      pButton.removeActionListener(this);
      pButton.setEnabled(false);
    }
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
    String cmd = e.getActionCommand();
    if(cmd.equals("edit-color")) 
      doEditColor();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Edit the field color using a color editor dialog.
   */ 
  private void 
  doEditColor() 
  {
    JColorEditorDialog diag = null;
    if(pOwnerDialog != null) 
      diag = new JColorEditorDialog(pOwnerDialog);
    else if(pOwnerFrame != null) 
      diag = new JColorEditorDialog(pOwnerFrame); 
    else 
      assert(false);
      
    diag.setHeaderTitle(pTitle);
    diag.setColor(pValue); 

    diag.setVisible(true);
    if(diag.wasConfirmed())
      setValue(diag.getColor());
  }
   
 

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2604254407873686123L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Parent window.
   */ 
  private Dialog  pOwnerDialog;
  private Frame   pOwnerFrame;

  /** 
   * The button which displays the color editor dialog.
   */ 
  private JButton  pButton; 

  /**
   * The color being displayed.
   */ 
  private Color3d  pValue; 

  /**
   * The title of the color editor dialog.
   */ 
  private String  pTitle;


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
