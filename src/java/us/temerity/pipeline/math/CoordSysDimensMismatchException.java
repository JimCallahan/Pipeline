// $Id: CoordSysDimensMismatchException.java,v 1.1 2004/12/22 00:44:57 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   C O O R D   S Y S   D I M E N S   M I S M A T C H   E X C E P T I O N                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that a coordinate system operation was attempted between coordinate 
 * systems of different dimensionality. 
 */
public   
class CoordSysDimensMismatchException
  extends RuntimeException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an CoordSysDimensMismatchException with no detail message.
   */ 
  public 
  CoordSysDimensMismatchException()
  {}

  /**
   * Constructs a new CoordSysDimensMismatchException class with an argument indicating the 
   * dimensions of the mismatched coordinate systems.
   * 
   * @param dimens1
   *   The number of dimensions in the first coordinate system.
   * 
   * @param dimens2
   *   The number of dimensions in the second coordinate system.
   */ 
  public 
  CoordSysDimensMismatchException
  (
   int dimens1, 
   int dimens2
  )
  {
    super("Dimension mismatch between the first " + dimens1 + "D coordinate system and " + 
	  "second " + dimens2 + "D coordinate system!"); 
  }
    
  /**
   * Constructs an CoordSysDimensMismatchException class with the specified detail message.
   */
  public
  CoordSysDimensMismatchException
  (
   String msg
  )
  {
    super(msg);
  }
          


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

}
  
