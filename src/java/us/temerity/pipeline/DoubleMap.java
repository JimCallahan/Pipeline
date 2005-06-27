// $Id: DoubleMap.java,v 1.1 2005/06/27 07:16:54 jim Exp $

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

    for(A key : tmap.keySet()) 
      put(key, new TreeMap<B,V>(tmap.get(key)));
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
    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) {
      tableB = new TreeMap<B,V>();
      put(keyA, tableB);
    }

    tableB.put(keyB, value);
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
    TreeMap<B,V> tableB = super.get(keyA);
    if(tableB == null) 
      return;

    tableB.remove(keyB);

    if(tableB.isEmpty())
      super.remove(keyA);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 929010464107527733L;
  
}
