// $Id: MatrixIndexOutOfBoundsException.java,v 1.1 2004/12/19 19:27:39 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   M A T R I X   I N D E X   O U T   O F   B O U N D S   E X C E P T I O N                */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that a matrix has been accessed with an illegal index. <P> 
 * 
 * The index is either negative or greater than or equal to the number of rows or columns
 * of the matrix.
 */
public
class MatrixIndexOutOfBoundsException
  extends IndexOutOfBoundsException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an MatrixIndexOutOfBoundsException with no detail message.
   */ 
  public 
  MatrixIndexOutOfBoundsException()
  {}

  /**
   * Constructs a new MatrixIndexOutOfBoundsException class with an argument indicating the 
   * illegal indices.
   */ 
  public 
  MatrixIndexOutOfBoundsException
  (
   int i, 
   int j
  )
  {
    super("The matrix index (" + i + ", " + j + ") was out-of-bounds.");
  }
    
  /**
   * Constructs a new MatrixIndexOutOfBoundsException class from a thrown 
   * {@link TupleIndexOutOfBoundsException TupleIndexOutOfBoundsException} for an illegal row 
   * index access.
   */ 
  public 
  MatrixIndexOutOfBoundsException
  (
   TupleIndexOutOfBoundsException ex
  )
  {
    super("The matrix row index (" + ex.getIllegalIndex() + ") was out-of-bounds.");
  }
  
  /**
   * Constructs a new MatrixIndexOutOfBoundsException class from a thrown 
   * {@link ArrayIndexOutOfBoundsException ArrayIndexOutOfBoundsException} for an illegal 
   * column index access.
   */ 
  public 
  MatrixIndexOutOfBoundsException
  (
   ArrayIndexOutOfBoundsException ex
  )
  {
    super("The matrix column index was out-of-bounds: " + ex.getMessage());
  }

  /**
   * Constructs an MatrixIndexOutOfBoundsException class with the specified detail message.
   */
  public
  MatrixIndexOutOfBoundsException
  (
   String msg
  )
  {
    super(msg);
  }
          

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1000025539610411369L;

}
  
