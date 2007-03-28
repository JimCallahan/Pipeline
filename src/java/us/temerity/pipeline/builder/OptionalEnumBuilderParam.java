/*
 * Created on Sep 22, 2006
 * Created by jesse
 * For Use in us.temerity.pipeline.builders
 * 
 */
package us.temerity.pipeline.builder;

import java.util.ArrayList;

import us.temerity.pipeline.OptionalEnumParam;
import us.temerity.pipeline.glue.GlueDecoder;

public 
class OptionalEnumBuilderParam 
  extends OptionalEnumParam
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
  OptionalEnumBuilderParam() 
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
   * 
   * @param values
   *   The complete set of enumerated values.
   */ 
  public
  OptionalEnumBuilderParam
  (
   String name,  
   String desc, 
   String value, 
   ArrayList<String> values
  ) 
  {
    super(name, desc, value, values);
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
    setValue(value);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4336919000788449670L;

}
