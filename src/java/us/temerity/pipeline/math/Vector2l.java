// $Id: Vector2l.java,v 1.4 2004/12/16 21:35:41 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   2 L                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A two component vector containing long values. <P> 
 * 
 * This class is a more restricted form of Tuple2l which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point2l class. <P> 
 */
public 
class Vector2l
  extends Tuple2l
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector2l() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector2l
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
  Vector2l
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
  Vector2l
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
  Vector2l
  (
   Vector2l t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector2l
  (
   Point2l a, 
   Point2l b
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
  public Vector2l
  abs() 
  {
    Vector2l rtn = new Vector2l(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector2l
  min
  (
   Vector2l a, 
   Vector2l b 
  ) 
  {
    Vector2l rtn = new Vector2l(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector2l
  max
  (
   Vector2l a, 
   Vector2l b 
  ) 
  {
    Vector2l rtn = new Vector2l(a);
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
  public Vector2l
  normalized() 
  {
    Vector2l rtn = new Vector2l(this);
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
  public Vector2l 
  add
  (
   Vector2l t
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
  public Vector2l 
  sub
  (
   Vector2l t
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
  public Vector2l 
  mult
  (
   Vector2l t
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
  public Vector2l 
  div
  (
   Vector2l t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector2l
  add
  (
   Vector2l a, 
   Vector2l b
  ) 
  {
    Vector2l rtn = new Vector2l(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector2l
  sub
  (
   Vector2l a, 
   Vector2l b
  ) 
  {
    Vector2l rtn = new Vector2l(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector2l
  mult
  (
   Vector2l a, 
   Vector2l b
  ) 
  {
    Vector2l rtn = new Vector2l(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector2l
  div
  (
   Vector2l a, 
   Vector2l b
  ) 
  {
    Vector2l rtn = new Vector2l(a);
    return rtn.div(b);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4680854728302037518L;

}
