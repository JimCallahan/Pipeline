// $Id: NodeDetails.java,v 1.3 2004/04/15 00:19:45 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.util.logging.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A detailed compilation of information related to state of node with respect to a 
 * particular user and view.
 * 
 * @see NodeSummary
 */
public
class NodeDetails
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * The <CODE>mod</CODE> argument may be <CODE>null</CODE> if the node has not been 
   * checked-out. <P> 
   * 
   * The <CODE>base</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version or if the node has not been checked-out. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param mod
   *   The working version of the node.
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based.
   * 
   * @param latest    
   *   The latest checked-in version of the node.
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
   * @param queueStates
   *   The queue states associated with each file sequence. 
   */
  protected 
  NodeDetails
  (
   String name, 
   NodeMod mod, 
   NodeVersion base, 
   NodeVersion latest, 
   OverallNodeState overallNodeState, 
   OverallQueueState overallQueueState, 
   VersionState versionState, 
   PropertyState propertyState, 
   LinkState linkState, 
   TreeMap<FileSeq,FileState[]> fileStates, 
   TreeMap<FileSeq,QueueState[]> queueStates
  ) 
  {
    super(name);

    pTimeStamp = new Date();

    pOverallNodeState  = overallNodeState;
    pOverallQueueState = overallQueueState;
    pVersionState      = versionState;
    pPropertyState     = propertyState;
    pLinkState         = linkState;

    pFileStates = new TreeMap<FileSeq,FileState[]>(); 
    for(FileSeq fseq : fileStates.keySet()) 
      pFileStates.put(fseq, fileStates.get(fseq).clone());
    
    pQueueStates = new TreeMap<FileSeq,QueueState[]>(); 
    for(FileSeq fseq : queueStates.keySet()) 
      pQueueStates.put(fseq, queueStates.get(fseq).clone());
  }

  /**
   * Construct an undefined state.
   * 
   * @param name 
   *   The fully resolved node name.
   */
  protected 
  NodeDetails
  (
   String name
  ) 
  {
    super(name);

    pTimeStamp = new Date();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get when the node state was determined.
   */ 
  public Date
  getTimeStamp() 
  {
    assert(pTimeStamp != null);
    return (Date) pTimeStamp.clone();
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the working version of the node.
   * 
   * @return
   *   The working version or <CODE>null</CODE> if none exists.
   */ 
  public NodeMod
  getWorkingVersion()
  {
    return pWorkingVersion;
  }

  /**
   * Get the checked-in version of the node upon which the working version was based.
   * 
   * @return
   *   The base version or <CODE>null</CODE> if none exists.
   */ 
  public NodeVersion
  getBaseVersion()
  {
    return pBaseVersion;
  }

  /**
   * Get the latest checked-in version of the node.
   * 
   * @return
   *   The latest version or <CODE>null</CODE> if none exists.
   */ 
  public NodeVersion
  getLatestVersion()
  {
    return pLatestVersion;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the overall revision control state of the node.
   * 
   * @return
   *   The node state or <CODE>null</CODE> if the node state is undefined.
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
   *   The queue state or <CODE>null</CODE> if the queue state is undefined.
   */
  public OverallQueueState
  getOverallQueueState() 
  {
    return pOverallQueueState;
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the version state of the node.
   */ 
  public VersionState
  getVersionState() 
  {
    return pVersionState;
  }

  /**
   * Get the state of the node properties.
   */ 
  public PropertyState
  getPropertyState() 
  {
    return pPropertyState;
  }

  /**
   * Get the state of the upstream node links.
   */ 
  public LinkState
  getLinkState() 
  {
    return pLinkState;
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the set of file sequences for which queue state information is defined.
   */ 
  public Set<FileSeq>
  getQueueStateSequences() 
  {
    return Collections.unmodifiableSet(pQueueStates.keySet());
  }
  
  /**
   * Get the queue states associated with the given file sequence. 
   *
   * @param fseq
   *   The file sequences to lookup.
   */ 
  public QueueState[]
  getQueueState
  (
   FileSeq fseq
  ) 
  {
    return pQueueStates.get(fseq);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8694000140938559535L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * When the message node state was determined.
   */ 
  private Date  pTimeStamp; 


  /**
   * The working version of the node.
   */ 
  private NodeMod  pWorkingVersion;

  /**
   * The checked-in version of the node upon which the working version was based.
   */
  private NodeVersion  pBaseVersion;
  
  /**
   * The latest checked-in version of the node.
   */
  private NodeVersion  pLatestVersion;


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
   * The relationship between the revision numbers of working and checked-in versions of 
   * a node. 
   */ 
  private VersionState  pVersionState;

  /** 
   * The relationship between the values of the node properties associated with the working 
   * and checked-in versions of a node. 
   */   
  private PropertyState  pPropertyState;

  /** 
   * A comparison of the upstream node link information associated with a working version 
   * and the latest checked-in version of a node. <P> 
   */   
  private LinkState  pLinkState;

  /** 
   * The relationship between the individual files associated with the working and checked-in 
   * versions of a node.
   */   
  private TreeMap<FileSeq,FileState[]> pFileStates;

  /** 
   * The status of individual files associated with a node with respect to the queue jobs
   * which generate them. 
   */   
  private TreeMap<FileSeq,QueueState[]>  pQueueStates;
  
  
}

