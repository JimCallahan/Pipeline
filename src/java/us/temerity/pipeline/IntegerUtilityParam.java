// $Id: IntegerUtilityParam.java,v 1.1 2007/06/15 22:29:47 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder; 

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   U T I L I T Y   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A Utility parameter with an Integer value. <P> 
 */
public 
class IntegerUtilityParam
  extends IntegerParam
  implements UtilityParam
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
  IntegerUtilityParam() 
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
  IntegerUtilityParam
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



