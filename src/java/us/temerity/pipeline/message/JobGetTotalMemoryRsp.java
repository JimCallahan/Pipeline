// $Id: JobGetTotalMemoryRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   T O T A L   M E M O R Y   R S P                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a request to get the total amount of system memory (in bytes).
 */ 
public
class JobGetTotalMemoryRsp
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
   * @param memory
   *   The total amount of system memory (in bytes).
   */ 
  public
  JobGetTotalMemoryRsp
  (
   TaskTimer timer, 
   long memory
  )
  { 
    super(timer);

    if(memory < 0) 
      throw new IllegalArgumentException("The total memory cannot be negative!");
    pMemory = memory;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "JobMgr.getTotalMemory():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the total amount of system memory (in bytes).
   */
  public long
  getMemory()
  {
    return pMemory;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6772108201747131934L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total amount of system memory (in bytes).
   */ 
  private long  pMemory; 

}
  
