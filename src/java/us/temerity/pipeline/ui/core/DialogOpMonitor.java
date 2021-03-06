package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   D I A L O G   O P   M O N I T O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Updates the UIMaster root panel in response to server progress messages.
 */ 
public 
class DialogOpMonitor
  extends BaseOpMonitor
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a response.
   * 
   * @param dialogID
   *   The unique ID of the dialog. 
   */
  public 
  DialogOpMonitor
  ( 
   long dialogID
  )
  { 
    super();

    pDialogID = dialogID;
  }

  
   
  /*----------------------------------------------------------------------------------------*/
  /*   O P   M O N I T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

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
  protected void 
  updateOp
  (
   String msg, 
   String timingMsg, 
   Float percentage
  ) 
  {
    UIMaster master = UIMaster.getInstance();
    master.updateDialogOp(pDialogID, msg, timingMsg, percentage); 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique ID of the dialog. 
   */ 
  private long pDialogID;

}

