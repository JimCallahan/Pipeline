// $Id: MatrixMNf.java,v 1.6 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   M N F                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An arbitrary sized MxN matrix containing float values. <P> 
 * 
 * The methods used to access the elements of a MxN matrix use array-like indexing, with 
 * column indices of [0, n-1] and row indices of [0, m-1]: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/MatrixArrayIndex.gif">
 * </DIV> <P>
 * 
 * However, for visual simplicty and compatibility with existing literature on linear 
 * algebra, the standard mathematical indexing notation of [1, n] for columns and [1, m] 
 * for rows is used by figures in the documentation. <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/MatrixMathIndex.gif">
 * </DIV> <P>
 * 
 * @see <A HREF="http://mathworld.wolfram.com/topics/MatrixOperations.html">
 *      Math World: Matrix Operations</A>
 */
public 
class MatrixMNf
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an (MxN) matrix setting all components to zero. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixZero.gif">
   * </DIV> 
   * 
   * @param rows
   *   The number of rows (M).
   * 
   * @param cols
   *   The number of columns (N).
   */ 
  public 
  MatrixMNf
  (
   int rows,
   int cols
  ) 
  {
    pCols = new TupleNf[cols];

    int j;
    for(j=0; j<pCols.length; j++) 
      pCols[j] = new TupleNf(rows);
  }

  /**
   * Construct from a two dimensional array. <P> 
   * 
   * The mapping of the elements of a two dimensional array (a[][]) to matrix components 
   * is: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixArray.gif">
   * </DIV> 
   * 
   * @param mx
   *   The initial component values. 
   */ 
  public 
  MatrixMNf
  (
   float[][] mx
  ) 
  {
    try {
      int rows = mx.length;
      int cols = mx[0].length;
      
      pCols = new TupleNf[cols];

      int j;
      for(j=0; j<cols; j++) {
	pCols[j] = new TupleNf(rows);

	int i;
	for(i=0; i<rows; i++) 
	  pCols[j].setComp(i, mx[i][j]);
      }
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new IllegalArgumentException
	("The initial values array had an inconsistent number of column values!");
    }
    catch(NullPointerException ex) {
      throw new IllegalArgumentException
	("The initial values array contained (null) values!");      
    }
  }

  /**
   * Copy constructor.
   * 
   * @param mx
   *   The matrix to copy.
   */ 
  public 
  MatrixMNf
  (
   MatrixMNf mx
  ) 
  {
    pCols = new TupleNf[mx.cols()];
    
    int j;
    for(j=0; j<pCols.length; j++) 
      pCols[j] = new TupleNf(mx.pCols[j]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   M I S C                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of rows in this matrix.
   */ 
  public int 
  rows() 
  {
    return pCols[0].size();
  }

  /**
   * Get the number columns in this matrix.
   */ 
  public int 
  cols() 
  {
    return pCols.length;
  }


  									    
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											    
  /**
   * Whether this matrix is square. <P> 
   * 
   * A square matrix has the same number of rows as columns.
   */
  public boolean
  isSquare() 
  {											    
    return (rows() == cols()); 
  }			

  /**
   * Whether the given matrix is the same size as this matrix.
   */ 
  public boolean
  isSameSize
  (
   MatrixMNf mx
  ) 
  {
    return ((rows() == mx.rows()) && (cols() == mx.cols()));
  }

     
											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   A C C E S S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the Ith row vector.
   * 
   * @param i
   *   The row index: [0, m-1]
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row index is not valid.
   */
  public TupleNf
  getRow
  (
   int i
  ) 
  {	
    try {
      TupleNf row = new TupleNf(cols());

      int j;
      for(j=0; j<cols(); j++)
	row.setComp(j, pCols[j].getComp(i));

      return row;
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }       
  }	

  /** 
   * Set the Ith row vector.
   * 
   * @param i
   *   The row index: [0, m-1]
   * 
   * @param v
   *   The new row vector.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row index is not valid.
   * 
   * @throws RowSizeMismatchException
   *   The the given row vector is not the same size a row of this matrix.
   */
  public void 
  setRow
  (
   int i, 
   TupleNf v
  ) 
  {
    if(v.size() != rows())
      throw new RowSizeMismatchException(v.size(), rows());

    try {
      int j;
      for(j=0; j<v.size(); j++)
	pCols[j].setComp(i, v.getComp(j));
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(i, 0);
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Jth column vector.
   * 
   * @param j
   *   The column index: [0, n-1]
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the column index is not valid.
   */
  public TupleNf
  getCol
  (
   int j
  ) 
  {	
    try {
      return new TupleNf(pCols[j]);
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(0, j);
    }       
  }	
  
  /** 
   * Set the Jth column vector.
   * 
   * @param j
   *   The column index: [0, n-1]
   * 
   * @param v
   *   The new column vector.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the column index is not valid.
   * 
   * @throws ColumnSizeMismatchException
   *   The the given column vector is not the same size a column of this matrix.
   */
  public void 
  setCol
  (
   int j, 
   TupleNf v
  ) 
  {
    try {
      pCols[j].set(v);
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(0, j);      
    }
    catch(TupleSizeMismatchException ex) {
      throw new ColumnSizeMismatchException(v.size(), cols());
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the component at the Jth column and Ith row. 
   * 
   * @param j
   *   The column index: [0, n-1]
   * 
   * @param i
   *   The row index: [0, m-1]
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the matrix index is not valid.
   */
  public float 
  getComp
  (
   int j,
   int i
  ) 
  {	
    try {
      return (pCols[j].getComp(i)); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(i, j);
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(i, j);
    }       
  }			
  
   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the components from a two dimensional array. <P> 
   * 
   * The mapping of the elements of a two dimensional array (a[][]) to matrix components 
   * is: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixArray.gif">
   * </DIV> 
   *
   * @param mx
   *   The new component values. 
   * 
   * @throws RowSizeMismatchException
   *   The given array does not have the same number of rows as this matrix.
   * 
   * @throws ColumnSizeMismatchException
   *   The the given array does not have the same number of columns as this matrix.
   */ 
  public void 
  set
  (
   float[][] mx
  ) 
  {
    try {
      if(rows() != mx.length) 
	throw new RowSizeMismatchException(rows(), mx.length);

      if(cols() != mx[0].length) 
	throw new ColumnSizeMismatchException(cols(), mx.length); 
      
      int i;
      for(i=0; i<rows(); i++) {
	int j;
	for(j=0; j<cols(); j++) 
	  pCols[j].setComp(i, mx[i][j]);
      }
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new IllegalArgumentException
	("The initial values array had an inconsistent number of column values!");
    }
    catch(NullPointerException ex) {
      throw new IllegalArgumentException
	("The initial values array contained (null) values!");      
    }
  }

  /**
   * Returns an array containing a copy of the component values. <P> 
   * 
   * The mapping of matrix components to the elements of a two dimensional array (a[][]) 
   * is: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixArray.gif">
   * </DIV> 
   */
  public float[][]
  toArray() 
  {
    float[][] mx = new float[rows()][];
    
    int i;
    for(i=0; i<rows(); i++) {
      mx[i] = new float[cols()];
      
      int j;
      for(j=0; j<cols(); j++) 
	mx[i][j] = pCols[j].getComp(i);
    }

    return mx;
  }

  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Set components from another matrix.
   * 
   * @param mx
   *   The matrix to copy. 
   * 
   * @throws MatrixSizeMismatchException
   *   The the given matrix does not have the same nuber of rows and columns as this matrix.
   */ 
  public void 
  set
  (
   MatrixMNf mx
  ) 
  {
    if(!isSameSize(mx)) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx.rows(), mx.cols());

    int j;
    for(j=0; j<cols(); j++) 
      pCols[j].set(mx.pCols[j]);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U N A R Y   O P S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Negate the components of this matrix in place.
   */
  public void 
  negate()
  {
    int j; 
    for(j=0; j<cols(); j++) 
      pCols[j].negate();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S C A L A R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Add a scalar to each component of this matrix.
   */ 
  public void 
  add
  (
   float v
  ) 
  {
    int j; 
    for(j=0; j<cols(); j++) 
      pCols[j].add(v);
  }
	
  /**
   * Subtract a scalar from each component of this matrix.
   */ 
  public void 
  sub
  (
   float v
  ) 
  {
    int j; 
    for(j=0; j<cols(); j++) 
      pCols[j].sub(v);
  }
	
  /**
   * Multiply each component of this matrix by a scalar.
   */ 
  public void 
  mult
  (
   float v
  ) 
  {
    int j; 
    for(j=0; j<cols(); j++) 
      pCols[j].mult(v);
  }
	
  /**
   * Divide each component of this matrix by a scalar. 
   */ 
  public void 
  div
  (
   float v
  ) 
  {
    int j; 
    for(j=0; j<cols(); j++) 
      pCols[j].div(v);
  }											    
											    
  											    
						    
  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
											    
  /**
   * Create a new vector which is the product of multiplying this matrix with the given 
   * column vector (on the right). <P> 
   * 
   * Multiplies a MxN matrix by a Nx1 vector to produce a Mx1 vector: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixMultTuple.gif">
   * </DIV>
   * 
   * @param v
   *   The column vector to multiply with this matrix.
   * 
   * @param rtn
   *   The results of the matrix vector multiplication are stored in this vector.
   * 
   * @throws ColumnSizeMismatchException
   *   If the number columns in this matrix is not identical to the number of components 
   *   in the given vector.  
   *    
   * @throws RowSizeMismatchException
   *   If the number of rows in this matrix is not identical to the number of components 
   *   in the results vector.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 					    
  public void
  multTuple
  (											    
   TupleNf v, 
   TupleNf rtn
  ) 									    
  {		
    if(cols() != v.size()) 
      throw new ColumnSizeMismatchException(cols(), v.size());

    if(rows() != rtn.size()) 
      throw new RowSizeMismatchException(rows(), rtn.size());

    int j; 
    for(j=0; j<cols(); j++) {
      TupleNf p = new TupleNf(pCols[j]);
      p.mult(v.getComp(j));
      rtn.addTuple(p);
    }
  }
      
						    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   R O W   O P S                                                                        */
  /*----------------------------------------------------------------------------------------*/
											    
  /**
   * Exchange two rows of this matrix.
   * 
   * @param i1
   *   The index of the first row: [0, m-1]
   * 
   * @param i2
   *   The index of the second row: [0, m-1]
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row indices are not valid.
   */ 
  public void
  rowOpI										    
  ( 											    
   int i1, 
   int i2
  ) 											    
  {	
    if(i1 == i2) 
      return;

    try {
      int j; 
      for(j=0; j<cols(); j++) {
	float v = pCols[j].getComp(i1);
	pCols[j].setComp(i1, pCols[j].getComp(i2));
	pCols[j].setComp(i2, v);
      }
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
    											    
  /**
   * Scale the values of a row of this matrix by dividing it by the given scalar.
   * 
   * @param i
   *   The index of the row to scale: [0, m-1]
   * 
   * @param s
   *   The row divisor. 
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row index is not valid.
   */ 						    
  public void
  rowOpII										    
  ( 											    
   int i, 
   float s
  ) 											    
  {
    try {
      int j; 
      for(j=0; j<cols(); j++) 
	pCols[j].setComp(i, pCols[j].getComp(i) / s);
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
											    
  /**
   * Add a multiple of one row of this matrix to another row.
   * 
   * @param i1
   *   The index of the row to modify: [0, m-1]
   * 
   * @param i2
   *   The index of the row to scale and add to the modified row: [0, m-1]
   * 
   * @param s
   *   The scaling factor.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row indices are not valid.
   */ 
  public void
  rowOpIII
  ( 											    
   int i1,
   int i2, 
   float s
  ) 											    
  {											    
    try {
      int j; 
      for(j=0; j<cols(); j++) 
	pCols[j].setComp(i1, pCols[j].getComp(i1) + (pCols[j].getComp(i2) * s)); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
  											    
  											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   M A T R I X   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											    
  /**
   * Create a new matrix which is the product of multiplying this matrix with the given 
   * matrix (on the right). <P> 
   * 
   * Matrix multiplication of a MxN matrix by a NxP matrix produces a MxP matrix: <P>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixMultMatrix.gif">
   * </DIV>
   * 
   * @param mx
   *   The matrix to multiply (on the right) with this matrix.
   * 
   * @param rtn
   *   The product is stored in this matrix.
   * 
   * @throws MatrixSizeMismatchException
   *   If the sizes of the matrices do not satisfy size requirements above.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public void
  multMatrix										    
  (											    
   MatrixMNf mx, 
   MatrixMNf rtn
  ) 									    
  {	
    if(cols() != mx.rows()) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx.rows(), mx.cols());

    if(mx.cols() != rtn.cols())
      throw new MatrixSizeMismatchException(mx.rows(), mx.cols(), rtn.rows(), rtn.cols());

    if(rows() != rtn.rows())
      throw new MatrixSizeMismatchException(rows(), cols(), rtn.rows(), rtn.cols());
    
    int j;
    for(j=0; j<mx.cols(); j++) {
      TupleNf t = new TupleNf(rows());
      multTuple(mx.getCol(j), t);
      rtn.setCol(j, t);
    }
  }

  /**
   * Concatenate (multiply in place) this matrix with the given matrix (on the right). <P> 
   * 
   * @throws MatrixSizeMismatchException
   *   If the size the given matrix is not identical to this matrix.
   * 
   * @throws UnsupportedOperationException 
   *   If either of the matrices are not square.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 	    
  public void
  concatMatrix									    
  (											    
   MatrixMNf mx
  ) 									    
  {	
    if(!isSameSize(mx)) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx.rows(), mx.cols());

    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot concatenate non-square matrices in place!");

    MatrixMNf rtn = new MatrixMNf(rows(), cols());
    multMatrix(mx, rtn);
    set(rtn);
  }
	
									    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new matrix which is a transposed copy of this matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixTranspose.gif">
   * </DIV>
   * 
   * @param rtn
   *   The results of the transpose are stored in matrix.
   * 
   * @throws MatrixSizeMismatchException
   *   If the number rows in this matrix is not identical to the number of columns in the
   *   results matrix.  If the number of columns in this matrix in not identical to the 
   *   number of rows in the results matrix. 
   * 
   * @see <A HREF="http://mathworld.wolfram.com/Transpose.html">
   *      Math World: Matrix Transpose </A>
   */
  public void 
  transpose
  (
   MatrixMNf rtn
  ) 
  {	
    if((cols() != rtn.rows()) || (rows() != rtn.cols()))
      throw new MatrixSizeMismatchException(rows(), cols(), rtn.rows(), rtn.cols());
    										    
    int i, j;									    
    for(j=0; j<cols(); j++) 
      for(i=0; i<rows(); i++) 								    
	rtn.pCols[i].setComp(j, pCols[j].getComp(i));
  }
	
  /**
   * Transpose this square matrix in place. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixTranspose.gif">
   * </DIV>
   * 
   * @throws UnsupportedOperationException 
   *   If either of the matrices are not square.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/Transpose.html">
   *      Math World: Matrix Transpose </A>
   */
  public void
  transpose() 
  {
    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot transpose a on-square matrix in place!");
    
    MatrixMNf mx = new MatrixMNf(this);
    										    
    int i, j;									    
    for(j=0; j<cols(); j++) 
      for(i=0; i<rows(); i++) 								    
	pCols[j].setComp(i, mx.pCols[i].getComp(j));
  }
	
										    
  /*----------------------------------------------------------------------------------------*/
				
  /**
   * Zero all components of this matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixZero.gif">
   * </DIV>
   */ 
  public void 
  zero()
  {
    int i, j;									    
    for(j=0; j<rows(); j++) 
      for(i=0; i<cols(); i++)
	pCols[j].setComp(i, 0.0f);
  }
				
  /**
   * Make this matrix an identiy matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixIdentity.gif">
   * </DIV>
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/IdentityMatrix.html">
   *      Math World: Identity Matrix</A>
   */ 
  public void 
  identity()
  {
    diagonal(1.f);
  }

  /**
   * Make this square matrix a diagonal matrix having the given value for all components 
   * along the diagonal of the matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixDiagonalScalar.gif">
   * </DIV>
   * 
   * @param s
   *   The value for all diagonal components.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/DiagonalMatrix.html">
   *      Math World: Diagonal Matrix</A>
   */
  public void
  diagonal
  (
   float s
  ) 
  {
    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot make a non-square matrix diagonal!");

    int i, j;									    
    for(j=0; j<rows(); j++) 
      for(i=0; i<cols(); i++)
	pCols[j].setComp(i, (i == j) ? s : 0.0f);
  }
				
  
  /**
   * Make this square matrix a diagonal matrix having the values of the components of the 
   * given vector as its diagonal. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixDiagonalTuple.gif">
   * </DIV>
   * 
   * @param v
   *   The diagonal vector.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @throws TupleSizeMismatchException
   *   If the size of the matrix is not identical to the size of the diagonal vector.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/DiagonalMatrix.html">
   *      Math World: Diagonal Matrix</A>
   */
  public void
  diagonal
  (
   TupleNf v
  ) 
  {
    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot make a non-square matrix diagonal!");

    if(v.size() != rows()) 
      throw new TupleSizeMismatchException(v.size(), rows());	

    int i, j;									    
    for(j=0; j<rows(); j++) 
      for(i=0; i<cols(); i++)
	pCols[j].setComp(i, (i == j) ? v.getComp(i) : 0.0f);
  }
		
  
											    
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Find the inverse of this square matrix. <P> 
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
   *   The epsilon used to determine singularity.
   * 
   * @param re [<B>modified</B>]
   *   If successful, the row eschelon form of this matrix is stored in this parameter.
   * 
   * @param rtn [<B>modified</B>]
   *   If successfull, the resulting inverse matrix is stored in this matrix.
   * 
   * @return 
   *   Whether this matrix has an inverse.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @throws MatrixSizeMismatchException
   *   If the row eschelon or results matrix are not the same size as this matrix.
   */
  public boolean
  inverse
  (
   float epsilon, 
   MatrixMNf re, 
   MatrixMNf rtn
  )
  {
    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot invert non-square matrices!");
      
    if(!isSameSize(re)) 
      throw new MatrixSizeMismatchException(rows(), cols(), re.rows(), re.cols());

    if(!isSameSize(rtn)) 
      throw new MatrixSizeMismatchException(rows(), cols(), rtn.rows(), rtn.cols());

    rtn.diagonal(1.0f);
    re.set(this);

    /* for each row/column down the diagonal of the matrix */ 
    int active;
    for(active=0; active<rows(); active++) {
      
      /* find largest pivot in active column at or below active row */ 
      int i, pi; 
      float pivot, pivotAbs, c;
      pivot = re.pCols[active].getComp(active);
      pivotAbs = Math.abs(pivot);
      
      for(i=active+1, pi=active; i<cols(); i++) {
	c = re.pCols[active].getComp(i);
	if(Math.abs(c) > pivotAbs) {
	  pi = i;
	  pivot = c; 
	  pivotAbs = Math.abs(pivot);
	}
      }
      
      /* if the pivot is zero (or nearly so), FAIL! */ 
      if(Math.abs(pivot) < epsilon)
	return false;
      
      /* swap the pivot row with the active row (if they are different) */ 
      if(pi > active) {
	re.rowOpI(pi, active);
	rtn.rowOpI(pi, active);
      }
      
      /* normalize the active row by multiplying it by 1/pivot */ 
      re.rowOpII(active, pivot);
      rtn.rowOpII(active, pivot);
      
      /* subtract the proper multiple of the active row from each of the other rows
	 so that they have zero's in the pivot column */ 
      for(i=0; i<cols(); i++) {
	if(i != active) {
	  float scale = -re.pCols[active].getComp(i);
	  if(scale != 0.0f) {
	    re.rowOpIII(i, active, scale);
	    rtn.rowOpIII(i, active, scale);
	  }
	}
      }
    }

    return true;
  }
  
  /**
   * Find the inverse of this square matrix. <P> 
   * 
   * The inverse of a matrix is defined as: <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixInverse.gif">
   * </DIV> <P> 
   * 
   * Where <CODE>I</CODE> is the identity matrix. This method computes the inverse of
   * the matrix using <A HREF="http://mathworld.wolfram.com/GaussianElimination.html">
   * Gaussian Elimination</A>. 
   * 
   * @param rtn [<B>modified</B>]
   *   If successfull, the resulting inverse matrix is stored in this matrix.
   * 
   * @return 
   *   Whether this matrix has an inverse.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @throws MatrixSizeMismatchException
   *   If results matrix is not the same size as this matrix.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   */
  public boolean
  inverse
  (
   float epsilon, 
   MatrixMNf rtn
  )
  {
    MatrixMNf re = new MatrixMNf(rows(), cols());
    return inverse(epsilon, re, rtn); 
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
    if((obj != null) && (obj instanceof MatrixMNf)) {
      MatrixMNf mx = (MatrixMNf) obj;
      if((rows() != mx.rows()) || (cols() != mx.cols()))
	return false;

      int j;
      for(j=0; j<cols(); j++) {
	if(!pCols[j].equals(mx.pCols[j])) 
	  return false;
      }
      return true;
    }
    return false;
  }

  /**
   * Generate a string representation of this tuple.
   */ 
  public String
  toString() 
  {
    StringBuilder buf = new StringBuilder();

    int i; 
    for(i=0; i<rows(); i++) {
      buf.append("[");
      int j;
      for(j=0; j<(cols()-1); j++) 
	buf.append(String.format("%1$+.4f", pCols[j].getComp(i)) + " ");
      buf.append(String.format("%1$+.4f", pCols[j].getComp(i)) + "]\n");
    }
	
    return buf.toString();
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
    encoder.encode("Cols", pCols);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    TupleNf[] cols = (TupleNf[]) decoder.decode("Cols"); 
    if(cols == null) 
      throw new GlueException("The \"Cols\" entry was missing!");
    pCols = cols;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3794263972349534350L;


		
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The column vectors of the matrix.
   */ 
  protected  TupleNf[]  pCols; 

}
