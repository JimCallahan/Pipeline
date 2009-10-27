// $Id: ListPathSet.java,v 1.2 2009/10/27 18:39:42 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L I S T   P A T H   S E T                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of unique arbitrary length paths of comparable keys represented using lists.<P> 
 * 
 * This is implemented as a heirarchical tree-like data structures where the elements of
 * these lists are treated as paths down the heirarchy.  Each element of the list therefore
 * corresponds to a level of the heirarchy.  In this way, lists which share a common
 * sequence of initial elements share keys within this tree-like data structure.  This can
 * be particularly useful when the lists in question are used to represent file system paths 
 * or similar lists which inherently have a high degree of similarity.<P> 
 * 
 * Note that this class does not implement the Set interface even though it does implement
 * some of the methods from Set.  The reason being that this class is less general and not
 * all of the Set methods make sense when dealing with arbitrary length lists as elements.<P>
 * 
 * The lists managed by this set must have non-null and Comparable elements.
 */
public
class ListPathSet<E>
  implements Iterable<LinkedList<E>>, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an empty set.
   */ 
  public 
  ListPathSet()
  {
    pPaths = new TreeMap<E,ListPathSet<E>>();
  }

  /**
   * Deep copy constructor. 
   */ 
  public 
  ListPathSet
  (
   ListPathSet<E> set
  )
  {
    pPaths = new TreeMap<E,ListPathSet<E>>();
    for(List<E> ls : this) 
      add(ls);
  }  

  

  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
    
  /**
   * Returns true if this set contains no elements.
   */ 
  public boolean 	
  isEmpty()
  {
    return pPaths.isEmpty();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns true if this set contains the specified list.
   */ 
  public boolean 	
  contains
  (
   List<E> ls
  )
  {
    sanityCheck(ls, "tested for membership in"); 
    return containsHelper(ls.listIterator());
  }    

  /**
   * Recursively test the remaining elements of the list.
   */ 
  private boolean
  containsHelper
  (
   ListIterator<E> iter
  ) 
  {
    if(iter.hasNext()) {
      E elem = iter.next();
      ListPathSet<E> next = pPaths.get(elem);
      if(next == null) 
        return false;

      return next.containsHelper(iter); 
    }
    else {
      return pIsTerminal;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Return true if any of the contained lists begins with the given element.
   */ 
  public boolean
  containsFirstElement
  (
   E elem
  ) 
  {
    return pPaths.keySet().contains(elem);
  }
    
    

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Returns the number of unique lists in this set.
   */ 
  public int 	
  size()
  {
    int size = 0;
    for(ListPathSet<E> p : pPaths.values()) 
      size += p.size();
      
    return size;
  }

  /**
   * Removes all of the elements from this set.
   */ 
  public void 	
  clear()
  {
    pIsTerminal = false;
    pPaths.clear();
  }

    
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Adds the specified list to this set if it is not already present. 
   * 
   * @param ls
   *   The list to add.
   * 
   * @return
   *   True if this set did not already contain the specified element.
   */
  public boolean 
  add
  (
   List<E> ls
  )
  {
    sanityCheck(ls, "added to"); 
    return addHelper(ls.listIterator());
  }
    
  /**
   * Recursively add the remaining elements of the list.
   */ 
  private boolean
  addHelper
  (
   ListIterator<E> iter
  ) 
  {
    boolean unique = false;
    if(iter.hasNext()) {
      E elem = iter.next();
      ListPathSet<E> next = pPaths.get(elem);
      if(next == null) {
        next = new ListPathSet<E>();
        pPaths.put(elem, next); 
        unique = true;
      }

      if(next.addHelper(iter)) 
        unique = true;
    }
    else {
      unique = !pIsTerminal;
      pIsTerminal = true;
    }
    
    return unique;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Removes the specified list from this set if it is present.
   * 
   * @param ls
   *   The list to be removed from this set, if present.
   * 
   * @return
   *   True if this set contained the specified list.
   */ 
  public boolean 	
  remove
  (
   List<E> ls
  )
  {
    sanityCheck(ls, "removed from"); 
    return removeHelper(ls.listIterator());
  }

  /**
   * Recursively remove the remaining elements of the list.
   */ 
  private boolean
  removeHelper
  (
   ListIterator<E> iter
  ) 
  { 
    if(iter.hasNext()) {
      E elem = iter.next();
      ListPathSet<E> next = pPaths.get(elem);
      if(next == null) 
        return false;

      boolean found = next.removeHelper(iter);
      if(next.isEmpty() && !next.pIsTerminal) 
        pPaths.remove(elem); 
      return found;
    }
    else {
      boolean found = pIsTerminal;
      pIsTerminal = false;
      return found;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Return the unique set of first elements from all lists.
   */ 
  public Set<E> 
  getFirstElements() 
  {
    return Collections.unmodifiableSet(pPaths.keySet());
  }
    
    

  /*----------------------------------------------------------------------------------------*/
  /*   I T E R A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns an iterator over a set of elements. <P> 
   *
   * Note that this operation requires first converting to a nested list representation and
   * therefore has a considerable amount of overhead.
   */
  public Iterator<LinkedList<E>> 
  iterator() 
  {
    return toNestedList().iterator(); 
  }

  /**
   * Convert to a list of lists.
   */ 
  public LinkedList<LinkedList<E>>
  toNestedList() 
  {
    LinkedList<LinkedList<E>> nested = new LinkedList<LinkedList<E>>(); 
    for(Map.Entry<E,ListPathSet<E>> entry : pPaths.entrySet()) {
      LinkedList<E> current = new LinkedList<E>();
      current.add(entry.getKey());
      entry.getValue().toNestedListHelper(current, nested);
    }
      
    return nested;
  }

  /**
   * Convert to a list of lists.
   */ 
  private void
  toNestedListHelper
  (
   LinkedList<E> current, 
   LinkedList<LinkedList<E>> nested 
  ) 
  {
    if(pIsTerminal) 
      nested.add(new LinkedList<E>(current)); 

    for(Map.Entry<E,ListPathSet<E>> entry : pPaths.entrySet()) {
      current.add(entry.getKey());
      entry.getValue().toNestedListHelper(current, nested);
    }

    current.removeLast(); 
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
    if(pIsTerminal) 
      encoder.encode("IsTerminal", true);

    if(!pPaths.isEmpty()) 
      encoder.encode("Paths", pPaths); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Boolean tf = (Boolean) decoder.decode("IsTerminal");
    if(tf != null) 
      pIsTerminal = tf;
      
    TreeMap<E,ListPathSet<E>> paths = 
      (TreeMap<E,ListPathSet<E>>) decoder.decode("Paths");
    if(paths != null) 
      pPaths = paths;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Test the list argument for empty and null elements.
   */ 
  private void 
  sanityCheck
  (
   List<E> ls, 
   String msg
  ) 
  {
    if(ls.isEmpty())
      throw new IllegalArgumentException
        ("Empty lists cannot be " + msg + " a ListPathSet!");
    
    for(E elem : ls) {
      if(elem == null) 
        throw new IllegalArgumentException
          ("Lists with (null) elements cannot be " + msg + " a ListPathSet!");
    }
  }

       

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5132968879058325258L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The paths which share a common set of initial list elements.<P> 
   * 
   * The key in this map is the list element at the current level of the hierarchy, while
   * the value contains all remaining paths which share that element.
   */ 
  private TreeMap<E,ListPathSet<E>> pPaths;

  /**
   * Whether this level of the heirarchy represents the last element of a list.
   */ 
  private boolean pIsTerminal; 
  
}
