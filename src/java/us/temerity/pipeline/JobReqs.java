// $Id: JobReqs.java,v 1.16 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R E Q S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The requirements that a server must meet in order to be eligable to run a job. <P>
 *
 * Pipeline determines assignment of jobs to available hosts using a flexible selection 
 * criteria which selects the best job to run on each available job server slot.  Job servers
 * are considered in ascending value of the dispatch Order parameter of each server.  <P> 
 * 
 * For each available job server slot, all pending jobs in the queue are considered for 
 * execution. Each job maintaines a set of requirements which must be met before the job 
 * can be executed on a given host.  All jobs which have requirements not met by the current
 * job server are excluded from consideration.  Among the jobs which are not excluded, the
 * jobs are ranked according to selection score, job priority and finally age in the queue. 
 * See the Selection Key section for a description of how selection scores are computed. 
 * In other words, if more than one job shares the highest selection score, job priority 
 * is used to rank these jobs.  If more than one job also shares the highest priority then
 * the oldest job among these highest priority jobs will be selected to run on the 
 * available server slot. <P> 
 * 
 * Note that it is possible that none of the jobs in the queue have requirements which can
 * be met by the current available server slot.  When this occurs the slot is ignored during
 * the current dispatch cycle and the next slot is considered instead.  As mentioned before,
 * the Order parameter of job servers is used to determine the order in which job servers are
 * considered for dispatch.  If there are fewer jobs in the queue than available server slots
 * the servers with lower Order will always be busier than those of higher Order.  This can 
 * be used to prefer servers with faster CPU speeds to reduce per-job execution times. <P> 
 * 
 * The criteria used by the job dispatcher to evaluate whether a server meets the requirements
 * of each job is composed of the following three phases: <BR> 
 * 
 * <DIV style="margin-left: 40px;">
 *   License Keys <BR>
 *   <DIV style="margin-left: 40px;">
 *     Some commercial software packages are distributed under license agreements which limit
 *     the number of copies of the software which may be run at a given time.  Lack of a
 *     floating license can be a common cause of job failure.  In order to avoid these 
 *     failures, Pipeline keeps track of the number of licenses in use and will wait until 
 *     a license becomes available before scheduling a job which requires a floating license.
 *     Each job maintains a set of license keys it requires.  The job dispatcher maintains
 *     the number of available licenses for each license key.  If there is at least one 
 *     license available for each key required by the job, all of the required keys for the 
 *     job are reserved and the dispatcher proceeds to the next phase of tests.  If any of 
 *     the license keys for a job are not available, the job dispatcher excludes the job from 
 *     consideration for the current server slot. 
 *   </DIV> <BR>
 * 
 *   Dynamic Resources <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each enabled host regularly checks its system load, available memory and temporary 
 *     local disk space and communicates this information to the job dispatcher.  Each job
 *     has a maximum allowable load, minimun available memory and minimun disk space it 
 *     requires in order to run. The job dispatcher compares the job requirements with the 
 *     last known state of these dynamic resources for the host.  If any of the dynamic
 *     requirements are not met, the job dispatcher excludes the job from consideration for 
 *     the current server slot. <P> 
 * 
 *     Some jobs may take a fair amount of time to consume a majority of the system 
 *     resources which will be used by the job over its lifetime.  In these cases, the 
 *     dynamic resources sampled early in the life of the job under represent the eventual
 *     resource usage of the job.  If several job of this kind are assigned in quick 
 *     succession to a server, it will become overloaded once the jobs have reached their
 *     peak resource usage.  To prevent overloads of this kind, jobs may optionally specify
 *     a ramp-up time interval which delays the assignment of further jobs to a server while
 *     the current job is getting started.  Job servers will not be considered by the 
 *     dispatcher during the ramp-up interval of any jobs currently running on the server.
 *   </DIV> <BR>
 *   
 *   Selection Keys <BR>
 *   <DIV style="margin-left: 40px;">
 *     For each job which has not be excluded from consideration a selection score is
 *     computed.  Each server maintains a table of integer biases associated with each 
 *     selection key it supports.  If the server does not support all of the selection keys 
 *     for a given job, this job is eliminated from consideration.  For each remaining job, 
 *     the selection bias for each selection key required by the job are summed to compute 
 *     the selection score of the jobs for the current server. <P> 
 *     
 *     Note that job may require different selection keys and that keys not required by a job
 *     to not contribute to the jobs selection score.  This can be used by system 
 *     administrators to control the the distribution of jobs to servers.  For instance, a 
 *     selection key may be associated with one of several projects in production at a site. 
 *     A set of servers may then be biased to run jobs for a project by giving the project 
 *     selection key a higher bias on those machines.  Jobs which do not require this 
 *     project selection key will still be considered for the server, but will have a lower
 *     selection score since the bias for the project key will not contribute to this
 *     score.  Selection biases can also be negative. <P> 
 * 
 *     Selection keys can be used to manage non-floating software licenses.  For instance, 
 *     consider a situation where a particular software application can only be run 
 *     on a few specific hosts.  A site could create a zero-biased selection key for this 
 *     software and add it only to those hosts which are capable of running the software.
 *     Jobs which use the software could then add this key to their job requirements. This
 *     type of selection key would limit the jobs to running only on hosts which contain
 *     the application selection key.
 *   </DIV> 
 * </DIV> 
 * 
 * @see LicenseKey
 * @see SelectionKey
 */
public
class JobReqs
  implements Cloneable, Glueable, Serializable
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
  JobReqs()
  {
    pLicenseKeys   = new HashSet<String>();
    pSelectionKeys = new HashSet<String>();
  }

  /**
   * Construct a new set of job requirements. <P> 
   * 
   * @param priority 
   *    The priority of the job relative to other jobs.  
   * 
   * @param rampUp
   *    The ramp-up interval (in seconds) for the job.
   * 
   * @param maxLoad 
   *    The maxmimum system load allowed on an eligable host.
   * 
   * @param minMemory 
   *    The minimum amount of free memory (in MB) required on an eligable host.
   * 
   * @param minDisk 
   *    The minimum amount of free temporary local disk space (in MB) required on an 
   *    eligable host.
   * 
   * @param licenseKeys 
   *    The set of license keys an eligable host is required to have or <CODE>null</CODE>
   *    for none.
   * 
   * @param selectionKeys 
   *   The set of selection keys an eligable host is required to have or <CODE>null</CODE>
   *   for none.
   */ 
  public 
  JobReqs
  (
   int priority, 
   int rampUp,
   float maxLoad,              
   int minMemory,              
   int minDisk,                
   Set<String> licenseKeys,
   Set<String> selectionKeys
  )
  {
    setPriority(priority);

    setRampUp(rampUp);
    setMaxLoad(maxLoad);
    setMinMemory(minMemory);
    setMinDisk(minDisk);

    pLicenseKeys = new HashSet<String>();
    if(licenseKeys != null) 
      pLicenseKeys.addAll(licenseKeys);

    pSelectionKeys = new HashSet<String>();
    if(selectionKeys != null) 
      pSelectionKeys.addAll(selectionKeys);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   F A C T O R Y   M E T H O D S                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new set of job requirements with default settings.
   */ 
  public static JobReqs
  defaultJobReqs() 
  {
    return (new JobReqs(50, 0, 2.5f, 134217728, 67108864, 
			new HashSet<String>(), new HashSet<String>()));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the relative job priority.
   */
  public int
  getPriority()
  {
    return pPriority;
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
   int priority   
  ) 
  {
    pPriority = priority;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the ramp-up interval (in seconds).
   */
  public int
  getRampUp() 
  {
    return pRampUp;
  }

  /** 
   * Set the ramp-up interval (in seconds).
   */ 
  public void 
  setRampUp
  (
   int interval
  ) 
  {
    if(interval < 0)
      throw new IllegalArgumentException("The ramp-up interval cannot be negative!");
    pRampUp = interval;
  }


  /** 
   * Get the maximum allowable system load.
   */
  public float
  getMaxLoad() 
  {
    return pMaxLoad;
  }

  /** 
   * Set the maximum allowable system load on an eligable host.
   */ 
  public void 
  setMaxLoad
  (
   float load 
  ) 
  {
    if(load < 0.0f)
      throw new IllegalArgumentException("The maximum load cannot be negative!");
    pMaxLoad = load;
  }

  
  /**
   * Get the minimum amount of free memory (in bytes) required on an eligable host.
   */ 
  public long 
  getMinMemory() 
  {
    return pMinMemory;
  }

  /**
   * Set the minimum amount of free memory (in bytes) required on an eligable host.
   * 
   */ 
  public void 
  setMinMemory
  (
   long bytes
  ) 
  {
    if(bytes < 0)
      throw new IllegalArgumentException("The minimum free memory cannot be negative!");
    pMinMemory = bytes;
  }
  

  /** 
   * Get the minimum amount of free temporary local disk space (in bytes) required on an 
   * eligable host.
   */
  public long 
  getMinDisk() 
  {
    return pMinDisk;
  }

  /** 
   * Set the minimum amount of free temporary local disk space (in bytes) required on an 
   * eligable host.
   */
  public void 
  setMinDisk
  (
   long bytes
  ) 
  {
    if(bytes < 0)
      throw new IllegalArgumentException("The minimum free disk space cannot be negative!");
    pMinDisk = bytes;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the required license keys. 
   */
  public Set<String>
  getLicenseKeys()
  {
    return Collections.unmodifiableSet(pLicenseKeys);
  }

  /** 
   * Add the named key to the set of required license keys.
   *
   * @param key 
   *    The name of the license key to add.
   */
  public void
  addLicenseKey
  (
   String key
  ) 
  {
    pLicenseKeys.add(key);
  }

  /** 
   * Add all of the given named keys to the set of required license keys.
   *
   * @param keys 
   *    The names of the license keys to add.
   */
  public void
  addLicenseKeys
  (
   Set<String> keys
  ) 
  {
    pLicenseKeys.addAll(keys);
  }

  /** 
   * Remove the named key from the set of required license keys.
   *
   * @param key 
   *    The name of the license key to remove.
   */
  public void
  removeLicenseKey
  (
   String key
  ) 
  {
    pLicenseKeys.remove(key);
  }

  /** 
   * Remove all of the named keys from the set of required license keys.
   *
   * @param keys 
   *    The names of the license keys to remove.
   */
  public void
  removeLicenseKeys
  (
   Set<String> keys
  ) 
  {
    pLicenseKeys.removeAll(keys);
  }

  /** 
   * Remove all required license keys.
    */
  public void
  removeAllLicenseKeys() 
  {
    pLicenseKeys.clear();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the required selection keys. 
   */
  public Set<String>
  getSelectionKeys()
  {
    return Collections.unmodifiableSet(pSelectionKeys);
  }

  /** 
   * Add the named key to the set of required selection keys.
   *
   * @param key 
   *    The name of the selection key to add.
   */
  public void
  addSelectionKey
  (
   String key
  ) 
  {
    pSelectionKeys.add(key);
  }

  /** 
   * Add all of the given named keys to the set of required selection keys.
   *
   * @param keys 
   *    The names of the selection keys to add.
   */
  public void
  addSelectionKeys
  (
   Set<String> keys
  ) 
  {
    pSelectionKeys.addAll(keys);
  }

  /** 
   * Remove the named key from the set of required selection keys.
   *
   * @param key 
   *    The name of the selection key to remove.
   */
  public void
  removeSelectionKey
  (
   String key
  ) 
  {
    pSelectionKeys.remove(key);
  }

  /** 
   * Remove all of the named keys from the set of required selection keys.
   *
   * @param keys 
   *    The names of the selection keys to remove.
   */
  public void
  removeSelectionKeys
  (
   Set<String> keys
  ) 
  {
    pSelectionKeys.removeAll(keys);
  }

  /** 
   * Remove all required selection keys.
    */
  public void
  removeAllSelectionKeys() 
  {
    pSelectionKeys.clear();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof JobReqs)) {
      JobReqs reqs = (JobReqs) obj;

      if((pPriority == reqs.pPriority) &&
	 (pRampUp == reqs.pRampUp) && 
	 (pMaxLoad == reqs.pMaxLoad) && 
	 (pMinMemory == reqs.pMinMemory) && 
	 (pMinDisk == reqs.pMinDisk) && 
	 pLicenseKeys.equals(reqs.pLicenseKeys) &&
	 pSelectionKeys.equals(reqs.pSelectionKeys))
	return true;
    }

    return false;
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    try {
      JobReqs clone = (JobReqs) super.clone();

      clone.pLicenseKeys   = new HashSet<String>(pLicenseKeys);
      clone.pSelectionKeys = new HashSet<String>(pSelectionKeys);
      
      return clone; 
    }
    catch(CloneNotSupportedException ex) {
      throw new IllegalStateException();       
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("Priority",  pPriority);
    encoder.encode("RampUp",    pRampUp);
    encoder.encode("MaxLoad",   pMaxLoad);
    encoder.encode("MinMemory", pMinMemory);
    encoder.encode("MinDisk",   pMinDisk);

    if(!pLicenseKeys.isEmpty())
      encoder.encode("LicenseKeys", pLicenseKeys);

    if(!pSelectionKeys.isEmpty())
      encoder.encode("SelectionKeys", pSelectionKeys);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Integer priority = (Integer) decoder.decode("Priority");
    if(priority == null) 
      throw new GlueException("The \"Priority\" was missing!");
    pPriority = priority;

    Integer interval = (Integer) decoder.decode("RampUp");
    if(interval == null) 
      interval = 0;
    pRampUp = interval;

    Float load = (Float) decoder.decode("MaxLoad");
    if(load == null) 
      throw new GlueException("The \"MaxLoad\" was missing!");
    pMaxLoad = load;
    
    Long mem = (Long) decoder.decode("MinMemory");
    if(mem == null) 
      throw new GlueException("The \"MinMemory\" was missing!");
    pMinMemory = mem;
    
    Long disk = (Long) decoder.decode("MinDisk");
    if(disk == null) 
      throw new GlueException("The \"MinDisk\" was missing!");
    pMinDisk = disk;

    {
      HashSet<String> keys = (HashSet<String>) decoder.decode("LicenseKeys");
      if(keys != null) 
	pLicenseKeys = keys;
    }

    {
      HashSet<String> keys = (HashSet<String>) decoder.decode("SelectionKeys");
      if(keys != null) 
	pSelectionKeys = keys;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5597354970617647694L;


 
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The relative job priority.
   */
  private int  pPriority;
 
  /**
   * The ramp-up interval (in seconds).
   */
  private int  pRampUp;
 
  /**
   * The maximum allowable system load on an eligable host.
   */
  private float  pMaxLoad;    
 
  /**
   * The minimum amount of free memory (in bytes) required on an eligable host.
   */      
  private long  pMinMemory;  

  /**
   * The minimum amount of free temporary local disk space (in bytes) required on an 
   * eligable host.
   */       
  private long  pMinDisk;           
					  

  /**
   * The names of the required license keys. 
   */
  private HashSet<String>  pLicenseKeys;

  /**
   * The names of the required selection keys. 
   */
  private HashSet<String>  pSelectionKeys;

}
