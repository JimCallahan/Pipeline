// $Id: FileMgrClient.java,v 1.33 2005/04/03 06:08:09 jim Exp $

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
 * @see FileMgr
 * @see FileMgrServer
 */
interface FileMgrClient
{  
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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;


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
    throws PipelineException;

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
   * @return
   *   The STDOUT output of the archiver process.
   */ 
  public String
  archive
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver
  ) 
    throws PipelineException;

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
    throws PipelineException;

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
    throws PipelineException;

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getOfflined() 
    throws PipelineException;
  

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
   long size
  ) 
    throws PipelineException;

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
    throws PipelineException;
  
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
    throws PipelineException;

}

