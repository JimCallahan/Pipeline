// $Id: JobGetNumProcessorsRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.job;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

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

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "JobMgr.getProcessors():\n  " + getTimer());
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
  
