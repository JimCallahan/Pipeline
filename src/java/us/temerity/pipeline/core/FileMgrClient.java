// $Id: FileMgrClient.java,v 1.22 2005/01/15 02:53:20 jim Exp $

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
    this(PackageInfo.sFileServer, PackageInfo.sFilePort);
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
   * @param isFrozen
   *   Whether the files associated with the working version are symlinks to the 
   *   checked-in files instead of copies.
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
   boolean isFrozen, 
   VersionID latest, 
   TreeMap<FileSeq, FileState[]> states, 
   TreeMap<FileSeq, Date[]> timestamps
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileStateReq req = 
      new FileStateReq(id, vstate, isFrozen, mod.getWorkingID(), latest, mod.getSequences());

    Object obj = performTransaction(FileRequest.State, req);

    if(obj instanceof FileStateRsp) {
      FileStateRsp rsp = (FileStateRsp) obj;
      states.putAll(rsp.getFileStates());
      if(rsp.getTimeStamps() != null) 
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
   *   If unable to check-in the files.
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
   * 
   * @param isFrozen
   *   Whether the files associated with the working version should be symlinks to the 
   *   checked-in files instead of copies.
   * 
   * @throws PipelineException
   *   If unable to check-out the files.
   */ 
  public synchronized void 
  checkOut
  (
   NodeID id, 
   NodeVersion vsn, 
   boolean isFrozen
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), 
			  isFrozen, !vsn.isActionEnabled());

    Object obj = performTransaction(FileRequest.CheckOut, req);
    handleSimpleResponse(obj);
  }

  /**
   * Revert specific working area files to an earlier checked-in version of the files. <P> 
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param files
   *   The table of checked-in file revision numbers indexed by file name.
   * 
   * @param writeable
   *   Whether the reverted working area files should be made writable.
   * 
   * @throws PipelineException
   *   If unable to revert the files.
   */ 
  public synchronized void 
  revert
  (
   NodeID id, 
   TreeMap<String,VersionID> files, 
   boolean writeable   
  )
    throws PipelineException
  {
    verifyConnection();

    FileRevertReq req = new FileRevertReq(id, files, writeable);
    
    Object obj = performTransaction(FileRequest.Revert, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove specific files associated with the given working version.
   *
   * @param id 
   *   The unique working version identifier.
   * 
   * @param files
   *   The specific files to remove.
   */  
  public synchronized void 
  remove 
  (
   NodeID id, 
   ArrayList<File> files
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRemoveReq req = 
      new FileRemoveReq(id, files);

    Object obj = performTransaction(FileRequest.Remove, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the all of the files associated with the given working version.
   *
   * @param id 
   *   The unique working version identifier.
   * 
   * @param fseqs
   *   The primary/secondary file sequences. 
   */  
  public synchronized void 
  removeAll
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRemoveAllReq req = new FileRemoveAllReq(id, fseqs);

    Object obj = performTransaction(FileRequest.RemoveAll, req);
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

  /**
   * Change the user write permission of all existing files associated with the given 
   * working version.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   * 
   * @param writeable
   *   Whether the working area files should be made writable by the owning user.
   */ 
  public synchronized void 
  changeMode 
  (
   NodeID id, 
   NodeMod mod, 
   boolean writeable
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileChangeModeReq req = 
      new FileChangeModeReq(id, mod.getSequences(), writeable);

    Object obj = performTransaction(FileRequest.ChangeMode, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the entire repository directory structure for the given node including all
   * files associated with all checked-in versions of a node.
   *
   * @param name
   *   The fully resolved node name. 
   */  
  public synchronized void 
  deleteCheckedIn
  (
   String name
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileDeleteCheckedInReq req = new FileDeleteCheckedInReq(name);

    Object obj = performTransaction(FileRequest.DeleteCheckedIn, req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the files associated with the given checked-in version of a node.
   *
   * @param name
   *   The fully resolved node name. 
   * 
   * @param vid
   *   The revision number.
   */  
  public synchronized void 
  offline
  (
   String name, 
   VersionID vid
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileOfflineReq req = new FileOfflineReq(name, vid);

    Object obj = performTransaction(FileRequest.Offline, req);
    handleSimpleResponse(obj);
  }

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getOfflined() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performTransaction(FileRequest.GetOfflined, null);
    if(obj instanceof FileGetOfflinedRsp) {
      FileGetOfflinedRsp rsp = (FileGetOfflinedRsp) obj;
      return rsp.getVersions();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for archival purposes. <P> 
   * 
   * File sizes are computed from the target of any symbolic links and therefore reflects the 
   * amount of bytes that would need to be copied if the files where archived.  This may be
   * considerably more than the actual amount of disk space used when several versions of 
   * a node have identical files. <P> 
   * 
   * @param fseqs
   *   The files sequences indexed by fully resolved node names and revision numbers.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getArchivedSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileGetSizesReq req = new FileGetSizesReq(fseqs, false);

    Object obj = performTransaction(FileRequest.GetSizes, req);
    if(obj instanceof FileGetSizesRsp) {
      FileGetSizesRsp rsp = (FileGetSizesRsp) obj;
      return rsp.getSizes();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions for offlining purposes. <P> 
   * 
   * File sizes reflect the actual amount of bytes that will be freed from disk if the 
   * given checked-in versions are offlined.  A file will only be added to this freed
   * size if it a regular file and there are no symbolic links which target it which are
   * not included in the given file sequences. <P> 
   * 
   * @param fseqs
   *   The files sequences indexed by fully resolved node names and revision numbers.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getOfflinedSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileGetSizesReq req = new FileGetSizesReq(fseqs, true);

    Object obj = performTransaction(FileRequest.GetSizes, req);
    if(obj instanceof FileGetSizesRsp) {
      FileGetSizesRsp rsp = (FileGetSizesRsp) obj;
      return rsp.getSizes();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plfilemgr(1) server daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }
}

