// $Id: ExtraMath.java,v 1.2 2004/12/17 20:07:36 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   E X T R A   M A T H                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * Additional common generate math operations.
 */
public 
class ExtraMath   
{  										    
  /*----------------------------------------------------------------------------------------*/
  /*   E Q U I V A L E N C E                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the two values are within the given epsilon of each other.
   */ 
  public static boolean 
  equiv
  (
   float a, 
   float b, 
   float epsilon
  ) 
  {
    return (Math.abs(a-b) < epsilon);
  }

  /**
   * Whether the two values are within an epsilon of (0.001f).
   */ 
  public static boolean 
  equiv
  (
   float a, 
   float b
  ) 
  {
    return equiv(a, b, 0.001f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the two values are within the given epsilon of each other.
   */ 
  public static boolean 
  equiv
  (
   double a, 
   double b, 
   double epsilon
  ) 
  {
    return (Math.abs(a-b) < epsilon);
  }

  /**
   * Whether the two values are within an epsilon of (0.000001).
   */ 
  public static boolean 
  equiv
  (
   double a, 
   double b
  ) 
  {
    return equiv(a, b, 0.000001);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the two values are within the given epsilon of each other.
   */ 
  public static boolean 
  equiv
  (
   int a, 
   int b, 
   int epsilon
  ) 
  {
    return (Math.abs(a-b) < epsilon);
  }

  /**
   * Whether the two values are within an epsilon of (0).
   */ 
  public static boolean 
  equiv
  (
   int a, 
   int b
  ) 
  {
    return (a == b);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the two values are within the given epsilon of each other.
   */ 
  public static boolean 
  equiv
  (
   long a, 
   long b, 
   long epsilon
  ) 
  {
    return (Math.abs(a-b) < epsilon);
  }

  /**
   * Whether the two values are within an epsilon of (0L).
   */ 
  public static boolean 
  equiv
  (
   long a, 
   long b
  ) 
  {
    return (a == b);
  }
  

						    
  /*----------------------------------------------------------------------------------------*/
  /*   M I N / M A X                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the smaller of two values. 
   */ 
  public static <T> Comparable<T> 
  min
  (
   Comparable<T> a, 
   Comparable<T> b
  ) 
  {
    return (a.compareTo((T) b) < 0) ? a : b;      
  }
   
  /**
   * Returns the larger of two values. 
   */ 
  public static <T> Comparable<T> 
  max
  (
   Comparable<T> a, 
   Comparable<T> b
  ) 
  {
    return (a.compareTo((T) b) < 0) ? b : a;      
  }
   

  /*----------------------------------------------------------------------------------------*/
  /*   C L A M P                                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Clamps the given value bewteen the given range.
   * 
   * @param v
   *   The value to clamp.
   * 
   * @param lower
   *   The lower bound.
   *
   * @param upper
   *   The upper bound.
   * 
   */ 
  public static float
  clamp
  (
   float v, 
   float lower,
   float upper
  )
  {
    return Math.max(Math.min(v, upper), lower);
  }
   
  /**
   * Clamps the given value bewteen the given range.
   * 
   * @param v
   *   The value to clamp.
   * 
   * @param lower
   *   The lower bound.
   *
   * @param upper
   *   The upper bound.
   * 
   */ 
  public static double
  clamp
  (
   double v, 
   double lower,
   double upper
  )
  {
    return Math.max(Math.min(v, upper), lower);
  }
   
  /**
   * Clamps the given value bewteen the given range.
   * 
   * @param v
   *   The value to clamp.
   * 
   * @param lower
   *   The lower bound.
   *
   * @param upper
   *   The upper bound.
   * 
   */ 
  public static int
  clamp
  (
   int v, 
   int lower,
   int upper
  )
  {
    return Math.max(Math.min(v, upper), lower);
  }
   
  /**
   * Clamps the given value bewteen the given range.
   * 
   * @param v
   *   The value to clamp.
   * 
   * @param lower
   *   The lower bound.
   *
   * @param upper
   *   The upper bound.
   * 
   */ 
  public static long
  clamp
  (
   long v, 
   long lower,
   long upper
  )
  {
    return Math.max(Math.min(v, upper), lower);
  }

  /**
   * Clamps the given value bewteen the given range.
   * 
   * @param v
   *   The value to clamp.
   * 
   * @param lower
   *   The lower bound.
   *
   * @param upper
   *   The upper bound.
   * 
   */ 
  public static <T> Comparable<T> 
  clamp
  (
   Comparable<T> v, 
   Comparable<T> lower,
   Comparable<T> upper
  )
  {
    return max(min(v, upper), lower);
  }


   									    
  /*----------------------------------------------------------------------------------------*/
  /*  I N T E R P O L A T I O N                                                             */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Interpolate linearly between the given values. <P> 
   * 
   * @param a
   *   The first value.
   *
   * @param b
   *   The second value.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   */ 
  public static float
  lerp
  (
   float a, 
   float b,
   float t 
  )
  {
    return (a + t * (b - a));
  }
  
  /**
   * Interpolate linearly between the given values. <P> 
   * 
   * @param a
   *   The first value.
   *
   * @param b
   *   The second value.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   */ 
  public static double
  lerp
  (
   double a,
   double b,
   double t 
  )
  {
    return (a + t * (b - a));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Interpolate between the given values using the smooth step function. <P> 
   * 
   * @param a
   *   The first value.
   *
   * @param b
   *   The second value.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   */ 
  public static float
  smoothlerp
  (
   float a,
   float b,
   float t 
  )
  {
    return lerp(a, b, smoothstep(t));
  }
  
  /**
   * Interpolate between the given values using the smooth step function. <P> 
   * 
   * @param a
   *   The first value.
   *
   * @param b
   *   The second value.
   * 
   * @param t
   *   The interpolation factor: 0.0=a, 0.5=(a+b)/2, 1.0=b
   */ 
  public static double
  smoothlerp
  (
   double a,
   double b,
   double t 
  )
  {
    return lerp(a, b, smoothstep(t));
  }
   	
								    
  /*----------------------------------------------------------------------------------------*/
  /*   S M O O T H   S T E P                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The smooth step interpolation function.
   */
  public static float 
  smoothstep
  (
   float t
  ) 
  {
    return (3.0f*t*t - 2.0f*t*t*t);
  }
 
  /**
   * The smooth step interpolation function.
   */
  public static double 
  smoothstep
  (
   double t
  ) 
  {
    return (3.0f*t*t - 2.0f*t*t*t);
  }
}
