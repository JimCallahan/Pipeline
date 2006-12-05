// $Id: HistogramRange.java,v 1.1 2006/12/05 18:23:30 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H I S T O G R A M   R A N G E                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The common base class for all ranges of values used to define the bounds of a catagory 
 * by instances of the Histogram class.
 * 
 * The range is defined as all values greater-than or equal to the minimum value and 
 * less-than the maximum value.  The range may be open ended (has no boundry) on either
 * minimum, maximum or both ends of the range.
 */
public
class HistogramRange
  implements ComparableRange, Glueable, Serializable
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
  HistogramRange()
  {}

  /**
   * Construct a new range which contains only a single value. <P> 
   * 
   * @param value
   *   The sole value considered inside the range. 
   */
  public
  HistogramRange
  (
   Comparable value
  ) 
  {
    if(value == null) 
      throw new IllegalArgumentException
	("A single valued range's sole value cannot be (null)!");

    pSingleValue = value;
  }
  
  /**
   * Construct a new range which spans multiple values. <P> 
   * 
   * @param minValue
   *   The minimum inclusive value of the range or <CODE>null<CODE> if open ended.
   * 
   * @param maxValue
   *   The maximum exclusive value of the range or <CODE>null<CODE> if open ended.
   */
  public
  HistogramRange
  (
   Comparable minValue, 
   Comparable maxValue
  ) 
  {
    if((minValue != null) && (maxValue != null) && 
       (minValue.compareTo(maxValue) >= 0))
      throw new IllegalArgumentException
	("The minimum value (" + minValue + ") must be less-than the " + 
	 "maximum (" + maxValue + ") value of the range!");

    pMinValue = minValue; 
    pMaxValue = maxValue; 
  }

  
   
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
  )
  {
    if(isSingleValued()) 
      return pSingleValue.equals(value);
    else
      return (((pMinValue == null) || (pMinValue.compareTo(value) <= 0)) && 
	      ((pMaxValue == null) || (pMaxValue.compareTo(value) > 0)));
  }

  /**
   * Whether this range contains only a single value.
   */ 
  public boolean
  isSingleValued()
  {
    return (pSingleValue != null);
  }

  /**
   * Whether the range has a lower bounds.
   */ 
  public boolean
  hasLowerBound()
  {
    return (pMinValue != null);
  }

  /**
   * Whether the range has an upper bounds.
   */ 
  public boolean
  hasUpperBound()
  {
    return (pMaxValue != null);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * Gets the only value considered to be inside the range  
   * or <CODE>null</CODE> if a this is a multi-valued range.
   */ 
  public Comparable
  getSingleValue()
  {
    return pSingleValue;
  }

  /** 
   * Gets the minimum inclusive value of the range 
   * or <CODE>null<CODE> if open ended or single valued.
   */ 
  public Comparable
  getMinValue()
  {
    return pMinValue;
  }

  /** 
   * Gets the maximum exclusive value of the range 
   * or <CODE>null<CODE> if open ended or single valued.
   */ 
  public Comparable
  getMaxValue()
  {
    return pMaxValue;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof HistogramRange)) {
      HistogramRange r = (HistogramRange) obj;
      
      if(isSingleValued() && r.isSingleValued())
	 return pSingleValue.equals(r.pSingleValue);
   
      if(!isSingleValued() && !r.isSingleValued()) 
	return ((((pMinValue == null) && (r.pMinValue == null)) || 
		 ((pMinValue != null) && (pMinValue.compareTo(r.pMinValue) == 0))) && 
		(((pMaxValue == null) && (r.pMaxValue == null)) || 
		 ((pMaxValue != null) && (pMaxValue.compareTo(r.pMaxValue) == 0))));
    }

    return false;
  }

  /**
   * Returns a string representation of the object. 
   */
  public String
  toString() 
  {
    if(isSingleValued()) {
      return formatValue(pSingleValue);
    }
    else if(pMinValue == null) {
      if(pMaxValue == null) 
	return "[any]";
      else 
	return ("<" + formatValue(pMaxValue)); 
    }
    else {
      if(pMaxValue == null) 
	return (">" + formatValue(pMinValue));
      else 
	return (formatValue(pMinValue) + "-" + formatValue(pMaxValue));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof HistogramRange))
      throw new IllegalArgumentException
	("The object to compare was NOT a HistogramRange!");

    return compareTo((HistogramRange) obj);
  }

  /**
   * Compares this <CODE>HistogramRange</CODE> with the given 
   * <CODE>HistogramRange</CODE> for order.
   * 
   * @param r
   *   The <CODE>HistogramRange</CODE> to be compared.
   */
  public int
  compareTo
  (
   HistogramRange r
  )
  {
    if(equals(r)) 
      return 0; 

    if(isSingleValued()) {
      if(r.isSingleValued())
	return pSingleValue.compareTo(r.pSingleValue);
      else {
	if((r.pMinValue != null) && (pSingleValue.compareTo(r.pMinValue) < 0))
	  return -1;

	if((r.pMaxValue != null) && (pSingleValue.compareTo(r.pMaxValue) >= 0))
	  return 1;
      }
    }

    if(r.isSingleValued()) {
      if(!isSingleValued()) {
	if((pMinValue != null) && (r.pSingleValue.compareTo(pMinValue) < 0))
	  return 1;
	
	if((pMaxValue != null) && (r.pSingleValue.compareTo(pMaxValue) >= 0))
	  return -1;
      }
    }

    if((pMaxValue != null) && (r.pMinValue != null) && 
       (pMaxValue.compareTo(r.pMinValue) <= 0))
      return -1;

    if((r.pMaxValue != null) && (pMinValue != null) && 
       (r.pMaxValue.compareTo(pMinValue) <= 0))
      return 1;

    throw new IllegalArgumentException
      ("The given range (" + r + ") cannot be compared for order with this range " + 
       "(" + toString() + ")!"); 
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
    if(isSingleValued())
      encoder.encode("Single", pSingleValue);
    else {
      encoder.encode("Min", pMinValue); 
      encoder.encode("Max", pMaxValue);
    }
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    pSingleValue = (Comparable) decoder.decode("Single"); 

    pMinValue = (Comparable) decoder.decode("Min"); 
    pMaxValue = (Comparable) decoder.decode("Max"); 
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
    return value.toString();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -191184569056560364L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The sole value considered inside the range 
   * or <CODE>null</CODE> if this is a multiple valued range.
   */
  private Comparable  pSingleValue; 

  /** 
   * The minimum inclusive value of the range 
   * or <CODE>null<CODE> if open ended or single valued.
   */ 
  private Comparable  pMinValue; 

  /** 
   * The maximum exclusive value of the range 
   * or <CODE>null<CODE> if open ended or single valued.
   */ 
  private Comparable  pMaxValue; 
 
}

