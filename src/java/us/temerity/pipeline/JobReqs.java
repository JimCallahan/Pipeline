// $Id: JobReqs.java,v 1.22 2007/11/30 20:14:23 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R E Q S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * The requirements that a server must meet in order to be eligible to run a job. <P>
 *
 * Pipeline determines assignment of jobs to available hosts using a flexible selection 
 * criteria which selects the best job to run on each available job server slot.  Job servers
 * are considered in ascending value of the dispatch Order parameter of each server.  <P> 
 * 
 * For each available job server slot, all pending jobs in the queue are considered for 
 * execution. Each job maintains a set of requirements which must be met before the job 
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
  extends JobReqsCommon
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
    super();
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
   *    The maximum system load allowed on an eligible host.
   * 
   * @param minMemory 
   *    The minimum amount of free memory (in bytes) required on an eligible host.
   * 
   * @param minDisk 
   *    The minimum amount of free temporary local disk space (in bytes) required on an 
   *    eligible host.
   * 
   * @param licenseKeys 
   *    The set of license keys an eligible host is required to have or <CODE>null</CODE>
   *    for none.
   * 
   * @param selectionKeys 
   *   The set of selection keys an eligible host is required to have or <CODE>null</CODE>
   *   for none.
   *   
   * @param hardwareKeys 
   *   The set of hardware keys an eligible host is required to have or <CODE>null</CODE>
   *   for none.
   */ 
  public 
  JobReqs
  (
   int priority, 
   int rampUp,
   float maxLoad,              
   long minMemory,              
   long minDisk,                
   Set<String> licenseKeys,
   Set<String> selectionKeys,
   Set<String> hardwareKeys
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
    
    pHardwareKeys = new HashSet<String>();
    if(hardwareKeys != null) 
      pHardwareKeys.addAll(hardwareKeys);
  }
  
  /**
   * Construct a new set of job requirements, using the values in the existing JobReq
   * as a base and overriding them wherever non-null values are passed in. <P> 
   * 
   * @param reqs
   *    The base JobReqs whose values will be used when no alternative is specified.
   * 
   * @param delta
   *    The changes that should be made to the base JobReqs.
   */ 
  public 
  JobReqs
  (
   JobReqs reqs,
   JobReqsDelta delta
  )
  {
    if (delta.getPriority() != null )
      setPriority(delta.getPriority());
    else
      setPriority(reqs.getPriority());

    if (delta.getRampUp() != null)
     setRampUp(delta.getRampUp());
    else
      setRampUp(reqs.getRampUp());
    
    if (delta.getMaxLoad() != null)
      setMaxLoad(delta.getMaxLoad());
    else
      setMaxLoad(reqs.getMaxLoad());
    
    if (delta.getMinMemory() != null)
      setMinMemory(delta.getMinMemory());
    else
      setMinMemory(reqs.getMinMemory());
    
    if (delta.getMinDisk() != null)
      setMinDisk(delta.getMinDisk());
    else
      setMinDisk(reqs.getMinDisk());

    pLicenseKeys = new HashSet<String>();
    if(delta.getLicenseKeys() != null) 
      pLicenseKeys.addAll(delta.getLicenseKeys());
    else if (reqs.getLicenseKeys().size() > 0)
      pLicenseKeys.addAll(reqs.getLicenseKeys());

    pSelectionKeys = new HashSet<String>();
    if(delta.getSelectionKeys() != null) 
      pSelectionKeys.addAll(delta.getSelectionKeys());
    else if (reqs.getSelectionKeys().size() > 0)
      pSelectionKeys.addAll(reqs.getSelectionKeys());
    
    pHardwareKeys = new HashSet<String>();
    if(delta.getHardwareKeys() != null) 
      pHardwareKeys.addAll(delta.getHardwareKeys());
    else if (reqs.getHardwareKeys().size() > 0)
      pHardwareKeys.addAll(reqs.getHardwareKeys());
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
    return (new JobReqs(50, 0, 2.5f, 134217728L, 67108864L, 
			new HashSet<String>(), new HashSet<String>(), new HashSet<String>()));
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

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
   * Set the maximum allowable system load on an eligible host.
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
   * Set the minimum amount of free memory (in bytes) required on an eligible host.
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
   * Set the minimum amount of free temporary local disk space (in bytes) required on an 
   * eligible host.
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

      if((pPriority.equals(reqs.pPriority)) &&
	 (pRampUp.equals(reqs.pRampUp)) && 
	 (pMaxLoad.equals(reqs.pMaxLoad)) && 
	 (pMinMemory.equals(reqs.pMinMemory)) && 
	 (pMinDisk.equals(reqs.pMinDisk)) && 
	 pLicenseKeys.equals(reqs.pLicenseKeys) &&
	 pSelectionKeys.equals(reqs.pSelectionKeys) &&
	 pHardwareKeys.equals(reqs.pHardwareKeys))
	return true;
    }
    
    return false;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5597354970617647694L;
}
