// DO NOT EDIT! -- Automatically generated by: permutations.bash

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   N L                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A tuple containing an arbitrary number of long values.
 */
public 
class TupleNl
  implements Glueable, Serializable
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
  TupleNl
  (
   int size
  ) 
  {
    pComps = new long[size];
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
  TupleNl
  (
   int size, 
   long value
  ) 
  {
    pComps = new long[size];
    
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
  TupleNl
  (
   long[] values
  ) 
  {
    pComps = values.clone();
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  TupleNl
  (
   TupleNl t
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
  public long
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
   long v
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
   long[] values
  ) 
  {
    if(pComps.length != values.length) 
      throw new TupleSizeMismatchException(pComps.length, values.length);
    pComps = values.clone();
  }

  /**
   * Returns an array containing a copy of the component values.
   */
  public long[]
  toArray() 
  {
    return pComps.clone();
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
   TupleNl t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());
    pComps = t.toArray();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Set all components to a scalar value. 
   * 
   * @param s
   *   The value to set all components. 
   */ 
  public void 
  set
  (
   long s 
  ) 
  {
    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = s;
  }

  /**
   * Set all components to zero.
   */ 
  public void
  zero()
  {
    set(0L);
  }

  /**
   * Set all components to one.
   */ 
  public void
  one()
  {
    set(1L);
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
   TupleNl lower, 
   TupleNl upper
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
   long lower,
   long upper
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
   TupleNl t
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
   TupleNl t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<size(); i++) 
      pComps[i] = Math.max(pComps[i], t.pComps[i]);
  }

      
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether all components of the given tuple are within the given epsilon of the 
   * corresponding components of this tuple.
   * 
   * @param t
   *   The tuple to compare
   * 
   * @param epsilon
   *   The maximum difference in values.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuples are not the same size.
   */
  public boolean 
  equiv
  (
   TupleNl t, 
   long epsilon
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<size(); i++) {
      if(!ExtraMath.equiv(pComps[i], t.pComps[i], epsilon)) 
	return false;
    }

    return true;
  }
  
  /**
   * Whether all components of the given tuple are within the standard epsilon of the
   * corresponding components of this tuple. <P> 
   * 
   * @param t
   *   The tuple to compare
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuples are not the same size.
   */
  public boolean 
  equiv
  (
   TupleNl t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i;
    for(i=0; i<size(); i++) {
      if(!ExtraMath.equiv(pComps[i], t.pComps[i])) 
	return false;
    }

    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   C O M P A R I S O N                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the smallest of the components.
   */
  public long 
  minComp() 
  {
    long m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.min(m, pComps[i]);
    return m; 
  }

  /**
   * Get the largest of the components.
   */
  public long 
  maxComp() 
  {
    long m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.max(m, pComps[i]);
    return m; 
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ALL components of this tuple are greater-than the given scalar.
   */ 
  public boolean
  allGt
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] <= s)
	return false;
    }
    return true;
  }
  
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
   TupleNl t
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
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ALL components of this tuple are greater-than-or-equal the given scalar.
   */ 
  public boolean
  allGe
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] < s)
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
   TupleNl t
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ALL components of this tuple are less-than the given scalar.
   */ 
  public boolean
  allLt
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] >= s)
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
   TupleNl t
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ALL components of this tuple are less-than-or-equal the given scalar.
   */ 
  public boolean
  allLe
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] > s)
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
   TupleNl t
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
   * Whether ANY components of this tuple are greater-than the given scalar.
   */ 
  public boolean
  anyGt
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] > s)
	return true;
    }
    return false;
  }

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
   TupleNl t
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ANY components of this tuple are greater-than-or-equal the given scalar.
   */ 
  public boolean
  anyGe
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] >= s)
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
   TupleNl t
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether ANY components of this tuple are less-than the given scalar.
   */ 
  public boolean
  anyLt
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] < s)
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
   TupleNl t
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


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether ANY components of this tuple are less-than-or-equal the given scalar.
   */ 
  public boolean
  anyLe
  (
   long s
  ) 
  {
    int i;
    for(i=0; i<pComps.length; i++) {
      if(pComps[i] <= s)
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
   TupleNl t
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
  public long 
  lengthSquared() 
  {
    return dot(this);
  }

  /**
   * Compute the length of this tuple.
   */ 
  public long 
  length() 
  {
    return (long) Math.sqrt(lengthSquared());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Normalize this tuple in place.
   * 
   * @return 
   *   The original length of the tuple.
   */ 
  public long
  normalize() 
  {
    long len = length();
    if(len > 0L) {
      int i;
      for(i=0; i<pComps.length; i++)
	pComps[i] /= len;
    }
    
    return len;
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
   long v
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
   long v
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
   long v
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
   long v
  ) 
  {
    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] /= v;
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
  public long 
  dot
  (
   TupleNl t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());
    
    long rtn = 0L;
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
  public static long
  dot
  (
   TupleNl a,
   TupleNl b
  ) 
  {
    return a.dot(b);
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
  public void 
  addTuple
  (
   TupleNl t
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
  public void 
  subTuple
  (
   TupleNl t
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
  public void 
  multTuple
  (
   TupleNl t
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
  public void 
  divTuple
  (
   TupleNl t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] /= t.pComps[i];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this <CODE>TupleNl</CODE> with the given <CODE>TupleNl</CODE> for order.
   * 
   * @param t
   *   The <CODE>TupleNl</CODE> to be compared.
   */
  protected int
  compareTo
  (
   TupleNl t
  )
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());

    if (this.anyLt(t))
      return -1;
    if (this.anyGt(t))
      return 1;
    return 0;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof TupleNl)) {
      TupleNl t = (TupleNl) obj;
      return Arrays.equals(pComps, t.pComps);
    }
    return false;
  }

  /**
   * Generate a string representation of this tuple.
   */ 
  public String
  toString() 
  {
    StringBuilder buf = new StringBuilder();

    buf.append("[");
    int i; 
    for(i=0; i<pComps.length; i++) {
      buf.append(pComps[i]);
      if(i<(pComps.length-1))
	buf.append(" ");
    }
    buf.append("]");
	
    return buf.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Comps", pComps);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    long[] comps = (long[]) decoder.decode("Comps"); 
    if(comps == null) 
      throw new GlueException("The \"Comps\" entry was missing!");
    pComps = comps;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5567729237636881756L;


		
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The tuple components.
   */ 
  protected long[]  pComps;

}
