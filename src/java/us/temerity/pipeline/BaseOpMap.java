// $Id: BaseOpMap.java,v 1.1 2009/11/06 00:49:36 jim Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   O P   M A P                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of numeric values where calls to the apply() method apply a numeric operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * This class has abstract methods for the common numeric operators which subclasses 
 * specialized for one of the numeric types will overload.
 */
public abstract 
class BaseOpMap<K, V>
  extends TreeMap<K, V>
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
  BaseOpMap()
  {
    this(Op.Add); 
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
  BaseOpMap
  (
   Op op
  ) 
  {
    super();
    setOperator(op); 
  }

  /**
   * Constructs a new, empty tree map, ordered according to the given comparator.
   * 
   * The default numeric operator is Add.
   * 
   * @see TreeMap#TreeMap(Comparator)
   */
  public 
  BaseOpMap
  (
   Comparator<? super K> comparator
  )
  {
    this(comparator, Op.Add);
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
  BaseOpMap
  (
   Comparator<? super K> comparator,
   Op op
  )
  {
    super(comparator);
    setOperator(op); 
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
  BaseOpMap
  (
   Map<? extends K, ? extends V> m
  )
  {
    this(m, Op.Add); 
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
  BaseOpMap
  (
   Map<? extends K, ? extends V> m,
   Op op
  )
  {
    super(m);
    setOperator(op); 
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
  BaseOpMap
  (
   SortedMap<K, ? extends V> m
  )
  {
    this(m, Op.Add);
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
  BaseOpMap
  (
   SortedMap<K, ? extends V> m,
   Op op
  )
  {
    super(m);
    setOperator(op); 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the default operator to apply during {@link #apply} method invocations.
   */
  public Op
  getOperator() 
  {
    return pOperator;
  }

  /**
   * Get the default operator to apply during {@link #apply} method invocations.
   */
  public void 
  setOperator
  (
   Op op   
  ) 
  {
    if(op == null) 
      throw new IllegalArgumentException("The default operator cannot be (null)!"); 
    pOperator = op;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create, destroy or modify the value associated with a particular key.<P>
   * 
   * If there is no existing value for the key, then a new entry will be created with the
   * given value.  If the value passed to this method is <code>null</code>, then the
   * entry associated with the key will be blanked and <code>null</code> will be returned.  
   * If both the existing and given value not <code>null</code>, then a numeric operator
   * will be applied to both values to determine the new value stored in the map.<P> 
   * 
   * The default numeric operator set during instantiation will be used. 
   * 
   * @param key
   *   Key with which the specified value is to be associated.
   *   
   * @param value
   *   Value to insert into the map. 
   *   
   * @return
   *   The new value for the given key. 
   */
  public V 
  apply
  (
    K key, 
    V value
  ) 
  {
    return apply(key, value, pOperator); 
  }
  
  /**
   * Create, destroy or modify the value associated with a particular key.<P>
   * 
   * If there is no existing value for the key, then a new entry will be created with the
   * given value.  If the value passed to this method is <code>null</code>, then the
   * entry associated with the key will be blanked and <code>null</code> will be returned.  
   * If both the existing and given value not <code>null</code>, then a numeric operator
   * will be applied to both values to determine the new value stored in the map.
   * 
   * @param key
   *   Key with which the specified value is to be associated.
   *   
   * @param value
   *   Value to insert into the map. 
   *   
   * @param op
   *   The numeric opertor to apply when both existing and new values are not 
   *   <code>null</code>: existing = existing (op) new-value
   *   
   * @return
   *   The new value for the given key. 
   */
  public V 
  apply
  (
    K key, 
    V value, 
    Op op
  ) 
  {
    if(value == null) {
      remove(key);
      return null;
    }
    
    V current = get(key);
    if(current == null) {
      super.put(key, value);
      return value;
    }
    else {
      V result = null;
      switch(op) {
      case Add:
        result = add(current, value); 
        break;

      case Subtract:
        result = subtract(current, value); 
        break;

      case Multiply:
        result = multiply(current, value); 
        break;

      case Divide:
        result = divide(current, value); 
        break;

      case Max:
        result = max(current, value); 
        break;

      case Min:
        result = min(current, value); 
        break;

      default:
        throw new IllegalArgumentException("The operator (" + op + ") was not supported!"); 
      }
        
      super.put(key, result);
      return result;
    }
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O P E R A T O R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the two values: (first+second)
   */
  abstract protected V
  add
  (
    V first, 
    V second
  );
   
  /**
   * Subtract the second value to the first value: (first-second)
   */
  abstract protected V
  subtract
  (
    V first, 
    V second
  );

  /**
   * Mutiply the two values: (first+second)
   */
  abstract protected V
  multiply
  (
    V first, 
    V second
  );
   
  /**
   * Divide the second value by the first value: (first+second)<P> 
   */
  abstract protected V
  divide
  (
    V first, 
    V second
  );
   
  /**
   * The minimum of the two values. 
   */
  abstract protected V
  min
  (
    V first, 
    V second
  );
   
  /**
   * The minimum of the two values. 
   */
  abstract protected V
  max
  (
    V first, 
    V second
  );
   
   

  /*----------------------------------------------------------------------------------------*/
  /*   P U B L I C   C L A S S E S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The operator to apply between the existing value and new value in {@link #apply} methods.
   */ 
  public 
  enum Op
  {
    Add, 
    Subtract, 
    Multiply, 
    Divide,    
    Min,    
    Max;      
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The default numeric operator to apply during {@link #apply} method invocations.
   */
  private Op pOperator; 
  
}
