// $Id: IntegerOpDoubleMap.java,v 1.1 2009/12/15 21:05:29 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.BaseOpMap.*;

/*------------------------------------------------------------------------------------------*/
/*   I N T E G E R   O P   D O U B L E   M A P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A double mapping of Integer values where calls to the apply() method apply an Integer 
 * operator to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   IntegerOpDoubleMap<String> map = new IntegerOpDoubleMap<String>();
 *   map.apply("horse", "mustang", 10);                  // <key="horse", "mustang", value=10>
 *   map.apply("horse", "mustang", 5);                   // <key="horse", "mustang", value=15>
 *   map.apply("horse", "mustang", 3, BaseOpMap.Op.Mutiply);  
 *                                                       // <key="horse", "mustang", value=45>
 *   map.apply("horse", "mustang", 5, BaseOpMap.Op.Subtract);  
 *                                                      // <key="horse", "mustang", value=40>
 *   map.apply("horse", "mustang", "null);              // <key="horse", value=null>
 * </code>
 */
public 
class IntegerOpDoubleMap<A, B>
  extends BaseOpDoubleMap<A, B, Integer>
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
  IntegerOpDoubleMap()
  {
    super();
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
  IntegerOpDoubleMap
  (
    Op op
  )
  {
    super(op);
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
  IntegerOpDoubleMap
  (
    DoubleMap<A, B, Integer> tmap
   )
  {
    super(tmap);
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
  IntegerOpDoubleMap
  (
    DoubleMap<A, B, Integer> tmap,
    Op op
  )
  {
    super(tmap, op);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected BaseOpMap<B, Integer> 
  createNewOpMap()
  {
    return new IntegerOpMap<B>();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7821276340373893498L;
}
