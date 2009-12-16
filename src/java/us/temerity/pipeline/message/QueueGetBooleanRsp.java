// $Id: QueueGetBooleanRsp.java,v 1.1 2009/12/16 04:13:33 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   B O O L E A N   R S P                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * Get a boolean response from the queue.
 */
public 
class QueueGetBooleanRsp
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
   * @param bool
   *   The boolean value.
   *   
   * @param methodName
   *   The name of the method being called, for use in the logger.
   */ 
  public
  QueueGetBooleanRsp
  (
   TaskTimer timer, 
   Boolean bool,
   String methodName
  )
  { 
    super(timer);

    if(bool == null) 
      throw new IllegalArgumentException("The boolean value cannot be (null)!");
    pBooleanValue = bool;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr." + methodName + "():\n  " + getTimer());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the boolean value.
   */
  public Boolean
  getBooleanValue()
  {
    return pBooleanValue;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4776341635412078460L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private Boolean pBooleanValue;
}
