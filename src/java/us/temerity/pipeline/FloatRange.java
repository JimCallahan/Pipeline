// $Id: FloatRange.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   F L O A T   R A N G E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A range of floating point values. 
 */
public
class FloatRange
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
  FloatRange()
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
  FloatRange
  (
   Float minValue, 
   Float maxValue
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
    return String.format("%1$.1f", (Float) value); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8390595780141557602L;

}

