// $Id: JAlphaNumField.java,v 1.2 2004/06/14 22:45:08 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   A L P H A   N U M   F I E L D                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of alphanumeric characters. <P>
 */ 
public 
class JAlphaNumField
  extends JTextField 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JAlphaNumField() 
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
    return new AlphaNumDocument();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class AlphaNumDocument 
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
	if(!Character.isLetterOrDigit(cs[wk])) {
	  Toolkit.getDefaultToolkit().beep();
	  return;
	}
      }

      super.insertString(offset, str, attr);
    }

    private static final long serialVersionUID = 2246427069106044900L;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 6663082194375309654L;

}
