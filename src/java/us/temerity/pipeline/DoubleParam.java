// $Id: DoubleParam.java,v 1.4 2006/12/10 22:52:54 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   P A R A M                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a Double value. <P> 
 */
public 
class DoubleParam
  extends BaseParam
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
  DoubleParam() 
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
  DoubleParam
  (
   String name,  
   String desc, 
   Double value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public Double
  getDoubleValue() 
  {
    return ((Double) getValue());
  }
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if((value != null) && !(value instanceof Double))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Double) values!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -506455666820823063L;

}



