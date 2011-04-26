package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.NotifyRsp;
import us.temerity.pipeline.math.ExtraMath;

import java.io.*;
import java.net.*;
import java.util.Locale;

/*------------------------------------------------------------------------------------------*/
/*   N E T   O P   N O T I F I E R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A helper class used by server operations to communicate the progress of a potentially 
 * long and expensive operation back to clients over a network socket.
 */ 
public 
class NetOpNotifier
  extends BaseOpNotifier
  implements OpNotifiable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a operation progress helper.
   * 
   * @param objOut
   *   The object stream connected to the network client.
   */
  public
  NetOpNotifier
  (
   ObjectOutput objOut
  )
  {
    super();

    pObjOut = objOut;
  }

    
  
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
  ) 
  {
    try {
      pObjOut.writeObject(new NotifyRsp(timer, msg, null)); 
      pObjOut.flush(); 
    }
    catch(IOException ex) {
      // ignore... 
    }
  }

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
  ) 
  {
    try {
      pObjOut.writeObject(new NotifyRsp(timer, msg, percentage)); 
      pObjOut.flush(); 
    }
    catch(IOException ex) {
      // ignore... 
    }
  }

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
  ) 
  {
    steps(timer, null, 1L);
  }

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
  ) 
  {
    steps(timer, msg, 1L);
  }

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
  ) 
  {
    incrementCompleted(completed);

    try {
      pObjOut.writeObject(new NotifyRsp(timer, msg, getPercentage())); 
      pObjOut.flush(); 
    }
    catch(IOException ex) {
      // ignore... 
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The object stream connected to the network client.
   */ 
  private ObjectOutput pObjOut;

}

