// $Id: GlueDecoder.java,v 1.4 2008/06/29 17:46:16 jim Exp $

package us.temerity.pipeline.glue;

/*------------------------------------------------------------------------------------------*/
/*   G L U E   D E C O D E R                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Intantiates a set of objects read from Glue format text files. <P> 
 * 
 * The Glue format is flexible enough to handle adding, removing and renaming of fields.  
 * All primitive types and well as most of the classes in java.lang and java.util are 
 * supported natively. All other classes can add Glue support by implementing the 
 * {@link Glueable Glueable} interface.
 * 
 * @see Glueable
 * @see GlueEncoder
 */
public
interface GlueDecoder
{      
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup an decoded <CODE>Object</CODE> with the given title from the current 
   * Glue scope. <P> 
   * 
   * This method is used by objects implementing the {@link Glueable Glueable} interface 
   * to initialize their fields from within 
   * {@link Glueable#fromGlue(GlueDecoder) Glueable.fromGlue}.
   * 
   * @return
   *   The decoded <CODE>Object</CODE> or <CODE>null</CODE> if no object with the given 
   *   title exists at the current Glue scope.
   */ 
  public Object
  decode
  ( 
   String title 
  ); 

}



