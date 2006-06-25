// $Id: FileMgr.java,v 1.52 2006/06/25 23:30:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.message.*;
import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

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
 * 
 * The location of checksum files for the working and checked-in versions: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <I>prod-dir</I>/checksum/ <BR> 
 *   <DIV style="margin-left: 20px;">
 *     working/<I>author</I>/<I>view</I>/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-path</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>node-name</I> <BR>
 *         ... <BR>
 *       </DIV> 
 *       ... <P>
 *     </DIV> 
 * 
 *     repository/ <BR>
 *     <DIV style="margin-left: 20px;">
 *       <I>fully-resolved-node-name</I>/ <BR>
 *       <DIV style="margin-left: 20px;">
 *         <I>revision-number</I> <BR>
 *         <DIV style="margin-left: 20px;">
 *           <I>node-name</I> <BR>
 *           ... <BR>
 *         </DIV> 
 *         ... <BR>
 *       </DIV> 
 *       ... <P> 
 *     </DIV> 
 *   </DIV>
 * </DIV>
 * 
 * Each file associated with a checked-in version of a node has a corresponding checksum 
 * file with the same name as the checked-in file but located under the 
 * (<I>prod-dir</I>/checksum) directory.  These checksum files are generated at 
 * the time of check-in if no working checksum file already exists. <P> 
 * 
 * The files associated with working versions of nodes may also have generated checksum 
 * files.  The working checksum files are generated as a post process after successfully 
 * completing a job which regenerates a working file.  They may also be generated for files 
 * associated with leaf nodes during node status computations.  These working leaf node 
 * checksum files are only generated when the working and checked-in versions of a file 
 * are exactly the same size. <P>
 * 
 * Both kinds of checksums are used by Pipeline to optimize the comparison of large data
 * files associated with nodes.  The {@link CheckSum CheckSum} class configured to use 
 * the 128-bit MD5 message digest algorithm generates all checksums. <P> 
 */
class FileMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager.
   */
  public
  FileMgr()
  {
    LogMgr.getInstance().log
      (LogMgr.Kind.Net, LogMgr.Level.Info,
       "Initializing...");
    LogMgr.getInstance().flush();

    assert(PackageInfo.sOsType == OsType.Unix);
    pProdDir = PackageInfo.sProdPath.toFile();
    pRepoDir = PackageInfo.sRepoPath.toFile();
    pTempDir = PackageInfo.sTempPath.toFile();

    pCheckedInLocks = new HashMap<String,ReentrantReadWriteLock>();
    pWorkingLocks   = new HashMap<NodeID,Object>();
    pMakeDirLock    = new Object();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
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
    timer.aquire();
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
    timer.aquire();
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
    assert(req != null);
    TaskTimer timer = new TaskTimer();

    assert(!req.isFrozen());

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.readLock().lock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();

	CheckSum checkSum = new CheckSum("MD5", pProdDir);

	NodeID id = req.getNodeID();
	TreeMap<FileSeq, FileState[]> states = new TreeMap<FileSeq, FileState[]>();

	switch(req.getVersionState()) {
	case Pending:
	  if((req.getWorkingVersionID() != null) || (req.getLatestVersionID() != null))
	    throw new PipelineException("Internal Error");

	  for(FileSeq fseq : req.getFileSequences()) {
	    FileState fs[] = new FileState[fseq.numFrames()];
	    
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      File work = new File(pProdDir, 
				   req.getNodeID().getWorkingParent() + "/" + file);

	      if(work.isFile())
		fs[wk] = FileState.Pending;
	      else 
		fs[wk] = FileState.Missing;
	      
	      wk++;
	    }
	    
	    states.put(fseq, fs);
	  }
	  break;
	  
	case CheckedIn:
	  throw new PipelineException("Internal Error");
	  
	case Identical:
	  if((req.getWorkingVersionID() == null) || (req.getLatestVersionID() == null))
	    throw new PipelineException("Internal Error");

	  for(FileSeq fseq : req.getFileSequences()) {
	    FileState fs[] = new FileState[fseq.numFrames()];
	    
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      File wpath = new File(req.getNodeID().getWorkingParent() + "/" + file);
	      File work  = new File(pProdDir, wpath.getPath());
	      
	      if(!work.isFile()) 
		fs[wk] = FileState.Missing;
	      else {
		VersionID lvid = req.getLatestVersionID();
		File lpath  = new File(req.getNodeID().getCheckedInPath(lvid) + "/" + file);
		File latest = new File(pProdDir, lpath.getPath());
		
		if(!latest.isFile()) 
		  fs[wk] = FileState.Added;
		else if(work.length() != latest.length()) 
		  fs[wk] = FileState.Modified;
		else if(NativeFileSys.realpath(work).equals(NativeFileSys.realpath(latest)))
		  fs[wk] = FileState.Identical;
		else {
		  checkSum.refresh(wpath);
		  if(checkSum.compare(wpath, lpath))
		    fs[wk] = FileState.Identical;
		  else 
		    fs[wk] = FileState.Modified;
		}
	      }
	      
	      wk++;
	    }
	    
	    states.put(fseq, fs);
	  }
	  break;
	  
	case NeedsCheckOut:
	  if((req.getWorkingVersionID() == null) || (req.getLatestVersionID() == null))
	    throw new PipelineException("Internal Error");

	  for(FileSeq fseq : req.getFileSequences()) {
	    FileState fs[] = new FileState[fseq.numFrames()];
	    
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      File wpath = new File(req.getNodeID().getWorkingParent() + "/" + file);
	      File work  = new File(pProdDir, wpath.getPath());

	      if(!work.isFile()) 
		fs[wk] = FileState.Missing;
	      else {
		VersionID lvid = req.getLatestVersionID();
		File lpath  = new File(req.getNodeID().getCheckedInPath(lvid) + "/" + file);
		File latest = new File(pProdDir, lpath.getPath());

		VersionID bvid = req.getWorkingVersionID();
		File bpath = new File(req.getNodeID().getCheckedInPath(bvid) + "/" + file);
		File base  = new File(pProdDir, bpath.getPath());
		
		if(!latest.isFile()) {
		  if(!base.isFile()) 
		    fs[wk] = FileState.Added;
		  else 
		    fs[wk] = FileState.Obsolete;
		}
		else {
		  boolean workRefreshed = false;
		  boolean workEqLatest = false;
		  if(NativeFileSys.realpath(work).equals(NativeFileSys.realpath(latest)))
		    workEqLatest = true;
		  else if(work.length() == latest.length()) {
		    checkSum.refresh(wpath);
		    workRefreshed = true;
		    workEqLatest = checkSum.compare(wpath, lpath);
		  }

		  if(workEqLatest) {
		    fs[wk] = FileState.Identical;
		  }
		  else if(!base.isFile()) {
		    fs[wk] = FileState.Conflicted;
		  }
		  else { 
		    if(NativeFileSys.realpath(base).equals(NativeFileSys.realpath(latest)))
		      fs[wk] = FileState.Modified;
		    else {
		      boolean workEqBase = false;
		      if(NativeFileSys.realpath(work).equals(NativeFileSys.realpath(base)))
			workEqBase = true;
		      else if(work.length() == base.length()) {
			if(!workRefreshed) 
			  checkSum.refresh(wpath);
			workEqBase = checkSum.compare(wpath, bpath);
		      }

		      if(workEqBase)
			fs[wk] = FileState.NeedsCheckOut;
		      else 
			fs[wk] = FileState.Conflicted;
		    }
		  }
		}
	      }

	      wk++;
	    }
	    
	    states.put(fseq, fs);
	  }
	  break;
	}

	/* lookup the last modification timestamps */ 
	TreeMap<FileSeq, Date[]> timestamps = timestamps = new TreeMap<FileSeq, Date[]>();
	{
	  for(FileSeq fseq : states.keySet()) {
	    Date stamps[] = new Date[fseq.numFrames()];

	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      File work = new File(pProdDir, 
				   req.getNodeID().getWorkingParent() + "/" + file);
	      long when = work.lastModified();
	      if(when > 0) 
		stamps[wk] = new Date(when);

	      wk++;
	    }

	    timestamps.put(fseq, stamps);
	  }
	}
	
	return new FileStateRsp(timer, req.getNodeID(), states, timestamps);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(IOException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.readLock().unlock();
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
    assert(req != null);
    TaskTimer timer = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.checkIn(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.writeLock().lock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();

	CheckSum checkSum = new CheckSum("MD5", pProdDir);

	/* refresh the working checksums */ 
	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File work = new File(req.getNodeID().getWorkingParent().toFile(), file.getPath());
	    checkSum.refresh(work);
	  }
	}

	/* create the repository file and checksum directories
	     as well any missing subdirectories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	{
	  File rpath = req.getNodeID().getCheckedInPath(rvid).toFile();
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);

	  timer.aquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();

	    if(rdir.exists()) {
	      if(rdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow the repository directory (" + rdir + 
		   ") already exists!");
	      else 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + rdir + 
		   ") in the location of the repository directory!");
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

	    if(crdir.exists()) {
	      if(crdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow the repository checksum directory (" + crdir + 
		   ") already exists!");
	      else 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + crdir + 
		   ") in the location of the repository checksum directory!");
	    }
	    
	    try {
	      if(!crdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the repository checksum directory (" + crdir + ")!");
	    }
	    catch (SecurityException ex) {
	      throw new PipelineException
		("Unable to create the repository checksum directory (" + crdir + ")!");
	    }
	  }
	}
	  
	/* the latest repository directory */ 
	VersionID lvid = req.getLatestVersionID();
	File ldir = null;
	if(lvid != null) {
	  ldir = new File(pProdDir, 
			  req.getNodeID().getCheckedInPath(lvid).toString());
	  if(!ldir.isDirectory()) {
	    throw new PipelineException
	      ("Somehow the latest repository directory (" + ldir + ") was missing!");
	  }
	}
	
	/* the base repository directory */ 
	String rbase = rdir.getParent();

	/* the working file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	}
	
	/* process the files */ 
	ArrayList<File> files  = new ArrayList<File>();
	ArrayList<File> copies = new ArrayList<File>();
	ArrayList<File> links  = new ArrayList<File>();
	{
	  TreeMap<FileSeq,boolean[]> isNovel = req.getIsNovel();
	  for(FileSeq fseq : req.getFileSequences()) {
	    boolean flags[] = isNovel.get(fseq);
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      files.add(file);

	      File work = new File(wdir, file.getPath());

	      if(flags[wk]) {
		copies.add(file);	
	      }
	      else {
		assert(ldir != null);
		File latest = new File(ldir, file.getPath());
		try {
		  assert(ldir != null);
		  String source = NativeFileSys.realpath(latest).getPath();
		  assert(source.startsWith(rbase));
		  links.add(new File(".." + source.substring(rbase.length())));
		}
		catch(IOException ex) {
		  throw new PipelineException
		    ("Unable to resolve the real path to the repository " + 
		     "file (" + latest + ")!");
		}
	      }

	      wk++;
	    }
	  }
	}

	boolean success = false;
	try {
	  Map<String,String> env = System.getenv();

	  /* copy the files */ 
	  if(!copies.isEmpty()) {	    
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--target-directory=" + rdir);
	    for(File file : copies) 
	      args.add(file.getPath());
	    
	    SubProcessLight proc = 
	      new SubProcessLight("CheckIn-Copy", "cp", args, env, wdir);
	    try {	    
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to copying files for working version (" + req.getNodeID() + 
		   ") into the file repository:\n" +
		   proc.getStdErr());
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while copying files for working version (" + req.getNodeID() + 
		 ") into the file repository!");
	    }
	    
	    for(File file : copies) {
	      File repo = new File(rdir, file.getPath());
	      repo.setReadOnly();
	    }
	  }

	  /* create the symbolic links */ 
	  if(!links.isEmpty()) {
	    for(File source : links) {
	      try {
		File target = new File(rdir, source.getName());
		NativeFileSys.symlink(source, target);
	      }
	      catch(IOException ex) {
		throw new PipelineException
		 ("Unable to create symbolic links for working version (" + req.getNodeID() + 
		 ") in the file repository:\n" +  
		  ex.getMessage());
	      }
	    }
	  }

	  /* copy the checksums */ 
	  {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--target-directory=" + crdir);
	    for(File file : files) 
	      args.add(file.getPath());

	    SubProcessLight proc = 
	      new SubProcessLight("CheckIn-CopyCheckSums", "cp", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to copying checksums for working version (" + 
		   req.getNodeID() + ") into the checksum repository:\n" + 
		   proc.getStdErr());		   
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while copying checksums for working version (" + 
		 req.getNodeID() + ") into the checksum repository!");
	    }

	    for(File file : files) {
	      File repo = new File(crdir, file.getPath());
	      repo.setReadOnly();
	    }
	  }

	  success = true;
	}
	finally {
	  /* make the repository directories read-only */ 
	  if(success) {
	    rdir.setReadOnly();
	    crdir.setReadOnly();
	  }

	  /* cleanup any partial results */ 
	  else {
	    for(File file : copies) {
	      File rfile = new File(rdir, file.getPath());
	      if(rfile.exists()) 
		rfile.delete();
	    }
	    
	    for(File link : links) {
	      File rlink = new File(rdir, link.getName());
	      if(rlink.exists()) 
		rlink.delete();
	    }

	    rdir.delete();
	    
	    for(FileSeq fseq : req.getFileSequences()) {
	      for(File file : fseq.getFiles()) {
		File cfile = new File(crdir, file.getPath());
		if(cfile.exists()) 
		  cfile.delete();		
	      }
	    }

	    crdir.delete();
	  }
	}

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.writeLock().unlock();
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
    assert(req != null);
    TaskTimer timer = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.checkOut(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.readLock().lock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	

	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	File wpath = null;
	{
	  wpath = req.getNodeID().getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);

	  timer.aquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();	

	    ArrayList<File> dirs = new ArrayList<File>();
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + 
		   ") in the location of the working directory!");
	    }
	    else {
	      dirs.add(wdir);
	    }
	    
	    if(cwdir.exists()) {
	      if(!cwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + cwdir + 
		   ") in the location of the working checksum directory!");
	    }
	    else {
	      if(!cwdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the working checksum directory (" + cwdir + ")!");
	    }

	    if(!dirs.isEmpty()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      SubProcessLight proc = 
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "CheckOut-MakeDirs", "mkdir", 
				    args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + 
		     req.getNodeID() + "):\n\n" + 
		     proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version (" + 
		   req.getNodeID() + ")!");
	      }
	    }
	  }
	}

	/* the repository file and checksum directories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	File rpath = null;
	{
	  rpath = req.getNodeID().getCheckedInPath(rvid).toFile();
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);
	}

	/* build the list of files to copy */ 
	ArrayList<File> files = new ArrayList<File>();
	for(FileSeq fseq : req.getFileSequences()) 
	  files.addAll(fseq.getFiles());
	
	/* if frozen, create relative symlinks from the working files to the 
	   checked-in files */ 
	if(req.isFrozen()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--symbolic-link");
	  args.add("--remove-destination");

	  {
	    StringBuffer buf = new StringBuffer();
	    String comps[] = wpath.getPath().split("/");
	    int wk;
	    for(wk=1; wk<comps.length; wk++) 
	      buf.append("../");
	    buf.append(rpath.getPath().substring(1));
	    String path = buf.toString();

	    for(File file : files) 
	      args.add(path + "/" + file);
	    args.add(".");
	  }

	  SubProcessLight proc = 
	    new SubProcessLight(req.getNodeID().getAuthor(), 
				"CheckOut-Symlink", "cp", args, env, wdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to create symbolic links to the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while creating symbolic links to the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* otherwise, copy the checked-in files to the working directory */ 
	else {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + wdir);
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcessLight proc = 
	    new SubProcessLight(req.getNodeID().getAuthor(), 
				"CheckOut-Copy", "cp", args, env, rdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copy files from the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying files from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* if frozen, remove the working checksums */ 
	if(req.isFrozen()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcessLight proc = 
	    new SubProcessLight("CheckOut-RemoveCheckSums", "rm", args, env, cwdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to remove the working checksums for version (" + 
		 req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while removing the working checksums for version (" + 
	       req.getNodeID() + ")!");
	  }
	}

	/* otherwise, overwrite the working checksums with the checked-in checksums */ 
	else {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + cwdir);
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcessLight proc = 
	    new SubProcessLight("CheckOut-CopyCheckSums", "cp", args, env, crdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copy checksums from the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying checksums from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* if not frozen, add write permission to the working files and checksums */ 
        if(!req.isFrozen()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("u+w");
	  for(File file : files) 
	    args.add(file.getName());

	  if(req.getWritable()) {
	    SubProcessLight proc = 
	      new SubProcessLight(req.getNodeID().getAuthor(), 
				  "CheckOut-SetWritable", "chmod", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to add write access permission to the files for " + 
		   "the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the files for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }

	  {
	    SubProcessLight proc = 
	      new SubProcessLight("CheckOut-SetWritableCheckSums", "chmod", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to add write access permission to the checksums for " + 
		   "the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the checksums for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }
	}
	  
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.readLock().unlock();
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
    assert(req != null);
    TaskTimer timer = new TaskTimer("FileMgr.revert(): " + req.getNodeID());

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.readLock().lock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);

	  timer.aquire();
	  synchronized(pMakeDirLock) { 
	    timer.resume();	

	    ArrayList<File> dirs = new ArrayList<File>();
	    if(wdir.exists()) {
	      if(!wdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + wdir + 
		   ") in the location of the working directory!");
	    }
	    else {
	      dirs.add(wdir);
	    }

	    if(cwdir.exists()) {
	      if(!cwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + cwdir + 
		   ") in the location of the working checksum directory!");
	    }
	    else {
	      if(!cwdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the working checksum directory (" + cwdir + ")!");
	    }

	    if(!dirs.isEmpty()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      SubProcessLight proc = 
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "Revert-MakeDirs", "mkdir", 
				    args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + 
		     req.getNodeID() + "):\n\n" + 
		     proc.getStdErr());	
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

	/* copy the checked-in files to the working directory */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + wdir);
	  args.addAll(rfiles);

	  File rdir = new File(pProdDir, 
			       "repository" + req.getNodeID().getName());
	    
	  SubProcessLight proc = 
	    new SubProcessLight(req.getNodeID().getAuthor(), 
				"Revert-Copy", "cp", args, env, rdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copy files from the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying files from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* overwrite the working checksums with the checked-in checksums */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  args.add("--target-directory=" + cwdir);
	  args.addAll(rfiles);

	  File crdir = new File(pProdDir, 
				"checksum/repository" + req.getNodeID().getName());

	  SubProcessLight proc = 
	    new SubProcessLight("Revert-CopyCheckSums", "cp", args, env, crdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copy checksums from the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying checksums from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}
	
	/* add write permission to the to working files and checksums */ 
        {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("u+w");
	  args.addAll(req.getFiles().keySet());

	  if(req.getWritable()) {
	    SubProcessLight proc = 
	      new SubProcessLight(req.getNodeID().getAuthor(), 
				  "Revert-SetWritable", "chmod", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to add write access permission to the files for " + 
		   "the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the files for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }

	  {
	    SubProcessLight proc = 
	      new SubProcessLight("Revert-SetWritableCheckSums", "chmod", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to add write access permission to the checksums for " + 
		   "the working version (" + req.getNodeID() + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the checksums for " + 
		 "the working version (" + req.getNodeID() + ")!");
	    }
	  }
	}
	
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.readLock().unlock();
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

    timer.aquire();
    try {
      Object workingLock = getWorkingLock(targetID);
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the source working area file and checksum directories */ 
	File owdir  = null;
	File ocwdir = null;
	{
	  File wpath = sourceID.getWorkingParent().toFile();
	  owdir  = new File(pProdDir, wpath.getPath());
	  ocwdir = new File(pProdDir, "checksum/" + wpath);
	}
	
	/* verify (or create) the target working area file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = targetID.getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	  
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
				    "Clone-MakeDirs", "mkdir", 
				    args, env, pProdDir);
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

	    if(cwdir.exists()) {
	      if(!cwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + cwdir + 
		   ") in the location of the working checksum directory!");
	    }
	    else {
	      if(!cwdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the working checksum directory (" + cwdir + ")!");
	    }
	  }
	}
	
	/* copy each primary file */ 
	for(File ofile : files.keySet()) {
	  File opath = new File(owdir, ofile.getName());
	  if(opath.isFile()) {
	    File file = files.get(ofile);

	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.add(opath.getPath());
	    args.add(file.getName());
	    
	    SubProcessLight proc = 
	      new SubProcessLight(targetID.getAuthor(), 
				  "Clone-Primary", "cp", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to clone a primary file for version (" + targetID + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while cloning a primary file for version " + 
		   "(" + targetID + ")!");
	    }
	  }
	}
	
	/* copy each primary checksum */ 
	for(File ofile : files.keySet()) {
	  File opath = new File(ocwdir, ofile.getName());
	  if(opath.isFile()) {
	    File file = files.get(ofile);

	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.add(opath.getPath());
	    args.add(file.getName());
	    
	    SubProcessLight proc = 
	      new SubProcessLight("Clone-PrimaryCheckSum", "cp", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to clone a primary checksum for version " + 
		   "(" + targetID + "):\n\n" + 
		     proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while cloning a primary checksum for version " + 
		 "(" + targetID + ")!");
	    }
	  }
	}

	/* set write permission to the to working files */ 
        {
	  ArrayList<String> args = new ArrayList<String>();
	  if(req.getWritable()) 
	    args.add("u+w");
	  else 
	    args.add("u-w");

	  for(File file : files.values()) {
	    File path = new File(wdir, file.getName());
	    if(path.isFile()) 
	      args.add(file.getPath()); 
	  }
	  
	  if(args.size() > 1) {
	    SubProcessLight proc = 
	      new SubProcessLight(targetID.getAuthor(), 
				  "Clone-SetWritable", "chmod", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to set the access permission to the files for " + 
		   "the working version (" + targetID + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while setting the access permission to the files for " + 
	       "the working version (" + targetID + ")!");
	    }
	  }
	}

	/* set write permission to the to working filesand checksums */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("u+w");

	  for(File file : files.values()) {
	    File path = new File(cwdir, file.getName());
	    if(path.isFile()) 
	      args.add(file.getPath()); 
	  }
	    
	  if(args.size() > 1) {
	    SubProcessLight proc = 
	      new SubProcessLight("Clone-SetWritableCheckSums", "chmod", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to add write access permission to the checksums for " + 
		   "the working version (" + targetID + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while adding write access permission to the checksums for " + 
		 "the working version (" + targetID + ")!");
	    }
	  }
	}	
      
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
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
    assert(req != null);
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
    assert(req != null);
    TaskTimer timer = null;
    {
      StringBuffer buf = new StringBuffer();
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
    assert(req != null);
    TaskTimer timer = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.rename(): " + req.getNodeID() + " to " + req.getFilePattern());
      timer = new TaskTimer(buf.toString());
    }

    timer.aquire();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the new named node identifier */ 
	FilePattern npat = req.getFilePattern();
	NodeID id = new NodeID(req.getNodeID(), npat.getPrefix());

	/* the old working area file and checksum directories */ 
	File owdir  = null;
	File ocwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingParent().toFile();
	  owdir  = new File(pProdDir, wpath.getPath());
	  ocwdir = new File(pProdDir, "checksum/" + wpath);
	}
	
	/* verify (or create) the new working area file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = id.getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	  
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
	    
	    if(cwdir.exists()) {
	      if(!cwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + cwdir + ") in the location " + 
		   "of the working checksum directory!");
	    }
	    else {
	      if(!cwdir.mkdirs())
		throw new PipelineException
		  ("Unable to create the working checksum directory (" + cwdir + ")!");
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
	  Iterator<File> oiter = opfiles.iterator();
	  Iterator<File>  iter = pfiles.iterator();
	  while(oiter.hasNext() && iter.hasNext()) {
	    File ofile = oiter.next();
	    File opath = new File(owdir, ofile.getName());
	    
	    File file = iter.next();
	    
	    if(opath.isFile()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--force");
	      args.add(opath.getPath());
	      args.add(file.getName());
	      
	      SubProcessLight proc = 
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "Rename-Primary", "mv", args, env, wdir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to rename a primary file for version (" + id + "):\n\n" + 
		     proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while renaming a primary file for version (" + id + ")!");
	      }
	    }
	  }
	}
	
	/* move all of the secondary files (if the node directory changed) */ 
	if(!owdir.equals(wdir)) {	
	  ArrayList<String> old = new ArrayList<String>();
	  for(File file : sfiles) {
	    File work = new File(owdir, file.getPath());
	    if(work.isFile()) 
	      old.add(file.getName());
	  }
	  
	  if(!old.isEmpty()) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.add("--target-directory=" + wdir);
	    args.addAll(old);
	    
	    SubProcessLight proc = 
	      new SubProcessLight(req.getNodeID().getAuthor(), 
				  "Rename-Secondary", "mv", args, env, owdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to rename the secondary files for version (" + id + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while renaming the secondary files for version (" + id + ")!");
	    }
	  }
	}

	/* move each primary checksum */ 
	{
	  Iterator<File> oiter = opfiles.iterator();
	  Iterator<File>  iter = pfiles.iterator();
	  while(oiter.hasNext() && iter.hasNext()) {
	    File ofile = oiter.next();
	    File opath = new File(ocwdir, ofile.getName());
	    
	    File file = iter.next();
	    
	    if(opath.isFile()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--force");
	      args.add(opath.getPath());
	      args.add(file.getName());
	      
	      SubProcessLight proc = 
		new SubProcessLight("Rename-PrimaryCheckSum", "mv", args, env, cwdir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to rename a primary checksum for version (" + id + "):\n\n" + 
		     proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while renaming a primary checksum for version (" + id + ")!");
	      }
	    }
	  }
	}
	
	/* move all of the secondary checksums (if the node directory changed) */ 
	if(!owdir.equals(wdir)) {	
	  ArrayList<String> old = new ArrayList<String>();
	  for(File file : sfiles) {
	    File work = new File(ocwdir, file.getPath());
	    if(work.isFile()) 
	      old.add(file.getName());
	  }
	  
	  if(!old.isEmpty()) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.add("--target-directory=" + cwdir);
	    args.addAll(old);

	    SubProcessLight proc = 
	      new SubProcessLight("Rename-SecondaryCheckSums", "mv", args, env, ocwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to rename the secondary checksums for version (" + id + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while renaming the secondary checksums for version (" + id + 
		 ")!");
	    }
	  }
	}

	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
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
    
    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    checkedInLock.writeLock().lock();
    try {
      String rdir  = (pProdDir + "/repository" + name);
      String crdir = (pProdDir + "/checksum/repository" + name);
      
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("--recursive");
	args.add("u+w");
	args.add(rdir);
	args.add(crdir);
	
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

      File rparent = (new File(rdir)).getParentFile();
      File crparent = (new File(crdir)).getParentFile();      

      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("--recursive");
	args.add("--force");
	args.add(rdir);
	args.add(crdir);
	
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

      deleteEmptyParentDirs(new File(pProdDir + "/repository"), 
			    rparent);

      deleteEmptyParentDirs(new File(pProdDir + "/checksum/repository"), 
			    crparent);

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.writeLock().unlock();
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
    assert(req != null);
    TaskTimer timer = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.changeMode(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      timer = new TaskTimer(buf.toString());
    }

    timer.aquire(); 
    Object workingLock = getWorkingLock(req.getNodeID());
    try {
      synchronized(workingLock) {
	timer.resume();	

	Map<String,String> env = System.getenv();
	File wdir = new File(pProdDir, 
			     req.getNodeID().getWorkingParent().toString());

	ArrayList<String> args = new ArrayList<String>();
	args.add(req.getWritable() ? "u+w" : "u-w");
	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File path = new File(wdir, file.getPath());
	    if(path.isFile()) 
	      args.add(file.getPath());
	  }
	}
      
	if(args.size() > 1) {
	  SubProcessLight proc = 
	    new SubProcessLight(req.getNodeID().getAuthor(), 
				"ChangeMode", "chmod", args, env, wdir);
	  try { 
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to change the write access permission of the files for " + 
		 "the working version (" + req.getNodeID() + "):\n\n" + 
		 proc.getStdErr());	
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
      return new FailureRsp(timer, ex.getMessage());
    }
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
      TreeMap<String,TreeMap<VersionID,Long>> sizes =
	new TreeMap<String,TreeMap<VersionID,Long>>();

      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getFileSequences();
      for(String name : fseqs.keySet()) {
	TreeMap<VersionID,TreeSet<FileSeq>> versions = fseqs.get(name);

	TreeMap<VersionID,Long> vsizes = new TreeMap<VersionID,Long>();
	sizes.put(name, vsizes);
	
	for(VersionID vid : versions.keySet()) {
	  File dir = new File(pRepoDir, name + "/" + vid);
	  
	  long total = 0L;
	  for(FileSeq fseq : versions.get(vid)) {
	    for(File file : fseq.getFiles()) { 
	      File target = new File(dir, file.getPath());
	      total += target.length();
	    }
	  }
	  
	  vsizes.put(vid, total);  
	}
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
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to remove the checked-in version files.
   */
  public Object
  archive
  (
   FileArchiveReq req
  ) 
  { 
    String archiveName = req.getArchiveName();	
    TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getSequences();
    BaseArchiver archiver = req.getArchiver();
    Map<String,String> env = req.getEnvironment();

    TaskTimer timer = new TaskTimer("FileMgr.archive: " + archiveName);

    timer.aquire();
    Stack<ReentrantReadWriteLock> locks = new Stack<ReentrantReadWriteLock>();
    try {
      for(String name : fseqs.keySet()) {
	ReentrantReadWriteLock lock = getCheckedInLock(name);
	lock.readLock().lock();
	locks.push(lock);
      }
      timer.resume();

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

      /* create temporary directories and files */ 
      File dir = new File(pTempDir, "plfilemgr/archive/" + archiveName);
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
	    proc.join(15000);
	    cycles++;
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException(ex);
	  }
	  
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

      /* report the contents of the generated STDERR file as the failure message */ 
      if(!proc.wasSuccessful()) {
	String errors = null;
	try {
	  if(errFile.length() > 0) {
	    FileReader in = new FileReader(errFile);
	    
	    StringBuffer buf = new StringBuffer();
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
	  ("The process creating archive volume (" + archiveName + ") failed with " +
	   "exit code (" + proc.getExitCode() + ")!\n\n" + 
	   "The STDERR output of the process:\n" + 
	   errors);
      }

      /* read the generated STDOUT file */ 
      String output = null;
      try {
	if(outFile.length() > 0) {
	  FileReader in = new FileReader(outFile);

	  StringBuffer buf = new StringBuffer();
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
      
      return new FileArchiverRsp(timer, output);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      return new FailureRsp(timer, getFullMessage(ex));
    }
    finally {
      while(!locks.isEmpty()) {
	ReentrantReadWriteLock lock = locks.pop();
	lock.readLock().unlock();
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
      TreeMap<String,TreeMap<VersionID,Long>> sizes =
	new TreeMap<String,TreeMap<VersionID,Long>>();

      TreeMap<String,TreeMap<VersionID,TreeSet<File>>> files = req.getFiles();
      for(String name : files.keySet()) {
	TreeMap<VersionID,TreeSet<File>> versions = files.get(name);

	TreeMap<VersionID,Long> vsizes = new TreeMap<VersionID,Long>();
	sizes.put(name, vsizes);
	
	for(VersionID vid : versions.keySet()) {
	  File dir = new File(pRepoDir, name + "/" + vid);
	  
	  long total = 0L;
	  for(File file : versions.get(vid)) {
	    File target = new File(dir, file.getPath());
	    total += target.length();
	  }
	  
	  vsizes.put(vid, total);  
	}
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
    Map<String,String> env = System.getenv();

    TaskTimer timer = new TaskTimer("FileMgr.offline(): " + name + " (" + vid + ")");
    
    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    checkedInLock.writeLock().lock();
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

      /* add write permission to the version directories being modified */ 
      {
	ArrayList<String> args = new ArrayList<String>();
	args.add("u+w");
	for(VersionID avid : avids) 
	  args.add(avid.toString());

	SubProcessLight proc = 
	  new SubProcessLight("Offline-SetWritable", "chmod", args, env, nodeDir);
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

      /* remove the symlinks in later versions which reference this version */ 
      {
 	ArrayList<String> args = new ArrayList<String>();
 	args.add("--force");
	
	for(File file : symlinks.keySet()) {
	  for(VersionID lvid : symlinks.get(file)) 
	    args.add(lvid + "/" + file);
	}
	
	if(args.size() > 1) {
	  SubProcessLight proc = 
	    new SubProcessLight("Offline-DeleteSymlinks", "rm", args, env, nodeDir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to remove the stale symlinks referencing the checked-in version " + 
		 "(" + vid + ") of node (" + name + ") from the repository:\n\n" +
		 proc.getStdErr());
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

	for(VersionID lvid : moves.keySet()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  args.add("--target-directory=" + nodeDir + "/" + lvid);
	  
	  for(File file : moves.get(lvid)) 
	    args.add(file.toString());

	  File dir = new File(nodeDir, vid.toString());
	  
	  SubProcessLight proc = 
	    new SubProcessLight("Offline-MoveFiles", "mv", args, env, dir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to move the files associated with the checked-in version " + 
		 "(" + vid + ") of node (" + name + ") referenced by later version " +
		 "(" + lvid + ") symlinks:\n\n" + 
		 proc.getStdErr());	
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

 	ArrayList<String> args = new ArrayList<String>();
 	args.add("--force");
	
 	{
 	  File files[] = rdir.listFiles(); 
 	  int wk;
 	  for(wk=0; wk<files.length; wk++) 
 	    args.add(files[wk].getName());
 	}
	
 	SubProcessLight proc = 
 	  new SubProcessLight("Offline-DeleteVersion", "rm", args, env, rdir);
 	try {
 	  proc.start();
 	  proc.join();
 	  if(!proc.wasSuccessful()) 
 	    throw new PipelineException
 	      ("Unable to remove the files associated with the checked-in version " + 
 	       "(" + vid + ") of node (" + name + ") from the repository:\n\n" +
 	       proc.getStdErr());
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

      return new SuccessRsp(timer);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    finally {
      checkedInLock.writeLock().unlock();
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
    try {
      TreeMap<String,TreeSet<VersionID>> offlined = new TreeMap<String,TreeSet<VersionID>>();

      File dir = new File(pProdDir, "repository");
      File files[] = dir.listFiles(); 
      if(files != null) {
	int wk;
	for(wk=0; wk<files.length; wk++) 
	  getOfflinedHelper(files[wk], offlined);
      }

      return new FileGetOfflinedRsp(timer, offlined);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Recursively scan the repository directories for offlined checked-in versions.
   * 
   * @param dir
   *   The current directory (or file).
   * 
   * @param offlined
   *   The fully resolved names and revision numbers of offlined checked-in versions.
   */ 
  private void 
  getOfflinedHelper
  (
   File dir, 
   TreeMap<String,TreeSet<VersionID>> offlined
  ) 
  {
    int head = (pProdDir + "/repository").length();
    File files[] = dir.listFiles(); 
    if(files != null) {
      if(files.length == 0) {
	String name = dir.getParent().substring(head);
	VersionID vid = new VersionID(dir.getName());
	
	TreeSet<VersionID> vids = offlined.get(name);
	if(vids == null) {
	  vids = new TreeSet<VersionID>();
	  offlined.put(name, vids);
	}

	vids.add(vid);
      }
      else {
	int wk;
	for(wk=0; wk<files.length; wk++) 
	  getOfflinedHelper(files[wk], offlined);
      }
    }
  }


  /**
   * Extract the files associated with the given checked-in versions from the given archive 
   * volume and place them into a temporary directory.
   * 
   * @param req 
   *   The extract request.
   * 
   * @return
   *   <CODE>FileArchiverRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable extract the files.
   */
  public Object
  extract
  (
   FileExtractReq req
  ) 
  {
    String archiveName = req.getArchiveName();	
    Date stamp = req.getTimeStamp(); 
    BaseArchiver archiver = req.getArchiver();
    Map<String,String> env = req.getEnvironment();    
    TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getSequences();
    
    File restoreDir = new File(pProdDir, 
			       "restore/" + archiveName + "-" + stamp.getTime());

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

      /* create temporary directories and files */ 
      File tmpdir = new File(pTempDir, 
			     "plfilemgr/restore/" + archiveName + "-" + stamp.getTime());
      File scratch = new File(tmpdir, "scratch");
      File outFile = new File(tmpdir, "stdout");
      File errFile = new File(tmpdir, "stderr"); 
      {
	timer.aquire();
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
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finer,
	   "Restoring archive volume (" + archiveName + ") using archiver plugin " + 
	   "(" + archiver.getName() + ").");
	
	proc.start();
	
	int cycles = 0; 
	while(proc.isAlive()) {
	  try {
	    proc.join(15000);
	    cycles++;
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException(ex);
	  }
	  
	  LogMgr.getInstance().log
	    (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	     "Process for archive volume (" + archiveName + "): " + 
	       "WAITING for (" + cycles + ") loops...");
	}
	
	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest, 
	   "Process for archive volume (" + archiveName + "): " + 
	   "COMPLETED after (" + cycles + ") loops...");
     
	
	/* report the contents of the generated STDERR file as the failure message */ 
	if(!proc.wasSuccessful()) {
	  String errors = null;
	  try {
	    if(errFile.length() > 0) {
	      FileReader in = new FileReader(errFile);
	      
	      StringBuffer buf = new StringBuffer();
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
	  
	  StringBuffer buf = new StringBuffer();
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
	CheckSum checkSum = new CheckSum("MD5", pProdDir);
	for(File file : files) {
	  if(!checkSum.validateRestore(restoreDir, file))
	    throw new PipelineException
	      ("The restored file (" + file + ") has been corrupted!");
	}
      }
      
      return new FileArchiverRsp(timer, output);
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
    catch(Exception ex) {
      return new FailureRsp(timer, getFullMessage(ex));
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
    Date stamp = req.getTimeStamp(); 
    String name = req.getName();
    VersionID vid = req.getVersionID();
    TreeMap<File,TreeSet<VersionID>> symlinks = req.getSymlinks();
    TreeMap<File,VersionID> targets = req.getTargets();
    
    File restoreDir = new File(pProdDir, 
			       "restore/" + archiveName + "-" + stamp.getTime());
    Map<String,String> env = System.getenv();

    TaskTimer timer = new TaskTimer("FileMgr.restore: " + archiveName);

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    checkedInLock.writeLock().lock();
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
	  new SubProcessLight("Restore-SetWritable", "chmod", args, env, 
			      pTempDir);
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
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  args.add("--target-directory=" + nodeDir + "/" + vid);
	
	  for(File file : symlinks.keySet()) 
	    args.add(file.getPath()); 
	
	  File sdir = new File(restoreDir, name + "/" + vid);
	
	  SubProcessLight proc = 
	    new SubProcessLight("Restore-MoveFiles", "mv", args, env, sdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to move the restored files associated with the checked-in " + 
		 "version (" + vid + ") of node (" + name + ") into the repository:\n\n" + 
		 proc.getStdErr());	
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
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  
	  for(File file : symlinks.keySet()) {
	    for(VersionID lvid : symlinks.get(file))
	      args.add(lvid + "/" + file);
	  }
	  
	  if(args.size() > 1) {
	    SubProcessLight proc = 
	      new SubProcessLight("Restore-DeleteSymlinks", "rm", args, env, nodeDir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the stale symlinks referencing the checked-in " + 
		   "version (" + vid + ") of node (" + name + ") from the repository:\n\n" +
		   proc.getStdErr());
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the stale symlinks referencing the " + 
		 "checked-in version (" + vid + ") of node (" + name + ") from the " + 
		 "repository.");	
	    }
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
      return new FailureRsp(timer, getFullMessage(ex));
    }
    finally {
      checkedInLock.writeLock().unlock();
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
    Date stamp = req.getTimeStamp();     
    File dir = new File(pProdDir, 
			"restore/" + archiveName + "-" + stamp.getTime());

    TaskTimer timer = new TaskTimer("FileMgr.removeExtractDir: " + archiveName);

    timer.aquire();
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
      return new FailureRsp(timer, getFullMessage(ex));
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Generate a string containing both the exception message and stack trace. 
   * 
   * @param ex 
   *   The thrown exception.   
   */ 
  private String 
  getFullMessage
  (
   Throwable ex
  ) 
  {
    StringBuffer buf = new StringBuffer();
     
    if(ex.getMessage() != null) 
      buf.append(ex.getMessage() + "\n\n"); 	
    else if(ex.toString() != null) 
      buf.append(ex.toString() + "\n\n"); 	
      
    buf.append("Stack Trace:\n");
    StackTraceElement stack[] = ex.getStackTrace();
    int wk;
    for(wk=0; wk<stack.length; wk++) 
      buf.append("  " + stack[wk].toString() + "\n");
   
    return (buf.toString());
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the node lock (or create one if none already exists). <P> 
   * 
   * @param name 
   *   The fully resolved node name
   */
  private ReentrantReadWriteLock
  getCheckedInLock
  (
   String name
  ) 
  {
    synchronized(pCheckedInLocks) {
      ReentrantReadWriteLock lock = pCheckedInLocks.get(name);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
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
    timer.aquire();
    try {
      Object workingLock = getWorkingLock(id);
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the working and working checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = id.getWorkingParent().toFile();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	}
	
	/* remove the working files */ 
	{
	  ArrayList<String> old = new ArrayList<String>();
	  for(File file : files) {
	    File work = new File(wdir, file.getPath());
	    if(work.isFile()) 
	      old.add(file.getName());
	  }
	  
	  if(!old.isEmpty()) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.addAll(old);
	    
	    SubProcessLight proc = 
	      new SubProcessLight(id.getAuthor(), 
				  "Remove-Files", "rm", args, env, wdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the working files for version (" + 
		   id + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the working files for version (" + 
		 id + ")!");
	    }
	  }
	}
	
	/* remove the working checksums */ 
	{
	  ArrayList<String> old = new ArrayList<String>();
	  for(File file : files) {
	    File cksum = new File(cwdir, file.getPath());
	    if(cksum.isFile()) 
	      old.add(file.getName());
	  }
	  
	  if(!old.isEmpty()) {
	    ArrayList<String> args = new ArrayList<String>();
	    args.add("--force");
	    args.addAll(old);
	    
	    SubProcessLight proc = 
	      new SubProcessLight("Remove-CheckSums", "rm", args, env, cwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to remove the working checksums for version (" + 
		   id + "):\n\n" + 
		   proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the working checksums for version (" + 
		 id + ")!");
	    }
	  }

	  deleteEmptyParentDirs
	    (new File(pProdDir, 
		      "checksum/working/" + id.getAuthor() + "/" + id.getView()),
	     cwdir);
	}
	
	return new SuccessRsp(timer);
      }
    }
    catch(PipelineException ex) {
      return new FailureRsp(timer, ex.getMessage());
    }
  }

  /**
   * Recursively remove all empty directories at or above the given directory.
   * 
   * @param root
   *   The delete operation should stop at this directory regardles of whether it is empty.
   * 
   * @param parent
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

	LogMgr.getInstance().log
	  (LogMgr.Kind.Ops, LogMgr.Level.Finest,
	   "Deleting Empty Directory: " + tmp);
	LogMgr.getInstance().flush();

	if(!tmp.delete()) 
	  throw new PipelineException
	    ("Unable to delete the empty directory (" + tmp + ")!");

	tmp = parent;
      }
    }
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The common back-end directories.
   * 
   * Since the file manager should always be run on a Unix system, these variables are always
   * initialized to Unix specific paths.
   */
  private File pProdDir; 
  private File pRepoDir; 
  private File pTempDir; 

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 
  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with all checked-in 
   * versions of each node. The per-node read-lock should be aquired for operations which 
   * will only access these checked-in file resources.  The per-node write-lock should be 
   * aquired when creating new files, symlinks or checksums.
   */
  private HashMap<String,ReentrantReadWriteLock>  pCheckedInLocks;

  /**
   * The per-working version locks indexed by NodeID. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with the working 
   * versions for each user and view of each node.  These locks should be used in a 
   * <CODE>synchronized()<CODE> statement block wrapping any access or modification of 
   * these file resources.
   */
  private HashMap<NodeID,Object>  pWorkingLocks;

}

