// $Id: QueueMgrControlClient.java,v 1.1 2004/08/04 01:43:45 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   M G R   C O N T R O L   C L I E N T                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to the Pipeline queue server daemon. <P> 
 * 
 * This class handles network communication with the Pipeline queue server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plqueuemgr</B><A>(1).  
 */
public
class QueueMgrControlClient
  extends QueueMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new queue manager control client.
   * 
   * @param hostname 
   *   The name of the host running <B>plqueuemgr</B>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plqueuemgr</B>(1).
   */
  public
  QueueMgrControlClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port);
  }

  /** 
   * Construct a new queue manager control client. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--queue-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--queue-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  QueueMgrControlClient() 
  {
    super(PackageInfo.sQueueServer, PackageInfo.sQueuePort);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   J O B S                                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the IDs of the latest jobs associated with the given node and file sequence.
   * 
   * 
   * 
   * @throws PipelineException
   *   If unable to determine the job IDs. 
   */
  public synchronized Long[] 
  getJobIDs
  (
   NodeID nodeID, 
   FileSeq fseq
  ) 
    throws PipelineException  
  {
    
    
   throw new PipelineException("Not Implemented");


  }
  

}

