// $Id: BBox4d.java,v 1.1 2004/12/14 12:26:25 jim Exp $

package us.temerity.pipeline.math;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   4 D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox4d 
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox4d() 
  {
    pMin = new Point4d();
    pMax = new Point4d();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox4d
  (
   Point4d a, 
   Point4d b
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
  BBox4d
  (
   BBox4d bbox
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
   Point4d p
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
   Point4d a, 
   Point4d b
  ) 
  {
    pMin = Point4d.min(a, b);
    pMax = Point4d.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point4d
  getMin() 
  {
    return new Point4d(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point4d p
  ) 
  {
    pMin = new Point4d(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point4d
  getMax() 
  {
    return new Point4d(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point4d p
  ) 
  {
    pMax = new Point4d(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector4d
  getRange() 
  {
    return new Vector4d(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point4d
  getCenter() 
  {
    return Point4d.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point4d p
  )
  {
    pMin = Point4d.min(pMin, p);
    pMax = Point4d.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox4d bbox
  )
  {
    pMin = Point4d.min(pMin, bbox.pMin);
    pMax = Point4d.max(pMax, bbox.pMax);
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point4d  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point4d  pMax; 
    	
}
