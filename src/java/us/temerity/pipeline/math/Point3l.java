// $Id: Point3l.java,v 1.4 2004/12/16 21:34:49 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   3 L                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component point containing long values. <P> 
 * 
 * This class is a more restricted form of Tuple3l which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector3l class. <P> 
 */
public 
class Point3l
  extends Tuple3l
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point3l() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point3l
  (
   long x, 
   long y, 
   long z
  ) 
  {
    super(x, y, z);
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
  Point3l
  (
   long[] values
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
  Point3l
  (
   Tuple3l t
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
  Point3l
  (
   Point3l t
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
  public Point3l
  abs() 
  {
    Point3l rtn = new Point3l(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point3l
  min
  (
   Point3l a, 
   Point3l b 
  ) 
  {
    Point3l rtn = new Point3l(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point3l
  max
  (
   Point3l a, 
   Point3l b 
  ) 
  {
    Point3l rtn = new Point3l(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point3l
  normalized() 
  {
    Point3l rtn = new Point3l(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public long
  distanceSquared
  (
   Point3l p
  ) 
  {
    Vector3l v = new Vector3l(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public long
  distance
  (
   Point3l p
  ) 
  {
    Vector3l v = new Vector3l(p, this);
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
  public Point3l 
  add
  (
   Vector3l t 
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
  public Point3l 
  sub
  (
   Vector3l t
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
  public Point3l 
  mult
  (
   Vector3l t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point3l
  add
  (
   Point3l p, 
   Vector3l v
  ) 
  {
    Point3l rtn = new Point3l(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point3l
  sub
  (
   Point3l p, 
   Vector3l v
  ) 
  {
    Point3l rtn = new Point3l(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point3l
  mult
  (
   Point3l p, 
   Vector3l v
  ) 
  {
    Point3l rtn = new Point3l(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5342940559792883146L;

}
