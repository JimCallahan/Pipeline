// $Id: Histogram.java,v 1.3 2009/12/11 04:21:10 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   H I S T O G R A M                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Counts the number of values sorted into a set of non-overlapping catagories.
 */
public
class Histogram
  extends HistogramSpec
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
  Histogram() 
  {}

  /**
   * Construct a new histogram from the given specification.
   * 
   * @param spec
   *   The histogram specification.
   */
  public
  Histogram
  (
   HistogramSpec spec
  ) 
  {
    super(spec);
    
    pCounts = new long[getNumCatagories()];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the number of items in the given category.
   * 
   * @param idx
   *   The category index.
   */ 
  public long 
  getCount
  (
   int idx
  )
  {
    return pCounts[idx];
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Clear all category counts.
   */ 
  public void 
  clearCounts()
  {
    int wk;
    for(wk=0; wk<pCounts.length; wk++) 
      pCounts[wk] = 0L;
  }

  /**
   * Increment the item count of the category which contains the given item.
   * 
   * @param item
   *   The value to match against the histogram ranges
   */ 
  @SuppressWarnings("unchecked")
  public void 
  catagorize
  (
   Comparable item
  ) 
  {
    int wk; 
    for(wk=0; wk<pCounts.length; wk++) {
      if(getRange(wk).isInsideRange(item)) {
	pCounts[wk]++;
	return;
      }
    }
  }
  
  /**
   * Increase the item count of the category which contains the given item.
   * 
   * @param item
   *   The value to match against the histogram ranges
   *   
   * @param increment
   *   The amount to increase the item count by.
   */ 
  @SuppressWarnings("unchecked")
  public void 
  catagorize
  (
   Comparable item,
   int increment
  ) 
  {
    int wk; 
    for(wk=0; wk<pCounts.length; wk++) {
      if(getRange(wk).isInsideRange(item)) {
        pCounts[wk]+= increment;
        return;
      }
    }
  }


    
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    encoder.encode("Counts", pCounts);
  }

  @Override
  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    pCounts = (long[]) decoder.decode("Counts"); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8142896086762001835L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
 
  /** 
   * The count of items in each category.
   */
  private long[]  pCounts; 
 
}

