// $Id: DoubleActionParam.java,v 1.1 2004/06/14 22:43:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E    A C T I O N   P A R A M                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with a Double value. <P> 
 */
public 
class DoubleActionParam
  extends BaseActionParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  DoubleActionParam
  (
   String name,  
   String desc, 
   Double value
  ) 
  {
    super(name, desc, value);

    if(value == null)
      throw new IllegalArgumentException("The value cannot be (null)!");
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
	("The action parameter (" + pName + ") only accepts (Double) values!");

    pValue = value;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1620927428846217433L;

}



