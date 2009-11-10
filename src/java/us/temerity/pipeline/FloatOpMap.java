// $Id: FloatOpMap.java,v 1.2 2009/11/10 23:51:53 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F L O A T   O P   M A P                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Float values where calls to the apply() method apply a Float operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   FloatOpMap<String> map = new FloatOpMap<String>();
 *   map.apply("horse", 10.0f);                       // <key="horse", value=10.0f>
 *   map.apply("horse", 5.0f);                        // <key="horse", value=15.0f>
 *   map.apply("horse", 3.0f, BaseOpMap.Op.Mutiply);  // <key="horse", value=45.0f>
 *   map.apply("horse", 5.0f, BaseOpMap.Op.Subtract); // <key="horse", value=40.0f>
 *   map.apply("horse", null);                        // <key="horse", value=null>
 * </code>
 */
public 
class FloatOpMap<K>
  extends BaseOpMap<K, Float>
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
  FloatOpMap()
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
  FloatOpMap
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
  FloatOpMap
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
  FloatOpMap
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
  FloatOpMap
  (
   Map<? extends K, ? extends Float> m
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
  FloatOpMap
  (
   Map<? extends K, ? extends Float> m,
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
  FloatOpMap
  (
   SortedMap<K, ? extends Float> m
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
  FloatOpMap
  (
   SortedMap<K, ? extends Float> m,
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
  protected Float
  add
  (
    Float first, 
    Float second
  )
  {
    return second + first;
  }
   
  /**
   * Subtract the first value from the second value: (first - second)
   */
  @Override
  protected Float
  subtract
  (
    Float first, 
    Float second
  )
  {
    return first - second;
  }

  /**
   * Multiply the two values: (first * second)
   */
  @Override
  protected Float
  multiply
  (
    Float first, 
    Float second
  )
  {
    return second * first;
  }
   
  /**
   * Divide the first value by the second value: (first / second)
   */
  @Override
  protected Float
  divide
  (
    Float first, 
    Float second
  )
  {
    return first / second;
  }
   
  /**
   * The minimum of the two values. 
   */
  @Override
  protected Float
  min
  (
    Float first, 
    Float second
  )
  {
    return Math.min(second, first);
  }
   
  /**
   * The minimum of the two values. 
   */
  @Override
  protected Float
  max
  (
    Float first, 
    Float second
  )
  {
    return Math.max(second, first);    
  }
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -1124076129258111555L;

}
