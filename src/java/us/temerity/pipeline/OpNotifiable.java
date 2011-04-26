package us.temerity.pipeline;

import us.temerity.pipeline.message.NotifyRsp;
import us.temerity.pipeline.math.ExtraMath;

import java.io.*;
import java.net.*;
import java.util.Locale;

/*------------------------------------------------------------------------------------------*/
/*   O P   N O T I F I A B L E                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * The interface of the network and direct helper classes used by server operations to 
 * communicate the progress of a potentially long and expensive operation back to clients.
 */ 
public 
interface OpNotifiable
{
  /*----------------------------------------------------------------------------------------*/
  /*  A C C E S S                                                                           */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Set the total number of steps required to complete the operation.
   */
  public void 
  setTotalSteps
  (
   long total
  );

  
  
  /*----------------------------------------------------------------------------------------*/
  /*  N O T I F I C A T I O N S                                                             */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Notify the client of progress.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   The progress message.
   */
  public void 
  notify
  (
   TaskTimer timer,
   String msg
  );

  /**
   * Notify the client of progress.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   The progress message.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  public void 
  notify
  (
   TaskTimer timer,
   String msg, 
   Float percentage
  );

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   */
  public void 
  step
  (
   TaskTimer timer
  );

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   A message describing the step. 
   */
  public void 
  step
  (
   TaskTimer timer, 
   String msg   
  );

  /**
   * Notify the client that a step has been completed.
   * 
   * @param timer
   *   The current task timer.
   * 
   * @param msg
   *   A message describing the step. 
   * 
   * @param completed
   *   The number of steps completed. 
   */
  public void 
  steps
  (
   TaskTimer timer, 
   String msg, 
   long completed
  );
}

