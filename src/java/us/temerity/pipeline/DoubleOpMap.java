// $Id: DoubleOpMap.java,v 1.1 2009/11/06 00:49:36 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   D O U B L E   O P   M A P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Double values where calls to the apply() method apply a Double operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   DoubleOpMap<String> map = new DoubleOpMap<String>();
 *   map.apply("horse", 10.0);                       // <key="horse", value=10.0>
 *   map.apply("horse", 5.0);                        // <key="horse", value=15.0>
 *   map.apply("horse", 3.0, BaseOpMap.Op.Mutiply);  // <key="horse", value=45.0>
 *   map.apply("horse", 5.0, BaseOpMap.Op.Subtract); // <key="horse", value=40.0>
 *   map.apply("horse", null);                       // <key="horse", value=null>
 * </code>
 */
public 
class DoubleOpMap<K>
  extends BaseOpMap<K, Double>
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
  DoubleOpMap()
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
  DoubleOpMap
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
  DoubleOpMap
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
  DoubleOpMap
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
  DoubleOpMap
  (
   Map<? extends K, ? extends Double> m
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
  DoubleOpMap
  (
   Map<? extends K, ? extends Double> m,
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
  DoubleOpMap
  (
   SortedMap<K, ? extends Double> m
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
  DoubleOpMap
  (
   SortedMap<K, ? extends Double> m,
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
  protected Double
  add
  (
    Double first, 
    Double second
  )
  {
    return second + first;
  }
   
  /**
   * Subtract the second value to the first value: (second - first)
   */
  protected Double
  subtract
  (
    Double first, 
    Double second
  )
  {
    return second - first;
  }

  /**
   * Mutiply the two values: (second * first)
   */
  protected Double
  multiply
  (
    Double first, 
    Double second
  )
  {
    return second * first;
  }
   
  /**
   * Divide the second value by the first value: (second / first)
   */
  protected Double
  divide
  (
    Double first, 
    Double second
  )
  {
    return second / first;
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Double
  min
  (
    Double first, 
    Double second
  )
  {
    return Math.min(second, first);
  }
   
  /**
   * The minimum of the two values. 
   */
  protected Double
  max
  (
    Double first, 
    Double second
  )
  {
    return Math.max(second, first);    
  }
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 8420446369770819717L;

}
