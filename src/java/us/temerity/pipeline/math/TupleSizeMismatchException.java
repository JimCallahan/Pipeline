// $Id: TupleSizeMismatchException.java,v 1.2 2004/12/17 21:23:55 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   S I Z E   M I S M A T C H   E X C E P T I O N                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that an operation was attempted between tuples of different sizes.
 */
public
class TupleSizeMismatchException
  extends RuntimeException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an TupleSizeMismatchException with no detail message.
   */ 
  public 
  TupleSizeMismatchException()
  {}

  /**
   * Constructs a new TupleSizeMismatchException class with an argument indicating the 
   * sizes of the mismatches tuples.
   * 
   * @param sizeA
   *   The size of the first tuple.
   * 
   * @param sizeB
   *   The size of the second tuple/array.
   */ 
  public 
  TupleSizeMismatchException
  (
   int sizeA,
   int sizeB
  )
  {
    super("Size mismatch between the first tuple (" + sizeS + ") and second tuple " + 
	  "(" + sizeB + ")!"); 
  }
    
  /**
   * Constructs an TupleSizeMismatchException class with the specified detail message.
   */
  public
  TupleSizeMismatchException
  (
   String msg
  )
  {
    super(msg);
  }
          


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8706906243542436324L;

}
  
