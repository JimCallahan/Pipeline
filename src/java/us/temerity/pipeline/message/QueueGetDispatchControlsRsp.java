// $Id: QueueGetDispatchControlsRsp.java,v 1.1 2009/09/16 03:54:40 jesse Exp $

package us.temerity.pipeline.message;

import java.util.TreeMap;

import us.temerity.pipeline.*;

/*---------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   D I S P A T C H   C O N T R O L S   R S P                             */
/*---------------------------------------------------------------------------------------------*/

/**
 * Get all the current dispatch controls.  
 */
public
class QueueGetDispatchControlsRsp
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
   * @param controls
   *   The dispatch controls indexed by group name. 
   */ 
  public
  QueueGetDispatchControlsRsp
  (
   TaskTimer timer, 
   TreeMap<String, DispatchControl> controls
  )
  { 
    super(timer);

    if(controls == null) 
      throw new IllegalArgumentException("The dispatch controls cannot be (null)!");
    pDispatchControls = controls;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "QueueMgr.getDispatchControls():\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the current dispatch controls indexed by control name. 
   */
  public TreeMap<String, DispatchControl>
  getDispatchControls() 
  {
    return pDispatchControls;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -2812689159737885851L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The current dispatch controls indexed by control name. 
   */ 
  private TreeMap<String, DispatchControl>  pDispatchControls;
}