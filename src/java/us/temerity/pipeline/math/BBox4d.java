// $Id: BBox4d.java,v 1.2 2004/12/14 14:08:43 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   B B O X   4 D                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * An axis aligned bound box defined two points. <P> 
 */
public 
class BBox4d 
  implements Glueable, Serializable  
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
   * @param bbox
   *   The bounding box to copy.
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
    if((obj != null) && (obj instanceof BBox4d)) {
      BBox4d t = (BBox4d) obj;
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
    Point4d min = (Point4d) decoder.decode("Min"); 
    if(min == null) 
      throw new GlueException("The \"Min\" entry was missing!");
    pMin = min;

    Point4d max = (Point4d) decoder.decode("Max"); 
    if(max == null) 
      throw new GlueException("The \"Max\" entry was missing!");
    pMax = max;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4550250439234661489L;



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
