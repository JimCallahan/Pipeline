// $Id: FileStateRsp.java,v 1.3 2004/03/12 23:08:23 jim Exp $

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
   * 
   * @param wait [<B>in</B>]
   *   The number of milliseconds spent waiting to aquire the needed locks.
   * 
   * @param start [<B>in</B>]
   *   The timestamp of when the request started to be fufilled.
   */
  public
  FileStateRsp
  (
   NodeID id, 
   TreeMap<FileSeq, FileState[]> states, 
   long wait, 
   Date start
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(states == null) 
      throw new IllegalArgumentException("The working file states cannot (null)!");
    pStates = states;

    pWait   = wait;
    pActive = (new Date()).getTime() - start.getTime();

    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.computeFileStates(): " + id + " ");
      for(FileSeq fseq : states.keySet()) 
	buf.append("[" + fseq + "]");
      Logs.net.finest("FileMgr.computeFileStates(): " + buf.toString() + ": " +
		      pWait + "/" + pActive + " (msec) wait/active");
    }
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

  
  /**
   * Gets the number of milliseconds spent waiting to aquire the needed locks.
   */
  public long 
  getWaitTime() 
  {
    return pWait;
  }

  /**
   * Gets the number of milliseconds spent fufilling the request.
   */
  public long
  getActiveTime() 
  {
    return pActive;
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


  /*
   * The number of milliseconds spent waiting to aquire the needed locks.
   */ 
  private long  pWait;

  /**
   * The number of milliseconds spent fufilling the request.
   */ 
  private long  pActive; 

}
  
