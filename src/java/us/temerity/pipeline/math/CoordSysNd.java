// DO NOT EDIT! -- Automatically generated by: permutations.bash

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O O R D   S Y S   N D                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A coordinate system of N dimensions containing double values. <P> 
 * 
 * Internally this is represented by an Nx(N+1) matrix.  The first N columns of the matrix 
 * are the N basis vectors and the last column is origin of the coordinate system. <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/CoordSys.gif">
 * </DIV> 
 */
public 
class CoordSysNd
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an uninitialized N dimensional coordinate system. 
   * 
   * @param n
   *   The number of dimensions.
   */ 
  public 
  CoordSysNd
  (
   int n
  ) 
  {
    pBasis = new TupleNd[n];

    int j;
    for(j=0; j<n; j++) 
      pBasis[j] = new TupleNd(n);

    pOrigin = new TupleNd(n);
  }

  /**
   * Copy constructor.
   * 
   * @param cs
   *   The coordinate system to copy.
   */ 
  public 
  CoordSysNd
  (
   CoordSysNd cs
  ) 
  {
    pBasis = new TupleNd[cs.dimens()];

    int j;
    for(j=0; j<pBasis.length; j++) 
      pBasis[j] = new TupleNd(cs.pBasis[j]);

    pOrigin = new TupleNd(cs.pOrigin);
  }


  											    
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the number of dimensions in this coordinate system.
   */ 
  public int
  dimens()
  {
    return pBasis.length;
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
   *   <IMG alt="" src="../../../../images/CoordSysXformPoint.gif">
   * </DIV>
   * 
   * @param p
   *   The point in this coordinate system.
   * 
   * @param rtn
   *   The point in the world coordinate system is stored in this parameter.
   */
  protected void 
  xformPoint
  (
   TupleNd p, 
   TupleNd rtn
  ) 
  {
    rtn.set(pOrigin);
    
    int j; 
    for(j=0; j<pBasis.length; j++) {
      TupleNd t = new TupleNd(pBasis[j]);
      t.mult(p.getComp(j));
      rtn.addTuple(t);
    }
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
   *   <IMG alt="" src="../../../../images/CoordSysXformVector.gif">
   * </DIV>
   * 
   * @param v
   *   The vector in this coordinate system.
   * 
   * @param rtn 
   *   The vector in the world coordinate system is stored in this parameter.
   */
  protected void
  xformVector
  (
   TupleNd v,
   TupleNd rtn
  ) 
  {
    rtn.zero();
    
    int j; 
    for(j=0; j<pBasis.length; j++) {
      TupleNd t = new TupleNd(pBasis[j]);
      t.mult(v.getComp(j));
      rtn.addTuple(t);
    }
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
   *   <IMG alt="" src="../../../../images/CoordSysMultCoordSys.gif">
   * </DIV>
   * 
   * @param cs
   *   The coordinate system multiply this coordinate system.
   * 
   * @param rtn
   *   The product is stored in this coordinate system.
   * 
   * @throws CoordSysDimensMismatchException
   *   If the sizes of the coordinate systems do not have the same number of dimensions.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public void 
  multCoordSys							    
  (											    
   CoordSysNd cs, 									    
   CoordSysNd rtn   
  ) 									    
  {
    if(dimens() != cs.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), cs.dimens()); 

    TupleNd t = new TupleNd(dimens());

    int j;
    for(j=0; j<dimens(); j++)  {
      xformVector(cs.pBasis[j], t);
      rtn.pBasis[j].set(t);
    }

    xformPoint(cs.pOrigin, t);
    rtn.pOrigin.set(t);
  }

  											    
  /**
   * Concatenate the given coordinate system with this coordinate system (in place). <P> 
   * 
   * In other words, multiply (in place) the matrix representation of this coordinate system 
   * with the matrix representation of the given coordinate system (on the right). <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSysMultCoordSys.gif">
   * </DIV>
   * 
   * @param cs
   *   The coordinate system multiply this coordinate system.
   * 
   * @throws CoordSysDimensMismatchException
   *   If the sizes of the coordinate systems do not have the same number of dimensions.
   * 
   * @see <A HREF="http://mathworld.wolfram.com/MatrixMultiplication.html">
   *      Math World: Matrix Multiplication</A>
   */ 		    
  public void 
  concatCoordSys								    
  (											    
   CoordSysNd cs 
  ) 									    
  {
    if(dimens() != cs.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), cs.dimens()); 

    CoordSysNd orig = new CoordSysNd(this);
    TupleNd t = new TupleNd(dimens());
    
    int j;
    for(j=0; j<dimens(); j++) {
      orig.xformVector(cs.pBasis[j], t);
      pBasis[j].set(t);
    }

    orig.xformPoint(cs.pOrigin, t);
    pOrigin.set(t);
  }

	
									    
  /*----------------------------------------------------------------------------------------*/
				
  /**
   * Make this coordinate system a world space (identity) coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSysIdentity.gif">
   * </DIV>
   * 
   * @see <A HREF="http://mathworld.wolfram.com/IdentityMatrix.html">
   *      Math World: Identity Matrix</A>
   */ 
  public void 
  identity()
  {
    scale(1.0);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Make this coordinate system a uniform scaling coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSysUScale.gif">
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
    int i, j;
    for(j=0; j<dimens(); j++) {
      for(i=0; i<dimens(); i++) 
	pBasis[j].setComp(i, (i == j) ? s : 0.0);
    }

    for(i=0; i<dimens(); i++) 
      pOrigin.setComp(i, 0.0);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this coordinate system a non-uniform scaling coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSysNScale.gif">
   * </DIV>
   * 
   * @param t
   *   The non-uniform scale vector.
   */ 
  protected void 
  scale
  (
   TupleNd t
  ) 
  {
    int i, j;
    for(j=0; j<dimens(); j++) {
      for(i=0; i<dimens(); i++) 
	pBasis[j].setComp(i, (i == j) ? t.getComp(i) : 0.0);
    }

    for(i=0; i<dimens(); i++) 
      pOrigin.setComp(i, 0.0);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Make this coordinate system a translation coordinate system. <P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   <IMG alt="" src="../../../../images/CoordSysTranslate.gif">
   * </DIV>
   * 
   * @param t
   *   The translation vector or origin point.
   */ 
  protected void 
  translate
  (
   TupleNd t
  ) 
  {
    identity();
    pOrigin.set(t);
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
   * @param rtn [<B>modified</B>]
   *   If successfull, the resulting inverse coordinate system is stored in this argument.
   * 
   * @return 
   *   Whether this matrix has an inverse.
   * 
   * @throws CoordSysDimensMismatchException
   *   If the row eschelon or results matrix are not the same size as this matrix.
   */
  protected boolean
  inverse
  (
   double epsilon, 
   CoordSysNd rtn
  )
  {
    if(dimens() != rtn.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), rtn.dimens()); 

    rtn.identity(); 
    CoordSysNd re = new CoordSysNd(this);

    /* for each row/column down the diagonal of the matrix */ 
    int active;
    for(active=0; active<dimens(); active++) {
      
      /* find largest pivot in active column at or below active row */ 
      int i, pi; 
      double pivot, pivotAbs, c;
      pivot = re.pBasis[active].getComp(active);
      pivotAbs = Math.abs(pivot);
      
      for(i=active+1, pi=active; i<dimens(); i++) {
	c = re.pBasis[active].getComp(i);
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
      re.rowOpII(active, 1.0 / pivot);
      rtn.rowOpII(active, 1.0 / pivot);
      
      /* subtract the proper multiple of the active row from each of the other rows
	 so that they have zero's in the pivot column */ 
      for(i=0; i<dimens(); i++) {
	if(i != active) {
	  double scale = -re.pBasis[active].getComp(i);
	  if(scale != 0.0) {
	    re.rowOpIII(i, active, scale);
	    rtn.rowOpIII(i, active, scale);
	  }
	}
      }
    }
									  
    /* handle the last (virtual) row:							  
         rowOpI can be skipped since this is the last row.  
         rowOpII can be skipped since the pivot is 1 by definition.			  
         rowOpIII can be simplified since the last row is all zeros except 		  
           for the last column which is one -- this means that only the last 		  
           column of the other rows need to be altered. */ 				  
    int i; 
    for(i=0; i<dimens(); i++) 
      rtn.pOrigin.setComp(i, rtn.pOrigin.getComp(i) - re.pOrigin.getComp(i));

    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R O W   O P S                                                                        */
  /*----------------------------------------------------------------------------------------*/
											    
  /**
   * Exchange two rows of this coordinate system
   * 
   * @param i1
   *   The index of the first row: [0, n-1]
   * 
   * @param i2
   *   The index of the second row: [0, n-1]
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row indices are not valid.
   */ 
  private void 
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
      for(j=0; j<pBasis.length; j++) {
	double v = pBasis[j].getComp(i1);
	pBasis[j].setComp(i1, pBasis[j].getComp(i2));
	pBasis[j].setComp(i2, v);
      }

      double v = pOrigin.getComp(i1);
      pOrigin.setComp(i1, pOrigin.getComp(i2));
      pOrigin.setComp(i2, v); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
  }
    											    
  /**
   * Scale the values of a row of this coordinate system by multiplying it by the 
   * given scalar.
   * 
   * @param i
   *   The index of the row to scale: [0, n-1]
   * 
   * @param s
   *   The scaling factor.
   * 
   * @throws MatrixIndexOutOfBoundsException
   *   If the row index is not valid.
   */ 						    
  private void
  rowOpII										    
  ( 											    
   int i, 
   double s
  ) 											    
  {
    try {
      int j; 
      for(j=0; j<dimens(); j++) 
	pBasis[j].setComp(i, pBasis[j].getComp(i) * s);

      pOrigin.setComp(i, pOrigin.getComp(i) * s);
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
  private void
  rowOpIII
  ( 											    
   int i1,
   int i2, 
   double s
  ) 											    
  {											    
    try {
      int j; 
      for(j=0; j<dimens(); j++) 
	pBasis[j].setComp(i1, pBasis[j].getComp(i1) + (pBasis[j].getComp(i2) * s)); 
      
      pOrigin.setComp(i1, pOrigin.getComp(i1) + (pOrigin.getComp(i2) * s)); 
    }
    catch(TupleIndexOutOfBoundsException ex) {
      throw new MatrixIndexOutOfBoundsException(ex);
    }
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
    if((obj != null) && (obj instanceof CoordSysNd)) {
      CoordSysNd cs = (CoordSysNd) obj;
      if(dimens() != cs.dimens())
	return false;

      int j;
      for(j=0; j<dimens(); j++) {
	if(!pBasis[j].equals(cs.pBasis[j])) 
	  return false;
      }
      return pOrigin.equals(cs.pOrigin);
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
    for(i=0; i<dimens(); i++) {
      buf.append("[");
      int j;
      for(j=0; j<dimens(); j++) 
	buf.append(String.format("%1$+.6f", pBasis[j].getComp(i)) + " ");
      buf.append("| " + String.format("%1$+.6f", pOrigin.getComp(i)) + "]\n");
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
    encoder.encode("Basis", pBasis);
    encoder.encode("Origin", pOrigin);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    TupleNd[] basis = (TupleNd[]) decoder.decode("Basis"); 
    if(basis == null) 
      throw new GlueException("The \"Basis\" entry was missing!");
    pBasis = basis;

    TupleNd origin = (TupleNd) decoder.decode("Origin"); 
    if(origin == null) 
      throw new GlueException("The \"Origin\" entry was missing!");
    pOrigin = origin;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4120832248146896026L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The basis vectors of the coordinate system.
   */ 
  protected TupleNd[]  pBasis; 

  /**
   * The origin of the coordinate system.
   */ 
  protected TupleNd  pOrigin; 

}
