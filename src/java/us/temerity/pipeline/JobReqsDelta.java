package us.temerity.pipeline;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A class that represents a series of changes that are supposed to be made to 
 * an existing {@link JobReqs}. <p>
 * 
 * Because of its nature, this class allows <code>null</code> values for some or 
 * all of its values.  When these changes are applied to a {@link JobReqs}, those
 * <code>null</code> values are just ignored.
 *
 */
public 
class JobReqsDelta
  extends ParentJobReqs
  implements Serializable
{
  /**
   * Constructor which builds a completely <code>null</code> delta. <p>
   * 
   * Applying the delta created by this constructor to an existing {@link JobReqs} will
   * cause no changes to the {@link JobReqs}.
   */
  public 
  JobReqsDelta
  (
    long jobID  
  )
  {
    pJobID = jobID;
    pPriority = null;
    pRampUp = null;
    pMaxLoad = null;
    pMinMemory = null;
    pMinDisk = null;
    pSelectionKeys = null;
    pLicenseKeys = null;
  }
  
  public JobReqsDelta
  (
    long jobID,
    Integer priority, 
    Integer rampUp,
    Float maxLoad,              
    Long minMemory,              
    Long minDisk,                
    Set<String> licenseKeys,
    Set<String> selectionKeys
  )
  {
    pJobID = jobID;
    setPriority(priority);
    setRampUp(rampUp);
    setMaxLoad(maxLoad);
    setMinMemory(minMemory);
    setMinDisk(minDisk);
    
    if (licenseKeys != null)
      pLicenseKeys = new HashSet<String>(licenseKeys);
    else
      pLicenseKeys = null;
    
    if (selectionKeys != null)
      pSelectionKeys = new HashSet<String>(selectionKeys);
    else
      pSelectionKeys = null;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the jobID
   */
  public long
  getJobID()
  {
    return pJobID;  
  }
  
  /** 
   * Set the relative job priority. The priority can be negative.
   * 
   * @param priority 
   *    The priority of the job relative to other jobs.  
   */
  public void
  setPriority
  (
   Integer priority   
  ) 
  {
    pPriority = priority;
  }
  
  /** 
   * Set the ramp-up interval (in seconds).
   */ 
  public void 
  setRampUp
  (
    Integer interval
  ) 
  {
    if (interval != null) 
      if(interval < 0)
        throw new IllegalArgumentException("The ramp-up interval cannot be negative!");
    pRampUp = interval;
  }
  
  /** 
   * Set the maximum allowable system load on an eligible host.
   */ 
  public void 
  setMaxLoad
  (
   Float load 
  ) 
  {
    if (load != null)
      if(load < 0.0f)
        throw new IllegalArgumentException("The maximum load cannot be negative!");
    pMaxLoad = load;
  }
  
  /**
   * Set the minimum amount of free memory (in bytes) required on an eligible host.
   * 
   */ 
  public void 
  setMinMemory
  (
   Long bytes
  ) 
  {
    if (bytes != null)
      if(bytes < 0)
        throw new IllegalArgumentException("The minimum free memory cannot be negative!");
    pMinMemory = bytes;
  }
  
  /** 
   * Set the minimum amount of free temporary local disk space (in bytes) required on an 
   * eligible host.
   */
  public void 
  setMinDisk
  (
   Long bytes
  ) 
  {
    if (bytes != null)
      if(bytes < 0)
        throw new IllegalArgumentException("The minimum free disk space cannot be negative!");
    pMinDisk = bytes;
  }

  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  addLicenseKey
  (
    String key
  )
  {
    initLicenseKeys();
    super.addLicenseKey(key);
  }
  
  @Override
  public void 
  addLicenseKeys
  (
    Set<String> keys
  )
  {
    initLicenseKeys();
    super.addLicenseKeys(keys);
  }
  
  @Override
  public void 
  removeLicenseKey
  (
    String key
  )
  {
    if (pLicenseKeys != null)
      super.removeLicenseKey(key);
  }
  
  @Override
  public void 
  removeLicenseKeys
  (
    Set<String> keys
  )
  {
    if (pLicenseKeys != null)
      super.removeLicenseKeys(keys);
  }
  
  @Override
  public void 
  removeAllLicenseKeys()
  {
    if (pLicenseKeys != null)
      super.removeAllLicenseKeys();
  }
 
  /**
  *  Creates a new HashSet for the License Keys. 
  */
  private void 
  initLicenseKeys()
  {
    if (pLicenseKeys == null)
      pLicenseKeys = new HashSet<String>();
  }

  /*----------------------------------------------------------------------------------------*/
  
  @Override
  public void 
  addSelectionKey
  (
    String key
  )
  {
    initSelectionKeys();
    super.addSelectionKey(key);
  }
  
  @Override
  public void 
  addSelectionKeys
  (
    Set<String> keys
  )
  {
    initSelectionKeys();
    super.addSelectionKeys(keys);
  }
  
  @Override
  public void 
  removeSelectionKey
  (
    String key
  )
  {
    if (pSelectionKeys != null)
      super.removeSelectionKey(key);
  }
  
  @Override
  public void 
  removeSelectionKeys
  (
    Set<String> keys
  )
  {
    if (pSelectionKeys != null)
      super.removeSelectionKeys(keys);
  }
  
  @Override
  public void 
  removeAllSelectionKeys()
  {
    if (pSelectionKeys != null)
      super.removeAllSelectionKeys();
  }
 
  /**
  *  Creates a new HashSet for the Selection Keys. 
  */
  private void 
  initSelectionKeys()
  {
    if (pSelectionKeys == null)
      pSelectionKeys = new HashSet<String>();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The ID of the job that this modification is supposed to be applied to.
   */
  private long pJobID;
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6613450234030093576L;
}
