// $Id: JobGetTotalDiskRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   J O B   T O T A L   D I S K   R S P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a request to get the size of the temporary disk drive (in bytes).
 */ 
public
class JobGetTotalDiskRsp
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
   * @param disk
   *   The size of the temporary disk drive (in bytes).
   */ 
  public
  JobGetTotalDiskRsp
  (
   TaskTimer timer, 
   long disk
  )
  { 
    super(timer);

    if(disk < 0) 
      throw new IllegalArgumentException("The disk size cannot be negative!");
    pDisk = disk;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"JobMgr.getTotalDisk():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the size of the temporary disk drive (in bytes).
   */
  public long
  getDisk()
  {
    return pDisk;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6055954572824820509L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The size of the temporary disk drive (in bytes).
   */ 
  private long  pDisk; 

}
  
