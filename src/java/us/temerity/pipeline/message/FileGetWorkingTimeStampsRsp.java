// $Id: FileGetWorkingTimeStampsRsp.java,v 1.1 2009/10/28 06:06:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   W O R K I N G   T I M E   S T A M P S   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileGetWorkingTimeStampsReq} request.
 */
public
class FileGetWorkingTimeStampsRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param stamps
   *   The timestamps for each file or <CODE>null</CODE> if a file is missing.
   */
  public
  FileGetWorkingTimeStampsRsp
  (
   TaskTimer timer,
   ArrayList<Long> stamps
  )
  { 
    super(timer);

    if(stamps == null) 
      throw new IllegalArgumentException("The timestamps cannot be (null)!");
    pTimeStamps = stamps;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString()); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets timestamps for each file or <CODE>null</CODE> if a file is missing.
   */
  public ArrayList<Long>
  getTimeStamps() 
  {
    return pTimeStamps; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1790621477649435368L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamps for each file or <CODE>null</CODE> if a file is missing.
   */ 
  private ArrayList<Long> pTimeStamps; 
}
  
