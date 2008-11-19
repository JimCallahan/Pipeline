// $Id: KeyValueUtilityParam.java,v 1.2 2008/11/19 04:32:03 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   K E Y   V A L U E   U T I L I T Y    P A R A M                                         */
/*------------------------------------------------------------------------------------------*/

public 
class KeyValueUtilityParam
  extends KeyValueParam<UtilityParam>
  implements UtilityParam
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from user
   * code.
   */
  public 
  KeyValueUtilityParam() 
  {
    super();
  }
  
  /**
   * Construct a parameter with the given name, description, default value, and layout.
   *
   * @param name
   *   The short name of the parameter.
   * 
   * @param desc
   *   A short description used in tooltips.
   * 
   * @param key
   *   The value of the key parameter.  This will not be changeable.
   *   
   * @param value
   *   The initial value.  This will be changeable.
   */
  public
  KeyValueUtilityParam
  (
   String name, 
   String desc,
   String key,
   String value
  ) 
  {
    super(name, desc, key, value);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  protected UtilityParam 
  createStringParam
  (
    String name,
    String desc,
    String value
  )
  {
    return new StringUtilityParam(name, desc, value);
  }
  
  @Override
  protected UtilityParam 
  createConstantStringParam
  (
    String name,
    String desc,
    String value
  )
  {
    return new ConstantStringUtilityParam(name, desc, value);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2308583997343427353L;
}
