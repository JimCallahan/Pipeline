// $Id: GlueEncoder.java,v 1.3 2004/05/08 23:28:49 jim Exp $

package us.temerity.pipeline.glue;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   E N C O D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Converts a set of objects into Glue format text files. <P> 
 * 
 * The Glue format is flexible enough to handle adding, removing and renaming of fields.  
 * All primitive types and well as most of the classes in java.lang and java.util are 
 * supported natively. All other classes can add Glue support by implementing the 
 * {@link Glueable Glueable} interface.
 * 
 * @see Glueable
 * @see GlueDecoder
 */
public
interface GlueEncoder
{     
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets a <CODE>String</CODE> containing the Glue representation of the encoded objects.
   */
  public String 
  getText();



  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Encode an arbitrarily typed Object as Glue format text at the current Glue scope. <P> 
   * 
   * This method is used by objects implementing the {@link Glueable Glueable} interface 
   * to encode their fields from within 
   * {@link Glueable#toGlue(GlueEncoder) Glueable.toGlue}.
   * 
   * @param title 
   *   The name to be given to the object when encoded.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be encoded.
   */ 
  public void 
  encode
  (
   String title,    
   Object obj       
  ) 
    throws GlueException;
 
}



