// $Id: NodeGetHistoryRsp.java,v 1.1 2004/07/07 13:30:26 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetHistoryReq NodeGetHistoryReq} request.
 */
public
class NodeGetHistoryRsp
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
   * @param history
   *   The log messages indexed by revision number.
   */
  public
  NodeGetHistoryRsp
  (
   TaskTimer timer, 
   String name, 
   TreeMap<VersionID,LogMessage> history
  )
  { 
    super(timer);

    if(history == null) 
      throw new IllegalArgumentException("The history cannot be (null)!");
    pHistory = history;

    Logs.net.finest("MasterMgr.getHistory(): " + name + ":\n" + 
		    "  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the log messages indexed by revision number.
   */
  public TreeMap<VersionID,LogMessage>
  getHistory()
  {
    return pHistory;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7980094870177684837L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The log messages indexed by revision number.
   */
  private TreeMap<VersionID,LogMessage>  pHistory;

}
  
