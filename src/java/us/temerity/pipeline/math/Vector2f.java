// $Id: Vector2f.java,v 1.3 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   2 F                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component vector containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple2f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point2f class. <P> 
 */
public 
class Vector2f
  extends Tuple2f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector2f() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector2f
  (
   float x, 
   float y
  ) 
  {
    super(x, y);
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
  Vector2f
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
  Vector2f
  (
   Tuple2f t
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
  Vector2f
  (
   Vector2f t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector2f
  (
   Point2f a, 
   Point2f b
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
  public Vector2f
  abs() 
  {
    Vector2f rtn = new Vector2f(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector2f
  min
  (
   Vector2f a, 
   Vector2f b 
  ) 
  {
    Vector2f rtn = new Vector2f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector2f
  max
  (
   Vector2f a, 
   Vector2f b 
  ) 
  {
    Vector2f rtn = new Vector2f(a);
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
  public static Vector2f
  lerp
  (
   Vector2f a, 
   Vector2f b, 
   float t
  ) 
  {
    Vector2f rtn = new Vector2f();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector2f
  normalized() 
  {
    Vector2f rtn = new Vector2f(this);
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
  public Vector2f 
  add
  (
   Vector2f t
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
  public Vector2f 
  sub
  (
   Vector2f t
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
  public Vector2f 
  mult
  (
   Vector2f t
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
  public Vector2f 
  div
  (
   Vector2f t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector2f
  add
  (
   Vector2f a, 
   Vector2f b
  ) 
  {
    Vector2f rtn = new Vector2f(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector2f
  sub
  (
   Vector2f a, 
   Vector2f b
  ) 
  {
    Vector2f rtn = new Vector2f(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector2f
  mult
  (
   Vector2f a, 
   Vector2f b
  ) 
  {
    Vector2f rtn = new Vector2f(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector2f
  div
  (
   Vector2f a, 
   Vector2f b
  ) 
  {
    Vector2f rtn = new Vector2f(a);
    return rtn.div(b);
  }
}
