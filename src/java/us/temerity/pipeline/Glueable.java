// $Id: Glueable.java,v 1.1 2004/02/12 15:50:12 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   G L U E A B L E                                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * An interface supporting the translation between the representation of Objects and a 
 * human readable text representation called GLUE. <BR>

 * Primitive types (int, float, boolean, ...) and their wrapper classes (Integer, Float, 
 * Boolean, ...) are handled automatically.  In addition, any class which supports the 
 * Collection or Map interface will also be translated without needing to support the 
 * Glueable interface.  All other classes will need to implement the Glueable interface in 
 * order to be written to and read from GLUE format. <BR>
 * 
 * @see us.temerity.pipeline.GlueEncoder
 * @see us.temerity.pipeline.GlueDecoder
 * @see java.util.Collection
 * @see java.util.Map
 */
public 
interface Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * 
   */
  public void 
  writeGlue
  ( 
   GlueEncoder ge    /* IN: the current GLUE encoder */ 
  )
    throws GlueError;


  public void 
  readGlue
  (
   GlueDecoder gd    /* IN: the current GLUE decoder */ 
  )
    throws GlueError;
  
}



