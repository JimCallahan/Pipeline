// $Id: FilePackNodesRsp.java,v 1.1 2007/10/23 02:29:58 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   P A C K   N O D E S   R S P                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FilePackNodesReq} request.
 */
public
class FilePackNodesRsp
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
   * @param nodeID 
   *   The unique working version identifier of the root node.
   * 
   * @param nodeArchive
   *   The abstract file system path to the node bundle.
   */
  public
  FilePackNodesRsp
  (
   TaskTimer timer, 
   NodeID nodeID,
   Path nodeArchive
  )
  { 
    super(timer);

    if(nodeID == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = nodeID;

    if(nodeArchive == null) 
      throw new IllegalArgumentException
        ("The path to the node bundle cannot be (null)!");
    pPath = nodeArchive;

    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "MasterMgr.packNodes(): " + nodeID + " (" + nodeArchive + "):" + 
       "\n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique working version identifier.
   */
  public NodeID
  getNodeID() 
  {
    return pNodeID;
  }
  
  /**
   * Gets the abstract file system path to the node bundle.
   */
  public Path
  getPath() 
  {
    return pPath;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8899534718809339413L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The abstract file system path to the node bundle.
   */
  private Path  pPath; 

}
  
