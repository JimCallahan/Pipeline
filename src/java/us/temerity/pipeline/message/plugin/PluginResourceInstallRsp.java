// $Id: PluginResourceInstallRsp.java,v 1.2 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.plugin;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

/*------------------------------------------------------------------------------------------*/
/*    P L U G I N S   C O U N T   R S P                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that there are required plugins that need to be installed.  
 * However, this response is not an error, it is a SuccessRsp with extra information.
 */
public 
class PluginResourceInstallRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a PluginsCount response.
   *
   * @param timer
   *   The timing statistics for a task.
   *
   * @param sessionID
   *   The number of required plugins that need to be installed.
   */
  public
  PluginResourceInstallRsp
  (
   TaskTimer timer, 
   long sessionID
  )
  { 
    super(timer);

    pSessionID = sessionID;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */
  public long
  getSessionID() 
  {
    return pSessionID;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8374345663387728866L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *
   */ 
  private long  pSessionID;

}

