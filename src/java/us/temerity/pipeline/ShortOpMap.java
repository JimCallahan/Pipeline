// $Id: ShortOpMap.java,v 1.1 2009/11/06 00:49:36 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S H O R T   O P   M A P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Short values where calls to the apply() method apply a Short operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   ShortOpMap<String> map = new ShortOpMap<String>();
 *   map.apply("horse", 10);                       // <key="horse", value=10>
 *   map.apply("horse", 5);                        // <key="horse", value=15>
 *   map.apply("horse", 3, BaseOpMap.Op.Mutiply);  // <key="horse", value=45>
 *   map.apply("horse", 5, BaseOpMap.Op.Subtract); // <key="horse", value=40>
 *   map.apply("horse", null);                     // <key="horse", value=null>
 * </code>
 */
public 
class ShortOpMap<K>
  extends BaseOpMap<K, Short>
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
  ShortOpMap()
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
  ShortOpMap
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
  ShortOpMap
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
  ShortOpMap
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
  ShortOpMap
  (
   Map<? extends K, ? extends Short> m
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
  ShortOpMap
  (
   Map<? extends K, ? extends Short> m,
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
  ShortOpMap
  (
   SortedMap<K, ? extends Short> m
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
  ShortOpMap
  (
   SortedMap<K, ? extends Short> m,
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
  protected Short
  add
  (
    Short first, 
    Short second
  )
  {
    return (short) ((int) second + (int) first);
  }
   
  /**
   * Subtract the second value to the first value: (second - first)
   */
  protected Short
  subtract
  (
    Short first, 
    Short second
  )
  {
    return (short) ((int) second - (int) first);
  }

  /**
   * Mutiply the two values: (second * first)
   */
  protected Short
  multiply
  (
    Short first, 
    Short second
  )
  {
    return (short) ((int) second * (int) first);
  }
   
  /**
   * Divide the second value by the first value: (second / first)
   */
  protected Short
  divide
  (
    Short first, 
    Short second
  )
  {
    return (short) ((int) second / (int) first);
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Short
  min
  (
    Short first, 
    Short second
  )
  {
    return (short) Math.min((int) second, (int) first);
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Short
  max
  (
    Short first, 
    Short second
  )
  {
    return (short) Math.max((int) second, (int) first);    
  }
   
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8185668343498847919L;
}
