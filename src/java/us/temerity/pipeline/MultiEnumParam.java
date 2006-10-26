// $Id: MultiEnumParam.java,v 1.1 2006/10/26 07:13:02 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M U L T I   E N U M   P A R A M                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An plugin parameter with a value that can be zero or more choices from a set of 
 * enumerated values. <P>
 */
public class MultiEnumParam
  extends BaseParam
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
  MultiEnumParam() 
  {
    super();

    pValues = new TreeSet<String>();
  }

  /**
   * Construct a parameter with the given name, description and default value.
   * 
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
   */
  public 
  MultiEnumParam
  (
   String name, 
   String desc, 
   ComparableTreeSet<String> value,
   TreeSet<String> values
  ) 
  {
    super(name, desc, value);

    if(value == null)
      pValue = new ComparableTreeSet<String>();

    if((pValues == null) || pValues.isEmpty())
      throw new IllegalArgumentException
	("The values parameter must contain at least one value.");
    pValues = values;

    for(String each : (ComparableTreeSet<String>) pValue) {
      if(!pValues.contains(each))
	throw new IllegalArgumentException
	  ("The value (" + each + ") is not a valid option in the Map Paramter " + 
	   "(" + pName + ")");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the value of the parameter.
   */
  public ComparableTreeSet<String> 
  getMapValue()
  {
    return ((ComparableTreeSet<String>) getValue());
  }

  /**
   * Sets the value of the parameter.
   */
  public void 
  setValue
  (
   Comparable value
  )
  {
    if((value != null) && !(value instanceof ComparableTreeSet))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (ComparableTreeSet) values!");

    pValue = value;

    for(String each : (ComparableTreeSet<String>) pValue) {
      if(!pValues.contains(each))
	throw new IllegalArgumentException
	  ("The value (" + each + ") is not a valid option in the Map Paramter " + 
	   "(" + pName + ")");
    }
  }

  /**
   * Add the given value to the list of chosen values.
   */ 
  public void 
  selectValue
  (
   String name
  )
  {
    if(!pValues.contains(name))
      throw new IllegalArgumentException
	("The value (" + name + ") is not one of the supported values of the " + 
	 "(" + pName + ") MultiMap parameter.");
    ((ComparableTreeSet<String>) pValue).add(name);
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
    if(!pValues.contains(name))
      throw new IllegalArgumentException
	("The value (" + name + ") is not one of the supported values of the " + 
	 "(" + pName + ") MultiMap parameter.");
    ((ComparableTreeSet<String>) pValue).remove(name);
  }

  /**
   * Get all possible enumerated values.
   */ 
  public Collection<String> 
  getValues()
  {
    return Collections.unmodifiableCollection(pValues);
  }

  /**
   * Get a table indexed by all possible enumerated values containing whether each 
   * value is currently chosen.
   */ 
  public TreeMap<String, Boolean> 
  getValuesMapping()
  {
    TreeMap<String, Boolean> toReturn = new TreeMap<String, Boolean>();
    for(String name : pValues) {
      if(((ComparableTreeSet<String>) pValue).contains(name))
	toReturn.put(name, true);
      else
	toReturn.put(name, false);
    }
    return toReturn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8685930896134087328L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The complete set of all possible enumerated values.
   */
  private TreeSet<String> pValues;

}
