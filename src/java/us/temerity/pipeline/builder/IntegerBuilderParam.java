// $Id: IntegerBuilderParam.java,v 1.5 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline.builder;

import us.temerity.pipeline.IntegerParam;
import us.temerity.pipeline.glue.GlueDecoder; 

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   B U I L D E R   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Builder parameter with an Integer value. <P> 
 */
public 
class IntegerBuilderParam
  extends IntegerParam
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
  IntegerBuilderParam() 
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
  IntegerBuilderParam
  (
   String name,  
   String desc, 
   Integer value
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
    setValue(Integer.parseInt(value));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8425305116840749504L;

}



