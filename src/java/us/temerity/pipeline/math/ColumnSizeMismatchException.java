// $Id: ColumnSizeMismatchException.java,v 1.1 2004/12/19 19:27:50 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   C O L U M N   S I Z E   M I S M A T C H   E X C E P T I O N                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Thcolumnn to indicate that a matrix operation was attempted between column vectors of 
 * different sizes.
 */
public
class ColumnSizeMismatchException
  extends RuntimeException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an ColumnSizeMismatchException with no detail message.
   */ 
  public 
  ColumnSizeMismatchException()
  {}

  /**
   * Constructs a new ColumnSizeMismatchException class with an argument indicating the 
   * sizes of the mismatches columns.
   * 
   * @param sizeA
   *   The size of the first column.
   * 
   * @param sizeB
   *   The size of the second column.
   */ 
  public 
  ColumnSizeMismatchException
  (
   int sizeA,
   int sizeB
  )
  {
    super("Size mismatch between the first column (" + sizeA + ") and second column " + 
	  "(" + sizeB + ")!"); 
  }
    
  /**
   * Constructs an ColumnSizeMismatchException class with the specified detail message.
   */
  public
  ColumnSizeMismatchException
  (
   String msg
  )
  {
    super(msg);
  }
          


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5872778529933907819L;

}
  
