// $Id: NotifyWatchRsp.java,v 1.1 2004/04/11 19:31:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   W A T C H   R S P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NotifyWatchReq NotifyWatchReq} request.
 */
public
class NotifyWatchRsp
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
   * @param dirs
   *   The modified directories.
   */
  public
  NotifyWatchRsp
  (
   TaskTimer timer, 
   HashSet<File> dirs
  )
  { 
    super(timer);

    if(dirs == null) 
      throw new IllegalArgumentException("The modified directories cannot be (null)!");
    pDirs = dirs;

    Logs.net.finest("DNotify.watch():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the modified directories.
   */
  public HashSet<File>
  getDirs() 
  {
    return pDirs;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5394819276643961446L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The modified directories.
   */
  private HashSet<File>  pDirs;

}
  
