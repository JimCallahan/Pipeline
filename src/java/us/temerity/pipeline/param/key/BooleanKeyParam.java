// $Id: BooleanKeyParam.java,v 1.1 2007/12/16 11:12:16 jesse Exp $

package us.temerity.pipeline.param.key;

import us.temerity.pipeline.BooleanParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   K E Y   P A R A M                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A Key parameter with an Boolean value. <P> 
 */
public 
class BooleanKeyParam
  extends BooleanParam
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
  BooleanKeyParam() 
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
  BooleanKeyParam
  (
   String name,  
   String desc, 
   Boolean value
  ) 
  {
    super(name, desc, value);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
 
  private static final long serialVersionUID = 2155430245613320376L;
}
