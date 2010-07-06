// $Id: NodeGetCheckedInRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

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
 * A successful response to a {@link NodeGetMultiCheckedInReq} request.
 */
public
class NodeGetMultiCheckedInRsp
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
   * @param versions
   *   The checked-in versions indexed by node name and revision number.
   */
  public
  NodeGetMultiCheckedInRsp
  (
   TaskTimer timer, 
   DoubleMap<String,VersionID,NodeVersion> versions
  )
  { 
    super(timer);

    if(versions == null) 
      throw new IllegalArgumentException("The checked-in versions cannot be (null)!");
    pNodeVersions = versions;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getMultiCheckedInVersion(): [multiple]\n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the checked-in versions indexed by node name and revision number.
   */
  public DoubleMap<String,VersionID,NodeVersion> 
  getNodeVersions() 
  {
    return pNodeVersions;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8570299297662142125L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in versions indexed by node name and revision number.
   */
  private DoubleMap<String,VersionID,NodeVersion>  pNodeVersions;

}
  
