// $Id: MasterControls.java,v 1.1 2006/12/01 18:41:57 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R   C O N T R O L S                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Runtime performance tuning parameters for the Master Manager server daemon.
 */
public
class MasterControls  
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a with all parameters unset. 
   */ 
  public 
  MasterControls() 
  {}

  /** 
   * Construct a with default values for all parameters. <P> 
   * 
   * Any parameter can be left unset by suppling <CODE>null</CODE> for its initial 
   * value.
   * 
   * @param avgNodeSize
   *   The estimated memory size of a node version (in bytes).
   * 
   * @param minOverhead
   *   The minimum amount of memory overhead to maintain at all times.
   * 
   * @param maxOverhead
   *   The maximum amount of memory overhead required to be available after a node garbage
   *   collection.
   * 
   * @param nodeGCInterval
   *   The minimum time a cycle of the node cache garbage collector loop should 
   *   take (in milliseconds).
   * 
   * @param restoreCleanupInterval
   *   The maximum age of a resolved (Restored or Denied) restore request before it 
   *   is deleted (in milliseconds).
   */ 
  public 
  MasterControls
  (
   Long avgNodeSize, 
   Long minOverhead, 
   Long maxOverhead, 
   Long nodeGCInterval, 
   Long restoreCleanupInterval
  ) 
  {    
    setAverageNodeSize(avgNodeSize); 
    setOverhead(minOverhead, maxOverhead);
    setNodeGCInterval(nodeGCInterval); 
    setRestoreCleanupInterval(restoreCleanupInterval); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the estimated memory size of a node version (in bytes).
   * 
   * @return 
   *   The node size or <CODE>null</CODE> if unset.
   */ 
  public Long
  getAverageNodeSize() 
  {
    return pAvgNodeSize; 
  }

  /**
   * Set the estimated memory size of a node version (in bytes).
   * 
   * @param size
   *   The node size or <CODE>null</CODE> to unset.
   */
  public void 
  setAverageNodeSize
  (
   Long size
  ) 
  {
    if((size != null) && ((size < 2048L) || (size > 16384L)))
      throw new IllegalArgumentException
	("The average node size (" + size + " bytes) must be in the 2K-16K range!"); 
    pAvgNodeSize = size;
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum amount of memory overhead to maintain at all times.
   * 
   * @return 
   *   The minimum overhead or <CODE>null</CODE> if unset.
   */ 
  public Long
  getMinimumOverhead()
  {
    return pMinOverhead; 
  }

  /**
   * Get the maximum amount of memory overhead required to be available after a node 
   * garbage collection.
   * 
   * @return 
   *   The maximum overhead or <CODE>null</CODE> if unset.
   */ 
  public Long
  getMaximumOverhead()
  {
    return pMaxOverhead; 
  }

  /**
   * Set the minimum amount of memory overhead to maintain at all times and the maximum 
   * amount of memory overhead required to be available after a node garbage collection.
   * 
   * @param min
   *   The minimum overhead or <CODE>null</CODE> to unset.
   * 
   * @param max
   *   The maximum overhead or <CODE>null</CODE> to unset.
   */
  public void 
  setOverhead
  (
   Long min,
   Long max 
  ) 
  {
    if((min != null) && (min <= 8388608L))
      throw new IllegalArgumentException 
	("The minimum memory overhead (" + min + " bytes) must at least 8M!"); 
    
    if((max != null) && (max <= 16777216L)) 
      throw new IllegalArgumentException 
	("The maximum memory overhead (" + max + " bytes) must at least 16M!"); 

    if((min != null) && (max != null) && (max <= min))
      throw new IllegalArgumentException 
	("The maximum memory overhead (" + max + " bytes) must greater-than the " + 
	 "minimum memory overhead (" + min + " bytes)!"); 

    pMinOverhead = min;
    pMaxOverhead = max; 
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the minimum time a cycle of the node cache garbage collector loop should 
   * take (in milliseconds).
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getNodeGCInterval() 
  {
    return pNodeGCInterval;
  }

  /**
   * Set the minimum time a cycle of the node cache garbage collector loop should 
   * take (in milliseconds).
   * 
   * @param msec
   *   The interval or <CODE>null</CODE> to unset.
   */
  public void 
  setNodeGCInterval
  (
   Long msec
  ) 
  {
    if((msec != null) && (msec <= 2048L))
      throw new IllegalArgumentException
	("The node garbage collection interval (" + msec + " ms) must be at " +
	 "least 15 seconds!");
    pNodeGCInterval = msec;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getRestoreCleanupInterval() 
  {
    return pRestoreCleanupInterval;
  }

  /**
   * Set the maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   * 
   * @param age
   *   The interval or <CODE>null</CODE> to unset.
   */
  public void 
  setRestoreCleanupInterval
  (
   Long interval
  ) 
  {
    if((interval != null) && (interval < 3600000L)) 
      throw new IllegalArgumentException
	("The restore cleanup interval (" +interval  + " ms) must be at " + 
	 "least 1 hour!"); 
    pRestoreCleanupInterval = interval; 
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 517296120185570860L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum time a cycle of the node cache garbage collector loop should 
   * take (in milliseconds).
   */ 
  private Long  pNodeGCInterval;

  /**
   * The minimum amount of memory overhead to maintain at all times.  The maximum amount of 
   * memory overhead required to be available after a node garbage collection.
   */ 
  private Long  pMinOverhead;
  private Long  pMaxOverhead;

  /**
   * The estimated memory size of a node version (in bytes).
   */ 
  private Long  pAvgNodeSize; 

  /**
   * The maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   */ 
  private Long  pRestoreCleanupInterval; 


}


