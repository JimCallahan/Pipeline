// $Id: QueueMgrClient.java,v 1.11 2004/09/03 01:51:48 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C L I E N T                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plqueuemgr</B><A>(1).  
 */
public
class QueueMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>plqueuemgr</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plqueuemgr</B>(1).
   */
  public
  QueueMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  QueueRequest.Disconnect, QueueRequest.Shutdown);
  }

  /** 
   * Construct a new queue manager client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--queue-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--queue-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  QueueMgrClient() 
  {
    super(PackageInfo.sQueueServer, PackageInfo.sQueuePort, 
	  QueueRequest.Disconnect, QueueRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R I V I L E G E D   U S E R S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the names of the privileged users. <P> 
   * 
   * Privileged users are allowed to perform operations which are restricted for normal
   * users. In general privileged access is required when an operation is dangerous or 
   * involves making changes which affect all users. The "pipeline" user is always 
   * privileged. <P> 
   * 
   * This operation updates the queue managers cache of the privileged users.  The master
   * manager will call this method whenever the set of privileged users is modified.
   * 
   * @param users
   *    The names of the privileged users.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized void
  setPrivilegedUsers
  (
   TreeSet<String> users
  ) 
    throws PipelineException
  {
    pPrivilegedUsers = null;

    verifyConnection();

    MiscSetPrivilegedUsersReq req = new MiscSetPrivilegedUsersReq(users);

    Object obj = performTransaction(QueueRequest.SetPrivilegedUsers, req);
    handleSimpleResponse(obj);
  }

  /**
   * Get the names of the privileged users. <P> 
   * 
   * Privileged users are allowed to perform operations which are restricted for normal
   * users. In general privileged access is required when an operation is dangerous or 
   * involves making changes which affect all users. The "pipeline" user is always 
   * privileged. <P> 
   * 
   * Each client caches the set of privileged users recieved from the master server the 
   * first time this method is called and uses this cache instead of network communication
   * for subsequent calls.  This cache can be ignored and rebuilt if the <CODE>useCache</CODE>
   * argument is set to <CODE>false</CODE>.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized TreeSet<String> 
  getPrivilegedUsers
  (
   boolean useCache   
  ) 
    throws PipelineException
  {
    if(!useCache || (pPrivilegedUsers == null)) 
      updatePrivilegedUsers();

    return new TreeSet<String>(pPrivilegedUsers);
  }

  /**
   * Is the given user privileged? <P> 
   * 
   * Privileged users are allowed to perform operations which are restricted for normal
   * users. In general privileged access is required when an operation is dangerous or 
   * involves making changes which affect all users. The "pipeline" user is always 
   * privileged. <P> 
   * 
   * Each client caches the set of privileged users recieved from the master server the 
   * first time this method is called and uses this cache instead of network communication
   * for subsequent calls.  This cache can be ignored and rebuilt if the <CODE>useCache</CODE>
   * argument is set to <CODE>false</CODE>.
   * 
   * @param author
   *   The user in question.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  (
   String author, 
   boolean useCache
  ) 
    throws PipelineException
  {
    if(author.equals(PackageInfo.sPipelineUser)) 
      return true;

    if(!useCache || (pPrivilegedUsers == null)) 
      updatePrivilegedUsers();
    assert(pPrivilegedUsers != null);

    return pPrivilegedUsers.contains(author);
  }

  /**
   * Is the given user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with a <CODE>useCache</CODE> argument of <CODE>true</CODE>.
   * 
   * @param author
   *   The user in question.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  (
   String author
  ) 
    throws PipelineException
  {
    return isPrivileged(author, true);
  }

  /**
   * Is the current user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with the current user as the <CODE>author</CODE> argument.
   * 
   * @param useCache
   *   Should the local cache be used to determine whether the current user is privileged?
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged
  ( 
   boolean useCache
  ) 
    throws PipelineException
  {
    return isPrivileged(PackageInfo.sUser, useCache);
  }

  /**
   * Is the current user privileged? <P> 
   * 
   * Identical to calling {@link #isPrivileged(String,boolean) isPrivileged}
   * with the current user as the <CODE>author</CODE> argument and a <CODE>useCache</CODE> 
   * argument of <CODE>true</CODE>.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  public synchronized boolean 
  isPrivileged() 
    throws PipelineException
  {
    return isPrivileged(PackageInfo.sUser, true);
  }

  /**
   * Update the local cache of privileged users.
   * 
   * @throws PipelineException
   *   If unable to determine the privileged users.
   */ 
  private synchronized void 
  updatePrivilegedUsers() 
    throws PipelineException
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetPrivilegedUsers, null);
    if(obj instanceof MiscGetPrivilegedUsersRsp) {
      MiscGetPrivilegedUsersRsp rsp = (MiscGetPrivilegedUsersRsp) obj;
      pPrivilegedUsers = rsp.getUsers();
    }
    else {
      handleFailure(obj);
      return;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I C E N S E   K E Y S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined license keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the license keys.
   */
  public synchronized TreeSet<String>
  getLicenseKeyNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetLicenseKeyNames, null);
    if(obj instanceof QueueGetLicenseKeyNamesRsp) {
      QueueGetLicenseKeyNamesRsp rsp = (QueueGetLicenseKeyNamesRsp) obj;
      return rsp.getKeyNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the currently defined license keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the license keys.
   */
  public synchronized ArrayList<LicenseKey>
  getLicenseKeys() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetLicenseKeys, null);
    if(obj instanceof QueueGetLicenseKeysRsp) {
      QueueGetLicenseKeysRsp rsp = (QueueGetLicenseKeysRsp) obj;
      return rsp.getKeys();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add the given license key to the currently defined license keys. <P> 
   * 
   * If a license key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param key
   *   The license key to add.
   * 
   * @throws PipelineException
   *   If unable to add the license key.
   */ 
  public synchronized void
  addLicenseKey
  (
   LicenseKey key
  ) 
    throws PipelineException  
  {
    if(!isPrivileged()) 
      throw new PipelineException
	("Only privileged users may add license keys!");

    verifyConnection();

    QueueAddLicenseKeyReq req = new QueueAddLicenseKeyReq(key);
    Object obj = performTransaction(QueueRequest.AddLicenseKey, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Remove the license key with the given name from currently defined license keys. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param kname
   *   The name of the license key to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the license key.
   */ 
  public synchronized void
  removeLicenseKey
  (
   String kname
  ) 
    throws PipelineException  
  {
    if(!isPrivileged()) 
      throw new PipelineException
	("Only privileged users may remove license keys!");
    
    verifyConnection();

    QueueRemoveLicenseKeyReq req = new QueueRemoveLicenseKeyReq(kname);
    Object obj = performTransaction(QueueRequest.RemoveLicenseKey, req); 
    handleSimpleResponse(obj);
  }  
  
  /**
   * Set the total number of licenses associated with the named license key. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param kname
   *   The name of the license key.
   * 
   * @param total 
   *   The total number of licenses.
   * 
   * @throws PipelineException
   *   If unable to set the license total for the given license key.
   */ 
  public synchronized void
  setTotalLicenses
  (
   String kname, 
   int total   
  ) 
    throws PipelineException  
  {
    if(!isPrivileged(false)) 
      throw new PipelineException
	("Only privileged users may set the total number of licenses!");

    verifyConnection();

    QueueSetTotalLicensesReq req = new QueueSetTotalLicensesReq(kname, total);
    Object obj = performTransaction(QueueRequest.SetTotalLicenses, req); 
    handleSimpleResponse(obj);    
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N   K E Y S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined selection keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the selection keys.
   */
  public synchronized TreeSet<String>
  getSelectionKeyNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionKeyNames, null);
    if(obj instanceof QueueGetSelectionKeyNamesRsp) {
      QueueGetSelectionKeyNamesRsp rsp = (QueueGetSelectionKeyNamesRsp) obj;
      return rsp.getKeyNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the currently defined selection keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the selection keys.
   */
  public synchronized ArrayList<SelectionKey>
  getSelectionKeys() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionKeys, null);
    if(obj instanceof QueueGetSelectionKeysRsp) {
      QueueGetSelectionKeysRsp rsp = (QueueGetSelectionKeysRsp) obj;
      return rsp.getKeys();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add the given selection key to the currently defined selection keys. <P> 
   * 
   * If a selection key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param key
   *   The selection key to add.
   * 
   * @throws PipelineException
   *   If unable to add the selection key.
   */ 
  public synchronized void
  addSelectionKey
  (
   SelectionKey key
  ) 
    throws PipelineException  
  {
    if(!isPrivileged()) 
      throw new PipelineException
	("Only privileged users may add selection keys!");

    verifyConnection();

    QueueAddSelectionKeyReq req = new QueueAddSelectionKeyReq(key);
    Object obj = performTransaction(QueueRequest.AddSelectionKey, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Remove the selection key with the given name from currently defined selection keys. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param kname
   *   The name of the selection key to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the selection key.
   */ 
  public synchronized void
  removeSelectionKey
  (
   String kname
  ) 
    throws PipelineException  
  {
    if(!isPrivileged()) 
      throw new PipelineException
	("Only privileged users may remove selection keys!");
    
    verifyConnection();

    QueueRemoveSelectionKeyReq req = new QueueRemoveSelectionKeyReq(kname);
    Object obj = performTransaction(QueueRequest.RemoveSelectionKey, req); 
    handleSimpleResponse(obj);
  }  


   
  /*----------------------------------------------------------------------------------------*/
  /*   J O B   M A N A G E R   H O S T S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts capable of executing jobs for the Pipeline queue. <P> 
   * 
   * In other words, the hosts which run the <B>pljobmgr</B>(1) daemon.
   * 
   * @return 
   *   The per-host information indexed by fully resolved host name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host information.
   */
  public synchronized TreeMap<String,QueueHost>
  getHosts()
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetHosts, null);
    if(obj instanceof QueueGetHostsRsp) {
      QueueGetHostsRsp rsp = (QueueGetHostsRsp) obj;
      return rsp.getHosts();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add a new execution host to the Pipeline queue. <P> 
   * 
   * The host will be added in a <CODE>Shutdown</CODE> state, unreserved and with no 
   * selection key biases.  If a host already exists with the given name, an exception 
   * will be thrown instead. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @throws PipelineException
   *   If unable to add the host.
   */ 
  public synchronized void 
  addHost
  (
   String hostname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddHostReq req = new QueueAddHostReq(hostname);
    Object obj = performTransaction(QueueRequest.AddHost, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing execution hosts from the Pipeline queue. <P> 
   * 
   * The hosts must be in a <CODE>Shutdown</CODE> state before they can be removed. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param hostnames
   *   The fully resolved names of the hosts.
   * 
   * @throws PipelineException
   *   If unable to remove the hosts.
   */ 
  public synchronized void 
  removeHosts
  (
   TreeSet<String> hostnames
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueRemoveHostsReq req = new QueueRemoveHostsReq(hostnames);
    Object obj = performTransaction(QueueRequest.RemoveHosts, req);
    handleSimpleResponse(obj);
  }

  /**
   * Change the status, user reservations, job slots and/or selection key biases
   * of the given hosts. <P> 
   * 
   * Any of the arguments may be <CODE>null</CODE> if no changes are do be made for
   * the type of host property the argument controls. <P> 
   * 
   * A <B>pljobmgr</B>(1) daemon must be running on a host before its status can be 
   * changed to <CODE>Disabled</CODE> or <CODE>Enabled</CODE>.  If <B>plqueuemgr<B>(1) cannot
   * establish a network connection to a <B>pljobmgr<B>(1) daemon running on the host, the 
   * status will be overridden and changed to <CODE>Shutdown</CODE>.
   * 
   * If the new status for a host is <CODE>Shutdown</CODE> and there is a <B>pljobmgr<B>(1) 
   * daemon running on the host, it will be stopped and any job it is currently running will
   * be killed. <P> 
   * 
   * When a host is reserved, only jobs submitted by the reserving user will be assigned
   * to the host.  The reservation can be cleared by setting the reserving user name to
   * <CODE>null</CODE> for the host in the <CODE>reservations</CODE> argument. <P> 
   * 
   * Each host has a maximum number of jobs which it can be assigned at any one time 
   * regardless of the other limits on system resources.  This method allows the number
   * of slots to be changed for a number of hosts at once.  The number of slots cannot 
   * be negative and probably should not be set considerably higher than the number of 
   * CPUs on the host.  A good value is probably (1.5 * number of CPUs). <P>
   * 
   * For the given hosts, the selection key biases are set to the values passed in the 
   * <CODE>biases</CODE> argument and all selection key biases not mentioned will be removed.
   * Hosts not mention in the <CODE>biases</CODE> argument will be unaltered. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param status
   *   The new host status indexed by fully resolved names of the hosts.
   * 
   * @param reservations
   *   The names of reserving users indexed by fully resolved names of the hosts.
   * 
   * @param slots 
   *   The number of job slots indexed by fully resolved names of the hosts.
   * 
   * @param biases
   *   The selection key biases indexed by fully resolved host name and selection key name.
   * 
   * @throws PipelineException 
   *   If unable to change the status of the hosts.
   */
  public synchronized void
  editHosts
  (
   TreeMap<String,QueueHost.Status> status, 
   TreeMap<String,String> reservations, 
   TreeMap<String,Integer> slots, 
   TreeMap<String,TreeMap<String,Integer>> biases
  ) 
    throws PipelineException  
  {
    if(!isPrivileged()) 
      throw new PipelineException
	("Only privileged users may edit the properties of the job server hosts!");
    
    verifyConnection();

    QueueEditHostsReq req = new QueueEditHostsReq(status, reservations, slots, biases);
    Object obj = performTransaction(QueueRequest.EditHosts, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Get the full system resource usage history of the given host.
   * 
   * @param hostname
   *   The fully resolved name of the host.
   * 
   * @return 
   *   The resource usage samples.
   * 
   * @throws PipelineException
   *   If unable to lookup the resource usage.
   */ 
  public synchronized ResourceSampleBlock
  getHostResourceSamples
  (
   String hostname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetHostResourceSamplesReq req = new QueueGetHostResourceSamplesReq(hostname);
    Object obj = performTransaction(QueueRequest.GetHostResourceSamples, req);
    if(obj instanceof QueueGetHostResourceSamplesRsp) {
      QueueGetHostResourceSamplesRsp rsp = (QueueGetHostResourceSamplesRsp) obj;
      return rsp.getSamples();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B S                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the JobStatus of all jobs associated with the given job group IDs. 
   * 
   * @param groupIDs
   *   The unique job group IDs.
   * 
   * @throws PipelineException
   *   If unable to determine the job status.
   */ 
  public synchronized TreeMap<Long,JobStatus>
  getJobStatus
  (
   TreeSet<Long> groupIDs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobStatusReq req = new QueueGetJobStatusReq(groupIDs);
    
    Object obj = performTransaction(QueueRequest.GetJobStatus, req);
    if(obj instanceof QueueGetJobStatusRsp) {
      QueueGetJobStatusRsp rsp = (QueueGetJobStatusRsp) obj;
      return rsp.getStatus();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the job with the given ID.
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @throws PipelineException
   *   If no job exists with the given ID.
   */ 
  public synchronized QueueJob
  getJob
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobReq req = new QueueGetJobReq(jobID);

    Object obj = performTransaction(QueueRequest.GetJob, req);
    if(obj instanceof QueueGetJobRsp) {
      QueueGetJobRsp rsp = (QueueGetJobRsp) obj;
      return rsp.getJob();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get information about the current status of a job in the queue. <P> 
   * 
   * @param jobID
   *   The unique job identifier.
   * 
   * @throws PipelineException
   *   If no job exists with the given ID.
   */ 
  public synchronized QueueJobInfo
  getJobInfo
  (
   long jobID
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobInfoReq req = new QueueGetJobInfoReq(jobID);

    Object obj = performTransaction(QueueRequest.GetJobInfo, req);
    if(obj instanceof QueueGetJobInfoRsp) {
      QueueGetJobInfoRsp rsp = (QueueGetJobInfoRsp) obj;
      return rsp.getJobInfo();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Kill the jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the jobs.
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to kill the jobs.
   */  
  public synchronized void
  killJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may jobs owned by another user!");

    verifyConnection();

    QueueKillJobsReq req = new QueueKillJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.KillJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Pause the jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the jobs.
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to pause the jobs.
   */  
  public synchronized void
  pauseJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may pause jobs owned by another user!");

    verifyConnection();

    QueuePauseJobsReq req = new QueuePauseJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.PauseJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Resume execution of the paused jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the jobs.
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to resume the jobs.
   */  
  public synchronized void
  resumeJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may resume jobs owned by another user!");

    verifyConnection();

    QueueResumeJobsReq req = new QueueResumeJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.ResumeJobs, req); 
    handleSimpleResponse(obj);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Notify the queue that a set of previously submitted jobs make up a job group.
   * 
   * @param group
   *   The queue job group.
   * 
   * @throws PipelineException
   *   If unable to group the jobs.
   */ 
  public synchronized void 
  groupJobs
  (
   QueueJobGroup group
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGroupJobsReq req = new QueueGroupJobsReq(group);
    Object obj = performTransaction(QueueRequest.GroupJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Get the job group with the given ID. 
   * 
   * @param groupID
   *   The unique job group identifier.
   * 
   * @throws PipelineException
   *   If no job group exists with the given ID.
   */ 
  public synchronized QueueJobGroup
  getJobGroup
  (
   long groupID
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobGroupReq req = new QueueGetJobGroupReq(groupID);

    Object obj = performTransaction(QueueRequest.GetJobGroup, req);
    if(obj instanceof QueueGetJobGroupRsp) {
      QueueGetJobGroupRsp rsp = (QueueGetJobGroupRsp) obj;
      return rsp.getJobGroup();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }
  
  /**
   * Get all of the existing job groups.
   * 
   * @throws PipelineException
   *   If no job groups exist.
   */ 
  public synchronized TreeMap<Long,QueueJobGroup>
  getJobGroups()
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetJobGroups, null);
    if(obj instanceof QueueGetJobGroupsRsp) {
      QueueGetJobGroupsRsp rsp = (QueueGetJobGroupsRsp) obj;
      return rsp.getJobGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Delete the completed job groups. <P> 
   * 
   * The authors specified by the <CODE>groupAuthors</CODE> argument must match the user 
   * who submitted the groups. <P> 
   * 
   * If any of the authors specified by the <CODE>groupAuthors</CODE> argument are different 
   * than the current user, this method will fail unless the current user has privileged 
   * access status.
   * 
   * @param groupAuthors
   *   The name of the user which submitted the group indexed by job group ID.
   * 
   * @throws PipelineException
   *   If no job group exists with the given ID or if the job group is not completed.
   */ 
  public synchronized void
  deleteJobGroups
  (
   TreeMap<Long,String> groupAuthors
  ) 
    throws PipelineException  
  {
    if(!isPrivileged(false)) {
      for(String author : groupAuthors.values()) 
	if(!PackageInfo.sUser.equals(author))
	  throw new PipelineException
	    ("Only privileged users may delete job groups owned by another user!");
    }

    verifyConnection();

    QueueDeleteJobGroupsReq req = new QueueDeleteJobGroupsReq(groupAuthors);

    Object obj = performTransaction(QueueRequest.DeleteJobGroups, req);
    handleSimpleResponse(obj);
  }

  /**
   * Delete all of the completed job groups created in the given working area. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param author 
   *   The name of the user which owns the job groups.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @throws PipelineException
   *   If unable to delete the job groups.
   */ 
  public synchronized void
  deleteViewJobGroups
  ( 
   String author, 
   String view
  ) 
    throws PipelineException  
  {
    if(!PackageInfo.sUser.equals(author) && !isPrivileged(false))
      throw new PipelineException
	("Only privileged users may delete job groups owned by another user!");

    verifyConnection();

    QueueDeleteViewJobGroupsReq req = new QueueDeleteViewJobGroupsReq(author, view);

    Object obj = performTransaction(QueueRequest.DeleteViewJobGroups, req);
    handleSimpleResponse(obj);
  }

  /**
   * Delete all of the completed job groups in all working areas. <P> 
   * 
   * This method will fail unless the current user has privileged access status.
   * 
   * @throws PipelineException
   *   If unable to delete the job groups.
   */ 
  public synchronized void
  deleteAllJobGroups() 
    throws PipelineException  
  {
    if(!isPrivileged(false))
      throw new PipelineException
	("Only privileged users may delete job groups owned by another user!");

    verifyConnection();

    Object obj = performTransaction(QueueRequest.DeleteAllJobGroups, null);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached names of the privileged users. <P> 
   *
   * May be <CODE>null</CODE> if the cache has been invalidated.
   */ 
  private TreeSet<String>  pPrivilegedUsers;

}

