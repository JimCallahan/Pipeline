// $Id: NodeGetEventsRsp.java,v 1.2 2007/03/28 19:56:42 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.event.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   E V E N T S   R S P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetEventsReq} request.
 */
public
class NodeGetEventsRsp
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
   * @param events
   *   The events indexed by the timestamp at which the events occurred.
   */
  public
  NodeGetEventsRsp
  (
   TaskTimer timer, 
   TreeMap<Long,BaseNodeEvent> events
  )
  { 
    super(timer);

    if(events == null) 
      throw new IllegalArgumentException("The events cannot be (null)!");
    pEvents = events;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getNodeEvents():\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The events indexed by the timestamp at which the events occurred.
   */
  public TreeMap<Long,BaseNodeEvent>
  getEvents()
  {
    return pEvents;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8757480448676394646L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The events indexed by the timestamp at which the events occurred.
   */
  private TreeMap<Long,BaseNodeEvent>  pEvents;

}
  
