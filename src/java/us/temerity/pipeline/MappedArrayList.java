// $Id: MappedArrayList.java,v 1.1 2007/03/18 02:24:20 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A P P E D   A R R A Y   L I S T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 *  A {@link TreeMap} that contains {@link ArrayList}s.
 */
public class MappedArrayList<K, V>
  extends TreeMap<K, ArrayList<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map. 
   */
  public MappedArrayList()
  {
    super();
  }
  
  /**
   * Deep copy contructor.
   */
  public MappedArrayList
  (
    MappedArrayList<K, V> mlist
  )
  {
    super();
    putAll(mlist);
  }
  
  /**
   * Inverts a {@link Map} into a {@link MappedArrayList}.
   * <p>
   * The values in the map will be converted into the keys of the new
   * MappedArrayList.  So if you had a Map with the following values
   * <ul>
   *   <li>Boy 1
   *   <li>Girl 2
   *   <li>Horse 1
   *   <li>Duck 4
   * </ul>
   * Then the new MappedArrayList would look like.
   * <ul>
   *   <li> 1 Boy, Horse
   *   <li> 2 Girl
   *   <li> 4 Duck
   * </ul>
   */
  public MappedArrayList
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
    
    ArrayList<V> list = this.get(key);
    if(list == null) {
      list = new ArrayList<V>();
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
   MappedArrayList<K,V> mlist
  )  
  {
    for(K a : mlist.keySet()) 
      for (V each : mlist.get(a))
	put(a, each);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -5605694581155130136L;
}