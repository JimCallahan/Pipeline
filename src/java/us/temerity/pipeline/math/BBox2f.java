// $Id: BBox2f.java,v 1.2 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   2 F                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox2f 
  implements Glueable, Serializable  
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox2f() 
  {
    pMin = new Point2f();
    pMax = new Point2f();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox2f
  (
   Point2f a, 
   Point2f b
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
  BBox2f
  (
   BBox2f bbox
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
   Point2f p
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
   Point2f a, 
   Point2f b
  ) 
  {
    pMin = Point2f.min(a, b);
    pMax = Point2f.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point2f
  getMin() 
  {
    return new Point2f(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point2f p
  ) 
  {
    pMin = new Point2f(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point2f
  getMax() 
  {
    return new Point2f(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point2f p
  ) 
  {
    pMax = new Point2f(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector2f
  getRange() 
  {
    return new Vector2f(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point2f
  getCenter() 
  {
    return Point2f.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point2f p
  )
  {
    pMin = Point2f.min(pMin, p);
    pMax = Point2f.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox2f bbox
  )
  {
    pMin = Point2f.min(pMin, bbox.pMin);
    pMax = Point2f.max(pMax, bbox.pMax);
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
    if((obj != null) && (obj instanceof BBox2f)) {
      BBox2f t = (BBox2f) obj;
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
    Point2f min = (Point2f) decoder.decode("Min"); 
    if(min == null) 
      throw new GlueException("The \"Min\" entry was missing!");
    pMin = min;

    Point2f max = (Point2f) decoder.decode("Max"); 
    if(max == null) 
      throw new GlueException("The \"Max\" entry was missing!");
    pMax = max;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4684427252547347L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point2f  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point2f  pMax; 
    	
}
