// $Id: BooleanParam.java,v 1.5 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with an Boolean value. <P> 
 */
public 
class BooleanParam
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
  BooleanParam() 
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
  BooleanParam
  (
   String name,  
   String desc, 
   Boolean value
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
  public Boolean
  getBooleanValue() 
  {
    return ((Boolean) getValue());
  }
  
  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException if the String value cannot be converted to a boolean
   * or if a null value is passed in.
   */
  public void
  setValueFromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    Boolean booleanValue = null;
    if ("false".equalsIgnoreCase(value))
      booleanValue = Boolean.FALSE;
    else if ("true".equalsIgnoreCase(value))
      booleanValue = Boolean.TRUE;
    else
      throw new IllegalArgumentException
        ("String (" +  value + ") is not a valid boolean value");
    setValue(booleanValue);
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
    if((value != null) && !(value instanceof Boolean))
      throw new IllegalArgumentException
      ("The parameter (" + getName()  + ") only accepts (Boolean) values!");
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 505509019414165480L;

}



