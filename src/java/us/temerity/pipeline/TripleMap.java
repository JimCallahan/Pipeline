// $Id: TripleMap.java,v 1.5 2006/10/23 18:29:31 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T R I P L E   M A P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A three level deep TreeMap. 
 */
public
class TripleMap<A,B,C,V> 
  extends TreeMap<A,TreeMap<B,TreeMap<C,V>>>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty map.
   */ 
  public 
  TripleMap()
  {
    super();
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  TripleMap
  (
   TripleMap<A,B,C,V> tmap
  )
  {
    super();
    putAll(tmap);
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns true if this map contains a mapping for the specified set of keys.
   */ 
  public boolean 	
  containsKey
  (
   A keyA,
   B keyB
  ) 
  {
    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    return ((tableB != null) && tableB.containsKey(keyB));
  }

  /**
   * Returns true if this map contains a mapping for the specified set of keys.
   */ 
  public boolean 	
  containsKey
  (
   A keyA,
   B keyB,
   C keyC
  ) 
  {
    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB != null) {
      TreeMap<C,V> tableC = tableB.get(keyB);
      return ((tableC != null) && tableC.containsKey(keyC));
    }
    return false;
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

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    if(keyC == null) 
      throw new IllegalArgumentException("The third key cannot be (null)!");

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) {
      tableB = new TreeMap<B,TreeMap<C,V>>();
      put(keyA, tableB);
    }

    TreeMap<C,V> tableC = tableB.get(keyB);
    if(tableC == null) {
      tableC = new TreeMap<C,V>();
      tableB.put(keyB, tableC);
    }

    tableC.put(keyC, value);
  }

  /**
   * Associates a value with the specified set of keys in this map.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
   * 
   * @param values
   *   The values indexed by the third key. 
   */ 
  public void
  put
  (
   A keyA,
   B keyB,
   TreeMap<C,V> values
  )  
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    if((values == null) || values.isEmpty()) 
      throw new IllegalArgumentException("The values cannot be (null) or empty!");

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) {
      tableB = new TreeMap<B,TreeMap<C,V>>();
      put(keyA, tableB);
    }

    tableB.put(keyB, values); 
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
   TripleMap<A,B,C,V> tmap
  )  
  {
    for(A a : tmap.keySet()) {
      for(B b : tmap.keySet(a)) {
	for(C c : tmap.keySet(a, b)) {
	  put(a, b, c, tmap.get(a, b, c));
	}
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
   * @param keyC
   *   The third key. 
   * 
   * @return
   *   The value or <CODE>null</CODE> if no entry exists.
   */ 
  public V
  get
  (
   A keyA,
   B keyB,
   C keyC
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    if(keyC == null) 
      throw new IllegalArgumentException("The third key cannot be (null)!");

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) 
      return null;

    TreeMap<C,V> tableC = tableB.get(keyB);
    if(tableC == null)
      return null; 

    return tableC.get(keyC);    
  }

  /**
   * Removes the mapping for the specified set of keys if present.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
   * 
   * @param keyC
   *   The third key. 
   */ 
  public void
  remove
  (
   A keyA,
   B keyB,
   C keyC
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    if(keyC == null) 
      throw new IllegalArgumentException("The third key cannot be (null)!");

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) 
      return;

    TreeMap<C,V> tableC = tableB.get(keyB);
    if(tableC == null) 
      return;

    tableC.remove(keyC);

    if(tableC.isEmpty()) 
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

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) 
      return null;

    return tableB.keySet(); 
  }

  /**
   * Returns the third set of keys given the first and second key indices. 
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *   The second key. 
   * 
   * @return
   *   The keys or <CODE>null</CODE> if no entry exists.
   */ 
  public Set<C>
  keySet
  (
   A keyA,
   B keyB
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");

    TreeMap<B,TreeMap<C,V>> tableB = super.get(keyA);
    if(tableB == null) 
      return null;

    TreeMap<C,V> tableC = tableB.get(keyB);
    if(tableC == null)
      return null; 

    return tableC.keySet(); 
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -477650821460977295L;
  
}
