// $Id: NodeDetailsHeavy.java,v 1.1 2008/07/21 17:31:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S   H E A V Y                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A heavyweight collection of node state information respect to a particular working 
 * area view.<P> 
 * 
 * When a heavyweight node status operation is performed, the information contained in 
 * this class is provided by plmaster(1).
 */
public
class NodeDetailsHeavy
  extends NodeDetailsLight
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * The <CODE>jobIDs</CODE> and <CODE>queueStates</CODE> arguments may contain 
   * <CODE>null</CODE> members if no queue job exists which generates that particular 
   * file. <P> 
   * 
   * The <CODE>fileTimeStamps</CODE> argument contains the timestamp which is most relevant
   * (newest) for determining when each file index was last modified.  This timestamp may be
   * the last modification date for the primary/secondary file sequence, the timestamp of 
   * when the last critical modification of node properties or links occurred.  For missing
   * files, this timestamp will be when the node state was last computed.<P> 
   * 
   * @param work
   *   The working version of the node or <CODE>null</CODE> if the node has not been 
   *   checked-out. 
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based or 
   *   <CODE>null</CODE> if this is an initial working version or if the node has not 
   *   been checked-out. 
   * 
   * @param latest  
   *   The latest checked-in version of the node or <CODE>null</CODE> if this is an 
   *   initial working version which has never been checked-in. 
   *
   * @param versionIDs
   *   The revision numbers of all checked-in versions.
   * 
   * @param overallNodeState 
   *   The overall revision control state of the node.
   * 
   * @param overallQueueState 
   *   The overall state of queue jobs associated with the node.
   * 
   * @param versionState
   *   The version state of the node.
   * 
   * @param propertyState  
   *   The state of the node properties.
   * 
   * @param linkState 
   *   The state of the upstream node links.
   * 
   * @param fileStates
   *   The files states associated with each file sequence. 
   * 
   * @param fileTimeStamps
   *   The newest timestamp which needs to be considered when computing whether each file 
   *   index is {@link QueueState#Stale Stale}.
   * 
   * @param jobIDs
   *   The unique job identifiers associated with all file sequences. 
   * 
   * @param queueStates
   *   The queue states associated with all file sequences. 
   * 
   * @param updateStates
   *   The update states associated with all file sequences. 
   */
  public 
  NodeDetailsHeavy
  (
   NodeMod work, 
   NodeVersion base, 
   NodeVersion latest, 
   Collection<VersionID> versionIDs, 
   OverallNodeState overallNodeState, 
   OverallQueueState overallQueueState, 
   VersionState versionState, 
   PropertyState propertyState, 
   LinkState linkState, 
   TreeMap<FileSeq,FileState[]> fileStates, 
   long[] fileTimeStamps, 
   Long[] jobIDs, 
   QueueState[] queueStates, 
   UpdateState[] updateStates
  ) 
  {
    super(work, base, latest, versionIDs, 
          versionState, propertyState, linkState); 

    if(overallNodeState == null) 
      throw new IllegalArgumentException("The overall node state cannot be (null)!");
    pOverallNodeState  = overallNodeState;

    if(overallQueueState == null) 
      throw new IllegalArgumentException("The overall queue state cannot be (null)!");
    pOverallQueueState = overallQueueState;

    if(fileStates == null) 
      throw new IllegalArgumentException("The file states cannot be (null)!");
    pFileStates = new TreeMap<FileSeq,FileState[]>(); 
    for(Map.Entry<FileSeq,FileState[]> entry : fileStates.entrySet()) 
      pFileStates.put(entry.getKey(), entry.getValue().clone());

    if(fileTimeStamps == null) 
      throw new IllegalArgumentException("The file time stamps cannot be (null)!");
    pFileTimeStamps = fileTimeStamps.clone();

    if(jobIDs == null) 
      throw new IllegalArgumentException("The job IDs cannot be (null)!");
    pJobIDs = jobIDs.clone();

    if(queueStates == null) 
      throw new IllegalArgumentException("The queue states cannot be (null)!");
    pQueueStates = queueStates.clone();

    if(updateStates == null) 
      throw new IllegalArgumentException("The update states cannot be (null)!");
    pUpdateStates = updateStates.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether all of the per-file states are Missing.
   */ 
  public boolean 
  isAllMissing()
  {
    for(FileState[] fs : pFileStates.values()) {
      for(FileState fstate : fs) {
        if(fstate != FileState.Missing) 
          return false; 
      }
    }

    return true;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the overall revision control state of the node.
   * 
   * @return
   *   The overall node state.
   */
  public OverallNodeState
  getOverallNodeState() 
  {
    return pOverallNodeState;
  }
  
  /**
   * Get the overall state of queue jobs associated with the node.
   * 
   * @return
   *   The overall queue state.
   */
  public OverallQueueState
  getOverallQueueState() 
  {
    return pOverallQueueState;
  }


  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of file sequences for which file state information is defined.
   */ 
  public Set<FileSeq>
  getFileStateSequences() 
  {
    return Collections.unmodifiableSet(pFileStates.keySet());
  }
  
  /**
   * Get the file states associated with the given file sequence. 
   *
   * @param fseq
   *   The file sequences to lookup.
   */ 
  public FileState[]
  getFileState
  (
   FileSeq fseq
  ) 
  {
    return pFileStates.get(fseq);
  }

  /**
   * Get the newest timestamp which needs to be considered when computing whether each file 
   * index is {@link QueueState#Stale Stale}. 
   */ 
  public long[] 
  getFileTimeStamps() 
  {
    return pFileTimeStamps;
  }

  /**
   * Get the unique job identifiers associated with the file sequences.
   */ 
  public Long[]
  getJobIDs() 
  {
    return pJobIDs; 
  }

  /**
   * Get the queue states associated with the file sequences.
   */ 
  public QueueState[]
  getQueueState() 
  {
    return pQueueStates;
  }

  /**
   * Get the update states associated with the file sequences.
   */ 
  public UpdateState[]
  getUpdateState() 
  {
    return pUpdateStates;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder); 

    encoder.encode("OverallNodeState", pOverallNodeState);
    encoder.encode("OverallQueueState", pOverallQueueState);

    encoder.encode("FileStates", pFileStates);
    encoder.encode("FileTimeStamps", pFileTimeStamps);
    encoder.encode("JobIDs", pJobIDs);
    encoder.encode("QueueStates", pQueueStates);
    encoder.encode("UpdateStates", pUpdateStates);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeDetailsHeavy does not support GLUE decoding!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6863666461143621850L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * A single state computed from the combination of {@link VersionState VersioState}, 
   * {@link PropertyState PropertyState}, {@link LinkState LinkState} and the individual
   * {@link FileState FileState} of each file associated with the node.
   */
  private OverallNodeState pOverallNodeState;
  
  /** 
   * A single state computed from the combination of the individual 
   * {@link QueueState QueueState} and {@link FileState FileState} of each file associated 
   * with the node. 
   */
  private OverallQueueState pOverallQueueState;

  /** 
   * The relationship between the individual files associated with the working and checked-in 
   * versions of a node.
   */   
  private TreeMap<FileSeq,FileState[]> pFileStates;
  
  /**
   * The newest timestamp which needs to be considered when computing whether each file 
   * index is up-to-date.  
   */
  private long[] pFileTimeStamps;

  /** 
   * The unique job identifiers of the job which generates individual files associated with 
   * a node. 
   */   
  private Long[]  pJobIDs; 

  /** 
   * The status of individual files associated with a node with respect to the queue jobs
   * which generate them. 
   */   
  private QueueState pQueueStates[];

  /** 
   * A cache of the reasons that individual files associated with a node might not be 
   * up-to-date based on the QueueStates of upstream file dependencies. 
   */   
  private UpdateState pUpdateStates[];
  
}

