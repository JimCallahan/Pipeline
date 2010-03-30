// $Id: FileStateRsp.java,v 1.17 2009/08/28 02:10:47 jim Exp $

package us.temerity.pipeline.message.file;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

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
   * @param fileInfos
   *   Per-file information for each primary and secondary file associated with the working 
   *   version indexed by file sequence or <CODE>null</CODE> if not checked-out.
   * 
   * @param updatedCheckSums
   *   The updated cache of checksums for files associated with the working version.
   */
  public
  FileStateRsp
  (
   TaskTimer timer, 
   NodeID id, 
   TreeMap<FileSeq,FileState[]> states,
   TreeMap<FileSeq,NativeFileInfo[]> fileInfos,
   CheckSumCache updatedCheckSums
  )
  { 
    super(timer);

    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(states == null) 
      throw new IllegalArgumentException("The working file states cannot (null)!");
    pStates = states;

    pFileInfos = fileInfos; 

    if(updatedCheckSums == null) 
      throw new IllegalArgumentException("The updated checksums cannot (null)!");
    pUpdatedCheckSums = updatedCheckSums; 

    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest)) {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.states(): " + id + " ");
      for(FileSeq fseq : states.keySet()) 
	buf.append("[" + fseq + "]");
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Net, LogMgr.Level.Finest,
	 buf.toString() + ":\n  " + getTimer());
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
   * Gets the er-file information for each primary and secondary file associated with the 
   * working version indexed by file sequence or <CODE>null</CODE> if not checked-out.
   * 
   * Individual timestamps may be <CODE>null</CODE> if no corresponding working file exists.
   */
  public TreeMap<FileSeq,NativeFileInfo[]>
  getFileInfos()
  {
    return pFileInfos; 
  }

  /**
   * The updated cache of checksums for files associated with the working version.
   */ 
  public CheckSumCache
  getUpdatedCheckSums()
  {
    return pUpdatedCheckSums; 
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
  private TreeMap<FileSeq,FileState[]>  pStates; 

  /**
   * The per-file information for each primary and secondary file associated with the working 
   * version indexed by file sequence or <CODE>null</CODE> if not checked-out.
   * 
   * Individual timestamps may be <CODE>null</CODE> if no corresponding working file exists.
   */ 
  private TreeMap<FileSeq,NativeFileInfo[]> pFileInfos; 

  /**
   * The updated cache of checksums for files associated with the working version.
   */ 
  private CheckSumCache  pUpdatedCheckSums; 
  
}
  
