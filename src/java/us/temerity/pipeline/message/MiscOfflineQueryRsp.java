// $Id: MiscOfflineQueryRsp.java,v 1.2 2005/03/23 20:45:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   O F F L I N E   Q U E R Y   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscOfflineQueryReq MiscOfflineQueryReq} request.
 */
public
class MiscOfflineQueryRsp
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
   * @param info
   *   Information about the offline state of each matching checked-in version. 
   */ 
  public
  MiscOfflineQueryRsp
  (
   TaskTimer timer, 
   ArrayList<OfflineInfo> info
  )
  { 
    super(timer);

    if(info == null) 
      throw new IllegalArgumentException("The information cannot be (null)!");
    pInfo = info;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.offlineQuery()\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the information about the offline state of each matching checked-in version. 
   */
  public ArrayList<OfflineInfo>
  getInfo()
  {
    return pInfo;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6170755369251855096L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The information about the offline state of each matching checked-in version. 
   */ 
  private ArrayList<OfflineInfo>  pInfo; 

}
  
