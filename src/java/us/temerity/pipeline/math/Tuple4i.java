// $Id: Tuple4i.java,v 1.3 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   4 I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component tuple containing int values.
 */
public 
class Tuple4i
  extends TupleNi
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple4i() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Tuple4i
  (
   int x, 
   int y, 
   int z, 
   int w
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
  Tuple4i
  (
   int[] values
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
  Tuple4i
  (
   Tuple4i t
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
  public int
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
   int value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the second component.
   */ 
  public int
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
   int value   
  ) 
  {  
    pComps[1] = value;
  }


  /** 
   * Get the value of the third component.
   */ 
  public int
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
   int value   
  ) 
  {  
    pComps[2] = value;
  }


  /** 
   * Get the value of the fourth component.
   */ 
  public int
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
   int value   
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
   int x, 
   int y,
   int z,
   int w 
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

  private static final long serialVersionUID = 7216628186865091035L;


  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 4; 


}
