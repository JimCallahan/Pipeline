// $Id: Vector4i.java,v 1.2 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   4 I                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component vector containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple4i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point4i class. <P> 
 */
public 
class Vector4i
  extends Tuple4i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector4i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector4i
  (
   int x, 
   int y, 
   int z, 
   int w
  ) 
  {
    super(x, y, z, w);
  }

  /**
   * Construct from an array.
   * 
   * @param values
   *   The initial component values. 
   * 
   * @throws TupleSizeMismatchException
   *   If the given array is not the same size as this vector.
   */ 
  public 
  Vector4i
  (
   int[] values
  ) 
  {
    super(values);
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  Vector4i
  (
   Tuple4i t
  ) 
  {
    this(t.pComps);
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The vector to copy.
   */ 
  public 
  Vector4i
  (
   Vector4i t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector4i
  (
   Point4i a, 
   Point4i b
  ) 
  {
    super();

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] = b.pComps[i] - a.pComps[i];
  }	


   
  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/
    		
  /**
   * Create a new vector which is the componentwise absolute value of this vector.
   */ 
  public Vector4i
  abs() 
  {
    Vector4i rtn = new Vector4i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector4i
  min
  (
   Vector4i a, 
   Vector4i b 
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector4i
  max
  (
   Vector4i a, 
   Vector4i b 
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    rtn.max(b);
    return rtn;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector4i
  normalized() 
  {
    Vector4i rtn = new Vector4i(this);
    rtn.normalize();
    return rtn;
  }


									    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T W I S E   O P S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given vector to this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4i 
  add
  (
   Vector4i t
  ) 
  {
    super.add(t);
    return this;
  }

  /**
   * Subtract the given vector from this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4i 
  sub
  (
   Vector4i t
  ) 
  {
    super.sub(t);
    return this;
  }

  /**
   * Multiply this vector by the given vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4i 
  mult
  (
   Vector4i t
  ) 
  {
    super.mult(t);
    return this;
  }

  /**
   * Divide the given vector by this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4i 
  div
  (
   Vector4i t
  ) 
  {
    super.div(t);
    return this;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector4i
  add
  (
   Vector4i a, 
   Vector4i b
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector4i
  sub
  (
   Vector4i a, 
   Vector4i b
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector4i
  mult
  (
   Vector4i a, 
   Vector4i b
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    return rtn.mult(b);
  }

  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector4i
  div
  (
   Vector4i a, 
   Vector4i b
  ) 
  {
    Vector4i rtn = new Vector4i(a);
    return rtn.div(b);
  }
  
}
