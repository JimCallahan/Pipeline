// $Id: JIntegerField.java,v 1.5 2004/06/23 22:31:07 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   F I E L D                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal Integer values. <P>
 */ 
public 
class JIntegerField
  extends JBaseNumberField<Integer>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JIntegerField() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Integer value.
   * 
   * @return 
   *   The Integer value or <CODE>null</CODE> if empty.
   */ 
  public Integer
  getValue()
  {
    String text = getText();
    if((text != null) && (text.length() > 0) && !text.equals("-")) {
      try {
	return new Integer(getText());
      }
      catch(NumberFormatException ex) {
      }
    }

    return null;
  }

  /**
   * Set the Integer value.
   * 
   * @param value
   *   The Integer value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Integer value
  ) 
  {
    if(value != null) 
      setText(value.toString());
    else 
      setText("-");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Checks a speculative result text for validity.
   */  
  protected boolean 
  isValidResult 
  (
   String text
  ) 
  {
    if((text.length() == 0) || text.equals("-"))
      return true;
    
    try {
      Integer value = new Integer(text);
      return true;
    }
    catch(NumberFormatException ex) {
      return false;
    }
  }      


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1441125486714429565L;

}
