// $Id: BooleanArchiverParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   A R C H I V E R   P A R A M                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * An Archiver parameter with an Boolean value. <P> 
 */
public 
class BooleanArchiverParam
  extends BooleanParam
  implements ArchiverParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  BooleanArchiverParam() 
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
  BooleanArchiverParam
  (
   String name,  
   String desc, 
   Boolean value
  ) 
  {
    super(name, desc, value);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4213607266254842057L;

}



