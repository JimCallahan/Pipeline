// $Id: Point2f.java,v 1.4 2004/12/16 21:34:49 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   2 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component point containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple2f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector2f class. <P> 
 */
public 
class Point2f
  extends Tuple2f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point2f() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point2f
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
  Point2f
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
  Point2f
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
  Point2f
  (
   Point2f t
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
  public Point2f
  abs() 
  {
    Point2f rtn = new Point2f(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point2f
  min
  (
   Point2f a, 
   Point2f b 
  ) 
  {
    Point2f rtn = new Point2f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point2f
  max
  (
   Point2f a, 
   Point2f b 
  ) 
  {
    Point2f rtn = new Point2f(a);
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
  public static Point2f
  lerp
  (
   Point2f a, 
   Point2f b, 
   float t
  ) 
  {
    Point2f rtn = new Point2f();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point2f
  normalized() 
  {
    Point2f rtn = new Point2f(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public float
  distanceSquared
  (
   Point2f p
  ) 
  {
    Vector2f v = new Vector2f(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public float
  distance
  (
   Point2f p
  ) 
  {
    Vector2f v = new Vector2f(p, this);
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
  public Point2f 
  add
  (
   Vector2f t 
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
  public Point2f 
  sub
  (
   Vector2f t
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
  public Point2f 
  mult
  (
   Vector2f t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point2f
  add
  (
   Point2f p, 
   Vector2f v
  ) 
  {
    Point2f rtn = new Point2f(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point2f
  sub
  (
   Point2f p, 
   Vector2f v
  ) 
  {
    Point2f rtn = new Point2f(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point2f
  mult
  (
   Point2f p, 
   Vector2f v
  ) 
  {
    Point2f rtn = new Point2f(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2418939175965276598L;

}
