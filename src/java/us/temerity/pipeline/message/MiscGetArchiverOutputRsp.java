// $Id: MiscGetArchiverOutputRsp.java,v 1.1 2005/04/03 01:54:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E R   O U T P U T   R S P                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetArchivedOutputReq MiscGetArchivedOutputReq} or 
 * {@link MiscGetRestoredOutputReq MiscGetRestoredOutputReq} request.
 */
public
class MiscGetArchiverOutputRsp
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
   * @param output
   *   The STDOUT output from the Archiver plugin.
   */ 
  public
  MiscGetArchiverOutputRsp
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
   * Gets the STDOUT output from the Archiver plugin.
   */
  public String
  getOutput()
  {
    return pOutput;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7961233303381812720L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The STDOUT output from the Archiver plugin.
   */ 
  private String  pOutput; 

}
  
