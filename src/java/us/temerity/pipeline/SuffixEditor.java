// $Id: SuffixEditor.java,v 1.2 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U F F I X   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A specification of the default editor to be used for files with a particular suffix. <P> 
 */
public
class SuffixEditor
  implements Comparable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  SuffixEditor() 
  {}

  /**
   * Construct a new suffix/editor specification.
   * 
   * @param suffix
   *   The filename suffix
   */ 
  public 
  SuffixEditor
  (
   String suffix
  ) 
  {
    if(suffix == null) 
      throw new IllegalArgumentException("The suffix cannot be (null)!");
    pSuffix = suffix;
  }

  /**
   * Construct a new suffix/editor specification.
   * 
   * @param suffix
   *   The filename suffix.
   * 
   * @param desc
   *   The description text.
   * 
   * @param editor
   *   The default editor name.
   */ 
  public 
  SuffixEditor
  (
   String suffix, 
   String desc,
   String editor
  ) 
  {
    if(suffix == null) 
      throw new IllegalArgumentException("The suffix cannot be (null)!");
    pSuffix = suffix;

    pDescription = desc;
    pEditor      = editor;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the filename suffix.
   */ 
  public String
  getSuffix() 
  {
    assert(pSuffix != null);
    return pSuffix;
  }

  
  /**
   * Get the short description of the file format.
   * 
   * @return 
   *   The description text or <CODE>null</CODE> if none exists.
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }
  
  /**
   * Set the short description of the file format.
   * 
   * @param desc
   *   The description text.
   */ 
  public void
  setDescription
  (
   String desc
  )
  {
    pDescription = desc;
  }
  

  /**
   * Gets the name of the default editor to use for files having this filename suffix.
   * 
   * @return 
   *   The editor name or <CODE>null</CODE> if undefined.
   */ 
  public String
  getEditor() 
  {
    return pEditor;
  }

  /**
   * Sets the name of the default editor to use for files having this filename suffix.
   * 
   * @param editor
   *   The default editor name.
   */ 
  public void
  setEditor
  (
   String editor
  ) 
  {
    pEditor = editor;
  }

 

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof SuffixEditor)) {
      SuffixEditor se = (SuffixEditor) obj;
      return pSuffix.equals(se.pSuffix);
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof SuffixEditor))
      throw new IllegalArgumentException("The object to compare was NOT a SuffixEditor!");

    return compareTo((SuffixEditor) obj);
  }


  /**
   * Compares this <CODE>SuffixEditor</CODE> with the given <CODE>SuffixEditor</CODE> for 
   * order.
   * 
   * @param se 
   *   The <CODE>SuffixEditor</CODE> to be compared.
   */
  public int
  compareTo
  (
   SuffixEditor se
  )
  {
    return pSuffix.compareTo(se.pSuffix);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Suffix", pSuffix);
    
    if(pDescription != null) 
      encoder.encode("Description", pDescription);

    if(pEditor != null) 
      encoder.encode("Editor", pEditor);		     
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String suffix = (String) decoder.decode("Suffix"); 
    if(suffix == null) 
      throw new GlueException("The \"Suffix\" was missing!");
    pSuffix = suffix;

    pDescription = (String) decoder.decode("Description");     
    pEditor      = (String) decoder.decode("Editor");     
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4174194640105458661L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The filename suffix.
   */
  private String  pSuffix;        

  /**
   * The short description of the file format.
   */
  private String  pDescription;        

  /**
   * The default editor name.
   */
  private String  pEditor;        
}
