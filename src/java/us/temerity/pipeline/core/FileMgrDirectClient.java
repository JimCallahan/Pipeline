// $Id: FileMgrDirectClient.java,v 1.3 2006/06/25 23:30:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;

import java.io.*;
import java.net.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R   D I R E C T   C L I E N T                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The client-side manager of file system queries and operations. <P> 
 * 
 * This class contains file manager instance so that all operations are direct method calls
 * do NOT involve any network communication. <P> 
 * 
 * @see FileMgr
 */
class FileMgrDirectClient
  implements FileMgrClient
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager client.
   */
  public
  FileMgrDirectClient()
  {
    pFileMgr = new FileMgr();
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
  public void  
  createWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    FileCreateWorkingAreaReq req = new FileCreateWorkingAreaReq(author, view);

    Object obj = pFileMgr.createWorkingArea(req);
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
  public void  
  removeWorkingArea
  ( 
   String author, 
   String view   
  ) 
    throws PipelineException 
  {
    FileRemoveWorkingAreaReq req = new FileRemoveWorkingAreaReq(author, view);

    Object obj = pFileMgr.removeWorkingArea(req);
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
  public void
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
    FileStateReq req = 
      new FileStateReq(id, vstate, isFrozen, mod.getWorkingID(), latest, mod.getSequences());

    Object obj = pFileMgr.states(req); 
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
  public void 
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
    FileCheckInReq req = 
      new FileCheckInReq(id, vid, latest, mod.getSequences(), isNovel); 

    Object obj = pFileMgr.checkIn(req);
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
  public void 
  checkOut
  (
   NodeID id, 
   NodeVersion vsn, 
   boolean isFrozen
  ) 
    throws PipelineException 
  {
    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), 
			  isFrozen, !vsn.isActionEnabled());

    Object obj = pFileMgr.checkOut(req); 
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
  public void 
  revert
  (
   NodeID id, 
   TreeMap<String,VersionID> files, 
   boolean writeable   
  )
    throws PipelineException
  {
    FileRevertReq req = new FileRevertReq(id, files, writeable);
    
    Object obj = pFileMgr.revert(req);
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
  public void 
  clone
  (
   NodeID sourceID,
   NodeID targetID,
   TreeMap<File,File> files, 
   boolean writeable   
  )
    throws PipelineException
  {
    FileCloneReq req = new FileCloneReq(sourceID, targetID, files, writeable);
    
    Object obj = pFileMgr.clone(req);
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
  public void 
  remove 
  (
   NodeID id, 
   ArrayList<File> files
  ) 
    throws PipelineException 
  {
    FileRemoveReq req = 
      new FileRemoveReq(id, files);

    Object obj = pFileMgr.remove(req);
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
  public void 
  removeAll
  (
   NodeID id, 
   TreeSet<FileSeq> fseqs
  ) 
    throws PipelineException 
  {
    FileRemoveAllReq req = new FileRemoveAllReq(id, fseqs);

    Object obj = pFileMgr.removeAll(req);
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
  public void 
  rename 
  (
   NodeID id, 
   NodeMod mod,
   FilePattern pattern   
  ) 
    throws PipelineException 
  {
    FileRenameReq req = 
      new FileRenameReq(id, mod.getPrimarySequence(), mod.getSecondarySequences(), pattern);

    Object obj = pFileMgr.rename(req);
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
  public void 
  changeMode 
  (
   NodeID id, 
   NodeMod mod, 
   boolean writeable
  ) 
    throws PipelineException 
  {
    FileChangeModeReq req = 
      new FileChangeModeReq(id, mod.getSequences(), writeable);

    Object obj = pFileMgr.changeMode(req);
    handleSimpleResponse(obj);
  }

  /**
   * Remove the entire repository directory structure for the given node including all
   * files associated with all checked-in versions of a node.
   *
   * @param name
   *   The fully resolved node name. 
   */  
  public void 
  deleteCheckedIn
  (
   String name
  ) 
    throws PipelineException 
  {
    FileDeleteCheckedInReq req = new FileDeleteCheckedInReq(name);

    Object obj = pFileMgr.deleteCheckedIn(req);
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
  public TreeMap<String,TreeMap<VersionID,Long>>
  getArchiveSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
    throws PipelineException 
  {
    FileGetArchiveSizesReq req = new FileGetArchiveSizesReq(fseqs);

    Object obj = pFileMgr.getArchiveSizes(req);
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
   * @return
   *   The STDOUT output of the archiver process.
   */ 
  public String
  archive
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver,
   Map<String,String> env
  ) 
    throws PipelineException 
  {
    FileArchiveReq req = new FileArchiveReq(name, fseqs, archiver, env);

    Object obj = pFileMgr.archive(req);
    if(obj instanceof FileArchiverRsp) {
      FileArchiverRsp rsp = (FileArchiverRsp) obj;
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
  public TreeMap<String,TreeMap<VersionID,Long>>
  getOfflineSizes
  (
   TreeMap<String,TreeMap<VersionID,TreeSet<File>>> files
  ) 
    throws PipelineException 
  {
    FileGetOfflineSizesReq req = new FileGetOfflineSizesReq(files);

    Object obj = pFileMgr.getOfflineSizes(req);
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
   */  
  public void 
  offline
  (
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks
  ) 
    throws PipelineException 
  {
    FileOfflineReq req = new FileOfflineReq(name, vid, symlinks);

    Object obj = pFileMgr.offline(req);
    handleSimpleResponse(obj);
  }

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getOfflined() 
    throws PipelineException 
  {
    Object obj = pFileMgr.getOfflined();
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
   *   The timestamp of the start of the restore operation.
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
   * @return
   *   The STDOUT output of the archiver process.
   */ 
  public String
  extract
  (
   String archiveName, 
   Date stamp, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver, 
   Map<String,String> env, 
   long size
  ) 
    throws PipelineException 
  {
    FileExtractReq req = new FileExtractReq(archiveName, stamp, fseqs, archiver, env, size);

    Object obj = pFileMgr.extract(req);
    if(obj instanceof FileArchiverRsp) {
      FileArchiverRsp rsp = (FileArchiverRsp) obj;
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
   *   The timestamp of the start of the restore operation.
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
  public void 
  restore
  (
   String archiveName, 
   Date stamp,  
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   TreeMap<File,VersionID> targets
  ) 
    throws PipelineException 
  {
    FileRestoreReq req = new FileRestoreReq(archiveName, stamp, name, vid, symlinks, targets);

    Object obj = pFileMgr.restore(req);
    handleSimpleResponse(obj);
  }
  
  /**
   * Remove the temporary directory use to extract the files from an archive volume.
   * 
   * @param archiveName
   *   The name of the archive volume.
   * 
   * @param stamp
   *   The timestamp of the start of the restore operation.
   */
  public void 
  extractCleanup
  (
   String archiveName, 
   Date stamp
  ) 
    throws PipelineException 
  {
    FileExtractCleanupReq req = new FileExtractCleanupReq(archiveName, stamp); 

    Object obj = pFileMgr.extractCleanup(req);
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Handle the simple Success/Failure response.
   * 
   * @param obj
   *   The response from the server.
   */ 
  private void 
  handleSimpleResponse
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(!(obj instanceof SuccessRsp))
      handleFailure(obj);
  }

  /**
   * Handle non-successful responses.
   * 
   * @param obj
   *   The response from the server.
   */ 
  private void 
  handleFailure
  ( 
   Object obj
  )
    throws PipelineException
  {
    if(obj instanceof FailureRsp) {
      FailureRsp rsp = (FailureRsp) obj;
      throw new PipelineException(rsp.getMessage());	
    }
    else {
      throw new PipelineException
	("Illegal response received from the server instance!");
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file manager instance.
   */ 
  private FileMgr  pFileMgr;   

}

