// $Id: FileStateReq.java,v 1.1 2004/03/09 09:45:31 jim Exp $

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
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param working [<B>in</B>]
   *   The revision number of the checked-in version upon which the working version 
   *   is based.
   * 
   * @param latest [<B>in</B>]
   *   The revision number of the latest checked-in version.
   * 
   * @param fseqs [<B>in</B>]
   *   The primary and secondary file sequences associated with the working version.
   */
  public
  FileStateReq
  (
   NodeID id, 
   VersionID working, 
   VersionID latest, 
   TreeSet<FileSeq> fseqs
  )
  { 
    if(id == null) 
      throw new IllegalArgumentException("The working version ID cannot be (null)!");
    pNodeID = id;

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
  
