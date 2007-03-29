// $Id: OptionalEnumParam.java,v 1.5 2007/03/29 19:27:48 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   O P T I O N A L   E N U M   P A R A M                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A plugin parameter with an optional Enum value, allowing selection from the enum or the
 * inputing of a new value.
 * <P>
 */
public 
class OptionalEnumParam 
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
  OptionalEnumParam() 
  {
    super();

    pValues = new ArrayList<String>();
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
  OptionalEnumParam
  (
   String name,  
   String desc, 
   String value, 
   ArrayList<String> values
  ) 
  {
    super(name, desc, value);

    if (values == null || values.isEmpty())
      throw new IllegalArgumentException
	("The values parameter must contain at least one value.");

    pValues = values;
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
   * Get the enumeration value based on the ordinal index.
   */ 
  public String
  getValueOfIndex
  (
   int idx
  ) 
  {
    return pValues.get(idx);
  }

  /**
   * Get the index of the current value 
   */ 
  public int 
  getIndex() 
  {
    return pValues.indexOf(getStringValue());
  }


  /**
   * The complete set of enumerated values.
   */ 
  public Collection<String>
  getValues() 
  {
    return Collections.unmodifiableCollection(pValues);
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
  @SuppressWarnings("unchecked")
  protected void 
  validate
  (
    Comparable value	  
  )
    throws IllegalArgumentException 
  {
    if(value == null)
      throw new IllegalArgumentException
	("The parameter (" + pName + ") cannot accept (null) values!");
      
    if(!(value instanceof String)) 
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (String) values!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -519132760794275781L;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The complete set of enumerated values.
   */
  private ArrayList<String>  pValues;
}
