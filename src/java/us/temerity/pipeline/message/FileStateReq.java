// $Id: FileStateReq.java,v 1.4 2004/03/23 07:40:37 jim Exp $

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
   * @param working 
   *   The revision number of the checked-in version upon which the working version 
   *   is based.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @param fseqs 
   *   The primary and secondary file sequences associated with the working version.
   */
  public
  FileStateReq
  (
   NodeID id, 
   VersionState vstate, 
   VersionID working, 
   VersionID latest, 
   TreeSet<FileSeq> fseqs
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

    if(vstate == null) 
      throw new IllegalArgumentException("The version state cannot be (null)!");
    pVersionState = vstate;

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

    pWorkingVersionID = working;
    pLatestVersionID  = latest;

    if(fseqs == null) 
      throw new IllegalArgumentException("The working file sequences cannot (null)!");
    pFileSeqs = fseqs;
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
   * Gets the primary and secondary file sequences associated with the working version.
   */
  public TreeSet<FileSeq>
  getFileSequences() 
  {
    return pFileSeqs;
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
   * The primary and secondary file sequences associated with the working version. 
   */
  private TreeSet<FileSeq>  pFileSeqs;
}
  
