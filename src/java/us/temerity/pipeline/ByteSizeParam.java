// $Id: ByteSizeParam.java,v 1.5 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   P A R A M                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with a Long value used to represent a size in bytes <P> 
 */
public 
class ByteSizeParam
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
  ByteSizeParam() 
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
  ByteSizeParam
  (
   String name,  
   String desc, 
   Long value
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
  public Long
  getLongValue() 
  {
    return ((Long) getValue());
  }
  
  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException if a null value is passed in.
   * @throws NumberFormatException if the String is not a valid Byte value.
   */
  public void
  setValueFromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    Long longValue = ByteSize.stringToLong(value);
    setValue(longValue);
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
    if((value != null) && !(value instanceof Long))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Long) values!");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3030725403826518345L;

}



