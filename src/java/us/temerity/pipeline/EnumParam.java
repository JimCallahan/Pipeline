// $Id: EnumParam.java,v 1.9 2007/03/29 19:27:48 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   E N U M   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter which is a single choice from a set of enumerated values. <P> 
 */
public 
class EnumParam
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
  EnumParam() 
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
  EnumParam
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
    
    validate(value);
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

    /*
     * Null check is necessary since validate is called in the constructor before
     * pValues is set.  Since the constructor also checks if pValues is null, 
     * the exception will only apply there.
     */
    if (pValues != null)
      if(!pValues.contains(value)) 
        throw new IllegalArgumentException
  	("The value (" + value + ") was not a member of the enumeration for the " + 
  	 "(" + pName + ") parameter!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 436958043289043276L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The complete set of enumerated values.
   */
  private ArrayList<String>  pValues;

}



