// $Id: CoordSysNf.java,v 1.1 2004/12/22 00:44:57 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   C O O R D   S Y S   N F                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A coordinate system of N dimensions. <P> 
 * 
 * Internally this is represented by an Nx(N+1)trix.  The first N columns of the matrix 
 * are the N basis vectors and the last column is origin of the coordinate system. <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../images/CoordSys.gif">
 * </DIV> 
 */
public 
class CoordSysNf
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
  CoordSysNf
  (
   int n
  ) 
  {
    pBasis = new TupleNf[n];

    int j;
    for(j=0; j<n; j++) 
      pBasis[j] = new TupleNf(n);

    pOrigin = new TupleNf(n);
  }

  /**
   * Copy constructor.
   * 
   * @param cs
   *   The coordinate system to copy.
   */ 
  public 
  CoordSysNf
  (
   CoordSysNf cs
  ) 
  {
    pBasis = new TupleNf[cs.dimens()];

    int j;
    for(j=0; j<pBasis.length; j++) 
      pBasis[j] = new TupleNf(cs.pBasis[j]);

    pOrigin = new TupleNf(cs.pOrigin);
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
   * Transform a point in THIS coordinate system to the world space (identity) coordinate 
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
   * @return 
   *   The point in the world coordinate system.
   */
  public TupleNf
  xformPoint
  (
   TupleNf p
  ) 
  {
    TupleNf rtn = new TupleNf(pOrigin);
    
    int j; 
    for(j=0; j<pBasis.length; j++) {
      TupleNf t = new TupleNf(pBasis[j]);
      t.mult(p.getComp(j));
      rtn.addTuple(t);
    }

    return rtn;
  }
   


  /*----------------------------------------------------------------------------------------*/
  /*   V E C T O R   O P S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Transform a vector in THIS coordinate system to the world space (identity) coordinate 
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
   * @return 
   *   The vector in the world coordinate system.
   */
  public TupleNf
  xformVector
  (
   TupleNf v
  ) 
  {
    TupleNf rtn = new TupleNf(dimens());
    
    int j; 
    for(j=0; j<pBasis.length; j++) {
      TupleNf t = new TupleNf(pBasis[j]);
      v.mult(v.getComp(j));
      rtn.addTuple(v);
    }

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
   CoordSysNf cs, 									    
   CoordSysNf rtn   
  ) 									    
  {
    if(dimens() != cs.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), cs.dimens()); 

    int j;
    for(j=0; j<dimens(); j++) 
      rtn.pBasis[j].set(xformVector(cs.pBasis[j]));

    rtn.pOrigin.set(xformPoint(cs.pOrigin));
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
   CoordSysNf cs 
  ) 									    
  {
    if(dimens() != cs.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), cs.dimens()); 

    CoordSysNf orig = new CoordSysNf(this);
    
    int j;
    for(j=0; j<dimens(); j++) 
      pBasis[j].set(orig.xformVector(cs.pBasis[j]));

    pOrigin = orig.xformPoint(cs.pOrigin);
  }

	
									    
  /*----------------------------------------------------------------------------------------*/
				
  /**
   * Make this coordinate system an identiy coordinate system. <P> 
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
    scale(1.0f);
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
   float s
  ) 
  {
    int i, j;
    for(j=0; j<dimens(); j++) {
      for(i=0; i<dimens(); i++) 
	pBasis[j].setComp(i, (i == j) ? s : 0.0f);
    }

    for(i=0; i<dimens(); i++) 
      pOrigin.setComp(i, 0.0f);
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
   TupleNf t
  ) 
  {
    int i, j;
    for(j=0; j<dimens(); j++) {
      for(i=0; i<dimens(); i++) 
	pBasis[j].setComp(i, (i == j) ? t.getComp(i) : 0.0f);
    }

    for(i=0; i<dimens(); i++) 
      pOrigin.setComp(i, 0.0f);
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
   TupleNf t
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
   float epsilon, 
   CoordSysNf rtn
  )
  {
    if(dimens() != rtn.dimens()) 
      throw new CoordSysDimensMismatchException(dimens(), rtn.dimens()); 

    rtn.identity(); 
    CoordSysNf re = new CoordSysNf(this);

    /* for each row/column down the diagonal of the matrix */ 
    int active;
    for(active=0; active<dimens(); active++) {
      
      /* find largest pivot in active column at or below active row */ 
      int i, pi; 
      float pivot, pivotAbs, c;
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
      re.rowOpII(active, 1.0f / pivot);
      rtn.rowOpII(active, 1.0f / pivot);
      
      /* subtract the proper multiple of the active row from each of the other rows
	 so that they have zero's in the pivot column */ 
      for(i=0; i<dimens(); i++) {
	if(i != active) {
	  float scale = -re.pBasis[active].getComp(i);
	  if(scale != 0.0f) {
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
      rtn.pOrigin.setComp(i, rtn.pOrigin.getComp(i) - pOrigin.getComp(i));	

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
	float v = pBasis[j].getComp(i1);
	pBasis[j].setComp(i1, pBasis[j].getComp(i2));
	pBasis[j].setComp(i1, v);
      }

      float v = pOrigin.getComp(i1);
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
   float s
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
   float s
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
    if((obj != null) && (obj instanceof CoordSysNf)) {
      CoordSysNf cs = (CoordSysNf) obj;
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
    StringBuffer buf = new StringBuffer();

    int i; 
    for(i=0; i<dimens(); i++) {
      buf.append("[");
      int j;
      for(j=0; j<dimens(); j++) 
	buf.append(String.format("%1$.4f", pBasis[j].getComp(i)) + " ");
      buf.append("| " + String.format("%1$.4f", pOrigin.getComp(i)) + "]\n");
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
    TupleNf[] basis = (TupleNf[]) decoder.decode("Basis"); 
    if(basis == null) 
      throw new GlueException("The \"Basis\" entry was missing!");
    pBasis = basis;

    TupleNf origin = (TupleNf) decoder.decode("Origin"); 
    if(origin == null) 
      throw new GlueException("The \"Origin\" entry was missing!");
    pOrigin = origin;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The basis vectors of the coordinate system.
   */ 
  protected TupleNf[]  pBasis; 

  /**
   * The origin of the coordinate system.
   */ 
  protected TupleNf  pOrigin; 

}
