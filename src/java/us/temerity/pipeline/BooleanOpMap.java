// $Id: BooleanOpMap.java,v 1.2 2009/11/10 20:48:25 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   O P   M A P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of Boolean values where calls to the apply() method apply a Boolean operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   BooleanOpMap<String> map = new BooleanOpMap<String>();
 *   map.apply("horse", true);                         // <key="horse", value=true>
 *   map.apply("horse", false);                        // <key="horse", value=true>
 *   map.apply("horse", false, BaseOpMap.Op.Mutiply);  // <key="horse", value=false>
 *   map.apply("horse", null);                         // <key="horse", value=null>
 * </code>
 */
public 
class BooleanOpMap<K>
  extends BaseOpMap<K, Boolean>
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
  BooleanOpMap()
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
  BooleanOpMap
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
  BooleanOpMap
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
  BooleanOpMap
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
  BooleanOpMap
  (
   Map<? extends K, ? extends Boolean> m
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
  BooleanOpMap
  (
   Map<? extends K, ? extends Boolean> m,
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
  BooleanOpMap
  (
   SortedMap<K, ? extends Boolean> m
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
  BooleanOpMap
  (
   SortedMap<K, ? extends Boolean> m,
   Op op
  )
  {
    super(m, op); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P E R A T O R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the two values: (second || first)
   */
  @Override
  protected Boolean
  add
  (
    Boolean first, 
    Boolean second
  )
  {
    return second || first;
  }
   
  /**
   * Subtract the second value to the first value (not unsupported).
   */
  @Override
  protected Boolean
  subtract
  (
    Boolean first, 
    Boolean second
  )
  {
    throw new UnsupportedOperationException("The subtract method is not supported!");
  }

  /**
   * Multiply the two values: (second && first)
   */
  @Override
  protected Boolean
  multiply
  (
    Boolean first, 
    Boolean second
  )
  {
    return second && first;
  }
   
  /**
   * Divide the first value by the second value: (not supported)
   */
  @Override
  protected Boolean
  divide
  (
    Boolean first, 
    Boolean second
  )
  {
    throw new UnsupportedOperationException("The divide method is not supported!");
  }
   
  /**
   * The minimum of the two values (not unsupported).
   */
  @Override
  protected Boolean
  min
  (
    Boolean first, 
    Boolean second
  )
  {
    throw new UnsupportedOperationException("The divide method is not supported!");
  }
   
  /**
   * The minimum of the two values (not unsupported).
   */
  @Override
  protected Boolean
  max
  (
    Boolean first, 
    Boolean second
  )
  {
    throw new UnsupportedOperationException("The divide method is not supported!");
  }
   
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -7979473903351604489L;
}
