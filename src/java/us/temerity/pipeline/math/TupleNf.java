// $Id: TupleNf.java,v 1.5 2004/12/17 20:07:37 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   N F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A tuple containing an arbitrary number of float values.
 */
public 
class TupleNf
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
  TupleNf
  (
   int size
  ) 
  {
    pComps = new float[size];
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
  TupleNf
  (
   int size, 
   float value
  ) 
  {
    pComps = new float[size];
    
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
  TupleNf
  (
   float[] values
  ) 
  {
    pComps = (float[]) values.clone();
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  TupleNf
  (
   TupleNf t
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
  public float
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
   float v
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
   float[] values
  ) 
  {
    if(pComps.length != values.length) 
      throw new TupleSizeMismatchException(pComps.length, values.length);
    pComps = (float[]) values.clone();
  }

  /**
   * Returns the mutable underlying array representation of the components.
   */
  public float[]
  getRaw()
  {
    return pComps;
  }

  /**
   * Returns an array containing a copy of the component values.
   */
  public float[]
  toArray() 
  {
    return (float[]) pComps.clone();
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
   TupleNf t
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
   TupleNf lower, 
   TupleNf upper
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
   float lower,
   float upper
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
   TupleNf t
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
   TupleNf t
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
   * Set the components of a tuple to a linear interpolation of two tuples.
   * 
   * @param a
   *   The first tuple.
   *
   * @param b
   *   The second tuple.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   * 
   * @param rtn
   *   The interpolated tuple.
   * 
   * @throws TupleSizeMismatchException
   *   If the given tuples are not the same size.
   */ 
  protected static TupleNf
  lerp
  (
   TupleNf a, 
   TupleNf b, 
   float t, 
   TupleNf rtn
  ) 
  {
    if(a.size() != b.size()) 
      throw new TupleSizeMismatchException(a.size(), b.size());
    
    if(a.size() != rtn.size()) 
      throw new TupleSizeMismatchException(a.size(), rtn.size());
    
    int i;
    for(i=0; i<a.size(); i++) 
      rtn.pComps[i] = ExtraMath.lerp(a.pComps[i], b.pComps[i], t);
    
    return rtn;
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
   TupleNf t, 
   float epsilon
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
   TupleNf t
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
  public float 
  minComp() 
  {
    float m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.min(m, pComps[i]);
    return m; 
  }

  /**
   * Get the largest of the components.
   */
  public float 
  maxComp() 
  {
    float m = pComps[0]; 
    int i;
    for(i=1; i<pComps.length; i++) 
      m = Math.max(m, pComps[i]);
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
  public float 
  lengthSquared() 
  {
    return dot(this);
  }

  /**
   * Compute the length of this tuple.
   */ 
  public float 
  length() 
  {
    return (float) Math.sqrt(lengthSquared());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Normalize this tuple in place.
   * 
   * @return 
   *   The original length of the tuple.
   */ 
  public float
  normalize() 
  {
    float len = length();
    if(len > 0.0f) {
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
  public float 
  dot
  (
   TupleNf t
  ) 
  {
    if(pComps.length != t.size()) 
      throw new TupleSizeMismatchException(pComps.length, t.size());
    
    float rtn = 0.0f;
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
  public static float
  dot
  (
   TupleNf a,
   TupleNf b
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
   float v
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
   float v
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
   float v
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
   float v
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
   TupleNf t
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
    if((obj != null) && (obj instanceof TupleNf)) {
      TupleNf t = (TupleNf) obj;
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
    StringBuffer buf = new StringBuffer();

    buf.append("(");
    int i; 
    for(i=0; i<(pComps.length-1); i++) 
      buf.append(String.format("%1$.4f", pComps[i]) + ", ");
    buf.append(String.format("%1$.4f", pComps[i]) + ")");
	
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
    float[] comps = (float[]) decoder.decode("Comps"); 
    if(comps == null) 
      throw new GlueException("The \"Comps\" entry was missing!");
    pComps = comps;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -681761072062846262L;


		
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The tuple components.
   */ 
  protected float[]  pComps;

}
