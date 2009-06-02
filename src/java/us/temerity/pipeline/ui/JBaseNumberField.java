// $Id: JBaseNumberField.java,v 1.4 2009/06/02 20:08:37 jlee Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   N U M B E R   F I E L D                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for the numeric fields.
 */ 
public abstract
class JBaseNumberField<T>
  extends JTextField 
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JBaseNumberField() 
  {
    super();

    setName("EditableTextField");

    addActionListener(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the numeric value.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if empty.
   */ 
  public abstract T
  getValue();

  /**
   * Set the numeric value.
   * 
   * @param value
   *   The value or <CODE>null</CODE> to clear.
   */ 
  public abstract void 
  setValue
  (
   T value
  );


  /**
   * Set the warning foreground color.
   */ 
  public void 
  setWarningColor
  (
   Color color
  ) 
  {
    if(color.equals(Color.cyan)) 
      setName("WarningTextField");
    else if(color.equals(Color.yellow)) 
      setName("SelectionTextField");
    else    
      setName("EditableTextField");
    
    setForeground(color);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   T E X T   F I E L D   O V E R R I D E S                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Creates the default implementation of the model to be used at construction if one 
   * isn't explicitly given.
   */ 
  protected Document 	
  createDefaultModel()
  {
    return new NumberDocument();
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
    super.setEnabled(enabled);
    setName(isEnabled() ? "EditableTextField" : "TextField");
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
    setValue(getValue());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Checks a speculative result text for validity.
   */ 
  protected abstract boolean 
  isValidResult
  (
   String text
  );



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class NumberDocument 
    extends PlainDocument 
  {
    /**
     * Removes some content from the document. 
     */ 
    public void 
    remove
    (
     int offset,
     int length
    )
      throws BadLocationException
    {
      String before = "";
      if(offset > 0) 
	before = getText(0, offset);
      
      String after = getText(offset+length, getLength()-(offset+length));

      if(isValidResult(before + after)) 
	super.remove(offset, length);
      else 
	if(UIFactory.getBeepPreference())
	  Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Inserts some content into the document. 
     */ 
    public void 
    insertString
    (
     int offset, 
     String str, 
     AttributeSet attr
    ) 
      throws BadLocationException 
    {
      if((str == null) || (str.length() == 0)) 
	return;

      String before = "";
      if(offset > 0) 
	before = getText(0, offset);

      String after = getText(offset, getLength()-offset);

      if(isValidResult(before + str + after)) 
	super.insertString(offset, str, attr);
      else 
	if(UIFactory.getBeepPreference())
	  Toolkit.getDefaultToolkit().beep();
    }


    private static final long serialVersionUID = -903864113105567861L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  //private static final long serialVersionUID = -1441125486714429565L;

}
