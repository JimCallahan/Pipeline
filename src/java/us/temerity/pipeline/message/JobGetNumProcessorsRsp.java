// $Id: JobGetNumProcessorsRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   N U M   P R O C E S S O R S   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a request to get the number of processors (CPUs).
 */ 
public
class JobGetNumProcessorsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   *
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param procs 
   *   The number of processors. 
   */ 
  public
  JobGetNumProcessorsRsp
  (
   TaskTimer timer, 
   int procs 
  )
  { 
    super(timer);

    if(procs <= 0) 
      throw new IllegalArgumentException("The number of processors must be positive!");
    pProcs = procs;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"JobMgr.getProcessors():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the number of processors. 
   */
  public int
  getProcessors()
  {
    return pProcs; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1272402769565834189L; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of processors. 
   */ 
  private int  pProcs; 

}
  
