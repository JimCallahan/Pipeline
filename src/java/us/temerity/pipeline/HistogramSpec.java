// $Id: HistogramSpec.java,v 1.3 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H I S T O G R A M   S P E C                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A specification of the categories into which a histogram sorts a set of values.<P> 
 * 
 * Besides categorizing values, the histogram specification also maintains a flag for 
 * each category which determines whether it is a member of the matching set.  A given 
 * value can be tested using the {@link #isIncludedItem} method to test whether it 
 * belongs to one of the categories which are included in this matching set.
 */
public
class HistogramSpec
  extends Named
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
  HistogramSpec()
  {}

  /**
   * Construct a new histogram specification with the given set of category ranges.
   * 
   * @param name
   *   The name of the histogram.
   * 
   * @param ranges
   *   The set of histogram category ranges.
   */
  public
  HistogramSpec
  (
   String name,
   SortedSet<HistogramRange> ranges
  ) 
  {
    super(name);
    
    if((ranges == null) || ranges.isEmpty()) 
       throw new IllegalArgumentException
	 ("A non-empty set of category ranges is required!");
    int size = ranges.size();

    int wk = 0;
    pRanges = new HistogramRange[size];
    pIncluded = new boolean[size];
    for(HistogramRange range : ranges) {
      pRanges[wk] = range;
      wk++;
    }
  }

  /**
   * Construct a new histogram specification which is a copy of an existing specification.
   * 
   * @param spec
   *   The histogram specification to copy.
   */
  public
  HistogramSpec
  (
   HistogramSpec spec
  ) 
  {
    super(spec.getName()); 
    
    int size = spec.getNumCatagories();

    int wk = 0;
    pRanges = new HistogramRange[size];
    pIncluded = new boolean[size];
    for(wk=0; wk<size; wk++) {
      pRanges[wk] = spec.getRange(wk);
      pIncluded[wk] = spec.isIncluded(wk);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether any categories are currently included in the matching set.
   */ 
  public boolean 
  anyIncluded() 
  {
    int wk; 
    for(wk=0; wk<pIncluded.length; wk++) {
      if(pIncluded[wk])
	return true;
    }

    return false;
  }

  /**
   * Whether all or none of the categories are currently included in the matching set.
   */ 
  public boolean 
  allIncluded() 
  {
    int cnt = 0;
    int wk; 
    for(wk=0; wk<pIncluded.length; wk++) {
      if(pIncluded[wk])
	cnt++;
    }

    return ((cnt == 0) || (cnt == pIncluded.length)); 
  }

  /**
   * Whether the given category is included in the matching set.
   * 
   * @param range
   *   The range of the category.
   */ 
  public boolean
  isIncluded
  (
   HistogramRange range
  ) 
  {
    int wk;
    for(wk=0; wk<pRanges.length; wk++) {
      if(pRanges[wk].equals(range)) 
	return pIncluded[wk];
    }

    return false;
  }

  /**
   * Whether the given category is included in the matching set.
   * 
   * @param idx
   *   The category index.
   */ 
  public boolean
  isIncluded
  (
   int idx
  ) 
  {
    return pIncluded[idx];
  }

  /**
   * Whether the given item belongs to one of the catagories included in the matching set.
   */ 
  public boolean
  isIncludedItem 
  (
   Comparable item
  ) 
  {
    int wk; 
    for(wk=0; wk<pRanges.length; wk++) {
      if(pRanges[wk].isInsideRange(item)) 
	return pIncluded[wk];
    }
    
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the number of histogram catagories.
   */ 
  public int
  getNumCatagories() 
  {
    return pRanges.length; 
  }

  /**
   * Get the range of the given category.
   * 
   * @param idx
   *   The category index.
   */ 
  public HistogramRange
  getRange
  (
   int idx
  )
  {
    return pRanges[idx];
  } 

  /**
   * Get the ranges of the catagories included in the matching set.
   */ 
  public TreeSet<HistogramRange> 
  getIncluded() 
  {
    TreeSet<HistogramRange> incs = new TreeSet<HistogramRange>();

    int wk;
    for(wk=0; wk<pIncluded.length; wk++) {
      if(pIncluded[wk]) 
	incs.add(pRanges[wk]);
    }

    return incs;
  }

  /**
   * Set whether to include the given category when matching a value using the 
   * {@link #isIncludedItem} method.
   * 
   * @param range
   *   The range of the category.
   * 
   * @param tf
   *   Whether to include the category.
   */ 
  public void
  setIncluded
  (
   HistogramRange range,
   boolean tf
  ) 
  {
    int wk;
    for(wk=0; wk<pRanges.length; wk++) {
      if(pRanges[wk].equals(range)) {
	pIncluded[wk] = tf;
	return;
      }
    }
  }
  
  /**
   * Set whether to include the given category when matching a value using the 
   * {@link #isIncludedItem} method.
   * 
   * @param idx
   *   The category index.
   * 
   * @param tf
   *   Whether to include the category.
   */ 
  public void
  setIncluded
  (
   int idx, 
   boolean tf
  ) 
  {
    pIncluded[idx] = tf;
  }
  
  /**
   * Set whether to include all catagories when matching a value using the 
   * {@link #isIncludedItem} method.
   * 
   * @param tf
   *   Whether to include all catagories.
   */ 
  public void
  setIncluded
  (
   boolean tf
  ) 
  {
    int wk;
    for(wk=0; wk<pIncluded.length; wk++) 
      pIncluded[wk] = tf;
  }

  /**
   * Toggle the whether the given category is included in the matching set.
   * 
   * @param idx
   *   The category index.
   */ 
  public void 
  toggleIncluded
  (
   int idx
  ) 
  {
    pIncluded[idx] = !pIncluded[idx];
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
    super.toGlue(encoder);

    encoder.encode("Ranges", pRanges);
    encoder.encode("Included", pIncluded);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pRanges = (HistogramRange[]) decoder.decode("Ranges"); 
    pIncluded = (boolean[]) decoder.decode("Included"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5657636068259907888L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Range of values which defines the bounds of each category.
   */
  private HistogramRange[]  pRanges; 
 
  /** 
   * Whether each category is included in the range of values matched by the pattern
   * matcher feature.
   */
  private boolean[]  pIncluded; 
 
}

