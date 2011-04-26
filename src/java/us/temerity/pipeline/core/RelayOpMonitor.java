// $Id: JobRank.java,v 1.6 2009/12/11 04:21:10 jesse Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   R E L A Y   O P   M O N I T O R                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A operation monitor that relays updates from one client into notifications.
 */ 
public 
class RelayOpMonitor
  implements OpMonitorable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  public 
  RelayOpMonitor
  (
   OpNotifiable opn
  ) 
  {
    pOpNotifier = opn;
  }
  

   
  /*----------------------------------------------------------------------------------------*/
  /*   O P   M O N I T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle a change in the status of an operation running on the server.
   * 
   * @param timer
   *   The current operation execution timer.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  public void 
  update
  (
   TaskTimer timer, 
   Float percentage 
  )
  {
    pOpNotifier.notify(timer, timer.getTitle(), percentage);                       
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Timer of how long since the operation began.
   */
  private OpNotifiable  pOpNotifier;

}

