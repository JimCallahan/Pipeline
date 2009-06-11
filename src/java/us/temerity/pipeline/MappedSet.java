// $Id: MappedSet.java,v 1.11 2009/06/11 05:15:44 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A P P E D   S E T                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  A {@link TreeMap} that contains {@link TreeSet TreeSets}.
 *  <p>
 *  This class does automatic lazy creation of the sets when a value is added to them.
 *  
 * @param <K> 
 *   The type of keys maintained by this map.
 *   
 * @param <V> 
 *   The type of the sets that are the mapped values.
 */
public class MappedSet<K, V>
  extends TreeMap<K, java.util.TreeSet<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map.
   */
  public 
  MappedSet()
  {
    super();
  }
  
  /**
   * Deep copy constructor.
   */
  public 
  MappedSet
  (
    MappedSet<K, V> mset
  )
  {
    super();
    putAll(mset);
  }

  /**
   * Inverts a {@link Map} into a {@link MappedSet}.
   * <p>
   * The values in the map will be converted into the keys of the new
   * MappedSet.  So if you had a Map with the following values
   * <ul>
   *   <li>Boy 1
   *   <li>Girl 2
   *   <li>Horse 1
   *   <li>Duck 4
   * </ul>
   * Then the new MappedSet would look like.
   * <ul>
   *   <li> 1 Boy, Horse
   *   <li> 2 Girl
   *   <li> 4 Duck
   * </ul>
   */
  public 
  MappedSet
  (
    Map<V, K> map
  )
  {
    super();
    for (V key : map.keySet())
      put(map.get(key), key);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Associates a value with the specified key.
   * 
   * @param key
   *   The first key.
   * 
   * @param value
   *   The value to insert.
   */ 
  public void 
  put
  (
    K key, 
    V value
  )
  {
    if(key == null) 
      throw new IllegalArgumentException("The key cannot be (null)!");
    
    TreeSet<V> set = this.get(key);
    if(set == null) {
      set = new TreeSet<V>();
      set.add(value);
      this.put(key, set);
    }
    else
      set.add(value);
  }
  
  /**
   * Associates a <code>null</code> value with the specified key.
   *
   * @param key
   *   The first key.
   *
   * @return
   *   Any value that might have existed in the now <code>null</code> mapping.
   */
  public TreeSet<V> 
  put
  (
    K key
  )
  {
    if(key == null)
      throw new IllegalArgumentException("The key cannot be (null)!");
   
    return super.put(key, null);
  }
  
  @Override
  public TreeSet<V> 
  put
  (
    K key, 
    TreeSet<V> value
  ) 
  {
    if (value == null || value.isEmpty())
      return super.put(key, null);
    else
      return super.put(key, value);
  }
  
  /**
   * Inserts all the of key/value mappings from the given map into this map.
   * 
   * @param mset
   *   The map to insert.
   */ 
  public void
  putAll
  (
   MappedSet<K,V> mset
  )  
  {
    for(K a : mset.keySet()) {
      TreeSet<V> values = mset.get(a);
      if (values == null || values.isEmpty())
        put(a);
      else {
        for (V each : values )
          put(a, each);
      }
    }
  }
  
  /**
   * Type-safe getter method.
   */
  public TreeSet<V> 
  get
  (
    K key
  )
  {
    return super.get(key);
  }
  
  /**
   * Remove the value from the set defined by the key.
   * <p>
   * If the key does not map to a set or if the value is not in the set, no exception will be
   * thrown.
   * 
   * @param key
   *   The key which indicates the set that is being edited.  
   * 
   * @param value
   *   The value which should be removed from the set.
   */
  public void
  remove
  (
    K key,
    V value
  )
  {
    TreeSet<V> set = get(key);
    if (set != null) {
      set.remove(value);
      if (set.isEmpty())
        put(key);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2368187723005633955L;

}
