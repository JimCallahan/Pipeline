// $Id: Color3f.java,v 1.1 2004/12/13 09:09:30 jim Exp $

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
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of color components.
   */ 
  private static final int  sSize = 3; 


}
