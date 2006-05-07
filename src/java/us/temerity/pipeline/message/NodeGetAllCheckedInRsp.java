// $Id: NodeGetAllCheckedInRsp.java,v 1.1 2006/05/07 21:33:59 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   G E T   A L L   C H E C K E D - I N   R S P                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link NodeGetAllCheckedInReq NodeGetAllCheckedInReq} request.
 */
public
class NodeGetAllCheckedInRsp
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
   *   The checked-in versions indexed by revision number.
   */
  public
  NodeGetAllCheckedInRsp
  (
   TaskTimer timer, 
   TreeMap<VersionID,NodeVersion> versions
  )
  { 
    super(timer);

    if(versions == null) 
      throw new IllegalArgumentException("The checked-in versions cannot be (null)!");
    pNodeVersions = versions;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.getAllCheckedInVersions():\n" + 
       "  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Gets the checked-in versions indexed by revision number.
   */
  public TreeMap<VersionID,NodeVersion>
  getNodeVersions() 
  {
    return pNodeVersions;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8299585793205981082L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checked-in versions indexed by revision number.
   */
  private TreeMap<VersionID,NodeVersion> pNodeVersions;

}
  
