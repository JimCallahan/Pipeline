// $Id: QueueJobResults.java,v 1.2 2004/08/22 21:54:05 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B   R E S U L T S                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The results of executing a QueueJob. 
 */
public
class QueueJobResults
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  QueueJobResults()
  {}

  /**
   * Construct a special job results in the event of an exception during job preparation.
   */ 
  public
  QueueJobResults
  (
   Exception ex
  )
  {
    pCommand  = ("Job Prep Failed: " + ex);
    pExitCode = 666;
  }

  /**
   * Construct a new job results.
   * 
   * @param command
   *   The literal command line used to execute the job's regeneration action.
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
  public
  QueueJobResults
  ( 
   String command,
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
    pCommand  = command; 
    pExitCode = exitCode;

    pUserSecs   = userSecs;
    pSystemSecs = systemSecs; 
    
    pAvgResidentSize = avgResidentSize;
    pMaxResidentSize = maxResidentSize;

    pAvgVirtualSize = avgVirtualSize;
    pMaxVirtualSize = maxVirtualSize;

    pPageFaults = pageFaults;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
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
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
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

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
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

  private static final long serialVersionUID = -202297518159187488L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

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
