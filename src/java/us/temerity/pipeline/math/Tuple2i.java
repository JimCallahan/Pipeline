// $Id: Tuple2i.java,v 1.2 2004/12/14 12:26:12 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   2 I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component tuple containing int values.
 */
public 
class Tuple2i
  extends TupleNi
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Tuple2i() 
  {
    super(sSize);
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Tuple2i
  (
   int x, 
   int y
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
  Tuple2i
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
  Tuple2i
  (
   Tuple2i t
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


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individually.   
   */ 
  public void
  set
  (
   int x, 
   int y
  ) 
  {
    pComps[0] = x;
    pComps[1] = y;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of tuple components.
   */ 
  private static final int  sSize = 2; 


}
