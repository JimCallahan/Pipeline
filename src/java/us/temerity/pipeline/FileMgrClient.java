// $Id: FileMgrClient.java,v 1.1 2004/03/10 11:48:12 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   C L I E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with {@link FileMgr FileMgr} instances running 
 * on the file server.  The methods of this class correspond directly to the methods with 
 * the same name of the <CODE>FileMgr</CODE> class.  See that class for more details.
 * 
 * @see FileMgr
 */
public
class FileMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager client.
   */
  public
  FileMgrClient() 
  {


  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Refresh any missing or out-of-date checksums for the given files sequences associated
   * with the given working version of a node.
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param fseqs [<B>in</B>]
   *   The primary and secondary file sequences associated with the working version.
   * 
   * @throws PipelineException
   *   If unable to regenerate the checksums.
   */
  public void 
  refreshCheckSums
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs
  ) 
    throws PipelineException 
  {
    FileCheckSumReq req = new FileCheckSumReq(id, fseqs);

    //...

    throw new PipelineException("Not implemented yet.");
  }

  /**
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   * 
   * @param mod [<B>in</B>]
   *   The working version of the node.
   * 
   * @param vstate [<B>in</B>]
   *   The relationship between the revision numbers of working and checked-in versions 
   *   of the node.
   * 
   * @param latest [<B>in</B>]
   *   The revision number of the latest checked-in version.
   * 
   * @return
   *   The <CODE>FileState</CODE> of each the primary and secondary file associated with 
   *   the working version indexed by file sequence.
   * 
   * @throws PipelineException
   *   If unable to compute the file states.
   */ 
  public TreeMap<FileSeq, FileState[]>
  computeFileStates
  (
   NodeID id, 
   NodeMod mod, 
   VersionState vstate, 
   VersionID latest
  ) 
    throws PipelineException 
  {
    FileStateReq req = 
      new FileStateReq(id, vstate, mod.getWorkingID(), latest, mod.getSequences());

    //...

    throw new PipelineException("Not implemented yet.");
  }




  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/


}

