// $Id: QueueMgrClient.java,v 1.2 2004/07/25 03:04:51 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C L I E N T                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A non-privileged connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plqueuemgr</B><A>(1).  No operations provided 
 * by this class require that the calling user to have privileged status.
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
    if(author.equals("pipeline")) 
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The cached names of the privileged users. <P> 
   *
   * May be <CODE>null</CODE> if the cache has been invalidated.
   */ 
  private TreeSet<String>  pPrivilegedUsers;

}

