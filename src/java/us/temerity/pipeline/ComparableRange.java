// $Id: ComparableRange.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M P A R A B L E   R A N G E                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A ranges of comparable values. 
 */
public
interface ComparableRange 
  extends Comparable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether the given value is inside the range.
   */
  public boolean 
  isInsideRange
  (
   Comparable value
  );

}

