// $Id: LinkActionParam.java,v 1.1 2004/06/14 22:43:27 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I N K   A C T I O N   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with a fully resolved name of an upstream node as the value. <P> 
 */
public 
class LinkActionParam
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
  LinkActionParam
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

  private static final long serialVersionUID = -8720908048024688428L;

}



