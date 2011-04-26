// $Id: SuccessRsp.java,v 1.10 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O T I F Y   R S P                                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A response which communicates information about the progress of an operation underway.
 */
public
class NotifyRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a progress response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param message
   *   An update message about the progress of the operation or 
   *   <CODE>null</CODE> if there are no new messages.
   * 
   * @param percentage
   *   An update of the estimated percentage complete or 
   *   <CODE>null</CODE> if no estimate is available.
   */
  public
  NotifyRsp
  (
   TaskTimer timer, 
   String message, 
   Float percentage
  ) 
  {
    super(timer, message); 
    pPercentage = percentage; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get an update of the estimated percentage complete or 
   * <CODE>null</CODE> if no estimate is available.
   */ 
  public Float
  getPercentage() 
  {
    return pPercentage;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8980608865978643692L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timing statistics for a task.
   */ 
  private TaskTimer  pTimer;

  /**
   * An update message about the progress of the operation or 
   * <CODE>null</CODE> if there are no new messages.
   */
  private String pMessage; 
  
  /**
   * An update of the estimated percentage complete or 
   * <CODE>null</CODE> if no estimate is available.
   */
  private Float pPercentage;
 
}
  
