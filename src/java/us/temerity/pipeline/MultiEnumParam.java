// $Id: MultiEnumParam.java,v 1.5 2007/02/23 21:10:13 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.ui.JMultiEnumField;

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
   * Construct a parameter with the given name, description, default value, and layout.
   * <p>
   * This class does not provide any sort of error checking on the layout that is passed
   * in.  The layout does not effect the functionality of this class in anyway.  The 
   * layout is only checked for validity if it is used to instantiate a new
   * {@link JMultiEnumField}.  If this never occurs, this class will operate the same
   * regardless of whether the layout is valid or not.
   * <p>
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
  MultiEnumParam
  (
   String name, 
   String desc, 
   ComparableTreeSet<String> value,
   Set<String> values,
   ArrayList<String> layout,
   TreeMap<String, String> tooltips
  ) 
  {
    super(name, desc, value);

    if((values == null) || values.isEmpty())
      throw new IllegalArgumentException
        ("The values parameter must contain at least one value.");
    pValues = values;
    
    // This gets called twice, once in super() and once here.
    // Because the call in super happens before pValues is set, 
    // it is not a true validation check.
    validate(value);
    
    if (layout != null)
      pLayout = new ArrayList<String>(layout);
    else
      pLayout = null;
    if (tooltips == null)
      pTooltips = null;
    else
      pTooltips = new TreeMap<String, String>(tooltips);
  }

  /**
   * Construct a parameter with the given name, description, and default value.
   * <p>
   * This class does not provide any sort of error checking on the layout that is passed
   * in.  The layout does not effect the functionality of this class in anyway.  The 
   * layout is only checked for validity if it is used to instantiate a new
   * {@link JMultiEnumField}.  If this never occurs, this class will operate the same
   * regardless of whether the layout is valid or not.
   * <p>
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
   Set<String> values
  ) 
  {
    this(name, desc, value, values, null, null);
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets all values of the parameter that are selected (set to <code>YES</code> is this is
   * being set in a GUI).
   */
  public ComparableTreeSet<String> 
  getSelectedValues()
  {
    return ((ComparableTreeSet<String>) getValue());
  }
  
  /**
   * Sets the value of the parameter.
   */ 
  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Comparable value)
  {
    if (value == null)
      super.setValue(new ComparableTreeSet<String>());
    else
      super.setValue(value);
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
         "(" + pName + ") MultiEnum parameter.");
    getSelectedValues().add(name);
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
         "(" + pName + ") MultiEnum parameter.");
    getSelectedValues().remove(name);
  }

  /**
   * Get all possible enumerated values.
   */ 
  public Set<String> 
  getValues()
  {
    return Collections.unmodifiableSet(pValues);
  }

  /**
   * Get a table indexed by all possible enumerated values containing whether each 
   * value is currently chosen.
   */ 
  public TreeMap<String, Boolean> 
  getValuesMapping()
  {
    TreeMap<String, Boolean> toReturn = new TreeMap<String, Boolean>();
    ComparableTreeSet<String> pValue = getSelectedValues();
    for(String name : pValues) {
      if(pValue.contains(name))
	toReturn.put(name, true);
      else
	toReturn.put(name, false);
    }
    return toReturn;
  }
  
  /**
   * Gets the layout associated with this multi-enum param.
   */
  public ArrayList<String> 
  getLayout()
  {
    return pLayout;
  }
  
  /**
   * Gets the tooltips associated with this multi-enum param.
   */
  public TreeMap<String, String> 
  getTooltips()
  {
    return pTooltips;
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
    if((value != null) && !(value instanceof ComparableTreeSet))
      throw new IllegalArgumentException
	("The parameter (" + pName + ") only accepts (ComparableTreeSet) values!");

    /*
     * Null check is necessary since validate is called in the constructor before
     * pValues is set.  Since the constructor also checks if pValues is null, 
     * the exception will only apply there.
     */
    if(pValues != null) {
      for(String each : getSelectedValues()) {
        if(!pValues.contains(each))
	  throw new IllegalArgumentException
  	    ("The value (" + each + ") is not a valid option in the Map Paramter " + 
  	     "(" + pName + ")");
      }
    }
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
  private Set<String> pValues;
  private ArrayList<String> pLayout;
  private TreeMap<String, String> pTooltips;
}
