// $Id: JFloatField.java,v 1.3 2004/06/23 22:31:07 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   F L O A T   F I E L D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal Float values. <P>
 */ 
public 
class JFloatField
  extends JBaseNumberField<Float>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JFloatField() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Float value.
   * 
   * @return 
   *   The Float value or <CODE>null</CODE> if empty.
   */ 
  public Float
  getValue()
  {
    String text = getText();
    if((text != null) && (text.length() > 0) && !text.equals("-")) {
      try {
	return new Float(getText());
      }
      catch(NumberFormatException ex) {
      }
    }

    return null;
  }

  /**
   * Set the Float value.
   * 
   * @param value
   *   The Float value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Float value
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
    
    char cs[] = text.toCharArray();
    int wk;
    for(wk=0; wk<cs.length; wk++) {
      if(!(Character.isDigit(cs[wk]) || (cs[wk] == '.') || (cs[wk] == '-')))
	return false;
    }
    
    try {
      Float value = new Float(text);
      return true;
    }
    catch(NumberFormatException ex) {
      return false;
    }
  }      



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7973320176011246090L;

}
