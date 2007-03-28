// $Id: PathParam.java,v 1.3 2007/03/28 20:43:45 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   P A T H   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a abstract file system pathname value. <P> 
 */
public 
class PathParam
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
  PathParam() 
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
  PathParam
  (
   String name,  
   String desc, 
   Path value
  ) 
  {
    super(name, desc, value);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the {@link Path} value of the parameter. 
   */ 
  public Path
  getPathValue() 
  {
    return ((Path) getValue());
  }
  
  /**
   * Gets the {@link String} value of the parameter. 
   */ 
  public String
  getStringValue() 
  {
    return (getPathValue().toString());
  }

  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException if a null value is passed in or the String value is
   * not a valid Path
   */
  public void
  setValueFromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    Path pathValue = new Path(value);
    setValue(pathValue);
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
    if((value != null) && !(value instanceof Path))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (Path) values!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2711738787273982353L;

}



