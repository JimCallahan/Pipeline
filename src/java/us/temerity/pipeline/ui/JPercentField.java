// $Id: JPercentField.java,v 1.1 2009/12/11 23:29:39 jesse Exp $

package us.temerity.pipeline.ui;

import java.text.*;


/*------------------------------------------------------------------------------------------*/
/*   P E R C E N T   F I E L D                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal Double values as percentages. <P>
 */ 
public 
class JPercentField
  extends JBaseNumberField<Double>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   * 
   * @param decimalPlaces
   *   The number of decimal places to display or <code>null</code> if no limit is 
   *   desired.
   */ 
  public 
  JPercentField
  (
    Integer decimalPlaces  
  ) 
  {
    super();

    NumberFormat fmt = NumberFormat.getNumberInstance();
    fmt.setMinimumIntegerDigits(1);
    fmt.setMinimumFractionDigits(1);
    if (decimalPlaces != null)
      fmt.setMaximumFractionDigits(decimalPlaces);
    pFormat = fmt;
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
  @Override
  public Double
  getValue()
  {
    String text = getText();
    if((text != null) && (text.length() > 0) && !text.equals("-")) {
      try {
	return new Double(getText())/100d;
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
  @Override
  public void 
  setValue
  (
   Double value
  ) 
  {
    Double v = value;
    if (v != null)
      v *=100;
    
    if(v != null) 
      setText(pFormat.format(v));
    else 
      setText("-");
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Checks a speculative result text for validity.
   */ 
  @Override
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private NumberFormat pFormat;
}
