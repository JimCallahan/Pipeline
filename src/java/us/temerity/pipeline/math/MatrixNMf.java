// $Id: MatrixNMf.java,v 1.1 2004/12/19 19:27:39 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   N M F                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * An arbitrary sized NxM matrix containing float values. <P> 
 * 
 * An (NxM) matrix A (N rows, M columns) is indexed as follows: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/Matrix.gif">
 * </DIV> 
 */
public 
class MatrixNMf
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an (MxN) matrix setting all components to zero.
   * 
   * @param rows
   *   The number of rows (M).
   * 
   * @param cols
   *   The number of columns (N).
   */ 
  public 
  MatrixNMf
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
   * Construct from a two dimensional array: mx[col][row]
   * 
   * @param mx
   *   The initial component values. 
   */ 
  public 
  MatrixNMf
  (
   float[][] mx
  ) 
  {
    pCols = new TupleNf[mx.length];

    int rows = -1;
    int j;
    for(j=0; j<pCols.length; j++) {
      float[] col = mx[j];

      if(col == null) 
	throw new IllegalArgumentException
	  ("The column (" + j + ") of the given array was (null)!");

      if(col.length == 0) 
	throw new IllegalArgumentException
	  ("The column (" + j + ") of the given array was empty!");

      if(rows == -1) 
	rows = col.length;
      else if(rows != col.length) 
	throw new IllegalArgumentException
	  ("The column (" + j + ") had a different number of elements (" + col.length + ") " +
	   "than the other columns (" + rows + ")!");
      
      pCols[j] = new TupleNf(col);
    }
  }

  /**
   * Copy constructor.
   * 
   * @param mx
   *   The matrix to copy.
   */ 
  public 
  MatrixNMf
  (
   MatrixNMf mx
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
   * Get the number of rows.
   */ 
  public int 
  rows() 
  {
    return pCols[0].size();
  }

  /**
   * Get the number columns.
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
   * Whether this matris is square. 
   */
  public boolean
  isSquare() 
  {											    
    return (rows() == cols()); 
  }			

  /**
   * Wheter the given matrix is the same size as this matrix.
   */ 
  public boolean
  isSameSize
  (
   MatrixNMf mx
  ) 
  {
    return ((rows() == mx.rows()) && (cols() == mx.cols()));
  }

     
											    
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P O N E N T   A C C E S S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the ith row vector.
   * 
   * @param i
   *   The row index.
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
   * Set the ith row vector.
   * 
   * @param i
   *   The row index.
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
   TupleNf row
  ) 
  {
    if(row.size() != rows())
      throw new RowSizeMismatchException(row.size(), rows());

    try {
      int j;
      for(j=0; j<row.size(); j++)
	pCols[j].setComp(i, row.getComp(j));
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(i, 0);
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the jth column vector.
   * 
   * @param j
   *   The column index.
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
   * Set the jth column vector.
   * 
   * @param j
   *   The column index.
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
   TupleNf col
  ) 
  {
    try {
      pCols[j].set(col);
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(0, j);      
    }
    catch(TupleSizeMismatchException ex) {
      throw new ColumnSizeMismatchException(col.size(), cols());
    }    
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the component in the ith row and jth column.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the matrix index is not valid.
   */
  public float 
  getComp
  (
   int i, 
   int j
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
  
  /**
   * Get the component in the ith row and jth column using a tuple index.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the matrix index is not valid.
   */
  public float 
  getComp
  (
   Tuple2i ij
  ) 
  {	
    try {
      return (pCols[ij.getComp(0)].getComp(ij.getComp(1))); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ij.x(), ij.y());
    }
    catch(ArrayIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ij.x(), ij.y());
    }       
  }			
  
   
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the components from a two dimensional array: mx[col][row]
   *
   * @param mx
   *   The initial component values. 
   * 
   * @throws RowSizeMismatchException
   *   The the given array does not have the same nuber of rows as this matrix.
   * 
   * @throws ColumnSizeMismatchException
   *   The the given array does not have the same nuber of columns as this matrix.
   */ 
  public void 
  set
  (
   float[][] mx
  ) 
  {
    if(cols() != mx.length) 
      throw new ColumnSizeMismatchException(cols(), mx.length); 

    int j;
    for(j=0; j<cols(); j++) {
      float[] col = mx[j];

      if(col == null) 
	throw new IllegalArgumentException
	  ("The column (" + j + ") of the given array was (null)!");

      if(col.length == 0) 
	throw new IllegalArgumentException
	  ("The column (" + j + ") of the given array was empty!");

      if(pCols[j].size() != col.length) 
	throw new RowSizeMismatchException(pCols[j].size(), col.length); 
	
      pCols[j] = new TupleNf(col);
    }
  }

  /**
   * Returns an array containing a copy of the component values.
   */
  public float[][]
  toArray() 
  {
    float[][] cols = new float[cols()][];
    
    int j;
    for(j=0; j<cols(); j++) 
      cols[j] = pCols[j].toArray();

    return cols;
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
   MatrixNMf mx
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
   * Post-multiply a column vector by this matrix. <P> 
   * 
   * Multiplies an (NxM) matrix A by a (Mx1) vector B to produce a (Nx1) vector C: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixVectorMult.gif">
   * </DIV>
   * 
   * @throws ColumnSizeMismatchException
   *   If the number columns in this matrix is not the same as the number of components 
   *   in the given vector.
   * 
   * @return 
   *   The product of this matrix and the given vector.
   */ 					    
  public TupleNf 
  multTuple
  (											    
   TupleNf col
  ) 									    
  {		
    if(cols() != col.size()) 
      throw new ColumnSizeMismatchException(cols(), col.size());

    TupleNf rtn = new TupleNf(cols());
    
    int j; 
    for(j=0; j<cols(); j++) {
      TupleNf t = new TupleNf(pCols[j]);
      t.mult(col.getComp(j));
      rtn.addTuple(t);
    }

    return rtn;
  }
      
						    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   R O W   O P S                                                                        */
  /*----------------------------------------------------------------------------------------*/
											    
  /**
   * Exchange two rows of this matrix.
   * 
   * @param i1
   *   The index of the ith row to swap.
   * 
   * @param i2
   *   The index of the ith row to swap.
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
    try {
      int j; 
      for(j=0; j<cols(); j++) {
	float tmp = pCols[j].getComp(i1);
	pCols[j].setComp(i1, pCols[j].getComp(i2));
	pCols[j].setComp(i1, tmp);
      }
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
    											    
  /**
   * Multiply the components of a row of this matrix by a scalar.
   * 
   * @param i
   *   The index of the ith row to scale. 
   * 
   * @param scale
   *   The scaling factor.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row index is not valid.
   */ 						    
  public void
  rowOpII										    
  ( 											    
   int i, 
   float scale
  ) 											    
  {
    try {
      int j; 
      for(j=0; j<cols(); j++) 
	pCols[j].setComp(i, pCols[j].getComp(i) * scale);
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
											    
  /**
   * Sum a multiple of one row with another row of this matrix. 
   * 
   * @param i1
   *   The index of the ith row to modify.
   * 
   * @param i2
   *   The index of the ith row to scale and sum with the modified row.
   * 
   * @param scale
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
   float scale
  ) 											    
  {											    
    try {
      int j; 
      for(j=0; j<cols(); j++) 
	pCols[j].setComp(i1, pCols[j].getComp(i1) + (pCols[j].getComp(i2) * scale)); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
  											    
  											    
											    
  /*----------------------------------------------------------------------------------------*/
  /*   M A T R I X   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  											    
  /**
   * Multiply this matrix with the given matrix (on the right). <P> 
   * 
   * Multiplies an (NxM) matrix A by a (MxP) matrix B to produce a (NxP) matrix C: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixMatrixMult.gif">
   * </DIV>
   * 
   * @throws MatrixSizeMismatchException
   *   If the number columns in this matrix is not the same as the number of rows in the
   *   given matrix.
   */ 		    
  public MatrixNMf
  multMatrix										    
  (											    
   MatrixNMf mx
  ) 									    
  {	
    if(rows() != mx.cols()) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx.rows(), mx.cols());

    MatrixNMf rtn = new MatrixNMf(cols(), mx.rows());

    int j;
    for(j=0; j<mx.cols(); j++) 
      rtn.setCol(j, multTuple(mx.getCol(j)));
    
    return mx;
  }

  /**
   * Concatenate (multiply in place) this matrix with the given matrix (on the right). <P> 
   * 
   * Multiplies an (NxM) matrix A by a (MxP) matrix B to produce a (NxP) matrix C, where 
   * N = M = P: <BR>
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixMatrixMult.gif">
   * </DIV>
   * 
   * @throws MatrixSizeMismatchException
   *   If the size the given matrix is not identical to this matrix.
   * 
   * @throws UnsupportedOperationException 
   *   If either of the matrices are not square.
   */ 	    
  public void
  concatMatrix									    
  (											    
   MatrixNMf mx
  ) 									    
  {	
    if(!isSameSize(mx)) 
      throw new MatrixSizeMismatchException(rows(), cols(), mx.rows(), mx.cols());

    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot concatenate non-square matrices in place!");

    MatrixNMf orig = new MatrixNMf(this);

    int j;
    for(j=0; j<mx.cols(); j++) 
      setCol(j, orig.multTuple(mx.getCol(j)));
  }
	
										    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Find the transpose of this matrix. <P> 
   * 
   * Generates a 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixTranspose.gif">
   * </DIV>
   * 
   */
  public MatrixNMf
  transpose() 
  {
    MatrixNMf rtn = new MatrixNMf(rows(), cols());
    										    
    int i, j;									    
    for(j=0; j<rows(); j++) 
      for(i=0; i<cols(); i++) 								    
	rtn.pCols[i].setComp(j, pCols[j].getComp(i));

    return rtn;
  }
	
										    
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this matrix an identity matrix. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/MatrixIdentity.gif">
   * </DIV>
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   */
  public void
  identity() 
  {
    int i, j;									    
    for(j=0; j<rows(); j++) 
      for(i=0; i<cols(); i++)
	pCols[j].setComp(i, (i == j) ? 1.0f : 0.0f);
  }
											    
											    
  /*----------------------------------------------------------------------------------------*/
	
  /**
   * Find the inverse of this matrix. 
   * 
   * @param re
   *   If successful, the row eschelon form of this matrix is stored in this parameter.
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @return 
   *   The inverse of this matrix or <CODE>null</CODE> if the matrix is singular.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   * 
   * @throws MatrixSizeMismatchException
   *   If the row eschelon matrix is not the same size as this matrix.
   */
  public MatrixNMf
  inverse
  (
   MatrixNMf re, 
   float epsilon
  )
  {
    if(!isSquare()) 
      throw new UnsupportedOperationException
	("Cannot invert non-square matrices!");
      
    MatrixNMf inv = new MatrixNMf(rows(), cols());
    inv.identity();

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
	return null;
      
      /* swap the pivot row with the active row (if they are different) */ 
      if(pi > active) {
	re.rowOpI(pi, active);
	inv.rowOpI(pi, active);
      }
      
      /* normalize the active row by multiplying it by 1/pivot */ 
      re.rowOpII(active, 1.0f / pivot);
      inv.rowOpII(active, 1.0f / pivot);
      
      /* subtract the proper multiple of the active row from each of the other rows
	 so that they have zero's in the pivot column */ 
      for(i=0; i<cols(); i++) {
	if(i != active) {
	  float scale = -re.pCols[active].getComp(i);
	  if(scale != 0.0f) {
	    re.rowOpIII(i, active, scale);
	    inv.rowOpIII(i, active, scale);
	  }
	}
      }
    }

    return inv;
  }
  
  /**
   * Find the inverse of this matrix.
   * 
   * @param epsilon
   *   The epsilon used in determining singularity.
   * 
   * @return 
   *   The inverse of this matrix or <CODE>null</CODE> if the matrix is singular.
   * 
   * @throws UnsupportedOperationException 
   *   If this matrix is not square.
   */
  public MatrixNMf
  inverse
  (
   float epsilon
  )
  {
    MatrixNMf re = new MatrixNMf(rows(), cols());
    return inverse(re, epsilon);
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

  //private static final long serialVersionUID = 


		
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The column vectors of the matrix.
   */ 
  protected  TupleNf[]  pCols; 

}
