// $Id: NodeGetCheckedInRsp.java,v 1.1 2004/06/14 22:32:16 jim Exp $

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

    Logs.net.finest("MasterMgr.getCheckedInVersion(): " + 
		    vsn.getName() + "(v" + vsn.getVersionID() + "):\n" + 
		    "  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
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
  
