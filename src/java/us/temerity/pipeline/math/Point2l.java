// $Id: Point2l.java,v 1.4 2004/12/16 21:34:49 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P O I N T   2 L                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component point containing long values. <P> 
 * 
 * This class is a more restricted form of Tuple2l which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Vector2l class. <P> 
 */
public 
class Point2l
  extends Tuple2l
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Point2l() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Point2l
  (
   long x, 
   long y
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
  Point2l
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
  Point2l
  (
   Tuple2l t
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
  Point2l
  (
   Point2l t
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
  public Point2l
  abs() 
  {
    Point2l rtn = new Point2l(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Point2l
  min
  (
   Point2l a, 
   Point2l b 
  ) 
  {
    Point2l rtn = new Point2l(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Point2l
  max
  (
   Point2l a, 
   Point2l b 
  ) 
  {
    Point2l rtn = new Point2l(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Point2l
  normalized() 
  {
    Point2l rtn = new Point2l(this);
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
   Point2l p
  ) 
  {
    Vector2l v = new Vector2l(p, this);
    return v.lengthSquared();
  }
  
  /**
   * Compute the distance between this point and the given point.
   */ 
  public long
  distance
  (
   Point2l p
  ) 
  {
    Vector2l v = new Vector2l(p, this);
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
  public Point2l 
  add
  (
   Vector2l t 
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
  public Point2l 
  sub
  (
   Vector2l t
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
  public Point2l 
  mult
  (
   Vector2l t
  ) 
  {
    super.mult(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new point by componentwise addition of the given point and vector.
   */ 
  public static Point2l
  add
  (
   Point2l p, 
   Vector2l v
  ) 
  {
    Point2l rtn = new Point2l(p);
    return rtn.add(v);
  }

  /**
   * Create a new point by componentwise subtraction of the vector from the given point.
   */ 
  public static Point2l
  sub
  (
   Point2l p, 
   Vector2l v
  ) 
  {
    Point2l rtn = new Point2l(p);
    return rtn.sub(v); 
  }

  /**
   * Create a new point by componentwise multiplication of the given point and vector.
   */ 
  public static Point2l
  mult
  (
   Point2l p, 
   Vector2l v
  ) 
  {
    Point2l rtn = new Point2l(p);
    return rtn.mult(v);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1420525176872561294L;

}
