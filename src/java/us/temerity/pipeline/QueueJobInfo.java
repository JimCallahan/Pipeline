// $Id: QueueJobInfo.java,v 1.1 2004/07/24 18:28:45 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B  I N F O                                                             */
/*------------------------------------------------------------------------------------------*/

/**
 * Information about the current status of a QueueJob in the Pipeline queue.
 */
public
class QueueJobInfo
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  QueueJobInfo()
  {}

  /**
   * Construct a new job information.
   * 
   * @param jobID
   *   The unique job identifier.
   */ 
  public
  QueueJobInfo
  (
   long jobID
  ) 
  {
    if(jobID < 0) 
      throw new IllegalArgumentException
	("The job ID (" + jobID + ") must be positive!");
    pJobID = jobID;

    pState = QueueState.Queued;
    pSubmittedStamp = Dates.now();
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the unique job identifier.
   */ 
  public long 
  getJobID() 
  {
    return pJobID;
  }

  /**
   * Get the queue state of the job.
   */
  public QueueState
  getState() 
  {
    return pState;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the timestamp of when the job was submitted to the queue.
   */ 
  public Date 
  getSubmittedStamp() 
  {
    return pSubmittedStamp;
  }
     
  /**
   * Get the timestamp of when the job was started to a host for execution.
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job was never started.
   */ 
  public Date 
  getStartedStamp() 
  {
    return pStartedStamp;
  }
     
  /**
   * Get the timestamp of when the job completed. 
   * 
   * @return 
   *   The timestamp or <CODE>null</CODE> if the job has not completed yet.
   */ 
  public Date 
  getCompletedStamp() 
  {
    return pCompletedStamp;
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The full name of the host assigned to execute the job.
   * 
   * @return 
   *   The hostname or <CODE>null</CODE> if the job was never assigned to a specific host.
   */ 
  public String 
  getHostname() 
  {
    return pHostname;   
  }
  
  /**
   * The literal command line used to execute the job's regeneration action.
   * 
   * @return 
   *   The command line or <CODE>null</CODE> if the job was never executed.
   */ 
  public String 
  getCommand() 
  {
    return pCommand;
  }

  /**
   * The exit code of the subprocess generated to execute the job's regeneration action.
   * 
   * @return 
   *   The exit code or <CODE>null</CODE> if the job was never executed.
   */ 
  public Integer 
  getExitCode() 
  {
    return pExitCode;
  }
  

  /** 
   * The number of seconds the regeneration action subprocess was running in user space.
   * 
   * @return 
   *    The user seconds or <CODE>null</CODE> if the job was never executed.
   */ 
  public Double
  getUserSecs() 
  {
    return pUserSecs;
  }

  /** 
   * The number of seconds the regeneration action subprocess was running in system 
   * (kernel) space.
   * 
   * @return
   *   The system seconds or <CODE>null</CODE> if the job was never executed.
   */ 
  public Double  
  getSystemSecs() 
  {
    return pSystemSecs;
  }


  /**
   * The average resident memory size of the regeneration action subprocess in kilobytes.
   * 
   * @return 
   *   The memory size or <CODE>null</CODE> if the job was never executed.
   */ 
  public Long
  getAverageResidentSize() 
  {
    return pAvgResidentSize;
  }

  /**
   * The maximum resident memory size of the regeneration action subprocess in kilobytes.
   * 
   * @return 
   *    The memory size or <CODE>null</CODE> if the job was never executed.
   */ 
  public Long 
  getMaximumResidentSize() 
  {
    return pMaxResidentSize;
  }
  

  /**
   * The average virtual memory size of the regeneration action subprocess in kilobytes.
   * 
   * @return 
   *   The memory size or <CODE>null</CODE> if the job was never executed.
   */ 
  public Long
  getAverageVirtualSize() 
  {
    return pAvgVirtualSize;
  }

  /**
   * The maximum virtual memory size of the regeneration action subprocess in kilobytes.
   * 
   * @return 
   *    The memory size or <CODE>null</CODE> if the job was never executed.
   */ 
  public Long 
  getMaximumVirtualSize() 
  {
    return pMaxVirtualSize;
  }
  

  /**
   * The total number of hard page faults during execution of the regeneration action 
   * subprocess. <P> 
   * 
   * A hard page fault is a memory fault that required I/O operations.
   * 
   * @return 
   *   The number of page faults or <CODE>null</CODE> if the job was never executed.
   */ 
  public Long
  getPageFaults() 
  {
    return pPageFaults; 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A G E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Records the job has started execution. 
   * 
   * @param hostname
   *   The full name of the host executing the job.
   * 
   * @param command
   *   The literal command line used to execute the job's regeneration action.
   */ 
  public void 
  started
  (
   String hostname, 
   String command
  ) 
  {
    if(hostname == null) 
      throw new IllegalArgumentException
	("The hostname cannot be (null)!");
    pHostname = hostname; 

    if(command == null) 
      throw new IllegalArgumentException
	("The command cannot be (null)!");
    pCommand = command; 

    pStartedStamp = Dates.now();
    pState = QueueState.Running;
  }
  

  /**
   * Records that the job was aborted (cancelled) prematurely by the user.
   */ 
  public void 
  aborted() 
  {
    pCompletedStamp = Dates.now();
    pState = QueueState.Aborted;
  }

  /**
   * Records that the job has exited normally.
   * 
   * @param exitCode
   *   The exit code of the subprocess.
   * 
   * @param userSecs 
   *   The number of seconds the subprocess was running in user space.
   * 
   * @param systemSecs
   *   The number of seconds the subprocess was running in system space.
   * 
   * @param avgResidentSize
   *   The average resident memory size of the subprocess in kilobytes.
   * 
   * @param maxResidentSize
   *   The maximum resident memory size of the subprocess in kilobytes.
   * 
   * @param avgVirtualSize
   *   The average virtual memory size of the subprocess in kilobytes.
   * 
   * @param maxVirtualSize
   *   The maximum virtual memory size of the subprocess in kilobytes.
   * 
   * @param pageFaults
   *   The total number of hard page faults during execution of the subprocess.
   */ 
  public void 
  exited
  (
   int exitCode, 
   double userSecs, 
   double systemSecs, 
   long avgResidentSize, 
   long maxResidentSize, 
   long avgVirtualSize, 
   long maxVirtualSize, 
   long pageFaults   
  ) 
  {
    pExitCode = exitCode;

    pUserSecs   = userSecs;
    pSystemSecs = systemSecs; 
    
    pAvgResidentSize = avgResidentSize;
    pMaxResidentSize = maxResidentSize;

    pAvgVirtualSize = avgVirtualSize;
    pMaxVirtualSize = maxVirtualSize;

    pPageFaults = pageFaults;

    pCompletedStamp = Dates.now();
    pState = (pExitCode == SubProcess.SUCCESS) ? QueueState.Finished : QueueState.Failed;
  }




  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("JobID", pJobID);
    encoder.encode("State", pState);

    {
      encoder.encode("SubmittedStamp", pSubmittedStamp);
      
      if(pStartedStamp != null) 
	encoder.encode("StartedStamp", pStartedStamp);

      if(pCompletedStamp != null) 
	encoder.encode("CompletedStamp", pCompletedStamp);
    }
    
    {
      if(pHostname != null) 
	encoder.encode("Hostname", pHostname);
      
      if(pCommand != null) 
	encoder.encode("Command", pCommand);
            
      if(pExitCode != null) 
	encoder.encode("ExitCode", pExitCode);
      
      if(pUserSecs != null) 
	encoder.encode("UserSecs", pUserSecs);
      
      if(pSystemSecs != null) 
	encoder.encode("SystemSecs", pSystemSecs);
      
      if(pAvgResidentSize != null) 
	encoder.encode("AvgResidentSize", pAvgResidentSize);
      
      if(pMaxResidentSize != null) 
	encoder.encode("MaxResidentSize", pMaxResidentSize);

      if(pAvgVirtualSize != null) 
	encoder.encode("AvgVirtualSize", pAvgVirtualSize);
      
      if(pMaxVirtualSize != null) 
	encoder.encode("MaxVirtualSize", pMaxVirtualSize);
      
      if(pPageFaults != null) 
	encoder.encode("PageFaults", pPageFaults);
    }
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long jobID = (Long) decoder.decode("JobID"); 
    if(jobID == null) 
      throw new GlueException("The \"JobID\" was missing!");
    pJobID = jobID;
        
    QueueState state = (QueueState) decoder.decode("State"); 
    if(state == null) 
      throw new GlueException("The \"State\" was missing!");
    pState = state;

    {
      Date date = (Date) decoder.decode("SubmittedStamp"); 
      if(date == null) 
	throw new GlueException("The \"SubmittedStamp\" was missing!");
      pSubmittedStamp = date;
    }

    {
      Date date = (Date) decoder.decode("StartedStamp"); 
      if(date != null) 
	pStartedStamp = date;
    }

    {
      Date date = (Date) decoder.decode("CompletedStamp"); 
      if(date != null) 
	pCompletedStamp = date;
    }

    {
      String host = (String) decoder.decode("Hostname"); 
      if(host != null) 
	pHostname = host;
    }

    {
      String cmd = (String) decoder.decode("Command"); 
      if(cmd != null) 
	pCommand = cmd;
    }
    
    {
      Integer code = (Integer) decoder.decode("ExitCode"); 
      if(code != null) 
	pExitCode = code;
    }


    {
      Double secs = (Double) decoder.decode("UserSecs"); 
      if(secs != null) 
	pUserSecs = secs;
    }

    {
      Double secs = (Double) decoder.decode("SystemSecs"); 
      if(secs != null) 
	pSystemSecs = secs;
    }

    
    {
      Long size = (Long) decoder.decode("AvgResidentSize"); 
      if(size != null) 
	pAvgResidentSize = size;
    }

    {
      Long size = (Long) decoder.decode("MaxResidentSize"); 
      if(size != null) 
	pMaxResidentSize = size;
    }
    

    {
      Long size = (Long) decoder.decode("AvgVirtualSize"); 
      if(size != null) 
	pAvgVirtualSize = size;
    }

    {
      Long size = (Long) decoder.decode("MaxVirtualSize"); 
      if(size != null) 
	pMaxVirtualSize = size;
    }

    
    {
      Long faults = (Long) decoder.decode("PageFaults"); 
      if(faults != null) 
	pPageFaults = faults;
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4135404054158504196L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique job identifier.
   */ 
  private long  pJobID;

  /**
   * The queue status of the job. 
   */
  private QueueState  pState;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the job was submitted to the queue.
   */ 
  private Date  pSubmittedStamp;

  /**
   * The timestamp of when the job was started to a host for execution.
   */ 
  private Date  pStartedStamp;

  /**
   * The timestamp of when the job completed. <P> 
   * 
   * Completion may be due to the job being aborted or killed in addition to normal exit
   * of the action process.
   */ 
  private Date  pCompletedStamp;

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * The full name of the host assigned to execute the job or <CODE>null</CODE> if the 
   * job was never assigned to a specific host.
   */ 
  private String pHostname;   

  /**
   * The literal command line used to execute the job's regeneration action or 
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private String pCommand;

  /**
   * The exit code of the subprocess generated to execute the job's regeneration action or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Integer pExitCode;


  /** 
   * The number of seconds the regeneration action subprocess was running in user space or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Double  pUserSecs;

  /** 
   * The number of seconds the regeneration action subprocess was running in system 
   * (kernel) space or <CODE>null</CODE> if the job was never executed.
   */ 
  private Double  pSystemSecs;


  /**
   * The average resident memory size of the regeneration action subprocess in kilobytes or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Long  pAvgResidentSize;

  /**
   * The maximum resident memory size of the regeneration action subprocess in kilobytes or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Long  pMaxResidentSize;
  

  /**
   * The average virtual memory size of the regeneration action subprocess in kilobytes or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Long  pAvgVirtualSize;

  /**
   * The maximum virtual memory size of the regeneration action subprocess in kilobytes or
   * <CODE>null</CODE> if the job was never executed.
   */ 
  private Long  pMaxVirtualSize;
  

  /**
   * The total number of hard page faults during execution of the regeneration action 
   * subprocess or <CODE>null</CODE> if the job was never executed. <P> 
   * 
   * A hard page fault is a memory fault that required I/O operations.
   */ 
  private Long  pPageFaults; 

  
}
