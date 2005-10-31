// $Id: QueueJobResults.java,v 1.6 2005/10/31 14:46:14 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
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
    pTimeStamp = new Date(); 
    pCommand   = ("Job Prep Failed: " + ex.getMessage() + "\n" + 
		  "(see Error log for details)");
    pExitCode  = 666;
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
   * @param userTime
   *   The number of seconds the subprocess and its children have been scheduled in 
   *   user mode.
   * 
   * @param systemTime
   *   The number of seconds the subprocess and its children have been scheduled in 
   *   kernal mode.
   * 
   * @param virtualSize
   *   The maximum virtual memory size of the subprocess and its children in bytes.
   * 
   * @param residentSize
   *   The maximum resident memory size of the subprocess and its children in bytes.
   * 
   * @param swappedSize 
   *   The cumilative amount of memory swapped by the process and its children in bytes.
   * 
   * @param pageFaults
   *   The number of major faults which occured for the process and its children which 
   *   have required loading a memory page from disk.
   */ 
  public
  QueueJobResults
  ( 
   String command,
   int exitCode, 
   Double userTime, 
   Double systemTime, 
   Long virtualSize, 
   Long residentSize, 
   Long swappedSize, 
   Long pageFaults   
  )
  {
    pTimeStamp    = new Date(); 
    pCommand      = command; 
    pExitCode     = exitCode;
    pUserTime     = userTime;
    pSystemTime   = systemTime;
    pVirtualSize  = virtualSize;
    pResidentSize = residentSize;
    pSwappedSize  = swappedSize;
    pPageFaults   = pageFaults;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the time stamp of when the results where recorded.
   */ 
  public Date 
  getTimeStamp() 
  {
    return pTimeStamp;
  } 


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
  
  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the number of seconds the subprocess and its children have been scheduled in 
   * user mode.
   *  
   * @return 
   *    The user seconds or <CODE>null</CODE> if unknown.
   */ 
  public Double
  getUserTime() 
  {
    return pUserTime;
  }

  /** 
   * Get the number of seconds the subprocess and its children have been scheduled in 
   * kernel mode.
   *  
   * @return 
   *    The system seconds or <CODE>null</CODE> if unknown.
   */ 
  public Double
  getSystemTime() 
  {
    return pSystemTime;
  }

  /**
   * Get the number of major faults which occured for the process and its children which 
   * have required loading a memory page from disk.
   * 
   * @return 
   *   The number of faults or <CODE>null</CODE> if unknown.
   */
  public Long
  getPageFaults() 
  {
    return pPageFaults;
  }

  /**
   * Get the maximum virtual memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getVirtualSize() 
  {
    return pVirtualSize;
  }

  /**
   * Get the maximum resident memory size of the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getResidentSize() 
  {
    return pResidentSize;
  }

  /**
   * Get the cumilative amount of memory swapped by the process and its children in bytes.
   * 
   * @return 
   *   The size in bytes or <CODE>null</CODE> if unknown.
   */
  public Long
  getSwappedSize()  
  {
    return pSwappedSize;
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
    encoder.encode("TimeStamp", pTimeStamp.getTime());

    if(pCommand != null) 
      encoder.encode("Command", pCommand);
    
    if(pExitCode != null) 
      encoder.encode("ExitCode", pExitCode);
    
    if(pUserTime != null) 
      encoder.encode("UserTime", pUserTime);
    
    if(pSystemTime != null) 
      encoder.encode("SystemTime", pSystemTime);
      
    if(pPageFaults != null) 
      encoder.encode("PageFaults", pPageFaults);

    if(pVirtualSize != null) 
      encoder.encode("VirtualSize", pVirtualSize);

    if(pResidentSize != null) 
      encoder.encode("ResidentSize", pResidentSize);

    if(pSwappedSize != null) 
      encoder.encode("SwappedSize", pSwappedSize);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    {
      Long stamp = (Long) decoder.decode("TimeStamp"); 
      if(stamp == null) 
	throw new GlueException
	  ("The \"TimeStamp\" was missing!");
      pTimeStamp = new Date(stamp);
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
      Double time = (Double) decoder.decode("UserTime"); 
      if(time != null) 
	pUserTime = time;
    }

    {
      Double time = (Double) decoder.decode("SystemTime"); 
      if(time != null) 
	pSystemTime = time;
    }
    
    {
      Long faults = (Long) decoder.decode("PageFaults"); 
      if(faults != null) 
	pPageFaults = faults;
    }

    {
      Long size = (Long) decoder.decode("VirtualSize"); 
      if(size != null) 
	pVirtualSize = size;
    }

    {
      Long size = (Long) decoder.decode("ResidentSize"); 
      if(size != null) 
	pResidentSize = size;
    }

    {
      Long size = (Long) decoder.decode("SwappedSize"); 
      if(size != null) 
	pSwappedSize = size;
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
   * The timestamp of when the results where recorded.
   */ 
  private Date  pTimeStamp;

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
   * The number of seconds the subprocess and its children have been scheduled in 
   * user mode or <CODE>null</CODE> if unknown.
   */  
  private Double  pUserTime;

  /** 
   * The number of seconds the subprocess and its children have been scheduled in 
   * kernel mode or <CODE>null</CODE> if unknown.
   */
  private Double  pSystemTime;

  /**
   * The number of major faults which occured for the process and its children which 
   * have required loading a memory page from disk or <CODE>null</CODE> if unknown.
   */
  private Long  pPageFaults;      

  /**
   * The maximum virtual memory size of the process and its children in bytes or 
   * <CODE>null</CODE> if unknown.
   */
  private Long  pVirtualSize;

  /**
   * The maximum resident memory size of the process and its children in bytes or 
   * <CODE>null</CODE> if unknown.
   */
  private Long pResidentSize;

  /**
   * The cumilative amount of memory swapped by the process and its children in bytes 
   * or <CODE>null</CODE> if unknown.
   */
  private Long pSwappedSize;
}
