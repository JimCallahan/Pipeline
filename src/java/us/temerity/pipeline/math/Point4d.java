// $Id: Point4d.java,v 1.3 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   4 D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component point containing double values. <P> 
 * 
 * This class is a more restricted form of Tuple4d which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector4d class. <P> 
 */
public 
class Point4d
  extends Tuple4d
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point4d() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point4d
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
  Point4d
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
  Point4d
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
  Point4d
  (
   Point4d t
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
  public Point4d
  abs() 
  {
    Point4d rtn = new Point4d(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point4d
  min
  (
   Point4d a, 
   Point4d b 
  ) 
  {
    Point4d rtn = new Point4d(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point4d
  max
  (
   Point4d a, 
   Point4d b 
  ) 
  {
    Point4d rtn = new Point4d(a);
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
  public static Point4d
  lerp
  (
   Point4d a, 
   Point4d b, 
   double t
  ) 
  {
    Point4d rtn = new Point4d();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point4d
  normalized() 
  {
    Point4d rtn = new Point4d(this);
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
   Point4d p
  ) 
  {
    Vector4d v = new Vector4d(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public double
  distance
  (
   Point4d p
  ) 
  {
    Vector4d v = new Vector4d(p, this);
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
  public Point4d 
  add
  (
   Vector4d t 
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
  public Point4d 
  sub
  (
   Vector4d t
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
  public Point4d 
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
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point4d
  add
  (
   Point4d p, 
   Vector4d v
  ) 
  {
    Point4d rtn = new Point4d(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point4d
  sub
  (
   Point4d p, 
   Vector4d v
  ) 
  {
    Point4d rtn = new Point4d(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point4d
  mult
  (
   Point4d p, 
   Vector4d v
  ) 
  {
    Point4d rtn = new Point4d(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3807928269275895806L;

}
