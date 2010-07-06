// $Id: NodeGetHistoryRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   M U L T I   C H E C K E D - I N   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetHistoryReq NodeGetHistoryReq} request.
 */
public
class NodeGetMultiHistoryRsp
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
   *   The log messages indexed by node name and revision number.
   */
  public
  NodeGetMultiHistoryRsp
  (
   TaskTimer timer, 
   DoubleMap<String,VersionID,LogMessage> history
  )
  { 
    super(timer);

    if(history == null) 
      throw new IllegalArgumentException("The history cannot be (null)!");
    pHistory = history;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getHistory(): [multiple]:\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the log messages indexed by node name and revision number.
   */
  public DoubleMap<String,VersionID,LogMessage>
  getHistory()
  {
    return pHistory;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5618972761677865448L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The log messages indexed by node name and revision number.
   */
  private DoubleMap<String,VersionID,LogMessage>  pHistory;

}
  
