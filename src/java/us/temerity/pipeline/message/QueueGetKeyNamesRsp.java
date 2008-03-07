// $Id: QueueGetKeyNamesRsp.java,v 1.1 2008/03/07 13:25:21 jim Exp $

package us.temerity.pipeline.message;

import java.util.TreeSet;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.TaskTimer;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   K E Y   N A M E S   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the names of the currently defined selection, license or hardware keys. 
 */
public
class QueueGetKeyNamesRsp
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
  QueueGetKeyNamesRsp
  (
   TaskTimer timer, 
   TreeSet<String> names
  )
  { 
    super(timer);

    if(names == null) 
      throw new IllegalArgumentException("The key names cannot be (null)!");
    pKeyNames = names;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getKeyNames():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the key names.
   */
  public TreeSet<String>
  getKeyNames() 
  {
    return pKeyNames;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3674289882026123612L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The key names.
   */ 
  private TreeSet<String>  pKeyNames; 

}
  
