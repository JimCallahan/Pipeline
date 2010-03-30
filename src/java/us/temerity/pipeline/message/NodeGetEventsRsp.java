// $Id: NodeGetEventsRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

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
   MappedLinkedList<Long,BaseNodeEvent> events
  )
  { 
    super(timer);

    if(events == null) 
      throw new IllegalArgumentException("The events cannot be (null)!");
    pEvents = events;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getNodeEvents():\n" + 
       "  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The events indexed by the timestamp at which the events occurred.
   */
  public MappedLinkedList<Long,BaseNodeEvent>
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
  private MappedLinkedList<Long,BaseNodeEvent>  pEvents;

}
  
