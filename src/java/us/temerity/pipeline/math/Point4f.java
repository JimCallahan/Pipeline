// $Id: Point4f.java,v 1.2 2004/12/13 11:57:20 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   4 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component point containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple4f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector4f class. <P> 
 */
public 
class Point4f
  extends Tuple4f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point4f() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point4f
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
  Point4f
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
  Point4f
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
  Point4f
  (
   Point4f t
  ) 
  {
    this(t.pComps);
  }
	


  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/
    		
  /**
   * Create a new vector which is the componentwise absolute value of this vector.
   */ 
  public Point4f
  abs() 
  {
    Point4f rtn = new Point4f(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point4f
  min
  (
   Point4f a, 
   Point4f b 
  ) 
  {
    Point4f rtn = new Point4f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point4f
  max
  (
   Point4f a, 
   Point4f b 
  ) 
  {
    Point4f rtn = new Point4f(a);
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
  public static Point4f
  lerp
  (
   Point4f a, 
   Point4f b, 
   float t
  ) 
  {
    Point4f rtn = new Point4f();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point4f
  normalized() 
  {
    Point4f rtn = new Point4f(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  	
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public float
  distanceSqaured
  (
   Point4f p
  ) 
  {
    Vector4f v = new Vector4f(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public float
  distance
  (
   Point4f p
  ) 
  {
    Vector4f v = new Vector4f(p, this);
    return v.length();
  }

  
    											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T W I S E   O P S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given vector to this point componentwise.
   * 
   * @return 
   *   This point to allow method chaining.
   */ 
  public Point4f 
  add
  (
   Vector4f t 
  ) 
  {
    super.add(t);
    return this;
  }

  /**
   * Subtract the given vector from this point componentwise.
   * 
   * @return 
   *   This point to allow method chaining.
   */ 
  public Point4f 
  sub
  (
   Vector4f t
  ) 
  {
    super.sub(t);
    return this;
  }

  /**
   * Multiply this point by the given vector componentwise.
   * 
   * @return 
   *   This point to allow method chaining.
   */ 
  public Point4f 
  mult
  (
   Vector4f t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point4f
  add
  (
   Point4f p, 
   Vector4f v
  ) 
  {
    Point4f rtn = new Point4f(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point4f
  sub
  (
   Point4f p, 
   Vector4f v
  ) 
  {
    Point4f rtn = new Point4f(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point4f
  mult
  (
   Point4f p, 
   Vector4f v
  ) 
  {
    Point4f rtn = new Point4f(p);
    return rtn.mult(v);
  }
}
