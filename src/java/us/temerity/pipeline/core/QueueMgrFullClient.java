// $Id: QueueMgrFullClient.java,v 1.1 2004/07/24 18:28:45 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   F U L L   C L I E N T                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A privileged connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plqueuemgr</B><A>(1).  All operations are 
 * assumed to operate as if the calling user has privileged status.
 */
public
class QueueMgrFullClient
  extends QueueMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager client.
   * 
   * @param hostname 
   *   The name of the host running <B>plmaster</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plmaster</B>(1).
   */
  public
  QueueMgrFullClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port);
  }

  /** 
   * Construct a new master manager client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--queue-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--queue-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  QueueMgrFullClient() 
  {
    super();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   L I C E N S E   K E Y S                                                              */
  /*----------------------------------------------------------------------------------------*/

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
    verifyConnection();

    QueueSetTotalLicensesReq req = new QueueSetTotalLicensesReq(kname, total);
    Object obj = performTransaction(QueueRequest.SetTotalLicenses, req); 
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N   K E Y S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
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
    throw new PipelineException("Not implemented..");
  }

  /**
   * Remove the selection key with the given name from currently defined selection keys. <P> 
   * 
   * This method will fail if the current user does not have privileged access status.
   * 
   * @param name
   *   The name of the selection key to remove.
   * 
   * @throws PipelineException
   *   If unable to remove the selection key.
   */ 
  public synchronized void
  removeSelectionKey
  (
   String name
  ) 
    throws PipelineException  
  {
    throw new PipelineException("Not implemented..");
  }  



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  
}

