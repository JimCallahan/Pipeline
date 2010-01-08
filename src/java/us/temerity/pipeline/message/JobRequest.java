// $Id: JobRequest.java,v 1.10 2010/01/08 09:38:10 jim Exp $

package us.temerity.pipeline.message;

/*------------------------------------------------------------------------------------------*/
/*   J O B   R E Q U E S T                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * The set of identifiers for job request messages which may be sent over a network 
 * connection from the <CODE>JobMgrClient</CODE> and <CODE>JobMgrFullClient</CODE>
 * instances to the <CODE>JobMgrServer</CODE>. <P> 
 * 
 * The protocol of communication between these job manager classes is for a 
 * <CODE>JobRequest</CODE> to be sent followed by a corresponding request
 * object which contains the needed request data.  The purpose of this enumeration is to 
 * make testing for which request is being send more efficient and cleaner on the 
 * <CODE>JobMgrServer</CODE> side of the connection.
 */
public
enum JobRequest
{    
  /** 
   * Launch an Editor plugin to edit the given files as the specified user.
   */ 
  EditAs, 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Get a point sample of the currently available system resources.
   */ 
  GetResources, 

  /**
   * Get the operating system type. 
   */ 
  GetOsType,  

  /**
   * Get the number of processors (CPUs).
   */ 
  GetNumProcessors,  

  /**
   * Get the total amount of system memory (in bytes).
   */ 
  GetTotalMemory, 

  /**
   * Get the size of the temporary disk drive (in bytes).
   */ 
  GetTotalDisk, 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * An instance {@link JobStartReq} is next.
   */ 
  Start, 

  /**
   * An instance {@link JobKillReq} is next.
   */ 
  Kill, 

  /**
   * An instance {@link JobWaitReq} is next.
   */ 
  Wait, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link JobCleanupResourcesReq} is next.
   */  
  CleanupResources, 

  /**
   * An instance {@link JobCleanupPreemptedResourcesReq} 
   * is next.
   */  
  CleanupPreemptedResources, 


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * An instance {@link JobGetExecDetailsReq} is next.
   */ 
  GetExecDetails, 
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * An instance {@link JobGetNumStdOutLinesReq} is next.
   */ 
  GetNumStdOutLines, 

  /**
   * An instance {@link JobGetStdOutLinesReq} is next.
   */ 
  GetStdOutLines, 

  /**
   * An instance {@link JobCloseStdOutReq} is next.
   */ 
  CloseStdOut, 


  /**
   * An instance {@link JobGetNumStdOutLinesReq} is next.
   */ 
  GetNumStdErrLines, 

  /**
   * An instance {@link JobGetStdErrLinesReq} is next.
   */ 
  GetStdErrLines, 

  /**
   * An instance {@link JobCloseStdErrReq} is next.
   */ 
  CloseStdErr, 


  /*----------------------------------------------------------------------------------------*/

  /**
   * Simple test of network connectivity.
   */ 
  Ping, 

  /**
   * No more requests will be send over this connection.
   */
  Disconnect,

  /**
   * Order the server to refuse any further requests and then to exit as soon as all
   * currently pending requests have be completed.
   */
  Shutdown;
 
}
