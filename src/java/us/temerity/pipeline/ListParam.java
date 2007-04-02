// $Id: ListParam.java,v 1.3 2007/04/02 21:45:49 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   P A R A M                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a value that can be zero or more choices from a set of 
 * enumerated values. <P>
 */
public abstract
class ListParam<E>
  extends ComplexParam<E>
  implements SimpleParamAccess
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class when
   * encountered during the reading of GLUE format files and should not be called from user
   * code.
   */
  public 
  ListParam() 
  {
    super();
  }

  /**
   * Construct a parameter with the given name, description, default value, and layout.

   * @param name
   *    The short name of the parameter.
   * 
   * @param desc
   *    A short description used in tooltips.
   * 
   * @param value
   *    The default value for this parameter. This is the chosen 
   *    subset of all possible values from <code>values</code>.
   * 
   * @param values
   *    The list of all possible values.
   *    
   * @param layout
   * 	A layout for all the parameters.  If this is <code>null</code>, then all
   *    the values will be arranged in alphabetical order.  Otherwise, this structure
   *    needs to contain a string value for each value the parameter could have in the 
   *    order that they are supposed to be displayed.  It can also contain <code>null</code> 
   *    values. Each <code>null</code> value will be displayed as a vertical space between
   *    values.
   *    
   * @param tooltips
   *  	A map of descriptions of what each value is, mapped by value name. 
   */
  public
  ListParam
  (
   String name, 
   String desc, 
   Set<String> value,
   Set<String> values,
   ArrayList<String> layout,
   TreeMap<String, String> tooltips
  ) 
  {
    super(name, desc);

    if((values == null) || values.isEmpty())
      throw new IllegalArgumentException
        ("The values parameter must contain at least one value.");
    
    verifyValues(value, values);
    
    for (String each : values) {
      Boolean check = value.contains(each);
      String tooltip = null;
      if (tooltips != null) {
        tooltip = tooltips.get(each);
      }
      E param = createBooleanParam(each, tooltip, check);
      addParam(param);
    }
    
    if (layout != null)
      setLayout(layout);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets all values of the parameter that are selected (set to <code>YES</code> is this is
   * being set in a GUI).
   */
  @SuppressWarnings("unchecked")
  public ComparableTreeSet<String> 
  getSelectedValues()
  {
    ComparableTreeSet<String> toReturn = new ComparableTreeSet<String>();
    for (String each : getParams().keySet()) {
      Boolean check = (Boolean) getValue(each);
      if (check)
	toReturn.add(each);
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S I M P L E   P A R A M E T E R   A C C E S S                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Implemented to allow this Complex Parameter to be used a Simple Parameter.  Returns
   * a {@link ComparableTreeSet};
   */
  @SuppressWarnings("unchecked")
  public Comparable
  getValue()
  {
    return getSelectedValues();
  }
  
  /**
   * Sets the value of the parameter.
   */ 
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
    Comparable value
  )
  {
    if (value != null && !(value instanceof ComparableTreeSet) )
      throw new IllegalArgumentException
        ("The parameter (" + pName + ") only accepts ComparableTreeSet values!");
    
    ComparableTreeSet<String> values = (ComparableTreeSet<String>) value;
    if (values == null)
      values = new ComparableTreeSet<String>();
    
    Set<String> allValues = getParams().keySet();
    verifyValues(values, allValues);
    
    for (String each : allValues) {
      if (values.contains(each)) 
	setValue(each, true);
      else
	setValue(each, false);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   U T I L I T I E S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given value to the list of chosen values.
   */ 
  public void 
  selectValue
  (
   String name
  )
  {
    setValue(name, true);
  }

  /**
   * Remove the given value to the list of chosen values.
   */ 
  public void 
  deselectValue
  (
   String name
  )
  {
    setValue(name, false);
  }

  /**
   * Get all possible enumerated values.
   */ 
  public Set<String> 
  getValues()
  {
    return Collections.unmodifiableSet(getParams().keySet());
  }

  /**
   * Gets a map indexed by all possible enumerated values containing whether each 
   * value is currently chosen.
   */ 
  public TreeMap<String, Boolean> 
  getValuesMapping()
  {
    TreeMap<String, Boolean> toReturn = new TreeMap<String, Boolean>();
    ComparableTreeSet<String> pValue = getSelectedValues();
    for(String name : getParams().keySet()) {
      if(pValue.contains(name))
	toReturn.put(name, true);
      else
	toReturn.put(name, false);
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   G E N E R I C S   S U P P O R T                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  protected abstract E
  createBooleanParam
  (
    String name, 
    String desc, 
    Boolean value 
  );
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   V A L I D A T O R                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A method to confirm that the input to the param is correct.
   * <P>
   */
  @SuppressWarnings("unchecked")
  private void 
  verifyValues
  (
    Set<String> value,
    Set<String> values
  )
  {
    for(String each : value) {
      if(!values.contains(each))
	throw new IllegalArgumentException
	  ("The value (" + each + ") is not a valid option in the List Paramter " + 
	   "(" + pName + ")");
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8685930896134087328L;
}
