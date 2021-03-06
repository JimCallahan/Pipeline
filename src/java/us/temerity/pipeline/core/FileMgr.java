// $Id: FileMgr.java,v 1.106 2010/01/18 18:21:19 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.message.*;
import us.temerity.pipeline.message.file.*;
import us.temerity.pipeline.message.misc.*;
import us.temerity.pipeline.message.simple.*;

import java.io.*;
import java.util.*;
import java.util.jar.*; 
import java.util.concurrent.locks.*;
import java.util.concurrent.atomic.*;
import java.text.*;
import java.util.regex.*;

import org.apache.commons.compress.archivers.tar.*; 


/*------------------------------------------------------------------------------------------*/
/*   F I L E   M G R                                                                        */
/*------------------------------------------------------------------------------------------*/
 
/**
 * The manager of file system queries and operations. <P> 
 * 
 * This class performs all file system related queries and checksum generation as well as 
 * all file copying, linking and deletion.  Because all file system operations are performed 
 * locally, NFS related performance and latency issues are avoided. <P> 
 * 
 * All methods of this class are thread safe.  Special care is taken to prevent access
 * to files associated with a particular node while they are being modifed by the 
 * <CODE>FileMgr</CODE> instance in another thread. <P> 
 * 
 * Instances of this class should never be created or interacted with directly.  
 * Instead, an instance of the {@link FileMgrServer FileMgrServer} class should be used to 
 * mediate the interaction. This <CODE>FileMgrServer</CODE> class manages access to 
 * an internal instance of <CODE>FileMgr</CODE> class.  Once an instance of the 
 * <CODE>FileMgrServer</CODE> class is running, instances of the 
 * {@link FileMgrClient FileMgrClient} class should then be used to communicate over a 
 * network connection with the <CODE>FileMgrServer</CODE>. <P> 
 * 
 * This class acts together with the {@link MasterMgr MasterMgr} class to provide the 
 * complete I/O support for Pipeline. The files managed by this class live under a 
 * standardized directory structure which has a close coupling with node related 
 * information managed by <CODE>MasterMgr</CODE>.  The following details the layout of 
 * files associated with Pipeline nodes. <P> 
 * 
 * The location of files (or symbolic links) associated with working versions: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>prod-dir</I>/working/<I>author</I>/<I>view</I>/ <BR>
 *   <DIV style="margin-left: 20px;">
 *     <I>fully-resolved-node-path</I>/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>node-name</I> <BR>
 *       ... <BR>
 *     </DIV> 
 *     ... <P>
 *   </DIV> 
 *   
 *   Where (<I>prod-dir</I>) is the root of the production file system hierarchy set by
 *   <B>plconfig</B>(1)</I> or as an agument to the constructor for this class.  The 
 *   (<I>author</I>) is the name of the user owning the working version of the node.  The 
 *   (<I>view</I>) is the name of the particular working area view of which the working 
 *   version is a member.  In practice the environmental variable <CODE>$WORKING</CODE> is 
 *   set to contain the full path to this particular view. <P> 
 * 
 *   The (<I>fully-resolved-node-path</I>) is all but the last component of the fully  
 *   resolved name of the node.  The last component of this name is the prefix of the files 
 *   which make up the primary file sequence of the node.  The (<I>node-name)</I> files are 
 *   one or more files which make up the primary and secondary file sequences associated 
 *   with the node. 
 * </DIV> <P> 
 * 
 * The location of files associated with checked-in versions: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>prod-dir</I>/repository/ <BR>
 *   <DIV style="margin-left: 20px;">
 *     <I>fully-resolved-node-name</I>/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>revision-number</I> <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>node-name</I> <BR>
 *         ... <BR>
 *       </DIV> 
 *       ... <BR>
 *     </DIV> 
 *     ... <P> 
 *   </DIV> 
 * 
 *   The files associated with each checked-in version of the node are grouped under the 
 *   (<I>revision-number</I>) of their respective versions.  The other components have the 
 *   same meaning described above for the working version files.  All checked-in files 
 *   have read-only file access permissions. <P> 
 * 
 *   When a new checked-in version is created, some of its associated files may identical
 *   to the respective files of previously checked-in versions for the node.  If this is the 
 *   case, a symbolic link will be created in place of the usual regular file which points 
 *   to this previously checked-in identical copy of the file.  This saves disk space and I/O 
 *   overhead associated with creating new checked-in versions which are largely similar 
 *   to previous checked-in versions.  These symbolic links will always point to the 
 *   earliest identical version of the file which is itself guaranteed to be a regular file.  
 *   In other words, checked-in files are either regular files or one level symbolic links 
 *   to regular files. 
 * </DIV> <P> 
 */
class FileMgr
  extends BaseMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager.
   * 
   * @param banner
   *   Whether to log a startup banner.
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
  FileMgr
  (
   boolean banner, 
   Path fileStatDir, 
   Path checksumDir
  )
  {
    super(banner); 

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing [FileMgr]...");

    /* init common back-end directories */ 
    if(PackageInfo.sOsType != OsType.Unix)
      throw new IllegalStateException("The OS type must be Unix!");
    pProdDir = PackageInfo.sProdPath.toFile();
    pRepoDir = PackageInfo.sRepoPath.toFile();
    pTempDir = PackageInfo.sTempPath.toFile();

    /* make sure that the scratch directory exists */ 
    try {
      validateScratchDirHelper();
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 ex.getMessage());
      System.exit(1);
    }
    
    /* runtime controls */ 
    pFileStatPath = new AtomicReference<Path>();
    pCheckSumPath = new AtomicReference<Path>();
    setRuntimeControlsHelper(fileStatDir, checksumDir);

    /* init file system locks */ 
    pCheckedInLocks = new TreeMap<String,LoggedLock>();
    pWorkingLocks   = new TreeMap<NodeID,Object>();
    pMakeDirLock    = new Object();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   R U N T I M E   P A R A M E T E R S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the current runtime performance controls.
   * 
   * @return
   *   <CODE>MiscGetMasterControlsRsp</CODE>.
   */ 
  public Object
  getRuntimeControls() 
  {
    TaskTimer timer = new TaskTimer();

    Path fileStatPath = null;
    {
      Path path = pFileStatPath.get();
      if(!path.equals(PackageInfo.sProdPath)) 
        fileStatPath = path;
    }

    Path checksumPath = null;
    {
      Path path = pCheckSumPath.get();
      if(!path.equals(PackageInfo.sProdPath)) 
        checksumPath = path;
    }

    return new MiscGetMasterControlsRsp(timer, new MasterControls(fileStatPath, checksumPath));
  }

  /**
   * Set the current runtime performance controls.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE>.
   */ 
  public synchronized Object
  setRuntimeControls
  (
   MiscSetMasterControlsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();

    MasterControls controls = req.getControls();
    setRuntimeControlsHelper(controls.getFileStatDir(), controls.getCheckSumDir());
    
    return new SuccessRsp(timer);
  }
  
  
  /**
   * Helper for setting the current runtime performance controls.
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
  private void 
  setRuntimeControlsHelper
  (
   Path fileStatDir, 
   Path checksumDir
  ) 
  {
    StringBuilder buf = new StringBuilder();
    buf.append
      ("--- FileMgr Runtime Controls -----------------------------------------------\n");
    {
      Path path = fileStatDir;
      if(path == null) 
        pFileStatPath.set(PackageInfo.sProdPath);
      else 
        pFileStatPath.set(path);

      buf.append("  File Stat Root Directory : " + pFileStatPath.get() + "\n");
    }
    
    {
      Path path = checksumDir;
      if(path == null) 
        pCheckSumPath.set(PackageInfo.sProdPath);
      else 
        pCheckSumPath.set(path);

      buf.append("   Checksum Root Directory : " + pCheckSumPath.get() + "\n");
    }
    
    buf.append
      ("----------------------------------------------------------------------------"); 

    LogMgr.getInstance().log
      (LogMgr.Kind.Ops, LogMgr.Level.Info,
       buf.toString());
  }
   
  



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Make sure that the temporary directory exists.
   */ 
  public Object 
  validateScratchDir()
  {
    TaskTimer timer = new TaskTimer("FileMgr.validateScratchDir()");
    
    timer.acquire();
    synchronized(pMakeDirLock) { 
      timer.resume();	

      try {
        validateScratchDirHelper();
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }	
    }

    return new SuccessRsp(timer);
  }

  /**
   * Make sure that the scratch directory exists.
   */ 
  private void 
  validateScratchDirHelper()
    throws PipelineException
  {
    try {
      pScratchPath = new Path(PackageInfo.sTempPath, "plfilemgr");
      pScratchDir = pScratchPath.toFile();
      if(!pScratchDir.isDirectory())
	if(!pScratchDir.mkdirs()) 
	  throw new IOException
	    ("Unable to create the temporary directory (" + pScratchDir + ")!");

      try {
        File dummy = File.createTempFile("TempTest.", null, pScratchDir);
        if(dummy.exists()) 
          dummy.delete(); 
      }
      catch(IOException ex) {
        throw new IOException
          ("Unable to create files in the temporary directory (" + pScratchDir + ")!");
      }
    }
    catch(Exception ex) {
      LogMgr.getInstance().logAndFlush
	(LogMgr.Kind.Ops, LogMgr.Level.Severe,
	 Exceptions.getFullMessage(ex));

      throw new PipelineException(null, ex, true, true); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new empty working area directory. <P> 
   * 
   * If the working area directory already exists, the operation is successful even though 
   * nothing is actually done.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the given working area directory.
   */ 
  public Object 
  createWorkingArea
  ( 
   FileCreateWorkingAreaReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("FileMgr.createWorkingArea(): " + 
		    req.getAuthor() + "|" + req.getView());
      
    String author = req.getAuthor();
    String view   = req.getView();

    /* create the working area directory */ 
    timer.acquire();
    synchronized(pMakeDirLock) { 
      timer.resume();	

      try {
	File wdir = new File(pProdDir, "working/" + author + "/" + view);
	if(wdir.exists()) {
	  if(!wdir.isDirectory()) 
	    throw new PipelineException
	      ("Somehow there exists a non-directory (" + wdir + 
	       ") in the location of the working directory!");
	  
	  return new SuccessRsp(timer);
	}
	else {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--parents");
	  args.add("--mode=755");
	  args.add(wdir.getPath());
	  
	  Map<String,String> env = System.getenv();

	  SubProcessLight proc = 
	    new SubProcessLight(author, "CreateWorkingArea", "mkdir", 
				args, env, pProdDir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to create the working area directory (" + wdir + "):\n\n" + 
		 proc.getStdErr());
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while creating working area directory (" + wdir + ")!");
	  }
	}
      }
      catch(PipelineException ex) {
        return new FailureRsp(timer, ex.getMessage());
      }	
    }

    return new SuccessRsp(timer);
  }  

  /**
   * Remove an entire working area directory. <P> 
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the given working area directory.
   */ 
  public Object 
  removeWorkingArea
  ( 
   FileRemoveWorkingAreaReq req 
  ) 
  {
    TaskTimer timer = 
      new TaskTimer("FileMgr.removeWorkingArea(): " + 
		    req.getAuthor() + "|" + req.getView());
      
    String author = req.getAuthor();
    String view   = req.getView();

    /* remove the working area directory */ 
    timer.acquire();
    synchronized(pMakeDirLock) { 
      timer.resume();	

      try {
	File wdir = new File(pProdDir, 
			     "working/" + author + ((view != null) ? ("/" + view) : ""));
	if(wdir.exists()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--recursive");
	  args.add("--force");
	  args.add(wdir.getPath());
	  
	  Map<String,String> env = System.getenv();

	  SubProcessLight proc = 
	    new SubProcessLight(author, "RemoveWorkingArea", "rm", 
				args, env, pProdDir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to remove the working area directory (" + wdir + "):\n\n" + 
		 proc.getStdErr());
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while removing the working area directory (" + wdir + ")!");
	  }
	}
      }
      catch(PipelineException ex) {
	  return new FailureRsp(timer, ex.getMessage());
      }	
    }

    return new SuccessRsp(timer);
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * @param req 
   *   The file state request.
   * 
   * @return
   *   <CODE>FileStateRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to compute the file states.
   */ 
  public Object
  states
  (
   FileStateReq req
  )
  {
    TaskTimer timer = new TaskTimer();

    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.acquireReadLock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();

	NodeID nodeID = req.getNodeID(); 
        long ctime = req.getChangeStamp();
        JobState jobStates[] = req.getJobStates(); 
        VersionID lvid = req.getLatestVersionID();
        VersionID bvid = req.getWorkingVersionID();

        boolean linter = req.isLatestIntermediate();
        boolean binter = req.isBaseIntermediate();

        SortedMap<String,CheckSum> lcheck = req.getLatestCheckSums();
        SortedMap<String,CheckSum> bcheck = req.getBaseCheckSums();
        CheckSumCache wcheck = req.getWorkingCheckSums();
        wcheck.resetModified(); 

	TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();
        TreeMap<FileSeq,NativeFileInfo[]> fileInfos = new TreeMap<FileSeq,NativeFileInfo[]>();
	TreeMap<FileSeq, Long[]> fileSizes = new TreeMap<FileSeq, Long[]>();

        Path statPath = new Path(pFileStatPath.get());

        String warning = null;
        if(req.hasEnabledAction()) 
          warning = ("Checksum rebuilt by Status for file which has an enabled Action: ");

	/* if frozen, the possibilities are more limited... */ 
	if(req.isFrozen()) {
	  switch(req.getVersionState()) {
	  case Identical:
            {
              Path wpath = new Path(statPath, nodeID.getWorkingParent());

              for(FileSeq fseq : req.getFileSequences()) {
                FileState fs[] = new FileState[fseq.numFrames()];
                NativeFileInfo infos[] = new NativeFileInfo[fs.length];
                
                int wk = 0;
                for(Path path : fseq.getPaths()) {
                  NativeFileStat work = new NativeFileStat(new Path(wpath, path));

                  if(!work.isFile()) {
                    /* this means that someone has manually removed or broken the symlink! */ 
                    fs[wk] = FileState.Missing; 
                  }
                  else {
                    fs[wk] = FileState.Identical;
                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }
                  
                  wk++;
                }

                states.put(fseq, fs);
                fileInfos.put(fseq, infos);
              }
            }
	    break;

	  case NeedsCheckOut:
            {
              Path wpath = new Path(statPath, nodeID.getWorkingParent());
              Path lpath = new Path(statPath, nodeID.getCheckedInPath(lvid));

              for(FileSeq fseq : req.getFileSequences()) {
                FileState fs[] = new FileState[fseq.numFrames()];
                NativeFileInfo infos[] = new NativeFileInfo[fs.length];
                
                int wk = 0;
                for(Path path : fseq.getPaths()) {
                  NativeFileStat work = new NativeFileStat(new Path(wpath, path));
                  NativeFileStat latest = new NativeFileStat(new Path(lpath, path));
                  
                  String fname = path.toString(); 
                  CheckSum lsum = lcheck.get(fname); 

                  if((linter && (lsum == null)) || (!linter && !latest.isFile())) {
                    fs[wk] = FileState.Obsolete;
                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }
                  else if(!work.isFile()) {
                    /* this means that someone has manually removed or broken the symlink! */ 
                    fs[wk] = FileState.Missing;  
                  }
                  else {
                    if(linter) {
                      wcheck.update(pCheckSumPath.get(), fname, work, ctime, warning); 
                      if(wcheck.isIdentical(fname, lsum)) 
                        fs[wk] = FileState.Identical;
                      else 
                        fs[wk] = FileState.NeedsCheckOut;
                    }
                    else {
                      if(work.isAlias(latest))
                        fs[wk] = FileState.Identical;
                      else 
                        fs[wk] = FileState.NeedsCheckOut;
                    }

                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }

                  wk++;
                }
               
                states.put(fseq, fs);
                fileInfos.put(fseq, infos);
              }
            }
          }
        }
	
	/* if not frozen, do a more exhaustive set of tests... */ 
	else {
	  switch(req.getVersionState()) {
	  case Pending:
            {
              if((bvid != null) || (lvid != null))
                throw new PipelineException("Internal Error");
              
              Path wpath = new Path(statPath, nodeID.getWorkingParent());
              
              for(FileSeq fseq : req.getFileSequences()) {
                FileState fs[] = new FileState[fseq.numFrames()];
                NativeFileInfo infos[] = new NativeFileInfo[fs.length];

                int wk = 0;
                for(Path path : fseq.getPaths()) {
                  NativeFileStat work = new NativeFileStat(new Path(wpath, path));
                  
                  if(work.isFile()) {
                    fs[wk] = FileState.Pending;
                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }
                  else {
                    fs[wk] = FileState.Missing;
                  }
                  
                  wk++;
                }
                
                states.put(fseq, fs);
                fileInfos.put(fseq, infos);
              }
            }
	    break;
	  
	  case CheckedIn:
	    throw new PipelineException("Internal Error");
	  
	  case Identical:
            {
              if((bvid == null) || (lvid == null))
                throw new PipelineException("Internal Error");
              
              Path wpath = new Path(statPath, nodeID.getWorkingParent());
              Path lpath = new Path(statPath, nodeID.getCheckedInPath(lvid));

              for(FileSeq fseq : req.getFileSequences()) {
                FileState fs[] = new FileState[fseq.numFrames()];
                NativeFileInfo infos[] = new NativeFileInfo[fs.length];
                
                int wk = 0;
                for(Path path : fseq.getPaths()) {
                  NativeFileStat work = new NativeFileStat(new Path(wpath, path));

                  if(!work.isFile()) 
                    fs[wk] = FileState.Missing;
                  else {
                    NativeFileStat latest = new NativeFileStat(new Path(lpath, path));

                    String fname = path.toString(); 
                    CheckSum lsum = lcheck.get(fname); 
                    
                    if(linter) {
                      if(lsum == null) 
                        fs[wk] = FileState.Added;
                      else if(jobStates[wk] == JobState.Running) 
                        fs[wk] = FileState.Modified;
                    }
                    else {
                      if(!latest.isFile()) 
                        fs[wk] = FileState.Added;
                      else if((work.fileSize() != latest.fileSize()) || 
                              (jobStates[wk] == JobState.Running))
                        fs[wk] = FileState.Modified;
                      else if(work.isAlias(latest))
                        fs[wk] = FileState.Identical;
                    }

                    if(fs[wk] == null) {
                      wcheck.update(pCheckSumPath.get(), fname, work, ctime, warning); 
                      if(wcheck.isIdentical(fname, lsum)) 
                        fs[wk] = FileState.Identical;
                      else 
                        fs[wk] = FileState.Modified;
                    }

                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }
                  
                  wk++;
                }
                
                states.put(fseq, fs);
                fileInfos.put(fseq, infos);
              }
            }
	    break;
	  
	  case NeedsCheckOut:
            {
              if((bvid == null) || (lvid == null))
                throw new PipelineException("Internal Error");

              Path wpath = new Path(statPath, nodeID.getWorkingParent());
              Path bpath = new Path(statPath, nodeID.getCheckedInPath(bvid));
              Path lpath = new Path(statPath, nodeID.getCheckedInPath(lvid));
              
              for(FileSeq fseq : req.getFileSequences()) {
                FileState fs[] = new FileState[fseq.numFrames()];
                NativeFileInfo infos[] = new NativeFileInfo[fs.length];
                
                int wk = 0;
                for(Path path : fseq.getPaths()) {
                  NativeFileStat work = new NativeFileStat(new Path(wpath, path));

                  if(!work.isFile()) 
                    fs[wk] = FileState.Missing;
                  else {
                    NativeFileStat latest = new NativeFileStat(new Path(lpath, path));
                    NativeFileStat base   = new NativeFileStat(new Path(bpath, path));

                    String fname = path.toString(); 
                    CheckSum lsum = lcheck.get(fname); 
                    CheckSum bsum = bcheck.get(fname); 

                    if((linter && (lsum == null)) || (!linter && !latest.isFile())) {
                      if((binter && (bsum == null)) || (!binter && !base.isFile())) 
                        fs[wk] = FileState.Added;
                      else 
                        fs[wk] = FileState.Obsolete;
                    }
                    else {
                      boolean workRefreshed = false;
                      boolean workEqLatest = false;
                      if(!linter && work.isAlias(latest)) 
                        workEqLatest = true;
                      else if((linter || (work.fileSize() == latest.fileSize())) && 
                              (jobStates[wk] != JobState.Running)) {
                        wcheck.update(pCheckSumPath.get(), fname, work, ctime, warning);
                        workRefreshed = true;
                        workEqLatest = wcheck.isIdentical(fname, lsum);
                      }

                      if(workEqLatest) 
                        fs[wk] = FileState.Identical;
                      else if((binter && (bsum == null)) || (!binter && !base.isFile())) 
                        fs[wk] = FileState.Conflicted;
                      else {
                        if(((linter || binter) && bsum.equals(lsum)) ||
                           ((!linter && !binter) && base.isAlias(latest)))
                          fs[wk] = FileState.Modified;
                        else {
                          boolean workEqBase = false;
                          if(!binter && work.isAlias(base))
                            workEqBase = true;
                          else if((binter || (work.fileSize() == base.fileSize())) && 
                                  (jobStates[wk] != JobState.Running)) {
                            if(!workRefreshed) 
                              wcheck.update(pCheckSumPath.get(), fname, work, ctime, warning); 
                            workEqBase = wcheck.isIdentical(fname, bsum); 
                          }

                          if(workEqBase)
                            fs[wk] = FileState.NeedsCheckOut;
                          else 
                            fs[wk] = FileState.Conflicted;
                        }
                      }
                    }
                      
                    infos[wk] = new NativeFileInfo(work, ctime); 
                  }
                  
                  wk++;
                }
                
                states.put(fseq, fs);
                fileInfos.put(fseq, infos);
              }
            }
          }
	}

        /**
         * You might be wondering why "wcheck" is read from the FileStateReq, locally 
         * modified and then passed directly back to FileStateRsp.  If you look at these 
         * message classes, you'll also notice that its not getting copied but is always 
         * passed by reference.  This is so that the FileMgrDirectClient can pass the 
         * working checksum cache around by reference entirely since the locks protecting 
         * this datastructure inside MasterMgr are always held when this method is called.  
         * The FileMgrNetClient has no choice but to pass by value as it gets serialized 
         * and deserialized, but that is unavoidable.  This approach is generally not 
         * recommended, but since the cache can be large and this is one of the more time 
         * critical methods in Pipeine, I'm intentionally breaking the rules here.
         */
        return new FileStateRsp(timer, nodeID, states, fileInfos, wcheck);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseReadLock();
    }  
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Perform the file system operations needed to create a new checked-in version of the 
   * node in the file repository based on the given working version. <P> 
   * 
   * @param req 
   *   The check-in request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to check-in the files.
   */
  public Object
  checkIn
  (
   FileCheckInReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.checkIn(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.acquireWriteLock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();

        NodeID nodeID = req.getNodeID(); 
        long ctime = req.getChangeStamp();
        
        CheckSumCache wcheck = req.getWorkingCheckSums();
        wcheck.resetModified(); 

        TreeMap<String,Long[]> movedStamps = new TreeMap<String,Long[]>();

        Path statPath = new Path(pFileStatPath.get());

        String warning = null;
        if(req.hasEnabledAction()) 
          warning = ("Checksum rebuilt by Check-In for file which has an enabled Action: ");

	/* create the repository file directory as well any missing subdirectories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
        File rpath = null;
	{
          rpath = nodeID.getCheckedInPath(rvid).toFile();
	  rdir  = new File(pProdDir, rpath.getPath());

	  timer.acquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();

	    if(rdir.exists()) {
	      if(rdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow the repository directory (" + rdir + ") already exists!");
	      else 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + rdir + ") in the location " + 
                   "of the repository directory!");
	    }
	    
	    try {
	      if(!rdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the repository directory (" + rdir + ")!");
	    }
	    catch (SecurityException ex) {
	      throw new PipelineException
		("Unable to create the repository directory (" + rdir + ")!");
	    }
	  }
	}
        
        /* update any out-of-date working checksums */ 
        {
          Path wpath = new Path(statPath, nodeID.getWorkingParent());
          for(FileSeq fseq : req.getFileSequences()) {
            for(Path path : fseq.getPaths()) {
              try {
                NativeFileStat work = new NativeFileStat(new Path(wpath, path));
                String fname = path.toString(); 
                wcheck.update(pCheckSumPath.get(), fname, work, ctime, warning); 
              }
              catch(IOException ex) {
                throw new PipelineException
                  ("Unable to update the checksums for working file (" + path + ") of " + 
                   "node (" + nodeID + ")!", ex); 
              }
            }
          }
        }

        /* skip file processing for intermediate nodes... */ 
        if(!req.isIntermediate()) {

          /* the latest repository directory */ 
          VersionID lvid = req.getLatestVersionID();
          File ldir = null;
          if(lvid != null) {
            ldir = new File(pProdDir, 
                            nodeID.getCheckedInPath(lvid).toString());
            if(!ldir.isDirectory()) {
              throw new PipelineException
                ("Somehow the latest repository directory (" + ldir + ") was missing!");
            }
          }

          /* the base repository directory */ 
          String rbase = rdir.getParent();

          /* the working file directories */ 
          File wdir  = null;
          File wpath = null;
          {
            wpath = nodeID.getWorkingParent().toFile();
            wdir  = new File(pProdDir, wpath.getPath());
          }

          /* determine how to process the files */ 
          ArrayList<File> filesToCopy = new ArrayList<File>();
          ArrayList<File> filesToMove = new ArrayList<File>();
          ArrayList<File> filesToLink = new ArrayList<File>();
          {
            TreeMap<FileSeq,boolean[]> isNovel = req.getIsNovel();
            for(FileSeq fseq : req.getFileSequences()) {
              boolean flags[] = isNovel.get(fseq);
              int wk = 0;
              for(File file : fseq.getFiles()) {
                if(flags[wk]) {
                  File work = new File(wdir, file.getPath());
                  if(!work.isFile())
                    throw new PipelineException
                      ("Somehow the working file (" + work + ") being checked-in does " + 
                       "not exist!"); 

                  /* we can't move/relink hand edited files or symlinks which are novel */ 
                  boolean mustCopy = false;
                  try {
                    mustCopy = (!req.hasEnabledAction() || NativeFileSys.isSymlink(work));
                  }
                  catch(IOException ex) {
                    throw new PipelineException
                      ("Unable to determine whether the working file (" + work + ") being " + 
                       "checked-in is a symbolic link or a regular file!"); 
                  }

                  if(mustCopy) 
                    filesToCopy.add(file);
                  else {
                    filesToMove.add(file);
                  }
                }
                else {
                  if(ldir == null)
                    throw new IllegalStateException(); 
                  File latest = new File(ldir, file.getPath());
                  try {
                    if(NativeFileSys.isSymlink(latest)) 
                      filesToLink.add(NativeFileSys.readlink(latest));
                    else 
                      filesToLink.add(new File("../" + lvid + "/" + file.getPath()));
                  }
                  catch(IOException ex) {
                    throw new PipelineException
                      ("Unable to determine target of the new repository symlink " + 
                       "(" + latest + "):\n  " + ex.getMessage());
                  }
                }

                wk++;
              }
            }
          }
        
          Map<String,String> env = System.getenv();
        
          Path instsbin = 
            new Path(PackageInfo.sInstPath, 
                     PackageInfo.sOsType + "-" + PackageInfo.sArchType + "-Opt"); 

          boolean copyLinkSuccess = false;
          try {
            /* we must copy files which are not procedurally generated and novel */ 
            if(!filesToCopy.isEmpty()) {

              /* copy the files into the repository */ 
              {
                ArrayList<String> preOpts = new ArrayList<String>();
                preOpts.add("--target-directory=" + rdir);
              
                ArrayList<String> args = new ArrayList<String>();
                for(File file : filesToCopy) 
                  args.add(file.getPath());
              
                LinkedList<SubProcessLight> procs = 
                  SubProcessLight.createMultiSubProcess
                  ("CheckIn-Copy", "cp", preOpts, args, env, wdir);
              
                try {	    
                  for(SubProcessLight proc : procs) {
                    proc.start();
                    proc.join();
                    if(!proc.wasSuccessful()) 
                      throw new PipelineException
                        ("Unable to copy files for working version " + 
                         "(" + nodeID + ") into the file repository:\n" +
                         proc.getStdErr());
                  }
                }
                catch(InterruptedException ex) {
                  throw new PipelineException
                    ("Interrupted while copying files for working version " +
                     "(" + nodeID + ") into the file repository!");
                }
              }

              /* verify that the copied files are correct */ 
              for(File file : filesToCopy) {
                File work = new File(wdir, file.getPath());
                File repo = new File(rdir, file.getPath());

                if(!repo.isFile())
                  throw new PipelineException
                    ("The newly created repository file (" + repo + ") was missing!\n\n" + 
                     "PLEASE NOTIFY YOUR SYSTEMS ADMINSTRATOR OF THIS ERROR IMMEDIATELY, " +
                     "SINCE IT IS A SYMPTOM OF A SERIOUS FILE SERVER PROBLEM."); 
              
                /* make sure it was copied completely */ 
                long repoSize = repo.length();
                long workSize = work.length();
                if(repoSize != workSize) 
                  throw new PipelineException
                    ("The newly created repository file (" + repo + ") was NOT the same " + 
                     "size as the working area file (" + work + ") being checked-in!  The " + 
                     "repository file size was (" + repoSize + ") bytes compared to the " + 
                     "working file size of (" + workSize + ") bytes.\n\n" + 
                     "PLEASE NOTIFY YOUR SYSTEMS ADMINSTRATOR OF THIS ERROR IMMEDIATELY, " +
                     "SINCE IT IS A SYMPTOM OF A SERIOUS FILE SERVER PROBLEM."); 
    
                /* make sure the repository file is now read-only */ 
                repo.setReadOnly();
              }
            }

            /* all we need to do for files that are the same as another repository version
               is to just create a symlink between the new and existing repository files */ 
            if(!filesToLink.isEmpty()) {
              for(File source : filesToLink) {
                try {
                  File target = new File(rdir, source.getName());
                  NativeFileSys.symlink(source, target);
                }
                catch(IOException ex) {
                  throw new PipelineException
                    ("Unable to create symbolic links for working version " + 
                     "(" + nodeID + ") in the file repository:\n" +  
                     ex.getMessage());
                }
              }
            }

            copyLinkSuccess = true;
          }
          finally {
            /* cleanup any partial results */ 
            if(!copyLinkSuccess) {
              for(File file : filesToCopy) {
                File rfile = new File(rdir, file.getPath());
                if(rfile.exists()) 
                  rfile.delete();
              }
	    
              for(File link : filesToLink) {
                File rlink = new File(rdir, link.getName());
                if(rlink.exists()) 
                  rlink.delete();
              }

              rdir.delete();
            }
          }
        
          /* move/relink files which are procedurally generated and novel */ 
          if(!filesToMove.isEmpty()) {
            boolean moveSuccess = false; 
            try {
              /* record the pre-move timestamps */ 
              {
                Path swpath = new Path(statPath, nodeID.getWorkingParent());
                for(File file : filesToMove) {
                  String fname = file.getPath();
                  Path bpath = new Path(swpath, fname);  
                  try {      
                    NativeFileStat before = new NativeFileStat(bpath);
                    Long[] both = new Long[2];
                    both[0] = before.lastCriticalChange(ctime); 
                    movedStamps.put(fname, both);
                  }
                  catch(IOException ex2) {
                    throw new PipelineException
                      ("Unable to determine the pre-move timestamp for file " + 
                       "(" + bpath + ") just prior to being moved/link by check-in!");
                  }
                }
              }

              /* rename the working files into repository ones, 
                 the underlying "mv" falls back to copying if the working and repository
                 directories are not on same filesystem */ 
              {
                Path plmv = new Path(instsbin, "/sbin/plmv");
              
                ArrayList<String> preOpts = new ArrayList<String>();
                preOpts.add(rdir.getPath());
              
                ArrayList<String> args = new ArrayList<String>();
                for(File file : filesToMove) 
                  args.add(file.getPath());
              
                LinkedList<SubProcessLight> procs = 
                  SubProcessLight.createMultiSubProcess
                  ("CheckIn-Move", plmv.toOsString(), preOpts, args, env, wdir);
              
                try {	    
                  for(SubProcessLight proc : procs) {
                    proc.start();
                    proc.join();
                    if(!proc.wasSuccessful()) 
                      throw new PipelineException
                        ("Unable to move files for working version " + 
                         "(" + nodeID + ") into the file repository:\n" +
                         proc.getStdErr());
                  }
                }
                catch(InterruptedException ex) {
                  throw new PipelineException
                    ("Interrupted while moving files for working version " +
                     "(" + nodeID + ") into the file repository!");
                }
              }
            
              /* change the ownership of the newly moved files to 
                 the "pipeline" admin user */ 
              { 
                Path plchown = new Path(instsbin, "/sbin/plchown");
              
                ArrayList<String> args = new ArrayList<String>();
                args.add(rdir.getPath()); 
              
                SubProcessLight proc = 
                  new SubProcessLight("CheckIn-Chown", plchown.toOsString(), 
                                      args, env, wdir); 
                try {  
                  proc.start();
                  proc.join();
                  if(!proc.wasSuccessful()) 
                    throw new PipelineException
                      ("Unable to change the ownership of files newly moved to the " +
                       "repository directory (" + rdir + "):\n" +
                       proc.getStdErr());
                }
                catch(InterruptedException ex) {
                  throw new PipelineException
                    ("Interrupted while change the ownership of files newly moved to the " +
                     "repository directory (" + rdir + ")!");
                }	    
              }

              /* verify that the moved or copy/delete operation was successful */ 
              for(File file : filesToMove) {
                File work = new File(wdir, file.getPath());
                File repo = new File(rdir, file.getPath());

                if(!repo.isFile())
                  throw new PipelineException
                    ("The newly created repository file (" + repo + ") was missing!\n\n" + 
                     "PLEASE NOTIFY YOUR SYSTEMS ADMINSTRATOR OF THIS ERROR IMMEDIATELY, " +
                     "SINCE IT IS A SYMPTOM OF A SERIOUS FILE SERVER PROBLEM."); 
               
                if(work.isFile()) 
                  throw new PipelineException
                    ("Somehow the working file (" + work + ") still exists after being " + 
                     "moved to the repository and renamed to (" + repo + ")!\n\n" + 
                     "PLEASE NOTIFY YOUR SYSTEMS ADMINSTRATOR OF THIS ERROR IMMEDIATELY, " +
                     "SINCE IT IS A SYMPTOM OF A SERIOUS FILE SERVER PROBLEM."); 
              } 

              moveSuccess = true;
            }
            finally {
              /* rename the repository directory if there was a failure */ 
              if(!moveSuccess) {
                Path p = new Path(new Path(rdir), 
                                  "-" + System.currentTimeMillis() + ".recover");
                File recover = p.toFile(); 
                if(!rdir.renameTo(recover))
                  LogMgr.getInstance().log
                    (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
                     "Failed to rename newly created repository directory (" + rdir + ") " +
                     "to recovery directory (" + recover + ") in response to an exception " + 
                     "while moving files from the working area!"); 
              }
            }
            
            /* create relative symlinks from the now missing working file names to their 
               new checked-in names */ 
            {
              ArrayList<String> preOpts = new ArrayList<String>();
              preOpts.add("--symbolic-link");
            
              ArrayList<String> args = new ArrayList<String>();
              {
                StringBuilder buf = new StringBuilder();
                String comps[] = wpath.getPath().split("/");
                int wk;
                for(wk=1; wk<comps.length; wk++) 
                  buf.append("../");
                buf.append(rpath.getPath().substring(1));
                String path = buf.toString();
              
                for(File file : filesToMove) 
                  args.add(path + "/" + file);
              }
            
              ArrayList<String> postOpts = new ArrayList<String>();
              postOpts.add(".");
            
              LinkedList<SubProcessLight> procs = 
                SubProcessLight.createMultiSubProcess
                (nodeID.getAuthor(), 
                 "CheckIn-Relink", "cp", preOpts, args, postOpts, env, wdir);
            
              try {
                for(SubProcessLight proc : procs) {
                  proc.start();
                  proc.join();
                  if(!proc.wasSuccessful()) 
                    throw new PipelineException
                      ("Unable to create symbolic links to the repository for the " +
                       "working version (" + nodeID + "):\n\n" + 
                       proc.getStdErr());
                }	
              }
              catch(InterruptedException ex) {
                throw new PipelineException
                  ("Interrupted while creating symbolic links to the repository for the " +
                   "working version (" + nodeID + ")!");
              }
            }

            /* record the post-move timestamps */ 
            {
              Path swpath = new Path(statPath, nodeID.getWorkingParent());
              for(File file : filesToMove) {
                String fname = file.getPath();
                Path apath = new Path(swpath, fname);
                try {      
                  NativeFileStat after = new NativeFileStat(apath);
                  Long[] both = movedStamps.get(fname); 
                  both[1] = after.lastCriticalChange(ctime); 

                  /* replace the updated-on timestamp for checksum since the symlink is now 
                     newer than the original file and would cause the checksum to be
                     recomputed otherwise at the next status/check-in */ 
                  wcheck.replaceUpdatedOn(fname, both[1]);
                }
                catch(IOException ex2) {
                  throw new PipelineException
                    ("Unable to determine the post-move timestamp for symlink " + 
                     "(" + apath + ") just after to being moved/linked by check-in!");
                }
              }
            }
          }
        } // if(!req.isIntermediate())

        /* make the repository directory read-only */ 
        rdir.setReadOnly();

        /* record the post check-in file information,
             identical to what FileMgr.states() returns */ 
        TreeMap<FileSeq,NativeFileInfo[]> fileInfos = new TreeMap<FileSeq,NativeFileInfo[]>();
        {
          Path wpath = new Path(statPath, nodeID.getWorkingParent());
          for(FileSeq fseq : req.getFileSequences()) {  
            NativeFileInfo infos[] = new NativeFileInfo[fseq.numFrames()];

            int wk;
            for(wk=0; wk<infos.length; wk++) {
              Path path = fseq.getPath(wk);
              try {
                NativeFileStat work = new NativeFileStat(new Path(wpath, path));
                infos[wk] = new NativeFileInfo(work, ctime); 
              }
              catch(IOException ex) {
                /* silently ignore */ 
              }
            }

            fileInfos.put(fseq, infos);
          }
        }

        /**
         * You might be wondering why "wcheck" is read from the FileCheckInReq, locally 
         * modified and then passed directly back to FileCheckInRsp.  If you look at these 
         * message classes, you'll also notice that its not getting copied but is always 
         * passed by reference.  This is so that the FileMgrDirectClient can pass the 
         * working checksum cache around by reference entirely since the locks protecting 
         * this datastructure inside MasterMgr are always held when this method is called.  
         * The FileMgrNetClient has no choice but to pass by value as it gets serialized 
         * and deserialized, but that is unavoidable.  This approach is generally not 
         * recommended, but since the cache can be large and this is one of the more time 
         * critical methods in Pipeine, I'm intentionally breaking the rules here.
         */
	return new FileCheckInRsp(timer, wcheck, movedStamps, fileInfos); 
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseWriteLock();
    }  
  }
   


  /*----------------------------------------------------------------------------------------*/

  /**
   * Overwrite the files associated with the working version of the node with a copy of
   * the files associated with the given checked-in version. 
   * 
   * @param req 
   *   The check-out request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to check-out the files.
   */
  public Object
  checkOut
  (
   FileCheckOutReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.checkOut(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.acquireReadLock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	

	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file directories */ 
	File wdir  = null;
	File wpath = null;
	{
	  wpath = req.getNodeID().getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());

	  timer.acquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();	

	    ArrayList<File> dirs = new ArrayList<File>();
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + ") in the location " + 
                   "of the working directory!");
	    }
	    else {
	      dirs.add(wdir);
	    }
	    
	    if(!dirs.isEmpty()) {
	      ArrayList<String> preOpts = new ArrayList<String>();
	      preOpts.add("--parents");
	      preOpts.add("--mode=755");

	      ArrayList<String> args = new ArrayList<String>();
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      LinkedList<SubProcessLight> procs = 
		SubProcessLight.createMultiSubProcess
		(req.getNodeID().getAuthor(), 
		 "CheckOut-MakeDirs", "mkdir", 
		 preOpts, args, env, pProdDir);

	      try {
		for(SubProcessLight proc : procs) {
		  proc.start();
		  proc.join();
		  if(!proc.wasSuccessful()) 
		    throw new PipelineException
		      ("Unable to create directories for working version " + 
                       "(" + req.getNodeID() + "):\n\n" + 
		       proc.getStdErr());	
		}
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version " + 
                   "(" + req.getNodeID() + ")!");
	      }
	    }
	  }
	}

        /* the repository file directories */ 
        VersionID rvid = req.getVersionID();
        File rdir  = null;
        File rpath = null;
        {
          rpath = req.getNodeID().getCheckedInPath(rvid).toFile();
          rdir  = new File(pProdDir, rpath.getPath());
        }
          
        /* build the list of files to copy */ 
        ArrayList<File> files = new ArrayList<File>();
        {
          /* if ignoring existing, we should only proceed if the working area file is either
             a symlink or is missing altogether leaving regular working area files as-is */ 
          if(req.ignoreExisting()) {
            for(FileSeq fseq : req.getFileSequences()) {
              for(File file : fseq.getFiles()) {
                File wfile = new File(wdir, file.getPath()); 
                try {
                  if(!wfile.isFile() || NativeFileSys.isSymlink(wfile))
                    files.add(file); 
                }
                catch(IOException ex2) {
                  files.add(file); 
                }
              }
            }
          }

          /* check-out everything */ 
          else {
            for(FileSeq fseq : req.getFileSequences()) 
              files.addAll(fseq.getFiles());
          }
        }
	
	/* create relative symlinks from the working files to the checked-in files */ 
	if(req.isLinked()) {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--symbolic-link");
	  preOpts.add("--remove-destination");

	  ArrayList<String> args = new ArrayList<String>();
	  {
	    StringBuilder buf = new StringBuilder();
	    String comps[] = wpath.getPath().split("/");
	    int wk;
	    for(wk=1; wk<comps.length; wk++) 
	      buf.append("../");
	    buf.append(rpath.getPath().substring(1));
	    String path = buf.toString();

	    for(File file : files) 
	      args.add(path + "/" + file);
	  }

	  ArrayList<String> postOpts = new ArrayList<String>();
	  postOpts.add(".");

	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "CheckOut-Symlink", "cp", preOpts, args, postOpts, env, wdir);

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to create symbolic links to the repository for the " +
		   "working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());
	    }	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while creating symbolic links to the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* otherwise, copy the checked-in files to the working directory */ 
	else {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--remove-destination");
	  preOpts.add("--target-directory=" + wdir);

	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : files) 
	    args.add(file.getName());

	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "CheckOut-Copy", "cp", preOpts, args, env, rdir);

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to copy files from the repository for the " +
		   "working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying files from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* if not symlinks, add write permission to the working files */ 
        if(!req.isLinked()) {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("u+w");

	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : files) 
	    args.add(file.getName());

	  if(req.getWritable()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
              (req.getNodeID().getAuthor(), 
               "CheckOut-SetWritable", "chmod", preOpts, args, env, wdir);

	    try {
	      for(SubProcessLight proc : procs) {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to add write access permission to the files for " + 
                     "the working version (" + req.getNodeID() + "):\n\n" + 
		     proc.getStdErr());	
	      }
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the files for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }
	}
        
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseReadLock();
    }  
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * A request to revert specific working area files to an earlier checked-in version of 
   * the files.
   * 
   * @param req 
   *   The revert request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to check-out the files.
   */
  public Object
  revert
  (
   FileRevertReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("FileMgr.revert(): " + req.getNodeID());

    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.acquireReadLock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file directories */ 
	File wdir  = null;
        File wpath = null;
	{
	  wpath = req.getNodeID().getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());

	  timer.acquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();	

	    ArrayList<File> dirs = new ArrayList<File>();
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + ") in the location " +
                   "of the working directory!");
	    }
	    else {
	      dirs.add(wdir);
	    }

	    if(!dirs.isEmpty()) {
	      ArrayList<String> preOpts = new ArrayList<String>();
	      preOpts.add("--parents");
	      preOpts.add("--mode=755");

	      ArrayList<String> args = new ArrayList<String>();
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      LinkedList<SubProcessLight> procs = 
		SubProcessLight.createMultiSubProcess
		  (req.getNodeID().getAuthor(), 
		   "Revert-MakeDirs", "mkdir", preOpts, args, env, pProdDir);

	      try {
		for(SubProcessLight proc : procs) {
		  proc.start();
		  proc.join();
		  if(!proc.wasSuccessful()) 
		    throw new PipelineException
		      ("Unable to create directories for working version (" + 
		     req.getNodeID() + "):\n\n" + 
		       proc.getStdErr());	
		}
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version (" + 
		   req.getNodeID() + ")!");
	      }
	    }
	  }
	}
	
	ArrayList<String> rfiles = new ArrayList<String>();
	for(String file : req.getFiles().keySet()) 
	  rfiles.add(req.getFiles().get(file) + "/" + file);

	/* create relative symlinks from the working files to the checked-in files */ 
	if(req.isLinked()) {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--symbolic-link");
	  preOpts.add("--remove-destination");

	  ArrayList<String> args = new ArrayList<String>();
          {
	    StringBuilder buf = new StringBuilder();
	    String comps[] = wpath.getPath().split("/");
	    int wk;
	    for(wk=1; wk<comps.length; wk++) 
	      buf.append("../");
	    String path = buf.toString();
            
	    for(String rfile : rfiles) 
	      args.add(path + "repository" + req.getNodeID().getName() + "/" + rfile); 
	  }

	  ArrayList<String> postOpts = new ArrayList<String>();
	  postOpts.add(".");

	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "Revert-Symlink", "cp", preOpts, args, postOpts, env, wdir);

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to create symbolic links to the repository for the " +
		   "working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while creating symbolic links to the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
        }

	/* copy the checked-in files to the working directory */ 
        else {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--remove-destination");
	  preOpts.add("--target-directory=" + wdir);

	  ArrayList<String> args = new ArrayList<String>();
	  args.addAll(rfiles);

	  File rdir = new File(pProdDir, "repository" + req.getNodeID().getName());
	    
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "Revert-Copy", "cp", preOpts, args, env, rdir);

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to copy files from the repository for the " +
		   "working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying files from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* add write permission to the to working files */ 
        {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("u+w");

	  ArrayList<String> args = new ArrayList<String>();
	  args.addAll(req.getFiles().keySet());

	  if(!req.isLinked()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
	      (req.getNodeID().getAuthor(), 
	       "Revert-SetWritable", "chmod", preOpts, args, env, wdir);

	    try {
	      for(SubProcessLight proc : procs) {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to add write access permission to the files for " + 
		     "the working version (" + req.getNodeID() + "):\n\n" + 
		     proc.getStdErr());	
	      }
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the files for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }
	}
	
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseReadLock();
    }  
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Replace the primary files associated one node with the primary files of another node. <P>
   * 
   * @param req 
   *   The clon request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to copy the files.
   */
  public Object
  clone
  (
   FileCloneReq req
  ) 
  {
    NodeID sourceID = req.getSourceID();
    NodeID targetID = req.getTargetID();
    TreeMap<File,File> files = req.getFiles();
    boolean writeable = req.getWritable();

    TaskTimer timer = 
      new TaskTimer("FileMgr.clone(): " + sourceID + " to " + targetID);

    timer.acquire();
    try {
      validateScratchDirHelper();

      Object workingLock = getWorkingLock(targetID);
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the source working area file directories */ 
	File owdir  = null;
	{
	  File wpath = sourceID.getWorkingParent().toFile();
	  owdir  = new File(pProdDir, wpath.getPath());
	}
	
	/* verify (or create) the target working area file directories */ 
	File wdir  = null;
	{
	  File wpath = targetID.getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  
	  synchronized(pMakeDirLock) { 
	    File dir = null;
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + 
		   ") in the location of the working directory!");
	    }
	    else {
	      dir = wdir; 
	    }

	    if(dir != null) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      args.add(wdir.getPath());

	      SubProcessLight proc = 
		new SubProcessLight(targetID.getAuthor(), 
				    "Clone-MakeDirs", "mkdir", args, env, pProdDir);

	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version " + 
		     "(" + targetID + "):\n\n" + 
		     proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version " + 
		   "(" + targetID + ")!");
	      }
	    }
	  }
	}
	
	/* copy each primary file */ 
	{
	  boolean hasCommands = false;
	  File script = null;
	  try {
	    script = File.createTempFile("Clone-Primary.", ".bash", pScratchDir);
	    FileCleaner.add(script);

	    FileWriter out = new FileWriter(script);

	    for(File ofile : files.keySet()) {
	      File opath = new File(owdir, ofile.getName());
	      if(opath.isFile()) {
		File file = files.get(ofile);
		out.write("if ! cp --force " + opath.getPath() + " " + file.getName() +
			  "; then exit 1; fi\n");
		hasCommands = true;
	      }
	    }

	    out.close();
	  }
	  catch(IOException ex) {
	    throw new PipelineException
	      ("Unable to write temporary script file (" + script + ")!\n" +
	       ex.getMessage());
	  }

	  if(hasCommands) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add(script.getPath());
	    
	    SubProcessLight proc = 
	      new SubProcessLight(targetID.getAuthor(), 
				  "Clone-Primary", "bash", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to clone the primary files for version " + 
		   "(" + targetID + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while cloning the primary files for version " + 
		 "(" + targetID + ")!");
	    }
	  }
	}

	/* set write permission to the to working files */ 
        {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add(writeable ? "u+w" : "u-w");

	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : files.values()) {
	    File path = new File(wdir, file.getName());
	    if(path.isFile()) 
	      args.add(file.getPath()); 
	  }
	  
	  if(!args.isEmpty()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
	      (targetID.getAuthor(), 
	       "Clone-SetWritable", "chmod", preOpts, args, env, wdir);

	    try {
	      for(SubProcessLight proc : procs) {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to set the access permission to the files for " + 
		     "the working version (" + targetID + "):\n\n" + 
		     proc.getStdErr());	
	      }
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while setting the access permission to the files for " + 
	       "the working version (" + targetID + ")!");
	    }
	  }
	}
      
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove specific files associated with the given working version.
   * 
   * @param req 
   *   The remove request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the files.
   */
  public Object
  remove
  (
   FileRemoveReq req
  ) 
  {
    TaskTimer timer = new TaskTimer("FileMgr.remove(): " + req.getNodeID());

    return removeHelper(timer, req.getNodeID(), req.getFiles());
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove all of the files associated with the given working version.
   * 
   * @param req 
   *   The remove request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the files.
   */
  public Object
  removeAll
  (
   FileRemoveAllReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.removeAll(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    ArrayList<File> files = new ArrayList<File>();
    for(FileSeq fseq : req.getFileSequences()) 
      files.addAll(fseq.getFiles());
    
    return removeHelper(timer, req.getNodeID(), files);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Rename the files associated with the given working version.
   * 
   * @param req 
   *   The rename request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to rename the files.
   */
  public Object
  rename
  (
   FileRenameReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.rename(): " + req.getNodeID() + " to " + req.getFilePattern());
      timer = new TaskTimer(buf.toString());
    }

    timer.acquire();
    try {
      validateScratchDirHelper();

      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the new named node identifier */ 
	FilePattern npat = req.getFilePattern();
	NodeID id = new NodeID(req.getNodeID(), npat.getPrefix());

	/* the old working area file directories */ 
	File owdir  = null;
	{
	  File wpath = req.getNodeID().getWorkingParent().toFile();
	  owdir  = new File(pProdDir, wpath.getPath());
	}
	
	/* verify (or create) the new working area file directories */ 
	File wdir  = null;
	{
	  File wpath = id.getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  
	  synchronized(pMakeDirLock) { 
	    File dir = null;
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + ") in the location " + 
		   "of the working directory!");
	    }
	    else {
	      dir = wdir; 
	    }
	    
	    if(dir != null) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      args.add(dir.getPath());
	      
	      SubProcessLight proc = 
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "Rename-MakeDirs", "mkdir", 
				    args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + id + "):\n\n" + 
		     proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version " + 
		   "(" + id + ")!");
	      }
	    }
	  }
	}
	
	/* build the lists of old primary, new primary and secondary file names */ 
	ArrayList<File> opfiles = null;
	ArrayList<File> pfiles  = null;
	ArrayList<File> sfiles  = null;
	{
	  opfiles = new ArrayList<File>();
	  pfiles  = new ArrayList<File>();
	  sfiles  = new ArrayList<File>();

	  {
	    FileSeq fseq = req.getPrimarySequence();

	    opfiles.addAll(fseq.getFiles());
	      
	    File path = new File(npat.getPrefix());
	    FilePattern pat =
	      new FilePattern(path.getName(), npat.getPadding(), npat.getSuffix());
	    FileSeq nfseq = new FileSeq(pat, fseq.getFrameRange());	  
	    pfiles.addAll(nfseq.getFiles());
	  }

	  for(FileSeq fseq : req.getSecondarySequences())
	    sfiles.addAll(fseq.getFiles());
	}
      
	/* move each primary file */ 
	{
	  boolean hasCommands = false;
	  File script = null;
	  try {
	    script = File.createTempFile("Rename-Primary.", ".bash", pScratchDir);
	    FileCleaner.add(script);

	    FileWriter out = new FileWriter(script);

	    Iterator<File> oiter = opfiles.iterator();
	    Iterator<File>  iter = pfiles.iterator();
	    while(oiter.hasNext() && iter.hasNext()) {
	      File ofile = oiter.next();
	      File opath = new File(owdir, ofile.getName());
	      
	      File file = iter.next();
	    
	      if(opath.isFile()) {
		out.write("if ! mv --force " + opath.getPath() + " " + file.getName() +
			  "; then exit 1; fi\n");
		hasCommands = true;
	      }
	    }

	    out.close();
	  }
	  catch(IOException ex) {
	    throw new PipelineException
	      ("Unable to write temporary script file (" + script + ")!\n" +
	       ex.getMessage());
	  }
	      
	  if(hasCommands) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add(script.getPath());
	    
	    SubProcessLight proc = 
	      new SubProcessLight(req.getNodeID().getAuthor(), 
				  "Rename-Primary", "bash", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to rename the primary files for version (" + id + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while renaming a primary file for version (" + id + ")!");
	    }
	  }
	}
	
	/* move all of the secondary files (if the node directory changed) */ 
	if(!owdir.equals(wdir)) {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
	  preOpts.add("--target-directory=" + wdir);
	
	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : sfiles) {
	    File work = new File(owdir, file.getPath());
	    if(work.isFile()) 
	      args.add(file.getName());
	  }
	  
	  if(!args.isEmpty()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
	      (req.getNodeID().getAuthor(), 
	       "Rename-Secondary", "mv", preOpts, args, env, owdir);

	    try {
	      for(SubProcessLight proc : procs) {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to rename the secondary files for version " + 
		     "(" + id + "):\n\n" + 
		     proc.getStdErr());	
	      }
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while renaming the secondary files for version " + 
		 "(" + id + ")!");
	    }
	  }
	}

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the entire repository directory structure for the given node including all
   * files associated with all checked-in versions of a node.
   * 
   * @param req 
   *   The delete request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to delete the files.
   */
  public Object
  deleteCheckedIn
  (
   FileDeleteCheckedInReq req
  ) 
  {
    String name = req.getName();
    TaskTimer timer = new TaskTimer("FileMgr.deleteCheckedIn(): " + name);
    
    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(name);
    checkedInLock.acquireWriteLock();
    try {
      timer.resume();	

      String rdir  = (pProdDir + "/repository" + name);
      
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("--recursive");
	args.add("u+w");
	args.add(rdir);
	
	Map<String,String> env = System.getenv();
	
	SubProcessLight proc = 
	  new SubProcessLight("ChmodCheckedIn", "chmod", args, env, pTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to make the files associated with the checked-in versions of " + 
	       "node (" + name + ") writeable:\n" +
	       proc.getStdErr());
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while making the files associated with the checked-in " + 
	     "versions of node (" + name + ") writeable!");
	}
      }

      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("--recursive");
	args.add("--force");
	args.add(rdir);
	
	Map<String,String> env = System.getenv();
	
	SubProcessLight proc = 
	  new SubProcessLight("DeleteCheckedIn", "rm", args, env, pTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to remove all files associated with the checked-in versions of " + 
	       "node (" + name + ") from the repository:\n" +
	       proc.getStdErr());
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while removing all files associated with the checked-in " + 
	     "versions of node (" + name + ") from the repository!");
	}
      }

      File rparent = (new File(rdir)).getParentFile();  
      deleteEmptyParentDirs(new File(pProdDir + "/repository"), rparent);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseWriteLock();
    }  
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a new node bundle.<P> 
   * 
   * @param req 
   *   The pack nodes request.
   * 
   * @param opn
   *   The operation progress notifier.
   *
   * @return
   *   <CODE>FilePackNodesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the node bundle.
   */
  public Object
  packNodes
  (
   FilePackNodesReq req,
   OpNotifiable opn 
  ) 
  {
    NodeBundle bundle = req.getBundle();
    NodeID rootID = bundle.getRootNodeID();

    TaskTimer timer = new TaskTimer("FileMgr.packNodes(): " + rootID);
    
    try {
      opn.notify(timer, "Packing Nodes...");

      /* determine the name of the node bundle and GLUE file containing the metadata */ 
      String bname = null;
      Path jarPath = null;
      Path gluePath = null;
      Path workPath = null;
      {
        Path path = new Path(rootID.getName());
        SimpleDateFormat fmt = new SimpleDateFormat("yyMMdd-HHmmss"); 
        bname = (path.getName() + "-" + fmt.format(new Date(bundle.getCreatedOn())));

        workPath = new Path(PackageInfo.sProdPath, 
                            "/working/" + rootID.getAuthor() + "/" + rootID.getView());

        jarPath  = new Path(workPath, bname + ".nb"); 
        gluePath = new Path(pScratchPath, bname + ".glue");
      }
      
      /* write the node bundle GLUE file */ 
      {
        File file = gluePath.toFile();
        if(file.exists()) 
          throw new PipelineException
            ("Somehow a node metadata GLUE file (" + gluePath + ") already exists!");
        FileCleaner.add(file);

        LogMgr.getInstance().log
          (LogMgr.Kind.Glu, LogMgr.Level.Finer,
           "Writing Node Bundle Metadata: " + bname);
        
        try {
          GlueEncoderImpl.encodeFile("NodeBundle", bundle, file);
        }
        catch(GlueException ex) {
          throw new PipelineException(ex);
        }
      }

      opn.notify(timer, "Packing Data Files...");

      /* create the node bundle JAR file */ 
      { 
	Map<String,String> jenv = PackageInfo.getJavaEnvironment();

        {
          ArrayList<String> args = new ArrayList<String>();
          args.add("-cvMf");
          args.add(jarPath.toOsString());
          args.add(gluePath.getName());
          
          SubProcessLight proc = 
            new SubProcessLight(rootID.getAuthor(), 
                                "PackNodes", "jar", args, jenv, pScratchDir); 
          
          try {
            proc.start();
            proc.join();
            if(!proc.wasSuccessful()) 
              throw new PipelineException
                ("Unable to create node bundle (" + jarPath + "):\n\n" + 
                 "  " + proc.getStdErr());	
          }
          catch(InterruptedException ex) {
            throw new PipelineException
            ("Interrupted while creating node bundle (" + jarPath + ")!");
          }
        }
        
        {
          ArrayList<String> preOpts = new ArrayList<String>();
          preOpts.add("-uvMf");
          preOpts.add(jarPath.toOsString());

          ArrayList<String> args = new ArrayList<String>();
          for(NodeMod mod : bundle.getWorkingVersions()) {
            if(!mod.isLocked()) {
              Path npath = new Path(mod.getName());
              Path parent = npath.getParentPath();
              for(FileSeq fseq : mod.getSequences()) {
                for(Path path : fseq.getPaths()) {
                  Path fpath = new Path(parent, path); 
                  args.add("." + fpath.toOsString());
                }
              }
            }
          }

	  if(!args.isEmpty()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
                (rootID.getAuthor(), 
                 "PackNodes", "jar", preOpts, args, jenv, workPath.toFile());
          
	    try {
              opn.setTotalSteps(procs.size());

	      for(SubProcessLight proc : procs) {
                proc.start();
                proc.join();
                if(!proc.wasSuccessful()) 
                  throw new PipelineException
                    ("Unable to append files to node bundle (" + jarPath + "):\n\n" + 
                     "  " + proc.getStdErr());

                opn.step(timer, "Packing Data Files...");
              }
            }
            catch(InterruptedException ex) {
              throw new PipelineException
                ("Interrupted while appending files to node bundle (" + jarPath + ")!");
            }
          }
        }
      }

      return new FilePackNodesRsp(timer, rootID, jarPath);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }  


  /*----------------------------------------------------------------------------------------*/

  /**
   * Extract the node metadata from a node bundle containing a tree of nodes packed at 
   * another site. <P> 
   * 
   * @param req 
   *   The extract bundle request.
   * 
   * @return
   *   <CODE>FileExtractBundleRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to create the node bundle.
   */
  public Object
  extractBundle
  (
   FileExtractBundleReq req
  ) 
  {
    Path bundlePath = req.getPath();
    TaskTimer timer = new TaskTimer("FileMgr.extractBundle(): " + bundlePath);
    try {
      return new FileExtractBundleRsp(timer, extractBundleHelper(bundlePath), bundlePath); 
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Extract the node metadata from a node bundle containing a tree of nodes packed at 
   * another site. <P> 
   * 
   * @param bundlePath
   *   The abstract file system path to the node bundle.
   */
  private NodeBundle
  extractBundleHelper
  (
   Path bundlePath
  ) 
    throws PipelineException
  {
    String jarName = bundlePath.getName();
    if(!jarName.endsWith(".nb")) 
      throw new PipelineException
        ("The file supplied (" + bundlePath + ") is not a node bundle (.nb)!");
    String glueName = (jarName.substring(0, jarName.length()-2) + "glue"); 
    
    NodeBundle bundle = null;
    {
      /* extract the raw bytes of the GLUE file */ 
      byte glueBytes[] = null;
      try {
        JarInputStream in = new JarInputStream(new FileInputStream(bundlePath.toFile())); 
      
        while(true) {
          JarEntry entry = in.getNextJarEntry();
          if(entry == null) 
            break;
        
          if(!entry.isDirectory()) {
            if(entry.getName().equals(glueName)) {
              ByteArrayOutputStream bout = new ByteArrayOutputStream();
            
              byte buf[] = new byte[4096];
              while(true) {
                int len = in.read(buf, 0, buf.length); 
                if(len == -1) 
                  break;
                bout.write(buf, 0, len);
              }
            
              glueBytes = bout.toByteArray();
              break;
            }
          }
        }          
      
        in.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to extract the GLUE file (" + glueName + ") from the node bundle " +
           "file (" + bundlePath + "): \n" + ex.getMessage());
      }
    
      if(glueBytes == null) 
        throw new PipelineException
          ("Unable to find the GLUE file (" + glueName + ") in the node bundle " +
           "file (" + bundlePath + ")!");

      PluginMgrClient.getInstance().update();
      try {
        bundle = (NodeBundle) GlueDecoderImpl.decodeBytes("NodeBundle", glueBytes);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }

    return bundle; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Unpack a node bundle files into the given working area.<P> 
   * 
   * @param req 
   *   The extract bundle request.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to unpack the node bundle files.
   */ 
  public Object
  unpackNodes
  ( 
   FileUnpackNodesReq req,
   OpNotifiable opn 
  ) 
  {
    Path jarPath = req.getPath();
    NodeBundle bundle = req.getBundle();
    String author = req.getAuthor();
    String view = req.getView();
    TreeSet<String> skipUnpack = req.getSkipUnpack();

    TaskTimer timer = new TaskTimer("FileMgr.unpackNodes(): " + jarPath);
    
    try {
      String jarName = jarPath.getName();
      if(!jarName.endsWith(".nb")) 
        throw new PipelineException
          ("The file supplied (" + jarPath + ") is not a node bundle (.nb)!");
      String glueName = (jarName.substring(0, jarName.length()-2) + "glue"); 

      Path workPath = new Path(PackageInfo.sProdPath, "/working/" + author + "/" + view);
      int workLen = workPath.toOsString().length();

      Map<String,String> env = System.getenv();
      Map<String,String> jenv = PackageInfo.getJavaEnvironment(); 

      /* generate a list of all files contained in the bundle in depth first order */ 
      ArrayList<Path> bundledPaths = new ArrayList<Path>();
      ArrayList<Path> proceduralPaths = new ArrayList<Path>();
      for(NodeMod mod : bundle.getWorkingVersions()) {
        if(!skipUnpack.contains(mod.getName())) {
          boolean isProcedural = (mod.getAction() != null) && mod.isActionEnabled();
          Path npath = new Path(mod.getName());
          Path parent = new Path(workPath, npath.getParentPath()); 
          for(FileSeq fseq : mod.getSequences()) {
            for(Path path : fseq.getPaths()) {
              Path fpath = new Path(parent, path);
              bundledPaths.add(fpath);
              if(isProcedural) 
                proceduralPaths.add(fpath);
            }
          }
        }
      }

      opn.notify(timer, "Unpacking Data Files..."); 

      /* make any previously existing files associated with nodes with enabled actions
           writable so that there won't be permissions errors when unpacking */ 
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("u+w");
        
        ArrayList<String> args = new ArrayList<String>();
        for(Path path : proceduralPaths) { 
          if(path.toFile().exists()) 
            args.add(path.toOsString());
        }
        
        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
              (author, "Unpack-SetWritable", "chmod", preOpts, args, env, workPath.toFile()); 

          try {
            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to add write access permission to the files being " + 
                   "unpacked from the node bundle (" + jarPath + "):\n\n" + 
                   proc.getStdErr());	
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while adding write access permission to the files being " + 
               "unpacked from the node bundle (" + jarPath + ").");
          }
        }
      }

      /* unpack all of the unskipped files */  
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("-xvf");
        preOpts.add(jarPath.toOsString());
        
        ArrayList<String> args = new ArrayList<String>();
        for(Path path : bundledPaths) {
          String full = path.toOsString();
          args.add(full.substring(workLen+1, full.length()));
        }

        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
              (author, "UnpackNodeFiles", "jar", preOpts, args, 
               jenv, workPath.toFile());

          try {
            opn.setTotalSteps(procs.size());

            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to unpack node bundle (" + jarPath + "):\n\n" + 
                   "  " + proc.getStdErr());	

              opn.step(timer, "Unpacking Data Files...");
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while unpacking node bundle (" + jarPath + ")!");
          }
        }
      }

      opn.step(timer, "Touching Data Files...");

      /* touch the files in depth first order */
      {
        ArrayList<String> preOpts = new ArrayList<String>();

        ArrayList<String> args = new ArrayList<String>();
        for(Path path : bundledPaths) 
          args.add(path.toOsString());

        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
              (author, "TouchUnpackedNodeFiles", "touch", preOpts, args, 
               env, workPath.toFile());

          try {
            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to touch unpacked files from node bundle (" + jarPath + "):\n\n" + 
                   "  " + proc.getStdErr());	
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while touching unpacked files from node bundle " + 
               "(" + jarPath + ")!");
          }
        }
      }

      opn.step(timer, "Setting File Permissions...");

      /* make all unpacked files associated with nodes with enabled actions read-only */ 
      {
        ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("u-w");
        
        ArrayList<String> args = new ArrayList<String>();
        for(Path path : proceduralPaths) { 
          if(path.toFile().exists()) 
            args.add(path.toOsString());
        }
        
        if(!args.isEmpty()) {
          LinkedList<SubProcessLight> procs = 
            SubProcessLight.createMultiSubProcess
              (author, "Unpack-SetReadOnly", "chmod", preOpts, args, env, workPath.toFile()); 

          try {
            for(SubProcessLight proc : procs) {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable make the procedurally generated files being unpacked from the " + 
                   "node bundle (" + jarPath + ") read-only:\n\n" + 
                   proc.getStdErr());	
            }
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while making the procedurally generated files being unpacked " +
               "from the node bundle (" + jarPath + ") read-only."); 
          }
        }
      }

      opn.step(timer, "Cleaning Up...");

      /* delete unpacked node bundle metadata GLUE file */ 
      {
        Path gluePath = new Path(workPath, glueName);

        ArrayList<String> args = new ArrayList<String>();
 	args.add("--force");
        args.add(glueName);

        SubProcessLight proc = 
          new SubProcessLight
            (author, "DeleteBundleMetadata", "rm", args, env, workPath.toFile()); 

        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to delete unpacked node bundle metadate GLUE file " + 
               "(" + gluePath + ")!\n\n" + 
               "  " + proc.getStdErr());	
        }
        catch(InterruptedException ex) {
          throw new PipelineException
            ("Interrupted while unpacking node bundle metadate GLUE file " + 
             "(" + gluePath + ")!");
        }
      }
      
      return new SuccessRsp(timer);      
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Creates a TAR archive containing both files and metadata associated with a checked-in
   * version of a node suitable for transfer to a remote site.<P>
   * 
   * @param req 
   *   The extract site version request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to extract the site version.
   */
  public Object
  extractSiteVersion
  (
   FileExtractSiteVersionReq req
  ) 
  {
    String name = req.getName(); 
    TreeSet<String> referenceNames = req.getReferenceNames();
    String localSiteName = req.getLocalSiteName();
    TreeSet<FileSeq> replaceSeqs = req.getReplaceSeqs();
    TreeMap<String,String> replacements = req.getReplacements();
    NodeVersion vsn = req.getNodeVersion();
    VersionID vid = vsn.getVersionID();
    long stamp = req.getStamp();
    String creator = req.getCreator();
    Path tarPath = req.getTarPath();

    TaskTimer timer = 
      new TaskTimer("FileMgr.extractSiteVersion(): " + name + " v" + vid + 
                    " (" + tarPath + "):");

    /* create a temporary directory for the TAR contents */ 
    Path scratchPath = 
      new Path(pScratchPath, "/site-versions/" + name + "/" + vid + "/" + stamp);
    try {
      File dir = scratchPath.toFile();
      if(!dir.isDirectory())
        if(!dir.mkdirs()) 
          throw new IOException
            ("Unable to create the extract site profile temporary directory (" + dir + ")!");
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }

    try {
      /* write the node version GLUE file */ 
      Path gluePath = new Path(scratchPath, "NodeVersion.glue");
      {
        LogMgr.getInstance().log
          (LogMgr.Kind.Glu, LogMgr.Level.Finer,
           "Writing Node Metadata: " + name + " v" + vid);

        try {
          GlueEncoderImpl.encodeFile("NodeVersion", vsn, gluePath.toFile());
        }
        catch(GlueException ex) {
          throw new PipelineException
            ("Unable to node version metadata file (" + gluePath + ") to be included in " + 
             "the extraced version TAR archive (" + tarPath + ")!\n" +
             ex.getMessage());
        }
      }
      
      /* write README file */ 
      Path readmePath = new Path(scratchPath, "README");
      try {
        FileWriter out = new FileWriter(readmePath.toString());
        out.write
          ("OrigName    : " + name + "\n" + 
           "VersionID   : " + vid + "\n\n" + 
           "ExtractedAt : " + localSiteName + "\n" + 
           "ExtractedOn : " + TimeStamps.format(stamp) + "\n" + 
           "ExtractedBy : " + creator + "\n\n" + 
           "TarName     : " + tarPath.getName() + "\n");
        out.close();
      }
      catch(IOException ex) {
        throw new PipelineException
          ("Unable to write temporary file (" + readmePath + ") to be included in the " + 
           "extraced version TAR archive (" + tarPath + ")!\n" +
           ex.getMessage());
      }

      /* copy and optionally process the primary/secondary files associated with the node */
      TreeSet<String> fileNames = new TreeSet<String>();
      {
        TreeMap<String,String> replaceMap = null; 
        if((replacements != null) && !replacements.isEmpty()) {
          replaceMap = new TreeMap<String,String>();
          for(String key : replacements.keySet()) {
            String value = replacements.get(key);
            if(value != null) 
              replaceMap.put(Pattern.quote(key), Matcher.quoteReplacement(value));
          }
        }

        Path rpath = new Path(PackageInfo.sRepoPath, name + "/" + vid);
        for(FileSeq fseq : vsn.getSequences()) {
          boolean replace = ((replaceMap != null) && 
                             (replaceSeqs != null) && replaceSeqs.contains(fseq));

          for(Path fpath : fseq.getPaths()) {
            Path path = new Path(rpath, fpath);
            Path spath = new Path(scratchPath, fpath);
            
            /* create a copy, performing string replacements on the text file */ 
            if(replace) {
              try {
                BufferedReader in  = new BufferedReader(new FileReader(path.toFile()));
                BufferedWriter out = new BufferedWriter(new FileWriter(spath.toFile()));
                while(true) {
                  String line = in.readLine();
                  if(line == null) 
                    break;
                  
                  for(String key : replaceMap.keySet()) 
                    line = line.replaceAll(key, replaceMap.get(key)); 
                
                  out.write(line + "\n");
                }
                in.close();
                out.close();
              }
              catch(IOException ex) {
                throw new PipelineException
                  ("Unable perform string replacements on original node file " + 
                   "(" + path + ") needed to produce the version of the file " + 
                   "(" + spath + ") to be included in the extraced version TAR archive " + 
                   "(" + tarPath + ")!\n" +
                   ex.getMessage());
              }
            }              

            /* just make a symlink to the original version, 
                 the TAR archive will contain a copy not a link */ 
            else {
              try {
                NativeFileSys.symlink(path.toFile(), spath.toFile());
              }
              catch(IOException ex) {
                throw new PipelineException
                  ("Unable create the symbolic link from (" + spath + ") to " + 
                   "(" + path + ") to be included in the extraced version TAR archive " + 
                   "(" + tarPath + ")!\n" +
                   ex.getMessage());
              }
            }

            fileNames.add(path.getName());
          }
        }
      }

      /* the node TAR archive file */ 
      {    
        Map<String,String> env = System.getenv();
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("--format=posix");
        args.add("--create");
        args.add("--verbose");
        args.add("--dereference");
        args.add("--file=" + tarPath.toOsString());
        args.add("./"); 
        
        SubProcessLight proc = 
          new SubProcessLight
           (creator, "ExtractSiteVersion", "tar", args, env, scratchPath.toFile()); 
        
        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to create the site version archive (" + tarPath + "):\n\n" + 
               "  " + proc.getStdErr());	
        }
        catch(InterruptedException ex) {
          throw new PipelineException
            ("Interrupted while the site version archive (" + tarPath + ")!");
        }
      }

      return new SuccessRsp(timer);      
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      /* clean up temporary files */ 
      try {
        Map<String,String> env = System.getenv();
        
        ArrayList<String> args = new ArrayList<String>();
        args.add("-rf");
        args.add(scratchPath.toOsString());
        
        SubProcessLight proc = 
          new SubProcessLight
           ("DeleteSiteVersionScratch", "rm", args, env, pScratchPath.toFile()); 
        
        try {
          proc.start();
          proc.join();
          if(!proc.wasSuccessful()) 
            throw new PipelineException
              ("Unable to delete the extract site profile temporary directory " + 
               "(" + scratchPath + "):\n" +
               "  " + proc.getStdErr());	
        }
        catch(InterruptedException ex) {
          throw new PipelineException
            ("Interrupted while deleting the extract site profile temporary directory " + 
             "(" + scratchPath + ")!");
        }
      }
      catch(PipelineException ex) {
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Severe, 
           ex.getMessage());
      }
    }
  }
  
  /**
   * Lookup the NodeVersion contained within the extracted site version TAR archive.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>FileLookupSiteVersionRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to lookup the node version.
   */
  public Object
  lookupSiteVersion
  (
    FileSiteVersionReq req
  ) 
  {
    Path tarPath = req.getTarPath();
    
    TaskTimer timer = new TaskTimer("FileMgr.lookupSiteVersion(): " + tarPath);
    try {
      NodeVersion vsn = lookupSiteVersionHelper(tarPath); 
      if(vsn == null)  
        throw new PipelineException
          ("Unable to read the contents of the node version GLUE file in the site version " + 
           "TAR archive (" + tarPath + ")!");

      return new FileLookupSiteVersionRsp(timer, vsn); 
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Lookup the NodeVersion contained within the extracted site version TAR archive.
   * 
   * @param tarPath 
   *   The name of the TAR archive to read.
   * 
   * @returns 
   *   The node version.
   */
  public NodeVersion
  lookupSiteVersionHelper
  (
   Path tarPath
  ) 
    throws PipelineException 
  {
    /* extract the raw bytes of the GLUE file */ 
    byte glueBytes[] = null;
    try {
      TarArchiveInputStream in = 
        new TarArchiveInputStream(new FileInputStream(tarPath.toFile())); 
      
      while(true) {
        TarArchiveEntry entry = in.getNextTarEntry();
        if(entry == null) 
          break;
        
        if(!entry.isDirectory()) {
          if(entry.getName().equals("./NodeVersion.glue")) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            
            byte buf[] = new byte[4096];
            while(true) {
              int len = in.read(buf, 0, buf.length); 
              if(len == -1) 
                break;
                bout.write(buf, 0, len);
            }
            
            glueBytes = bout.toByteArray();
            break;
          }
        }
      }          
      
      in.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to extract the node version GLUE file from the site version TAR archive " +
         "(" + tarPath + "): \n" + ex.getMessage());
    }
    
    if(glueBytes == null) 
      throw new PipelineException
        ("Unable to find node version GLUE file in the site version TAR archive " +
         "(" + tarPath + ")!");
    
    {
      PluginMgrClient.getInstance().update();
      try {
        return (NodeVersion) GlueDecoderImpl.decodeBytes("NodeVersion", glueBytes);
      }	
      catch(GlueException ex) {
        throw new PipelineException(ex);
      }
    }
  }

  /**
   * Extract the node files in a extracted site version TAR archive and insert them into the 
   * repository.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE><SuccessRsp/CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to insert the node files.
   */
  public Object
  insertSiteVersion
  (
    FileSiteVersionReq req
  ) 
  {
    Path tarPath = req.getTarPath();

    TaskTimer timer = new TaskTimer("FileMgr.insertSiteVersion(): " + tarPath);  
    try {
      NodeVersion vsn = lookupSiteVersionHelper(tarPath); 
      if(vsn == null)  
        throw new PipelineException
          ("Unable to read the contents of the node version GLUE file in the site version " + 
           "TAR archive (" + tarPath + ")!");

      String name = vsn.getName();
      VersionID vid = vsn.getVersionID();

      timer.acquire();
      LoggedLock checkedInLock = getCheckedInLock(name);
      checkedInLock.acquireWriteLock();
      try {
	timer.resume();

	/* create the repository file directories as well any missing subdirectories */ 
	Path rdir  = null;
	{
	  Path rpath = new Path("/repository/" + name + "/" + vid); 
	  rdir = new Path(PackageInfo.sProdPath, rpath);

	  timer.acquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();

            File repoDir = rdir.toFile();
	    if(repoDir.exists()) {
	      if(repoDir.isDirectory()) 
		throw new PipelineException
		  ("Somehow the repository directory (" + rdir + 
		   ") already exists!");
	      else 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + rdir + 
		   ") in the location of the repository directory!");
	    }
	    
	    try {
	      if(!repoDir.mkdirs())
		throw new PipelineException
		  ("Unable to create the repository directory (" + rdir + ")!");
	    }
	    catch (SecurityException ex) {
	      throw new PipelineException
		("Unable to create the repository directory (" + rdir + ")!");
	    }
	  }
	}

	boolean success = false;
	try {
          /* insert the files into the repository */ 
          {
            Map<String,String> env = System.getenv();

            ArrayList<String> args = new ArrayList<String>();
            args.add("--exclude=./NodeVersion.glue"); 
            args.add("--exclude=./README");             
            args.add("--extract");
            args.add("--verbose");
            args.add("--file=" + tarPath.toOsString());
            
            SubProcessLight proc = 
              new SubProcessLight("InsertSiteVersion", "tar", args, env, rdir.toFile()); 
            
            try {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to insert files from the site version TAR archive "  + 
                   "(" + tarPath + "):\n\n" + 
                   "  " + proc.getStdErr());	
            }
            catch(InterruptedException ex) {
              throw new PipelineException
                ("Interrupted while inserting files from the site version TAR archive " + 
                 "(" + tarPath + ")!");
            }
          }
          
	  /* make the repository files read-only */ 
          for(FileSeq fseq : vsn.getSequences()) {
            for(Path path : fseq.getPaths()) {
              Path p = new Path(rdir, path);
              File repo = p.toFile();
              
              if(!repo.isFile()) 
                throw new PipelineException
                  ("The newly created repository file (" + repo + ") was missing!\n\n" + 
                   "PLEASE NOTIFY YOUR SYSTEMS ADMINSTRATOR OF THIS ERROR IMMEDIATELY, " +
                   "SINCE IT IS A SYMPTOM OF A SERIOUS FILE SERVER PROBLEM."); 
              
              repo.setReadOnly();
            }
          }

	  success = true;
	}
	finally {
	  /* make the repository directory read-only */ 
	  if(success) 
	    rdir.toFile().setReadOnly();

	  /* cleanup any partial results */ 
	  else { 
            for(FileSeq fseq : vsn.getSequences()) {
              for(Path path : fseq.getPaths()) {
                Path p = new Path(rdir, path);
                File file = p.toFile();
                if(file.exists()) 
                  file.delete();
              }
            }

	    rdir.toFile().delete();
	  }
	}
      }
      finally {
        checkedInLock.releaseWriteLock();
      }  

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Change the user write permission of all existing files associated with the given 
   * working version.
   * 
   * @param req 
   *   The change mode request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the permissions of the files.
   */
  public Object
  changeMode
  (
   FileChangeModeReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.changeMode(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.acquire(); 
    Object workingLock = getWorkingLock(req.getNodeID());
    try {
      synchronized(workingLock) {
	timer.resume();	

	Map<String,String> env = System.getenv();
	File wdir = new File(pProdDir, 
			     req.getNodeID().getWorkingParent().toString());

	ArrayList<String> preOpts = new ArrayList<String>();
	preOpts.add(req.getWritable() ? "u+w" : "u-w");

	ArrayList<String> args = new ArrayList<String>();
	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File path = new File(wdir, file.getPath());
	    if(path.isFile()) 
	      args.add(file.getPath());
	  }
	}
      
	if(!args.isEmpty()) {
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "ChangeMode", "chmod", preOpts, args, env, wdir);
	  
	  try { 
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to change the write access permission of the files for " + 
		   "the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while changing the write access permission of  the files for " + 
	       "the working version (" + req.getNodeID() + ")!");
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the last modification time stamp of all existing files associated with the given 
   * working version.  
   * 
   * @param req 
   *   The change mode request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the time stamps of the files.
   */
  public Object
  touchAll
  (
   FileTouchAllReq req
  ) 
  {
    TaskTimer timer = null;
    {
      StringBuilder buf = new StringBuilder();
      buf.append("FileMgr.touchAll(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.acquire(); 
    Object workingLock = getWorkingLock(req.getNodeID());
    try {
      synchronized(workingLock) {
	timer.resume();	

	Map<String,String> env = System.getenv();
	File wdir = new File(pProdDir, 
			     req.getNodeID().getWorkingParent().toString());

	ArrayList<String> preOpts = new ArrayList<String>();
        preOpts.add("--no-create"); 

	ArrayList<String> args = new ArrayList<String>();
	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File path = new File(wdir, file.getPath());
	    if(path.isFile()) 
	      args.add(file.getPath());
	  }
	}
      
	if(!args.isEmpty()) {
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    (req.getNodeID().getAuthor(), 
	     "TouchAll", "touch", preOpts, args, env, wdir);
	  
	  try { 
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to update the last modification time stamp for all files " + 
		   "associated with the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while update the last modification time stamp for all files " + 
               "associated with the working version (" + req.getNodeID() + ")!");
	  }
	}
      }

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the last modification time stamp of all existing files associated with the given 
   * working version.  
   * 
   * @param req 
   *   The change mode request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to change the time stamps of the files.
   */
  public Object
  getWorkingTimeStamps
  (
   FileGetWorkingTimeStampsReq req
  ) 
  {
    NodeID nodeID = req.getNodeID();

    TaskTimer timer = new TaskTimer("FileMgr.getWorkingTimeStamps(): " + nodeID);

    Path wpath = new Path(pFileStatPath.get(), nodeID.getWorkingParent());
    ArrayList<String> fnames = req.getFileNames(); 
    ArrayList<Long> stamps = new ArrayList<Long>(fnames.size());
    for(String fname : fnames) {
      Long stamp = null;
      try {
        NativeFileStat work = new NativeFileStat(new Path(wpath, fname));
        if(work.isValid()) 
          stamp = work.lastModOrChange();
      }
      catch(IOException ex) {
        /* silently ignore missing files */ 
      }
      
      stamps.add(stamp); 
    }
    
    return new FileGetWorkingTimeStampsRsp(timer, stamps); 
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
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>FileGetSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getArchiveSizes
  (
   FileGetArchiveSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();
    try {
      TreeMap<VersionID,Long> sizes = new TreeMap<VersionID,Long>();

      String name = req.getName();
      MappedSet<VersionID,FileSeq> versions = req.getFileSequences();
	
      for(VersionID vid : versions.keySet()) {
        File dir = new File(pRepoDir, name + "/" + vid);
	  
        long total = 0L;
        for(FileSeq fseq : versions.get(vid)) {
          for(File file : fseq.getFiles()) { 
            File target = new File(dir, file.getPath());
            total += target.length();
          }
        }
	
        sizes.put(vid, total);  
      }

      return new FileGetSizesRsp(timer, sizes);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Create an archive volume by running the given archiver plugin on a set of checked-in 
   * file sequences. 
   * 
   * @param req 
   *   The archive request.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the checked-in version files.
   */
  public Object
  archive
  (
   FileArchiveReq req,
   OpNotifiable opn 
  ) 
  { 
    String archiveName = req.getArchiveName();	
    TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getSequences();
    BaseArchiver archiver = req.getArchiver();
    Map<String,String> env = req.getEnvironment();
    boolean dryrun = req.isDryRun(); 

    TaskTimer timer = new TaskTimer("FileMgr.archive: " + archiveName);

    timer.acquire();
    Stack<LoggedLock> locks = new Stack<LoggedLock>();
    try {
      for(String name : fseqs.keySet()) {
	LoggedLock lock = getCheckedInLock(name);
	lock.acquireReadLock();
	locks.push(lock);
      }
      timer.resume();

      opn.notify(timer, "Running Archiver..."); 

      /* determine the names of the files to archive */ 
      TreeSet<File> files = new TreeSet<File>();
      for(String name : fseqs.keySet()) {
	TreeMap<VersionID,TreeSet<FileSeq>> vsnSeqs = fseqs.get(name);
	for(VersionID vid : vsnSeqs.keySet()) {
	  for(FileSeq fseq : vsnSeqs.get(vid)) {
	    for(File file : fseq.getFiles()) {
	      files.add(new File(name + "/" + vid + "/" + file));	  
	    }
	  }
	}
      }

      /* set the number of files to be processed */ 
      opn.setTotalSteps(files.size());     
      
      /* create temporary directories and files */ 
      File dir = new File(pScratchDir, "archive/" + archiveName);
      File scratch = new File(dir, "scratch");
      File outFile = new File(dir, "stdout");
      File errFile = new File(dir, "stderr"); 
      synchronized(pMakeDirLock) {
	if(!scratch.mkdirs()) 
	  throw new IOException
	    ("Unable to create output directory (" + dir + ") for the archive of " + 
	     "(" + archiveName + ")!");
      }	  

      SubProcessHeavy proc = 
	archiver.archive(archiveName, files, env, pRepoDir, outFile, errFile);

      FileCleaner.add(outFile);
      FileCleaner.add(errFile);

      /* if this is a dry run, just report what would have happened... */ 
      if(dryrun) 
        return new FileArchiverRsp(timer, null, proc.getDryRunInfo()); 

      /* create monitors for the output files */ 
      FileMonitor outMonitor = new FileMonitor(outFile);
      FileMonitor errMonitor = new FileMonitor(errFile);
      try {

        /* run the archiver */ 
        LogMgr.getInstance().log
          (LogMgr.Kind.Ops, LogMgr.Level.Finer,
           "Creating archive volume (" + archiveName + ") using archiver plugin " + 
           "(" + archiver.getName() + ")."); 
        {
          proc.start();
          
          int cycles = 0; 
          while(proc.isAlive()) {
            try {
              proc.join(500);
              cycles++;
            }
            catch(InterruptedException ex) {
              throw new PipelineException(ex);
            }
            
            /* monitor the output so far... (for progress reporting purposes) */ 
            archiver.archiveMonitor(outMonitor, errMonitor, timer, opn);

            LogMgr.getInstance().log
              (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
               "Process for archive volume (" + archiveName + "): " + 
               "WAITING for (" + cycles + ") loops...");
          }
          
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
             "Process for archive volume (" + archiveName + "): " + 
             "COMPLETED after (" + cycles + ") loops...");
        }

        /* monitor the final output */ 
        archiver.archiveMonitor(outMonitor, errMonitor, timer, opn);
      }
      finally {
        outMonitor.close();
        errMonitor.close();
      }

      /* report the contents of the generated STDERR file as the failure message */ 
      if(!proc.wasSuccessful()) {
	String errors = null;
	try {
	  if(errFile.length() > 0) {
	    FileReader in = new FileReader(errFile);
	    
	    StringBuilder buf = new StringBuilder();
	    char[] cs = new char[4096];
	    while(true) {
	      int cnt = in.read(cs);
	      if(cnt == -1) 
		break;
	      
	      buf.append(cs, 0, cnt);
	    }
	    
	    errors = buf.toString();
	  }
	}
	catch(IOException ex) {
	}

        {
          StringBuilder buf = new StringBuilder();
          buf.append
            ("The process creating archive volume (" + archiveName + ") failed with " +
             "exit code (" + proc.getExitCode() + ")!\n\n");
          
          if(errors != null) 
            buf.append("The STDERR output of the process:\n" + 
                       errors);
          else 
            buf.append("There was no STDERR output for the failed process.");
          
          throw new PipelineException(buf.toString());
        }
      }

      /* read the generated STDOUT file */ 
      String output = null;
      try {
	if(outFile.length() > 0) {
	  FileReader in = new FileReader(outFile);

	  StringBuilder buf = new StringBuilder();
	  char[] cs = new char[4096];
	  while(true) {
	    int cnt = in.read(cs);
	    if(cnt == -1) 
	      break;

	    buf.append(cs, 0, cnt);
	  }
	   
	  output = buf.toString();
	}
      }
      catch(IOException ex) {
      }
      
      return new FileArchiverRsp(timer, output, null);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      return new FailureRsp(timer, Exceptions.getFullMessage(ex));
    }
    finally {
      while(!locks.isEmpty()) {
	LoggedLock lock = locks.pop();
	lock.releaseReadLock();
      }
    }
  }


  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Calculate the total size (in bytes) of specific files associated with the given 
   * checked-in versions for offlining purposes. <P> 
   * 
   * Only files which contribute to the offline size should be passed to this method. <P> 
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>FileGetSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getOfflineSizes
  (
   FileGetOfflineSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();
    try {
      TreeMap<VersionID,Long> sizes = new TreeMap<VersionID,Long>();

      String name = req.getName();
      MappedSet<VersionID,File> files = req.getFiles();
	
      for(VersionID vid : files.keySet()) {
        File dir = new File(pRepoDir, name + "/" + vid);
	  
        long total = 0L;
        for(File file : files.get(vid)) {
          File target = new File(dir, file.getPath());
          total += target.length();
        }
	  
        sizes.put(vid, total);  
      }

      return new FileGetSizesRsp(timer, sizes);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Remove the files associated with the given checked-in version of a node.
   * 
   * @param req 
   *   The offline request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the checked-in version files.
   */
  public Object
  offline
  (
   FileOfflineReq req
  ) 
  {
    String name = req.getName();
    VersionID vid = req.getVersionID();
    TreeMap<File,TreeSet<VersionID>> symlinks = req.getSymlinks();
    boolean dryrun = req.isDryRun(); 

    Map<String,String> env = System.getenv();

    TaskTimer timer = new TaskTimer("FileMgr.offline(): " + name + " (" + vid + ")");
    
    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(name);
    checkedInLock.acquireWriteLock();
    try {
      timer.resume();

      StringBuilder dryRunResults = null;
      if(dryrun) 
        dryRunResults = new StringBuilder();

      File nodeDir = new File(pProdDir, "repository" + name);

      /* all versions being modified */ 
      TreeSet<VersionID> avids = new TreeSet<VersionID>();
      {
	for(File file : symlinks.keySet()) 
	  avids.addAll(symlinks.get(file));
	avids.add(vid);
      }

      /* add write permission to the version directories being modified */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("u+w");
	for(VersionID avid : avids) 
	  args.add(avid.toString());

	SubProcessLight proc = 
	  new SubProcessLight("Offline-SetWritable", "chmod", args, env, nodeDir);

        if(dryrun) {
          dryRunResults.append(proc.getDryRunInfo(true, false) + "\n\n"); 
        }
        else {
          try {
            proc.start();
            proc.join();
            if(!proc.wasSuccessful()) 
              throw new PipelineException
                ("Unable to add write access permission to the directories modified " + 
                 "by the offline of the checked-in version (" + vid + ") of node " + 
                 "(" + name + "):\n\n" + 
                 proc.getStdErr());	
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while adding write access permission to the directories " + 
               "modified by the offline of the checked-in version (" + vid + ") of node " + 
               "(" + name + ")");
          }
        }
      }

      /* remove the symlinks in later versions which reference this version */ 
      {
 	ArrayList<String> preOpts = new ArrayList<String>();
 	preOpts.add("--force");
	
 	ArrayList<String> args = new ArrayList<String>();
	for(File file : symlinks.keySet()) {
	  for(VersionID lvid : symlinks.get(file)) 
	    args.add(lvid + "/" + file);
	}

	if(!args.isEmpty()) {
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	    ("Offline-DeleteSymlinks", "rm", preOpts, args, env, nodeDir);
	  
	  try {
	    for(SubProcessLight proc : procs) {
              if(dryrun) {
                dryRunResults.append(proc.getDryRunInfo(true, false) + "\n\n"); 
              }
              else {
                proc.start();
                proc.join();
                if(!proc.wasSuccessful()) 
                  throw new PipelineException
                    ("Unable to remove the stale symlinks referencing the checked-in " + 
                     "version (" + vid + ") of node (" + name + ") from the repository:\n\n" +
                     proc.getStdErr());
              }
            }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while removing the stale symlinks referencing the checked-in " + 
	       "version (" + vid + ") of node (" + name + ") from the repository.");	
	  }
	}
      }

      /* replace the first symlink in a later version with the regular file from 
	 this version and redirect all subsequence symlinks to this new location */ 
      {
	TreeMap<VersionID,TreeSet<File>> moves = new TreeMap<VersionID,TreeSet<File>>();
	for(File file : symlinks.keySet()) {
	  File target = null;
	  for(VersionID lvid : symlinks.get(file)) {
	    if(target == null) {
	      target = new File("../" + lvid + "/" + file);

	      TreeSet<File> targets = moves.get(lvid);
	      if(targets == null) {
		targets = new TreeSet<File>();
		moves.put(lvid, targets);
	      }
	      targets.add(file);
	    }
	    else {
	      File link = new File(nodeDir, lvid + "/" + file);
              if(dryrun) {
                dryRunResults.append("Symlink: " + link + " -> " + target + "\n\n"); 
              }
              else {
                try {
                  NativeFileSys.symlink(target, link);
                }
                catch(IOException ex) {
                  throw new PipelineException
                    ("Unable to redirect the symlink (" + link + ") to target " + 
                     "(" + target + ") during the offlining of the checked-in version " + 
                     "(" + vid + ") of node (" + name + ") from the repository!");
                }
              }
	    }
	  }	
	}

	for(VersionID lvid : moves.keySet()) {
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
	  preOpts.add("--target-directory=" + nodeDir + "/" + lvid);
	  
	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : moves.get(lvid)) 
	    args.add(file.toString());

	  File dir = new File(nodeDir, vid.toString());
	  
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	      ("Offline-MoveFiles", "mv", preOpts, args, env, dir);

	  try {
	    for(SubProcessLight proc : procs) {
              if(dryrun) {
                dryRunResults.append(proc.getDryRunInfo(true, false) + "\n\n"); 
              }
              else {
                proc.start();
                proc.join();
                if(!proc.wasSuccessful()) 
                  throw new PipelineException
                    ("Unable to move the files associated with the checked-in version " + 
                     "(" + vid + ") of node (" + name + ") referenced by later version " +
                     "(" + lvid + ") symlinks:\n\n" + 
                     proc.getStdErr());	
              }
            }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while moving the files associated with the checked-in version " + 
	       "(" + vid + ") of node (" + name + ") referenced by later version " +
	       "(" + lvid + ") symlinks!");
	  }
	}
      }

      /* remove all remaining files and/or symlinks for this version */ 
      {
	File rdir = new File(nodeDir, vid.toString());

 	ArrayList<String> preOpts = new ArrayList<String>();
 	preOpts.add("--force");
	
 	ArrayList<String> args = new ArrayList<String>();
 	{
 	  File files[] = rdir.listFiles(); 
          if(files != null) {
            int wk;
            for(wk=0; wk<files.length; wk++) 
              args.add(files[wk].getName());
          }
        }
	
	LinkedList<SubProcessLight> procs = 
	  SubProcessLight.createMultiSubProcess
	    ("Offline-DeleteVersion", "rm", preOpts, args, env, rdir);

 	try {
	  for(SubProcessLight proc : procs) {
            if(dryrun) {
              dryRunResults.append(proc.getDryRunInfo(true, false) + "\n\n"); 
            }
            else {
              proc.start();
              proc.join();
              if(!proc.wasSuccessful()) 
                throw new PipelineException
                  ("Unable to remove the files associated with the checked-in version " + 
                   "(" + vid + ") of node (" + name + ") from the repository:\n\n" +
                   proc.getStdErr());
            }
          }
	}
 	catch(InterruptedException ex) {
 	  throw new PipelineException
 	    ("Interrupted while offline the files associated with the checked-in version " + 
 	     "(" + vid + ") of node (" + name + ") from the repository!");
 	}
      }
      
      /* make the modified version directories read-only */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("u-w");
	for(VersionID avid : avids) 
	  args.add(avid.toString());

	SubProcessLight proc = 
	  new SubProcessLight("Offline-SetReadOnly", "chmod", args, env, nodeDir);

        if(dryrun) {
          dryRunResults.append(proc.getDryRunInfo(true, false) + "\n\n"); 
        }
        else {
          try {
            proc.start();
            proc.join();
            if(!proc.wasSuccessful()) 
              throw new PipelineException
                ("Unable to make the modified directories read-only after the " + 
                 "offline of the checked-in version (" + vid + ") of node " + 
                 "(" + name + "):\n\n" + 
                 proc.getStdErr());	
          }
          catch(InterruptedException ex) {
            throw new PipelineException
              ("Interrupted while making the modified directories read-only after the " + 
               "offline of the checked-in version (" + vid + ") of node (" + name + ")!");
          }
        }
      }

      if(dryrun) 
        return new DryRunRsp(timer, dryRunResults.toString());

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.releaseWriteLock();
    }  
  }

  /**
   * Get the fully resolved names and revision numbers of all offlined checked-in versions.
   * 
   * @return
   *   <CODE>FileGetOfflinedRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the offlined files.
   */ 
  public Object
  getOfflined() 
  {
    TaskTimer timer = new TaskTimer();
    OfflineProgressTask task = null;
    try {
      TreeMap<String,TreeSet<VersionID>> offlined = new TreeMap<String,TreeSet<VersionID>>();

      int head = (pProdDir + "/repository").length();
      File dir = new File(pProdDir, "repository");
      File files[] = dir.listFiles(); 
      if(files != null) {
        task = new OfflineProgressTask();
        task.start();

	int wk;
	for(wk=0; wk<files.length; wk++) 
          getOfflinedHelper(head, files[wk], offlined, task);
      }

      return new FileGetOfflinedRsp(timer, offlined);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      LogMgr.getInstance().log
        (LogMgr.Kind.Ops, LogMgr.Level.Severe,
         Exceptions.getFullMessage(ex));
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      if(task != null) {
        try {
          task.interrupt();
          task.join();
        }
        catch(InterruptedException ex) {
        }
      }
    }
  }

  /**
   * Recursively scan the repository directories for offlined checked-in versions.
   * 
   * @param head
   *   The number of characters in path before node name starts.
   * 
   * @param dir
   *   The current directory (or file).
   * 
   * @param offlined
   *   The fully resolved names and revision numbers of offlined checked-in versions.
   * 
   * @param task
   *   The thread keeping track of progress being made rebuilding the cache.
   */ 
  private void 
  getOfflinedHelper
  (
   int head, 
   File dir, 
   TreeMap<String,TreeSet<VersionID>> offlined, 
   OfflineProgressTask task
  )
    throws PipelineException
  {
    if(Thread.interrupted()) 
      throw new PipelineException
        ("Interrupted during Offlined Cache Rebuild."); 

    /* ignore any aborted check-in recovery directories */ 
    if(dir.getName().endsWith(".recover")) 
      return;

    task.addTotal();

    File files[] = dir.listFiles(); 
    if(files != null) {
      /* empty directory must be the leaf revision number named directory */ 
      if(files.length == 0) {
        try {
          VersionID vid = new VersionID(dir.getName());

          task.addOffline();
          String name = dir.getParent().substring(head);
          TreeSet<VersionID> vids = offlined.get(name);
          if(vids == null) {
            vids = new TreeSet<VersionID>();
            offlined.put(name, vids);
          }
          
          vids.add(vid);
        }
        catch(IllegalArgumentException ex) {
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Warning, 
             "During Offlined Cache Rebuild, found an empty repository directory " + 
             "(" + dir + ") which should have had a revision number as its last " + 
             "component (" + dir.getName() + ")!  Ignoring it..."); 
        }
      }
      
      /* process any subdirectories */ 
      else {
	int wk;
	for(wk=0; wk<files.length; wk++) {
          if(files[wk].isDirectory()) 
            getOfflinedHelper(head, files[wk], offlined, task);
        }
      }
    }
  }

  /**
   * Get the revision numbers of all offlined checked-in versions of the given node.
   * 
   * @param req
   *   The offlined request.
   * 
   * @return
   *   <CODE>FileGetOfflinedNodeVersionsRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the offlined files.
   */ 
  public Object
  getOfflinedNodeVersions
  (
   FileGetOfflinedNodeVersionsReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();
    try {
      String name = req.getName();

      TreeSet<VersionID> vids = new TreeSet<VersionID>();

      File ndir = new File(pProdDir, "repository" + name);

      /* make sure a repository version exits, might be newly registered... */ 
      if(ndir.exists()) {
        if(!ndir.isDirectory()) 
          throw new PipelineException 
            ("Unable to find any repository directory for the node (" + name + ")!");

        File vdirs[] = ndir.listFiles(); 
        if(vdirs == null) 
          throw new PipelineException 
            ("Unable to find any repository directory for the versions of " + 
             "node (" + name + ")!");
        
        int wk;
        for(wk=0; wk<vdirs.length; wk++) {
          if(!vdirs[wk].isDirectory()) 
            throw new PipelineException 
              ("Found a file in the repository (" + vdirs[wk] + ") where there should " +
               "have been a node version directory for the node (" + name + ")!");
          
          if(!vdirs[wk].getName().endsWith(".recover")) {
            File files[] = vdirs[wk].listFiles(); 
            if((files != null) && (files.length == 0)) 
              vids.add(new VersionID(vdirs[wk].getName()));
          }
        }
      }

      return new FileGetOfflinedNodeVersionsRsp(timer, vids);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }


  /**
   * Extract the files associated with the given checked-in versions from the given archive 
   * volume and place them into a temporary directory.
   * 
   * @param req 
   *   The extract request.
   * 
   * @param opn
   *   The operation progress notifier.
   * 
   * @return
   *   <CODE>FileArchiverRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable extract the files.
   */
  public Object
  extract
  (
   FileExtractReq req,
   OpNotifiable opn 
  ) 
  {
    String archiveName = req.getArchiveName();	
    long stamp = req.getTimeStamp(); 
    BaseArchiver archiver = req.getArchiver();
    Map<String,String> env = req.getEnvironment();    
    TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getSequences();
    TreeMap<String,TreeMap<VersionID,SortedMap<String,CheckSum>>> checkSums = 
      req.getCheckSums();
    boolean dryrun = req.isDryRun(); 
    
    File restoreDir = new File(pProdDir, "restore/" + archiveName + "-" + stamp);

    TaskTimer timer = new TaskTimer("FileMgr.extract: " + archiveName);
    try {
      /* verify that enough disk space exists to perform the restore operation */ 
      {
	Long size = req.getSize() + 134217728L;  
	long freeDisk = NativeFileSys.freeDiskSpace(pProdDir);
	if(size > freeDisk)
	  throw new PipelineException
	    ("There is not enough free disk space (" + formatLong(size) + ") in " +
	     "the production directory (" + pProdDir + ") to restore the " + 
	     "archive volume (" + archiveName + ")!");
      }

      opn.notify(timer, "Running Archiver..."); 

      /* determine the names of the files to extract */ 
      TreeSet<File> files = new TreeSet<File>();
      for(String name : fseqs.keySet()) {
	TreeMap<VersionID,TreeSet<FileSeq>> vsnSeqs = fseqs.get(name);
	for(VersionID vid : vsnSeqs.keySet()) {
	  for(FileSeq fseq : vsnSeqs.get(vid)) {
	    for(File file : fseq.getFiles()) {
	      files.add(new File(name + "/" + vid + "/" + file));	  
	    }
	  }
	}
      }

      /* set the number of files to be processed */ 
      opn.setTotalSteps(files.size());     

      /* create temporary directories and files */ 
      File tmpdir = new File(pScratchDir, "restore/" + archiveName + "-" + stamp);
      File scratch = new File(tmpdir, "scratch");
      File outFile = new File(tmpdir, "stdout");
      File errFile = new File(tmpdir, "stderr"); 
      {
	timer.acquire();
	synchronized(pMakeDirLock) {
	  timer.resume();

	  if(!restoreDir.mkdirs()) 
	    throw new IOException
	      ("Unable to create temporary directory (" + restoreDir + ") for the " + 
	       "restore of (" + archiveName + ")!");
	  
	  if(!scratch.mkdirs()) 
	    throw new IOException
	      ("Unable to create output directory (" + tmpdir + ") for the restore of " + 
	       "(" + archiveName + ")!");
	}	  
      }

      /* run the archiver */ 
      {
	SubProcessHeavy proc = 
	  archiver.restore(archiveName, stamp, files, env, restoreDir, outFile, errFile);
	
	FileCleaner.add(outFile);
	FileCleaner.add(errFile);
	
        /* if this is a dry run, just report what would have happened... */ 
        if(dryrun) 
          return new FileArchiverRsp(timer, null, proc.getDryRunInfo()); 

        /* create monitors for the output files */ 
        FileMonitor outMonitor = new FileMonitor(outFile);
        FileMonitor errMonitor = new FileMonitor(errFile);
        try {

          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Finer,
             "Restoring archive volume (" + archiveName + ") using archiver plugin " + 
             "(" + archiver.getName() + ").");
	
          proc.start();
	
          int cycles = 0; 
          while(proc.isAlive()) {
            try {
              proc.join(500);
              cycles++;
            }
            catch(InterruptedException ex) {
              throw new PipelineException(ex);
            }
            
            /* monitor the output so far... (for progress reporting purposes) */ 
            archiver.restoreMonitor(outMonitor, errMonitor, timer, opn);

            LogMgr.getInstance().log
              (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
               "Process for archive volume (" + archiveName + "): " + 
	       "WAITING for (" + cycles + ") loops...");
          }
          
          LogMgr.getInstance().log
            (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
             "Process for archive volume (" + archiveName + "): " + 
             "COMPLETED after (" + cycles + ") loops...");

          /* monitor the final output */ 
          archiver.restoreMonitor(outMonitor, errMonitor, timer, opn);
        }
        finally {
          outMonitor.close();
          errMonitor.close();
        }
        
	
	/* report the contents of the generated STDERR file as the failure message */ 
	if(!proc.wasSuccessful()) {
	  String errors = null;
	  try {
	    if(errFile.length() > 0) {
	      FileReader in = new FileReader(errFile);
	      
	      StringBuilder buf = new StringBuilder();
	      char[] cs = new char[4096];
	      while(true) {
		int cnt = in.read(cs);
		if(cnt == -1) 
		  break;
		
		buf.append(cs, 0, cnt);
	      }
	      
	      errors = buf.toString();
	    }
	  }
	  catch(IOException ex) {
	  }
	  
	  throw new PipelineException
	    ("The process restoring archive volume (" + archiveName + ") failed with " +
	     "exit code (" + proc.getExitCode() + ")!\n\n" + 
	     "The STDERR output of the process:\n" + 
	     errors);
	}
      }
	
      /* read the generated STDOUT file */ 
      String output = null;
      try {
	if(outFile.length() > 0) {
	  FileReader in = new FileReader(outFile);
	  
	  StringBuilder buf = new StringBuilder();
	  char[] cs = new char[4096];
	  while(true) {
	    int cnt = in.read(cs);
	    if(cnt == -1) 
	      break;
	    
	    buf.append(cs, 0, cnt);
	  }
	  
	  output = buf.toString();
	}
      }
      catch(IOException ex) {
      }
      
      /* verify that all files where restored and that their checksums are correct */ 
      { 
        /* set the number of files to be processed */ 
        opn.setTotalSteps(files.size());     

        for(String name : fseqs.keySet()) {
          TreeMap<VersionID,TreeSet<FileSeq>> vsnSeqs = fseqs.get(name);
          for(VersionID vid : vsnSeqs.keySet()) {
            for(FileSeq fseq : vsnSeqs.get(vid)) {
              for(File file : fseq.getFiles()) {
                CheckSum vsum = null;
                try {
                  vsum = checkSums.get(name).get(vid).get(file.getPath());
                }
                catch(NullPointerException ex) {
                }
                if(vsum == null) 
                  throw new PipelineException
                    ("Somehow there was no checksum for the restored file (" + file + "), " + 
                     "so its validity cannot be determined!"); 
                
                try {
                  File rfile = new File(restoreDir, name + "/" + vid + "/" + file); 
                  CheckSum sum = new CheckSum(new Path(rfile)); 
                  if(!vsum.equals(sum)) 
                    throw new PipelineException
                      ("The restored file (" + file + ") has been corrupted!");
                }
                catch(IOException ex) {
                  throw new PipelineException(ex); 
                } 

                opn.step(timer, "Checked: " + file);
              }
            }
	  }
	}
      }
      
      return new FileArchiverRsp(timer, output, null);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      return new FailureRsp(timer, Exceptions.getFullMessage(ex));
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
   * @param req 
   *   The restore request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the checked-in version files.
   */
  public Object
  restore
  (
   FileRestoreReq req
  ) 
  {
    String archiveName = req.getArchiveName();	
    long stamp = req.getTimeStamp(); 
    String name = req.getName();
    VersionID vid = req.getVersionID();
    TreeMap<File,TreeSet<VersionID>> symlinks = req.getSymlinks();
    TreeMap<File,VersionID> targets = req.getTargets();
    
    File restoreDir = new File(pProdDir, "restore/" + archiveName + "-" + stamp);
    Map<String,String> env = System.getenv();

    TaskTimer timer = new TaskTimer("FileMgr.restore: " + archiveName);

    timer.acquire();
    LoggedLock checkedInLock = getCheckedInLock(name);
    checkedInLock.acquireWriteLock();
    try {
      timer.resume();

      File nodeDir = new File(pProdDir, "repository" + name);

      /* all versions being modified */ 
      TreeSet<VersionID> avids = new TreeSet<VersionID>();
      {
	for(File file : symlinks.keySet()) 
	  avids.addAll(symlinks.get(file));
	avids.add(vid);
      }

      /* add write permission to the respository and extract temporary directories 
	 being modified */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("u+w");
	
	for(VersionID avid : avids) {
	  args.add(nodeDir + "/" + avid);
	  if(avid.equals(vid)) 
	    args.add(restoreDir + name + "/" + avid);
	}

	SubProcessLight proc = 
	  new SubProcessLight("Restore-SetWritable", "chmod", args, env, pTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to add write access permission to the directories modified " + 
	       "by the restore of the checked-in version (" + vid + ") of node " + 
	       "(" + name + "):\n\n" + 
	       proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while adding write access permission to the directories " + 
	     "modified by the restore of the checked-in version (" + vid + ") of node " + 
	     "(" + name + ")");
	}
      }
      

      /* move restored files into the repository */ 
      if(!symlinks.isEmpty()) {
	{
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
	  preOpts.add("--target-directory=" + nodeDir + "/" + vid);
	
	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : symlinks.keySet()) 
	    args.add(file.getPath()); 
	
	  File sdir = new File(restoreDir, name + "/" + vid);
	
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	      ("Restore-MoveFiles", "mv", preOpts, args, env, sdir);

	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to move the restored files associated with the checked-in " + 
		   "version (" + vid + ") of node (" + name + ") into the repository:\n\n" + 
		   proc.getStdErr());	
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while moving the restored files associated with the " + 
	       "checked-in version (" + vid + ") of node (" + name + ") into the " + 
	       "repository!");
	  }
	}

	/* remove the symlinks in later versions which reference this version */ 
	{
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
	  
	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : symlinks.keySet()) {
	    for(VersionID lvid : symlinks.get(file))
	      args.add(lvid + "/" + file);
	  }
	  
	  LinkedList<SubProcessLight> procs = 
	    SubProcessLight.createMultiSubProcess
	      ("Restore-DeleteSymlinks", "rm", preOpts, args, env, nodeDir);
	  
	  try {
	    for(SubProcessLight proc : procs) {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the stale symlinks referencing the checked-in " + 
		   "version (" + vid + ") of node (" + name + ") from the repository:\n\n" +
		   proc.getStdErr());
	    }
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while removing the stale symlinks referencing the " + 
	       "checked-in version (" + vid + ") of node (" + name + ") from the " + 
	       "repository.");	
	  }
	}
	
	/* redirect the symlinks from later versions to point to the restored files */ 
	for(File file : symlinks.keySet()) {
	  File target = new File("../" + vid + "/" + file);
	  for(VersionID svid : symlinks.get(file)) {
	    File link = new File(pProdDir, 
				 "repository" + name + "/" + svid + "/" + file);
	    NativeFileSys.symlink(target, link);
	  }
	}
      }

      /* create symlinks for restored files which are identical to an existing 
	 repository file instead of moving them into the respository */ 
      for(File file : targets.keySet()) {
	VersionID tvid = targets.get(file);
	File target = new File("../" + tvid + "/" + file);
	File link = new File(pProdDir, 
			     "repository" + name + "/" + vid + "/" + file);
	NativeFileSys.symlink(target, link);
      }

      /* make the modified version directories read-only */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("u-w");
	for(VersionID avid : avids) 
	  args.add(avid.toString());

	SubProcessLight proc = 
	  new SubProcessLight("Restore-SetReadOnly", "chmod", args, env, nodeDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to make the modified directories read-only after the " + 
	       "restore of the checked-in version (" + vid + ") of node " + 
	       "(" + name + "):\n\n" + 
	       proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while making the modified directories read-only after the " + 
	     "restore of the checked-in version (" + vid + ") of node (" + name + ")!");
	}
      }	
      
      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      return new FailureRsp(timer, Exceptions.getFullMessage(ex));
    }
    finally {
      checkedInLock.releaseWriteLock();
    }
  }

  /**
   * Remove the temporary directory use to extract the files from an archive volume.
   * 
   * @param req 
   *   The request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable extract remove the directory. 
   */
  public Object
  extractCleanup
  (
   FileExtractCleanupReq req
  ) 
  {
    String archiveName = req.getArchiveName();	
    long stamp = req.getTimeStamp();     
    File dir = new File(pProdDir, "restore/" + archiveName + "-" + stamp);

    TaskTimer timer = new TaskTimer("FileMgr.removeExtractDir: " + archiveName);

    timer.acquire();
    try {
      synchronized(pMakeDirLock) {
	timer.resume();

	ArrayList<String> args = new ArrayList<String>();
	args.add("--force");
	args.add("--recursive");
	args.add(dir.getPath());
	
	Map<String,String> env = System.getenv();
	
	SubProcessLight proc = 
	  new SubProcessLight("Remove-TempDir", "rm", args, env, pTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    return new FailureRsp
	      (timer, 
               "Unable to remove the temporary directory (" + dir + "):\n\n" + 
	       proc.getStdErr());	
	}
	catch(InterruptedException ex) {
	  return new FailureRsp
	    (timer, 
	     "Interrupted while removing the temporary directory (" + dir + ")!");
	}
      }

      return new SuccessRsp(timer);      
    }
    catch(Exception ex) {
      return new FailureRsp(timer, Exceptions.getFullMessage(ex));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the node lock (or create one if none already exists). <P> 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private LoggedLock
  getCheckedInLock
  (
   String name
  ) 
  {
    synchronized(pCheckedInLocks) {
      LoggedLock lock = pCheckedInLocks.get(name);

      if(lock == null) { 
	lock = new LoggedLock("CheckedInFile");
	pCheckedInLocks.put(name, lock);
      }

      return lock;
    }
  }

  /** 
   * Lookup the working version lock (or create one if none already exists). <P> 
   * 
   * @param id 
   *   The unique working version identifier.
   */
  private Object
  getWorkingLock
  (
   NodeID id
  ) 
  {
    synchronized(pWorkingLocks) {
      Object lock = pWorkingLocks.get(id);

      if(lock == null) { 
	lock = new Object();
	pWorkingLocks.put(id, lock);
      }

      return lock;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Remove the given working area files.
   * 
   * @param id
   *   The unique working version identifier.
   * 
   * @param files
   *   The specific files to remove.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the files.
   */
  private Object
  removeHelper
  (
   TaskTimer timer, 
   NodeID id, 
   ArrayList<File> files
  ) 
  {
    timer.acquire();
    try {
      Object workingLock = getWorkingLock(id);
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the working and working checksum directories */ 
	File wdir  = null;
	{
	  File wpath = id.getWorkingParent().toFile();
	  wdir = new File(pProdDir, wpath.getPath());
	}
	
	/* remove the working files */ 
	{
	  ArrayList<String> preOpts = new ArrayList<String>();
	  preOpts.add("--force");
	    
	  ArrayList<String> args = new ArrayList<String>();
	  for(File file : files) {
	    File work = new File(wdir, file.getPath());
	    if(work.isFile()) 
	      args.add(file.getName());
	  }
	  
	  if(!args.isEmpty()) {
	    LinkedList<SubProcessLight> procs = 
	      SubProcessLight.createMultiSubProcess
	      (id.getAuthor(), "Remove-Files", "rm", preOpts, args, env, wdir);
	    
	    try {
	      for(SubProcessLight proc : procs) {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to remove the working files for version (" + 
		     id + "):\n\n" + 
		     proc.getStdErr());	
	      }
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the working files for version (" + 
		 id + ")!");
	    }
	  }
	}
	
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Severe, 
	 ex.getMessage());
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Recursively remove all empty directories at or above the given directory.
   * 
   * @param root
   *   The delete operation should stop at this directory regardles of whether it is empty.
   * 
   * @param dir
   *   The start directory of the delete operation.
   */ 
  private void 
  deleteEmptyParentDirs
  (
   File root, 
   File dir
  ) 
    throws PipelineException
  { 
    synchronized(pMakeDirLock) {
      File tmp = dir;
      while(true) {
	if((tmp == null) || tmp.equals(root) || !tmp.isDirectory())
	  break;
	
	File files[] = tmp.listFiles();
	if((files == null) || (files.length > 0)) 
	  break;
	
	File parent = tmp.getParentFile();

	LogMgr.getInstance().logAndFlush
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Deleting Empty Directory: " + tmp);

	if(!tmp.delete()) 
	  throw new PipelineException
	    ("Unable to delete the empty directory (" + tmp + ")!");

	tmp = parent;
      }
    }
  }
 


  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Generates a formatted string representation of a large integer number.
   */ 
  private String
  formatLong
  (
   Long value
  ) 
  {
    if(value == null) 
      return "-";
    
    if(value < 1024) {
      return value.toString();
    }
    else if(value < 1048576) {
      double k = ((double) value) / 1024.0;
      return String.format("%1$.1fK", k);
    }
    else if(value < 1073741824) {
      double m = ((double) value) / 1048576.0;
      return String.format("%1$.1fM", m);
    }
    else {
      double g = ((double) value) / 1073741824.0;
      return String.format("%1$.1fG", g);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T A S K S                                                                            */
  /*----------------------------------------------------------------------------------------*/
 
  private 
  class OfflineProgressTask
    extends Thread
  {
    /** 
     * Construct a new task.
     */
    public
    OfflineProgressTask()
    {
      super("FileMgr:OfflineProgressTask"); 

      pTotal   = new AtomicLong();
      pOffline = new AtomicLong();
    }

    public void 
    run() 
    {
      TaskTimer timer = new TaskTimer();

      boolean active = true;
      while(active) {
        try {
          Thread.sleep(900000);    // 15-minutes
        }
        catch(InterruptedException ex) {
          active = false;
        }
        
        timer.acquire();
        long millis = timer.getTotalDuration();
        timer.resume();        

        long total   = pTotal.get();
        long offline = pOffline.get();

        long rate = 0L;
        if(millis > 0) 
          rate = total*1000 / millis;

        LogMgr.getInstance().logAndFlush
          (LogMgr.Kind.Net, LogMgr.Level.Info,
           "--- Offlined Task (Active) -----\n" + 
           "      Time Spent: " + TimeStamps.formatInterval(millis) + "\n" +
           "  Dirs Processed: " + total + " (" + rate + " dirs/sec)\n" + 
           "   Offline Found: " + offline + "\n" + 
           "--------------------------------");
      }
    }

    public void
    addTotal() 
    {
      pTotal.getAndIncrement();
    }

    public void
    addOffline() 
    {
      pOffline.getAndIncrement();
    }

    private AtomicLong  pTotal; 
    private AtomicLong  pOffline; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The common back-end directories.
   * 
   * Historically, these where File objects instead of Path objects since the backend only
   * ran on Unix systems.  Eventually they should all be converted to Paths instead.
   */
  private File pProdDir; 
  private File pRepoDir; 
  private File pTempDir; 

  private File pScratchDir; 
  private Path pScratchPath; 

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;


  /*----------------------------------------------------------------------------------------*/
  
  /* RUNTINE CONTROLS */ 
  
  /**
   * An alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for file status query traffic.  Setting this to 
   * <CODE>null</CODE> will cause the default root production directory to be used instead.
   */ 
  private AtomicReference<Path> pFileStatPath;

  /**
   * An alternative root production directory accessed via a different NFS mount point
   * to provide an exclusively network for checksum generation traffic.  Setting this to 
   * <CODE>null</CODE> will cause the default root production directory to be used instead.
   */ 
  private AtomicReference<Path> pCheckSumPath;


  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with all checked-in 
   * versions of each node. The per-node read-lock should be acquired for operations which 
   * will only access these checked-in file resources.  The per-node write-lock should be 
   * acquired when creating new files, symlinks or checksums.
   */
  private TreeMap<String,LoggedLock>  pCheckedInLocks;

  /**
   * The per-working version locks indexed by NodeID. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with the working 
   * versions for each user and view of each node.  These locks should be used in a 
   * <CODE>synchronized()<CODE> statement block wrapping any access or modification of 
   * these file resources.
   */
  private TreeMap<NodeID,Object>  pWorkingLocks;

}

