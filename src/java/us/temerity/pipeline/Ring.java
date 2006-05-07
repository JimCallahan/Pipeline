// $Id: Ring.java,v 1.1 2006/05/07 21:24:04 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R I N G                                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A ring buffer collection.
 */
public
class Ring<V> 
  extends AbstractCollection<V> 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new ring buffer.
   * 
   * @param size
   *   The number of elements stored in the ring.
   * 
   * @param value
   *   The initial value of the elements.
   */ 
  public 
  Ring
  (
   int size, 
   V value
  )
  {
    pElems = new ArrayList<V>(size);
    int wk;
    for(wk=0; wk<size; wk++) 
      pElems.add(value);
  }  

  /**
   * Copy another collection. 
   */ 
  public 
  Ring
  (
   Collection<V> ring
  )
  {
    pElems = new ArrayList<V>(ring.size());
    for(V value : ring) 
      pElems.add(value);
  }  


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add an element to the ring buffer at the current position (replacing the existing 
   * value) and then advance to the next position. <P> 
   * 
   * The current position cyles around the ring buffer so that every {@link #size} calls
   * to this method will overwrite the same element.
   */ 
  public boolean 
  add
  (
   V value
  )
  {
    pElems.set(pCurIdx, value);

    pCurIdx++;
    pCurIdx = pCurIdx % pElems.size();

    return true;
  }

  /**
   * Removes all of the elements from this collection (optional operation).   <P> 
   * 
   * This operation is not supported.   
   */ 
  public void 
  clear()
  {
    throw new UnsupportedOperationException();
  }
       
  /**
   * Returns an iterator over the elements contained in this collection. <P> 
   * 
   * Note that the returned iterator always starts with the next element in the 
   * ring buffer and ends with the current element. 
   */ 
  public Iterator<V> 	
  iterator()
  {
    return new RingIterator(pCurIdx);
  }

  /**
   * Removes a single instance of the specified element from this collection, if it is 
   * present (optional operation).<P> 
   * 
   * This operation is not supported.
   */ 
  public boolean 	
  remove
  (
   Object o
  )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Removes from this collection all of its elements that are contained in the specified 
   * collection (optional operation).<P> 
   * 
   * This operation is not supported.
   */ 
  public boolean 
  removeAll
  (
   Collection<?> c
  )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Retains only the elements in this collection that are contained in the specified 
   * collection (optional operation). <P> 
   * 
   * This operation is not supported.
   */ 
  public boolean 	
  retainAll
  (
   Collection<?> c
  )
  {
    throw new UnsupportedOperationException();    
  }

  /**
   * Returns the number of elements in this collection.
   */ 
  public int 	
  size()
  {
    return pElems.size();
  }
          

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private class
  RingIterator
    implements Iterator<V> 
  {
    public 
    RingIterator
    (
     int startIdx
    )
    {
      pIdx = (startIdx + 1) % pElems.size();
    } 

    /**
     * Returns true if the iteration has more elements.
     */
    public boolean
    hasNext()
    {
      return (pIdx != pCurIdx);
    }

    /**
     * Returns the next element in the iteration.
     */
    public V 	
    next()
    {
      pIdx++;
      pIdx = pIdx % pElems.size();

      return pElems.get(pIdx);
    }
          
    /**
     * Removes from the underlying collection the last element returned by the 
     * iterator (optional operation).
     */ 
    public void 
    remove()
    {
      throw new UnsupportedOperationException();    
    }

    private int pIdx; 
  }


          
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  // private static final long serialVersionUID = 
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The current index.
   */ 
  private int  pCurIdx; 

  /**
   * The ring buffer elements. 
   */ 
  private ArrayList<V> pElems; 
}
