// $Id: QueueGetHostResourceSamplesRsp.java,v 1.3 2005/01/22 01:36:36 jim Exp $

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
   * @param block
   *   The resource usage samples.
   */ 
  public
  QueueGetHostResourceSamplesRsp
  (
   TaskTimer timer, 
   ResourceSampleBlock block
  )
  { 
    super(timer);

    if(block == null) 
      throw new IllegalArgumentException("The resource samples block cannot be (null)!");
    pBlock = block;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"QueueMgr.getHostResourceSamples():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the resource usage samples.
   */
  public ResourceSampleBlock
  getSamples() 
  {
    return pBlock; 
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
  private ResourceSampleBlock  pBlock; 

}
  
