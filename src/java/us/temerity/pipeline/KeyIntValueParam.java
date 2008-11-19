// $Id: KeyIntValueParam.java,v 1.3 2008/11/19 04:32:03 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   K E Y   I N T   V A L U E   P A R A M                                                  */
/*------------------------------------------------------------------------------------------*/

public abstract class 
KeyIntValueParam<E>
  extends ComplexParam<E>
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
  KeyIntValueParam() 
  {
    super();
  }
  
  /**
   * Construct a parameter with the given name, description, and default value.
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
  KeyIntValueParam
  (
   String name, 
   String desc,
   String key,
   Integer value
  ) 
  {
    super(name, desc);
    
    {
      E param = createConstantStringParam(aKey, "The key.  Not user modifiable", key);
      addParam(param);
    }
    {
      E param = createIntegerParam(aValue, "The value.  User modifiable", value);
      addParam(param);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected boolean 
  needsUpdating()
  {
    return false;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected abstract E
  createConstantStringParam
  (
    String name, 
    String desc, 
    String value 
  );
  
  protected abstract E
  createIntegerParam
  (
    String name, 
    String desc, 
    Integer value 
  );
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the key value.
   */
  public String
  getKeyValue()
  {
    return (String) getValue(aKey);
  }
  
  /**
   * Get the value value.
   */
  public Integer
  getValueValue()
  {
    return (Integer) getValue(aValue);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 216419723438576765L;

  public static final String aKey = "Key";
  public static final String aValue = "Value";  
}
