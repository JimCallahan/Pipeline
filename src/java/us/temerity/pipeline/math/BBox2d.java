// $Id: BBox2d.java,v 1.2 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   2 D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox2d 
  implements Glueable, Serializable  
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox2d() 
  {
    pMin = new Point2d();
    pMax = new Point2d();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox2d
  (
   Point2d a, 
   Point2d b
  ) 
  {
    set(a, b);
  }

  /**
   * Copy constructor.
   * 
   * @param bbox
   *   The bounding box to copy.
   */ 
  public 
  BBox2d
  (
   BBox2d bbox
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
   Point2d p
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
   Point2d a, 
   Point2d b
  ) 
  {
    pMin = Point2d.min(a, b);
    pMax = Point2d.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point2d
  getMin() 
  {
    return new Point2d(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point2d p
  ) 
  {
    pMin = new Point2d(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point2d
  getMax() 
  {
    return new Point2d(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point2d p
  ) 
  {
    pMax = new Point2d(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector2d
  getRange() 
  {
    return new Vector2d(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point2d
  getCenter() 
  {
    return Point2d.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point2d p
  )
  {
    pMin = Point2d.min(pMin, p);
    pMax = Point2d.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox2d bbox
  )
  {
    pMin = Point2d.min(pMin, bbox.pMin);
    pMax = Point2d.max(pMax, bbox.pMax);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof BBox2d)) {
      BBox2d t = (BBox2d) obj;
      return (pMin.equals(t.pMin) && pMax.equals(t.pMax));
    }
    return false;
  }

  /**
   * Generate a string representation of this tuple.
   */ 
  public String
  toString() 
  {
    return ("[" + pMin + ", " + pMax + "]");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Min", pMin);
    encoder.encode("Max", pMax);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Point2d min = (Point2d) decoder.decode("Min"); 
    if(min == null) 
      throw new GlueException("The \"Min\" entry was missing!");
    pMin = min;

    Point2d max = (Point2d) decoder.decode("Max"); 
    if(max == null) 
      throw new GlueException("The \"Max\" entry was missing!");
    pMax = max;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1429174433364053287L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point2d  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point2d  pMax; 
    	
}
