// $Id: QueueGetHardwareKeysRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.queue;

import java.util.ArrayList;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H A R D W A R E   K E Y S   R S P                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the currently defined hardware keys. 
 */
public
class QueueGetHardwareKeysRsp
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
   *   The hardware keys 
   */ 
  public
  QueueGetHardwareKeysRsp
  (
   TaskTimer timer, 
   ArrayList<HardwareKey> keys
  )
  { 
    super(timer);

    if(keys == null) 
      throw new IllegalArgumentException("The hardware keys cannot be (null)!");
    pKeys = keys;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHardwareKeys():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the hardware keys.
   */
  public ArrayList<HardwareKey>
  getKeys() 
  {
    return pKeys;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3588425573175019731L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware keys.
   */ 
  private ArrayList<HardwareKey>  pKeys;

}
  
