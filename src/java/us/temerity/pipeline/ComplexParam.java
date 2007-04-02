package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.builder.BaseUtil;
import us.temerity.pipeline.glue.GlueDecoder;

/*------------------------------------------------------------------------------------------*/
/*   C O M P L E X   P A R A M                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Parameter that can contain other parameters.
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
   * {@link BaseParam}.
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
  
  public final void
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
    pParams.remove(paramName);
    pParams.put(paramName, param);
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
    return getParam(BaseUtil.listFromString(key));
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
  public Comparable
  getValue
  (
    String key
  )
  {
    return getValue(BaseUtil.listFromString(key));
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
  public TreeMap<String, E> 
  getParams()
  {
    return pParams;
  }
  
  @SuppressWarnings("unchecked")
  public void 
  setValue
  (
    String key, 
    Comparable value
  )
  {
    setValue(BaseUtil.listFromString(key), value);
  }
  
  @SuppressWarnings("unchecked")
  public void 
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
	return;
      }
      throw new IllegalArgumentException
        ("Attempt to call setValue() on a parameter that is not a simple parameter.");
    }

    ComplexParamAccess<E> param = getComplexParam(key);
    param.setValue(newKeys, value);
    valueUpdated(key);
  }
  
  public boolean 
  hasParam
  (
    String key
  )
  {
    return hasParam(BaseUtil.listFromString(key));
  }

  public boolean 
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
  
  public boolean 
  hasSimpleParam
  (
    String key
  )
  {
    return hasSimpleParam(BaseUtil.listFromString(key));
  }
  
  public boolean 
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
  public boolean 
  canSetSimpleParamFromString
  (
    String key
  )
  {
    return hasSimpleParam(BaseUtil.listFromString(key));
  }

  /**
   * Is there a nested Simple Parameter which implements {@link SimpleParamFromString} 
   * identified by this list of keys? 
   */
  public boolean 
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
   * 
   * @param paramName The name of the parameter that has been changed.
   */
  protected void
  valueUpdated
  (
    @SuppressWarnings("unused")
    String paramName
  )
  {}
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   L A Y O U T                                                                          */
  /*----------------------------------------------------------------------------------------*/

  public ArrayList<String> 
  getLayout()
  {
    if(pLayout == null)
      return new ArrayList<String>(pParams.keySet());
    return pLayout;
  }
  
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


//  public TreeMap<String, ArrayList<String>> getPossibleEnumValues()
//  {
//    return null;
//  }
//
//  public boolean needsUpdating()
//  {
//    return false;
//  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   P R I V A T E   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Checks that a list of keys is not <code>null</code> and contains at least
   * on key value;
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
  
