// $Id: QueueGetHostResourceSamplesRsp.java,v 1.6 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   R E S O U R C E   S A M P L E S   R S P                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the full system resource usage history of the given host.
 */
public
class QueueGetHostResourceSamplesRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param samples
   *   The requested samples indexed by fully resolved hostname. 
   */ 
  public
  QueueGetHostResourceSamplesRsp
  (
   TaskTimer timer, 
   TreeMap<String,ResourceSampleCache> samples
  )
  { 
    super(timer);

    if(samples == null) 
      throw new IllegalArgumentException("The resource samples cannot be (null)!");
    pSamples = samples;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHostResourceSamples():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the requested samples indexed by fully resolved hostname. 
   */
  public TreeMap<String,ResourceSampleCache> 
  getSamples() 
  {
    return pSamples; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3088144478486820741L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The requested samples indexed by fully resolved hostname. 
   */ 
  private TreeMap<String,ResourceSampleCache>   pSamples; 

}
  
