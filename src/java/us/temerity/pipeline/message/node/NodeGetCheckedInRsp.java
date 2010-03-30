// $Id: NodeGetCheckedInRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message.node;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

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

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getCheckedInVersion(): " + 
       vsn.getName() + "(v" + vsn.getVersionID() + "):\n" + 
       "  " + getTimer());
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
  
