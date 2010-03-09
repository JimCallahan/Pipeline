// $Id: DoubleMappedSet.java,v 1.2 2009/04/22 21:44:16 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   M A P P E D   S E T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link DoubleMap} that contains {@link LinkedList LinkedLists}.
 * <p>
 * This class does automatic lazy creation of the lists when a value is added to them.
 *  
 * @param <A> 
 *   The type of the first set of keys maintained by this map.
 *
 * @param <B> 
 *   The type of the second set of keys maintained by this map.
 *   
 * @param <V> 
 *   The type of the sets that are the mapped values.
 */
public 
class DoubleMappedLinkedList<A,B,V>
  extends DoubleMap<A, B, LinkedList<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map.
   */
  public 
  DoubleMappedLinkedList()
  {
    super();
  }
  
  /**
   * Deep copy constructor.
   * 
   * @param mset
   *   The set to copy.
   */
  public 
  DoubleMappedLinkedList
  (
    DoubleMappedLinkedList<A, B, V> mset
  )
  {
    super();
    putAll(mset);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Associates a value with the specified keys.
   * 
   * @param keyA
   *   The first key.
   *   
   * @param keyB
   *   The second key
   * 
   * @param value
   *   The value to insert.
   */ 
  public void 
  put
  (
    A keyA,
    B keyB,
    V value
  )
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");
    
    if (keyB == null)
      throw new IllegalArgumentException("The second key cannot be (null)!");
    
    LinkedList<V> set = this.get(keyA, keyB);
    if(set == null) {
      set = new LinkedList<V>();
      this.put(keyA, keyB, set);
    }
    set.add(value);
  }
  
  /**
   * Associates a <code>null</code> value with the specified keys.
   *
   * @param keyA
   *   The first key.
   *   
   * @param keyB
   *   The second key.
   *
   * @return
   *   Any value that might have existed in the now <code>null</code> mapping.
   */
  public LinkedList<V> 
  put
  (
    A keyA,
    B keyB
  )
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");
    
    if (keyB == null)
      throw new IllegalArgumentException("The second key cannot be (null)!");
   
    return super.put(keyA, keyB, null);
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
    DoubleMappedLinkedList<A,B,V> mset
  )  
  {
    for( A a : mset.keySet())
    for (B b : mset.keySet(a)) {
      LinkedList<V> values = mset.get(a, b);
      if (values == null)
        put(a, b);
      else {
        for (V each : values)
          put(a, b, each);
      }
    }
  }

  /**
   * Type-safe getter method.
   */
  @Override
  public LinkedList<V> 
  get
  (
    A keyA,
    B keyB
  )
  {
    return super.get(keyA, keyB);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 687987491345420310L;
}
