// $Id: QueueGetSelectionKeysRsp.java,v 1.1 2004/07/25 03:13:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

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

    Logs.net.finest("QueueMgr.getSelectionKeys():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
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
  
