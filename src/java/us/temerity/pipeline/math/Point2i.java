// $Id: Point2i.java,v 1.4 2004/12/16 21:34:49 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   2 I                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component point containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple2i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector2i class. <P> 
 */
public 
class Point2i
  extends Tuple2i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point2i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point2i
  (
   int x, 
   int y
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
  Point2i
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
  Point2i
  (
   Tuple2i t
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
  Point2i
  (
   Point2i t
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
  public Point2i
  abs() 
  {
    Point2i rtn = new Point2i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point2i
  min
  (
   Point2i a, 
   Point2i b 
  ) 
  {
    Point2i rtn = new Point2i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point2i
  max
  (
   Point2i a, 
   Point2i b 
  ) 
  {
    Point2i rtn = new Point2i(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point2i
  normalized() 
  {
    Point2i rtn = new Point2i(this);
    rtn.normalize();
    return rtn;
  }

	
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Compute the distance squared between this point and the given point.
   */ 
  public int
  distanceSquared
  (
   Point2i p
  ) 
  {
    Vector2i v = new Vector2i(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public int
  distance
  (
   Point2i p
  ) 
  {
    Vector2i v = new Vector2i(p, this);
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
  public Point2i 
  add
  (
   Vector2i t 
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
  public Point2i 
  sub
  (
   Vector2i t
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
  public Point2i 
  mult
  (
   Vector2i t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point2i
  add
  (
   Point2i p, 
   Vector2i v
  ) 
  {
    Point2i rtn = new Point2i(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point2i
  sub
  (
   Point2i p, 
   Vector2i v
  ) 
  {
    Point2i rtn = new Point2i(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point2i
  mult
  (
   Point2i p, 
   Vector2i v
  ) 
  {
    Point2i rtn = new Point2i(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1737829702409090627L;

}
