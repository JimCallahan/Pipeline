// $Id: IncMethod.java,v 1.1 2004/03/01 21:45:04 jim Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   I N C   M E T H O D                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * The method to use to increment a revision number.
 * 
 * @see VersionID
 */
public
enum IncMethod
{  
  /**
   * Generate an initial version number (1.0.0.0);
   */
  Initial, 

  /**
   * Increment the fourth component of the previous revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.0.0.1 <BR>
   *    1.3.2.5 to 1.3.2.6 <BR>
   *    2.4.0.2 to 2.4.0.3 <BR>
   * </DIV>
   */
  Trivial, 


  /**
   * Increment the third component of the previous revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.0.1.0 <BR>
   *    1.3.2.5 to 1.3.3.0 <BR>
   *    2.4.0.2 to 2.4.1.0 <BR>
   * </DIV>
   */
  Minor,

  /**
   * Increment the second component of the previous revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 1.1.0.0 <BR>
   *    1.3.2.5 to 1.4.0.0 <BR>
   *    2.4.0.2 to 2.5.0.0 <BR>
   * </DIV>
   */
  Major,

  /**
   * Increment the first component of the previous revision number. <P> 
   * 
   * Examples include: 
   * <DIV style="margin-left: 40px;">
   *    1.0.0.0 to 2.0.0.0 <BR>
   *    1.3.2.5 to 2.0.0.0 <BR>
   *    2.4.0.2 to 3.0.0.0 <BR>
   * </DIV>
   */
  Massive;
}
