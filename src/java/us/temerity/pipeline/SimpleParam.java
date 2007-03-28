package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   P A R A M                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * The abstract base class for all parameters that consist of a value that can set with a
 * single variable.
 */
public abstract 
class SimpleParam
  extends BaseParam
  implements SimpleParamAccess
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  protected
  SimpleParam() 
  {
    super();
  }
  
  /** 
   * Construct a parameter with the given name, description and default value.
   * 
   * @param name 
   *   The short name of the editor.  
   * 
   * @param desc 
   *   A short description used in tooltips.
   * 
   * @param value 
   *   The default value for this parameter.
   */ 
  @SuppressWarnings("unchecked")
  protected 
  SimpleParam
  (
   String name,  
   String desc, 
   Comparable value
  ) 
  {
    super(name, desc);

    setValue(value);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the value of the parameter. 
   */ 
  @SuppressWarnings("unchecked")
  public final Comparable
  getValue() 
  {
    return pValue;
  }
  
  /**
   * Sets the value of the parameter. 
   */
  @SuppressWarnings("unchecked")
  public final void 
  setValue
  (
   Comparable value  
  )
  {
    validate(value);
    pValue = value;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   * Override this method in each individual param class. 
   */
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    @SuppressWarnings("unused")
    Comparable value	  
  )
    throws IllegalArgumentException 
  {}
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof SimpleParam)) {
      SimpleParam param = (SimpleParam) obj;
    
      return (super.equals(obj) && 
	      (((pValue == null) && (param.pValue == null)) ||  
	       ((pValue != null) && pValue.equals(param.pValue))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(pValue != null) 
      return pValue.toString();
    return null;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Value", pValue);
  }
  
  @SuppressWarnings("unchecked")
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pValue = (Comparable) decoder.decode("Value"); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The value of the parameter.                
   */     
  @SuppressWarnings("unchecked")
  private Comparable  pValue;
  
}
