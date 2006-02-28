// $Id: StringArchiverParam.java,v 1.3 2006/02/28 19:43:34 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S T R I N G   A R C H I V E R   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An Archiver parameter with a short String value. <P> 
 */
public 
class StringArchiverParam
  extends StringParam
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
  StringArchiverParam() 
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
  StringArchiverParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1412799666501225286L;

}



