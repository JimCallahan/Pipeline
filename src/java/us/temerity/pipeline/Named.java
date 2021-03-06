// $Id: Named.java,v 1.12 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N A M E D                                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides a simple name <CODE>String</CODE> and accessor.    
 */
public
class Named
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected 
  Named() 
  {}

  /**
   * Internal constructor used by subclasses to initialize the name. 
   * 
   * @param name 
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
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
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
    if((obj != null) && (obj instanceof Named)) {
      Named named = (Named) obj;
      return pName.equals(named.pName);
    }
    return false;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    try {
      return super.clone();
    }
    catch(CloneNotSupportedException ex) {
      throw new IllegalStateException();     
    }
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
    pName = name;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4473685551529032568L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the instance. 
   */
  protected String  pName;        
}
