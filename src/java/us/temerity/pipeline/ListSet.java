// $Id: ListSet.java,v 1.2 2010/01/07 22:23:37 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   S E T                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 *  A {@link Set} that preserves its entries in a user specified order.
 *  <p>
 *  The ordering is based upon the order than things are added to the collection. 
 *  <p>
 *  This implementation is inherently slower than something like a {@link TreeSet}. 
 *  The very nature of the keySet in this implementation requires an O(n) cost search
 *  every time something is inserted or removed as opposed to the O(log(n)) provided by 
 *  {@link TreeSet}.  Therefore this class should only be used where the ordering qualities 
 *  it provides are necessary.
 *  <p>
 *  One other advantage of this class is that unlike the {@link TreeSet} it does not require
 *  its dataType to implement {@link Comparable}.
 *  
 *  @param <E>
 *    The type of elements maintained by this set.
 *    
 *  @see Set
 *  @see TreeSet
 */
public 
class ListSet<E>
  extends AbstractSet<E>
  implements Set<E>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new, empty set.
   */
  public 
  ListSet() 
  {
    pMap = new ListMap<E,Object>();
  }

  /**
   * Constructs a new set containing the elements in the specified
   * collection. 
   *
   * @param c
   *   The collection whose elements are to be placed into this set
   */
  public ListSet
  (
    Collection<? extends E> c
  ) 
  {
    pMap = new ListMap<E,Object>();
    addAll(c);
  }

  @Override
  public boolean 
  add
  (
    E e
  )
  {
    return pMap.put(e, PRESENT) == null;
  }
  
  /**
   * Return the entry at the specified position in this set. 
   * 
   * @param index
   *   the position index
   */
  public E
  get
  (
    int index
  )
  {
    return pMap.getKey(index);
  }

  @Override
  public void 
  clear()
  {
    pMap.clear();
  }

  @Override
  public boolean 
  contains
  (
    Object o
  )
  {
    return pMap.containsKey(o);
  }

  @Override
  public boolean 
  isEmpty()
  {
    return pMap.isEmpty();
  }

  @Override
  public Iterator<E> 
  iterator()
  {
    return pMap.keySet().iterator();
  }

  @Override
  public boolean 
  remove
  (
    Object o
  )
  {
    return pMap.remove(o) == PRESENT;
  }

  @Override
  public int 
  size()
  {
    return pMap.size();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  // Dummy value to associate with an Object in the backing Map
  private static final Object PRESENT = new Object();


  
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R N A L S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  private transient ListMap<E,Object> pMap;
}
