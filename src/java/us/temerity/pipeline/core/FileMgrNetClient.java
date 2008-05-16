// $Id: FileMgrNetClient.java,v 1.12 2008/05/16 01:11:40 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   N E T   C L I E N T                                                  */
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
class FileMgrNetClient
  extends BaseMgrClient
  implements FileMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager client.
   */
  public
  FileMgrNetClient()
  {
    super(PackageInfo.sFileServer, PackageInfo.sFilePort, 
	  FileRequest.Disconnect, FileRequest.Shutdown);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the File Manager can create temporary files.
   */ 
  public void 
  validateScratchDir() 
    throws PipelineException
  {
    verifyConnection();
    
    Object obj = performTransaction(FileRequest.ValidateScratchDir, null);
    handleSimpleResponse(obj);
  }


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
   * Remove an entire working area directory for the given user and view. <P> 
   * 
   * If the working area directory does not exist, the operation is successful even though 
   * nothing is actually done.
   * 
   * @param author 
   *   The name of the user which owns the working area.
   * 
   * @param view 
   *   The name of the user's working area view. 
   *
   * @throws PipelineException
   *   If unable to remove the working area directory.
   */
  public synchronized void  
  removeWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRemoveWorkingAreaReq req = new FileRemoveWorkingAreaReq(author, view);

    Object obj = performLongTransaction(FileRequest.RemoveWorkingArea, req, 15000, 60000);  
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
   * @param critical
   *   The last legitimate change time (ctime) of the file.
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
   long critical, 
   TreeMap<FileSeq, FileState[]> states, 
   TreeMap<FileSeq, Long[]> timestamps
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileStateReq req = 
      new FileStateReq(id, vstate, isFrozen, mod.getWorkingID(), latest, critical, 
		       mod.getSequences());

    Object obj = performLongTransaction(FileRequest.State, req, 15000, 60000);  

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

    Object obj = performLongTransaction(FileRequest.CheckIn, req, 15000, 60000);  
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

    Object obj = performLongTransaction(FileRequest.CheckOut, req, 15000, 60000);  
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
    
    Object obj = performLongTransaction(FileRequest.Revert, req, 15000, 60000);  
    handleSimpleResponse(obj);
  }

  /**
   * Replace the primary files associated one node with the primary files of another node. <P>
   * 
   * If the <CODE>author</CODE> argument is different than the current user, this method 
   * will fail unless the current user has privileged access status.
   * 
   * @param sourceID
   *   The unique working version identifier of the node owning the files being copied. 
   * 
   * @param targetID
   *   The unique working version identifier of the node owning the files being replaced.
   * 
   * @param files
   *   The target files to copy indexed by corresponding source files.
   * 
   * @param writeable
   *   Whether the target node's files should be made writable.
   * 
   * @throws PipelineException
   *   If unable to clone the files.
   */ 
  public synchronized void 
  clone
  (
   NodeID sourceID,
   NodeID targetID,
   TreeMap<File,File> files, 
   boolean writeable   
  )
    throws PipelineException
  {
    verifyConnection();

    FileCloneReq req = new FileCloneReq(sourceID, targetID, files, writeable);
    
    Object obj = performLongTransaction(FileRequest.Clone, req, 15000, 60000);  
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

    Object obj = performLongTransaction(FileRequest.Remove, req, 15000, 60000);  
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

    Object obj = performLongTransaction(FileRequest.RemoveAll, req, 15000, 60000);  
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
   * 
   * @param pattern
   *   The new fully resolved file pattern.
   */  
  public synchronized void 
  rename 
  (
   NodeID id, 
   NodeMod mod,
   FilePattern pattern   
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRenameReq req = 
      new FileRenameReq(id, mod.getPrimarySequence(), mod.getSecondarySequences(), pattern);

    Object obj = performLongTransaction(FileRequest.Rename, req, 15000, 60000);  
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

    Object obj = performLongTransaction(FileRequest.ChangeMode, req, 15000, 60000);  
    handleSimpleResponse(obj);
  }

  /**
   * Update the last modification time stamp of all files associated with the given 
   * working version.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param mod 
   *   The working version of the node.
   */ 
  public void 
  touchAll 
  (
   NodeID id, 
   NodeMod mod
  ) 
    throws PipelineException
  {
    verifyConnection();

    FileTouchAllReq req = 
      new FileTouchAllReq(id, mod.getSequences());

    Object obj = performLongTransaction(FileRequest.TouchAll, req, 15000, 60000);  
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

    Object obj = performLongTransaction(FileRequest.DeleteCheckedIn, req, 15000, 60000);  
    handleSimpleResponse(obj);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new node bundle.<P> 
   * 
   * @param bundle
   *   The node bundle metadata. 
   * 
   * @return
   *   The abstract file system path to the newly create node bundle.
   */ 
  public Path
  packNodes
  (
   NodeBundle bundle
  ) 
    throws PipelineException
  {
    verifyConnection();

    FilePackNodesReq req = new FilePackNodesReq(bundle);

    Object obj = performLongTransaction(FileRequest.PackNodes, req, 15000, 60000);
    if(obj instanceof FilePackNodesRsp) {
      FilePackNodesRsp rsp = (FilePackNodesRsp) obj;
      return rsp.getPath();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
    
  /**
   * Extract the node metadata from a node bundle containing a tree of nodes packed at 
   * another site. <P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   */ 
  public NodeBundle
  extractBundle
  (
   Path bundlePath
  ) 
    throws PipelineException
  {
    verifyConnection();

    FileExtractBundleReq req = new FileExtractBundleReq(bundlePath);

    Object obj = performTransaction(FileRequest.ExtractBundle, req);
    if(obj instanceof FileExtractBundleRsp) {
      FileExtractBundleRsp rsp = (FileExtractBundleRsp) obj;
      return rsp.getBundle();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  
  /**
   * Unpack a node bundle files into the given working area.<P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   * 
   * @param bundle
   *   The node bundle metadata. 
   * 
   * @param author 
   *   The name of the user which owns the working version.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param skipUnpack
   *   The names the nodes who's files should not be unpacked. 
   */ 
  public void
  unpackNodes
  ( 
   Path bundlePath, 
   NodeBundle bundle,
   String author, 
   String view, 
   TreeSet<String> skipUnpack
  ) 
    throws PipelineException
  {
    verifyConnection();

    FileUnpackNodesReq req = 
      new FileUnpackNodesReq(bundlePath, bundle, author, view, skipUnpack);
    
    Object obj = performLongTransaction(FileRequest.UnpackNodes, req, 15000, 60000);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/

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
  getArchiveSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileGetArchiveSizesReq req = new FileGetArchiveSizesReq(fseqs);

    Object obj = performLongTransaction(FileRequest.GetArchiveSizes, req, 15000, 60000);  
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
   * Create an archive volume by running the given archiver plugin on a set of checked-in 
   * file sequences. 
   * 
   * @param name 
   *   The name of the archive volume to create.
   * 
   * @param fseqs
   *   The file sequences to archive indexed by fully resolved node name and checked-in 
   *   revision number.
   * 
   * @param archiver
   *   The archiver plugin to use to create the archive volume.
   * 
   * @param env
   *   The cooked toolset environment.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   * 
   * @return
   *   The STDOUT output of the archiver process.
   */ 
  public synchronized String
  archive
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver,
   Map<String,String> env, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileArchiveReq req = 
      new FileArchiveReq(name, fseqs, archiver, env, dryRunResults != null);

    Object obj = performLongTransaction(FileRequest.Archive, req, 15000, 60000);  
    if(obj instanceof FileArchiverRsp) {
      FileArchiverRsp rsp = (FileArchiverRsp) obj;
      if(dryRunResults != null) 
        dryRunResults.append(rsp.getMessage());
      return rsp.getOutput();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * Calculate the total size (in bytes) of specific files associated with the given 
   * checked-in versions for offlining purposes. <P> 
   * 
   * Only files which contribute to the offline size should be passed to this method
   * as members of the <CODE>files</CODE> parameter.
   * 
   * @param files
   *   The specific files indexed by fully resolved node names and revision numbers.
   * 
   * @return
   *   The total version file sizes indexed by fully resolved node name and revision number.
   */ 
  public synchronized TreeMap<String,TreeMap<VersionID,Long>>
  getOfflineSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<File>>> files
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileGetOfflineSizesReq req = new FileGetOfflineSizesReq(files);

    Object obj = performLongTransaction(FileRequest.GetOfflineSizes, req, 15000, 60000);  
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
   * Remove the files associated with the given checked-in version of a node. <P> 
   * 
   * @param name
   *   The fully resolved node name. 
   * 
   * @param vid
   *   The revision number.
   * 
   * @param symlinks
   *   The revision numbers of the symlinks from later versions which target files being 
   *   offlined, indexed by the names of the to be offlined files.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   */  
  public synchronized void 
  offline
  (
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileOfflineReq req = new FileOfflineReq(name, vid, symlinks, dryRunResults != null);

    Object obj = performLongTransaction(FileRequest.Offline, req, 15000, 60000);  
    if(obj instanceof DryRunRsp) {
      DryRunRsp rsp = (DryRunRsp) obj; 
      dryRunResults.append(rsp.getMessage());
    }
    else if(!(obj instanceof SuccessRsp)) {
      handleFailure(obj);
    }
  }

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public synchronized TreeMap<String,TreeSet<VersionID>>
  getOfflined() 
    throws PipelineException 
  {
    verifyConnection();

    Object obj = performLongTransaction(FileRequest.GetOfflined, null, 15000, 60000); 
    if(obj instanceof FileGetOfflinedRsp) {
      FileGetOfflinedRsp rsp = (FileGetOfflinedRsp) obj;
      return rsp.getVersions();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }
  

  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Extract the files associated with the given checked-in versions from the given archive 
   * volume and place them into a temporary directory.
   * 
   * @param archiveName 
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp(milliseconds since midnight, January 1, 1970 UTC)  of the start of 
   *   the restore operation.
   * 
   * @param fseqs
   *   The file sequences to archive indexed by fully resolved node name and checked-in 
   *   revision number.
   * 
   * @param archiver
   *   The archiver plugin to use to restore the versions from the archive volume.
   * 
   * @param env
   *   The cooked toolset environment.
   * 
   * @param size
   *   The required temporary disk space needed for the restore operation.
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   * 
   * @return
   *   The STDOUT output of the archiver process.
   */ 
  public synchronized String
  extract
  (
   String archiveName, 
   long stamp, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver, 
   Map<String,String> env, 
   long size, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileExtractReq req = 
      new FileExtractReq(archiveName, stamp, fseqs, archiver, env, size, 
                         dryRunResults != null);

    Object obj = performLongTransaction(FileRequest.Extract, req, 15000, 60000); 
    if(obj instanceof FileArchiverRsp) {
      FileArchiverRsp rsp = (FileArchiverRsp) obj;
      if(dryRunResults != null) 
        dryRunResults.append(rsp.getMessage());
      return rsp.getOutput();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Move the files extracted from the archive volume into the repository. <P> 
   * 
   * Depending on the current state of files in the repository and whether files are 
   * identical across multiple revision of a node, the extracted files will either be 
   * moved into the repository or symlinks will be created in the repository for the files.
   * In addition, symlinks for later versions may be changed to target the newly restored
   * files.
   * 
   * @param archiveName
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp(milliseconds since midnight, January 1, 1970 UTC)  of the start of 
   *   the restore operation.
   * 
   * @param name
   *   The fully resolved node name. 
   * 
   * @param vid
   *   The revision number.
   * 
   * @param symlinks
   *   The revision numbers of the existing checked-in symlinks which should target the 
   *   restored file indexed by restored filename.
   * 
   * @param targets
   *   The revision number of the targets of the restored symlinks indexed by restored 
   *   symlink filename.
   */ 
  public synchronized void 
  restore
  (
   String archiveName, 
   long stamp,  
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   TreeMap<File,VersionID> targets
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileRestoreReq req = new FileRestoreReq(archiveName, stamp, name, vid, symlinks, targets);

    Object obj = performLongTransaction(FileRequest.Restore, req, 15000, 60000);  
    handleSimpleResponse(obj);
  }
  
  /**
   * Remove the temporary directory use to extract the files from an archive volume.
   * 
   * @param archiveName
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   */
  public synchronized void 
  extractCleanup
  (
   String archiveName, 
   long stamp
  ) 
    throws PipelineException 
  {
    verifyConnection();

    FileExtractCleanupReq req = new FileExtractCleanupReq(archiveName, stamp); 

    Object obj = performLongTransaction(FileRequest.ExtractCleanup, req, 15000, 60000); 
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the object input given a socket input stream.
   */ 
  protected ObjectInput
  getObjectInput
  (
   InputStream in
  ) 
    throws IOException
  {
    return new PluginInputStream(in);
  }

  /**
   * Get the error message to be shown when the server cannot be contacted.
   */ 
  protected String
  getServerDownMessage()
  {
    return ("Unable to contact the the plfilemgr(1) daemon running on " +
	    "(" + pHostname + ") using port (" + pPort + ")!");
  }
}

