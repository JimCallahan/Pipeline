// $Id: Point4i.java,v 1.2 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   4 I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A four component point containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple4i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector4i class. <P> 
 */
public 
class Point4i
  extends Tuple4i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point4i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point4i
  (
   int x, 
   int y, 
   int z, 
   int w
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
  Point4i
  (
   int[] values
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
  Point4i
  (
   Tuple4i t
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
  Point4i
  (
   Point4i t
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
  public Point4i
  abs() 
  {
    Point4i rtn = new Point4i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point4i
  min
  (
   Point4i a, 
   Point4i b 
  ) 
  {
    Point4i rtn = new Point4i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point4i
  max
  (
   Point4i a, 
   Point4i b 
  ) 
  {
    Point4i rtn = new Point4i(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point4i
  normalized() 
  {
    Point4i rtn = new Point4i(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  	
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public int
  distanceSqaured
  (
   Point4i p
  ) 
  {
    Vector4i v = new Vector4i(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public int
  distance
  (
   Point4i p
  ) 
  {
    Vector4i v = new Vector4i(p, this);
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
  public Point4i 
  add
  (
   Vector4i t 
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
  public Point4i 
  sub
  (
   Vector4i t
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
  public Point4i 
  mult
  (
   Vector4i t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point4i
  add
  (
   Point4i p, 
   Vector4i v
  ) 
  {
    Point4i rtn = new Point4i(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point4i
  sub
  (
   Point4i p, 
   Vector4i v
  ) 
  {
    Point4i rtn = new Point4i(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point4i
  mult
  (
   Point4i p, 
   Vector4i v
  ) 
  {
    Point4i rtn = new Point4i(p);
    return rtn.mult(v);
  }
}
