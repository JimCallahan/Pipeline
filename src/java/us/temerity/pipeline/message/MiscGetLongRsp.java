// $Id: MiscGetLongRsp.java,v 1.1 2009/12/19 21:14:28 jesse Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   L O N G   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Get a long response from a server.
 */
public 
class MiscGetLongRsp
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
   * @param value 
   *   The Long value.
   *   
   * @param methodName
   *   The fully qualified name of the method being called, for use in the logger. For 
   *   example: <code>QueueMgr.getChooserUpdateTime()</code>.
   */ 
  public
  MiscGetLongRsp
  (
   TaskTimer timer, 
   Long value,
   String methodName
  )
  { 
    super(timer);

    if(value == null) 
      throw new IllegalArgumentException("The Long value cannot be (null)!");
    pValue = value;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       methodName + "():\n  " + getTimer());
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the Long value.
   */
  public Long
  getLongValue()
  {
    return pValue;
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8694733208252637141L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private Long pValue;
}
