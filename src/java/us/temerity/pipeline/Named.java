// $Id: Named.java,v 1.3 2004/02/25 02:59:28 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   N A M E D                                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides a simple name <CODE>String</CODE> and accessor.    
 */
public
class Named
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  Named() 
  {}

  /**
   * Internal constructor used by subclasses to initialize the name. 
   * 
   * @param name [<B>in</B>]
   *   The name of this instance.
   */ 
  protected
  Named
  (
   String name 
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The name cannot be (null)!");
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of this instance. 
   */ 
  public String
  getName() 
  {
    assert(pName != null);
    return pName;
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
    encoder.encode("Name", pName);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String name = (String) decoder.decode("Name"); 
    if(name == null) 
      throw new GlueException("The \"Name\" was missing!");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the instance. 
   */
  protected String  pName;        
}
