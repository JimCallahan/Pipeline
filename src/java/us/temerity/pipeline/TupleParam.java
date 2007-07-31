// $Id: TupleParam.java,v 1.1 2007/07/31 14:58:14 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.math.*;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   P A R A M                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with an Tuple* value. <P> 
 */
public 
class TupleParam<T>
  extends SimpleParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public 
  TupleParam() 
  {
    super();
  }

  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the parameter.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  TupleParam
  (
   String name,  
   String desc, 
   T value
  ) 
  {
    super(name, desc, (Comparable) value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public T
  getTupleValue() 
  {
    return ((T) getValue());
  }

  /**
   * Sets the value of the parameter from a String.<p>
   * 
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException 
   *   If a null value is passed in.
   */
  public void
  fromString
  (
    String value
  )
  {
    throw new IllegalArgumentException("Not implemented yet!"); 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8372901136871034894L;

}



