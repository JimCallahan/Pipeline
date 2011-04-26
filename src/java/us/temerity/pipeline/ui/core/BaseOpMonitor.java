package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   O P   M O N I T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class of both PanelOpMonitor and DialogOpMonitor. 
 */ 
public abstract
class BaseOpMonitor
  implements OpMonitorable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a response.
   */
  public 
    BaseOpMonitor()
  { 
    pTimer = new TaskTimer();
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
    long wait   = timer.getWaitDuration();
    long active = timer.getActiveDuration();

    pTimer.acquire();
    long total = pTimer.getActiveDuration();
    pTimer.resume();

    String timingMsg = null;
    if((percentage != null) && (percentage > 0.0f)) {
      if(percentage < 1.0f) {
        long remaining = (long) (((1.0f - percentage) * ((float) total)) / percentage);
        timingMsg = (String.format("%1$.1f", percentage * 100.0f) + "% - ETA " + 
                     TimeStamps.formatInterval(remaining));

        if(wait > 1000L) 
          timingMsg += (" - Wait " + TimeStamps.formatInterval(wait));
      }
    }
    else {
      if(wait > 1000L) {
        timingMsg = ("Active " + TimeStamps.formatInterval(active) + 
                     " - Wait " + TimeStamps.formatInterval(wait));
      }
      else {
        timingMsg = ("Total " + TimeStamps.formatInterval(total));
      }
    }

    updateOp(timer.getTitle(), timingMsg, percentage); 
  }

  /**
   * Send the message to the UI.
   *
   * @param msg
   *   A short message describing the operation.
   * 
   * @param timingMsg
   *   A short message describing the amount of time the operation has been or is expected
   *   to be running or <CODE>null</CODE> if no timing information is known.
   * 
   * @param percentage
   *   The completion percentage [0.0, 1.0] if known or <CODE>null</CODE> if unknown.
   */
  protected abstract void 
  updateOp
  (
   String msg , 
   String timingMsg, 
   Float percentage
  );

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Timer of how long since the operation began.
   */
  private TaskTimer pTimer;

}

