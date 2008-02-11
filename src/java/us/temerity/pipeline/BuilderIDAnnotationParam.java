// $Id: BuilderIDAnnotationParam.java,v 1.1 2008/02/11 03:16:25 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B U I L D E R   I D   A N N O T A T I O N   P A R A M                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An Annotation parameter with a BuilderID value. <P> 
 */
public 
class BuilderIDAnnotationParam
  extends BuilderIDParam
  implements AnnotationParam
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
  BuilderIDAnnotationParam() 
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
  BuilderIDAnnotationParam
  (
   String name,  
   String desc, 
   BuilderID value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3578989142679478108L;

}



