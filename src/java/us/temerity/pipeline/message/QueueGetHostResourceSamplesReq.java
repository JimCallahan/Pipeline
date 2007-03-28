// $Id: QueueGetHostResourceSamplesReq.java,v 1.3 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   R E S O U R C E   S A M P L E S   R E Q                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the full system resource usage history of the given host.
 */
public 
class QueueGetHostResourceSamplesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param intervals
   *   The sample intervals to retrieve indexed by fully resolved hostnames.
   * 
   * @param runtimeOnly
   *   Whether to only read samples from the runtime cache ignoring any saved samples
   *   on disk.
   */
  public
  QueueGetHostResourceSamplesReq
  (
   TreeMap<String,TimeInterval> intervals,
   boolean runtimeOnly
  )
  { 
    if(intervals == null) 
      throw new IllegalArgumentException
	("The intervals cannot be (null)!");
    pIntervals = intervals;

    pRuntimeOnly = runtimeOnly;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the sample intervals to retrieve indexed by fully resolved hostnames.
   */
  public TreeMap<String,TimeInterval>
  getIntervals() 
  {
    return pIntervals; 
  }

  /**
   * Whether to only read samples from the runtime cache ignoring any saved samples
   * on disk.
   */
  public boolean
  runtimeOnly() 
  {
    return pRuntimeOnly;
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1874346801984531952L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sample intervals to retrieve indexed by fully resolved hostnames.
   */ 
  private TreeMap<String,TimeInterval>  pIntervals; 

  /**
   * Whether to only read samples from the runtime cache ignoring any saved samples
   * on disk.
   */ 
  private boolean  pRuntimeOnly; 
}
  
