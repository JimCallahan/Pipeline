// $Id: UserNameKeyParam.java,v 1.1 2007/12/16 11:12:16 jesse Exp $

package us.temerity.pipeline.param.key;

import us.temerity.pipeline.WorkGroupParam;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   U S E R   N A M E   K E Y   P A R A M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * An key parameter which contains the name of a Pipeline user or WorkGroup.<P> 
 * 
 * This parameter can be configured to only allow user names, only allow WorkGroup names
 * or allow both.
 */
public 
class UserNameKeyParam
  extends WorkGroupParam
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
  UserNameKeyParam() 
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
  UserNameKeyParam
  (
   String name,  
   String desc, 
   String value
  ) 
  {
    super(name, desc, true, false, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4662831084763321939L;
}
