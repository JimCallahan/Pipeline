// $Id: FileArchiverRsp.java,v 1.1 2005/03/21 08:52:08 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   A R C H I V E R   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileArchiveReq FileArchiveReq} or 
 * {@link FileRestoreReq FileRestoreReq} request.
 */
public
class FileArchiverRsp
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
   * @param output
   *   The STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */
  public
  FileArchiverRsp
  (
   TaskTimer timer, 
   String output
  )
  { 
    super(timer);

    pOutput = output;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString()); 
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */
  public String
  getOutput() 
  {
    return pOutput; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7262758143788564436L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */ 
  private String   pOutput; 

}
  
