// $Id: Point3i.java,v 1.3 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   3 I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component point containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple3i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector3i class. <P> 
 */
public 
class Point3i
  extends Tuple3i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point3i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point3i
  (
   int x, 
   int y, 
   int z
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
  Point3i
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
  Point3i
  (
   Tuple3i t
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
  Point3i
  (
   Point3i t
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
  public Point3i
  abs() 
  {
    Point3i rtn = new Point3i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point3i
  min
  (
   Point3i a, 
   Point3i b 
  ) 
  {
    Point3i rtn = new Point3i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point3i
  max
  (
   Point3i a, 
   Point3i b 
  ) 
  {
    Point3i rtn = new Point3i(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point3i
  normalized() 
  {
    Point3i rtn = new Point3i(this);
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
   Point3i p
  ) 
  {
    Vector3i v = new Vector3i(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public int
  distance
  (
   Point3i p
  ) 
  {
    Vector3i v = new Vector3i(p, this);
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
  public Point3i 
  add
  (
   Vector3i t 
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
  public Point3i 
  sub
  (
   Vector3i t
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
  public Point3i 
  mult
  (
   Vector3i t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point3i
  add
  (
   Point3i p, 
   Vector3i v
  ) 
  {
    Point3i rtn = new Point3i(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point3i
  sub
  (
   Point3i p, 
   Vector3i v
  ) 
  {
    Point3i rtn = new Point3i(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point3i
  mult
  (
   Point3i p, 
   Vector3i v
  ) 
  {
    Point3i rtn = new Point3i(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6997682793454754689L;

}
