// $Id: QueueGetHostResourceSamplesRsp.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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
   *   The resource usage samples.
   */ 
  public
  QueueGetHostResourceSamplesRsp
  (
   TaskTimer timer, 
   ArrayList<ResourceSample> samples
  )
  { 
    super(timer);

    if(samples == null) 
      throw new IllegalArgumentException("The samples cannot be (null)!");
    pSamples = samples;

    Logs.net.finest("QueueMgr.getHostResourceSamples():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the resource usage samples.
   */
  public ArrayList<ResourceSample>
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
   * The resource usage samples.
   */ 
  private ArrayList<ResourceSample>  pSamples;

}
  
