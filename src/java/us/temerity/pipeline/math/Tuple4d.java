// $Id: Tuple4d.java,v 1.4 2004/12/16 21:35:41 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component tuple containing double values.
 */
public 
class Tuple4d
  extends TupleNd
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple4d() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Tuple4d
  (
   double x, 
   double y, 
   double z, 
   double w
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
  Tuple4d
  (
   double[] values
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
  Tuple4d
  (
   Tuple4d t
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
  public double
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
   double value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the second component.
   */ 
  public double
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
   double value   
  ) 
  {  
    pComps[1] = value;
  }


  /** 
   * Get the value of the third component.
   */ 
  public double
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
   double value   
  ) 
  {  
    pComps[2] = value;
  }


  /** 
   * Get the value of the fourth component.
   */ 
  public double
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
   double value   
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
   double x, 
   double y,
   double z,
   double w 
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

  private static final long serialVersionUID = -873205338876336747L;


  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 4; 


}
