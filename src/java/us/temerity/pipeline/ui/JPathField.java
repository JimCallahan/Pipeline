// $Id: JPathField.java,v 1.5 2007/07/31 14:52:27 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.Path;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/*------------------------------------------------------------------------------------------*/
/*   P A T H   F I E L D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A text field which only allows input of legal identifier paths. <P>
 * 
 * An identifier path may only contain one of the following characters: 
 * '<CODE>a</CODE>'-'<CODE>z</CODE>', '<CODE>A</CODE>'-'<CODE>Z</CODE>',
 * '<CODE>0</CODE>'-'<CODE>9</CODE>', '<CODE>_</CODE>', '<CODE>-</CODE>', 
 * '<CODE>.</CODE>', '<CODE>/</CODE>' <P>
 * 
 * The path must also start with a '<CODE>/</CODE>' character, not contain two or more 
 * '<CODE>/</CODE>' characters in a row and not end with a '<CODE>/</CODE>' character.
 */ 
public 
class JPathField
  extends JTextField 
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new field.
   */ 
  public 
  JPathField() 
  {
    super();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Is the current value of the field a valid path string?
   */ 
  public boolean
  isPathValid()
  {
    String text = getText();
    if((text == null) || (text.length() == 0))
      return false;
    
    String comps[] = text.split("/", -1);
    if(comps.length > 0) {
      if(comps[0].length() > 0) 
	return false;	
      
      int wk;
      for(wk=1; wk<(comps.length-1); wk++) {
	if(comps[wk].length() == 0) 
	  return false;	
      }
    }

    return true;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current value as a Path or <CODE>null</CODE> if unset or not a valid path. 
   */ 
  public Path
  getPath() 
  {
    if(!isPathValid()) 
      return null;

    String text = getText();
    if(text == null) 
      return null;

    return new Path(text);
  }

  /**
   * Set the current value as a Path or <CODE>null</CODE> to unset.
   */ 
  public void
  setPath
  (
   Path path
  ) 
  {
    if(path != null)
      setText(path.toString()); 
    else 
      setText(null); 
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
    return new PathDocument();
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private 
  class PathDocument 
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
      char[] cs = text.toCharArray();
      int wk;
      for(wk=1; wk<cs.length; wk++) {
	if(!(Character.isLetterOrDigit(cs[wk]) || 
	     (cs[wk] == '_') || (cs[wk] == '-') || (cs[wk] == '.') || (cs[wk] == '/'))) 
	  return false;
      }
    
      return true;
    }      

    private static final long serialVersionUID = 5609555271270203955L;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8036828987671944771L;

}
