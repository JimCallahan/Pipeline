// $Id: QueueMgrClient.java,v 1.61 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.message.queue.*;
import us.temerity.pipeline.message.simple.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C L I E N T                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A connection to the Pipeline queue manager daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue manager daemon 
 * <A HREF="../../../../man/plqueuemgr.html"><B>plqueuemgr</B><A>(1).  
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
   */
  public
  QueueMgrClient()
  {
    this(false);
  }

  /** 
   * Construct a new queue manager client.
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link #performTransaction} like 
   *   {@link #performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   */
  public
  QueueMgrClient
  (
   boolean forceLongTransactions   
  )
  {
    super(PackageInfo.sQueueServer, PackageInfo.sQueuePort, forceLongTransactions, 
	  QueueRequest.Ping, QueueRequest.Disconnect, QueueRequest.Shutdown, "QueueMgr");

    /* the canonical names of this host */ 
    pLocalHostnames = new TreeSet<String>();
    try {
      Enumeration nets = NetworkInterface.getNetworkInterfaces();  
      while(nets.hasMoreElements()) {
	NetworkInterface net = (NetworkInterface) nets.nextElement();
	Enumeration addrs = net.getInetAddresses();
	while(addrs.hasMoreElements()) {
	  InetAddress addr = (InetAddress) addrs.nextElement();
	  if((addr instanceof Inet4Address) && !addr.isLoopbackAddress()) 
	    pLocalHostnames.add(addr.getCanonicalHostName().toLowerCase(Locale.ENGLISH));
	}
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Net, LogMgr.Level.Warning,
	 ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L O G G I N G                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current logging levels.
   */ 
  public synchronized LogControls
  getLogControls() 
    throws PipelineException 
  {
    verifyConnection();
	 
    Object obj = performTransaction(QueueRequest.GetLogControls, null);
    if(obj instanceof MiscGetLogControlsRsp) {
      MiscGetLogControlsRsp rsp = (MiscGetLogControlsRsp) obj;
      return rsp.getControls();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Set the current logging levels.
   */ 
  public synchronized void
  setLogControls
  (
   LogControls controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
	 
    MiscSetLogControlsReq req = new MiscSetLogControlsReq(controls);

    Object obj = performTransaction(QueueRequest.SetLogControls, req);
    handleSimpleResponse(obj);
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   C O N T R O L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   */ 
  public synchronized QueueControls
  getRuntimeControls() 
    throws PipelineException 
  {
    verifyConnection();
	 
    Object obj = performTransaction(QueueRequest.GetQueueControls, null);
    if(obj instanceof QueueGetQueueControlsRsp) {
      QueueGetQueueControlsRsp rsp = (QueueGetQueueControlsRsp) obj;
      return rsp.getControls();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Set the current runtime performance controls.
   */ 
  public synchronized void
  setRuntimeControls
  (
   QueueControls controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
	 
    QueueSetQueueControlsReq req = new QueueSetQueueControlsReq(controls);

    Object obj = performTransaction(QueueRequest.SetQueueControls, req);
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I C E N S E   K E Y S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined license keys. <P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the license keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the license keys.
   */
  public synchronized TreeSet<String>
  getLicenseKeyNames
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyNamesReq req = new QueueGetKeyNamesReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetLicenseKeyNames, req);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get a table containing the descriptions of the currently defined license keys
   * indexed by license key name.<P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the license keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the license keys.
   */
  public synchronized TreeMap<String,String>
  getLicenseKeyDescriptions
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyDescriptionsReq req = new QueueGetKeyDescriptionsReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetLicenseKeyDescriptions, req);
    if(obj instanceof QueueGetKeyDescriptionsRsp) {
      QueueGetKeyDescriptionsRsp rsp = (QueueGetKeyDescriptionsRsp) obj;
      return rsp.getKeyDescriptions();
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
    verifyConnection();

    QueueRemoveLicenseKeyReq req = new QueueRemoveLicenseKeyReq(kname);
    Object obj = performTransaction(QueueRequest.RemoveLicenseKey, req); 
    handleSimpleResponse(obj);
  }  
  
  /**
   * Set the licensing scheme and maximum number of licenses associated with a 
   * license key. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param kname
   *   The name of the license key.
   * 
   * @param scheme
   *   The scheme used to determine the number of available licenses.
   * 
   * @param maxSlots
   *   The maximum number of slots running a job which requires the license key or 
   *   <CODE>null</CODE> if the license scheme is not PerSlot.
   * 
   * @param maxHosts
   *   The maximum number of hosts which may run a job which requires the license key or 
   *   <CODE>null</CODE> if the license scheme is PerSlot.
   * 
   * @param maxHostSlots
   *   The maximum number of slots which may run a job requiring the license key on a 
   *   single host or <CODE>null</CODE> if the license scheme is not PerHostSlot.
   * 
   * @throws PipelineException
   *   If unable to set the maximum licenses for the given license key.
   */ 
  public synchronized void
  setMaxLicenses
  (
   String kname, 
   LicenseScheme scheme, 
   Integer maxSlots, 
   Integer maxHosts, 
   Integer maxHostSlots
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueSetMaxLicensesReq req =
      new QueueSetMaxLicensesReq(kname, scheme, maxSlots, maxHosts, maxHostSlots);
    Object obj = performTransaction(QueueRequest.SetMaxLicenses, req); 
    handleSimpleResponse(obj);    
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N   K E Y S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined selection keys. <P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the selection keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the selection keys.
   */
  public synchronized TreeSet<String>
  getSelectionKeyNames
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyNamesReq req = new QueueGetKeyNamesReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetSelectionKeyNames, req);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get a table containing the descriptions of the currently defined selection keys
   * indexed by selection key name.<P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the selection keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the selection keys.
   */
  public synchronized TreeMap<String,String>
  getSelectionKeyDescriptions
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyDescriptionsReq req = new QueueGetKeyDescriptionsReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetSelectionKeyDescriptions, req);
    if(obj instanceof QueueGetKeyDescriptionsRsp) {
      QueueGetKeyDescriptionsRsp rsp = (QueueGetKeyDescriptionsRsp) obj;
      return rsp.getKeyDescriptions();
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
    verifyConnection();

    QueueRemoveSelectionKeyReq req = new QueueRemoveSelectionKeyReq(kname);
    Object obj = performTransaction(QueueRequest.RemoveSelectionKey, req); 
    handleSimpleResponse(obj);
  }  


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing selection groups. 
   * 
   * @return
   *   The selection group names. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeSet<String> 
  getSelectionGroupNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionGroupNames, null);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the current selection biases for all existing selection groups. 
   * 
   * @return
   *   The selection groups indexed by group name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeMap<String,SelectionGroup> 
  getSelectionGroups() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionGroups, null);
    if(obj instanceof QueueGetSelectionGroupsRsp) {
      QueueGetSelectionGroupsRsp rsp = (QueueGetSelectionGroupsRsp) obj;
      return rsp.getSelectionGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add a new selection group. <P> 
   * 
   * @param gname
   *   The name of the new selection group. 
   * 
   * @throws PipelineException
   *   If a selection group already exists with the given name. 
   */ 
  public synchronized void
  addSelectionGroup
  (
   String gname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddByNameReq req = new QueueAddByNameReq(gname, "selection group");
    Object obj = performTransaction(QueueRequest.AddSelectionGroup, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing selection group. <P> 
   * 
   * @param gname
   *   The name of the selection group. 
   * 
   * @throws PipelineException
   *   If unable to remove the selection group.
   */ 
  public synchronized void
  removeSelectionGroup
  (
   String gname
  ) 
    throws PipelineException 
  {
    TreeSet<String> gnames = new TreeSet<String>();
    gnames.add(gname);
    
    removeSelectionGroups(gnames);
  }
  
  /**
   * Remove the given existing selection groups. <P> 
   * 
   * @param gnames
   *   The names of the selection groups. 
   * 
   * @throws PipelineException
   *   If unable to remove the selection groups.
   */ 
  public synchronized void
  removeSelectionGroups
  (
   TreeSet<String> gnames
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(gnames, "selection groups");
    Object obj = performTransaction(QueueRequest.RemoveSelectionGroups, req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Change the selection key biases for the given selection group. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param group
   *   The selection group to modify.
   */ 
  public synchronized void
  editSelectionGroup
  (
   SelectionGroup group
  ) 
    throws PipelineException 
  {
    ArrayList<SelectionGroup> groups = new ArrayList<SelectionGroup>();
    groups.add(group);

    editSelectionGroups(groups);
  }

  /**
   * Change the selection key biases for the given selection groups. <P> 
   * 
   * For an detailed explanation of how selection keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param groups
   *   The selection groups to modify.
   */ 
  public synchronized void
  editSelectionGroups
  (
   Collection<SelectionGroup> groups
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueEditSelectionGroupsReq req = new QueueEditSelectionGroupsReq(groups);
    Object obj = performTransaction(QueueRequest.EditSelectionGroups, req); 
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing selection schedules. 
   * 
   * @return
   *   The selection schedule names. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeSet<String> 
  getSelectionScheduleNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionScheduleNames, null);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the existing selection schedules. 
   * 
   * @return
   *   The selection schedules indexed by schedule name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeMap<String,SelectionSchedule> 
  getSelectionSchedules() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetSelectionSchedules, null);
    if(obj instanceof QueueGetSelectionSchedulesRsp) {
      QueueGetSelectionSchedulesRsp rsp = (QueueGetSelectionSchedulesRsp) obj;
      return rsp.getSelectionSchedules();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }
  
  public synchronized SelectionScheduleMatrix
  getSelectionScheduleMatrix()
    throws PipelineException
  {
    TreeMap<String,SelectionSchedule> schedules = getSelectionSchedules();
    SelectionScheduleMatrix matrix = 
      new SelectionScheduleMatrix(schedules, System.currentTimeMillis());
    return matrix;
  }

  /**
   * Add a new selection schedule. <P> 
   * 
   * @param sname
   *   The name of the new selection schedule. 
   * 
   * @throws PipelineException
   *   If a selection schedule already exists with the given name.
   */ 
  public synchronized void
  addSelectionSchedule
  (
   String sname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddByNameReq req = new QueueAddByNameReq(sname, "selection schedule");
    Object obj = performTransaction(QueueRequest.AddSelectionSchedule, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing selection schedule. <P> 
   * 
   * @param sname
   *   The name of the selection schedule. 
   * 
   * @throws PipelineException
   *   If unable to remove the selection schedule.
   */ 
  public synchronized void
  removeSelectionSchedule
  (
   String sname
  ) 
    throws PipelineException 
  {
    TreeSet<String> snames = new TreeSet<String>();
    snames.add(sname);

    removeSelectionSchedules(snames);
  }

  /**
   * Remove the given existing selection schedules. <P> 
   * 
   * @param snames
   *   The names of the selection schedules. 
   * 
   * @throws PipelineException
   *   If unable to remove the selection schedules.
   */ 
  public synchronized void
  removeSelectionSchedules
  (
   TreeSet<String> snames
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(snames, "selection schedules");
    Object obj = performTransaction(QueueRequest.RemoveSelectionSchedules, req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Modify the given selection schedule. <P> 
   * 
   * @param schedule
   *   The selection schedule to modify.
   */ 
  public synchronized void
  editSelectionSchedule
  (
   SelectionSchedule schedule
  ) 
    throws PipelineException 
  {
    ArrayList<SelectionSchedule> schedules = new ArrayList<SelectionSchedule>();
    schedules.add(schedule);

    editSelectionSchedules(schedules);
  }

  /**
   * Modify the given selection schedules. <P> 
   * 
   * @param schedules
   *   The selection schedules to modify.
   */ 
  public synchronized void
  editSelectionSchedules
  (
   Collection<SelectionSchedule> schedules
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueEditSelectionSchedulesReq req = new QueueEditSelectionSchedulesReq(schedules);
    Object obj = performTransaction(QueueRequest.EditSelectionSchedules, req); 
    handleSimpleResponse(obj);
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   H A R D W A R E   K E Y S                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the currently defined hardware keys. <P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the hardware keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the hardware keys.
   */
  public synchronized TreeSet<String>
  getHardwareKeyNames
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyNamesReq req = new QueueGetKeyNamesReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetHardwareKeyNames, req);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get a table containing the descriptions of the currently defined hardware keys
   * indexed by hardware key name.<P>
   * 
   * @param userChoosableOnly
   *   Whether to exclude the names of the hardware keys currently controlled by a KeyChooser
   *   plugin.
   * 
   * @throws PipelineException
   *   If unable to retrieve the names of the hardware keys.
   */
  public synchronized TreeMap<String,String>
  getHardwareKeyDescriptions
  (
    boolean userChoosableOnly  
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetKeyDescriptionsReq req = new QueueGetKeyDescriptionsReq(userChoosableOnly);
    Object obj = performTransaction(QueueRequest.GetHardwareKeyDescriptions, req);
    if(obj instanceof QueueGetKeyDescriptionsRsp) {
      QueueGetKeyDescriptionsRsp rsp = (QueueGetKeyDescriptionsRsp) obj;
      return rsp.getKeyDescriptions();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the currently defined hardware keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the hardware keys.
   */
  public synchronized ArrayList<HardwareKey>
  getHardwareKeys() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetHardwareKeys, null);
    if(obj instanceof QueueGetHardwareKeysRsp) {
      QueueGetHardwareKeysRsp rsp = (QueueGetHardwareKeysRsp) obj;
      return rsp.getKeys();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add the given hardware key to the currently defined hardware keys. <P> 
   * 
   * If a hardware key already exists which has the same name as the given key, it will be 
   * silently overridden by this operation. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param key
   *   The hardware key to add.
   * 
   * @throws PipelineException
   *   If unable to add the hardware key.
   */ 
  public synchronized void
  addHardwareKey
  (
    HardwareKey key
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueAddHardwareKeyReq req = new QueueAddHardwareKeyReq(key);
    Object obj = performTransaction(QueueRequest.AddHardwareKey, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Remove the hardware key with the given name from currently defined hardware keys. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param kname
   *   The name of the hardware key to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the hardware key.
   */ 
  public synchronized void
  removeHardwareKey
  (
   String kname
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueRemoveHardwareKeyReq req = new QueueRemoveHardwareKeyReq(kname);
    Object obj = performTransaction(QueueRequest.RemoveHardwareKey, req); 
    handleSimpleResponse(obj);
  }  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing hardware groups. 
   * 
   * @return
   *   The hardware group names. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeSet<String> 
  getHardwareGroupNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetHardwareGroupNames, null);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the current hardware key values for all existing hardware groups. 
   * 
   * @return
   *   The hardware groups indexed by group name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeMap<String,HardwareGroup> 
  getHardwareGroups() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetHardwareGroups, null);
    if(obj instanceof QueueGetHardwareGroupsRsp) {
      QueueGetHardwareGroupsRsp rsp = (QueueGetHardwareGroupsRsp) obj;
      return rsp.getHardwareGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add a new hardware group. <P> 
   * 
   * @param gname
   *   The name of the new hardware group. 
   * 
   * @throws PipelineException
   *   If a hardware group already exists with the given name. 
   */ 
  public synchronized void
  addHardwareGroup
  (
   String gname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddByNameReq req = new QueueAddByNameReq(gname, "hardware group");
    Object obj = performTransaction(QueueRequest.AddHardwareGroup, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing hardware group. <P> 
   * 
   * @param gname
   *   The name of the hardware group. 
   * 
   * @throws PipelineException
   *   If unable to remove the hardware group.
   */ 
  public synchronized void
  removeHardwareGroup
  (
   String gname
  ) 
    throws PipelineException 
  {
    TreeSet<String> gnames = new TreeSet<String>();
    gnames.add(gname);
    
    removeHardwareGroups(gnames);
  }
  
  /**
   * Remove the given existing hardware groups. <P> 
   * 
   * @param gnames
   *   The names of the hardware groups. 
   * 
   * @throws PipelineException
   *   If unable to remove the hardware groups.
   */ 
  public synchronized void
  removeHardwareGroups
  (
   TreeSet<String> gnames
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(gnames, "hardware groups");
    Object obj = performTransaction(QueueRequest.RemoveHardwareGroups, req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Change the hardware key values for the given hardware group. <P> 
   * 
   * For an detailed explanation of how hardware keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param group
   *   The hardware group to modify.
   */ 
  public synchronized void
  editHardwareGroup
  (
    HardwareGroup group
  ) 
    throws PipelineException 
  {
    ArrayList<HardwareGroup> groups = new ArrayList<HardwareGroup>();
    groups.add(group);

    editHardwareGroups(groups);
  }

  /**
   * Change the hardware key values for the given hardware groups. <P> 
   * 
   * For an detailed explanation of how hardware keys are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param groups
   *   The hardware groups to modify.
   */ 
  public synchronized void
  editHardwareGroups
  (
   Collection<HardwareGroup> groups
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueEditHardwareGroupsReq req = new QueueEditHardwareGroupsReq(groups);
    Object obj = performTransaction(QueueRequest.EditHardwareGroups, req); 
    handleSimpleResponse(obj);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing dispatch controls. 
   * 
   * @return
   *   The dispatch control names. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeSet<String> 
  getDispatchControlNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetDispatchControlNames, null);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }
  
  /**
   * Get the current criteria list for all existing control groups. 
   * 
   * @return
   *   The dispatch controls indexed by group name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeMap<String, DispatchControl> 
  getDispatchControls() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetDispatchControls, null);
    if(obj instanceof QueueGetDispatchControlsRsp) {
      QueueGetDispatchControlsRsp rsp = (QueueGetDispatchControlsRsp) obj;
      return rsp.getDispatchControls();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }
  
  /**
   * Add a new dispatch control. <P> 
   * 
   * @param cname
   *   The name of the new dispatch control. 
   * 
   * @throws PipelineException
   *   If a dispatch control already exists with the given name. 
   */ 
  public synchronized void
  addDispatchControl
  (
    String cname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddByNameReq req = new QueueAddByNameReq(cname, "dispatch control");
    Object obj = performTransaction(QueueRequest.AddDispatchControl, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing dispatch control. <P> 
   * 
   * @param cname
   *   The name of the dispatch control. 
   * 
   * @throws PipelineException
   *   If unable to remove the dispatch control.
   */ 
  public synchronized void
  removeDispatchControl
  (
    String cname
  ) 
    throws PipelineException 
  {
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add(cname);
    
    removeDispatchControls(cnames);
  }
  
  /**
   * Remove the given existing dispatch controls. <P> 
   * 
   * @param cnames
   *   The names of the dispatch controls. 
   * 
   * @throws PipelineException
   *   If unable to remove the dispatch controls.
   */ 
  public synchronized void
  removeDispatchControls
  (
    TreeSet<String> cnames
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(cnames, "dispatch controls");
    Object obj = performTransaction(QueueRequest.RemoveDispatchControls, req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Change the criteria list for the given dispatch control. <P> 
   * 
   * For an detailed explanation of how dispatch controls are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param group
   *   The dispatch controls to modify.
   */ 
  public synchronized void
  editDispatchControl
  (
    DispatchControl group
  ) 
    throws PipelineException 
  {
    ArrayList<DispatchControl> controls = new ArrayList<DispatchControl>();
    controls.add(group);

    editDispatchControls(controls);
  }
  
  /**
   * Change the criteria list for the given dispatch controls. <P> 
   * 
   * For an detailed explanation of how dispatch controls are used to determine the assignment
   * of jobs to hosts, see {@link JobReqs JobReqs}. <P> 
   * 
   * @param controls
   *   The dispatch controls to modify.
   */ 
  public synchronized void
  editDispatchControls
  (
    Collection<DispatchControl> controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueEditDispatchControlsReq req = new QueueEditDispatchControlsReq(controls);
    Object obj = performTransaction(QueueRequest.EditDispatchControls, req); 
    handleSimpleResponse(obj);
  }

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of all existing user balance groups. 
   * 
   * @return
   *   The user balance group names. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeSet<String> 
  getBalanceGroupNames() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetBalanceGroupNames, null);
    if(obj instanceof QueueGetNamesRsp) {
      QueueGetNamesRsp rsp = (QueueGetNamesRsp) obj;
      return rsp.getNames();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }
  
  /**
   * Get the current user weighting for all existing user balance groups. 
   * 
   * @return
   *   The groups indexed by group name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized TreeMap<String, UserBalanceGroup> 
  getBalanceGroups() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetBalanceGroups, null);
    if(obj instanceof QueueGetUserBalanceGroupsRsp) {
      QueueGetUserBalanceGroupsRsp rsp = (QueueGetUserBalanceGroupsRsp) obj;
      return rsp.getUserBalanceGroups();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the current user weighting for a single user balance groups. 
   * 
   * @param groupName
   *   The name of the group.
   * 
   * @return
   *   The user balance group. 
   * 
   * @throws PipelineException
   *   If no group with that name exists.
   */ 
  public synchronized UserBalanceGroup
  getBalanceGroup
  (
    String groupName  
  )
    throws PipelineException
  {
    verifyConnection();
    
    QueueGetByNameReq req = new QueueGetByNameReq(groupName);
    Object obj = performTransaction(QueueRequest.GetBalanceGroup, req);
    if(obj instanceof QueueGetUserBalanceGroupRsp) {
      QueueGetUserBalanceGroupRsp rsp = (QueueGetUserBalanceGroupRsp) obj;
      return rsp.getUserBalanceGroup();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Add a new user balance group. <P> 
   * 
   * @param cname
   *   The name of the new group. 
   * 
   * @throws PipelineException
   *   If a group already exists with the given name. 
   */ 
  public synchronized void
  addBalanceGroup
  (
    String cname
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddByNameReq req = new QueueAddByNameReq(cname, "user balance group");
    Object obj = performTransaction(QueueRequest.AddBalanceGroup, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the given existing user balance control. <P> 
   * 
   * @param cname
   *   The name of the group. 
   * 
   * @throws PipelineException
   *   If unable to remove the user balance group.
   */ 
  public synchronized void
  removeBalanceGroup
  (
    String cname
  ) 
    throws PipelineException 
  {
    TreeSet<String> cnames = new TreeSet<String>();
    cnames.add(cname);
    
    removeUserBalanceGroups(cnames);
  }
  
  /**
   * Remove the given existing user balance groups. <P> 
   * 
   * @param cnames
   *   The names of the groups. 
   * 
   * @throws PipelineException
   *   If unable to remove the user balance groups.
   */ 
  public synchronized void
  removeUserBalanceGroups
  (
    TreeSet<String> cnames
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(cnames, "user balance groups");
    Object obj = performTransaction(QueueRequest.RemoveBalanceGroups, req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Change the user weighting for the given user balance group. <P> 
   * 
   * @param group
   *   The user balance group to modify.
   */ 
  public synchronized void
  editBalanceGroup
  (
    UserBalanceGroup group
  ) 
    throws PipelineException 
  {
    ArrayList<UserBalanceGroup> groups = new ArrayList<UserBalanceGroup>();
    groups.add(group);

    editBalanceGroups(groups);
  }
  
  /**
   * Change the user weighting for the given user balance groups. <P> 
   * 
   * @param controls
   *   The groups to modify.
   */ 
  public synchronized void
  editBalanceGroups
  (
    Collection<UserBalanceGroup> controls
  ) 
    throws PipelineException 
  {
    verifyConnection();
    
    QueueEditUserBalanceGroupsReq req = new QueueEditUserBalanceGroupsReq(controls);
    Object obj = performTransaction(QueueRequest.EditBalanceGroups, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Get the current user usage for all existing balance groups. 
   * 
   * @return
   *   The percentage of the balance group used, indexed by balance group name and then user
   *   name. 
   * 
   * @throws PipelineException
   *   If unable to retrieve the information.
   */ 
  public synchronized DoubleMap<String, String, Double> 
  getBalanceGroupUsage() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetBalanceGroupUsage, null);
    if(obj instanceof QueueGetBalanceGroupUsageRsp) {
      QueueGetBalanceGroupUsageRsp rsp = (QueueGetBalanceGroupUsageRsp) obj;
      return rsp.getBalanceGroupUsage();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S E R V E R   E X T E N S I O N S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current queue extension configurations. <P> 
   * 
   * @param name
   *   The name of the queue extension configuration.
   * 
   * @return 
   *   The extension configuration 
   *   or <CODE>null</CODE> if no extension with the given name exists.
   * 
   * @throws PipelineException
   *   If unable to determine the extensions.
   */ 
  public synchronized QueueExtensionConfig
  getQueueExtensionConfig
  (
   String name
  ) 
    throws PipelineException  
  {
    if(name == null) 
      throw new IllegalArgumentException
	("The extension configuration name cannot be (null)!");
    return getQueueExtensionConfigs().get(name);
  }

  /**
   * Get the current queue extension configurations. <P> 
   * 
   * @return 
   *   The extension configurations indexed by configuration name.
   * 
   * @throws PipelineException
   *   If unable to determine the extensions.
   */ 
  public synchronized TreeMap<String,QueueExtensionConfig> 
  getQueueExtensionConfigs() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetQueueExtension, null); 
    if(obj instanceof QueueGetQueueExtensionsRsp) {
      QueueGetQueueExtensionsRsp rsp = (QueueGetQueueExtensionsRsp) obj;
      return rsp.getExtensions();
    }
    else {
      handleFailure(obj);
      return null;
    } 
  }
  
  /**
   * Remove an existing the queue extension configuration. <P> 
   * 
   * @param name
   *   The name of the queue extension configuration to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the extension.
   */ 
  public synchronized void
  removeQueueExtensionConfig
  (
   String name
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueRemoveQueueExtensionReq req = new QueueRemoveQueueExtensionReq(name);

    Object obj = performTransaction(QueueRequest.RemoveQueueExtension, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Add or modify an existing the queue extension configuration. <P> 
   * 
   * @param extension
   *   The queue extension configuration to add (or modify).
   * 
   * @throws PipelineException
   *   If unable to set the extension.
   */ 
  public synchronized void
  setQueueExtensionConfig
  (
   QueueExtensionConfig extension
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueSetQueueExtensionReq req = new QueueSetQueueExtensionReq(extension);

    Object obj = performTransaction(QueueRequest.SetQueueExtension, req); 
    handleSimpleResponse(obj);
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   J O B   M A N A G E R   H O S T S                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current state of the hosts participating in the Pipeline queue filtered
   * by the given histogram specs. <P> 
   * 
   * In order to be considered a match, the individual histogram spec for each host property 
   * with any included categories must include the category (see 
   * {@link HistogramSpec#isIncluded(int)}) which contains the host property.
   * 
   * @param specs
   *   The set of histogram specifications to match.
   * 
   * @return 
   *   The per-host information indexed by fully resolved host name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host information.
   */
  public synchronized TreeMap<String,QueueHostInfo>
  getHosts
  (
   QueueHostHistogramSpecs specs
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueGetHostsReq req = new QueueGetHostsReq(specs);

    Object obj = performTransaction(QueueRequest.GetHosts, req);
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
   * Get the current state of all hosts participating in the Pipeline queue. <P> 
   * 
   * In other words, the hosts which run the <B>pljobmgr</B>(1) daemon.
   * 
   * @return 
   *   The per-host information indexed by fully resolved host name.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host information.
   */
  public synchronized TreeMap<String,QueueHostInfo>
  getHosts()
    throws PipelineException  
  {
    return getHosts(null);
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
    
    QueueAddByNameReq req = new QueueAddByNameReq(hostname, "hostname");
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
    
    QueueRemoveByNameReq req = new QueueRemoveByNameReq(hostnames, "hostnames");
    Object obj = performTransaction(QueueRequest.RemoveHosts, req);
    handleSimpleResponse(obj);
  }

  /**
   * Change the editable properties of the given hosts. <P> 
   * 
   * A <B>pljobmgr</B>(1) daemon must first be running on a host before a change can be 
   * made to its status. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param changes
   *   The changes in host properties indexed by fully resolved names of the hosts.
   * 
   * @throws PipelineException 
   *   If unable to change the status of the hosts.
   */
  public synchronized void
  editHosts
  (
   TreeMap<String,QueueHostMod> changes
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueEditHostsReq req = new QueueEditHostsReq(changes, pLocalHostnames);
    Object obj = performTransaction(QueueRequest.EditHosts, req); 
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the host names and timestamps of all existing job server notes. 
   * 
   * @return 
   *   The set of timestamps for each note indexed by fully resolved host names.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host notes information.
   */
  public synchronized MappedSet<String,Long>
  getHostsWithNotes()
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetHostsWithNotes, null);
    if(obj instanceof QueueGetHostsWithNotesRsp) {
      QueueGetHostsWithNotesRsp rsp = (QueueGetHostsWithNotesRsp) obj;
      return rsp.getNoteIndex();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get all notes (if any) associated with the given host.
   * 
   * @param hname
   *   The fully resolved name of the host.
   * 
   * @return 
   *   The note message or <CODE>null</CODE> if no note matches.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host notes information.
   */
  public synchronized TreeMap<Long,SimpleLogMessage>
  getHostNotes
  (
   String hname 
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueByHostNotesReq req = new QueueByHostNotesReq(hname);

    Object obj = performTransaction(QueueRequest.GetHostNotes, req);
    if(obj instanceof QueueGetHostNotesRsp) {
      QueueGetHostNotesRsp rsp = (QueueGetHostNotesRsp) obj;
      return rsp.getHostNotes();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get the note (if any) associated with the given host and timestamp.
   * 
   * @param hname
   *   The fully resolved name of the host.
   * 
   * @param stamp
   *   The timestamp of when the note was written.
   * 
   * @return 
   *   The note message or <CODE>null</CODE> if no note matches.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host notes information.
   */
  public synchronized SimpleLogMessage
  getHostNote
  (
   String hname, 
   long stamp
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueByHostNoteReq req = new QueueByHostNoteReq(hname, stamp);

    Object obj = performTransaction(QueueRequest.GetHostNote, req);
    if(obj instanceof QueueGetHostNoteRsp) {
      QueueGetHostNoteRsp rsp = (QueueGetHostNoteRsp) obj;
      return rsp.getHostNote();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Add a note to the given host. 
   *
   * @param hname
   *   The fully resolved name of the host.
   * 
   * @param text
   *   The text of the note.
   * 
   * @throws PipelineException
   *   If unable to retrieve the host notes information.
   */
  public synchronized void
  addHostNote
  (
   String hname, 
   String text
  )
    throws PipelineException  
  {
    verifyConnection();
    
    QueueAddHostNoteReq req = new QueueAddHostNoteReq(hname, new SimpleLogMessage(text));
    Object obj = performTransaction(QueueRequest.AddHostNote, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove all notes (if any) associated with the given host.
   * 
   * @param hname
   *   The fully resolved name of the host.
   * 
   * @throws PipelineException
   *   If unable to remove the host notes.
   */
  public synchronized void 
  removeHostNotes
  (
   String hname 
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueByHostNotesReq req = new QueueByHostNotesReq(hname);
    Object obj = performTransaction(QueueRequest.RemoveHostNotes, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the note (if any) associated with the given host and timestamp.
   * 
   * @param hname
   *   The fully resolved name of the host.
   * 
   * @param stamp
   *   The timestamp of when the note was written.
   * 
   * @throws PipelineException
   *   If unable to remove the host note.
   */
  public synchronized void
  removeHostNote
  (
   String hname, 
   long stamp
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueByHostNoteReq req = new QueueByHostNoteReq(hname, stamp);
    Object obj = performTransaction(QueueRequest.RemoveHostNote, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the dynamic resource sample history of the given host.<P> 
   * 
   * If the <CODE>runtimeOnly</CODE> argument is <CODE>true</CODE> then only samples already
   * stored in the 30-minute runtime cache for the selected hosts will be returned.  Note that
   * this is much faster for cases wanting only recent samples since no resource sample cache 
   * files need to be read by the server.  If <CODE>runtimeOnly</CODE> is <CODE>false</CODE> 
   * and the <CODE>interval</CODE> specified for a host overlaps the time period stored in an 
   * existing resource sample file, this file will be loaded every time this method is called.
   * Since these files can be quite large, care should be taken calling this method for large 
   * numbers of hosts using large sample intervals.<P> 
   * 
   * The intended usage is to call this method only once using a large sample interval and 
   * then to update a local ResourceSampleCache using a small interval spanning the time 
   * since the last update until now for all subsequent calls. This will minimize both the
   * load on the server and the amount of data which must be transmitted over the network.<P> 
   * 
   * If you only want to know the most recent resource sample collected for a host, it is 
   * much more efficient to use the {@link #getHosts() getHosts} method to obtain the current 
   * job server host information and then accessing the last collected resource sample using
   * the {@link QueueHostInfo#getLatestSample QueueHostInfo.getLatestSample} method.
   * 
   * @param intervals
   *   The sample intervals to retrieve indexed by fully resolved hostnames.
   * 
   * @param runtimeOnly
   *   Whether to only read samples from the runtime cache ignoring any saved samples
   *   on disk.
   * 
   * @return 
   *   The requested samples indexed by fully resolved hostname. 
   * 
   * @throws PipelineException
   *   If unable to lookup the resource usage.
   */ 
  public synchronized TreeMap<String,ResourceSampleCache> 
  getHostResourceSamples
  (
   TreeMap<String,TimeInterval> intervals,
   boolean runtimeOnly
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetHostResourceSamplesReq req = 
      new QueueGetHostResourceSamplesReq(intervals, runtimeOnly);

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

  /**
   * Gets frequency distribution data for significant catagories of information shared 
   * by all job server hosts. 
   * 
   * @param specs
   *   The histogram catagory specifications.
   * 
   * @return 
   *   The histograms which fulfill the given specifications. 
   * 
   * @throws PipelineException
   *   If unable to lookup the host information.
   */
  public synchronized QueueHostHistograms
  getHostHistograms
  (
   QueueHostHistogramSpecs specs
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    QueueGetHostHistogramsReq req = new QueueGetHostHistogramsReq(specs);
    Object obj = performTransaction(QueueRequest.GetHostHistograms, req);
    if(obj instanceof QueueGetHostHistogramsRsp) {
      QueueGetHostHistogramsRsp rsp = (QueueGetHostHistogramsRsp) obj;
      return rsp.getHistograms();
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
   * Get the distribution of job states for the jobs associated with each of the given 
   * job group IDs.
   * 
   * @param groupIDs
   *   The unique job group IDs.
   * 
   * @throws PipelineException
   *   If unable to determine the job states.
   * 
   * @return
   *   The normalized frequency distribution of each JobState index by job group ID.
   */ 
  public synchronized TreeMap<Long,double[]>
  getJobStateDistribution
  (
   TreeSet<Long> groupIDs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobStateDistributionReq req = new QueueGetJobStateDistributionReq(groupIDs);
    
    Object obj = performTransaction(QueueRequest.GetJobStateDistribution, req);
    if(obj instanceof QueueGetJobStateDistributionRsp) {
      QueueGetJobStateDistributionRsp rsp = (QueueGetJobStateDistributionRsp) obj;
      return rsp.getStateDistribution();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

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
   * Get the JobStatus of all currently running jobs. <P>
   * 
   * @throws PipelineException
   *   If unable to determine the job status.
   */ 
  public synchronized TreeMap<Long,JobStatus>
  getRunningJobStatus() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetRunningJobStatus, null);
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
   * Get the jobs with the given IDs.
   * 
   * @param jobIDs
   *   The set of unique job identifiers.
   * 
   * @throws PipelineException
   *   If no jobs exists with the given IDs or if the set of jobIDs is empty or 
   *   <code>null</code>.
   */ 
  public synchronized Map<Long, QueueJob>
  getJobs
  (
   Set<Long> jobIDs
  ) 
    throws PipelineException  
  {
    verifyConnection();
    
    if (jobIDs == null || jobIDs.isEmpty())
      throw new PipelineException
        ("At least one job must be requested");

    QueueGetJobReq req = new QueueGetJobReq(jobIDs);

    Object obj = performTransaction(QueueRequest.GetJob, req);
    if(obj instanceof QueueGetJobRsp) {
      QueueGetJobRsp rsp = (QueueGetJobRsp) obj;
      return rsp.getJobs();
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
  
  /**
   * Get information about the current status of jobs in the queue. <P> 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException
   *   If no jobs exists with the given IDs.
   */ 
  public synchronized Map<Long, QueueJobInfo>
  getJobInfos
  (
    Set<Long> jobIDs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobInfoReq req = new QueueGetJobInfoReq(jobIDs);

    Object obj = performTransaction(QueueRequest.GetJobInfo, req);
    if(obj instanceof QueueGetJobInfoRsp) {
      QueueGetJobInfoRsp rsp = (QueueGetJobInfoRsp) obj;
      return rsp.getJobInfos();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }

  /**
   * Get information about the currently running jobs. <P> 
   * 
   * @return 
   *   The information about running jobs indexed by job ID.
   * 
   * @throws PipelineException
   *   If unable to retrieve the job information.
   */ 
  public synchronized TreeMap<Long,QueueJobInfo>
  getRunningJobInfo() 
    throws PipelineException  
  {
    verifyConnection();

    Object obj = performTransaction(QueueRequest.GetRunningJobInfo, null);
    if(obj instanceof QueueGetRunningJobInfoRsp) {
      QueueGetRunningJobInfoRsp rsp = (QueueGetRunningJobInfoRsp) obj;
      return rsp.getJobInfo();
    }
    else {
      handleFailure(obj);
      return null;
    }        
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Kill and requeue the jobs with the given IDs. <P> 
   * 
   * If successful, the jobs will be killed but instead of failing, the jobs will be
   * automatically requeued and will be rerun at the next available opportunity. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to preempt the jobs.
   */  
  public synchronized void
  preemptJobs
  (
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.PreemptJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Kill, requeue and pause the jobs with the given IDs. <P> 
   * 
   * If successful, the jobs will be killed but instead of failing, the jobs will be
   * automatically requeued and then paused.<P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to preempt/pause the jobs.
   */  
  public synchronized void
  preemptAndPauseJobs
  (
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.PreemptAndPauseJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Kill the jobs with the given IDs. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
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
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.KillJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Pause the jobs with the given IDs. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
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
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.PauseJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Resume execution of the paused jobs with the given IDs. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
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
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.ResumeJobs, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Changes the job requirements defined by the deltas. <P> 
   * 
   * If the owner of the jobs is different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param jobReqsChanges
   *   The job requirements indexed by unique job identifiers
   * 
   * @throws PipelineException 
   *   If unable to change the job requirements for the jobs.
   */  
  public synchronized void
  changeJobReqs
  (
    LinkedList<JobReqsDelta> jobReqsChanges
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobReqsReq req = new QueueJobReqsReq(jobReqsChanges);
    Object obj = performTransaction(QueueRequest.ChangeJobReqs, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Updates the generated keys for the jobs with the given IDs. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to update the jobs keys.
   */  
  public synchronized void
  updateJobKeys
  (
    TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueJobsReq req = new QueueJobsReq(jobIDs);
    Object obj = performTransaction(QueueRequest.UpdateJobKeys, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Get a boolean which reflects whether key choosers need to be rerun for all jobs in the 
   * queue. 
   * 
   * @throws PipelineException 
   *   If unable to get the boolean value.
   */
  public synchronized Boolean
  doJobKeysNeedUpdate()
    throws PipelineException
  {
    verifyConnection();
    
    Object obj = performTransaction(QueueRequest.DoJobKeysNeedUpdate, null);
    if(obj instanceof QueueGetBooleanRsp) {
      QueueGetBooleanRsp rsp = (QueueGetBooleanRsp) obj;
      return rsp.getBooleanValue();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Get the timestamp that reflects the last time a keychoosers or a key that uses a 
   * keychooser was updated. 
   * <p>
   * This value can then be compared against the value saved in jobs to determine if the job 
   * needs to have its keys re-evaluated. 
   * 
   * @throws PipelineException 
   *   If unable to get the long value.
   */
  public synchronized Long
  getChooserUpdateTime()
    throws PipelineException
  {
    verifyConnection();
    
    Object obj = performTransaction(QueueRequest.GetChooserUpdateTime, null);
    if(obj instanceof SimpleLongRsp) {
      SimpleLongRsp rsp = (SimpleLongRsp) obj;
      return rsp.getLongValue();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Updates the generated keys for all jobs in the queue. <P> 
   * 
   * This method will fail unless the current user has QueueAdmin privileges. <P>
   * 
   * This method will return before it updates any of the jobs, so it does not cause the 
   * client to have to wait while all the jobs in the queue are processed.  Any errors which 
   * occur will be written into the QueueMgr log, but not be returned.<p>
   * 
   * @throws PipelineException 
   *   If the user does not have permission to update the keys.
   */  
  public synchronized void
  updateAllJobKeys() 
    throws PipelineException
  {
    verifyConnection();

    PrivilegedReq req = new PrivilegedReq();
    Object obj = performTransaction(QueueRequest.UpdateAllJobKeys, req); 
    handleSimpleResponse(obj);
  }
  
  /**
   * Set the Key State in every non-executed job to Stale, prompting users to rerun the key
   * choosers on the jobs. <P>
   * 
   * This method will fail unless the current user has QueueAdmin privileges. <P>
   * 
   * @throws PipelineException 
   *   If the user does not have permission to update the keys.
   */ 
  public synchronized void 
  invalidateAllJobKeys()
    throws PipelineException
  {
    verifyConnection();
    
    PrivilegedReq req = new PrivilegedReq();
    Object obj = performTransaction(QueueRequest.InvalidateAllJobKeys, req); 
    handleSimpleResponse(obj);
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Kill and requeue all jobs associated with the given working version.<P> 
   * 
   * If successful, the jobs will be killed but instead of failing, the jobs will be
   * automatically requeued and will be rerun at the next available opportunity. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to preempt the jobs.
   */  
  public synchronized void
  preemptJobs
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueNodeJobsReq req = new QueueNodeJobsReq(nodeID);
    Object obj = performTransaction(QueueRequest.PreemptNodeJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Kill, requeue and pause all jobs associated with the given working version.<P> 
   * 
   * If successful, the jobs will be killed but instead of failing, the jobs will be
   * automatically requeued and paused. <P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to preempt/pause the jobs.
   */  
  public synchronized void
  preemptAndPauseJobs
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueNodeJobsReq req = new QueueNodeJobsReq(nodeID);
    Object obj = performTransaction(QueueRequest.PreemptAndPauseNodeJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Kill all jobs associated with the given working version.<P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to kill the jobs.
   */  
  public synchronized void
  killJobs
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueNodeJobsReq req = new QueueNodeJobsReq(nodeID);
    Object obj = performTransaction(QueueRequest.KillNodeJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Pause all jobs associated with the given working version.<P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to pause the jobs.
   */  
  public synchronized void
  pauseJobs
  (
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueNodeJobsReq req = new QueueNodeJobsReq(nodeID);
    Object obj = performTransaction(QueueRequest.PauseNodeJobs, req); 
    handleSimpleResponse(obj);
  }

  /**
   * Resume execution of all paused jobs associated with the given working version.<P> 
   * 
   * If the owner of the jobs are different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @param nodeID
   *   The unique working version identifier. 
   * 
   * @throws PipelineException 
   *   If unable to resume the jobs.
   */  
  public synchronized void
  resumeJobs
  ( 
   NodeID nodeID
  ) 
    throws PipelineException
  {
    verifyConnection();

    QueueNodeJobsReq req = new QueueNodeJobsReq(nodeID);
    Object obj = performTransaction(QueueRequest.ResumeNodeJobs, req); 
    handleSimpleResponse(obj);
  }
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * Kill and requeue the jobs with the given IDs. <P> 
   * 
   * If successful, the jobs will be killed but instead of failing, the jobs will be
   * automatically requeued and will be rerun at the next available opportunity. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @deprecated 
   *   The <CODE>author</CODE> argument is no longer neccessary and is now ignored by
   *   this method.  Please use the other form of this method which does not take an
   *   <CODE>author</CODE> in future code.
   * 
   * @param author 
   *   The name of the user which owns the jobs.
   * 
   * @param jobIDs
   *   The unique job identifiers.
   * 
   * @throws PipelineException 
   *   If unable to preempt the jobs.
   */ 
  @Deprecated 
  public synchronized void
  preemptJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    preemptJobs(jobIDs);
  }

  /**
   * Kill the jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @deprecated 
   *   The <CODE>author</CODE> argument is no longer neccessary and is now ignored by
   *   this method.  Please use the other form of this method which does not take an
   *   <CODE>author</CODE> in future code.
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
  @Deprecated 
  public synchronized void
  killJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    killJobs(jobIDs);
  }

  /**
   * Pause the jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @deprecated 
   *   The <CODE>author</CODE> argument is no longer neccessary and is now ignored by
   *   this method.  Please use the other form of this method which does not take an
   *   <CODE>author</CODE> in future code.
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
  @Deprecated 
  public synchronized void
  pauseJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    pauseJobs(jobIDs);
  }

  /**
   * Resume execution of the paused jobs with the given IDs. <P> 
   * 
   * The <CODE>author</CODE> argument must match the user who submitted the jobs. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has QueueAdmin privileges. 
   * 
   * @deprecated 
   *   The <CODE>author</CODE> argument is no longer neccessary and is now ignored by
   *   this method.  Please use the other form of this method which does not take an
   *   <CODE>author</CODE> in future code.
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
  @Deprecated 
  public synchronized void
  resumeJobs
  (
   String author, 
   TreeSet<Long> jobIDs
  ) 
    throws PipelineException
  {
    resumeJobs(jobIDs);
  }

  
  /*----------------------------------------------------------------------------------------*/

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
   * Get the job groups with the given IDs. 
   * 
   * @param groupIDs
   *   The unique job group identifiers.
   * 
   * @throws PipelineException
   *   If no job group exists with the given ID.
   */ 
  public synchronized TreeMap<Long, QueueJobGroup>
  getJobGroups
  (
    Set<Long> groupIDs
  ) 
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobGroupReq req = new QueueGetJobGroupReq(groupIDs);

    Object obj = performTransaction(QueueRequest.GetJobGroupsByID, req);
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
   * Get all of the existing job groups.
   * 
   * @throws PipelineException
   *   If no job groups exist.
   */ 
  public synchronized TreeMap<Long,QueueJobGroup>
  getJobGroups()
    throws PipelineException  
  {
    return getJobGroups(null, null); 
  }

  /**
   * Get the job groups which match the following working area pattern.
   * 
   * @param author
   *   The name of the user owning the job groups or 
   *   <CODE>null</CODE> to match all users.
   * 
   * @param view 
   *   The name of the working area view owning the job groups or 
   *   <CODE>null</CODE> to match all working areas.
   * 
   * @throws PipelineException
   *   If no job groups exist.
   */ 
  public synchronized TreeMap<Long,QueueJobGroup>
  getJobGroups
  (
   String author,
   String view
  )
    throws PipelineException  
  {
    verifyConnection();

    QueueGetJobGroupsReq req = new QueueGetJobGroupsReq(author, view); 

    Object obj = performTransaction(QueueRequest.GetJobGroups, req);
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
   * Get the job groups owned by one of the given users.
   * 
   * @param authors
   *   The names of the users owning the job groups or 
   *   <CODE>null</CODE> to match all users.
   * 
   * @throws PipelineException
   *   If no job groups exist.
   */ 
  public synchronized TreeMap<Long,QueueJobGroup>
  getJobGroupsByUsers
  (
    TreeSet<String> authors
  )
    throws PipelineException  
  {
    verifyConnection();
    
    if((authors == null) || authors.isEmpty())
      throw new PipelineException
        ("The authors cannot be (null)!"); 

    SimpleSetReq req = new SimpleSetReq(authors); 

    Object obj = performTransaction(QueueRequest.GetJobGroupsByUsers, req);
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
    verifyConnection();

    QueueDeleteViewJobGroupsReq req = new QueueDeleteViewJobGroupsReq(author, view);

    Object obj = performTransaction(QueueRequest.DeleteViewJobGroups, req);
    handleSimpleResponse(obj);
  }

  /**
   * Delete all of the completed job groups owned by a set of users. <P> 
   * 
   * This method will fail unless the current user has privileged access status for all the 
   * users in the set.  However, the method will not terminated abruptly when such a 
   * privilege failure occurs.  Instead it will continue iterating through the set until the 
   * end and then report a single error.
   * 
   * @param users
   *   The set of user names whose job groups will be deleted.
   * 
   * @throws PipelineException
   *   If unable to delete the job groups for any user.  This command will attempt to delete
   *   the job groups for all the specified users and will then throw an exception summing up
   *   the failures it experienced.
   */ 
  public synchronized void
  deleteUsersJobGroups
  ( 
    TreeSet<String> users
  ) 
    throws PipelineException  
  {
    verifyConnection();

    SimpleSetReq req = new SimpleSetReq(users);

    Object obj = performTransaction(QueueRequest.DeleteUsersJobGroups, req);
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
    verifyConnection();

    PrivilegedReq req = new PrivilegedReq();

    Object obj = performTransaction(QueueRequest.DeleteAllJobGroups, req);
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.
   */ 
  @Override
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new PluginInputStream(in);
  }

  
  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  @Override
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plqueuemgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The canonical names of this host.
   */ 
  private TreeSet<String>  pLocalHostnames;

}

