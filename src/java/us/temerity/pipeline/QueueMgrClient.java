// $Id: QueueMgrClient.java,v 1.1 2004/07/24 18:28:45 jim Exp $

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


  
  /*----------------------------------------------------------------------------------------*/
  /*   S E L E C T I O N   K E Y S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the names of the currently defined selection keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the selection keys.
   */
  public synchronized TreeSet<String>
  getSelectionKeyNames() 
    throws PipelineException  
  {
    // TEMPORARY

    TreeSet<String> names = new TreeSet<String>();
    names.add("Rush");
    names.add("Fast");

    return names;
  }

  /**
   * Get the set of currently defined selection keys. <P>  
   * 
   * @throws PipelineException
   *   If unable to retrieve the selection keys.
   */
  public synchronized TreeSet<SelectionKey>
  getSelectionKeys() 
    throws PipelineException  
  {
    // TEMPORARY

    TreeSet<SelectionKey> keys = new TreeSet<SelectionKey>();
    keys.add(new SelectionKey("Rush", "High priority jobs."));
    keys.add(new SelectionKey("Fast", "Fastest available CPU speed."));

    return keys;
  }
  
}

