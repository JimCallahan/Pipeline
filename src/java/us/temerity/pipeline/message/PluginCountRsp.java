// $Id: PluginCountRsp.java,v 1.1 2009/02/11 16:32:39 jlee Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

/*------------------------------------------------------------------------------------------*/
/*    P L U G I N S   C O U N T   R S P                                                     */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which signifies that there are required plugins that need to be installed.  
 * However, this response is not an error, it is a SuccessRsp with extra information.
 */
public 
class PluginCountRsp
  extends SuccessRsp
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
   * @param requiredPluginCount
   *   The number of required plugins that need to be installed.
   *
   * @param unknownPluginCount
   *   The number of unregistered plugins detected that may need to be reinstalled.
   */
  public
  PluginCountRsp
  (
   TaskTimer timer, 
   int requiredPluginCount, 
   int unknownPluginCount
  )
  { 
    super(timer);

    pRequiredPluginCount = requiredPluginCount;
    pUnknownPluginCount  = unknownPluginCount;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the number of required plugins that need to be installed.
   */
  public int
  getRequiredPluginCount() 
  {
    return pRequiredPluginCount;
  }

  /**
   * Gets the number of unregistered plugins detected during plpluginmgr startup.
   */
  public int
  getUnknownPluginCount()
  {
    return pUnknownPluginCount;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5311608991962495929L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The number of required plugins that need to be installed.
   */ 
  private int pRequiredPluginCount;

  /**
   * The number of unregistered plugins detected during plpluginmgr startup.
   */
  private int pUnknownPluginCount;

}
