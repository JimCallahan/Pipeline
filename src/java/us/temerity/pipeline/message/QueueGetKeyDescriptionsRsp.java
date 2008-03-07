// $Id: QueueGetKeyDescriptionsRsp.java,v 1.1 2008/03/07 13:25:21 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*;

import java.util.*;


/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   K E Y   D E S C R I P T I O N S   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The names and descriptions of the currently defined selection, license or hardware keys. 
 */
public
class QueueGetKeyDescriptionsRsp
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
   * @param desc
   *   The key descriptions indexed by key name.
   */ 
  public
  QueueGetKeyDescriptionsRsp
  (
   TaskTimer timer, 
   TreeMap<String,String> desc
  )
  { 
    super(timer);

    if(desc == null) 
      throw new IllegalArgumentException("The key descriptions cannot be (null)!");
    pKeyDescriptions = desc;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getKeyDescriptions():\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the key descriptions indexed by key name.
   */
  public TreeMap<String,String>
  getKeyDescriptions() 
  {
    return pKeyDescriptions;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4624550580818694848L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The key descriptions indexed by key name.
   */ 
  private TreeMap<String,String>  pKeyDescriptions; 

}
  
