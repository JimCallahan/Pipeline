// $Id: FileStateReq.java,v 1.10 2009/09/01 10:59:39 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   S T A T E   R E Q                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to compute the {@link FileState FileState} for each file associated with the 
 * working version of a node.
 */
public
class FileStateReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * The <CODE>working</CODE> and <CODE>latest</CODE> arguments may be <CODE>null</CODE> 
   * if this is an initial working version. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vstate 
   *   The relationship between the revision numbers of working and checked-in versions 
   *   of the node.
   * 
   * @param jobStates 
   *   The jobs states for each primary/secondary file (if any).
   * 
   * @param isFrozen
   *   Whether the files associated with the working version are symlinks to the 
   *   checked-in files instead of copies.
   * 
   * @param working 
   *   The revision number of the checked-in version upon which the working version 
   *   is based.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @param ctime
   *   The last legitimate change time (ctime) of the file.
   * 
   * @param fseqs 
   *   The primary and secondary file sequences associated with the working version.
   * 
   * @param isBaseIntermediate
   *   Is the base version an intermediate node with no repository files.
   * 
   * @param baseCheckSums
   *   Read-only checksums for all files associated with the base checked-in version
   *   or <CODE>null</CODE> if no base version exists.
   * 
   * @param isLatestIntermediate
   *   Is the latest version an intermediate node with no repository files.
   * 
   * @param latestCheckSums
   *   Read-only checksums for all files associated with the latest checked-in version
   *   or <CODE>null</CODE> if no base version exists.
   * 
   * @param workingCheckSums
   *   Current cache of checksums for files associated with the working version.
   */
  public
  FileStateReq
  (
   NodeID id, 
   VersionState vstate, 
   JobState jobStates[], 
   boolean isFrozen, 
   VersionID working, 
   VersionID latest, 
   long ctime, 
   TreeSet<FileSeq> fseqs,
   boolean isBaseIntermediate, 
   SortedMap<String,CheckSum> baseCheckSums, 
   boolean isLatestIntermediate, 
   SortedMap<String,CheckSum> latestCheckSums, 
   CheckSumCache workingCheckSums
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(vstate == null) 
      throw new IllegalArgumentException("The version state cannot be (null)!");
    pVersionState = vstate;

    pIsFrozen = isFrozen;

    switch(vstate) {
    case Pending:
      if(working != null) 
	throw new IllegalArgumentException
	  ("The working revision number must be (null) if the " +
	   "VersionState is (Pending)!");
      if(latest != null) 
	throw new IllegalArgumentException
	  ("The latest checked-in revision number must be (null) if the " + 
	   "VersionState is (Pending)!");
      break;

    case CheckedIn:
      throw new IllegalArgumentException
	("No FileStates should ever need to be computed when the " +
	 "VersionState is (CheckedIn)!");

    case Identical:
    case NeedsCheckOut:
      if(working == null) 
	throw new IllegalArgumentException
	  ("The working revision number cannot be (null) if the " +
	   "VersionState is (" + vstate.name() + ")!");
      if(latest == null) 
	throw new IllegalArgumentException
	  ("The latest checked-in revision number cannot  be (null) if the " + 
	   "VersionState is (" + vstate.name() + ")!");
    }

    if(jobStates == null) 
      throw new IllegalArgumentException("The job states cannot be (null)!");
    pJobStates = jobStates;

    pWorkingVersionID = working;
    pLatestVersionID  = latest;

    pChangeStamp = ctime;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;

    pIsBaseIntermediate   = isBaseIntermediate; 
    pBaseCheckSums        = baseCheckSums;
    pIsLatestIntermediate = isBaseIntermediate; 
    pLatestCheckSums      = latestCheckSums;

    if(workingCheckSums == null) 
      throw new IllegalArgumentException("The working checksum cache cannot be (null)!");
    pWorkingCheckSums = workingCheckSums; 
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
   * Gets relationship between the revision numbers of working and checked-in versions 
   * of the node.
   */
  public VersionState
  getVersionState() 
  {
    return pVersionState;
  }
  
  /**
   * Gets the jobs states for each primary/secondary file (if any).
   */
  public JobState[]
  getJobStates() 
  {
    return pJobStates;
  }
  
  /**
   * Get whether the files associated with the working version are symlinks to the 
   * checked-in files instead of copies.
   */ 
  public boolean 
  isFrozen() 
  {
    return pIsFrozen; 
  }

  /**
   * Gets the revision number of the checked-in version upon which the working version 
   * is based.
   */
  public VersionID
  getWorkingVersionID() 
  {
    return pWorkingVersionID;
  }
  
  /**
   * Gets the revision number of the latest checked-in version.
   */
  public VersionID
  getLatestVersionID() 
  {
    return pLatestVersionID;
  }
  
  /**
   * Gets the last legitimate change time (ctime) of the file.
   */
  public long
  getChangeStamp() 
  {
    return pChangeStamp;
  }
  
  /**
   * Gets the primary and secondary file sequences associated with the working version.
   */
  public TreeSet<FileSeq>
  getFileSequences() 
  {
    return pFileSeqs;
  }

  /**
   * Is the base version an intermediate node with no repository files.
   */
  public boolean
  isBaseIntermediate()
  {
    return pIsBaseIntermediate; 
  }

  /**
   * Read-only checksums for all files associated with the base checked-in version
   * or <CODE>null</CODE> if no base version exists.
   */
  public SortedMap<String,CheckSum> 
  getBaseCheckSums()
  {
    return pBaseCheckSums; 
  }

  /**
   * Is the latest version an intermediate node with no repository files.
   */
  public boolean
  isLatestIntermediate()
  {
    return pIsLatestIntermediate; 
  }

  /**
   * Read-only checksums for all files associated with the latest checked-in version
   * or <CODE>null</CODE> if no latest version exists.
   */ 
  public SortedMap<String,CheckSum> 
  getLatestCheckSums()
  {
    return pLatestCheckSums; 
  }

  /**
   * Current cache of checksums for files associated with the working version.
   */ 
  public CheckSumCache
  getWorkingCheckSums()
  {
    return pWorkingCheckSums; 
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3198145834839538096L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique working version identifier.
   */ 
  private NodeID  pNodeID;

  /**
   * The relationship between the revision numbers of working and checked-in versions 
   * of the node.
   */
  private VersionState  pVersionState;

  /**
   * The jobs states for each primary/secondary file (if any).
   */
  private JobState  pJobStates[];

  /**
   * Whether the files associated with the working version are symlinks to the 
   * checked-in files instead of copies.
   */ 
  private boolean pIsFrozen; 

  /**
   * The revision number of the checked-in version upon which the working version  is based.  
   * If <CODE>null</CODE>, then this is an intial working version of a node which has never 
   * been checked-in.
   */
  private VersionID  pWorkingVersionID;

  /**
   * The revision number of the latest checked-in version.  If <CODE>null</CODE>, then this 
   * is an intial working version of a node which has never been checked-in.
   */
  private VersionID  pLatestVersionID;

  /**
   * The last legitimate change time (ctime) of the file.
   */ 
  private long  pChangeStamp; 

  /** 
   * The primary and secondary file sequences associated with the working version. 
   */
  private TreeSet<FileSeq>  pFileSeqs;

  /**
   * Is the base version an intermediate node with no repository files.
   */ 
  private boolean pIsBaseIntermediate; 

  /**
   * Read-only checksums for all files associated with the base checked-in version
   * or <CODE>null</CODE> if no base version exists.
   */
  private SortedMap<String,CheckSum>  pBaseCheckSums; 

  /**
   * Is the latest version an intermediate node with no repository files.
   */ 
  private boolean pIsLatestIntermediate; 

  /**
   * Read-only checksums for all files associated with the latest checked-in version
   * or <CODE>null</CODE> if no latest version exists.
   */ 
  private SortedMap<String,CheckSum>  pLatestCheckSums; 

  /**
   * Current cache of checksums for files associated with the working version.
   */ 
  private CheckSumCache pWorkingCheckSums; 

}
  
