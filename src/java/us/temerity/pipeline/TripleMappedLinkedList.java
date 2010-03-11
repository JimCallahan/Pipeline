// $Id: TripleMappedSet.java,v 1.2 2009/04/22 21:44:16 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T R I P L E   M A P P E D   S E T                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A {@link TripleMap} that contains {@link LinkedList LinkedLists}.
 * <p>
 * This class does automatic lazy creation of the lists when a value is added to them.
 *  
 * @param <A> 
 *   The type of the first set of keys maintained by this map.
 *
 * @param <B> 
 *   The type of the second set of keys maintained by this map.
 *   
 * @param <C> 
 *   The type of the third set of keys maintained by this map.
 *   
 * @param <V> 
 *   The type of the sets that are the mapped values.
 */
public 
class TripleMappedLinkedList<A, B, C, V>
  extends TripleMap<A, B, C, LinkedList<V>>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs an empty map.
   */
  public 
  TripleMappedLinkedList()
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
  TripleMappedLinkedList
  (
    TripleMappedLinkedList<A, B, C, V> mset
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
   *   The second key.
   *
   * @param keyC
   *   The third key.
   * 
   * @param value
   *   The value to insert.
   */ 
  public void 
  put
  (
    A keyA,
    B keyB,
    C keyC,
    V value
  )
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");
    
    if (keyB == null)
      throw new IllegalArgumentException("The second key cannot be (null)!");
    
    if (keyC == null)
      throw new IllegalArgumentException("The third key cannot be (null)!");
   
    
    LinkedList<V> set = this.get(keyA, keyB, keyC);
    if(set == null) {
      set = new LinkedList<V>();
      this.put(keyA, keyB, keyC, set);
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
   * @param keyC
   *   The third key.
   *
   * @return
   *   Any value that might have existed in the now <code>null</code> mapping.
   */
  public LinkedList<V> 
  put
  (
    A keyA,
    B keyB,
    C keyC
  )
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");
    
    if (keyB == null)
      throw new IllegalArgumentException("The second key cannot be (null)!");
    
    if (keyC == null)
      throw new IllegalArgumentException("The third key cannot be (null)!");
   
   
    return super.put(keyA, keyB, keyC, null);
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
    TripleMappedLinkedList<A,B,C,V> mset
  )  
  {
    for(A a : mset.keySet())
    for (B b : mset.keySet(a))
    for (C c : mset.keySet(a, b)) {
      LinkedList<V> values = mset.get(a, b, c);
      if (values == null)
        put(a, b, c);
      else {
        for (V each : values )
          put(a, b, c, each);
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
    B keyB,
    C keyC
  )
  {
    return super.get(keyA, keyB, keyC);
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4717043130363068296L;
}
