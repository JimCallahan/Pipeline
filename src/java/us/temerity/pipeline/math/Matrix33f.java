// $Id: Matrix33f.java,v 1.3 2004/12/22 00:45:16 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   3 3 F                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A 3x3 matrix containing float values. <P> 
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
class Matrix33f
  extends MatrixMNf
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
  Matrix33f()
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
  Matrix33f
  (
   float[][] mx
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
  Matrix33f
  (
   Matrix33f mx
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
  Matrix33f
  (
   MatrixMNf mx
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
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 					    
  public Tuple3f 
  mult
  (											    
   Tuple3f v
  ) 									    
  {	
    Tuple3f rtn = new Tuple3f();
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
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public Matrix33f
  mult							    
  (											    
   Matrix33f mx
  ) 									    
  {
    Matrix33f rtn = new Matrix33f();
    multMatrix(mx, rtn);
    return rtn;
  }

  /**
   * Concatenate (multiply in place) this matrix with the given matrix (on the right). 
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 	    
  public void
  concat
  (											    
   Matrix33f mx
  ) 									    
  {	
    concatMatrix(mx);
  }
	
										    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new matrix which is a transposed copy of this matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/Matrix33Transpose.gif">
   * </DIV>
   * 
   * @see <A HREF="http://mathworld.wolfram.com/Transpose.html">
   *      Math World: Matrix Transpose </A>
   */
  public Matrix33f
  transposed() 
  {
    Matrix33f rtn = new Matrix33f(this);
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
   * 
   * @see <A HREF="http://mathworld.wolfram.com/IdentityMatrix.html">
   *      Math World: Identity Matrix</A>
   */ 
  public static Matrix33f
  newIdentity() 
  {
    Matrix33f mx = new Matrix33f();
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
   float s
  ) 
  {
    diagonal(new Tuple3f(s, s, 1.0f));
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
  public static Matrix33f
  newScale
  (
   float s
  ) 
  {
    Matrix33f rtn = new Matrix33f();
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
   Vector2f v
  ) 
  {
    diagonal(new Tuple3f(v.x(), v.y(), 1.0f));
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
  public static Matrix33f
  newScale
  (
   Vector2f v
  ) 
  {
    Matrix33f rtn = new Matrix33f();
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
   Vector2f v
  ) 
  {
    identity();
    pCols[2].set(new Tuple3f(v.x(), v.y(), 1.0f));
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
  public static Matrix33f
  newTranslate
  (
   Vector2f v
  ) 
  {
    Matrix33f rtn = new Matrix33f();
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
   float theta
  ) 
  {								    
    float s = (float) Math.sin(theta);
    float c = (float) Math.cos(theta);
    
    identity();
    pCols[0].setComp(0, c); pCols[0].setComp(1, -s);
    pCols[1].setComp(0, s); pCols[1].setComp(1, c);
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
  public static Matrix33f
  newRotate
  (
   float theta
  ) 
  {
    Matrix33f rtn = new Matrix33f();
    rtn.rotate(theta);
    return rtn;
  }

							    
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Find the inverse of this matrix. <P> 
   * 
   * The inverse of a matrix is defined as: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixInverse.gif">
   * </DIV> <P> 
   * 
   * Where <CODE>I</CODE> is the identity matrix.  This method computes the inverse of
   * the matrix using <A HREF="http://mathworld.wolfram.com/GaussianElimination.html">
   * Gaussian Elimination</A>.
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
  public Matrix33f
  inverse
  (
   float epsilon, 
   Matrix33f re
  )
  {
    Matrix33f rtn = new Matrix33f();
    if(inverse(epsilon, re, rtn))
      return rtn;
    return null;
  }
  
  /**
   * Find the inverse of this matrix. <P> 
   * 
   * The inverse of a matrix is defined as: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixInverse.gif">
   * </DIV> <P> 
   * 
   * Where <CODE>I</CODE> is the identity matrix.  This method computes the inverse of
   * the matrix using <A HREF="http://mathworld.wolfram.com/GaussianElimination.html">
   * Gaussian Elimination</A>.
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @return 
   *   The inverse of this matrix or <CODE>null</CODE> if the matrix is singular.
   */
  public Matrix33f
  inverse
  (
   float epsilon
  )
  {
    Matrix33f rtn = new Matrix33f();
    if(super.inverse(epsilon, rtn)) 
      return rtn;
    return null;
  }					



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 


}
