// $Id: QueueControls.java,v 1.7 2010/01/08 09:37:57 jim Exp $
  
package us.temerity.pipeline;

import java.io.*;

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
   * Construct with all parameters initialized to defaults.
   */ 
  public 
  QueueControls() 
  {
    this(null, null, null, null, null, null, null); 
  }

  /** 
   * Construct with the given values for all parameters. <P> 
   * 
   * Any parameter can be set to its default value by suppling <CODE>null</CODE>. 
   * 
   * @param collectorBatchSize
   *   The maximum number of job servers per collection sub-thread.
   * 
   * @param dispatcherInterval
   *   The minimum time a cycle of the dispatcher loop should take (in milliseconds).
   * 
   * @param isMaxLoadEnabled 
   *   Whether the job manager load should be tested in quailifying jobs.
   * 
   * @param nfsCacheInterval
   *   The minimum time to wait before attempting a NFS directory attribute lookup operation
   *   after a file in the directory has been created by another host on the network 
   *   (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   *   mount option for the root production directory on the host running the Queue Manager.
   * 
   * @param backupSyncInterval
   *   The interval (in milliseconds) between the live synchronization of the database 
   *   files associated with the Master Manager and backup copies of these files.
   *   
   * @param balanceSampleInterval
   *   The interval (in milliseconds) between when the User Balance Info class updates its 
   *   samples.   
   *   
   * @param balanceSamplesToKeep
   *   The number of samples of balance group information that are being kept.
   */ 
  public 
  QueueControls
  (
   Integer collectorBatchSize,
   Long dispatcherInterval, 
   Boolean isMaxLoadEnabled, 
   Long nfsCacheInterval,
   Long backupSyncInterval,
   Long balanceSampleInterval,
   Integer balanceSamplesToKeep
  ) 
  {    
    setCollectorBatchSize(collectorBatchSize); 
    setDispatcherInterval(dispatcherInterval); 
    setIsMaxLoadEnabled(isMaxLoadEnabled); 
    setNfsCacheInterval(nfsCacheInterval); 
    setBackupSyncInterval(backupSyncInterval);
    setBalanceSampleInterval(balanceSampleInterval);
    setBalanceSamplesToKeep(balanceSamplesToKeep);
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
   *   The batch size or <CODE>null</CODE> for default.
   */
  public void 
  setCollectorBatchSize
  (
   Integer size
  ) 
  {
    if(size != null) {
      if(size <= 0L) 
        throw new IllegalArgumentException
          ("The collector batch size (" + size + ") must be positive!");
      pCollectorBatchSize = size;
    }
    else {
      pCollectorBatchSize = 50;
    }
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
    if(interval != null) {
      if(interval < 500L)
        throw new IllegalArgumentException
          ("The dispatcher interval (" + interval + ") must be at least 500ms!");
      pDispatcherInterval = interval; 
    }
    else {
      pDispatcherInterval = 2000L;  /* 2-seconds */ 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get whether the job manager load should be tested in quailifying jobs.
   *
   * @return 
   *   Whether it is or <CODE>null</CODE> if unset.
   */ 
  public Boolean
  getIsMaxLoadEnabled() 
  {
    return pIsMaxLoadEnabled;
  }

  /**
   * Set whether the job manager load should be tested in quailifying jobs.
   * 
   * @param tf
   *   Whether it is or <CODE>null</CODE> to unset.
   */
  public void 
  setIsMaxLoadEnabled
  (
   Boolean tf
  ) 
  {
    if(tf != null) 
      pIsMaxLoadEnabled = tf; 
    else 
      pIsMaxLoadEnabled = true;
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
    if(interval != null) {
      if(interval < 0L)
        throw new IllegalArgumentException
          ("The NFS cache interval (" + interval + ") cannot be negative!");
      pNfsCacheInterval = interval; 
    }
    else {
      pNfsCacheInterval = 5000L;  /* 5-seconds */ 
    }
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the interval (in milliseconds) between the live synchronization of the database 
   * files associated with the Master Manager and backup copies of these files.
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getBackupSyncInterval() 
  {
    return pBackupSyncInterval;
  }

  /**
   * Set the interval (in milliseconds) between the live synchronization of the database 
   * files associated with the Master Manager and backup copies of these files.
   * 
   * @param interval
   *   The interval or <CODE>null</CODE> for default. 
   */
  public void 
  setBackupSyncInterval
  (
   Long interval
  ) 
  {
    if(interval != null) {
      if(interval < 3600000L)
        throw new IllegalArgumentException
          ("The backup sync interval (" + interval + " msec) must be at " + 
           "least 1 hour!"); 
      pBackupSyncInterval = interval; 
    }
    else {
      pBackupSyncInterval = 43200000L;  /* 12-hours */ 
    }
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the interval (in milliseconds) between when the User Balance Info class updates its 
   * samples.
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getBalanceSampleInterval() 
  {
    return pBalanceSampleInterval;
  }
  
  /**
   * Set the interval (in milliseconds) between when the User Balance Info class updates its 
   * samples.
   * 
   * @param interval
   *   The interval or <CODE>null</CODE> for default. 
   */
  public void 
  setBalanceSampleInterval
  (
   Long interval
  ) 
  {
    if(interval != null) {
      if(interval < 15000L)
        throw new IllegalArgumentException
          ("The balance sample interval (" + interval + " msec) must be at " + 
           "least 15 seconds!"); 
      pBalanceSampleInterval = interval; 
    }
    else {
      pBalanceSampleInterval = 120000L;  /* 2 minutes */ 
    }
  }
  
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the number of samples of balance group information that are being kept. <p>
   *
   * @return 
   *   The number of samples or <CODE>null</CODE> if unset.
   */ 
  public Integer
  getBalanceSamplesToKeep()
  {
    return pBalanceSamplesToKeep;
  }
  
  /**
   * Set the number of samples of balance group information that are being kept. <p>
   * 
   * @param samples
   *   The number of samples or <CODE>null</CODE> for default. 
   */
  public void 
  setBalanceSamplesToKeep
  (
   Integer samples
  ) 
  {
    if(samples != null) {
      if(samples < 1)
        throw new IllegalArgumentException
          ("The number of samples to keep (" + samples + ") must be at " + 
           "least 1!"); 
      pBalanceSamplesToKeep = samples; 
    }
    else {
      pBalanceSamplesToKeep = 30; 
    }
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
   * Whether the job manager load should be tested in quailifying jobs.
   */ 
  private Boolean  pIsMaxLoadEnabled; 

  /**
   * The minimum time to wait before attempting a NFS directory attribute lookup operation
   * after a file in the directory has been created by another host on the network 
   * (in milliseconds).  This should be set to the same value as the NFS (acdirmax) 
   * mount option for the root production directory on the host running the Queue Manager.
   */ 
  private Long  pNfsCacheInterval; 

  /**
   * The interval (in milliseconds) between the live synchronization of the database 
   * files associated with the Master Manager and backup copies of these files.
   */ 
  private Long  pBackupSyncInterval; 
  
  /**
   * The interval (in milliseconds) between when the User Balance Info class updates its 
   * samples.<P>
   * 
   * A longer sample interval will result in less responsive user balancing, but will allow 
   * more samples to be stored (making balancing more fair over longer periods of time). When 
   * tuning, this variable should be considered along with the the number of samples being 
   * kept.
   */
  private Long pBalanceSampleInterval;
  
  /**
   * The number of samples of balance group information that are being kept. <p>
   */
  private Integer pBalanceSamplesToKeep;
}
