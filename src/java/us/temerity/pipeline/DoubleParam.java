// $Id: DoubleParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

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

  /**
   * Sets the value of the parameter. 
   */
  public void 
  setValue
  (
   Comparable value  
  ) 
  {
    if((value != null) && !(value instanceof Double))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Double) values!");

    pValue = value;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -506455666820823063L;

}



