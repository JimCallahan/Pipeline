// $Id: FileMgrDirectClient.java,v 1.23 2010/01/15 22:08:52 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.file.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.message.simple.*;

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
   * 
   * @param fileStatDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for file status query traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   * 
   * @param checksumDir
   *   An alternative root production directory accessed via a different NFS mount point
   *   to provide an exclusively network for checksum generation traffic.  Setting this to 
   *   <CODE>null</CODE> will cause the default root production directory to be used instead.
   */
  public
  FileMgrDirectClient
  (
   Path fileStatDir, 
   Path checksumDir
  ) 
  {
    pFileMgr = new FileMgr(false, fileStatDir, checksumDir);
    pOpNotifier = new DirectOpNotifier();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   C O N T R O L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   */ 
  public MasterControls
  getRuntimeControls() 
    throws PipelineException 
  {
    Object obj = pFileMgr.getRuntimeControls(); 
    if(obj instanceof MiscGetMasterControlsRsp) {
      MiscGetMasterControlsRsp rsp = (MiscGetMasterControlsRsp) obj;
      return rsp.getControls();
    }
    else {
      handleFailure(obj);
      return null;
    }    
  }

  /**
   * Set the current runtime performance controls.
   */ 
  public void
  setRuntimeControls
  (
   MasterControls controls
  ) 
    throws PipelineException 
  {
    MiscSetMasterControlsReq req = new MiscSetMasterControlsReq(controls);

    Object obj = pFileMgr.setRuntimeControls(req);
    handleSimpleResponse(obj);
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
    Object obj = pFileMgr.validateScratchDir();
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
   * @param fileInfos
   *   An empty table which will be filled with per-file information for each primary and 
   *   secondary file associated with the working version indexed by file sequence.  
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
   TreeMap<FileSeq,NativeFileInfo[]> fileInfos
  ) 
    throws PipelineException 
  {
    Long ctime = null;
    if(mod != null)
      ctime = mod.getLastCTimeUpdate(); 
      
    FileStateReq req = 
      new FileStateReq(id, vstate, jobStates, isFrozen, mod.isActionEnabled(), 
                       mod.getWorkingID(), latest, ctime, mod.getSequences(), 
                       isBaseIntermediate, baseCheckSums, isLatestIntermediate, 
                       latestCheckSums, workingCheckSums);

    Object obj = pFileMgr.states(req); 
    if(obj instanceof FileStateRsp) {
      FileStateRsp rsp = (FileStateRsp) obj;
      states.putAll(rsp.getFileStates());
      if(rsp.getFileInfos() != null) 
	fileInfos.putAll(rsp.getFileInfos());
      return rsp.getUpdatedCheckSums(); 
    }
    else {
      handleFailure(obj);
      return null;
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
   * @param workingCheckSums
   *   Current cache of checksums for files associated with the working version.
   * 
   * @param movedStamps
   *   A table into which the timestamps are recorded for files before being moved into the
   *   repository and the symlink created after the move.
   * 
   * @param fileInfos
   *   An table into which per-file information is recorded for each file sequence after 
   *   the check-in takes place.
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
   TreeMap<String,Long[]> movedStamps, 
   TreeMap<FileSeq,NativeFileInfo[]> fileInfos
  ) 
    throws PipelineException 
  {
    FileCheckInReq req = 
      new FileCheckInReq(id, vid, latest, mod.isIntermediate(), mod.isActionEnabled(), 
                         mod.getSequences(), isNovel, mod.getLastCTimeUpdate(), 
                         workingCheckSums); 

    Object obj = pFileMgr.checkIn(req);
    if(obj instanceof FileCheckInRsp) {
      FileCheckInRsp rsp = (FileCheckInRsp) obj;
      movedStamps.putAll(rsp.getMovedStamps());
      fileInfos.putAll(rsp.getFileInfos());
      return rsp.getUpdatedCheckSums(); 
    }
    else {
      handleFailure(obj);
      return null;
    }
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
    throws PipelineException 
  {
    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), 
			  isLinked, !vsn.isActionEnabled(), false);

    Object obj = pFileMgr.checkOut(req); 
    handleSimpleResponse(obj);
  }

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
    throws PipelineException
  {
    FileCheckOutReq req = 
      new FileCheckOutReq(id, vsn.getVersionID(), vsn.getSequences(), false, true, true);
    
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
    throws PipelineException
  {
    FileRevertReq req = new FileRevertReq(id, files, isLinked);
    
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
    FileChangeModeReq req = new FileChangeModeReq(id, mod.getSequences(), writeable);

    Object obj = pFileMgr.changeMode(req);
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
    FileTouchAllReq req = new FileTouchAllReq(id, mod.getSequences());

    Object obj = pFileMgr.touchAll(req);
    handleSimpleResponse(obj);
  }

  /**
   * Get the newest of the last modification and last change time stamps for all of the 
   * given files associated with the given working version.
   * 
   * @param id 
   *   The unique working version identifier.
   * 
   * @param fnames
   *   The working primary/secondary file names.
   * 
   * @return
   *   The timestamps for each file or <CODE>null</CODE> if a file is missing.
   */ 
  public ArrayList<Long>
  getWorkingTimeStamps
  (
   NodeID id, 
   ArrayList<String> fnames
  ) 
    throws PipelineException
  {
    FileGetWorkingTimeStampsReq req = new FileGetWorkingTimeStampsReq(id, fnames);

    Object obj = pFileMgr.getWorkingTimeStamps(req);
    if(obj instanceof FileGetWorkingTimeStampsRsp) {
      FileGetWorkingTimeStampsRsp rsp = (FileGetWorkingTimeStampsRsp) obj;
      return rsp.getTimeStamps();
    }
    else {
      handleFailure(obj);
      return null;
    }
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
    FilePackNodesReq req = new FilePackNodesReq(bundle);

    Object obj = pFileMgr.packNodes(req, pOpNotifier);
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
    FileExtractBundleReq req = new FileExtractBundleReq(bundlePath);

    Object obj = pFileMgr.extractBundle(req);
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
    FileUnpackNodesReq req = 
      new FileUnpackNodesReq(bundlePath, bundle, author, view, skipUnpack);
    
    Object obj = pFileMgr.unpackNodes(req, pOpNotifier);
    handleSimpleResponse(obj);    
  }


  /*----------------------------------------------------------------------------------------*/
   
  /**
   * Creates a TAR archive containing both files and metadata associated with a checked-in
   * version of a node suitable for transfer to a remote site.<P> 
   * 
   * The TAR archive will contain a copy of the original NodeVersion which has been altered 
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
   *   information about when the TAR archive was created and by whom. 
   * </DIV><P> 
   *   
   * Each file associated with the target node will also be copied and included in the TAR 
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
   * node files, a "README" text file will also be added to the TAR archive which details 
   * the contents and all changes made to the node version being extracted.<P> 
   * 
   * If successfull, the TAR archive file will be written to "tarPath".<P>
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
   *   TAR archive.
   * 
   * @param stamp
   *   The timestamp of when this node was extracted.
   * 
   * @param creator
   *   The name of the user who extracted the node.
   * 
   * @param tarPath
   *   The name of the TAR archive to create.
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
   Path tarPath
  )
    throws PipelineException
  {
    FileExtractSiteVersionReq req = 
      new FileExtractSiteVersionReq(name, referenceNames, localSiteName, 
                                    replaceSeqs, replacements, 
                                    vsn, stamp, creator, tarPath);
    
    Object obj = pFileMgr.extractSiteVersion(req); 
    handleSimpleResponse(obj);    
  }

  /**
   * Lookup the NodeVersion contained within the extracted site version TAR archive.
   * 
   * @param tarPath
   *   The name of the TAR archive to read.
   */ 
  public NodeVersion
  lookupSiteVersion
  ( 
   Path tarPath
  ) 
    throws PipelineException
  {
    FileSiteVersionReq req = new FileSiteVersionReq(tarPath);
    
    Object obj = pFileMgr.lookupSiteVersion(req); 
    if(obj instanceof FileLookupSiteVersionRsp) {
      FileLookupSiteVersionRsp rsp = (FileLookupSiteVersionRsp) obj;
      return rsp.getNodeVersion();
    }
    else {
      handleFailure(obj);
      return null;
    }
  }

  /**
   * Extract the node files in a extracted site version TAR archive and insert them into the 
   * repository.
   * 
   * @param tarPath
   *   The name of the TAR archive to read.
   */ 
  public void
  insertSiteVersion
  ( 
   Path tarPath
  ) 
    throws PipelineException
  {
    FileSiteVersionReq req = new FileSiteVersionReq(tarPath);
    
    Object obj = pFileMgr.insertSiteVersion(req); 
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
   * @param name
   *   The fully resolved name of the node.
   * 
   * @param fseqs
   *   The files sequences indexed by checked-in revision numbers.
   * 
   * @return
   *   The total per-version file sizes indexed by revision number.
   */ 
  public TreeMap<VersionID,Long>
  getArchiveSizes
  (
   String name, 
   MappedSet<VersionID,FileSeq> vfseqs
  ) 
    throws PipelineException 
  {
    FileGetArchiveSizesReq req = new FileGetArchiveSizesReq(name, vfseqs);

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
    throws PipelineException 
  {
    FileArchiveReq req = 
      new FileArchiveReq(name, fseqs, archiver, env, dryRunResults != null);

    Object obj = pFileMgr.archive(req, pOpNotifier);
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
   * @param name
   *   The fully resolved name of the node.
   * 
   * @param files
   *   The specific files indexed by checked-in revision numbers.
   * 
   * @return
   *   The total version file sizes indexed by revision number.
   */ 
  public TreeMap<VersionID,Long>
  getOfflineSizes
  (
   String name,
   MappedSet<VersionID,File> files
  ) 
    throws PipelineException 
  {
    FileGetOfflineSizesReq req = new FileGetOfflineSizesReq(name, files);

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
    throws PipelineException 
  {
    FileOfflineReq req = new FileOfflineReq(name, vid, symlinks, dryRunResults != null);

    Object obj = pFileMgr.offline(req);
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
    throws PipelineException
  { 
    FileGetOfflinedNodeVersionsReq req = new FileGetOfflinedNodeVersionsReq(name);

    Object obj = pFileMgr.getOfflinedNodeVersions(req);
    if(obj instanceof FileGetOfflinedNodeVersionsRsp) {
      FileGetOfflinedNodeVersionsRsp rsp = (FileGetOfflinedNodeVersionsRsp) obj;
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
    throws PipelineException 
  {
    FileExtractReq req = 
      new FileExtractReq(archiveName, stamp, fseqs, checkSums, archiver, env, size, 
                         dryRunResults != null);

    Object obj = pFileMgr.extract(req, pOpNotifier);
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
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
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
    throws PipelineException 
  {
    FileRestoreReq req = 
      new FileRestoreReq(archiveName, stamp, name, vid, symlinks, targets);

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
   *   The timestamp (milliseconds since midnight, January 1, 1970 UTC) of the start of 
   *   the restore operation.
   */
  public void 
  extractCleanup
  (
   String archiveName, 
   long stamp
  ) 
    throws PipelineException 
  {
    FileExtractCleanupReq req = new FileExtractCleanupReq(archiveName, stamp); 

    Object obj = pFileMgr.extractCleanup(req);
    handleSimpleResponse(obj);    
  }



  /*----------------------------------------------------------------------------------------*/
  /*  O P E R A T I O N   M O N I T O R I N G                                               */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Add a operation progress monitor.
   * 
   * @returns
   *   The unique ID used to remove the monitor.
   */
  public synchronized long 
  addMonitor
  (
   OpMonitorable monitor
  ) 
  {
    return pOpNotifier.addMonitor(monitor);
  }
  
  /**
   * Remove an operation progress monitor.
   * 
   * @param monitorID
   *   The unique ID of the monitor.
   * 
   * @returns
   *   The removed monitor or <CODE>null</CODE> if none exists.
   */
  public synchronized OpMonitorable
  removeMonitor
  (
   long monitorID
  ) 
  {
    return pOpNotifier.removeMonitor(monitorID);
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


  /**
   * The direct operation notifier.
   */
  private DirectOpNotifier  pOpNotifier;
}

