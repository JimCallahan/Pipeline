// $Id: IdentifierBuilderParam.java,v 1.1 2006/08/25 20:02:06 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   I D E N T I F I E R   B U I D E R   P A R A M                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an identifier String value. <P> 
 */
public 
class IdentifierBuilderParam
  extends StringParam
  implements BuilderParam
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
  IdentifierBuilderParam() 
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
  IdentifierBuilderParam
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

  private static final long serialVersionUID = 6562921895708739669L;

}



