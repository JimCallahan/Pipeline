// $Id: FileMgr.java,v 1.27 2004/11/17 13:33:50 jim Exp $

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
 * 
 * @see FileMgrClient
 * @see FileMgrServer
 * @see MasterMgr
 * @see CheckSum
 */
public
class FileMgr
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new file manager.
   * 
   * @param dir 
   *   The root production directory.
   */
  public
  FileMgr
  (
   File dir
  ) 
  {
    pCheckedInLocks = new HashMap<String,ReentrantReadWriteLock>();
    pWorkingLocks   = new HashMap<NodeID,Object>();
    pMakeDirLock    = new Object();

    if(dir == null)
      throw new IllegalArgumentException("The root production directory cannot be (null)!");
    pProdDir = dir;

    pCheckSum = new CheckSum("MD5", pProdDir);
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
	    new SubProcessLight(author, "CreateWorkingArea", "mkdir", args, env, pProdDir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to create the working area directory (" + wdir + "):\n\n" + 
		 "  " + proc.getStdErr());
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

    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(req.getNodeID().getName());
    checkedInLock.readLock().lock();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();

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
	      File work = new File(pProdDir, req.getNodeID().getWorkingParent() + "/" + file);

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
		  pCheckSum.refresh(wpath);
		  if(pCheckSum.compare(wpath, lpath))
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
		    pCheckSum.refresh(wpath);
		    workRefreshed = true;
		    workEqLatest = pCheckSum.compare(wpath, lpath);
		  }

		  if(workEqLatest) {
		    fs[wk] = FileState.Identical;
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
			  pCheckSum.refresh(wpath);
			workEqBase = pCheckSum.compare(wpath, bpath);
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
	      File work = new File(pProdDir, req.getNodeID().getWorkingParent() + "/" + file);
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

	/* refresh the working checksums */ 
	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File work = new File(req.getNodeID().getWorkingParent(), file.getPath());
	    pCheckSum.refresh(work);
	  }
	}

	/* create the repository file and checksum directories
	     as well any missing subdirectories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	{
	  File rpath = req.getNodeID().getCheckedInPath(rvid);
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
	  ldir = new File(pProdDir, req.getNodeID().getCheckedInPath(lvid).getPath());
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
	  File wpath = req.getNodeID().getWorkingParent();
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
		   "  " + proc.getStdErr());
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
		   "  " + proc.getStdErr());		   
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
	  wpath = req.getNodeID().getWorkingParent();
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
				    "CheckOut-MakeDirs", "mkdir", args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + 
		     req.getNodeID() + "):\n\n" + 
		     "  " + proc.getStdErr());	
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
	  rpath = req.getNodeID().getCheckedInPath(rvid);
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
		 "  " + proc.getStdErr());	
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
		 "  " + proc.getStdErr());	
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
		 "  " + proc.getStdErr());	
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
		 "  " + proc.getStdErr());	
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
		   "  " + proc.getStdErr());	
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
		   "  " + proc.getStdErr());	
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
	  File wpath = req.getNodeID().getWorkingParent();
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
				    "Revert-MakeDirs", "mkdir", args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + 
		     req.getNodeID() + "):\n\n" + 
		     "  " + proc.getStdErr());	
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

	  File rdir = new File(pProdDir, "repository" + req.getNodeID().getName());
	    
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
		 "  " + proc.getStdErr());	
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

	  File crdir = new File(pProdDir, "checksum/repository" + req.getNodeID().getName());

	  SubProcessLight proc = 
	    new SubProcessLight("Revert-CopyCheckSums", "cp", args, env, crdir);
	  try {
	    proc.start();
	    proc.join();
	    if(!proc.wasSuccessful()) 
	      throw new PipelineException
		("Unable to copy checksums from the repository for the " +
		 "working version (" + req.getNodeID() + "):\n\n" + 
		 "  " + proc.getStdErr());	
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
		   "  " + proc.getStdErr());	
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
		   "  " + proc.getStdErr());	
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
      buf.append("FileMgr.rename(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      buf.append(" to " + req.getNewName());
      timer = new TaskTimer(buf.toString());
    }

    timer.aquire();
    try {
      Object workingLock = getWorkingLock(req.getNodeID());
      synchronized(workingLock) {
	timer.resume();	
	
	Map<String,String> env = System.getenv();
	
	/* the new named node identifier */ 
	NodeID id = new NodeID(req.getNodeID(), req.getNewName());

	/* the old working area file and checksum directories */ 
	File owdir  = null;
	File ocwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingParent();
	  owdir  = new File(pProdDir, wpath.getPath());
	  ocwdir = new File(pProdDir, "checksum/" + wpath);
	}
	
	/* verify (or create) the new working area file and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = id.getWorkingParent();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	  
	  synchronized(pMakeDirLock) { 
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
		  ("Unable to create the working  checksum directory (" + cwdir + ")!");
	    }
	    
	    if(!dirs.isEmpty()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      SubProcessLight proc = 
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "Rename-MakeDirs", "mkdir", args, env, pProdDir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to create directories for working version (" + id + "):\n\n" + 
		     "  " + proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while creating directories for working version (" + id + 
		   ")!");
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
	  boolean primary = true;
	  for(FileSeq fseq : req.getFileSequences()) {
	    if(primary) {
	      opfiles.addAll(fseq.getFiles());
	      
	      File path = new File(req.getNewName());
	      FilePattern pat = fseq.getFilePattern();
	      FileSeq nfseq = new FileSeq(new FilePattern(path.getName(), pat.getPadding(), 
							  pat.getSuffix()),
					  fseq.getFrameRange());	  
	      pfiles.addAll(nfseq.getFiles());
	      
	      primary = false;
	    }
	    else {
	      sfiles.addAll(fseq.getFiles());
	    }
	  }
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
		     "  " + proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while renaming a primary file for version (" + id + ")!");
	      }
	    }
	  }
	}
	
	/* move all of the secondary files */ 
	{	
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
		   "  " + proc.getStdErr());	
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
		new SubProcessLight(req.getNodeID().getAuthor(), 
				    "Rename-PrimaryCheckSum", "mv", args, env, cwdir);
	      try {
		proc.start();
		proc.join();
		if(!proc.wasSuccessful()) 
		  throw new PipelineException
		    ("Unable to rename a primary checksum for version (" + id + "):\n\n" + 
		     "  " + proc.getStdErr());	
	      }
	      catch(InterruptedException ex) {
		throw new PipelineException
		  ("Interrupted while renaming a primary checksum for version (" + id + ")!");
	      }
	    }
	  }
	}
	
	/* move all of the secondary checksums */ 
	{	
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
	      new SubProcessLight(req.getNodeID().getAuthor(), 
				  "Rename-SecondaryCheckSums", "mv", args, env, ocwdir);
	    try {
	      proc.start();
	      proc.join();
	      if(!proc.wasSuccessful()) 
		throw new PipelineException
		  ("Unable to rename the secondary checksums for version (" + id + "):\n\n" + 
		   "  " + proc.getStdErr());	
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
	  new SubProcessLight("ChmodCheckedIn", "chmod", args, env, PackageInfo.sTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to make the files associated with the checked-in versions of " + 
	       "node (" + name + ") writeable:\n" +
	       "  " + proc.getStdErr());
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
	args.add(crdir);
	
	Map<String,String> env = System.getenv();
	
	SubProcessLight proc = 
	  new SubProcessLight("DeleteCheckedIn", "rm", args, env, PackageInfo.sTempDir);
	try {
	  proc.start();
	  proc.join();
	  if(!proc.wasSuccessful()) 
	    throw new PipelineException
	      ("Unable to remove all files associated with the checked-in versions of " + 
	       "node (" + name + ") from the repository:\n" +
	       "  " + proc.getStdErr());
	}
	catch(InterruptedException ex) {
	  throw new PipelineException
	    ("Interrupted while removing all files associated with the checked-in " + 
	     "versions of node (" + name + ") from the repository!");
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
	File wdir = new File(pProdDir, req.getNodeID().getWorkingParent().getPath());

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
		 "  " + proc.getStdErr());	
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
    TaskTimer timer = new TaskTimer("FileMgr.offline(): " + name + " (" + vid + ")");
    
    timer.aquire();
    ReentrantReadWriteLock checkedInLock = getCheckedInLock(name);
    checkedInLock.writeLock().lock();
    try {
      File rdir = new File(pProdDir, "repository" + name + "/" + vid);
      
      ArrayList<String> args = new ArrayList<String>();
      args.add("--force");

      {
	File files[] = rdir.listFiles(); 
	int wk;
	for(wk=0; wk<files.length; wk++) 
	  args.add(files[wk].getName());
      }
	
      Map<String,String> env = System.getenv();
	
      SubProcessLight proc = 
	new SubProcessLight("Offline", "rm", args, env, rdir);
      try {
	proc.start();
	proc.join();
	if(!proc.wasSuccessful()) 
	  throw new PipelineException
	    ("Unable to offline the files associated with the checked-in version " + 
	     "(" + vid + ") of node (" + name + ") from the repository:\n" +
	     "  " + proc.getStdErr());
      }
      catch(InterruptedException ex) {
	throw new PipelineException
	  ("Interrupted while offline the files associated with the checked-in version " + 
	   "(" + vid + ") of node (" + name + ") from the repository!");
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


  /*----------------------------------------------------------------------------------------*/

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
      getOfflinedHelper(dir, offlined);

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
    File files[] = dir.listFiles(); 
    if(files != null) {
      if(files.length == 0) {
	String name = dir.getParent().substring(pProdDir.getPath().length());
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


  /*----------------------------------------------------------------------------------------*/

  /**
   * Calculate the total size (in bytes) of the files associated with the given 
   * checked-in versions.
   * 
   * @param req
   *   The file sizes request.
   * 
   * @return
   *   <CODE>FileGetSizesRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to determine the file sizes.
   */ 
  public Object
  getSizes
  (
   FileGetSizesReq req
  ) 
  {
    TaskTimer timer = new TaskTimer();
    try {
      TreeMap<String,TreeMap<VersionID,Long>> sizes = null;

      TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs = req.getFileSequences();
      if(req.considerLinks()) 
	sizes = getOfflinedSizes(timer, fseqs);
      else
	sizes = getArchivedSizes(timer, fseqs);

      return new FileGetSizesRsp(timer, sizes);
    }
    catch(Exception ex) {
      return new FailureRsp(timer, ex.getMessage());
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
  private TreeMap<String,TreeMap<VersionID,Long>>
  getArchivedSizes
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
  {
    TreeMap<String,TreeMap<VersionID,Long>> sizes =
      new TreeMap<String,TreeMap<VersionID,Long>>();
    
    for(String name : fseqs.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = fseqs.get(name);

      TreeMap<VersionID,Long> vsizes = new TreeMap<VersionID,Long>();
      sizes.put(name, vsizes);

      for(VersionID vid : versions.keySet()) {
	File dir = new File(PackageInfo.sRepoDir, name + "/" + vid);

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

    return sizes;
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
  private TreeMap<String,TreeMap<VersionID,Long>>
  getOfflinedSizes
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,TreeSet<FileSeq>>> fseqs
  ) 
  {
    TreeMap<String,TreeMap<VersionID,Long>> sizes =
      new TreeMap<String,TreeMap<VersionID,Long>>();
    
    for(String name : fseqs.keySet()) {
      TreeMap<VersionID,TreeSet<FileSeq>> versions = fseqs.get(name);

      TreeMap<VersionID,Long> vsizes = new TreeMap<VersionID,Long>();
      sizes.put(name, vsizes);

      for(VersionID vid : versions.keySet()) {
	File dir = new File(PackageInfo.sRepoDir, name + "/" + vid);

	long total = 0L;
	for(FileSeq fseq : versions.get(vid)) {
	  for(File file : fseq.getFiles()) { 
	    File target = new File(dir, file.getPath());
	  
	    // ...

	  }
	}

	vsizes.put(vid, total);  
      }
    }

    return sizes;
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
	  File wpath = id.getWorkingParent();
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
		   "  " + proc.getStdErr());	
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
		   "  " + proc.getStdErr());	
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while removing the working checksums for version (" + 
		 id + ")!");
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
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The file system directory creation lock.
   */
  private Object pMakeDirLock;
 
  /**
   * The root production directory.
   */ 
  private File  pProdDir;

  /**
   * The checksum generator. 
   */ 
  private CheckSum  pCheckSum; 


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

