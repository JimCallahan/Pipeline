// $Id: StringActionParam.java,v 1.1 2004/06/14 22:43:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G   A C T I O N   P A R A M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with a short String value. <P> 
 */
public 
class StringActionParam
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
  StringActionParam
  (
   String name,  
   String desc, 
   String value
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
  public String
  getStringValue() 
  {
    return ((String) getValue());
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
    if((value != null) && !(value instanceof String))
      throw new IllegalArgumentException
	("The action parameter (" + pName + ") only accepts (String) values!");

    pValue = value;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8407737145983539458L;

}



