// $Id: EnumArchiverParam.java,v 1.3 2006/02/28 19:43:34 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

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

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */    
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



