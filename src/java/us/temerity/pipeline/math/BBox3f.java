// $Id: BBox3f.java,v 1.2 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   3 F                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox3f 
  implements Glueable, Serializable  
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox3f() 
  {
    pMin = new Point3f();
    pMax = new Point3f();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox3f
  (
   Point3f a, 
   Point3f b
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
  BBox3f
  (
   BBox3f bbox
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
   Point3f p
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
   Point3f a, 
   Point3f b
  ) 
  {
    pMin = Point3f.min(a, b);
    pMax = Point3f.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point3f
  getMin() 
  {
    return new Point3f(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point3f p
  ) 
  {
    pMin = new Point3f(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point3f
  getMax() 
  {
    return new Point3f(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point3f p
  ) 
  {
    pMax = new Point3f(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector3f
  getRange() 
  {
    return new Vector3f(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point3f
  getCenter() 
  {
    return Point3f.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point3f p
  )
  {
    pMin = Point3f.min(pMin, p);
    pMax = Point3f.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox3f bbox
  )
  {
    pMin = Point3f.min(pMin, bbox.pMin);
    pMax = Point3f.max(pMax, bbox.pMax);
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
    if((obj != null) && (obj instanceof BBox3f)) {
      BBox3f t = (BBox3f) obj;
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
    Point3f min = (Point3f) decoder.decode("Min"); 
    if(min == null) 
      throw new GlueException("The \"Min\" entry was missing!");
    pMin = min;

    Point3f max = (Point3f) decoder.decode("Max"); 
    if(max == null) 
      throw new GlueException("The \"Max\" entry was missing!");
    pMax = max;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3684913278002275578L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point3f  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point3f  pMax; 
    	
}
