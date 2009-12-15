// $Id: LongOpDoubleMap.java,v 1.1 2009/12/15 21:05:29 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.BaseOpMap.*;

/*------------------------------------------------------------------------------------------*/
/*   L O N G   O P   D O U B L E   M A P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A double mapping of Long values where calls to the apply() method apply a Long
 * operator to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   LongOpDoubleMap<String> map = new LongOpDoubleMap<String>();
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
class LongOpDoubleMap<A, B>
  extends BaseOpDoubleMap<A, B, Long>
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
  LongOpDoubleMap()
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
  LongOpDoubleMap
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
  LongOpDoubleMap
  (
    DoubleMap<A, B, Long> tmap
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
  LongOpDoubleMap
  (
    DoubleMap<A, B, Long> tmap,
    Op op
  )
  {
    super(tmap, op);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected BaseOpMap<B, Long> 
  createNewOpMap()
  {
    return new LongOpMap<B>();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6354728565554024802L;
}
