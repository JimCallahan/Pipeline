// $Id: JByteSizeField.java,v 1.1 2004/06/19 00:36:29 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   F I E L D                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which represents an integer quantity of bytes. <P> 
 * 
 * The field only accepts legal integer values with an optional size suffix of: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 * <TABLE style="width: 20%; text-align: left;" border="1" cellpadding="2" cellspacing="2">
 *   <TBODY>
 *     <TR>
 *       <TD style="text-align: center; vertical-align: top;">Suffix<br></TD>
 *       <TD style="text-align: center; vertical-align: top;">Multiplier<br></TD>
 *     </TR>
 *     <TR>
 *       <TD style="text-align: center; vertical-align: top;">"K"<br></TD>
 *       <TD style="text-align: right; vertical-align: top;">1024<br></TD>
 *     </TR>
 *     <TR>
 *       <TD style="text-align: center; vertical-align: top;">"M"<br></TD>
 *       <TD style="text-align: right; vertical-align: top;">1048576<br></TD>
 *     </TR>
 *     <TR>
 *       <TD style="text-align: center; vertical-align: top;">"G"<br></TD>
 *       <TD style="text-align: right; vertical-align: top;">1073741824<br></TD>
 *     </TR>
 *   </TBODY>
 * </TABLE></DIV><P>
 *
 * If a size Suffix character is present in field text, the value returned by 
 * {@link #getValue getValue} will be scaled by the corresponding Multiplier.  <P> 
 * 
 * When the value of the field is set using {@link #setValue setValue} integer value 
 * displayed will be divided by the largest Multiplier which is an even divisor of the 
 * value along with the corresponding Suffix character.
 */ 
public 
class JByteSizeField
  extends JTextField 
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * ConsTRuct a new field.
   */ 
  public 
  JByteSizeField() 
  {
    super();

    addActionListener(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the integer value.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if empty or invalid.
   */ 
  public Long
  getValue()
  {
    return stringToLong(getText());
  }

  /**
   * Set the integer value.
   * 
   * @param value
   *   The integer value or <CODE>null</CODE> to clear.
   */ 
  public void 
  setValue
  (
   Long value
  ) 
  {
    setText(longToString(value));
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
    return new IntegerDocument();
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
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class IntegerDocument 
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

      String ustr = null;
      {
	char cs[] = str.toCharArray();
	int wk;
	for(wk=0; wk<cs.length; wk++) 
	  cs[wk] = Character.toUpperCase(cs[wk]);
	ustr = new String(cs); 
      }

      String before = "";
      if(offset > 0) 
	before = getText(0, offset);

      String after = getText(offset, getLength()-offset);

      if(isValidResult(before + ustr + after)) 
	super.insertString(offset, ustr, attr);
      else 
	Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Checks a speculative result text for validity.
     */ 
    private boolean 
    isValidResult
    (
     String text
    ) 
    {
      try {
	stringToLong(text);
	return true;
      }
      catch(NumberFormatException ex) {
	return false;
      }
    }      

    private static final long serialVersionUID = -34742317502709134L;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E S I O N                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Convert the given byte size String to a Long value.
   * 
   * @param text
   *   The byte size string.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the given string is <CODE>null</CODE> or empty.
   * 
   * @throws NumberFormatException
   *   If the given string is invalid.
   */ 
  public static Long
  stringToLong
  (
   String text
  )
    throws NumberFormatException
  {
    if((text != null) && (text.length() > 0)) {
      String istr = text;
      long scale = 1;
      if(text.endsWith("K")) {
	istr = text.substring(0, text.length()-1);
	scale = 1024L;
      }
      else if(text.endsWith("M")) {
	istr = text.substring(0, text.length()-1);
	scale = 1048576L;
      }
      else if(text.endsWith("G")) {
	istr = text.substring(0, text.length()-1);
	scale = 1073741824L;
      }

      Long value = new Long(istr);
      if(value < 0) 
	throw new NumberFormatException();

      return (value * scale);
    }

    return null;
  }

  /**
   * Convert the given Long value into a byte size String.
   * 
   * @param value
   *   The integer value or <CODE>null</CODE> to clear.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the given value is <CODE>null</CODE>..
   */ 
  public static String
  longToString
  (
   Long value
  ) 
  {
    if(value != null) {
      if((value % 1073741824L) == 0) 
	return ((value / 1073741824L) + "G");
      else if((value % 1048576L) == 0) 
	return ((value / 1048576L) + "M");
      else if((value % 1024L) == 0) 
	return ((value / 1024L) + "K");
      else 
	return (value.toString());
    }
    else {
      return ("");
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1441125486714429565L;

}
