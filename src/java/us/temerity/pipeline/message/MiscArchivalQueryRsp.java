// $Id: MiscArchivalQueryRsp.java,v 1.3 2005/01/22 06:10:09 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V A L   Q U E R Y   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscArchivalQueryReq MiscArchivalQueryReq} 
 * request.
 */
public
class MiscArchivalQueryRsp
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
   *   Archival information for each matching checked-in version indexed by fully resolved 
   *   node name and revision number.
   */ 
  public
  MiscArchivalQueryRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,ArchivalInfo>> info
  )
  { 
    super(timer);

    if(info == null) 
      throw new IllegalArgumentException("The archival information cannot be (null)!");
    pInfo = info;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.archivalQuery()\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the archival info.
   */
  public TreeMap<String,TreeMap<VersionID,ArchivalInfo>> 
  getInfo()
  {
    return pInfo;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8111998008065857229L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The archival info.
   */ 
  private TreeMap<String,TreeMap<VersionID,ArchivalInfo>>  pInfo;

}
  
