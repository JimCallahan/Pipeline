// $Id: MatrixSizeMismatchException.java,v 1.3 2004/12/22 00:45:28 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   S I Z E   M I S M A T C H   E X C E P T I O N                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that a matrix operation was attempted between matrices of 
 * different sizes.
 */
public
class MatrixSizeMismatchException
  extends RuntimeException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an MatrixSizeMismatchException with no detail message.
   */ 
  public 
  MatrixSizeMismatchException()
  {}

  /**
   * Constructs a new MatrixSizeMismatchException class with an argument indicating the 
   * sizes of the mismatched matrices.
   * 
   * @param rows1
   *   The number of rows in the first matrix.
   * 
   * @param cols1
   *   The number of columns in the first matrix.
   * 
   * @param rows2
   *   The number of rows in the first matrix.
   * 
   * @param cols2
   *   The number of columns in the first matrix.
   */ 
  public 
  MatrixSizeMismatchException
  (
   int rows1, 
   int cols1, 
   int rows2, 
   int cols2
  )
  {
    super("Size mismatch between the first " + rows1 + "x" + cols1 + " matrix and " + 
	  "second " + rows2 + "x" + cols2 + " matrix!");
  }
    
  /**
   * Constructs an MatrixSizeMismatchException class with the specified detail message.
   */
  public
  MatrixSizeMismatchException
  (
   String msg
  )
  {
    super(msg);
  }
          


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -49533191020604700L;

}
  
