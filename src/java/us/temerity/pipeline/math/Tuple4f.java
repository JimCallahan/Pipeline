// $Id: Tuple4f.java,v 1.4 2009/08/19 22:37:36 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component tuple containing float values.
 */
public 
class Tuple4f
  extends TupleNf
  implements Comparable<Tuple4f>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple4f() 
  {
    super(sSize);
  }

  /**
   * Construct with all components set to a constant value. 
   */ 
  public 
  Tuple4f
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
  Tuple4f
  (
   float x, 
   float y, 
   float z, 
   float w
  ) 
  {
    super(sSize);

    pComps[0] = x;
    pComps[1] = y;
    pComps[2] = z;
    pComps[3] = w;
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
  Tuple4f
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
  Tuple4f
  (
   Tuple4f t
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


  /** 
   * Get the value of the third component.
   */ 
  public float
  z() 
  {
    return pComps[2];
  }

  /** 
   * Set the value of the third component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  z
  (
   float value   
  ) 
  {  
    pComps[2] = value;
  }


  /** 
   * Get the value of the fourth component.
   */ 
  public float
  w() 
  {
    return pComps[3];
  }

  /** 
   * Set the value of the fourth component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  w
  (
   float value   
  ) 
  {  
    pComps[3] = value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individually.   
   */ 
  public void
  set
  (
   float x, 
   float y,
   float z,
   float w 
  ) 
  {
    pComps[0] = x;
    pComps[1] = y;
    pComps[2] = z;
    pComps[3] = w;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public int 
  compareTo
  (
    Tuple4f o
  )
  {
    return super.compareTo(o);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -289659481288029971L;


  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 4; 


}
