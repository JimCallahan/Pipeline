// $Id: TupleNi.java,v 1.2 2004/12/14 12:24:55 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   N I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A tuple containing an arbitrary number of int values.
 */
public 
class TupleNi
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct setting all components to zero.
   * 
   * @param size
   *   The number of components.
   */ 
  public 
  TupleNi
  (
   int size
  ) 
  {
    pComps = new int[size];
  }

  /**
   * Construct setting all components to the given value.
   * 
   * @param size
   *   The number of components.
   * 
   * @param value
   *   The initial value of all components.
   */ 
  public 
  TupleNi
  (
   int size, 
   int value
  ) 
  {
    pComps = new int[size];
    
    int i;
    for(i=0; i<pComps.length; i++) 
      pComps[i] = value;
  }

  /**
   * Construct from an array.
   * 
   * @param values
   *   The initial component values. 
   */ 
  public 
  TupleNi
  (
   int[] values
  ) 
  {
    pComps = (int[]) values.clone();
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  TupleNi
  (
   TupleNi t
  ) 
  {
    pComps = t.toArray();
  }

       											    
  /*----------------------------------------------------------------------------------------*/
  /*   M I S C                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of tuple components.
   */ 
  public int 
  size() 
  {
    return pComps.length;
  }


    											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   A C C E S S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the value of the Nth component.
   * 
   * @param idx
   *   The component index.
   * 
   * @throws TupleIndexOutOfBoundsException
   *   If the component index is not valid.
   */ 
  public int
  getComp
  (
   int idx
  ) 
  {
    try {
      return pComps[idx];
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new TupleIndexOutOfBoundsException(idx); 
    }       
  }

  /** 
   * Set the value of the Nth component.
   * 
   * @param idx
   *   The component index.
   * 
   * @param v
   *   The new component value.
   * 
   * @throws TupleIndexOutOfBoundsException
   *   If the component index is not valid.
   */
  public void 
  setComp
  (
   int idx, 
   int v
  ) 
  {
    try {
      pComps[idx] = v;
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new TupleIndexOutOfBoundsException(idx); 
    }    
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the components from an array.
   * 
   * @param values
   *   The new component values. 
   * 
   * @throws TupleSizeMismatchException
   *   If the given array is not the same size as this tuple.
   */ 
  public void 
  set
  (
   int[] values
  ) 
  {
    if(pComps.length != values.length) 
      throw new TupleSizeMismatchException(pComps.length, values.length);
    pComps = (int[]) values.clone();
  }

  /**
   * Returns the mutable underlying array representation of the components.
   */
  public int[]
  getRaw()
  {
    return pComps;
  }

  /**
   * Returns an array containing a copy of the component values.
   */
  public int[]
  toArray() 
  {
    return (int[]) pComps.clone();
  }

   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set components from another tuple.
   * 
   * @param t
   *   The tuple to copy.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public void 
  set
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());
    pComps = t.toArray();
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the components of this tuple to the absolute value of their current value.
   */ 
  public void
  absolute()
  {
    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = Math.abs(pComps[i]);
  }



  /*----------------------------------------------------------------------------------------*/

  /**
   * Clamp the components in place to the given range.
   * 
   * @param lower
   *   The lower bounds.
   * 
   * @param upper
   *   The upper bounds.
   * 
   * @throws TupleSizeMismatchException
   *   If the source tuples are not the same size as this tuple.
   */ 
  public void 
  clamp
  (
   TupleNi lower, 
   TupleNi upper
  ) 
  {
    if(pComps.length != lower.size()) 
      throw new TupleSizeMismatchException(pComps.length, lower.size());

    if(pComps.length != upper.size()) 
      throw new TupleSizeMismatchException(pComps.length, upper.size());

    int i;
    for(i=0; i<pComps.length; i++) 
      pComps[i] = ExtraMath.clamp(pComps[i], lower.pComps[i], upper.pComps[i]);
  }

  /**
   * Clamp the components in place to the given range.
   * 
   * @param lower
   *   The lower bound to use for all components.
   * 
   * @param upper
   *   The upper bounds to use for all components.
   */ 
  public void 
  clamp
  (
   int lower,
   int upper
  ) 
  {
    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = ExtraMath.clamp(pComps[i], lower, upper);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace the components of this tuple with the componentwise minimum of this and the
   * given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public void 
  min
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = Math.min(pComps[i], t.pComps[i]);
  }

  /**
   * Replace the components of this tuple with the componentwise maximum of this and the
   * given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public void 
  max
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = Math.max(pComps[i], t.pComps[i]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   C O M P A R I S O N                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the smallest of the components.
   */
  public int 
  minComp() 
  {
    int m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.min(m, pComps[i]);
    return m; 
  }

  /**
   * Get the largest of the components.
   */
  public int 
  maxComp() 
  {
    int m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.min(m, pComps[i]);
    return m; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ALL components of this tuple are greater-than the corresponding components 
   * of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  allGt
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] <= t.pComps[i])
	return false;
    }
    return true;
  }
  
  /**
   * Whether ALL components of this tuple are greater-than-or-equal to the corresponding 
   * components of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  allGe
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] < t.pComps[i])
	return false;
    }
    return true;
  }

  /**
   * Whether ALL components of this tuple are less-than the corresponding components 
   * of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  allLt
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] >= t.pComps[i])
	return false;
    }
    return true;
  }
  
  /**
   * Whether ALL components of this tuple are less-than-or-equal to the corresponding 
   * components of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  allLe
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] > t.pComps[i])
	return false;
    }
    return true;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ANY components of this tuple are greater-than the corresponding components 
   * of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  anyGt
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] > t.pComps[i])
	return true;
    }
    return false;
  }

  /**
   * Whether ANY components of this tuple are greater-than-or-equal to the corresponding 
   * components of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  anyGe
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] >= t.pComps[i])
	return true;
    }
    return false;
  }

  /**
   * Whether ANY components of this tuple are less-than the corresponding components 
   * of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  anyLt
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] < t.pComps[i])
	return true;
    }
    return false;
  }
  
  /**
   * Whether ANY components of this tuple are less-than-or-equal to the corresponding 
   * components of the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public boolean
  anyLe
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] <= t.pComps[i])
	return true;
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Negate the components of this tuple in place.
   */
  public void 
  negate()
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] = -pComps[i];
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the length squared of this tuple.
   */ 
  public int 
  lengthSquared() 
  {
    return dot(this);
  }

  /**
   * Compute the length of this tuple.
   */ 
  public int 
  length() 
  {
    return (int) Math.sqrt(lengthSquared());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Normalize this tuple in place.
   * 
   * @return 
   *   The original length of the tuple.
   */ 
  public int
  normalize() 
  {
    int len = length();
    if(len > 0) {
      int i;
      for(i=0; i<pComps.length; i++)
	pComps[i] /= len;
    }
    
    return len;
  }
  
  					    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
						
  /**
   * The inner product of the this tuple and the given tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  public int 
  dot
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());
    
    int rtn = 0;
    int i; 
    for(i=0; i<pComps.length; i++) 
      rtn += pComps[i] * t.pComps[i];
    return rtn;
  }

  /**
   * The inner product of the given tuples.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuples are not the same size.
   */ 
  public static int
  dot
  (
   TupleNi a,
   TupleNi b
  ) 
  {
    return a.dot(b);
  }
  

									    
  /*----------------------------------------------------------------------------------------*/
  /*   S C A L A R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add a scalar to each component of this tuple.
   */ 
  public void 
  add
  (
   int v
  ) 
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] += v;
  }

  /**
   * Subtract a scalar from each component of this tuple.
   */ 
  public void 
  sub
  (
   int v
  ) 
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] -= v;
  }

  /**
   * Multiply each component of this tuple by a scalar.
   */ 
  public void 
  mult
  (
   int v
  ) 
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] *= v;
  }

  /**
   * Divide each component of this tuple by a scalar.
   */ 
  public void 
  div
  (
   int v
  ) 
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] /= v;
  }



										    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T W I S E   O P S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given tuple to this tuple componentwise.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  protected void 
  add
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] += t.pComps[i];
  }

  /**
   * Subtract the given tuple from this tuple componentwise.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  protected void 
  sub
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] -= t.pComps[i];
  }

  /**
   * Multiply the this tuple by the given tuple componentwise.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  protected void 
  mult
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] *= t.pComps[i];
  }

  /**
   * Divide the given tuple by this tuple componentwise.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuple is not the same size as this tuple.
   */ 
  protected void 
  div
  (
   TupleNi t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] /= t.pComps[i];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a string representation of this tuple.
   */ 
  public String
  toString() 
  {
    StringBuffer buf = new StringBuffer();

    buf.append("(");
    int i; 
    for(i=0; i<(pComps.length-1); i++) 
      buf.append(pComps[i] + ", ");
    buf.append(pComps[i] + ")");
	
    return buf.toString();
  }


		
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The tuple components.
   */ 
  protected int[]  pComps;

}
