// $Id: EnumBuilderParam.java,v 1.2 2006/12/10 23:02:25 jesse Exp $

package us.temerity.pipeline.builder;

import java.util.ArrayList;

import us.temerity.pipeline.EnumParam;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   B U I L D E R   P A R A M                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An Builder parameter with an Enum value. <P> 
 */
public 
class EnumBuilderParam
  extends EnumParam
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
  EnumBuilderParam() 
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
  EnumBuilderParam
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2885971711944580825L;

}



