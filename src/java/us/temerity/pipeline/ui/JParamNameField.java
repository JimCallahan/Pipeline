// $Id: JParamNameField.java,v 1.3 2009/06/04 09:26:58 jim Exp $

package us.temerity.pipeline.ui;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.*;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   P A R A M   N A M E   F I E L D                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal param names. <P>
 * 
 * A param name may only contain one of the following characters: 
 * '<CODE>a</CODE>'-'<CODE>z</CODE>', '<CODE>A</CODE>'-'<CODE>Z</CODE>',
 * '<CODE>0</CODE>'-'<CODE>9</CODE>' <P>
 */ 
public 
class JParamNameField
  extends JTextField 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JParamNameField() 
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
    @Override
    public void 
    insertString
    (
     int offset, 
     String str, 
     AttributeSet attr
    ) 
      throws BadLocationException 
    {
      if(str == null) 
	return;
      
      if(!Identifiers.hasAlphaNumericChars(str)) {
        if(UIFactory.getBeepPreference())
          Toolkit.getDefaultToolkit().beep();
        return;
      }

      super.insertString(offset, str, attr);
    }

    private static final long serialVersionUID = 3333117855756336544L;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2490336127084575013L;
}
