// $Id: ByteSizeParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   P A R A M                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with a Long value used to represent a size in bytes <P> 
 */
public 
class ByteSizeParam
  extends BaseParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ByteSizeParam() 
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
  ByteSizeParam
  (
   String name,  
   String desc, 
   Long value
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
  public Long
  getLongValue() 
  {
    return ((Long) getValue());
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
    if((value != null) && !(value instanceof Long))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Long) values!");

    pValue = value;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3030725403826518345L;

}



