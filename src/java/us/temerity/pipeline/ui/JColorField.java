// $Id: JColorField.java,v 1.3 2004/12/29 17:32:38 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.math.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

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
   * Construct with a grey color.
   */ 
  public 
  JColorField() 
  {
    this(new Color3d(0.5, 0.5, 0.5));
  }
  
  /**
   * Construct with an initial color.
   */ 
  public 
  JColorField
  (
   Color3d color
  ) 
  {
    super();
    
    pValue = new Color3d();
    setValue(color);

    pTitle = "Color Editor:";

    initUI();
  }

  /**
   * Construct with an initial AWT color.
   */ 
  public 
  JColorField
  (
   Color color
  ) 
  {
    super();
    
    pValue = new Color3d();
    setColor(color);

    pTitle = "Color Editor:";

    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize the common user interface components. <P> 
   */ 
  private void 
  initUI()
  {
    setLayout(new BorderLayout());
    setAlignmentX(0.5f);
    setOpaque(true);

    {
      JButton btn = new JButton();
      btn.setName("ColorButton");
      btn.setOpaque(false);
   
      btn.addActionListener(this);
      btn.setActionCommand("edit-color");
   
      btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
   
      add(btn);
    }
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
    JColorEditorDialog diag = UIMaster.getInstance().showColorEditorDialog(pTitle, pValue);
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
   * The color being displayed.
   */ 
  private Color3d  pValue; 

  /**
   * The title of the color editor dialog.
   */ 
  private String  pTitle;

}
