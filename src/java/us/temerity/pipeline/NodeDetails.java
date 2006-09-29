// $Id: NodeDetails.java,v 1.16 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   D E T A I L S                                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A detailed compilation of information related to state of node with respect to a 
 * particular user and view.
 * 
 * @see NodeStatus
 */
public
class NodeDetails
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct with the given state information. <P> 
   * 
   * The <CODE>work</CODE> argument may be <CODE>null</CODE> if the node has not been 
   * checked-out. <P> 
   * 
   * The <CODE>base</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version or if the node has not been checked-out. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial working
   * version. <P> 
   * 
   * The <CODE>jobIDs</CODE> and <CODE>queueStates</CODE> arguments may contain 
   * <CODE>null</CODE> members if no queue job exists which generates that particular 
   * file. <P> 
   * 
   * The <CODE>fileTimeStamps</CODE> argument contains the timestamp which is most relevant
   * (newest) for determining when each file index was last modified.  This timestamp may be
   * the last modification date for the primary/secondary file sequence, the timestamp of 
   * when the last critical modification of node properties occurred or when the node state
   * was computed in the case of missing files. <P> 
   * 
   * If the <CODE>ignoreTimeStamps</CODE> argument is <CODE>true</CODE> for a file index, 
   * then the timestamp stored in <CODE>fileTimeStamps</CODE> should usually be ignored when 
   * computing whether downstream nodes are {@link OverallQueueState#Stale Stale} due to this
   * node being newer.  However, if the working revision number of this node does not match
   * the revision number of the link from the downstream node who's 
   * {@link OverallQueueState OverallQueueState} is being computed, the timestamp should 
   * be considered regardless of the value of <CODE>ignoreTimeStamps</CODE>. <P> 
   * 
   * @param name 
   *   The fully resolved node name.
   * 
   * @param work
   *   The working version of the node.
   * 
   * @param base
   *   The checked-in version of the node upon which the working version was based.
   * 
   * @param latest    
   *   The latest checked-in version of the node.
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
   * @param ignoreTimeStamps
   *   Whether the timestamp stored in <CODE>fileTimeStamps</CODE> should be ignored
   *   when computing whether each file index is {@link QueueState#Stale Stale}.
   * 
   * @param jobIDs
   *   The unique job identifiers associated with all file sequences. 
   * 
   * @param queueStates
   *   The queue states associated with all file sequences. 
   */
  public 
  NodeDetails
  (
   String name, 
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
   Date[] fileTimeStamps, 
   boolean[] ignoreTimeStamps, 
   Long[] jobIDs, 
   QueueState[] queueStates
  ) 
  {
    if(name == null) 
      throw new IllegalArgumentException("The node name cannot be (null)!");
    pName = name;

    pTimeStamp = Dates.now();

    if((work != null) && !work.getName().equals(pName))
      throw new IllegalArgumentException
	("The working version name (" + work.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pWorkingVersion = work;

    if((base != null) && !base.getName().equals(pName))
      throw new IllegalArgumentException
	("The base checked-in version name (" + base.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pBaseVersion = base;

    if((latest != null) && !latest.getName().equals(pName))
      throw new IllegalArgumentException
	("The latest checked-in version name (" + latest.getName() + ") didn't match the " + 
	 "details name (" + pName + ")!");
    pLatestVersion = latest;

    pVersionIDs = new ArrayList<VersionID>(versionIDs);

    pOverallNodeState  = overallNodeState;
    pOverallQueueState = overallQueueState;
    pVersionState      = versionState;
    pPropertyState     = propertyState;
    pLinkState         = linkState;

    pFileStates = new TreeMap<FileSeq,FileState[]>(); 
    for(FileSeq fseq : fileStates.keySet())
      pFileStates.put(fseq, fileStates.get(fseq).clone());

    pFileTimeStamps   = fileTimeStamps.clone();
    pIgnoreTimeStamps = ignoreTimeStamps.clone();

    pJobIDs      = jobIDs.clone();
    pQueueStates = queueStates.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved node name.
   */ 
  public String
  getName() 
  {
    if(pName == null)
      throw new IllegalStateException(); 
    return pName;
  }

  /**
   * Get when the node state was determined.
   */ 
  public Date
  getTimeStamp() 
  {
    if(pTimeStamp == null)
      throw new IllegalStateException(); 
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


  /**
   * Get the revision numbers of all checked-in versions.
   */ 
  public ArrayList<VersionID> 
  getVersionIDs() 
  {
    return new ArrayList<VersionID>(pVersionIDs);
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

  /**
   * Get the newest timestamp which needs to be considered when computing wheter each file 
   * index is {@link QueueState#Stale Stale}. 
   */ 
  public Date[] 
  getFileTimeStamps() 
  {
    return pFileTimeStamps;
  }

  /**
   * Whether the timestamps returned by {@link #getFileTimeStamps getFileTimeStamps} should 
   * be ignored when computing whether each file index is {@link QueueState#Stale Stale}.
   */ 
  public boolean[] 
  ignoreTimeStamps() 
  {
    return pIgnoreTimeStamps;
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



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The string representation of the primary file sequence.
   */ 
  public String
  toString()
  {
    if(pWorkingVersion != null)
      return pWorkingVersion.toString();
    return pLatestVersion.toString();
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
    encoder.encode("Name", pName); 
    encoder.encode("TimeStamp", pTimeStamp.getTime());
    
    if(pWorkingVersion != null) 
      encoder.encode("WorkingVersion", pWorkingVersion);

    if(pBaseVersion != null) 
      encoder.encode("BaseVersion", pBaseVersion);
    
    if(pLatestVersion != null) 
      encoder.encode("LatestVersion", pLatestVersion);

    if(!pVersionIDs.isEmpty()) 
      encoder.encode("VersionIDs", pVersionIDs);

    encoder.encode("OverallNodeState", pOverallNodeState);
    encoder.encode("OverallQueueState", pOverallQueueState);
    encoder.encode("VersionState", pVersionState);
    encoder.encode("PropertyState", pPropertyState);
    encoder.encode("LinkState", pLinkState);
    encoder.encode("FileStates", pFileStates);
    
    {
      Long ts[] = new Long[pFileTimeStamps.length];
      int wk;
      for(wk=0; wk<pFileTimeStamps.length; wk++) 
	ts[wk] = pFileTimeStamps[wk].getTime();
      encoder.encode("FileTimeStamps", ts);
    }

    encoder.encode("IgnoreTimeStamps", pIgnoreTimeStamps);
    encoder.encode("JobIDs", pJobIDs);
    encoder.encode("QueueStates", pQueueStates);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    throw new GlueException("NodeDetails does not support GLUE decoding!");
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6369659954228775104L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved node name.
   */
  private String  pName;      


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
   * The revision numbers of all checked-in versions.
   */ 
  private ArrayList<VersionID>  pVersionIDs;


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
   * The newest timestamp which needs to be considered when computing wheter each file 
   * index is {@link QueueState#Stale Stale}. <P> 
   */
  private Date[] pFileTimeStamps;

  /**
   * Whether the timestamps returned by {@link #getFileTimeStamps getFileTimeStamps} should 
   * be ignored when computing whether each file index is {@link QueueState#Stale Stale}.
   */
  private boolean[] pIgnoreTimeStamps;

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
  
}

