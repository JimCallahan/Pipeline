// $Id: BBox4f.java,v 1.1 2004/12/14 12:26:25 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   4 F                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox4f 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox4f() 
  {
    pMin = new Point4f();
    pMax = new Point4f();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox4f
  (
   Point4f a, 
   Point4f b
  ) 
  {
    set(a, b);
  }

  /**
   * Copy constructor.
   * 
   * @param t
   *   The tuple to copy.
   */ 
  public 
  BBox4f
  (
   BBox4f bbox
  ) 
  {
    pMin = bbox.getMin();
    pMax = bbox.getMax();
  }
		

									 
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											 
  /**
   * Is given position inside the bounding box?
   */ 					 
  public boolean
  isInside										 
  ( 											 
   Point4f p
  ) 
  {
    return (p.allGe(pMin) && p.allLt(pMax));
  }									 



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the bounds to the box defined by the given points.
   */
  public void 
  set
  (
   Point4f a, 
   Point4f b
  ) 
  {
    pMin = Point4f.min(a, b);
    pMax = Point4f.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point4f
  getMin() 
  {
    return new Point4f(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point4f p
  ) 
  {
    pMin = new Point4f(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point4f
  getMax() 
  {
    return new Point4f(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point4f p
  ) 
  {
    pMax = new Point4f(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector4f
  getRange() 
  {
    return new Vector4f(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point4f
  getCenter() 
  {
    return Point4f.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point4f p
  )
  {
    pMin = Point4f.min(pMin, p);
    pMax = Point4f.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox4f bbox
  )
  {
    pMin = Point4f.min(pMin, bbox.pMin);
    pMax = Point4f.max(pMax, bbox.pMax);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point4f  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point4f  pMax; 
    	
}
