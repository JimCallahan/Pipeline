// $Id: JobMgrPlgControlClient.java,v 1.2 2007/02/07 21:13:54 jim Exp $

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
public
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
  /*   E D I T O R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Launch an Editor plugin to edit the given files as the specified user.
   * 
   * @param editor 
   *  The editor plugin instance use to edit the files.
   * 
   * @para author
   *   The name of the user owning the files.
   * 
   * @param fseq  
   *   The file sequence to edit.
   * 
   * @param env  
   *   The environment under which the editor is run.  
   * 
   * @param dir  
   *   The working directory where the editor is run.
   * 
   * @return 
   *   If the editor process failed [exit-code, command-line, stdout, stderr] or
   *   <CODE>null</CODE> on success.
   * 
   * @throws PipelineException
   *   If unable to even attempt to launch the editor.
   */
  public synchronized Object[]
  editAs
  ( 
   BaseEditor editor, 
   String author, 
   FileSeq fseq,      
   Map<String,String> env,      
   File dir        
  ) 
    throws PipelineException 
  {
    verifyConnection();

    JobEditAsReq req = new JobEditAsReq(editor, author, fseq, env, dir); 

    Object obj = performLongTransaction(JobRequest.EditAs, req, 15000, 60000);  
    if(obj instanceof SuccessRsp) {
      return null;
    }
    else if(obj instanceof JobEditAsFailedRsp) {
      JobEditAsFailedRsp rsp = (JobEditAsFailedRsp) obj;
      return rsp.getResults(); 
    }
    else {
      handleFailure(obj);
      return null;
    }    
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

