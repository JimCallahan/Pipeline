// $Id: QueueGetHardwareKeysRsp.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.ArrayList;

import us.temerity.pipeline.*;

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

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHardwareKeys():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
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
  
