// $Id: FileMgrClient.java,v 1.10 2004/07/07 13:19:59 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   C L I E N T                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side manager of file system queries and operations. <P> 
 * 
 * This class handles network communication with the Pipeline file management daemon 
 * <A HREF="../../../../man/plfilemgr.html"><B>plfilemgr</B><A>(1) running on the file 
 * server. An instance of this class is used by the Pipeline master server daemon 
 * <A HREF="../../../../man/plmaster.html"><B>plmaster</B><A>(1) to communicate with 
 * <B>plfilemgr</B>(1).
 * 
 * @see FileMgr
 * @see FileMgrServer
 */
public
class FileMgrClient
  extends BaseMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager client.
   * 
   * @param hostname 
   *   The name of the host running the <B>plfilemgr</B><A>(1).
   * 
   * @param port 
   *   The network port listened to by <B>plfilemgr</B><A>(1).
   */
  public
  FileMgrClient
  ( 
   String hostname, 
   int port
  ) 
  {
    super(hostname, port, 
	  FileRequest.Disconnect, FileRequest.Shutdown);
  }

  /** 
   * Construct a new file manager client using the default hostname and port. <P> 
   * 
   * The hostname and port used are those specified by the 
   * <CODE><B>--file-host</B>=<I>host</I></CODE> and 
   * <CODE><B>--file-port</B>=<I>num</I></CODE> options to <B>plconfig</B>(1).
   */
  public
  FileMgrClient() 
  {
    super(PackageInfo.sFileServer, PackageInfo.sFilePort, 
	  FileRequest.Disconnect, FileRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new empty working area directory for the given user and view. <P> 
   * 
   * If the working area directory already exists, the operation is successful even though 
   * nothing is actually done.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @throws PipelineException
   *   If unable to create the working area directory.
   */
  public synchronized void  
  createWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCreateWorkingAreaReq req = new FileCreateWorkingAreaReq(author, view);

    Object obj = performTransaction(FileRequest.CreateWorkingArea, req);
    handleSimpleResponse(obj);
  }

  /**
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial 
   * working version. <P> 
   * 
   * The <CODE>states</CODE> and <CODE>timestamps</CODE> arguments should be empty 
   * tables as they are populated by a successful invocation of this method. <P> 
   * 
   * The <CODE>timestamps</CODE> argument may contain <CODE>null</CODE> entries for files
   * which for which no working version exists.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @param vstate 
   *   The relationship between the revision numbers of working and checked-in versions 
   *   of the node.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @param states
   *   An empty table which will be filled with the <CODE>FileState</CODE> of each the 
   *   primary and secondary file associated with the working version indexed by file 
   *   sequence.
   * 
   * @param timestamps
   *   An empty table which will be filled with the last modification timestamps of 
   *   each primary and secondary file associated with the working version indexed by file 
   *   sequence.  
   * 
   * @throws PipelineException
   *   If unable to compute the file states.
   */ 
  public synchronized void
  states
  (
   NodeID id, 
   NodeMod mod, 
   VersionState vstate, 
   VersionID latest, 
   TreeMap<FileSeq, FileState[]> states, 
   TreeMap<FileSeq, Date[]> timestamps
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileStateReq req = 
      new FileStateReq(id, vstate, mod.getWorkingID(), latest, mod.getSequences());

    Object obj = performTransaction(FileRequest.State, req);

    if(obj instanceof FileStateRsp) {
      FileStateRsp rsp = (FileStateRsp) obj;
      states.putAll(rsp.getFileStates());
      timestamps.putAll(rsp.getTimeStamps());
    }
    else {
      handleFailure(obj);
    }
  }

  /**
   * Perform the file system operations needed to create a new checked-in version of the 
   * node in the file repository based on the given working version. <P> 
   * 
   * The <CODE>latest</CODE> argument may be <CODE>null</CODE> if this is an initial 
   * working version. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @param vid 
   *   The revision number of the new checked-in version being created.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
   * 
   * @param isNovel
   *   Whether each file associated with the version contains new data not present in the
   *   previous checked-in version.
   * 
   * @throws PipelineException
   *   If unable to check-in the working files.
   */
  public synchronized void 
  checkIn
  (
   NodeID id, 
   NodeMod mod, 
   VersionID vid,
   VersionID latest, 
   TreeMap<FileSeq,boolean[]> isNovel
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckInReq req = 
      new FileCheckInReq(id, vid, latest, mod.getSequences(), isNovel); 

    Object obj = performTransaction(FileRequest.CheckIn, req);
    handleSimpleResponse(obj);
  }

  /**
   * Overwrite the files associated with the given working version of the node with the 
   * files associated with the given checked-in version. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vsn 
   *   The checked-in version to check-out.
   */ 
  public synchronized void 
  checkOut
  (
   NodeID id, 
   NodeVersion vsn
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), !vsn.hasAction());

    Object obj = performTransaction(FileRequest.CheckOut, req);
    handleSimpleResponse(obj);
  }

  /**
   * Replaces the files associated with the given working version with symlinks to the 
   * respective files associated with the checked-in version upon which the working 
   * version is based.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */
  public synchronized void 
  freeze
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException 
  {
    if(mod.isFrozen()) 
      throw new IllegalArgumentException
	("Cannot freeze an already frozen working version!");

    verifyConnection();

    FileFreezeReq req = 
      new FileFreezeReq(id, mod.getWorkingID(), mod.getSequences());

    Object obj = performTransaction(FileRequest.Freeze, req);
    handleSimpleResponse(obj);
  }

  /**
   * Replace the symlinks associated with the given working version with copies of the 
   * respective checked-in files which are the current targets of the symlinks. 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */  
  public synchronized void 
  unfreeze
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException 
  {
    if(mod.isFrozen()) 
      throw new IllegalArgumentException
	("Cannot unfreeze a working version which is not currently frozen!");

    verifyConnection();

    FileUnfreezeReq req = 
      new FileUnfreezeReq(id, mod.getWorkingID(), mod.getSequences(), !mod.hasAction());

    Object obj = performTransaction(FileRequest.Unfreeze, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the files associated with the given working version.
   *
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */  
  public synchronized void 
  remove 
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRemoveReq req = 
      new FileRemoveReq(id, mod.getSequences());

    Object obj = performTransaction(FileRequest.Remove, req);
    handleSimpleResponse(obj);
  }

  /**
   * Rename the files associated with the given working version.
   *
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */  
  public synchronized void 
  rename 
  (
   NodeID id, 
   NodeMod mod, 
   String newName
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRenameReq req = 
      new FileRenameReq(id, mod.getSequences(), newName);

    Object obj = performTransaction(FileRequest.Rename, req);
    handleSimpleResponse(obj);
  }
}

