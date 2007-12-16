// $Id: PathKeyParam.java,v 1.1 2007/12/16 11:12:16 jesse Exp $

package us.temerity.pipeline.param.key;

import us.temerity.pipeline.Path;
import us.temerity.pipeline.PathParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   P A T H   K E Y   P A R A M                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A Key parameter with a Path value. <P> 
 */
public 
class PathKeyParam
  extends PathParam
  implements KeyParam
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
  PathKeyParam() 
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
  PathKeyParam
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

  private static final long serialVersionUID = -7099867296668690923L;
}
