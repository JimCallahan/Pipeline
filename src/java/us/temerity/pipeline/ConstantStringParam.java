// $Id: ConstantStringParam.java,v 1.2 2009/11/03 03:48:00 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   C O N S T A N T   S T R I N G   P A R A M                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with a short String value which cannot be changed. 
 * <P>
 * Used for creating complex parameters who have some values which cannot change.
 */
public  
class ConstantStringParam
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
  ConstantStringParam() 
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
  ConstantStringParam
  (
    String name,
    String desc,
    String value
  )
  {
    super(name, desc, value);
    pSet = true;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter. 
   */ 
  public String
  getStringValue() 
  {
    return ((String) getValue());
  }

  /**
   * Sets the value of the parameter from a String.
   * <p>
   * This method is used for setting parameter values from command line arguments.
   * 
   * @throws IllegalArgumentException if a null value is passed in.
   */
  public void
  fromString
  (
    String value
  )
  {
    if (value == null)
      throw new IllegalArgumentException("Cannot set a Parameter value from a null string");
    setValue(value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value      
  )
    throws IllegalArgumentException 
  {
    if((value != null) && !(value instanceof String))
      throw new IllegalArgumentException
        ("The parameter (" + pName + ") only accepts (String) values!");
    if (pSet == true)
      throw new IllegalStateException
        ("The parameter (" + pName + ") is a ConstantStringParam, which means its value " +
         "cannot be changed after it is created.");
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Set", pSet);
  }
  
  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pSet = (Boolean) decoder.decode("Set"); 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 3351606101441159005L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private boolean pSet = false;
}