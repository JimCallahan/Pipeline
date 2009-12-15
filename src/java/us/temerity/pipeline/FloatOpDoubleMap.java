// $Id: FloatOpDoubleMap.java,v 1.1 2009/12/15 21:05:29 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.BaseOpMap.*;

/*------------------------------------------------------------------------------------------*/
/*   F L O A T   O P   D O U B L E   M A P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A double mapping of Float values where calls to the apply() method apply a Float
 * operator to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   FloatOpDoubleMap<String> map = new FloatOpDoubleMap<String>();
 *   map.apply("horse", "mustang", 10.0);              // <key="horse", "mustang", value=10.0>
 *   map.apply("horse", "mustang", 5.0);               // <key="horse", "mustang", value=15.0>
 *   map.apply("horse", "mustang", 3.0, BaseOpMap.Op.Mutiply);  
 *                                                     // <key="horse", "mustang", value=45.0>
 *   map.apply("horse", "mustang", 5.0, BaseOpMap.Op.Subtract);  
 *                                                     // <key="horse", "mustang", value=40.0>
 *   map.apply("horse", "mustang", "null);             // <key="horse", value=null>
 * </code>
 */
public 
class FloatOpDoubleMap<A, B>
  extends BaseOpDoubleMap<A, B, Float>
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
  FloatOpDoubleMap()
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
  FloatOpDoubleMap
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
  FloatOpDoubleMap
  (
    DoubleMap<A, B, Float> tmap
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
  FloatOpDoubleMap
  (
    DoubleMap<A, B, Float> tmap,
    Op op
  )
  {
    super(tmap, op);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected BaseOpMap<B, Float> 
  createNewOpMap()
  {
    return new FloatOpMap<B>();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1643452010818958802L;
}
