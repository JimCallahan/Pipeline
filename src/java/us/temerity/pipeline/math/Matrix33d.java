// DO NOT EDIT! -- Automatically generated by: permutations.bash

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   3 3 D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A 3x3 matrix containing double values. <P> 
 * 
 * The methods used to access the elements of a 3x3 matrix use array-like indexing, with 
 * column indices of [0, 2] and row indices of [0, 2]: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/Matrix33ArrayIndex.gif">
 * </DIV> <P>
 * 
 * However, for visual simplicty and compatibility with existing literature on linear 
 * algebra, the standard mathematical indexing notation of [1, 3] for columns and [1, 3] 
 * for rows is used by figures in the documentation. <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/Matrix33MathIndex.gif">
 * </DIV> <P>
 */
public 
class Matrix33d
  extends MatrixMNd
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a 3x3 matrix setting all components to zero. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Zero.gif">
   * </DIV> 
   */ 
  public 
  Matrix33d()
  {
    super(3, 3);
  }

  /**
   * Construct from a two dimensional array. <P> 
   * 
   * The mapping of the elements of a two dimensional array (a[][]) to matrix components 
   * is: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Array.gif">
   * </DIV> 
   * 
   * @param mx
   *   The initial component values. 
   */ 
  public 
  Matrix33d
  (
   double[][] mx
  ) 
  {
    super(mx);
    if(mx.length != cols()) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx[0].length, mx.length);
  }

  /**
   * Copy constructor.
   * 
   * @param mx
   *   The matrix to copy.
   */ 
  public 
  Matrix33d
  (
   Matrix33d mx
  ) 
  {
    super(mx);
  }

  /**
   * Copy constructor.
   * 
   * @param mx
   *   The matrix to copy.
   */ 
  protected 
  Matrix33d
  (
   MatrixMNd mx
  ) 
  {
    super(mx);
  }

	
						    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
											    
  /**
   * Create a new vector which is the product of multiplying this matrix with the given 
   * column vector (on the right). <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33MultTuple.gif">
   * </DIV>
   * 
   * @param v
   *   The column vector to multiply with this matrix.
   * 
   * @return 
   *   The product of this matrix and the given vector.
   */ 					    
  public Tuple3d 
  mult
  (											    
   Tuple3d v
  ) 									    
  {	
    Tuple3d rtn = new Tuple3d();
    multTuple(v, rtn);
    return rtn;
  }

  											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   M A T R I X   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											    
  /**
   * Create a new matrix which is the product of multiplying this matrix with the given 
   * matrix (on the right). <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33MultMatrix.gif">
   * </DIV>
   * 
   * @param mx
   *   The matrix to multiply (on the right) with this matrix.
   */ 		    
  public Matrix33d
  mult							    
  (											    
   Matrix33d mx
  ) 									    
  {
    Matrix33d rtn = new Matrix33d();
    multMatrix(mx, rtn);
    return rtn;
  }

  /**
   * Concatenate (multiply in place) this matrix with the given matrix (on the right). 
   */ 	    
  public void
  concat
  (											    
   Matrix33d mx
  ) 									    
  {	
    concatMatrix(mx);
  }
	
										    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new matrix which is a transposed copy of this matrix. 
   */
  public Matrix33d
  transposed() 
  {
    Matrix33d rtn = new Matrix33d(this);
    rtn.transpose();
    return rtn;
  }

									    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new identity matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Identity.gif">
   * </DIV>
   */ 
  public static Matrix33d
  newIdentity() 
  {
    Matrix33d mx = new Matrix33d();
    mx.identity();
    return mx;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Make this matrix a 2D homogeneous uniform scale matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33UScale.gif">
   * </DIV>
   * 
   * @param s
   *   The uniform scale.
   */ 
  public void 
  scale
  (
   double s
  ) 
  {
    diagonal(new Tuple3d(s, s, 1.0));
  }

  /**
   * Create a new 2D homogeneous uniform scale matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33UScale.gif">
   * </DIV>
   * 
   * @param s
   *   The uniform scale.
   */ 
  public static Matrix33d
  newScale
  (
   double s
  ) 
  {
    Matrix33d rtn = new Matrix33d();
    rtn.scale(s);
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this matrix a 2D homogeneous non-uniform scale matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33NScale.gif">
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
    diagonal(new Tuple3d(v.x(), v.y(), 1.0));
  }

  /**
   * Create a new 2D homogeneous non-uniform scale matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33NScale.gif">
   * </DIV>
   * 
   * @param v
   *   The non-uniform scale vector.
   */ 
  public static Matrix33d
  newScale
  (
   Vector2d v
  ) 
  {
    Matrix33d rtn = new Matrix33d();
    rtn.scale(v);
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this matrix a 2D homogeneous translation matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Translate.gif">
   * </DIV>
   * 
   * @param v
   *   The translation vector. 
   */ 
  public void 
  translate
  (
   Vector2d v
  ) 
  {
    identity();
    pCols[2].set(new Tuple3d(v.x(), v.y(), 1.0));
  }

  /**
   * Create a new 2D homogeneous translation matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Translate.gif">
   * </DIV>
   * 
   * @param v
   *   The translation vector. 
   */ 
  public static Matrix33d
  newTranslate
  (
   Vector2d v
  ) 
  {
    Matrix33d rtn = new Matrix33d();
    rtn.translate(v);
    return rtn;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this matrix a 2D homogeneous rotation matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Rotate.gif">
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
    
    identity();
    pCols[0].setComp(0, c); pCols[0].setComp(1, -s);
    pCols[0].setComp(0, s); pCols[0].setComp(1, c);
  }

  /**
   * Create a new 2D homogeneous rotation matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Rotate.gif">
   * </DIV>
   * 
   * @param theta
   *   The rotation angle (in radians).
   */ 
  public static Matrix33d
  newRotate
  (
   double theta
  ) 
  {
    Matrix33d rtn = new Matrix33d();
    rtn.rotate(theta);
    return rtn;
  }

							    
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Find the inverse of this matrix. 
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @param re [<B>modified</B>]
   *   If successful, the row eschelon form of this matrix is stored in this parameter.
   * 
   * @return 
   *   The inverse of this matrix or <CODE>null</CODE> if the matrix is singular.
   */
  public Matrix33d
  inverse
  (
   double epsilon, 
   Matrix33d re
  )
  {
    Matrix33d rtn = new Matrix33d();
    if(inverse(epsilon, re, rtn))
      return rtn;
    return null;
  }
  
  /**
   * Find the inverse of this matrix.
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @return 
   *   The inverse of this matrix or <CODE>null</CODE> if the matrix is singular.
   */
  public Matrix33d
  inverse
  (
   double epsilon
  )
  {
    Matrix33d rtn = new Matrix33d();
    if(super.inverse(epsilon, rtn)) 
      return rtn;
    return null;
  }					



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 


}
