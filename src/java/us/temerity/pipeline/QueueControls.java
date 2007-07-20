// $Id: QueueControls.java,v 1.3 2007/07/20 07:47:11 jim Exp $
  
package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   C O N T R O L S                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Runtime performance tuning parameters for the Queue Manager server daemon.
 */
public
class QueueControls  
  implements Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a with all parameters unset. 
   */ 
  public 
  QueueControls() 
  {}

  /** 
   * Construct a with default values for all parameters. <P> 
   * 
   * Any parameter can be left unset by suppling <CODE>null</CODE> for its initial 
   * value.
   * 
   * @param collectorBatchSize
   *   The maximum number of job servers per collection sub-thread.
   * 
   * @param dispatcherInterval
   *   The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   * 
   * @param nfsCacheInterval
   *   The minimum time to wait before attempting a NFS directory attribute lookup operation
   *   after a file in the directory has been created by another host on the network 
   *   (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   *   mount option for the root production directory on the host running the Queue Manager.
   */ 
  public 
  QueueControls
  (
   Integer collectorBatchSize,
   Long dispatcherInterval, 
   Long nfsCacheInterval
  ) 
  {    
    setCollectorBatchSize(collectorBatchSize); 
    setDispatcherInterval(dispatcherInterval); 
    setNfsCacheInterval(nfsCacheInterval); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum number of job servers per collection sub-thread.
   * 
   * @return 
   *   The batch size or <CODE>null</CODE> if unset.
   */ 
  public Integer
  getCollectorBatchSize() 
  {
    return pCollectorBatchSize; 
  }

  /**
   * Set the maximum number of job servers per collection sub-thread.
   * 
   * @param size
   *   The batch size or <CODE>null</CODE> to unset.
   */
  public void 
  setCollectorBatchSize
  (
   Integer size
  ) 
  {
    if((size != null) && (size <= 0L)) 
      throw new IllegalArgumentException
        ("The collector batch size (" + size + ") must be positive!");
    pCollectorBatchSize = size;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the minimum time a cycle of the dispatcher loop should take (in milliseconds).
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getDispatcherInterval() 
  {
    return pDispatcherInterval;
  }

  /**
   * Set the minimum time a cycle of the dispatcher loop should take (in milliseconds).
   * 
   * @param interval
   *   The interval or <CODE>null</CODE> to unset.
   */
  public void 
  setDispatcherInterval
  (
   Long interval
  ) 
  {
    if((interval != null) && (interval < 500L)) 
      throw new IllegalArgumentException
        ("The dispatcher interval (" + interval + ") must be at least 500ms!");
    pDispatcherInterval = interval; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the minimum time to wait before attempting a NFS directory attribute lookup 
   * operation after a file in the directory has been created by another host on the 
   * network (in milliseconds). 
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getNfsCacheInterval() 
  {
    return pNfsCacheInterval;
  }

  /**
   * Set the minimum time to wait before attempting a NFS directory attribute lookup 
   * operation after a file in the directory has been created by another host on the 
   * network (in milliseconds). 
   * 
   * @param interval
   *   The interval or <CODE>null</CODE> to unset.
   */
  public void 
  setNfsCacheInterval
  (
   Long interval
  ) 
  {
    if((interval != null) && (interval < 0L)) 
      throw new IllegalArgumentException
        ("The NFS cache interval (" + interval + ") cannot be negative!");
    pNfsCacheInterval = interval; 
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6283662886376668921L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The maximum number of job servers per collection sub-thread.
   */ 
  private Integer  pCollectorBatchSize; 

  /**
   * The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   */ 
  private Long  pDispatcherInterval; 

  /**
   * The minimum time to wait before attempting a NFS directory attribute lookup operation
   * after a file in the directory has been created by another host on the network 
   * (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   * mount option for the root production directory on the host running the Queue Manager.
   */ 
  private Long  pNfsCacheInterval; 

}


