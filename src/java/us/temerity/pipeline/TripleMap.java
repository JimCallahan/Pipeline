// $Id: TripleMap.java,v 1.2 2005/06/28 18:05:21 jim Exp $

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

    for(A keyA : tmap.keySet()) {
      TreeMap<B,TreeMap<C,V>> tableB = new TreeMap<B,TreeMap<C,V>>();
      put(keyA, tableB);
      
      for(B keyB : tmap.get(keyA).keySet()) 
	tableB.put(keyB, new TreeMap<C,V>(tmap.get(keyA).get(keyB)));
    }
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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -477650821460977295L;
  
}
