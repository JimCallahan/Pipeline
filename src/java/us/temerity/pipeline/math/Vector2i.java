// $Id: Vector2i.java,v 1.4 2004/12/16 21:35:41 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   2 I                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component vector containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple2i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point2i class. <P> 
 */
public 
class Vector2i
  extends Tuple2i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector2i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector2i
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
  Vector2i
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
  Vector2i
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
  Vector2i
  (
   Vector2i t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector2i
  (
   Point2i a, 
   Point2i b
  ) 
  {
    super();

    int i; 
    for(i=0; i<pComps.length; i++) 
      pComps[i] = b.pComps[i] - a.pComps[i];
  }	

  
    	
  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/
    		
  /**
   * Create a new vector which is the componentwise absolute value of this vector.
   */ 
  public Vector2i
  abs() 
  {
    Vector2i rtn = new Vector2i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector2i
  min
  (
   Vector2i a, 
   Vector2i b 
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector2i
  max
  (
   Vector2i a, 
   Vector2i b 
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    rtn.max(b);
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/
  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector2i
  normalized() 
  {
    Vector2i rtn = new Vector2i(this);
    rtn.normalize();
    return rtn;
  }


									    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T W I S E   O P S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given vector to this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector2i 
  add
  (
   Vector2i t
  ) 
  {
    super.add(t);
    return this;
  }

  /**
   * Subtract the given vector from this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector2i 
  sub
  (
   Vector2i t
  ) 
  {
    super.sub(t);
    return this;
  }

  /**
   * Multiply this vector by the given vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector2i 
  mult
  (
   Vector2i t
  ) 
  {
    super.mult(t);
    return this;
  }

  /**
   * Divide the given vector by this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector2i 
  div
  (
   Vector2i t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector2i
  add
  (
   Vector2i a, 
   Vector2i b
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector2i
  sub
  (
   Vector2i a, 
   Vector2i b
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector2i
  mult
  (
   Vector2i a, 
   Vector2i b
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector2i
  div
  (
   Vector2i a, 
   Vector2i b
  ) 
  {
    Vector2i rtn = new Vector2i(a);
    return rtn.div(b);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5560542871610101750L;

}
