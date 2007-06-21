// $Id: WorkGroupAnnotationParam.java,v 1.1 2007/06/21 20:18:31 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   W O R K   G R O U P   A N N O T A T I O N   P A R A M                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An Annotation parameter with a String value containing the name of a Pipeline user or 
 * WorkGroup. <P> 
 */
public 
class WorkGroupAnnotationParam
  extends WorkGroupParam
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
  WorkGroupAnnotationParam() 
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
   * @param allowsUsers
   *   Whether the value can be a user name.
   * 
   * @param allowsGroups
   *   Whether the value can be a WorkGroup name.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  public
  WorkGroupAnnotationParam
  (
   String name,  
   String desc, 
   boolean allowsUsers, 
   boolean allowsGroups, 
   String value
  ) 
  {
    super(name, desc, allowsUsers, allowsGroups, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2092747492607078015L;

}



