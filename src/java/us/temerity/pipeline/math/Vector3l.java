// $Id: Vector3l.java,v 1.2 2004/12/14 12:25:59 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   3 L                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component vector containing long values. <P> 
 * 
 * This class is a more restricted form of Tuple3l which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point3l class. <P> 
 */
public 
class Vector3l
  extends Tuple3l
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector3l() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector3l
  (
   long x, 
   long y, 
   long z
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
  Vector3l
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
  Vector3l
  (
   Tuple3l t
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
  Vector3l
  (
   Vector3l t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector3l
  (
   Point3l a, 
   Point3l b
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
  public Vector3l
  abs() 
  {
    Vector3l rtn = new Vector3l(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector3l
  min
  (
   Vector3l a, 
   Vector3l b 
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector3l
  max
  (
   Vector3l a, 
   Vector3l b 
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector3l
  normalized() 
  {
    Vector3l rtn = new Vector3l(this);
    rtn.normalize();
    return rtn;
  }


			    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
						
  /**
   * Creates a new vector which is the cross product of the this vector and the given vector.
   */ 
  public Vector3l
  cross
  (
   Vector3l t
  ) 
  {
    return new Vector3l(pComps[1] * t.pComps[2] - pComps[2] * t.pComps[1],
			pComps[2] * t.pComps[0] - pComps[0] * t.pComps[2],
			pComps[0] * t.pComps[1] - pComps[1] * t.pComps[0]);	
  }

  /**
   * Creates a new vector which is the cross product of the given vectors.
   */ 
  public static Vector3l
  cross
  (
   Vector3l a,
   Vector3l b
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
  public Vector3l 
  add
  (
   Vector3l t
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
  public Vector3l 
  sub
  (
   Vector3l t
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
  public Vector3l 
  mult
  (
   Vector3l t
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
  public Vector3l 
  div
  (
   Vector3l t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector3l
  add
  (
   Vector3l a, 
   Vector3l b
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector3l
  sub
  (
   Vector3l a, 
   Vector3l b
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector3l
  mult
  (
   Vector3l a, 
   Vector3l b
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector3l
  div
  (
   Vector3l a, 
   Vector3l b
  ) 
  {
    Vector3l rtn = new Vector3l(a);
    return rtn.div(b);
  }
}
