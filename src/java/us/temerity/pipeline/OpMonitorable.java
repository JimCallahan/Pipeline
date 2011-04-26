// $Id: JobRank.java,v 1.6 2009/12/11 04:21:10 jesse Exp $

package us.temerity.pipeline;

/*------------------------------------------------------------------------------------------*/
/*   O P   M O N I T O R A B L E                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The interface of classes which monitor the progress of server operations.
 */ 
public 
interface OpMonitorable
{
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
  );

}

