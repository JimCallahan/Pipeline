// $Id: BooleanOpDoubleMap.java,v 1.1 2009/12/15 21:05:29 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.BaseOpMap.*;

/*------------------------------------------------------------------------------------------*/
/*   B O O L E A N   O P   D O U B L E   M A P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A double mapping of Boolean values where calls to the apply() method apply a Boolean 
 * operator to the new and existing values in order to compute the new value in the map. <P> 
 * 
 * For example:<P>
 * <code>
 *   BooleanOpDoubleMap<String> map = new BooleanOpDoubleMap<String>();
 *   map.apply("horse", "mustang", true);           // <key="horse", "mustang", value=true>
 *   map.apply("horse", "mustang", false);          // <key="horse", "mustang", value=false>
 *   map.apply("horse", "mustang", false, BaseOpMap.Op.Mutiply);  
 *                                                  // <key="horse", "mustang", value=false>
 *   map.apply("horse", "mustang", "null);          // <key="horse", value=null>
 * </code>
 */
public 
class BooleanOpDoubleMap<A, B>
  extends BaseOpDoubleMap<A, B, Boolean>
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
  BooleanOpDoubleMap()
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
  BooleanOpDoubleMap
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
  BooleanOpDoubleMap
  (
    DoubleMap<A, B, Boolean> tmap
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
  BooleanOpDoubleMap
  (
    DoubleMap<A, B, Boolean> tmap,
    Op op
  )
  {
    super(tmap, op);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   O V E R R I D E S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  @Override
  protected BaseOpMap<B, Boolean> 
  createNewOpMap()
  {
    return new BooleanOpMap<B>();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 7821276340373893498L;
}
