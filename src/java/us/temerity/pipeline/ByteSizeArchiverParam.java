// $Id: ByteSizeArchiverParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   A R C H I V E R   P A R A M                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An Archiver parameter with a Long value used to represent a size in bytes <P>  
 */
public 
class ByteSizeArchiverParam
  extends ByteSizeParam
  implements ArchiverParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  ByteSizeArchiverParam() 
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
  ByteSizeArchiverParam
  (
   String name,  
   String desc, 
   Long value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4804829589916734492L;

}



