// $Id: JNodeIdentifierField.java,v 1.1 2005/04/30 01:02:20 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   I D E N T I F I E R   F I E L D                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal node identifiers. <P>
 * 
 * An identifier may only contain one of the following characters: 
 * '<CODE>a</CODE>'-'<CODE>z</CODE>', '<CODE>A</CODE>'-'<CODE>Z</CODE>',
 * '<CODE>0</CODE>'-'<CODE>9</CODE>', '<CODE>_</CODE>', '<CODE>-</CODE>' <P>
 */ 
public 
class JNodeIdentifierField
  extends JTextField 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JNodeIdentifierField() 
  {
    super();
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
    return new IdentifierDocument();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class IdentifierDocument 
    extends PlainDocument 
  {
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
      if(str == null) {
	return;
      }
      
      char[] cs = str.toCharArray();
      int wk;
      for(wk=0; wk<cs.length; wk++) {
	if(!(Character.isLetterOrDigit(cs[wk]) || (cs[wk] == '_') ||(cs[wk] == '-'))) {
	  Toolkit.getDefaultToolkit().beep();
	  return;
	}
      }

      super.insertString(offset, str, attr);
    }

    private static final long serialVersionUID = -5553701934901463032L;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6581802923163598599L;

}
