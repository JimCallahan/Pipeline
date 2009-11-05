// $Id: AdditiveDoubleTreeMap.java,v 1.1 2009/11/05 18:06:39 jesse Exp $

package us.temerity.pipeline;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   A D D I T I V E   D O U B L E   T R E E   M A P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An implementation of an {@link AdditiveTreeMap} for use with {@link Double Doubles}.
 * 
 * @param <K>
 *   The type of the key in this map. 
 */
public 
class AdditiveDoubleTreeMap<K>
  extends AdditiveTreeMap<K, Double>
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  AdditiveDoubleTreeMap()
  {
    super();
  }

  public 
  AdditiveDoubleTreeMap
  (
    Comparator<? super K> comparator
  )
  {
    super(comparator);
  }

  public 
  AdditiveDoubleTreeMap
  (
    Map<? extends K, ? extends Double> m
  )
  {
    super(m);
  }

  public 
  AdditiveDoubleTreeMap
  (
    SortedMap<K, ? extends Double> m
  )
  {
    super(m);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I M P L E M E N T A T I O N                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  protected Double 
  add
  (
    Double one,
    Double two
  )
  {
    return one + two;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -9210043804364931877L;
}
