// $Id: Tuple2f.java,v 1.4 2009/08/19 22:37:36 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component tuple containing float values.
 */
public 
class Tuple2f
  extends TupleNf
  implements Comparable<Tuple2f>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple2f() 
  {
    super(sSize);
  }

  /**
   * Construct with all components set to a constant value. 
   */ 
  public 
  Tuple2f
  (
   float v
  ) 
  {
    super(sSize, v);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Tuple2f
  (
   float x, 
   float y
  ) 
  {
    super(sSize);

    pComps[0] = x;
    pComps[1] = y;
  }

  /**
   * Construct from an array.
   * 
   * @param values
   *   The initial component values. 
   * 
   * @throws TupleSizeMismatchException
   *   If the given array is not the same size as this tuple.
   */ 
  public 
  Tuple2f
  (
   float[] values
  ) 
  {
    super(values);
    if(values.length != sSize) 
      throw new TupleSizeMismatchException(sSize, values.length);
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  Tuple2f
  (
   Tuple2f t
  ) 
  {
    this(t.pComps);
  }
		
  
    											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   A C C E S S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the first component.
   */ 
  public float
  x() 
  {
    return pComps[0]; 
  }

  /** 
   * Set the value of the first component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  x
  (
   float value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the second component.
   */ 
  public float
  y() 
  {
    return pComps[1];
  }

  /** 
   * Set the value of the second component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  y
  (
   float value   
  ) 
  {  
    pComps[1] = value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individually.   
   */ 
  public void
  set
  (
   float x, 
   float y
  ) 
  {
    pComps[0] = x;
    pComps[1] = y;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public int 
  compareTo
  (
    Tuple2f o
  )
  {
    return super.compareTo(o);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4287878958595610861L;


  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 2; 


}
