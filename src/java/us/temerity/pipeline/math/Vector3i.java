// $Id: Vector3i.java,v 1.4 2004/12/16 21:35:41 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   V E C T O R   3 I                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A three component vector containing int values. <P> 
 * 
 * This class is a more restricted form of Tuple3i which uses only defines the vector operator
 * methods which make geometric sense in conjunction with the Point3i class. <P> 
 */
public 
class Vector3i
  extends Tuple3i
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Vector3i() 
  {
    super();
  }

  /**
   * Construct from individual components.
   */ 
  public 
  Vector3i
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
  Vector3i
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
  Vector3i
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
  Vector3i
  (
   Vector3i t
  ) 
  {
    this(t.pComps);
  }
	
  /**
   * Construct a vector from the first point to the second point.
   */ 
  public 
  Vector3i
  (
   Point3i a, 
   Point3i b
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
  public Vector3i
  abs() 
  {
    Vector3i rtn = new Vector3i(this);
    rtn.absolute();
    return rtn;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Vector3i
  min
  (
   Vector3i a, 
   Vector3i b 
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Vector3i
  max
  (
   Vector3i a, 
   Vector3i b 
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    rtn.max(b);
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Vector3i
  normalized() 
  {
    Vector3i rtn = new Vector3i(this);
    rtn.normalize();
    return rtn;
  }


			    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
						
  /**
   * Creates a new vector which is the cross product of the this vector and the given vector.
   */ 
  public Vector3i
  cross
  (
   Vector3i t
  ) 
  {
    return new Vector3i(pComps[1] * t.pComps[2] - pComps[2] * t.pComps[1],
			pComps[2] * t.pComps[0] - pComps[0] * t.pComps[2],
			pComps[0] * t.pComps[1] - pComps[1] * t.pComps[0]);	
  }

  /**
   * Creates a new vector which is the cross product of the given vectors.
   */ 
  public static Vector3i
  cross
  (
   Vector3i a,
   Vector3i b
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
  public Vector3i 
  add
  (
   Vector3i t
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
  public Vector3i 
  sub
  (
   Vector3i t
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
  public Vector3i 
  mult
  (
   Vector3i t
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
  public Vector3i 
  div
  (
   Vector3i t
  ) 
  {
    super.div(t);
    return this;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector by componentwise addition of the given vectors.
   */ 
  public static Vector3i
  add
  (
   Vector3i a, 
   Vector3i b
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    return rtn.add(b);
  }

  /**
   * Create a new vector by componentwise subtraction of the second vector from first vector.
   */ 
  public static Vector3i
  sub
  (
   Vector3i a, 
   Vector3i b
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    return rtn.sub(b);
  }

  /**
   * Create a new vector by componentwise multiplication of the given vectors.
   */ 
  public static Vector3i
  mult
  (
   Vector3i a, 
   Vector3i b
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    return rtn.mult(b);
  }
  
  /**
   * Create a new vector by componentwise division of the first vector by the second vector.
   */ 
  public static Vector3i
  div
  (
   Vector3i a, 
   Vector3i b
  ) 
  {
    Vector3i rtn = new Vector3i(a);
    return rtn.div(b);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3646303620663151552L;

}
