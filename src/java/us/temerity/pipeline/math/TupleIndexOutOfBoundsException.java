// $Id: TupleIndexOutOfBoundsException.java,v 1.2 2004/12/17 21:23:55 jim Exp $

package us.temerity.pipeline.math;

/*------------------------------------------------------------------------------------------*/
/*   T U P L E   I N D E X   O U T   O F   B O U N D S   E X C E P T I O N                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Thrown to indicate that a tuple has been accessed with an illegal index. <P> 
 * 
 * The index is either negative or greater than or equal to the size of the tuple.
 */
public
class TupleIndexOutOfBoundsException
  extends IndexOutOfBoundsException
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Constructs an TupleIndexOutOfBoundsException with no detail message.
   */ 
  public 
  TupleIndexOutOfBoundsException()
  {}

  /**
   * Constructs a new TupleIndexOutOfBoundsException class with an argument indicating the 
   * illegal index.
   */ 
  public 
  TupleIndexOutOfBoundsException
  (
   int idx
  )
  {
    super("The tuple index (" + idx + ") was out-of-bounds.");
  }
    
  /**
   * Constructs an TupleIndexOutOfBoundsException class with the specified detail message.
   */
  public
  TupleIndexOutOfBoundsException
  (
   String msg
  )
  {
    super(msg);
  }
          

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2476265770478464111L;

}
  
