// $Id: AdditiveTreeMap.java,v 1.1 2009/11/05 18:06:39 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D I T I V E   T R E E   M A P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of numeric values where calls to the put() method increase the value of the key by
 * the value passed in the method.
 * <p>
 * Useful for situations where a variety of values are being summed up for a keySet.
 * <p>
 * Example:
 * If there was an AdditiveTreeMap<String, Long> with the entry <"horse", 10> and a call was 
 * made of put("horse", 5"), the map would end up with an entry of <"horse", "15">.  This class
 * also changes the return policy of the put() method, which now returns the new total value, 
 * rather than the previous value (as {@link TreeMap} does).  
 *
 * @param <K>
 * @param <V>
 */
public abstract class 
AdditiveTreeMap<K, V extends Number>
  extends TreeMap<K, V>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new, empty tree map, using the natural ordering of its keys. 
   * 
   * @see TreeMap#TreeMap()
   */
  public 
  AdditiveTreeMap()
  {
    super();
  }

  /**
   * Constructs a new, empty tree map, ordered according to the given comparator.
   * 
   * @see TreeMap#TreeMap(Comparator)
   */
  public 
  AdditiveTreeMap
  (
    Comparator<? super K> comparator
  )
  {
    super(comparator);
  }

  /**
   * Constructs a new tree map containing the same mappings as the given map, ordered 
   * according to the natural ordering of its keys
   * 
   * @see TreeMap#TreeMap(Map)
   */
  public 
  AdditiveTreeMap
  (
    Map<? extends K, ? extends V> m
  )
  {
    super(m);
  }

  /**
   * Constructs a new tree map containing the same mappings and using the same ordering as 
   * the specified sorted map.
   * 
   * @see TreeMap#TreeMap(SortedMap)
   */
  public 
  AdditiveTreeMap
  (
    SortedMap<K, ? extends V> m
  )
  {
    super(m);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create, destroy, or increment the value associated with a particular key.
   * <p>
   * If there is no existing value for the key, then a new entry will be created which has the 
   * value as its value.  If the value passed to this method is <code>null</code>, then the
   * entry associated with the key will be blanked and <code>null</code> will be returned.  If 
   * there is an existing value for the key and value is not null, then the new value in the 
   * map will be sum of the existing value and the value parameter.  The method will then 
   * return this new summed value.
   * 
   * @param key
   *   Key with which the specified value is to be associated.
   *   
   * @param value
   *   Value to add to the existing value or <code>null</code> to clear the entry value.
   *   
   * @return
   *   The new summed value or <code>null</code> if the value was cleared.
   */
  @Override
  public V 
  put
  (
    K key, 
    V value
  ) 
  {
    if (value == null) {
      remove(key);
      return null;
    }
    
    V current = get(key);
    if (current == null) {
      super.put(key, value);
      return value;
    }
    else {
      V sum = add(value, current);
      super.put(key, sum);
      return sum;
    }
    
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A B S T R A C T                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the two numbers passed in.
   */
  abstract protected V
  add
  (
    V one, 
    V two
  );
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -3218606118374919047L;
}
