// $Id: Vector4d.java,v 1.1 2004/12/13 12:56:48 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   4 D                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component vector containing double values. <P> 
 * 
 * This class is a more restricted form of Tuple4d which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point4d class. <P> 
 */
public 
class Vector4d
  extends Tuple4d
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector4d() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector4d
  (
   double x, 
   double y, 
   double z, 
   double w
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
  Vector4d
  (
   double[] values
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
  Vector4d
  (
   Tuple4d t
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
  Vector4d
  (
   Vector4d t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector4d
  (
   Point4d a, 
   Point4d b
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
  public Vector4d
  abs() 
  {
    Vector4d rtn = new Vector4d(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector4d
  min
  (
   Vector4d a, 
   Vector4d b 
  ) 
  {
    Vector4d rtn = new Vector4d(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector4d
  max
  (
   Vector4d a, 
   Vector4d b 
  ) 
  {
    Vector4d rtn = new Vector4d(a);
    rtn.max(b);
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the linear interpolation of the given vectors.
   * 
   * @param a
   *   The first vector.
   *
   * @param b
   *   The second vector.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   */ 
  public static Vector4d
  lerp
  (
   Vector4d a, 
   Vector4d b, 
   double t
  ) 
  {
    Vector4d rtn = new Vector4d();
    lerp(a, b, t, rtn);
    return rtn;
  }
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector4d
  normalized() 
  {
    Vector4d rtn = new Vector4d(this);
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
  public Vector4d 
  add
  (
   Vector4d t
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
  public Vector4d 
  sub
  (
   Vector4d t
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
  public Vector4d 
  mult
  (
   Vector4d t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector4d
  add
  (
   Vector4d a, 
   Vector4d b
  ) 
  {
    Vector4d rtn = new Vector4d(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector4d
  sub
  (
   Vector4d a, 
   Vector4d b
  ) 
  {
    Vector4d rtn = new Vector4d(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector4d
  mult
  (
   Vector4d a, 
   Vector4d b
  ) 
  {
    Vector4d rtn = new Vector4d(a);
    return rtn.mult(b);
  }
  
}
