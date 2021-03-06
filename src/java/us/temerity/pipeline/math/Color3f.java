// $Id: Color3f.java,v 1.5 2008/07/21 20:35:19 jesse Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O L O R   3 F                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * An RGB color containing float components.
 */
public 
class Color3f
  extends TupleNf
  implements Comparable<Color3f>
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with all components set to zero.
   */ 
  public 
  Color3f() 
  {
    super(sSize);
  }
  
  /**
   * Construct from individual components.
   */ 
  public 
  Color3f
  (
   float r, 
   float g, 
   float b
  ) 
  {
    super(sSize);

    pComps[0] = r;
    pComps[1] = g;
    pComps[2] = b;
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
  Color3f
  (
   float[] values
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
  Color3f
  (
   Color3f t
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
  public float
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
   float value   
  ) 
  {  
    pComps[0] = value;
  }

    
  /** 
   * Get the value of the green component.
   */ 
  public float
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
   float value   
  ) 
  {  
    pComps[1] = value;
  }


  /** 
   * Get the value of the blue component.
   */ 
  public float
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
   float value   
  ) 
  {  
    pComps[2] = value;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Set all components individuallg.   
   */ 
  public void
  set
  (
   float r, 
   float g,
   float b
  ) 
  {
    pComps[0] = r;
    pComps[1] = g;
    pComps[2] = b;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M I S C E L L A N E O U S    M A T H                                                 */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new vector which is the componentwise minimum of the given vectors.
   */ 
  public static Color3f
  min
  (
   Color3f a, 
   Color3f b 
  ) 
  {
    Color3f rtn = new Color3f(a);
    rtn.min(b);
    return rtn;
  }

  /**
   * Create a new vector which is the componentwise maximum of the given vectors.
   */ 
  public static Color3f
  max
  (
   Color3f a, 
   Color3f b 
  ) 
  {
    Color3f rtn = new Color3f(a);
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
  public static Color3f
  lerp
  (
   Color3f a, 
   Color3f b, 
   float t
  ) 
  {
    Color3f rtn = new Color3f();
    lerp(a, b, t, rtn);
    return rtn;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Create a new vector which is the normalized form of this vector.
   */ 
  public Color3f
  normalized() 
  {
    Color3f rtn = new Color3f(this);
    rtn.normalize();
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   C O L O R   C O N V E R S I O N                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Convert from an HSV color representation. 
   */ 
  public void
  fromHSV
  ( 
   Tuple3f hsv
  )
  {
    if(hsv.anyLt(new Tuple3f(0.0f, 0.0f, 0.0f))) 
      throw new IllegalArgumentException
	("The HSV color components must be positive!");

    if(hsv.x() > 360.0) 
      throw new IllegalArgumentException
	("The Hue component (X) cannot be greater than (360.0)!");
    
    if(hsv.y() > 1.0) 
      throw new IllegalArgumentException
	("The Saturation component (Y) cannot be greater than (1.0)!");

    if(hsv.z() > 1.0) 
      throw new IllegalArgumentException
	("The Value component (Y) cannot be greater than (1.0)!");
      
    /* no saturation, R = G = B = Value */ 
    if(ExtraMath.equiv(hsv.y(), 0.0f)) {
      set(hsv.z(), hsv.z(), hsv.z());
    }

    /* has saturation... */ 
    else {
      float hue  = hsv.x() / 60.0f;
      int region = (int) Math.floor((double) hue);
      float frac = hue - ((float) region);

      float p = hsv.z() * (1.0f - hsv.y());
      float q = hsv.z() * (1.0f - hsv.y() * frac);
      float t = hsv.z() * (1.0f - hsv.y() * (1.0f - frac));

      switch(region) {
      case 0:
	 pComps[0] = hsv.z();
	 pComps[1] = t;
	 pComps[2] = p;
	 break;

      case 1:
	 pComps[0] = q;
	 pComps[1] = hsv.z();
	 pComps[2] = p;
	 break;

      case 2:
	 pComps[0] = p;
	 pComps[1] = hsv.z();
	 pComps[2] = t;
	 break;

      case 3:
	 pComps[0] = p;
	 pComps[1] = q;
	 pComps[2] = hsv.z();
	 break;

      case 4:
	 pComps[0] = t;
	 pComps[1] = p;
	 pComps[2] = hsv.z();
	 break;

      default:
	 pComps[0] = hsv.z();
	 pComps[1] = p;
	 pComps[2] = q;
      }
    }
  }

  /**
   * Convert to an HSV color representation.
   */ 
  public Tuple3f
  toHSV() 
  {
    if(anyLt(new Tuple3f(0.0f, 0.0f, 0.0f))) 
      throw new IllegalArgumentException
	("The RGB color components must be positive!");

    if(anyGt(new Tuple3f(1.0f, 1.0f, 1.0f))) 
      throw new IllegalArgumentException
	("The RGB color components cannot be greater-than one!");

    Tuple3f hsv = new Tuple3f();

    float minC  = minComp();
    float maxC  = maxComp();
    float range = maxC - minC;
    
    if(ExtraMath.equiv(range, 0.0f)) {
      hsv.x(0.0f);
      hsv.y(0.0f);
      hsv.z(maxC);
    }
    else {
      if(maxC > 0.0f) {
	hsv.y(range / maxC);      
	hsv.z(maxC);
      }
      else {
	hsv.set(0.0f, 0.0f, 0.0f);
      }

      if(ExtraMath.equiv(pComps[0], maxC)) 
	hsv.x((pComps[1] - pComps[2]) / range);
      else if(ExtraMath.equiv(pComps[1], maxC)) 
	hsv.x(2.0f + (pComps[2] - pComps[0]) / range);
      else 
	hsv.x(4.0f + (pComps[0] - pComps[1]) / range);
      
      hsv.x(hsv.x() * 60.0f); 
      if(hsv.x() < 0.0f)
	hsv.x(hsv.x() + 360.0f); 
    }

    return hsv;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public int 
  compareTo
  (
    Color3f o
  )
  {
    return super.compareTo(o);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  static final long serialVersionUID = -4390780477392240248L;


  /**
   * The number of color components.
   */ 
  private static final int  sSize = 3; 

}
