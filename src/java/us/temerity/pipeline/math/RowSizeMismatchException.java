// $Id: RowSizeMismatchException.java,v 1.1 2004/12/19 19:27:50 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   R O W   S I Z E   M I S M A T C H   E X C E P T I O N                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that a matrix operation was attempted between row vectors of 
 * different sizes.
 */
public
class RowSizeMismatchException
  extends RuntimeException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an RowSizeMismatchException with no detail message.
   */ 
  public 
  RowSizeMismatchException()
  {}

  /**
   * Constructs a new RowSizeMismatchException class with an argument indicating the 
   * sizes of the mismatches rows.
   * 
   * @param sizeA
   *   The size of the first row.
   * 
   * @param sizeB
   *   The size of the second row.
   */ 
  public 
  RowSizeMismatchException
  (
   int sizeA,
   int sizeB
  )
  {
    super("Size mismatch between the first row (" + sizeA + ") and second row " + 
	  "(" + sizeB + ")!"); 
  }
    
  /**
   * Constructs an RowSizeMismatchException class with the specified detail message.
   */
  public
  RowSizeMismatchException
  (
   String msg
  )
  {
    super(msg);
  }
          


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 9180348961413119818L;

}
  
