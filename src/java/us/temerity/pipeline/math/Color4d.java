// $Id: Color4d.java,v 1.3 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L O R   4 D                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An RGBA color containing double components.
 */
public 
class Color4d
  extends TupleNd
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Color4d() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Color4d
  (
   double r, 
   double g, 
   double b, 
   double a
  ) 
  {
    super(sSize);

    pComps[0] = r;
    pComps[1] = g;
    pComps[2] = b;
    pComps[3] = a;
  }

  /**
   * Construct from an array.
   * 
   * @param values
   *   The initial component values. 
   * 
   * @throws TupleSizeMismatchException
   *   If the given array is not the same size as this color.
   */ 
  public 
  Color4d
  (
   double[] values
  ) 
  {
    super(values);
    if(values.length != sSize) 
      throw new TupleSizeMismatchException(sSize, values.length);
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The color to copy.
   */ 
  public 
  Color4d
  (
   Color4d t
  ) 
  {
    this(t.pComps);
  }
		
  
    											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   A C C E S S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the value of the red component.
   */ 
  public double
  r() 
  {
    return pComps[0]; 
  }

  /** 
   * Set the value of the red component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  r
  (
   double value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the green component.
   */ 
  public double
  g() 
  {
    return pComps[1];
  }

  /** 
   * Set the value of the green component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  g
  (
   double value   
  ) 
  {  
    pComps[1] = value;
  }


  /** 
   * Get the value of the blue component.
   */ 
  public double
  b() 
  {
    return pComps[2];
  }

  /** 
   * Set the value of the blue component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  b
  (
   double value   
  ) 
  {  
    pComps[2] = value;
  }


  /** 
   * Get the value of the alpha component.
   */ 
  public double
  a() 
  {
    return pComps[3];
  }

  /** 
   * Set the value of the alpha component.
   * 
   * @param value
   *   The new component value.
   */ 
  public void
  a
  (
   double value   
  ) 
  {  
    pComps[3] = value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individuallg.   
   */ 
  public void
  set
  (
   double r, 
   double g,
   double b,
   double a
  ) 
  {
    pComps[0] = r;
    pComps[1] = g;
    pComps[2] = b;
    pComps[3] = a;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Color4d
  min
  (
   Color4d a, 
   Color4d b 
  ) 
  {
    Color4d rtn = new Color4d(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Color4d
  max
  (
   Color4d a, 
   Color4d b 
  ) 
  {
    Color4d rtn = new Color4d(a);
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
  public static Color4d
  lerp
  (
   Color4d a, 
   Color4d b, 
   double t
  ) 
  {
    Color4d rtn = new Color4d();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Color4d
  normalized() 
  {
    Color4d rtn = new Color4d(this);
    rtn.normalize();
    return rtn;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  static final long serialVersionUID = -6362156338845482665L;

  /**
   * The number of color components.
   */ 
  private static final int  sSize = 4; 


}
