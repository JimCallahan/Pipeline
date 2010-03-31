// $Id: MiscArchiveQueryRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.toolset.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A R C H I V E   Q U E R Y   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscArchiveQueryReq MiscArchiveQueryReq} request.
 */
public
class MiscArchiveQueryRsp
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
   *   Information about the archival state of each matching checked-in version. 
   */ 
  public
  MiscArchiveQueryRsp
  (
   TaskTimer timer, 
   ArrayList<ArchiveInfo> info
  )
  { 
    super(timer);

    if(info == null) 
      throw new IllegalArgumentException("The information cannot be (null)!");
    pInfo = info;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.archiveQuery()\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the information about the archival state of each matching checked-in version. 
   */
  public ArrayList<ArchiveInfo>
  getInfo()
  {
    return pInfo;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6545767021166195285L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The information about the archival state of each matching checked-in version. 
   */ 
  private ArrayList<ArchiveInfo>  pInfo; 

}
  
