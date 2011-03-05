// $Id: MiscGetSizesRsp.java,v 1.5 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   L O C K   I N F O   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Return detailed information about the holders of all currently existing locks.
 */
public
class MiscGetLockInfoRsp 
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
   * @param info
   *   The lock request info.
   */
  public
  MiscGetLockInfoRsp
  (
   TaskTimer timer, 
   TreeMap<String,RequestInfo> info
  )
  { 
    super(timer);

    pInfo = info;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getLockInfo(): \n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the lock request info.
   */
  public TreeMap<String,RequestInfo>
  getInfo() 
  {
    return pInfo; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2179977531941658916L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The lock request info.
   */ 
  private TreeMap<String,RequestInfo> pInfo; 

}
  
