// $Id: Vector4f.java,v 1.5 2004/12/19 19:28:49 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   4 F                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component vector containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple4f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point4f class. <P> 
 */
public 
class Vector4f
  extends Tuple4f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector4f() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector4f
  (
   float x, 
   float y, 
   float z, 
   float w
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
  Vector4f
  (
   float[] values
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
  Vector4f
  (
   Tuple4f t
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
  Vector4f
  (
   Vector4f t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector4f
  (
   Point4f a, 
   Point4f b
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
  public Vector4f
  abs() 
  {
    Vector4f rtn = new Vector4f(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector4f
  min
  (
   Vector4f a, 
   Vector4f b 
  ) 
  {
    Vector4f rtn = new Vector4f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector4f
  max
  (
   Vector4f a, 
   Vector4f b 
  ) 
  {
    Vector4f rtn = new Vector4f(a);
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
  public static Vector4f
  lerp
  (
   Vector4f a, 
   Vector4f b, 
   float t
  ) 
  {
    Vector4f rtn = new Vector4f();
    lerp(a, b, t, rtn);
    return rtn;
  }
 

  
  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector4f
  normalized() 
  {
    Vector4f rtn = new Vector4f(this);
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
  public Vector4f 
  add
  (
   Vector4f t
  ) 
  {
    addTuple(t);
    return this;
  }

  /**
   * Subtract the given vector from this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4f 
  sub
  (
   Vector4f t
  ) 
  {
    subTuple(t);
    return this;
  }

  /**
   * Multiply this vector by the given vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4f 
  mult
  (
   Vector4f t
  ) 
  {
    multTuple(t);
    return this;
  }

  /**
   * Divide the given vector by this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector4f 
  div
  (
   Vector4f t
  ) 
  {
    divTuple(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector4f
  add
  (
   Vector4f a, 
   Vector4f b
  ) 
  {
    Vector4f rtn = new Vector4f(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector4f
  sub
  (
   Vector4f a, 
   Vector4f b
  ) 
  {
    Vector4f rtn = new Vector4f(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector4f
  mult
  (
   Vector4f a, 
   Vector4f b
  ) 
  {
    Vector4f rtn = new Vector4f(a);
    return rtn.mult(b);
  }

  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector4f
  div
  (
   Vector4f a, 
   Vector4f b
  ) 
  {
    Vector4f rtn = new Vector4f(a);
    return rtn.div(b);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6905631586098765133L;

}
