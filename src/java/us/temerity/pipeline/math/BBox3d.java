// $Id: BBox3d.java,v 1.2 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   3 D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox3d 
  implements Glueable, Serializable  
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct unitialized.
   */ 
  public 
  BBox3d() 
  {
    pMin = new Point3d();
    pMax = new Point3d();
  }

  /**
   * Construct from two points.
   */ 
  public 
  BBox3d
  (
   Point3d a, 
   Point3d b
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
  BBox3d
  (
   BBox3d bbox
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
   Point3d p
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
   Point3d a, 
   Point3d b
  ) 
  {
    pMin = Point3d.min(a, b);
    pMax = Point3d.max(a, b);
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum corner.
   */ 
  public Point3d
  getMin() 
  {
    return new Point3d(pMin);
  }

  /**
   * Set the minimum corner.
   */ 
  public void
  setMin
  (										 
   Point3d p
  ) 
  {
    pMin = new Point3d(p);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum corner.
   */ 
  public Point3d
  getMax() 
  {
    return new Point3d(pMax);
  }

  /**
   * Set the maximum corner.
   */ 
  public void
  setMax
  (										 
   Point3d p
  ) 
  {
    pMax = new Point3d(p);
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the range of the bounding box.
   */
  public Vector3d
  getRange() 
  {
    return new Vector3d(pMin, pMax);
  }

  /** 
   * Get the center of the bounding box.
   */
  public Point3d
  getCenter() 
  {
    return Point3d.lerp(pMin, pMax, 0.5f);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Expand the existing bounding box to include the given point.
   */
  public void
  grow
  (
   Point3d p
  )
  {
    pMin = Point3d.min(pMin, p);
    pMax = Point3d.max(pMax, p);
  }

  /**
   * Expand the existing bounding box to include the given bounding box.
   */ 
  public void
  grow
  (
   BBox3d bbox
  )
  {
    pMin = Point3d.min(pMin, bbox.pMin);
    pMax = Point3d.max(pMax, bbox.pMax);
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
    if((obj != null) && (obj instanceof BBox3d)) {
      BBox3d t = (BBox3d) obj;
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
    Point3d min = (Point3d) decoder.decode("Min"); 
    if(min == null) 
      throw new GlueException("The \"Min\" entry was missing!");
    pMin = min;

    Point3d max = (Point3d) decoder.decode("Max"); 
    if(max == null) 
      throw new GlueException("The \"Max\" entry was missing!");
    pMax = max;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2626818321975206766L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum corner.
   */ 
  private Point3d  pMin; 
    	
  /**
   * The maxnimum corner.
   */ 
  private Point3d  pMax; 
    	
}
