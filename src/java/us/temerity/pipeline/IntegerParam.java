// $Id: IntegerParam.java,v 1.5 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with an Integer value. <P> 
 */
public 
class IntegerParam
  extends SimpleParam
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
  IntegerParam() 
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
  IntegerParam
  (
   String name,  
   String desc, 
   Integer value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public Integer
  getIntegerValue() 
  {
    return ((Integer) getValue());
  }

  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException if a null value is passed in.
   * @throws NumberFormatException if the String is not a valid Integer value.
   */
  public void
  setValueFromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    Integer intValue = Integer.valueOf(value);
    setValue(intValue);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if((value != null) && !(value instanceof Integer))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Integer) values!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3371638711041167312L;

}



