// $Id: JDoubleField.java,v 1.1 2004/06/14 22:55:00 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   F I E L D                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal integer values. <P>
 */ 
public 
class JDoubleField
  extends JTextField 
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
   * Get the integer value.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if empty.
   */ 
  public Double
  getValue()
  {
    String text = getText();
    if((text != null) && (text.length() > 0)) {
      try {
	return new Double(getText());
      }
      catch(NumberFormatException ex) {
      }
    }

    return null;
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
   Double value
  ) 
  {
    if(value != null) 
      setText(value.toString());
    else 
      setText("");
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
    return new DoubleDocument();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class DoubleDocument 
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

      String before = "";
      if(offset > 0) 
	before = getText(0, offset);

      String after = getText(offset, getLength()-offset);

      if(isValidResult(before + str + after)) 
	super.insertString(offset, str, attr);
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
      if(text.length() == 0) 
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

    private static final long serialVersionUID = -1072612196078397445L;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6104507511387214787L;

}
