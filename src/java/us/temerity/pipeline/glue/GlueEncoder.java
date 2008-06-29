// $Id: GlueEncoder.java,v 1.4 2008/06/29 17:46:16 jim Exp $

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



