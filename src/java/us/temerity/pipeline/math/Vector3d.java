// $Id: Vector3d.java,v 1.2 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   3 D                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component vector containing double values. <P> 
 * 
 * This class is a more restricted form of Tuple3d which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point3d class. <P> 
 */
public 
class Vector3d
  extends Tuple3d
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector3d() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector3d
  (
   double x, 
   double y, 
   double z
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
  Vector3d
  (
   double[] values
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
  Vector3d
  (
   Tuple3d t
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
  Vector3d
  (
   Vector3d t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector3d
  (
   Point3d a, 
   Point3d b
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
  public Vector3d
  abs() 
  {
    Vector3d rtn = new Vector3d(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector3d
  min
  (
   Vector3d a, 
   Vector3d b 
  ) 
  {
    Vector3d rtn = new Vector3d(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector3d
  max
  (
   Vector3d a, 
   Vector3d b 
  ) 
  {
    Vector3d rtn = new Vector3d(a);
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
  public static Vector3d
  lerp
  (
   Vector3d a, 
   Vector3d b, 
   double t
  ) 
  {
    Vector3d rtn = new Vector3d();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector3d
  normalized() 
  {
    Vector3d rtn = new Vector3d(this);
    rtn.normalize();
    return rtn;
  }


			    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
						
  /**
   * Creates a new vector which is the cross product of the this vector and the given vector.
   */ 
  public Vector3d
  cross
  (
   Vector3d t
  ) 
  {
    return new Vector3d(pComps[1] * t.pComps[2] - pComps[2] * t.pComps[1],
			pComps[2] * t.pComps[0] - pComps[0] * t.pComps[2],
			pComps[0] * t.pComps[1] - pComps[1] * t.pComps[0]);	
  }

  /**
   * Creates a new vector which is the cross product of the given vectors.
   */ 
  public static Vector3d
  cross
  (
   Vector3d a,
   Vector3d b
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
  public Vector3d 
  add
  (
   Vector3d t
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
  public Vector3d 
  sub
  (
   Vector3d t
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
  public Vector3d 
  mult
  (
   Vector3d t
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
  public Vector3d 
  div
  (
   Vector3d t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector3d
  add
  (
   Vector3d a, 
   Vector3d b
  ) 
  {
    Vector3d rtn = new Vector3d(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector3d
  sub
  (
   Vector3d a, 
   Vector3d b
  ) 
  {
    Vector3d rtn = new Vector3d(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector3d
  mult
  (
   Vector3d a, 
   Vector3d b
  ) 
  {
    Vector3d rtn = new Vector3d(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector3d
  div
  (
   Vector3d a, 
   Vector3d b
  ) 
  {
    Vector3d rtn = new Vector3d(a);
    return rtn.div(b);
  }
}
