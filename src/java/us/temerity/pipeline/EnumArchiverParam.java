// $Id: EnumArchiverParam.java,v 1.1 2004/11/11 00:40:09 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   A R C H I V E R   P A R A M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An Archiver parameter with an Enum value. <P> 
 */
public 
class EnumArchiverParam
  extends EnumParam
  implements ArchiverParam
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  public
  EnumArchiverParam() 
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
   * 
   * @param values
   *   The complete set of enumerated values.
   */ 
  public
  EnumArchiverParam
  (
   String name,  
   String desc, 
   String value, 
   ArrayList<String> values
  ) 
  {
    super(name, desc, value, values);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8956511418224618706L;

}



