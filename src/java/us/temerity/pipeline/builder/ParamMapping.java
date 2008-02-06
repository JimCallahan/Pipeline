package us.temerity.pipeline.builder;

import java.util.*;

import us.temerity.pipeline.ComplexParam;
import us.temerity.pipeline.ComplexUtilityParam;

/**
 * A key into a parameter contained in a Utility.  It consists of a mandatory name, which
 * specifies the parameter name, and an optional list of keys which specify the path
 * to descend if the parameter is in question is a {@link ComplexUtilityParam}.
 * <P>
 * If Complex Parameters are extended to other parts of the Pipeline API, this class will
 * most likely be converted into a standalone class. 
 */
public
class ParamMapping
  implements Comparable<ParamMapping>
{
  /**
   * Copy Constructor.
   */
  public
  ParamMapping
  (
    ParamMapping mapping
  )
  {
    this(mapping.getParamName(), mapping.getKeys());
  }
  
  /**
   * Constructs a {@link ParamMapping} which has just a name and no keys. 
   */
  public
  ParamMapping
  (
    String paramName
  )
  {
    this(paramName, new LinkedList<String>());
  }
  
  /**
   * Constructs a {@link ParamMapping} which has just a name and a single key. 
   */
  public
  ParamMapping
  (
    String paramName,
    String firstKey
  )
  {
    this(paramName, listFromObject(firstKey));
  }
  
  /**
   * Constructs a {@link ParamMapping} which has just a name and a single key. 
   */
  public
  ParamMapping
  (
    String paramName,
    String firstKey,
    String secondKey
  )
  {
    this(paramName, listFromObject(firstKey, secondKey));
  }
  
  /**
   * Constructs a {@link ParamMapping} which has just a name and a single key. 
   */
  public
  ParamMapping
  (
    String paramName,
    String firstKey,
    String secondKey,
    String thirdKey
  )
  {
    this(paramName, listFromObject(firstKey, secondKey, thirdKey));
  }
  
  /**
   * Constructs a {@link ParamMapping} which has a name and a list of keys.
   */
  public
  ParamMapping
  (
    String paramName,
    List<String> keys
  )
  {
    if (paramName == null)
      throw new IllegalArgumentException("Cannot have a null parameter name");
    pParamName = paramName;
    if (keys == null)
      pKeys = null;
    else if (keys.isEmpty())
      pKeys = null;
    else
      pKeys = new LinkedList<String>(keys);
  }
  
  /**
   * Gets the name of the parameter. 
   */
  public String
  getParamName()
  {
    return pParamName;
  }
  
  /**
   * Does the mapping have keys?  
   */
  public boolean
  hasKeys()
  {
    if (pKeys == null)
      return false;
    return true;
  }
  
  /**
   * Gets a list of the keys from this mapping
   * <p>
   * Returns <code>null</code> if there are no keys.  
   */
  public List<String>
  getKeys()
  {
    if (pKeys == null)
      return null;
    return Collections.unmodifiableList(pKeys);
  }
  
  /**
   *  Appends a key to the list of keys 
   */
  public void
  addKey
  (
    String key
  )
  {
    if (pKeys == null)
      pKeys = listFromObject(key);
    else
      pKeys.add(key);
  }

  public int 
  compareTo
  (
    ParamMapping that
  )
  {
    int compare = this.pParamName.compareTo(that.pParamName);
    if (compare != 0)
      return compare;
    if (this.pKeys == null) {
      if (that.pKeys == null)
        return 0;
      return -1;
    }
    if (that.pKeys == null)
      return 1;
    int thisSize = this.pKeys.size();
    int thatSize = that.pKeys.size();
    if (thisSize > thatSize)
      return 1;
    else if (thatSize < thisSize)
      return -1;
    for (int i = 0; i < thisSize; i++) {
      String thisKey = this.pKeys.get(i);
      String thatKey = that.pKeys.get(i);
      compare = thisKey.compareTo(thatKey);
      if (compare != 0)
        return compare;
    }
    return 0;
  }
  
  @Override
  public boolean 
  equals
  (
    Object obj
  )
  {
    if (!(obj instanceof ParamMapping ) )
      return false;
    ParamMapping mapping = (ParamMapping) obj;
    int compare = this.compareTo(mapping);
    if (compare == 0)
      return true;
    return false;
  }

  @Override
  public String
  toString()
  {
    return "Param Name (" + pParamName + ") with Keys: " + pKeys;
  }
  
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
  
  /**
   * Creates a {@link LinkedList} from a single item.
   */
  private static <E> LinkedList<E>
  listFromObject
  (
    E key1,
    E key2
  )
  {
    LinkedList<E> list = new LinkedList<E>();
    list.add(key1);
    list.add(key2);
    return list;
  }
  
  /**
   * Creates a {@link LinkedList} from a single item.
   */
  private static <E> LinkedList<E>
  listFromObject
  (
    E key1,
    E key2,
    E key3
  )
  {
    LinkedList<E> list = new LinkedList<E>();
    list.add(key1);
    list.add(key2);
    list.add(key3);
    return list;
  }
  
  private String pParamName;
  private LinkedList<String> pKeys;
  
  public static final ParamMapping NullMapping = new ParamMapping("NULL");
}
