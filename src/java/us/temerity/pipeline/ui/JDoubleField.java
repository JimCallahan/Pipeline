// $Id: JDoubleField.java,v 1.3 2004/06/23 22:31:07 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   F I E L D                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal Double values. <P>
 */ 
public 
class JDoubleField
  extends JBaseNumberField<Double>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JDoubleField() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Double value.
   * 
   * @return 
   *   The Double value or <CODE>null</CODE> if empty.
   */ 
  public Double
  getValue()
  {
    String text = getText();
    if((text != null) && (text.length() > 0) && !text.equals("-")) {
      try {
	return new Double(getText());
      }
      catch(NumberFormatException ex) {
      }
    }

    return null;
  }

  /**
   * Set the Double value.
   * 
   * @param value
   *   The Double value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Double value
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
      Double value = new Double(text);
      return true;
    }
    catch(NumberFormatException ex) {
      return false;
    }
  }      



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6104507511387214787L;

}
