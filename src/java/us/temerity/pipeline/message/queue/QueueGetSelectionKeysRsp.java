// $Id: QueueGetSelectionKeysRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   S E L E C T I O N   K E Y S   R S P                                */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the currently defined selection keys. 
 */
public
class QueueGetSelectionKeysRsp
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
   * @param keys
   *   The selection keys 
   */ 
  public
  QueueGetSelectionKeysRsp
  (
   TaskTimer timer, 
   ArrayList<SelectionKey> keys
  )
  { 
    super(timer);

    if(keys == null) 
      throw new IllegalArgumentException("The selection keys cannot be (null)!");
    pKeys = keys;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getSelectionKeys():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the selection keys.
   */
  public ArrayList<SelectionKey>
  getKeys() 
  {
    return pKeys;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7711452537599078957L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The selection keys.
   */ 
  private ArrayList<SelectionKey>  pKeys;

}
  
