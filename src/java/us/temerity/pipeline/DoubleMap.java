// $Id: DoubleMap.java,v 1.2 2006/05/07 21:18:17 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   M A P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A two level deep TreeMap.
 */
public
class DoubleMap<A,B,V> 
 extends TreeMap<A,TreeMap<B,V>>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty map.
   */ 
  public 
  DoubleMap()
  {
    super();
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  DoubleMap
  (
   DoubleMap<A, B, V> tmap
  )
  {
    super();
    putAll(tmap);
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Associates a value with the specified set of keys in this map.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
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

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) {
      tableB = new TreeMap<B,V>();
      put(keyA, tableB);
    }

    tableB.put(keyB, value);
  }

  /**
   * Inserts all the of key/value mappings from the given map into this map.
   * 
   * @param tmap
   *   The map to insert.
   */ 
  public void
  putAll
  (
   DoubleMap<A,B,V> tmap
  )  
  {
    for(A a : tmap.keySet()) {
      for(B b : tmap.keySet(a)) {
	put(a, b, tmap.get(a, b));
      }
    }
  }

  /**
   * Returns the value to which this map maps the specified set of keys.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
   * 
   * @return
   *   The value or <CODE>null</CODE> if no entry exists.
   */ 
  public V
  get
  (
   A keyA,
   B keyB
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) 
      return null;
   
    return tableB.get(keyB);
  }

  /**
   * Removes the mapping for the specified set of keys if present.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
   */ 
  public void
  remove
  (
   A keyA,
   B keyB
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) 
      return;

    tableB.remove(keyB);

    if(tableB.isEmpty())
      super.remove(keyA);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the second set of keys given the first key index. 
   * 
   * @param keyA
   *   The first key.
   * 
   * @return
   *   The keys or <CODE>null</CODE> if no entry exists.
   */ 
  public Set<B>
  keySet
  (
   A keyA
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) 
      return null;

    return tableB.keySet(); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 929010464107527733L;
  
}
