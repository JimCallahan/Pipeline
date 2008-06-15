// $Id: ComparableRange.java,v 1.2 2008/06/15 01:59:49 jim Exp $

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
interface ComparableRange<T>
  extends Comparable<T>
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
   Comparable<T> value
  );

}

