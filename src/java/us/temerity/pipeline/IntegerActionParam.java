// $Id: IntegerActionParam.java,v 1.4 2004/11/11 00:37:06 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   A C T I O N   P A R A M                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * An Action parameter with an Integer value. <P> 
 */
public 
class IntegerActionParam
  extends IntegerParam
  implements ActionParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  IntegerActionParam() 
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
  IntegerActionParam
  (
   String name,  
   String desc, 
   Integer value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3065720750652779863L;

}



