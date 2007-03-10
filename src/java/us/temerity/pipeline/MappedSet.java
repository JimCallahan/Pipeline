package us.temerity.pipeline;

import java.util.*;

/**
 *  A {@link TreeMap} that contains {@link TreeSet}s.
 */
public class MappedSet<K, V>
  extends TreeMap<K, java.util.TreeSet<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Contructs an empty map.
   */
  public MappedSet()
  {
    super();
  }
  
  /**
   * Deep copy constructor.
   */
  public MappedSet
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
  public MappedSet
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
  public void put
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
      this.put(key, set);
    }
    set.add(value);
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
    for(K a : mset.keySet()) 
      for (V each : mset.get(a))
	put(a, each);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 2368187723005633955L;

}
