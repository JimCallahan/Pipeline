// $Id: FileStateRsp.java,v 1.10 2004/06/08 20:05:10 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E   R S P                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileStateReq FileStateReq} request.
 */
public
class FileStateRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * The <CODE>timestamps</CODE> argument may contain <CODE>null</CODE> entries for those 
   * working files which do not exist.
   * 
   *   file exists.
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param states 
   *   The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   *   the working version indexed by file sequence.
   * 
   * @param timestamps
   *   The last modification timestamp of each the primary and secondary file associated with 
   *   the working version indexed by file sequence. 
   */
  public
  FileStateRsp
  (
   TaskTimer timer, 
   NodeID id, 
   TreeMap<FileSeq, FileState[]> states,
   TreeMap<FileSeq, Date[]> timestamps
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(states == null) 
      throw new IllegalArgumentException("The working file states cannot (null)!");
    pStates = states;

    if(timestamps == null) 
      throw new IllegalArgumentException("The working file timestamps cannot (null)!");
    pTimeStamps = timestamps;

    if(Logs.net.isLoggable(Level.FINEST)) {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.computeFileStates(): " + id + " ");
      for(FileSeq fseq : states.keySet()) 
	buf.append("[" + fseq + "]");
      Logs.net.finest(buf.toString() + ":\n  " + getTimer());
      Logs.flush();
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
   * Gets the last modification timestamp of each the primary and secondary file associated 
   * with the working version indexed by file sequence. <P> 
   * 
   * Individual timestamps may be <CODE>null</CODE> if no corresponding working file exists.
   */
  public TreeMap<FileSeq, Date[]>
  getTimeStamps() 
  {
    return pTimeStamps;
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

  /** 
   * The last modification timestamp of each the primary and secondary file associated 
   * with the working version indexed by file sequence. <P> 
   * 
   * Individual timestamps may be <CODE>null</CODE> if no corresponding working file exists.
   */
  private TreeMap<FileSeq, Date[]>  pTimeStamps; 

}
  
