package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A P P E D   L I N K E D   L I S T                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 *  A {@link TreeMap} that contains {@link LinkedList}s.
 */
public 
class MappedLinkedList<K, V>
  extends TreeMap<K, LinkedList<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map. 
   */
  public 
  MappedLinkedList()
  {
    super();
  }
  
  /**
   * Deep copy contructor.
   */
  public 
  MappedLinkedList
  (
    MappedLinkedList<K, V> mlist
  )
  {
    super();
    putAll(mlist);
  }
  
  /**
   * Inverts a {@link Map} into a {@link MappedLinkedList}.
   * <p>
   * The values in the map will be converted into the keys of the new
   * MappedLinkedList.  So if you had a Map with the following values
   * <ul>
   *   <li>Boy 1
   *   <li>Girl 2
   *   <li>Horse 1
   *   <li>Duck 4
   * </ul>
   * Then the new MappedLinkedList would look like.
   * <ul>
   *   <li> 1 Boy, Horse
   *   <li> 2 Girl
   *   <li> 4 Duck
   * </ul>
   */
  public 
  MappedLinkedList
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
    
    LinkedList<V> list = this.get(key);
    if(list == null) {
      list = new LinkedList<V>();
      list.add(value);
      this.put(key, list);
    }
    else
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
   MappedLinkedList<K,V> mlist
  )  
  {
    for(K a : mlist.keySet()) {
      LinkedList<V> values = mlist.get(a);
      if (values == null || values.isEmpty())
        put(a);
      else {
        for (V each : values)
          put(a, each);
      }
    }
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
  public LinkedList<V> 
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
  public LinkedList<V> 
  put
  (
    K key, 
    LinkedList<V> value
  ) 
  {
    if (value == null || value.isEmpty())
      return super.put(key, null);
    else
      return super.put(key, value);
  }
  
  /**
   * Type-safe getter method.
   */
  public LinkedList<V> get
  (
    K key
  )
  {
    return super.get(key);
  }
  
  /**
   * Remove the first instance of the value from the list defined by the key.
   * <p>
   * If the key does not map to a list or if the value is not in the list, no exception 
   * will be thrown.
   * 
   * @param key
   *   The key which indicates the list that is being edited.  
   * 
   * @param value
   *   The value which should be removed from the list.
   */
  public void
  remove
  (
    K key,
    V value
  )
  {
    LinkedList<V> list = get(key);
    if (list != null) {
      list.remove(value);
      if (list.isEmpty())
        put(key);
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 5426868408480655290L;
}
