// $Id: Point3f.java,v 1.1 2004/12/13 09:09:30 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   3 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component point containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple3f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector3f class. <P> 
 */
public 
class Point3f
  extends Tuple3f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point3f() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point3f
  (
   float x, 
   float y, 
   float z
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
  Point3f
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
  Point3f
  (
   Tuple3f t
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
  Point3f
  (
   Point3f t
  ) 
  {
    this(t.pComps);
  }
	
	
  
  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public float
  distanceSqaured
  (
   Point3f p
  ) 
  {
    Vector3f v = new Vector3f(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public float
  distance
  (
   Point3f p
  ) 
  {
    Vector3f v = new Vector3f(p, this);
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
  public Point3f 
  add
  (
   Vector3f t 
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
  public Point3f 
  sub
  (
   Vector3f t
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
  public Point3f 
  mult
  (
   Vector3f t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point3f
  add
  (
   Point3f p, 
   Vector3f v
  ) 
  {
    Point3f rtn = new Point3f(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point3f
  sub
  (
   Point3f p, 
   Vector3f v
  ) 
  {
    Point3f rtn = new Point3f(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point3f
  mult
  (
   Point3f p, 
   Vector3f v
  ) 
  {
    Point3f rtn = new Point3f(p);
    return rtn.mult(v);
  }
}
