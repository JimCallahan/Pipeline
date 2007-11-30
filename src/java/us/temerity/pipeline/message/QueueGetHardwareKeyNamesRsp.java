// $Id: QueueGetHardwareKeyNamesRsp.java,v 1.1 2007/11/30 20:06:25 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.TaskTimer;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H A R D W A R E   K E Y   N A M E S   R S P                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names of the currently defined hardware keys. 
 */
public
class QueueGetHardwareKeyNamesRsp
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
   * @param names
   *   The hardware key names
   */ 
  public
  QueueGetHardwareKeyNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The hardware key names cannot be (null)!");
    pKeyNames = names;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getHardwareKeyNames():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the hardware key names.
   */
  public TreeSet<String>
  getKeyNames() 
  {
    return pKeyNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5749646765813090207L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hardware key names.
   */ 
  private TreeSet<String>  pKeyNames; 

}
  
