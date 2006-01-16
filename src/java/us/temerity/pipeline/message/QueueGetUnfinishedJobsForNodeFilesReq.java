// $Id: QueueGetUnfinishedJobsForNodeFilesReq.java,v 1.1 2006/01/16 04:11:12 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   U N F I N I S H E D   J O B S   F O R   N O D E   F I L E S        */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the job IDs of unfinished jobs which will regenerate the given 
 * files of a working node.
 */
public
class QueueGetUnfinishedJobsForNodeFilesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param nodeID
   *   The unique working version identifier.
   * 
   * @param files
   *   The specific files to check.
   */
  public
  QueueGetUnfinishedJobsForNodeFilesReq
  (
   NodeID nodeID, 
   ArrayList<File> files 
  )
  {
    if(nodeID == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = nodeID;
 
    if(files == null) 
      throw new IllegalArgumentException("The file sequences cannot be (null)!");
    pFiles = files;
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
   * Get the specific files to check.
   */
  public ArrayList<File>
  getFiles()
  {
    return pFiles;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8554357546100748866L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /** 
   * The specific files to check.
   */
  private ArrayList<File>  pFiles;

}
  
