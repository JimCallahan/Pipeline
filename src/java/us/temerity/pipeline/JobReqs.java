// $Id: JobReqs.java,v 1.6 2004/03/23 07:40:37 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R E Q S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The requirements that a server must meet in order to be eligable to run a job. <P>
 *
 * Pipeline determines assignment of jobs to available hosts based on a flexible selection 
 * criteria which compares job requirements with available host resources.  The job dispatcher
 * processes jobs in the order of their priority and in order of their submission among jobs
 * of equal priority. Each job has a set of requirements which a prospective host must satisfy
 * in order for the job to be run on the host. The job dispatcher compares each job 
 * against the enabled hosts and the host which best satisfies the requirements is assigned 
 * the job and the job is removed from the queue. The dispatcher then proceeds to the next 
 * job in the queue.  <P> 
 * 
 * If no hosts satisfy the requirements for a job, the job is left in the queue and the 
 * dispatcher goes on to process the next job.  Once the job dispatcher reaches the end of 
 * the queue, it starts again at the head of the queue and tries again to find a host which 
 * satisfies each job's requirements. <P> 
 * 
 * The criteria used by the job dispatcher to evaluate whether a host meets the job's
 * requirements is composed of the following three phases: <BR> 
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
 *     the license keys for a job are not available, the job dispatcher leaves the current 
 *     job in the queue and proceeds to test the next job.
 *   </DIV> <BR>

 *   Dynamic Resources <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each enabled host regularly checks its system load, available memory and temporary 
 *     local disk space and communicates this information to the job dispatcher.  Each job
 *     has a maximum allowable load, minimun available memory and minimun disk space it 
 *     requires in order to run. The job dispatcher compares the job requirements with the 
 *     last known state of these dynamic resources for the host.  If all of the dynamic
 *     requirements are met, the job dispatcher adds the server to its list of candidate 
 *     hosts for the job.  If after all hosts are processed, there are no candidate hosts
 *     which meet the dynamic requirements, the current job is left in the queue and the 
 *     dispatcher proceeds the next job. <BR>
 *   </DIV> <BR>
 *   
 *   Selection Keys <BR>
 *   <DIV style="margin-left: 40px;">
 *     This last phase selects a single host from the list of candidate hosts to run 
 *     the job.  Each job maintains a set of selection keys.  Each hosts maintains a 
 *     table of integer biases associated with each selection key it supports.  Any host
 *     which does not support all of the selection keys for the job is eliminated from 
 *     the list of candidate hosts.  For each remaining host, the selection biases 
 *     associated with each selection key held by the job are summed to compute an 
 *     overall selection bias.  The host which has the highest overall selection bias
 *     is assigned the job. <P> 
 *     
 *     Note that two hosts which support the same set of selection keys may have different
 *     biases for the same selection key and therefore different overall biases for a given
 *     job. The per-host selection biases are set by system administrators to control the 
 *     the distribution of jobs to hosts.  For instance, a selection key may be associated
 *     with one of several projects in production at a site. A set of hosts may then be
 *     biased to run jobs for a project by giving the project selection key a higher bias 
 *     on those machines. <P>
 * 
 *     Selection keys can also be used to manage non-floating software licenses.  For
 *     instance, consider a situation where a particular piece of software can only be run 
 *     on a few specific hosts.  A site could create a zero-biased selection key for this 
 *     software and add it only to those hosts which are capable of running the software.
 *     Jobs which use the software could then add this key to their job requirements. This
 *     type of selection key would limit the jobs to running only on hosts which are capable
 *     of running the software. 
 *   </DIV> 
 * </DIV> 
 */
public
class JobReqs
implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

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
   *    The set of license keys an eligable host is required to have.
   * 
   * @param selectionKeys 
   *   The set of selection keys an eligable host is required to have.
   */ 
  public 
  JobReqs
  (
   int priority, 
   float maxLoad,              
   int minMemory,              
   int minDisk,                
   Set<String> licenseKeys,
   Set<String> selectionKeys
  )
  {
    setPriority(priority);

    setMaxLoad(maxLoad);
    setMinMemory(minMemory);
    setMinDisk(minDisk);

    pLicenseKeys   = new HashSet<String>(licenseKeys);
    pSelectionKeys = new HashSet<String>(selectionKeys);
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
    return (new JobReqs(0, 2.5f, 128, 64, new HashSet<String>(), new HashSet<String>()));
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
   * Get the maximum allowable system load.
   */
  public float
  getMaxLoad() 
  {
    return pMaxLoad;
  }

  /** 
   * Set the maximum allowable system load on an eligable host.
   * 
   * @param load 
   *    The maxmimum system load allowed.
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
   * Get the minimum amount of free memory (in MB) required on an eligable host.
   */ 
  public int 
  getMinMemory() 
  {
    return pMinMemory;
  }

  /**
   * Set the minimum amount of free memory (in MB) required on an eligable host.
   * 
   * @param megs 
   *    The minimum amount of free memory.
   */ 
  public void 
  setMinMemory
  (
   int megs
  ) 
  {
    if(megs < 0)
      throw new IllegalArgumentException("The minimum free memory cannot be negative!");
    pMinMemory = megs;
  }
  

  /** 
   * Get the minimum amount of free temporary local disk space (in MB) required on an 
   * eligable host.
   */
  public int 
  getMinDisk() 
  {
    return pMinDisk;
  }

  /** 
   * Set the minimum amount of free temporary local disk space (in MB) required on an 
   * eligable host.
   *
   * @param megs 
   *    The minimum amount of free temporary local disk space.
   */
  public void 
  setMinDisk
  (
   int megs
  ) 
  {
    if(megs < 0)
      throw new IllegalArgumentException("The minimum free disk space cannot be negative!");
    pMinDisk = megs;
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
    throws CloneNotSupportedException
  {
    JobReqs clone = (JobReqs) super.clone();

    clone.pLicenseKeys   = new HashSet<String>(pLicenseKeys);
    clone.pSelectionKeys = new HashSet<String>(pSelectionKeys);

    return clone;
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

    Float load = (Float) decoder.decode("MaxLoad");
    if(load == null) 
      throw new GlueException("The \"MaxLoad\" was missing!");
    pMaxLoad = load;
    
    Integer mem = (Integer) decoder.decode("MinMemory");
    if(mem == null) 
      throw new GlueException("The \"MinMemory\" was missing!");
    pMinMemory = mem;
    
    Integer disk = (Integer) decoder.decode("MinDisk");
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
   * The maximum allowable system load on an eligable host.
   */
  private float  pMaxLoad;    
 
  /**
   * The minimum amount of free memory (in MB) required on an eligable host.
   */      
  private int  pMinMemory;  

  /**
   * The minimum amount of free temporary local disk space (in MB) required on an 
   * eligable host.
   */       
  private int  pMinDisk;           
					  

  /**
   * The names of the required license keys. 
   */
  private HashSet<String>  pLicenseKeys;

  /**
   * The names of the required selection keys. 
   */
  private HashSet<String>  pSelectionKeys;

}
