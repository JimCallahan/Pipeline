// $Id: FileMgrClient.java,v 1.50 2009/09/21 23:21:45 jim Exp $

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
  /*   R U N T I M E   C O N T R O L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   */ 
  public MasterControls
  getRuntimeControls() 
    throws PipelineException;

  /**
   * Set the current runtime performance controls.
   */ 
  public void
  setRuntimeControls
  (
   MasterControls controls
  ) 
    throws PipelineException;
  


  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the File Manager can create temporary files.
   */ 
  public void 
  validateScratchDir() 
    throws PipelineException;    


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
   * The <CODE>jobStates</CODE> argument may contain <CODE>null</CODE> entries if there are 
   * no jobs which regenerate the corresponding primary/secondary file.
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
   * @param jobStates 
   *   The jobs states for each primary/secondary file (if any).
   * 
   * @param isFrozen
   *   Whether the files associated with the working version are symlinks to the 
   *   checked-in files instead of copies.
   * 
   * @param latest 
   *   The revision number of the latest checked-in version.
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
   * @return
   *   The updated cache of checksums for files associated with the working version.
   * 
   * @throws PipelineException
   *   If unable to compute the file states.
   */ 
  public CheckSumCache
  states
  (
   NodeID id, 
   NodeMod mod, 
   VersionState vstate, 
   JobState jobStates[], 
   boolean isFrozen, 
   VersionID latest, 
   boolean isBaseIntermediate, 
   SortedMap<String,CheckSum> baseCheckSums, 
   boolean isLatestIntermediate, 
   SortedMap<String,CheckSum> latestCheckSums, 
   CheckSumCache workingCheckSums, 
   TreeMap<FileSeq,FileState[]> states, 
   TreeMap<FileSeq,Long[]> timestamps
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
   * @param workingCheckSums
   *   Current cache of checksums for files associated with the working version.
   * 
   * @param movedStamps
   *   A table into which the timestamps are recorded for files before being moved into the
   *   repository and the symlink created after the move.
   * 
   * @return
   *   The updated cache of checksums for files associated with the working version.
   * 
   * @throws PipelineException
   *   If unable to check-in the files.
   */
  public CheckSumCache
  checkIn
  (
   NodeID id, 
   NodeMod mod, 
   VersionID vid,
   VersionID latest, 
   TreeMap<FileSeq,boolean[]> isNovel,
   CheckSumCache workingCheckSums, 
   TreeMap<String,Long[]> movedStamps
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
   * @param isLinked
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
   boolean isLinked
  ) 
    throws PipelineException;

  /**
   * Overwrite any files associated with the given working version of the node which are 
   * currently symlinks or are missing with corresponding files associated with the given 
   * checked-in version. <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param vsn 
   *   The checked-in version to check-out.
   * 
   * @throws PipelineException
   *   If unable to check-out the files.
   */ 
  public void 
  checkOutPrelinked
  (
   NodeID id, 
   NodeVersion vsn
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
   * @param isLinked
   *   Whether the files associated with the working version should be symlinks to the 
   *   checked-in files instead of copies.
   * 
   * @throws PipelineException
   *   If unable to revert the files.
   */ 
  public void 
  revert
  (
   NodeID id, 
   TreeMap<String,VersionID> files, 
   boolean isLinked 
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
    throws PipelineException;
    
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
    throws PipelineException;

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
    throws PipelineException;


  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Creates a JAR archive containing both files and metadata associated with a checked-in
   * version of a node suitable for transfer to a remote site.<P> 
   * 
   * The JAR archive will contain a copy of the original NodeVersion which has been altered 
   * from its original form in several ways:<P>
   * 
   * <DIV style="margin-left: 40px;">
   *   The full name of the target node as well as the names of any source nodes of this
   *   target node will have been changed to append the "localSiteName" as the last 
   *   directory component of the node path before the node prefix.  <P> 
   * 
   *   For each source node listed in the "referenceNames", the link type will be changed 
   *   to Reference.  The name of the source nodes will also be modified to include the 
   *   "localSiteName" as the last directory component.  Any source nodes not contained in 
   *   "referenceNames" will be removed as a source for the target node.<P> 
   * 
   *   Any action associated with the target node will be removed.
   * 
   *   A RemoteVersion per-version annotation will be added to the NodeVersion who's 
   *   annotation parameters included detailed information about the original node version
   *   being extracted.  This includes the original node name, local site name as well as
   *   information about when the JAR archive was created and by whom. 
   * </DIV><P> 
   *   
   * Each file associated with the target node will also be copied and included in the JAR 
   * archive generated.  These files will also be altered from their original in the 
   * following ways:<P> 
   * 
   * <DIV style="margin-left: 40px;">
   *   The names of the files will similarly renamed to include the local site name as the 
   *   last directory component of the file path.<P> 
   * 
   *   If a file is part of one of the primary/secondary file sequences contained in 
   *   "replaceSeqs", then a series of string substitutions will be performed on each file 
   *   to make it portable to the new site.  All occurances of the names of source nodes 
   *   included in the "referenceNames" will be automatically changed to include the local 
   *   site name.  In addition, all key entries in the "replacements" table will replaced 
   *   by their value in this table.  This provides a way of adding in any arbitrary site 
   *   localization fixes which may be node specific.
   * </DIV><P> 
   * 
   * In addition to a GLUE format file containing the altered NodeVersion copy and associated
   * node files, a "README" text file will also be added to the JAR archive which details 
   * the contents and all changes made to the node version being extracted.<P> 
   * 
   * If successfull, the JAR archive file will be written to "jarPath".<P>
   * 
   * This method will go away when true multi-site support is added to Pipeline.<P>  
   * 
   * @param name
   *   The fully resolved node name of the node to extract.
   * 
   * @param referenceNames
   *   The fully resolved names of the source nodes to include as Reference links or
   *   <CODE>null</CODE> if no links should be included.
   * 
   * @param localSiteName
   *   Name for the local site which will be used to modify extracted node names.
   * 
   * @param replaceSeqs
   *   The primary and secondary file sequences associated with the node to which all 
   *   string replacements should be applied or <CODE>null</CODE> to skip all file contents 
   *   replacements.
   * 
   * @param replacements
   *   The table of additional string replacements to perform on the files associated
   *   with the node version being extracted or <CODE>null</CODE> if there are no
   *   additional replacements. 
   * 
   * @param vsn
   *   The extracted node version with all modifications applied to include in the 
   *   JAR archive.
   * 
   * @param stamp
   *   The timestamp of when this node was extracted.
   * 
   * @param creator
   *   The name of the user who extracted the node.
   * 
   * @param jarPath
   *   The name of the JAR archive to create.
   */ 
  public void 
  extractSiteVersion
  (
   String name, 
   TreeSet<String> referenceNames, 
   String localSiteName, 
   TreeSet<FileSeq> replaceSeqs, 
   TreeMap<String,String> replacements,
   NodeVersion vsn, 
   long stamp, 
   String creator, 
   Path jarPath
  )
    throws PipelineException;

  /**
   * Lookup the NodeVersion contained within the extracted site version JAR archive.
   * 
   * @param jarPath
   *   The name of the JAR archive to read.
   */ 
  public NodeVersion
  lookupSiteVersion
  ( 
   Path jarPath
  ) 
    throws PipelineException;

  /**
   * Extract the node files in a extracted site version JAR archive and insert them into the 
   * repository.
   * 
   * @param jarPath
   *   The name of the JAR archive to read.
   */ 
  public void
  insertSiteVersion
  ( 
   Path jarPath
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
  public String
  archive
  (
   String name, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   BaseArchiver archiver,
   Map<String,String> env, 
   StringBuilder dryRunResults
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
   * 
   * @param dryRunResults
   *   If not <CODE>null</CODE>, the operation will not be performed but the given buffer
   *   will be filled with a message detailing the steps that would have been performed
   *   during an actual execution.
   */  
  public void 
  offline
  (
   String name, 
   VersionID vid, 
   TreeMap<File,TreeSet<VersionID>> symlinks, 
   StringBuilder dryRunResults
  ) 
    throws PipelineException;

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   */
  public TreeMap<String,TreeSet<VersionID>>
  getOfflined() 
    throws PipelineException;
  
  /**
   * Get the revision numbers of all offlined checked-in versions of the given node.
   *
   * @param name
   *   The fully resolved node name.
   */
  public TreeSet<VersionID>
  getOfflinedNodeVersions
  (
   String name
  ) 
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
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   * 
   * @param fseqs
   *   The file sequences to archive indexed by fully resolved node name and checked-in 
   *   revision number.
   * 
   * @param checkSums
   *   Read-only checksums for all files associated with the checked-in version
   *   being extracted indexed by fully resolved node name and checked-in 
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
  public String
  extract
  (
   String archiveName, 
   long stamp, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs, 
   TreeMap<String,TreeMap<VersionID,SortedMap<String,CheckSum>>> checkSums, 
   BaseArchiver archiver, 
   Map<String,String> env, 
   long size, 
   StringBuilder dryRunResults
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
  public void 
  restore
  (
   String archiveName, 
   long stamp,  
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
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   */
  public void 
  extractCleanup
  (
   String archiveName, 
   long stamp
  ) 
    throws PipelineException;

}

