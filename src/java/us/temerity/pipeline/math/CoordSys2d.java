// DO NOT EDIT! -- Automatically generated by: permutations.bash

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O O R D   S Y S   2 D                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A 2D coordinate system. <P> 
 * 
 * Internally this is represented by a 3x2 matrix.  The first two columns of the matrix are 
 * the X and Y basis vectors and the last column is origin of the coordinate system. <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/CoordSys2.gif">
 * </DIV> 
 */
public 
class CoordSys2d
  extends CoordSysNd
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an uninitialized coordinate system. 
   */ 
  public 
  CoordSys2d() 
  {
    super(2);
  }

  /**
   * Construct a new coordinate system defined by the given basis vectors. <P> 
   * 
   * The origin of the coordinate system is assumed to be at: [0 0]
   * 
   * @param basisX
   *   The X basis vector. 
   * 
   * @param basisY
   *   The Y basis vector. 
   */ 
  public 
  CoordSys2d
  (
   Vector2d basisX, 
   Vector2d basisY 
  ) 
  {
    super(2);
    pBasis[0].set(basisX);
    pBasis[1].set(basisY);
  }

  /**
   * Construct a new coordinate system defined by the given basis vectors and origin. <P> 
   * 
   * @param basisX
   *   The X basis vector. 
   * 
   * @param basisY
   *   The Y basis vector. 
   * 
   * @param origin
   *   The origin of the coordinate system.
   */ 
  public 
  CoordSys2d
  (
   Vector2d basisX, 
   Vector2d basisY, 
   Point2d origin
  ) 
  {
    super(2);
    pBasis[0].set(basisX);
    pBasis[1].set(basisY);

    pOrigin.set(origin);
  }

  /**
   * Copy constructor.
   * 
   * @param cs
   *   The coordinate system to copy.
   */ 
  public 
  CoordSys2d
  (
   CoordSys2d cs
  ) 
  {
    super(cs);
  }


  											    
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the X basis vector.
   */ 
  public Vector2d
  getBasisX
  (
   int j
  ) 
  {
    Vector2d rtn = new Vector2d();
    rtn.set(pBasis[0]);
    return rtn;
  }
  
  /**
   * Set the X basis vector.
   * 
   * @param v
   *   The new basis vector.
   */ 
  public void
  setBasisX
  (
   Vector2d v
  ) 
  {
    pBasis[0].set(v);
  }


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the Y basis vector.
   */ 
  public Vector2d
  getBasisY
  (
   int j
  ) 
  {
    Vector2d rtn = new Vector2d();
    rtn.set(pBasis[1]);
    return rtn;
  }
  
  /**
   * Set the Y basis vector.
   * 
   * @param v
   *   The new basis vector.
   */ 
  public void
  setBasisY
  (
   Vector2d v
  ) 
  {
    pBasis[1].set(v);
  }
	

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the origin of the coordinate system.
   */ 
  public Point2d
  getOrigin() 
  {
    Point2d rtn = new Point2d();
    rtn.set(pOrigin);
    return rtn;
  }
  
  /**
   * Set the origin of the coordinate system.
   * 
   * @param p
   *   The new origin. 
   */ 
  public void
  setOrigin
  (
   Point2d p
  ) 
  {
    pOrigin.set(p);
  }

	
											    
  /*----------------------------------------------------------------------------------------*/
  /*   P O I N T   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Transform a point in this coordinate system to the world space (identity) coordinate 
   * system. <P> 
   * 
   * In other words, post-multiply a column vector by the basis matrix and offset by 
   * the coordinate system origin. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Coord2XformPoint.gif">
   * </DIV>
   * 
   * @param p
   *   The point in this coordinate system.
   * 
   * @return 
   *   The point in the world coordinate system.
   */
  public Point2d
  xform
  (
   Point2d p
  ) 
  {
    Point2d rtn = new Point2d();
    xformPoint(p, rtn);
    return rtn;
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Transform a vector in this coordinate system to the world space (identity) coordinate 
   * system. <P> 
   * 
   * In other words, post-multiply a column vector by the basis matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Coord2XformVector.gif">
   * </DIV>
   * 
   * @param v
   *   The vector in this coordinate system.
   * 
   * @return 
   *   The vector in the world coordinate system.
   */
  public Vector2d
  xform
  (
   Vector2d v
  ) 
  {
    Vector2d rtn = new Vector2d();
    xformVector(v, rtn);
    return rtn;
  }

  											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   M A T R I X   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											    
  /**
   * Create a new coordinate system which is the product of this coordinate system and the 
   * given coordinate system. <P> 
   * 
   * In other words, create a coordinate system which is the product of multiplying the matrix
   * representation of this coordinate system with the matrix representation of the given 
   * coordinate system (on the right). <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2MultCoordSys.gif">
   * </DIV>
   * 
   * @param cs
   *   The coordinate system multiply this coordinate system.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public CoordSys2d
  mult							    
  (											    
   CoordSys2d cs 
  ) 									    
  {
    CoordSys2d rtn = new CoordSys2d();
    multCoordSys(cs, rtn);
    return rtn;
  }

  											    
  /**
   * Concatenate the given coordinate system with this coordinate system (in place). <P> 
   * 
   * In other words, multiply (in place) the matrix representation of this coordinate system 
   * with the matrix representation of the given coordinate system (on the right). <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2MultCoordSys.gif">
   * </DIV>
   * 
   * @param cs
   *   The coordinate system multiply this coordinate system.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public void 
  concat							    
  (											    
   CoordSys2d cs 
  ) 									    
  {
    concatCoordSys(cs);
  }

	
									    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new world space (identity) coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2Identity.gif">
   * </DIV>
   * 
   * @see <A HREF="http://mathworld.wolfram.com/IdentityMatrix.html">
   *      Math World: Identity Matrix</A>
   */ 
  public static CoordSys2d
  newIdentity() 
  {
    CoordSys2d rtn = new CoordSys2d();
    rtn.identity();
    return rtn; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new uniform scaling coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2UScale.gif">
   * </DIV>
   * 
   * @param s
   *   The uniform scale.
   */ 
  public static CoordSys2d
  newScale
  (
   double s
  ) 
  {
    CoordSys2d rtn = new CoordSys2d();
    rtn.scale(s);
    return rtn; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this coordinate system a non-uniform scaling coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2NScale.gif">
   * </DIV>
   * 
   * @param v
   *   The non-uniform scale vector.
   */ 
  public void 
  scale
  (
   Vector2d v
  ) 
  {
    scale(v);
  }

  /**
   * Create a new non-uniform scaling coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2NScale.gif">
   * </DIV>
   * 
   * @param v
   *   The non-uniform scale vector.
   */ 
  public static CoordSys2d
  newScale
  (
   Vector2d v
  ) 
  {
    CoordSys2d rtn = new CoordSys2d();
    rtn.scale(v);
    return rtn; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new translation coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2Translate.gif">
   * </DIV>
   * 
   * @param t
   *   The translation vector or origin point.
   */ 
  public static CoordSys2d
  newTranslate
  (
   Tuple2d t
  ) 
  {
    CoordSys2d rtn = new CoordSys2d();
    rtn.translate(t);
    return rtn; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this coordinate system a rotation coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2Rotate.gif">
   * </DIV>
   * 
   * @param theta
   *   The rotation angle (in radians).
   */ 
  public void 
  rotate
  (
   double theta
  ) 
  {								    
    double s = (double) Math.sin(theta);
    double c = (double) Math.cos(theta);
    
    pBasis[0].setComp(0, c); pBasis[0].setComp(1, -s);
    pBasis[1].setComp(0, s); pBasis[1].setComp(1, c);

    pOrigin.setComp(0, 0.0); pOrigin.setComp(1, 0.0);
  }

  /**
   * Create a new rotation coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSys2Rotate.gif">
   * </DIV>
   * 
   * @param theta
   *   The rotation angle (in radians).
   */ 
  public static CoordSys2d
  newRotate
  (
   double theta
  ) 
  {
    CoordSys2d rtn = new CoordSys2d();
    rtn.rotate(theta);
    return rtn;
  }

							    
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Find the inverse of this coordinate system. <P> 
   * 
   * The inverse coordinate system transforms points/vectors from the world space (identity)
   * coordinate system to this coordinate system. 
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @return 
   *   The inverse coordinate system or <CODE>null</CODE> if there is no inverse. 
   */
  public CoordSys2d
  inverse
  (
   double epsilon
  )
  {
    CoordSys2d rtn = new CoordSys2d(); 
    if(inverse(epsilon, rtn)) 
      return rtn;
    return null;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1719826926386389147L;

}
