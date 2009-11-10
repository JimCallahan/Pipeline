// $Id: LongOpMap.java,v 1.2 2009/11/10 20:48:25 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   L O N G   O P   M A P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Long values where calls to the apply() method apply a Long operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   LongOpMap<String> map = new LongOpMap<String>();
 *   map.apply("horse", 10);                       // <key="horse", value=10>
 *   map.apply("horse", 5);                        // <key="horse", value=15>
 *   map.apply("horse", 3, BaseOpMap.Op.Mutiply);  // <key="horse", value=45>
 *   map.apply("horse", 5, BaseOpMap.Op.Subtract); // <key="horse", value=40>
 *   map.apply("horse", null);                     // <key="horse", value=null>
 * </code>
 */
public
class LongOpMap<K>
  extends BaseOpMap<K, Long>
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
  LongOpMap()
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
  LongOpMap
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
  LongOpMap
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
  LongOpMap
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
  LongOpMap
  (
   Map<? extends K, ? extends Long> m
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
  LongOpMap
  (
   Map<? extends K, ? extends Long> m,
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
  LongOpMap
  (
   SortedMap<K, ? extends Long> m
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
  LongOpMap
  (
   SortedMap<K, ? extends Long> m,
   Op op
  )
  {
    super(m, op); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P E R A T O R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the two values: (first + second)
   */
  @Override
  protected Long
  add
  (
    Long first, 
    Long second
  )
  {
    return second + first;
  }
   
  /**
   * Subtract the first value from the second value: (first - second)
   */
  @Override
  protected Long
  subtract
  (
    Long first, 
    Long second
  )
  {
    return first - second;
  }

  /**
   * Multiply the two values: (first * second)
   */
  @Override
  protected Long
  multiply
  (
    Long first, 
    Long second
  )
  {
    return second * first;
  }
   
  /**
   * Divide the first value by the second value: (first / second)
   */
  @Override
  protected Long
  divide
  (
    Long first, 
    Long second
  )
  {
    return first / second;
  }
   
  /**
   * The minimum of the two values. 
   */
  @Override
  protected Long
  min
  (
    Long first, 
    Long second
  )
  {
    return Math.min(second, first);
  }
   
  /**
   * The minimum of the two values. 
   */
  @Override
  protected Long
  max
  (
    Long first, 
    Long second
  )
  {
    return Math.max(second, first);    
  }
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1674892115037161405L;
}
