// $Id: MiscGetSizesRsp.java,v 1.5 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.misc;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   L O C K   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to acquiring a lock.
 */
public
class MiscGetLockRsp 
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
   * @param lockID
   *  The unique ID of the lock obtained when it was acquired. 
   */
  public
  MiscGetLockRsp
  (
   TaskTimer timer, 
   Long lockID   
  )
  { 
    super(timer);

    if(lockID == null) 
      throw new IllegalArgumentException("The lockID cannot be (null)!");
    pLockID = lockID;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique ID of the lock obtained when it was acquired. 
   */
  public Long
  getLockID() 
  {
    return pLockID; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1570958170843447567L; 

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique ID of the lock obtained when it was acquired. 
   */ 
  private Long pLockID; 

}
  
