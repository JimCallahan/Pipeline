// $Id: IntegerOpMap.java,v 1.1 2009/11/06 00:49:36 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   O P   M A P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Integer values where calls to the apply() method apply an Integer operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   IntegerOpMap<String> map = new IntegerOpMap<String>();
 *   map.apply("horse", 10);                       // <key="horse", value=10>
 *   map.apply("horse", 5);                        // <key="horse", value=15>
 *   map.apply("horse", 3, BaseOpMap.Op.Mutiply);  // <key="horse", value=45>
 *   map.apply("horse", 5, BaseOpMap.Op.Subtract); // <key="horse", value=40>
 *   map.apply("horse", null);                     // <key="horse", value=null>
 * </code>
 */
public 
class IntegerOpMap<K>
  extends BaseOpMap<K, Integer>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs a new, empty tree map, using the natural ordering of its keys. 
   * 
   * The default numeric operator is Add.
   * 
   * @see TreeMap#TreeMap()
   */
  public 
  IntegerOpMap()
  {
    super();
  }

  /**
   * Constructs a new, empty tree map, using the natural ordering of its keys. 
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see TreeMap#TreeMap()
   */
  public 
  IntegerOpMap
  (
   Op op
  ) 
  {
    super(op);
  }

  /**
   * Constructs a new, empty tree map, ordered according to the given comparator.
   * 
   * The default numeric operator is Add.
   * 
   * @see TreeMap#TreeMap(Comparator)
   */
  public 
  IntegerOpMap
  (
   Comparator<? super K> comparator
  )
  {
    super(comparator);
  }

  /**
   * Constructs a new, empty tree map, ordered according to the given comparator.
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see TreeMap#TreeMap(Comparator)
   */
  public 
  IntegerOpMap
  (
   Comparator<? super K> comparator,
   Op op
  )
  {
    super(comparator, op); 
  }

  /**
   * Constructs a new tree map containing the same mappings as the given map, ordered 
   * according to the natural ordering of its keys
   * 
   * The default numeric operator is Add.
   * 
   * @see TreeMap#TreeMap(Map)
   */
  public 
  IntegerOpMap
  (
   Map<? extends K, ? extends Integer> m
  )
  {
    super(m);
  }

  /**
   * Constructs a new tree map containing the same mappings as the given map, ordered 
   * according to the natural ordering of its keys
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see TreeMap#TreeMap(Map)
   */
  public 
  IntegerOpMap
  (
   Map<? extends K, ? extends Integer> m,
   Op op
  )
  {
    super(m, op); 
  }

  /**
   * Constructs a new tree map containing the same mappings and using the same ordering as 
   * the specified sorted map.
   * 
   * The default numeric operator is Add.
   * 
   * @see TreeMap#TreeMap(SortedMap)
   */
  public 
  IntegerOpMap
  (
   SortedMap<K, ? extends Integer> m
  )
  {
    super(m);
  }
  
  /**
   * Constructs a new tree map containing the same mappings and using the same ordering as 
   * the specified sorted map.
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see TreeMap#TreeMap(SortedMap)
   */
  public 
  IntegerOpMap
  (
   SortedMap<K, ? extends Integer> m,
   Op op
  )
  {
    super(m, op); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P E R A T O R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the two values: (second + first)
   */
  protected Integer
  add
  (
    Integer first, 
    Integer second
  )
  {
    return second + first;
  }
   
  /**
   * Subtract the second value to the first value: (second - first)
   */
  protected Integer
  subtract
  (
    Integer first, 
    Integer second
  )
  {
    return second - first;
  }

  /**
   * Mutiply the two values: (second * first)
   */
  protected Integer
  multiply
  (
    Integer first, 
    Integer second
  )
  {
    return second * first;
  }
   
  /**
   * Divide the second value by the first value: (second / first)
   */
  protected Integer
  divide
  (
    Integer first, 
    Integer second
  )
  {
    return second / first;
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Integer
  min
  (
    Integer first, 
    Integer second
  )
  {
    return Math.min(second, first);
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Integer
  max
  (
    Integer first, 
    Integer second
  )
  {
    return Math.max(second, first);    
  }
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -6792688191829405488L;

}
