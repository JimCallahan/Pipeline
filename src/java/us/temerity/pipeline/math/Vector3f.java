// $Id: Vector3f.java,v 1.6 2009/08/19 22:37:36 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   3 F                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component vector containing float values. <P> 
 * 
 * This class is a more restricted form of Tuple3f which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point3f class. <P> 
 */
public 
class Vector3f
  extends Tuple3f
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector3f() 
  {
    super();
  }

  /**
   * Construct all components set to a constant value. 
   */ 
  public 
  Vector3f
  (
   float v
  ) 
  {
    super(v); 
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector3f
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
  Vector3f
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
  Vector3f
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
  Vector3f
  (
   Vector3f t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector3f
  (
   Point3f a, 
   Point3f b
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
  public Vector3f
  abs() 
  {
    Vector3f rtn = new Vector3f(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector3f
  min
  (
   Vector3f a, 
   Vector3f b 
  ) 
  {
    Vector3f rtn = new Vector3f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector3f
  max
  (
   Vector3f a, 
   Vector3f b 
  ) 
  {
    Vector3f rtn = new Vector3f(a);
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
  public static Vector3f
  lerp
  (
   Vector3f a, 
   Vector3f b, 
   float t
  ) 
  {
    Vector3f rtn = new Vector3f();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector3f
  normalized() 
  {
    Vector3f rtn = new Vector3f(this);
    rtn.normalize();
    return rtn;
  }


			    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
						
  /**
   * Creates a new vector which is the cross product of the this vector and the given vector.
   */ 
  public Vector3f
  cross
  (
   Vector3f t
  ) 
  {
    return new Vector3f(pComps[1] * t.pComps[2] - pComps[2] * t.pComps[1],
			pComps[2] * t.pComps[0] - pComps[0] * t.pComps[2],
			pComps[0] * t.pComps[1] - pComps[1] * t.pComps[0]);	
  }

  /**
   * Creates a new vector which is the cross product of the given vectors.
   */ 
  public static Vector3f
  cross
  (
   Vector3f a,
   Vector3f b
  ) 
  {
    return a.cross(b);
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
  public Vector3f 
  add
  (
   Vector3f t
  ) 
  {
    addTuple(t);
    return this;
  }

  /**
   * Subtract the given vector from this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector3f 
  sub
  (
   Vector3f t
  ) 
  {
    subTuple(t);
    return this;
  }

  /**
   * Multiply this vector by the given vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector3f 
  mult
  (
   Vector3f t
  ) 
  {
    multTuple(t);
    return this;
  }

  /**
   * Divide the given vector by this vector componentwise.
   * 
   * @return 
   *   This vector to allow method chaining.
   */ 
  public Vector3f 
  div
  (
   Vector3f t
  ) 
  {
    divTuple(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector3f
  add
  (
   Vector3f a, 
   Vector3f b
  ) 
  {
    Vector3f rtn = new Vector3f(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector3f
  sub
  (
   Vector3f a, 
   Vector3f b
  ) 
  {
    Vector3f rtn = new Vector3f(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector3f
  mult
  (
   Vector3f a, 
   Vector3f b
  ) 
  {
    Vector3f rtn = new Vector3f(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector3f
  div
  (
   Vector3f a, 
   Vector3f b
  ) 
  {
    Vector3f rtn = new Vector3f(a);
    return rtn.div(b);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4074371289412725210L;

}
