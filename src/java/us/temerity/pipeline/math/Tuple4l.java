// $Id: Tuple4l.java,v 1.2 2004/12/14 12:26:12 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 L                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component tuple containing long values.
 */
public 
class Tuple4l
  extends TupleNl
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple4l() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Tuple4l
  (
   long x, 
   long y, 
   long z, 
   long w
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
  Tuple4l
  (
   long[] values
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
  Tuple4l
  (
   Tuple4l t
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
  public long
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
   long value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the second component.
   */ 
  public long
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
   long value   
  ) 
  {  
    pComps[1] = value;
  }


  /** 
   * Get the value of the third component.
   */ 
  public long
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
   long value   
  ) 
  {  
    pComps[2] = value;
  }


  /** 
   * Get the value of the fourth component.
   */ 
  public long
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
   long value   
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
   long x, 
   long y,
   long z,
   long w 
  ) 
  {
    pComps[0] = x;
    pComps[1] = y;
    pComps[2] = z;
    pComps[3] = w;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 4; 


}
