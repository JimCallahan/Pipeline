// $Id: Point4f.java,v 1.1 2004/12/13 09:09:30 jim Exp $

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
  /*   U N A R Y   O P S                                                                    */
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
