// $Id: BooleanParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with an Boolean value. <P> 
 */
public 
class BooleanParam
  extends BaseParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  BooleanParam() 
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
  BooleanParam
  (
   String name,  
   String desc, 
   Boolean value
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
  public Boolean
  getBooleanValue() 
  {
    return ((Boolean) getValue());
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
    if((value != null) && !(value instanceof Boolean))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Boolean) values!");

    pValue = value;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 505509019414165480L;

}



