// $Id: NodeGetCheckedInRsp.java,v 1.3 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   C H E C K E D - I N   R S P                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetCheckedInReq NodeGetCheckedInReq} request.
 */
public
class NodeGetCheckedInRsp
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
   * @param vsn
   *   The checked-in version.
   */
  public
  NodeGetCheckedInRsp
  (
   TaskTimer timer, 
   NodeVersion vsn
  )
  { 
    super(timer);

    if(vsn == null) 
      throw new IllegalArgumentException("The checked-in version cannot be (null)!");
    pNodeVersion = vsn;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getCheckedInVersion(): " + 
       vsn.getName() + "(v" + vsn.getVersionID() + "):\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the checked-in version of the node.
   */
  public NodeVersion
  getNodeVersion() 
  {
    return pNodeVersion;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1567748353077243967L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in version of the node.
   */
  private NodeVersion  pNodeVersion;

}
  
