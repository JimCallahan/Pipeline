// $Id: Range.java,v 1.1 2007/04/05 08:37:49 jim Exp $

package us.temerity.pipeline.math;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   R A N G E                                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A range of values which may be either open or closed, inclusive or exclusive.
 */
public 
class Range<T>
  implements Glueable, Serializable  
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct an inclusive range of values. 
   * 
   * @param lower
   *   The lower bounds or <CODE>null</CODE> for no lower bounds.
   * 
   * @param upper
   *   The upper bounds or <CODE>null</CODE> for no lower bounds.
   */ 
  public 
  Range
  (
   Comparable<T> lower, 
   Comparable<T> upper
  ) 
  {
    this(lower, upper, true, true);
  }

  /**
   * Construct an range of values. 
   * 
   * @param lower
   *   The lower bounds or <CODE>null</CODE> for no lower bounds.
   * 
   * @param upper
   *   The upper bounds or <CODE>null</CODE> for no lower bounds.
   * 
   * @param inc
   *   Whether both upper and lower bounds are inclusive (true) or exclusive (false).
   */ 
  public 
  Range
  (
   Comparable<T> lower, 
   Comparable<T> upper,
   boolean inc
  ) 
  {
    this(lower, upper, inc, inc);
  }

  /**
   * Construct an range from lower to upper values.
   * 
   * @param lower
   *   The lower bounds or <CODE>null</CODE> for no lower bounds.
   * 
   * @param upper
   *   The upper bounds or <CODE>null</CODE> for no lower bounds.
   * 
   * @param incLower
   *   Whether the lower bounds is inclusive (true) or exclusive (false).
   * 
   * @param incUpper
   *   Whether the upper bounds is inclusive (true) or exclusive (false).
   */ 
  public 
  Range
  (
   Comparable<T> lower, 
   Comparable<T> upper, 
   boolean incLower, 
   boolean incUpper   
  ) 
  {
    pLower = lower; 
    pUpper = upper; 

    pIncLower = incLower;
    pIncUpper = incUpper; 
  }

		
									 
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  									 
  /**
   * Whether the range has a lower bounds. 
   */ 					 
  public boolean
  hasLower() 
  {
    return (pLower != null);
  }
											 
  /**
   * Whether the range has a upper bounds. 
   */ 					 
  public boolean
  hasUpper() 
  {
    return (pUpper != null);
  }
											 
  /**
   * Whether the range has an inclusive lower bounds. 
   */ 					 
  public boolean
  isLowerInclusive() 
  {
    return (hasLower() && pIncLower); 
  }		
									 
  /**
   * Whether the range has an inclusive upper bounds. 
   */ 					 
  public boolean
  isUpperInclusive() 
  {
    return (hasUpper() && pIncUpper); 
  }
											 
  /**
   * Whether the given value is inside the range. 
   */ 					 
  public boolean
  isInside										 
  ( 											 
   T value
  ) 
  {
    Integer result = rangeCompare(value);
    return ((result != null) && (result == 0));
  }									 
  		

									 
  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/
 											 
  /**
   * Compare the given value with the range. <P> 
   * 
   * The value returned is as follows: <BR>
   * <DIV style="margin-left: 40px;">
   *   (null) - If the value is null.
   *   (-1) - If the value is less-than the lower bounds (if one exists).
   *   (0) - If the value is inside the range.
   *   (1) - If the value is greater-than the upper bounds (if one exists).
   */ 					 
  public Integer
  rangeCompare									 
  ( 											 
   T value
  ) 
  {
    if(value == null) 
      return null;

    if(pLower != null) {
      if((pIncLower && (pLower.compareTo(value) > 0)) ||
         (!pIncLower && (pLower.compareTo(value) >= 0))) 
        return -1;
    }

    if(pUpper != null) {
      if((pIncUpper && (pUpper.compareTo(value) < 0)) ||
         (!pIncUpper && (pUpper.compareTo(value) <= 0))) 
        return -1;
    }

    return 0;
  }	



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the lower bounds or <CODE>null</CODE> if none exists.
   */ 
  public Comparable<T>
  getLower() 
  {
    return pLower; 
  }

  /**
   * Get the upper bounds or <CODE>null</CODE> if none exists.
   */ 
  public Comparable<T>
  getUpper() 
  {
    return pUpper; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Generate a string representation of this range.
   */ 
  public String
  toString() 
  {
    String lower = "[-inf";
    if(hasLower()) 
      lower = (isLowerInclusive() ? "[" : "(") + pLower;

    String upper = "inf]";
    if(hasUpper()) 
      upper = pUpper + (isUpperInclusive() ? "]" : ")");

    return (lower + ", " + upper);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    if(pLower != null) {
      encoder.encode("Lower", pLower);
      encoder.encode("IncLower", pIncLower);
    }

    if(pUpper != null) {
      encoder.encode("Upper", pUpper);
      encoder.encode("IncUpper", pIncUpper);
    }
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    pLower = (Comparable<T>) decoder.decode("Lower"); 
    if(pLower != null) 
      pIncLower = (Boolean) decoder.decode("IncLower"); 

    pUpper = (Comparable<T>) decoder.decode("Upper"); 
    if(pUpper != null) 
      pIncUpper = (Boolean) decoder.decode("IncUpper"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8783747900751620513L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The lower/upper bounds of the range.
   */ 
  private Comparable<T>  pLower; 
  private Comparable<T>  pUpper; 
    	
  /**
   * Whether the lower/upper bounds of the range are inclusive (true) or exclusive (false).
   */ 
  private boolean pIncLower; 
  private boolean pIncUpper; 
    		
}
