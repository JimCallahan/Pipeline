// $Id: MiscGetSizesRsp.java,v 1.5 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   I S   L O C K   R S P                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Return whether the given lock is currently held.
 */
public
class MiscIsLockedRsp 
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
   * @param isLocked
   *   Whether the given lock is currently held.
   */
  public
  MiscIsLockedRsp
  (
   TaskTimer timer, 
   boolean isLocked
  )
  { 
    super(timer);

    pIsLocked = isLocked;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given lock is currently held.
   */
  public boolean
  isLocked() 
  {
    return pIsLocked; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3643527937465278364L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the given lock is currently held.
   */ 
  private boolean pIsLocked; 

}
  
