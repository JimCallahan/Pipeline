// $Id: Tuple3d.java,v 1.3 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   3 D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component tuple containing double values.
 */
public 
class Tuple3d
  extends TupleNd
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple3d() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Tuple3d
  (
   double x, 
   double y, 
   double z
  ) 
  {
    super(sSize);

    pComps[0] = x;
    pComps[1] = y;
    pComps[2] = z;
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
  Tuple3d
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
  Tuple3d
  (
   Tuple3d t
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


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individually.   
   */ 
  public void
  set
  (
   double x, 
   double y,
   double z
  ) 
  {
    pComps[0] = x;
    pComps[1] = y;
    pComps[2] = z;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -143283502929447629L;


  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 3; 


}
