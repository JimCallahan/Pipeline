// $Id: ComparableTreeSet.java,v 1.1 2006/09/21 15:55:20 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   */
/*------------------------------------------------------------------------------------------*/

/**
 * 
 */
public
class ComparableTreeSet<E> 
 extends TreeSet<E> 
 implements Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new, empty set, sorted according to the elements' natural order.
   */
  public 
  ComparableTreeSet() 
  {
    super();
  }


  /**
   * Constructs a new set containing the elements in the specified collection, sorted 
   * according to the elements' natural order.
   */
  public 
  ComparableTreeSet
  (
   Collection<? extends E> c
  )
  {
    super(c);
  }

  /**
   * Constructs a new, empty set, sorted according to the specified comparator.
   */
  public 
  ComparableTreeSet
  (
   Comparator<? super E> c
  )
  {
    super(c);
  }

  /**
   * Constructs a new set containing the same elements as the specified sorted set, 
   * sorted according to the same ordering.
   */
  public    
  ComparableTreeSet
  (
   SortedSet<E> s
  )
  {
    super(s);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof ComparableTreeSet))
      throw new IllegalArgumentException
	("The object to compare was NOT a ComparableTreeSet!");

    return compareTo((ComparableTreeSet<E>) obj);
  }  

  /**
   * Compares this <CODE>ComparableTreeSet</CODE> with the given 
   * <CODE>ComparableTreeSet</CODE> for order.
   * 
   * @param s
   *   The set to be compared.
   */
  public int
  compareTo
  (
   ComparableTreeSet<E> s
  )
  {
    if(s == null) 
      throw new NullPointerException();

    Iterator<E> iter  = iterator();
    Iterator<E> siter = s.iterator();

    boolean next = false; 
    boolean snext = false; 
    while(true) {
      next = iter.hasNext();
      snext = siter.hasNext();
      if(!next || !snext) 
	break;

      Comparable val  = (Comparable) iter.next();
      Comparable sval = (Comparable) siter.next();
      int order = val.compareTo(sval);
      if(order != 0) 
	return order;
    }

    if(next) 
      return 1;
    else if(snext)
      return -1;
    return 0;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1628415843769751397L;
  
}
