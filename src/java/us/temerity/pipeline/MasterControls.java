// $Id: MasterControls.java,v 1.9 2009/12/01 06:50:59 jim Exp $
  
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
   * Construct a with all parameters initialized to defaults.
   */ 
  public 
  MasterControls() 
  {
    this(null, null, null, null, null, null, null, null, null, null, null); 
  }

  /** 
   * Construct a with default values for only file system related parameters. <P> 
   * 
   * Any parameter can be left unset by suppling <CODE>null</CODE> for its initial 
   * value.
   * 
   * @param fileStatDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for file status query traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   * 
   * @param checkSumDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for checksum generation traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   */ 
  public 
  MasterControls
  (
   Path fileStatDir, 
   Path checkSumDir
  ) 
  {
    this(null, null, null, null, null, null, null, null, null, fileStatDir, checkSumDir);
  }

  /** 
   * Construct a with default values for all parameters. <P> 
   * 
   * Any parameter can be left unset by suppling <CODE>null</CODE> for its initial 
   * value.
   * 
   * @param minFreeMem
   *   The minimum amount of free Java heap memory available before caches must be reduced. 
   * 
   * @param gcInterval
   *   The maximum amount of time between runs of the cache garbage collector 
   *   (in milliseconds).
   * 
   * @param gcMisses
   *   The maximum number of cache misses before the cache garbage collector is run.
   * 
   * @param cacheFactor
   *   The ratio between minimum and maximum number of items maintained in each cache. 
   * 
   * @param repoCacheSize
   *   The minimum number of checked-in versions of nodes to cache.
   * 
   * @param workCacheSize
   *   The minimum number of working versions of nodes to cache.
   * 
   * @param checkCacheSize
   *   The minimum number of working version checksums to cache.
   * 
   * @param annotCacheSize
   *   The minimum number of per-node annotations to cache.
   * 
   * @param restoreCleanupInterval
   *   The maximum age of a resolved (Restored or Denied) restore request before it 
   *   is deleted (in milliseconds).
   * 
   * @param fileStatDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for file status query traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   * 
   * @param checkSumDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for checksum generation traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   */ 
  public 
  MasterControls
  (
   Long minFreeMem, 
   Long gcInterval, 
   Long gcMisses, 
   Double cacheFactor, 
   Long repoCacheSize, 
   Long workCacheSize, 
   Long checkCacheSize, 
   Long annotCacheSize, 
   Long restoreCleanupInterval,
   Path fileStatDir, 
   Path checkSumDir
  ) 
  {    
    setMinFreeMemory(minFreeMem); 
    setCacheGCInterval(gcInterval); 
    setCacheGCMisses(gcMisses); 
    setCacheFactor(cacheFactor); 
    setRepoCacheSize(repoCacheSize);
    setWorkCacheSize(workCacheSize); 
    setCheckCacheSize(checkCacheSize); 
    setAnnotCacheSize(annotCacheSize);
    setRestoreCleanupInterval(restoreCleanupInterval); 
    setFileStatDir(fileStatDir);
    setCheckSumDir(checkSumDir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum amount of free Java heap memory available before caches must be reduced.
   *
   * @return 
   *   The amount of memory or <CODE>null</CODE> if unset.
   */ 
  public Long
  getMinFreeMemory() 
  {
    return pMinFreeMemory;
  }

  /**
   * Set the minimum amount of free Java heap memory available before caches must be reduced.
   * 
   * @param bytes
   *   The amount of memory or <CODE>null</CODE> for default.
   */
  public void 
  setMinFreeMemory
  (
   Long bytes
  ) 
  {
    /* bound checking is done on the server */ 
    pMinFreeMemory = bytes;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the minimum time a cycle of the cache garbage collector loop should 
   * take (in milliseconds).
   *
   * @return 
   *   The interval or <CODE>null</CODE> if unset.
   */ 
  public Long
  getCacheGCInterval() 
  {
    return pCacheGCInterval;
  }

  /**
   * Set the minimum time a cycle of the cache garbage collector loop should 
   * take (in milliseconds).
   * 
   * @param msec
   *   The interval or <CODE>null</CODE> for default.
   */
  public void 
  setCacheGCInterval
  (
   Long msec
  ) 
  {
    if(msec != null) {
      if(msec < 15000L)
        throw new IllegalArgumentException
          ("The node garbage collection interval (" + msec + " msec) must be at " +
           "least 15-seconds!");
      pCacheGCInterval = msec;
    }
    else {
      pCacheGCInterval = 300000L;  /* 5-minutes */ 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the maximum number of cache misses before the cache garbage collector is run.
   *
   * @return 
   *   The number of misses or <CODE>null</CODE> if unset.
   */ 
  public Long
  getCacheGCMisses() 
  {
    return pCacheGCMisses;
  }

  /**
   * Set the maximum number of cache misses before the cache garbage collector is run.
   * 
   * @param misses 
   *   The number of misses or <CODE>null</CODE> for default.
   */
  public void 
  setCacheGCMisses
  (
   Long misses
  ) 
  {
    if(misses != null) {
      if(misses < 100L)
        throw new IllegalArgumentException
          ("The number of cache misses (" + misses + ") must be at least 100!");
      pCacheGCMisses = misses;
    }
    else {
      pCacheGCMisses = 2500L;
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the ratio between minimum and maximum number of items maintained in each cache. 
   * 
   * @return 
   *   The number of misses or <CODE>null</CODE> if unset.
   */ 
  public Double
  getCacheFactor() 
  {
    return pCacheFactor;
  }

  /**
   * Set the ratio between minimum and maximum number of items maintained in each cache. 
   * 
   * @param factor
   *   The cache factor or <CODE>null</CODE> for default.
   */
  public void 
  setCacheFactor
  (
   Double factor
  ) 
  {
    if(factor != null) {
      if((factor < 0.25) || (factor > 0.95)) 
        throw new IllegalArgumentException
          ("The cache factor (" + factor + ") must be in the range [0.5-0.95]!");
      pCacheFactor = factor;
    }
    else {
      pCacheFactor = 0.85;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the minimum number of checked-in versions of nodes to cache.
   * 
   * @return 
   *   The number of versions or <CODE>null</CODE> if unset.
   */ 
  public Long
  getRepoCacheSize()
  {
    return pRepoCacheSize; 
  }

  /**
   * Set the minimum and maximum number of checked-in versions of nodes to cache.
   * 
   * @param size
   *   The minimum size or <CODE>null</CODE> for default.
   */
  public void 
  setRepoCacheSize
  (
   Long size
  ) 
  {
    if(size != null) {
      if(size < 500L)
        throw new IllegalArgumentException 
          ("The minimum checked-in versions cache size (" + size + ") must at least 500!"); 
      pRepoCacheSize = size;
    }
    else {
      pRepoCacheSize = 500L;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the minimum number of working versions of nodes to cache.
   * 
   * @return 
   *   The number of versions or <CODE>null</CODE> if unset.
   */ 
  public Long
  getWorkCacheSize()
  {
    return pWorkCacheSize; 
  }

  /**
   * Set the minimum and maximum number of working versions of nodes to cache.
   * 
   * @param size
   *   The minimum size or <CODE>null</CODE> for default.
   */
  public void 
  setWorkCacheSize
  (
   Long size
  ) 
  {
    if(size != null) {
      if(size < 250L)
        throw new IllegalArgumentException 
          ("The minimum working versions cache size (" + size + ") must at least 250!"); 
      pWorkCacheSize = size;
    }
    else {
      pWorkCacheSize = 500L;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the minimum number of working version checksums of nodes to cache.
   * 
   * @return 
   *   The number of versions or <CODE>null</CODE> if unset.
   */ 
  public Long
  getCheckCacheSize()
  {
    return pCheckCacheSize; 
  }

  /**
   * Set the minimum and maximum number of working version checksums of nodes to cache.
   * 
   * @param size
   *   The minimum size or <CODE>null</CODE> for default. 
   */
  public void 
  setCheckCacheSize
  (
   Long size
  ) 
  {
    if(size != null) {
      if(size < 250L)
        throw new IllegalArgumentException 
          ("The minimum working version checksums cache size (" + size + ") must at " + 
           "least 250!"); 
      pCheckCacheSize = size;
    }
    else {
      pCheckCacheSize = 250L;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get the minimum number of per-version annotations of nodes to cache.
   * 
   * @return 
   *   The number of versions or <CODE>null</CODE> if unset.
   */ 
  public Long
  getAnnotCacheSize()
  {
    return pAnnotCacheSize; 
  }

  /**
   * Set the minimum and maximum number of per-version annotations of nodes to cache.
   * 
   * @param size
   *   The minimum size or <CODE>null</CODE> for default. 
   */
  public void 
  setAnnotCacheSize
  (
   Long size
  ) 
  {
    if(size != null) {
      if(size < 100L)
        throw new IllegalArgumentException 
          ("The minimum per-version annotations cache size (" + size + ") must at " + 
           "least 100!"); 
      pAnnotCacheSize = size;
    }
    else {
      pAnnotCacheSize = 100L;
    }
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
   * @param interval
   *   The cleanup internval or <CODE>null</CODE> for default. 
   */
  public void 
  setRestoreCleanupInterval
  (
   Long interval
  ) 
  {
    if(interval != null) {
      if(interval < 3600000L)
        throw new IllegalArgumentException
          ("The restore cleanup interval (" + interval + " msec) must be at " + 
           "least 1 hour!"); 
      pRestoreCleanupInterval = interval; 
    }
    else {
      pRestoreCleanupInterval = 172800000L;  /* 48-hours */ 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for file status query traffic.
   *
   * @return 
   *   The file status root production directory or <CODE>null</CODE> if the default root
   *   production directory is being used instead.
   */ 
  public Path
  getFileStatDir() 
  {
    return pFileStatDir;
  }

  /**
   * Set the alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for file status query traffic.
   * 
   * @param dir
   *   The file status root production directory or <CODE>null</CODE> for default. 
   */
  public void 
  setFileStatDir
  (
   Path dir
  ) 
  {
    pFileStatDir = dir; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for checksum generation traffic.
   *
   * @return 
   *   The checksum root production directory or <CODE>null</CODE> if the default root
   *   production directory is being used instead.
   */ 
  public Path
  getCheckSumDir() 
  {
    return pCheckSumDir;
  }

  /**
   * Set the alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for checksum generation traffic.
   * 
   * @param dir
   *   The checksum root production directory or <CODE>null</CODE> for default. 
   */
  public void 
  setCheckSumDir
  (
   Path dir
  ) 
  {
    pCheckSumDir = dir; 
  }


 
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 517296120185570860L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The minimum amount of free Java heap memory available before caches must be reduced. 
   */
  private Long  pMinFreeMemory;

  /**
   * The minimum time a cycle of the node cache garbage collector loop should 
   * take (in milliseconds).
   */ 
  private Long  pCacheGCInterval;

  /**
   * The maximum number of cache misses before the cache garbage collector is run.
   */
  private Long  pCacheGCMisses;

  /**
   * The ratio between minimum and maximum number of items maintained in each cache. 
   */
  private Double  pCacheFactor;

  /**
   * The minimum number of checked-in versions of nodes to cache.
   */ 
  private Long  pRepoCacheSize;

  /**
   * The minimum number of working versions of nodes to cache.
   */ 
  private Long  pWorkCacheSize;

  /**
   * The minimum number of working version checksums of nodes to cache.
   */ 
  private Long  pCheckCacheSize;

  /**
   * The minimum number of per-node annotations of nodes to cache.
   */ 
  private Long  pAnnotCacheSize;

  /**
   * The maximum age of a resolved (Restored or Denied) restore request before it 
   * is deleted (in milliseconds).
   */ 
  private Long  pRestoreCleanupInterval; 

  /**
   * An alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for file status query traffic.
   */ 
  private Path pFileStatDir;

  /**
   * An alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for checksum generation traffic.
   */ 
  private Path pCheckSumDir;

}


