package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Parameter that can contain other parameters.
 * <p>
 * A Complex Parameter is a parameter that contains other parameters, both
 * {@link SimpleParam SimpleParams} and other ComplexParams. It is implemented using Generics,
 * allowing for easy subclassing for each parameter type. For an example of this behavior, see
 * {@link ComplexUtilityParam} which extends this class. In the end, this class is just an
 * implementation of the {@link ComplexParamAccess} interface, which is also implemented using
 * Generics. It is theoretically possible to create an alternative implementation of
 * {@link ComplexParamAccess} and not use {@link ComplexParam}, though it is hard to think of
 * a compelling reason to go to the trouble.
 * <p>
 * Complex Parameters are more than just grouping of other parameters, however. They also have
 * functionality that can cause some of their members to change their values based upon
 * changes to values in other of their member. This is done through two methods that
 * implementing classes should override. The method {@link #needsUpdating()} simply needs to
 * return a boolean that indicates if this ComplexParameter ever needs to update its values.
 * This method should only consider the scope of the current Parameter. Complex Parameters
 * which are nested inside this parameter will be asked as well and a final answer will be
 * created from an aggregate of all their responses. So a ComplexParameter is being created
 * which nests another Complex Parameter which does need updating, the top level parameter
 * does not have return true for this method.
 * <p>
 * The other method is {@link #valueUpdated(String)}. This method is called by
 * {@link #setValue(List, Comparable)} or {@link #setValue(String, Comparable)} if it is
 * indicated by {@link #needsUpdating()} that the Parameter needs to be updated. This method
 * needs to return a boolean that indicates if any values were actually changed. That boolean
 * allows the GUI to correctly display the changes in values.
 * <p>
 * Complex Parameters also need to specify layouts that describe the order in which their
 * component parameters are displayed. The layout is simply an {@link ArrayList} of parameter
 * names and <code>null</code> values to indicate spaces.
 */
public abstract
class ComplexParam<E>
  extends BaseParam
  implements ComplexParamAccess<E>
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
  ComplexParam()
  {
    super();
  }
  
  /**
   *  Constructor which takes the name of the parameter and a description. 
   */
  protected 
  ComplexParam
  (
    String name,  
    String desc
  )
  {
    super(name, desc);
    pParams = new TreeMap<String, E>();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Adds a parameter to this complex parameters.
   * 
   * @throws IllegalArgumentException if the value passed in is not an instance of 
   * {@link BaseParam} or if there is already a parameter with the same name.
   */
  public final void
  addParam
  (
    E param
  )
  {
    if (!(param instanceof BaseParam))
      throw new IllegalArgumentException
      ("A Complex Parameter can only contain instances of BaseParam");
    BaseParam base = (BaseParam) param;
    String paramName = base.getName();
    if (pParams.keySet().contains(paramName))
      throw new IllegalArgumentException
      ("Attempting to add a parameter with the name (" + paramName + ").  " +
       "A parameter with this name already exists in this Complex Parameter");
    pParams.put(paramName, param);
  }
  
  /**
   * Replaces an existing parameter with a new one.
   * <p>
   * This is a name based replacement. So if the new parameter you pass in has the name 'foo',
   * the existing parameter with the name 'foo' will be removed and the new parameter will be
   * added in its place.
   * <p>
   * If there is no parameter that exists with the name 'foo', then this method will perform
   * similarly to addParam.
   */
  public final E
  replaceParam
  (
    E param
  )
  {
    if (!(param instanceof BaseParam))
      throw new IllegalArgumentException
      ("A Complex Parameter can only contain instances of BaseParam");
    BaseParam base = (BaseParam) param;
    String paramName = base.getName();
    E old = pParams.remove(paramName);
    pParams.put(paramName, param);
    return old;
  }
  
  /**
   * Gets a top level Parameter represented by the given key.
   */
  public final E
  getParam
  (
    String key
  )
  {
    return getParam(listFromObject(key));
  }

  /**
   * Gets a Parameter defined by the list of keys.
   * <p>
   * Each key represents another level of nesting.  Every key except that last
   * one must point to a Complex Parameter.
   * <p>
   * Note that this will throw exceptions if you ask for a parameter that doesn't exist.
   * If, for some reason, there is uncertainty about the existance of a particular nested
   * parameter, it is better to use {@link #hasParam(List)} to check for existance
   * before calling this method.
   * 
   * @throws IllegalArgumentException if the list of keys is null or empty, if there
   * is a null key in the list of keys, or if the given parameter does not exist.
   */
  public final E
  getParam
  (
    List<String> keys
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = nextKey(newKeys);
    if (newKeys.size() == 0) {
      return pParams.get(key);
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    return param.getParam(newKeys);
  }
  
  /**
   * Gets the value of the parameter identified by the key.
   */
  @SuppressWarnings("unchecked")
  public final Comparable
  getValue
  (
    String key
  )
  {
    return getValue(listFromObject(key));
  }
  
  /**
   * Gets the value of the nested parameter identified by the list of keys.
   */
  @SuppressWarnings("unchecked")
  public final Comparable
  getValue
  (
    List<String> keys
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = nextKey(newKeys);
    if (newKeys.size() == 0) {
      E param = getParam(keys);
      if(param instanceof SimpleParamAccess) {
	SimpleParamAccess simple = (SimpleParamAccess) param;
	return simple.getValue();
      }
      throw new IllegalArgumentException
        ("Attempt to call getValue() on a parameter that is not a simple parameter.");
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    return param.getValue(newKeys);
  }
  
  /**
   *  Returns a list of all the Parameters in this Complex Parameter with their names as keys. 
   */
  public final TreeMap<String, E> 
  getParams()
  {
    return pParams;
  }
  
  /**
   * Sets the value of the parameter identified by the key.
   * <p>
   * The param being set must implement the {@link SimpleParamAccess} interface. The
   * {@link #hasSimpleParam(String)} method can be used to check if a given parameter is
   * setable.
   */
  @SuppressWarnings("unchecked")
  public final boolean 
  setValue
  (
    String key, 
    Comparable value
  )
  {
    return setValue(listFromObject(key), value);
  }
  
  /**
   * Sets the value of the parameter identified by the list of keys.
   * <p>
   * The param being set must implement the {@link SimpleParamAccess} interface. The
   * {@link #hasSimpleParam(List)} method can be used to check if a given parameter is
   * setable.
   */
  @SuppressWarnings("unchecked")
  public final boolean
  setValue
  (
    List<String> keys, 
    Comparable value
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = nextKey(newKeys);
    if (newKeys.size() == 0) {
      E param = getParam(keys);
      if(param instanceof SimpleParamAccess) {
	SimpleParamAccess simple = (SimpleParamAccess) param;
	simple.setValue(value);
	if (needsUpdating())
	  return valueUpdated(key);
	return false;
      }
      throw new IllegalArgumentException
        ("Attempt to call setValue() on a parameter that is not a simple parameter.");
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    boolean returned = param.setValue(newKeys, value);
    if (needsUpdating())
      return (valueUpdated(key) || returned);
    return returned;
  }
  
  /**
   * Sets the value of the nested parameter identified by the key from a String value.
   * <p>
   * The param being set must implement the {@link SimpleParamFromString} interface. The
   * {@link #canSetSimpleParamFromString(String)} method can be used to check if a given parameter is
   * setable.
   */
  @SuppressWarnings("unchecked")
  public boolean
  fromString
  (
    String key, 
    String value
  )
  {
    return fromString(listFromObject(key), value);
  }
  

  /**
   * Sets the value of the nested parameter identified by the list of keys from a 
   * String value.
   * <p>
   * The param being set must implement the {@link SimpleParamFromString} interface. The
   * {@link #canSetSimpleParamFromString(List)} method can be used to check if a given parameter is
   * setable.
   */
  @SuppressWarnings("unchecked")
  public boolean
  fromString
  (
    List<String> keys, 
    String value
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = nextKey(newKeys);
    if (newKeys.size() == 0) {
      E param = getParam(keys);
      if(param instanceof SimpleParamFromString) {
	SimpleParamFromString simple = (SimpleParamFromString) param;
	simple.fromString(value);
	if (needsUpdating())
	  return valueUpdated(key);
	return false;
      }
      throw new IllegalArgumentException
        ("Attempt to call fromString() on a parameter that is not settable from a String.");
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    boolean returned = param.setValue(newKeys, value);
    if (needsUpdating())
      return (valueUpdated(key) || returned);
    return returned;
  }

  /**
   *  Checks for the existence of a parameter identified by the key.
   */
  public final boolean 
  hasParam
  (
    String key
  )
  {
    return hasParam(listFromObject(key));
  }

  /**
   *  Checks for the existence of a parameter identified by the list of keys. 
   */
  public final boolean 
  hasParam
  (
    List<String> keys
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = newKeys.poll();
    if (key == null)
      throw new IllegalArgumentException("Cannot have a null key in a Complex Parameter");
    if (!pParams.containsKey(key))
	return false;
    if (newKeys.size() == 0)
	return true;

    ComplexParamAccess<E> param = getComplexParam(key);
    return param.hasParam(newKeys);
  }
  
  /**
   * Is there a Simple Parameter identified with this key?
   */
  public final boolean 
  hasSimpleParam
  (
    String key
  )
  {
    return hasSimpleParam(listFromObject(key));
  }

  /**
   * Is there a Simple Parameter identified with this list of keys?
   */
  public final boolean 
  hasSimpleParam
  (
    List<String> keys
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = newKeys.poll();
    if (key == null)
      throw new IllegalArgumentException("Cannot have a null key in a Complex Parameter");
    if (!pParams.containsKey(key))
	return false;
    if (newKeys.size() == 0) {
      if (pParams.get(key) instanceof SimpleParamAccess)
	return true;
      return false;
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    return param.hasParam(newKeys);
  }
  
  /**
   * Is there a Simple Parameter which implements {@link SimpleParamFromString} 
   * identified with this key? 
   */
  public final boolean 
  canSetSimpleParamFromString
  (
    String key
  )
  {
    return canSetSimpleParamFromString(listFromObject(key));
  }

  /**
   * Is there a nested Simple Parameter which implements {@link SimpleParamFromString} 
   * identified by this list of keys? 
   */
  public final boolean 
  canSetSimpleParamFromString
  (
    List<String> keys
  )
  {
    verifyKeys(keys);
    LinkedList<String> newKeys = new LinkedList<String>(keys);
    String key = newKeys.poll();
    if (key == null)
      throw new IllegalArgumentException("Cannot have a null key in a Complex Parameter");
    if (!pParams.containsKey(key))
	return false;
    if (newKeys.size() == 0) {
      if (pParams.get(key) instanceof SimpleParamFromString)
	return true;
      return false;
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    return param.hasParam(newKeys);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   U P D A T E                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This method needs to be overwritten if the Parameter has to adjust some of its values
   * when some of its other values have changed.
   * <p>
   * By default it does nothing.
   * <p>
   * This method gets called by setValue. There should never be a need for this method to be
   * called directly.
   * 
   * @param paramName
   *        The name of the parameter that has been changed.
   * 
   * @return A boolean that indicates if values have been changed. This value is used by the
   *         GUI to determine if it needs to update its fields.
   */
  protected boolean
  valueUpdated
  (
    @SuppressWarnings("unused")
    String paramName
  )
  {
    return false;
  }
  
  /**
   * Method that can be called to check if this Complex Parameter needs to have its values
   * updated after its values have been changed.
   * <p>
   * This method is used by Builders in the the GUI mode to know which parameters need to have
   * ActionListeners assigned to them. This method works by first querying the
   * {@link #needsUpdating()} method of the current ComplexParameter and then recursing down
   * into any Complex Parameters that are nested inside this one.
   */
  @SuppressWarnings("unchecked")
  public final boolean
  requiresUpdating()
  {
    if(needsUpdating() == true)
      return true;
    for (E param : pParams.values()) {
      if(!(param instanceof SimpleParamAccess)) {
	ComplexParamAccess<E> complex = (ComplexParamAccess<E>) param;
	if (complex.requiresUpdating() == true)
	  return true;
      }
    }
    return false;
  }
  
  /**
   * Does this Complex Parameter require updating if any of its values change?
   * <p>
   * Having this return <code>true</code> will cause this Complex Parameter and any Complex
   * Parameter it is a member of to return <code>true</code> when
   * {@link #requiresUpdating()} is called.
   */
  protected abstract boolean
  needsUpdating();
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the layout of member parameters for this parameter.
   * <p>
   * If no layout has been specified, an alphabetically ordered list is returned. 
   */
  public ArrayList<String> 
  getLayout()
  {
    if(pLayout == null)
      return new ArrayList<String>(pParams.keySet());
    return pLayout;
  }
  
  /**
   * Sets the layout of member parameters.
   * <p>
   * The List passed in must contain every parameter name once and only once.
   * @param layout
   */
  protected void 
  setLayout
  (
    ArrayList<String> layout
  )
  {
    if(layout == null)
      throw new IllegalArgumentException("Cannot pass a null layout into setLayout");

    TreeSet<String> entries = new TreeSet<String>();
    Set<String> paramNames = pParams.keySet();
    for(String entry : layout) {
      if (entry == null)
	continue;
      if(!paramNames.contains(entry))
	throw new IllegalArgumentException
	("The entry (" + entry + ") specified by the layout does not match any parameter " +
	 "defined in this complex parameter!");
      if(entries.contains(entry))
	throw new IllegalArgumentException
  	("The parameter (" + entry + ") was specified more than once in the layout!");
      entries.add(entry);
    }
    for(String entry : paramNames) {
      if (!entries.contains(entry))
	throw new IllegalArgumentException
	("The parameter (" + entry+ ") defined by this complex parameter was not " + 
	 "specified in the parameter layout group!");
    }
    pLayout = layout;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   P R I V A T E   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Checks that a list of keys is not <code>null</code> and contains at least
   * one key value;
   * 
   * @throws IllegalArgumentException if the above conditions aren't met.
   */
  private void 
  verifyKeys
  (
    List<String> keys
  )
  {
    if (keys == null || keys.size() == 0)
      throw new IllegalArgumentException
        ("The list of keys passed to Complex Parameters must be instantiated " +
         "and contain at least one key.");
  }
  
  /**
   * Finds the next key value in a list of keys, removes it from the list, and verifies that
   * the key is actually a valid key for this Complex Parameter.
   * 
   * @throws IllegalArgumentException if there is a <code>null</code> key in the list or
   * if there is no parameter in the Complex Parameter with the named key.
   */
  private String
  nextKey
  (
    LinkedList<String> keys
  )
  {
    String toReturn = keys.poll();
    if (toReturn == null)
      throw new IllegalArgumentException("Cannot have a null key in a Complex Parameter");
    if (!pParams.containsKey(toReturn))
	throw new IllegalArgumentException
	  ("No Parameter with the name (" + toReturn + ") exists in this Complex Parameter");
    return toReturn;
  }
  
  /**
   * Returns a member Complex Parameter identified by the given key.
   * 
   * @param key
   *        The parameter key.
   * @throws IllegalArgumentException
   *         if the key passed in does not represent a valid Complex Parameter.
   */
  @SuppressWarnings("unchecked")
  private ComplexParamAccess<E>
  getComplexParam
  (
    String key
  )
  {
    ComplexParamAccess<E> toReturn = null;
    try {
      toReturn = (ComplexParamAccess<E>) pParams.get(key);
    }
    catch (ClassCastException ex) {
      throw new IllegalArgumentException
        ("The branch key named (" + key + ") does not correspond to a complex parameter");
    }
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   U T I L I T Y   M E T H O D S                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Creates a {@link LinkedList} from a single item.
   */
  private static <E> LinkedList<E>
  listFromObject
  (
    E key
  )
  {
    LinkedList<E> list = new LinkedList<E>();
    list.add(key);
    return list;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8468987523733273623L;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Parameter Layout.
   */
  private ArrayList<String> pLayout;
  
  /**
   * List of Parameters.
   */
  private TreeMap<String, E> pParams;
}
  
