// $Id: Point2d.java,v 1.2 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   2 D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component point containing double values. <P> 
 * 
 * This class is a more restricted form of Tuple2d which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector2d class. <P> 
 */
public 
class Point2d
  extends Tuple2d
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point2d() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point2d
  (
   double x, 
   double y
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
  Point2d
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
  Point2d
  (
   Tuple2d t
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
  Point2d
  (
   Point2d t
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
  public Point2d
  abs() 
  {
    Point2d rtn = new Point2d(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point2d
  min
  (
   Point2d a, 
   Point2d b 
  ) 
  {
    Point2d rtn = new Point2d(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point2d
  max
  (
   Point2d a, 
   Point2d b 
  ) 
  {
    Point2d rtn = new Point2d(a);
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
  public static Point2d
  lerp
  (
   Point2d a, 
   Point2d b, 
   double t
  ) 
  {
    Point2d rtn = new Point2d();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point2d
  normalized() 
  {
    Point2d rtn = new Point2d(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public double
  distanceSqaured
  (
   Point2d p
  ) 
  {
    Vector2d v = new Vector2d(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public double
  distance
  (
   Point2d p
  ) 
  {
    Vector2d v = new Vector2d(p, this);
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
  public Point2d 
  add
  (
   Vector2d t 
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
  public Point2d 
  sub
  (
   Vector2d t
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
  public Point2d 
  mult
  (
   Vector2d t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point2d
  add
  (
   Point2d p, 
   Vector2d v
  ) 
  {
    Point2d rtn = new Point2d(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point2d
  sub
  (
   Point2d p, 
   Vector2d v
  ) 
  {
    Point2d rtn = new Point2d(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point2d
  mult
  (
   Point2d p, 
   Vector2d v
  ) 
  {
    Point2d rtn = new Point2d(p);
    return rtn.mult(v);
  }
}
