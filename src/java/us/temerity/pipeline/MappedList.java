package us.temerity.pipeline;

import java.util.*;

/**
 *  A {@link TreeMap} that contains {@link LinkedList}s.
 */
public class MappedList<K, V>
  extends TreeMap<K, LinkedList<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map. 
   */
  public MappedList()
  {
    super();
  }
  
  /**
   * Deep copy contructor.
   */
  public MappedList
  (
    MappedList<K, V> mlist
  )
  {
    super();
    putAll(mlist);
  }
  
  /**
   * Inverts a {@link Map} into a {@link MappedList}.
   * <p>
   * The values in the map will be converted into the keys of the new
   * MappedList.  So if you had a Map with the following values
   * <ul>
   *   <li>Boy 1
   *   <li>Girl 2
   *   <li>Horse 1
   *   <li>Duck 4
   * </ul>
   * Then the new MappedList would look like.
   * <ul>
   *   <li> 1 Boy, Horse
   *   <li> 2 Girl
   *   <li> 4 Duck
   * </ul>
   */
  public MappedList
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
    
    LinkedList<V> list = this.get(key);
    if(list == null) {
      list = new LinkedList<V>();
      this.put(key, list);
    }
    list.add(value);
  }
  
  /**
   * Inserts all the of key/value mappings from the given map into this map.
   * 
   * @param mlist
   *   The map to insert.
   */ 
  public void
  putAll
  (
   MappedList<K,V> mlist
  )  
  {
    for(K a : mlist.keySet()) 
      for (V each : mlist.get(a))
	put(a, each);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7196857898120923302L;
}
