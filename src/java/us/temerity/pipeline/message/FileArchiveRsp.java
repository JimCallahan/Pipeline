// $Id: FileArchiveRsp.java,v 1.1 2005/03/10 08:07:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileArchiveReq FileArchiveReq} request.
 */
public
class FileArchiveRsp
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
   * @param name
   *   The name of the create archive.
   * 
   * @param output
   *   The STDOUT output of the archiver process or <CODE>null</CODE> if none exists.
   */
  public
  FileArchiveRsp
  (
   TaskTimer timer, 
   String name, 
   String output
  )
  { 
    super(timer);

    pOutput = output;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "FileMgr.archive(): " + name + "\n  " + getTimer());
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
  
