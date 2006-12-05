// $Id: ByteSizeRange.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B Y T E   S I Z E   R A N G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A range of long byte size values. 
 */
public
class ByteSizeRange
  extends HistogramRange
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public 
  ByteSizeRange()
  {}

  /**
   * Construct a new range of floating point values. <P> 
   * 
   * @param minValue
   *   The minimum inclusive value of the range or <CODE>null<CODE> if open ended.
   * 
   * @param maxValue
   *   The maximum exclusive value of the range or <CODE>null<CODE> if open ended.
   */
  public
  ByteSizeRange
  (
   Long minValue, 
   Long maxValue
  ) 
  {
    super(minValue, maxValue); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I / O                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Format the value as a string.
   */ 
  public String
  formatValue
  (
   Comparable value
  ) 
  {
    return ByteSize.longToString((Long) value); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3609477994509225449L;

}

