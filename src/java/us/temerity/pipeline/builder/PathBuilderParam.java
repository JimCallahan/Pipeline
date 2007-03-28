// $Id: PathBuilderParam.java,v 1.4 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.Path;
import us.temerity.pipeline.PathParam;
import us.temerity.pipeline.glue.GlueDecoder;

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
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Sets the value of this parameter from a string.
   */
  public void 
  valueFromString
  (
    String value
  )
  {
    if (value == null)
      return;
    setValue(new Path(value));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3611514841873329087L;

}



