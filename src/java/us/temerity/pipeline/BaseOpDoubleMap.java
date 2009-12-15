// $Id: BaseOpDoubleMap.java,v 1.1 2009/12/15 21:05:29 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

import us.temerity.pipeline.BaseOpMap.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   O P   D O U B L E   M A P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A mapping of numeric values where calls to the apply() method apply a numeric operator
 * to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * This class has abstract methods for the common numeric operators which subclasses 
 * specialized for one of the numeric types will overload.
 * 
 * @param <A>
 *   The type of the first key.
 *    
 * @param <B>
 *   The type of the second key.
 *    
 * @param <V>
 *   The type of the map value. 
 */
public abstract
class BaseOpDoubleMap<A, B, V>
  extends DoubleMap<A, B, V>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Constructs a new, empty double map, using the natural ordering of its keys. 
   * 
   * The default numeric operator is Add.
   * 
   * @see DoubleMap#DoubleMap()
   */
  public 
  BaseOpDoubleMap()
  {
    this(Op.Add); 
  }
  
  /**
   * Constructs a new, empty double map, using the natural ordering of its keys. 
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see DoubleMap#DoubleMap()
   */
  public 
  BaseOpDoubleMap
  (
    Op op
  ) 
  {
    super();
    setOperator(op); 
  }
  
  /**
   * Constructs a new double map containing the same mappings as the given map, ordered 
   * according to the natural ordering of its keys
   * 
   * The default numeric operator is Add.
   * 
   * @see DoubleMap#DoubleMap(DoubleMap)
   */
  public 
  BaseOpDoubleMap
  (
    DoubleMap<A, B, V> tmap
  )
  {
    this(tmap, Op.Add); 
  }

  /**
   * Constructs a new double map containing the same mappings as the given map, ordered 
   * according to the natural ordering of its keys
   * 
   * @param op
   *   Sets the default numeric operator to apply during {@link #apply} method invocations.
   * 
   * @see DoubleMap#DoubleMap(DoubleMap)
   */
  public 
  BaseOpDoubleMap
  (
    DoubleMap<A, B, V> tmap,
    Op op
  )
  {
    super(tmap);
    setOperator(op); 
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   *  Override in child classes to return {@link BaseOpMap BaseOpMaps} of the appropriate
   *  type for the value of this double map. 
   */
  protected abstract BaseOpMap<B, V>
  createNewOpMap();

  
  
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
  
  /**
   * Create, destroy or modify the value associated with a particular set of keys.<P>
   * 
   * If there is no existing value for the key, then a new entry will be created with the
   * given value.  If the value passed to this method is <code>null</code>, then the
   * entry associated with the key will be blanked and <code>null</code> will be returned.  
   * If both the existing and given value not <code>null</code>, then a numeric operator
   * will be applied to both values to determine the new value stored in the map.<P> 
   * 
   * The default numeric operator set during instantiation will be used.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *  The second key.
   * 
   * @param value
   *   The value to insert.
   *   
   * @return 
   *   The new value for the given key.
   */ 
  public V
  apply
  (
   A keyA,
   B keyB,
   V value
  ) 
  {
    return apply(keyA, keyB, value, pOperator);
  }
  
  /**
   * Create, destroy or modify the value associated with a particular set of keys.<P>
   * 
   * If there is no existing value for the key, then a new entry will be created with the
   * given value.  If the value passed to this method is <code>null</code>, then the
   * entry associated with the key will be blanked and <code>null</code> will be returned.  
   * If both the existing and given value not <code>null</code>, then a numeric operator
   * will be applied to both values to determine the new value stored in the map.<P> 
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *  The second key.
   * 
   * @param value
   *   The value to insert.
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
   A keyA,
   B keyB,
   V value,
   Op op
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");
    
    if(value == null) {
      remove(keyA, keyB);
      return null;
    }
    
    V current = get(keyA, keyB);
    if(current == null) {
      put(keyA, keyB, value);
      return value;
    }
    
    BaseOpMap<B,V> tableB = (BaseOpMap<B, V>) super.get(keyA);
    if(tableB == null) {
      tableB = createNewOpMap();
      put(keyA, tableB);
    }

    return tableB.apply(keyB, value, op);
  }
  
  /**
   * Associates a value with the specified set of keys in this map.
   * 
   * @param keyA
   *   The first key.
   * 
   * @param keyB
   *  The second key.
   * 
   * @param value
   *   The value to insert.
   * 
   * @return 
   *   The previous value associated with the key or <code>null</code> if there was no previous
   *   value for the key.
   */ 
  @Override
  public V
  put
  (
   A keyA,
   B keyB,
   V value
  ) 
  {
    if(keyA == null) 
      throw new IllegalArgumentException("The first key cannot be (null)!");

    if(keyB == null) 
      throw new IllegalArgumentException("The second key cannot be (null)!");
    
    V toReturn = null;

    BaseOpMap<B,V> tableB = (BaseOpMap<B, V>) super.get(keyA);
    if(tableB == null) {
      tableB = createNewOpMap();
      put(keyA, tableB);
    }
    else
      toReturn = tableB.get(keyB);

    tableB.put(keyB, value);
    
    return toReturn;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -2061850469400319681L;


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The default numeric operator to apply during {@link #apply} method invocations.
   */
  private Op pOperator; 
}
