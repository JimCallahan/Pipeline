// $Id: FileStateRsp.java,v 1.2 2004/03/10 11:49:00 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileStateReq FileStateReq} request.
 */
public
class FileStateRsp
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param states [<B>in</B>]
   *   The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   *   the working version indexed by file sequence.
   */
  public
  FileStateRsp
  (
   NodeID id, 
   TreeMap<FileSeq, FileState[]> states
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(states == null) 
      throw new IllegalArgumentException("The working file states cannot (null)!");
    pStates = states;
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
   * Gets the <CODE>FileState</CODE> of each the primary and secondary file associated with 
   * the working version indexed by file sequence.
   */
  public TreeMap<FileSeq, FileState[]>
  getFileStates() 
  {
    return pStates;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5719464566999952190L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /** 
   * The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   * the working version indexed by file sequence.
   */
  private TreeMap<FileSeq, FileState[]>  pStates; 
}
  
