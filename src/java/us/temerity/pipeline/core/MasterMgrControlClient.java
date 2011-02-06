// $Id: MasterMgrClient.java,v 1.157 2010/01/22 00:14:33 jim Exp $

package us.temerity.pipeline.core;

import java.util.*;
import java.util.Map.*;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.message.*; 
import us.temerity.pipeline.message.node.*;
import us.temerity.pipeline.message.misc.*;

/*------------------------------------------------------------------------------------------*/
/*   M A S T E R    M G R   C O N T R O L   C L I E N T                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline master manager daemon. <P> 
 */
public
class MasterMgrControlClient
  extends MasterMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new master manager client.
   */
  public
  MasterMgrControlClient()
  {
    this(false); 
  }

  /** 
   * Construct a new master manager client.
   * 
   * @param forceLongTransactions
   *   Whether to treat all uses of {@link #performTransaction} like 
   *   {@link #performLongTransaction} with an infinite request timeout and a 60-second 
   *   response retry interval with infinite retries.
   */
  public
  MasterMgrControlClient
  (
   boolean forceLongTransactions   
  )
  {
    super(forceLongTransactions); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A D M I N I S T R A T I V E   P R I V I L E G E S                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Pull the work groups and administrative privileges from the MasterMgr.
   * 
   * @param privs
   *   The privileges to update. 
   * 
   * @throws PipelineException
   *   If unable to pull the privileges.
   */ 
  public synchronized void 
  pullAdminPrivileges
  (
   AdminPrivileges privs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(MasterRequest.PullAdminPrivileges, null); 
    if(obj instanceof MiscPullAdminPrivilegesRsp) {
      MiscPullAdminPrivilegesRsp rsp = (MiscPullAdminPrivilegesRsp) obj;
      privs.pullAdminPrivileges(rsp);
    }
    else {
      handleFailure(obj);
    }        
  }



  /*----------------------------------------------------------------------------------------*/
  /*  C H E C K S U M S                                                                     */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Sent updates to the checksums for files associated with the given set of working 
   * versions. <P> 
   * 
   * This is used by the Queue Manager to transmit checksums stored in QueueJobInfo
   * instances which are about to be garbage collected.
   *
   * @param checksums
   *   The checksum caches for each working version.
   */ 
  public synchronized void
  updateCheckSums
  (
   TreeMap<NodeID,CheckSumCache> checksums
  ) 
    throws PipelineException
  {
    verifyConnection();

    NodeUpdateCheckSumsReq req = new NodeUpdateCheckSumsReq(checksums); 
    Object obj = performTransaction(MasterRequest.UpdateCheckSums, req); 
    handleSimpleResponse(obj);
  }

}

