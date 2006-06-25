// $Id: RefCountTable.java,v 1.1 2006/06/25 23:33:49 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   R E F   C O U N T   T A B L E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A table storing reference counts for each key.
 */
public
class RefCountTable<T> 
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty table.
   */ 
  public 
  RefCountTable()
  {
    pTable = new TreeMap<T,Integer>();
  }  

  /**
   * Deep copy constructor. 
   */ 
  public 
  RefCountTable
  (
   RefCountTable<T> ref
  )
  {
    this();
    pTable.putAll(ref.pTable);
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the table contains a refernence count for the given key.
   * 
   * @param key
   *   The reference key. 
   */ 
  public boolean
  contains
  (
   T key
  ) 
  {
    return pTable.containsKey(key);
  }

  /**
   * Whether the table contains no reference counts.
   */ 
  public boolean
  isEmpty() 
  {
    return pTable.isEmpty();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Increment the reference count for the given key.
   * 
   * @param key
   *   The key to increment.
   */ 
  public void
  ref
  (
   T key
  ) 
  {
    if(key == null) 
      throw new IllegalArgumentException("The key cannot be (null)!");

    Integer cnt = pTable.get(key);
    if(cnt != null) 
      pTable.put(key, cnt+1);
    else 
      pTable.put(key, new Integer(1));
  }

  /**
   * Decrement the reference count for the given key.
   * 
   * @param key
   *   The key to decrement.
   * 
   * @return 
   *   The count prior to being decremented or <CODE>null</CODE> if the key did not exist.
   */ 
  public Integer
  unref
  (
   T key
  ) 
  {
    if(key == null) 
      throw new IllegalArgumentException("The key cannot be (null)!");

    Integer cnt = pTable.get(key);
    if(cnt != null) {
      if(cnt == 1) 
	pTable.remove(key);
      else 
	pTable.put(key, cnt-1);
    }

    return cnt;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the reference count for the given key.
   * 
   * @param key
   *   The key to lookup. 
   * 
   * @return 
   *   The current reference count or <CODE>null</CODE> if the key did not exist.
   */ 
  public Integer
  getCount
  (
   T key
  ) 
  {
    return pTable.get(key);
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of all referenced keys.
   */ 
  public Set<T> 
  getKeys() 
  {
    return Collections.unmodifiableSet(pTable.keySet());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    TreeMap<T,Integer> table = null;
    if(!pTable.isEmpty()) 
      table = pTable;

    encoder.encode("RefCounts", table);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    TreeMap<T,Integer> table = (TreeMap<T,Integer>) decoder.decode("RefCounts");
    if(table != null) 
      pTable.putAll(table);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8775708840584074610L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of reference counts.
   */ 
  private TreeMap<T,Integer>  pTable; 
  
}
