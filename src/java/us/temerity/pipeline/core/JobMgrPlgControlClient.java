// $Id: JobMgrPlgControlClient.java,v 1.1 2006/11/16 07:29:25 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   M G R   P L G   C O N T R O L   C L I E N T                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A control connection to a Pipeline job server daemon capable of handing Pipeline 
 * plugins as part of the object stream. <P> 
 * 
 * This class handles network communication with a Pipeline job server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>pljobmgr</B><A>(1) running on one of the 
 * hosts which are capable of executing jobs for the Pipeline queue.  <P> 
 */
class JobMgrPlgControlClient
  extends JobMgrControlClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new job manager control client.
   * 
   * @param hostname 
   *   The name of the host running <B>pljobmgr</B>(1).
   */
  public
  JobMgrPlgControlClient
  ( 
   String hostname
  ) 
  {
    super(hostname); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   J O B   E X E C U T I O N                                                            */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Starts execution of the job on the server.
   * 
   * @param job 
   *   The job to execute.
   * 
   * @param envs  
   *   The cooked toolset environments indexed by operating system type.
   * 
   * @throws PipelineException 
   *   If unable to contact the job server. 
   */ 
  public synchronized void
  jobStart
  (
   QueueJob job, 
   DoubleMap<OsType,String,String> envs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    JobStartReq req = new JobStartReq(job, envs);

    Object obj = performTransaction(JobRequest.Start, req, 60000); 
    handleSimpleResponse(obj);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.<P> 
   * 
   * Reenables support for Pipeline plugins over the connection.
   */ 
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new PluginInputStream(in);
  }

}

