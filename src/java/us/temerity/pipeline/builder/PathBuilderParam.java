// $Id: PathBuilderParam.java,v 1.2 2006/12/10 23:02:25 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.Path;
import us.temerity.pipeline.PathParam;
import us.temerity.pipeline.glue.GlueDecoder;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A T H   B U I D E R   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an abstract file system pathname value. <P> 
 */
public 
class PathBuilderParam
  extends PathParam
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
  PathBuilderParam() 
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
  PathBuilderParam
  (
   String name,  
   String desc, 
   Path value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3611514841873329087L;

}



