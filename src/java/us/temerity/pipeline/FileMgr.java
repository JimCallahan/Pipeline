// $Id: FileMgr.java,v 1.5 2004/03/16 16:11:33 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.message.*;

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
 * This class acts together with the {@link NodeMgr NodeMgr} class to provide the 
 * complete I/O support for Pipeline. The files managed by this class live under a 
 * standardized directory structure which has a close coupling with node related 
 * information managed by <CODE>NodeMgr</CODE>.  The following details the layout of 
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
 *   the <CODE>--with-prod=DIR</CODE> option to <I>configure(1)</I> or as an agument to the 
 *   constructor for this class.  The (<I>author</I>) is the name of the user owning the 
 *   working version of the node.  The (<I>view</I>) is the name of the particular working 
 *   area view of which the working version is a member.  In practice the environmental 
 *   variable <CODE>$WORKING</CODE> is set to contain the full path to this particular 
 *   view. <P> 
 * 
 *   The (<I>fully-resolved-node-path</I>) is all but the last component of the fully  
 *   resolved name of the node.  The last component of this name is the prefix of the files 
 *   which make up the primary file sequence of the node.  The (<I>node-name)</I> files are 
 *   one or more files which make up the primary and secondary file sequences associated 
 *   with the node. <P> 
 * 
 *   If the working version of a node has been frozen (see {@link #freeze freeze}), the 
 *   (<I>node-name)</I> files will not be regular files. Instead, they will be symbolic 
 *   links to the respective read-only file associated with the checked-in version upon 
 *   which the working version is based.
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
 * @see NodeMgr
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
   * @param dir [<B>in</B>]
   *   The root production directory.
   */
  public
  FileMgr
  (
   File dir
  )
  { 
    init(dir);
  }
  
  /** 
   * Construct a new file manager.
   * 
   * The root production directory is set by the <CODE>--with-prod=DIR</CODE> 
   * option to <I>configure(1)</I>.
   */
  public
  FileMgr() 
  { 
    init(PackageInfo.sProdDir);
  }


  /*-- CONTRUCTION HELPERS -----------------------------------------------------------------*/

  private synchronized void 
  init
  (
   File dir
  )
  { 
    pNodeLocks   = new HashMap<String,ReentrantReadWriteLock>();
    pWorkLocks   = new HashMap<NodeID,Object>();
    pMakeDirLock = new Object();

    if(dir == null)
      throw new IllegalArgumentException("The root production directory cannot be (null)!");
    pProdDir = dir;

    pCheckSum = new CheckSum("MD5", pProdDir);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Refresh any missing or out-of-date checksums for the given working version of a node.
   * 
   * @param req [<B>in</B>]
   *   The checksum request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to generate the checksum files.
   */
  public Object
  refreshCheckSums
  (
   FileCheckSumReq req
  ) 
  {
    if(req == null) 
      return new FailureRsp("The checksum request cannot be (null)!");
    
    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.refreshCheckSums(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

	for(FileSeq fseq : req.getFileSequences()) {
	  for(File file : fseq.getFiles()) {
	    File work = new File(req.getNodeID().getWorkingDir(), file.getPath());
	    pCheckSum.refresh(work);
	  }
	}

	return new SuccessRsp(task, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
  }

  /**
   * Compute the {@link FileState FileState} for each file associated with the working 
   * version of a node. <P> 
   * 
   * @param req [<B>in</B>]
   *   The file state request.
   * 
   * @return
   *   <CODE>FileStateRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to compute the file states.
   */ 
  public Object
  computeFileStates
  (
   FileStateReq req
  ) 
  {
    if(req == null) 
      return new FailureRsp("The file state request cannot be (null)!");

    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.computeFileStates(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock nodeLock = getNodeLock(req.getNodeID().getName());
    nodeLock.readLock().lock();
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

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
	      File work = new File(pProdDir, req.getNodeID().getWorkingDir() + "/" + file);

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
	      File wpath = new File(req.getNodeID().getWorkingDir() + "/" + file);
	      File work  = new File(pProdDir, wpath.getPath());
	      
	      if(!work.isFile()) 
		fs[wk] = FileState.Missing;
	      else {
		VersionID lvid = req.getLatestVersionID();
		File lpath  = new File(req.getNodeID().getCheckedInDir(lvid) + "/" + file);
		File latest = new File(pProdDir, lpath.getPath());
		
		if(!latest.isFile()) 
		  fs[wk] = FileState.Added;
		else if(work.length() != latest.length()) 
		  fs[wk] = FileState.Modified;
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
	      File wpath = new File(req.getNodeID().getWorkingDir() + "/" + file);
	      File work  = new File(pProdDir, wpath.getPath());

	      if(!work.isFile()) 
		fs[wk] = FileState.Missing;
	      else {
		VersionID lvid = req.getLatestVersionID();
		File lpath  = new File(req.getNodeID().getCheckedInDir(lvid) + "/" + file);
		File latest = new File(pProdDir, lpath.getPath());

		VersionID bvid = req.getWorkingVersionID();
		File bpath = new File(req.getNodeID().getCheckedInDir(bvid) + "/" + file);
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
		  if(work.length() == latest.length()) {
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
		      if(work.length() == base.length()) {
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
	
	return new FileStateRsp(req.getNodeID(), states, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    catch(IOException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      nodeLock.readLock().unlock();
    }  
  }

  /**
   * Perform the file system operations needed to create a new checked-in version of the 
   * node in the file repository based on the given working version. <P> 
   * 
   * @param req [<B>in</B>]
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
    if(req == null) 
      return new FailureRsp("The check-in request cannot be (null)!");
    
    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.checkIn(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock nodeLock = getNodeLock(req.getNodeID().getName());
    nodeLock.writeLock().lock();
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

	/* create the repository file and checksum directories
	     as well any missing subdirectories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	{
	  File rpath = req.getNodeID().getCheckedInDir(rvid);
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);

	  synchronized(pMakeDirLock) { 
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
	  ldir = new File(pProdDir, req.getNodeID().getCheckedInDir(lvid).getPath());
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
	  File wpath = req.getNodeID().getWorkingDir();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	}
	

	/* process the files */ 
	ArrayList<File> copies = new ArrayList<File>();
	ArrayList<File> links  = new ArrayList<File>();
	{
	  TreeMap<FileSeq, FileState[]> stable = req.getFileStates();
	  for(FileSeq fseq : req.getFileSequences()) {
	    FileState[] states = stable.get(fseq);
	    int wk = 0;
	    for(File file : fseq.getFiles()) {
	      File work = new File(wdir, file.getPath());

	      switch(states[wk]) {
	      case Pending:
	      case Modified:
	      case Added:
		copies.add(file);	
		break;

	      case Identical:
		{
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
		break;
		  
	      case Obsolete:
		break;

	      default:
		//assert(false);
		throw new PipelineException
		  ("Somehow the working file (" + work + ") with a file state of (" + 
		   states[wk].name() + ") was erroneously submitted for check-in!");
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
	    
	    SubProcess proc = 
	      new SubProcess("CheckIn-Copy", "cp", args, env, wdir);
	    proc.start();
	    
	    try {
	      proc.join();
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while copying files for working version (" + req.getNodeID() + 
		 ") into the file repository!");
	    }
	    
	    if(!proc.wasSuccessful()) {
	      throw new PipelineException
		("Unable to copy files for working version (" + req.getNodeID() + 
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
	    for(FileSeq fseq : req.getFileSequences()) 
	      for(File file : fseq.getFiles()) 
		args.add(file.getPath());

	    SubProcess proc = 
	      new SubProcess("CheckIn-CopyCheckSums", "cp", args, env, cwdir);
	    proc.start();
	    
	    try {
	      proc.join();
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while copying checksums for working version (" + 
		 req.getNodeID() + ") into the checksum repository!");
	    }

	    for(FileSeq fseq : req.getFileSequences()) {
	      for(File file : fseq.getFiles()) {
		File repo = new File(crdir, file.getPath());
		repo.setReadOnly();
	      }
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

	return new SuccessRsp(task, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      nodeLock.writeLock().unlock();
    }  
  }

  /**
   * Overwrite the files associated with the working version of the node with a copy of
   * the files associated with the given checked-in version. 
   * 
   * @param req [<B>in</B>]
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
    if(req == null) 
      return new FailureRsp("The check-out request cannot be (null)!");
    
    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.checkOut(): " + req.getNodeID() + " (" + req.getVersionID() + ") ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock nodeLock = getNodeLock(req.getNodeID().getName());
    nodeLock.readLock().lock();
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file, backup and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	File bwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingDir();
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

	    bwdir = new File(wdir, ".backup");
	    if(bwdir.exists()) {
	      if(!bwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + bwdir + 
		   ") in the location of the working backup directory!");
	    }
	    else {
	      dirs.add(bwdir);
	    }
	    
	    if(cwdir.exists()) {
	      if(!cwdir.isDirectory()) 
		throw new PipelineException
		  ("Somehow there exists a non-directory (" + cwdir + 
		   ") in the location of the working checksum directory!");
	    }
	    else {
	      dirs.add(cwdir);
	    }

	    if(!dirs.isEmpty()) {
	      ArrayList<String> args = new ArrayList<String>();
	      args.add("--parents");
	      args.add("--mode=755");
	      for(File dir : dirs)
		args.add(dir.getPath());
	      
	      SubProcess proc = 
		new SubProcess(req.getNodeID().getAuthor(), 
			       "CheckOut-MakeDirs", "mkdir", args, env, pProdDir);
	      proc.start();
	      
	      try {
		proc.join();
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
	{
	  File rpath = req.getNodeID().getCheckedInDir(rvid);
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);
	}

	/* build the list of files to copy */ 
	ArrayList<File> files = new ArrayList<File>();
	for(FileSeq fseq : req.getFileSequences()) 
	  files.addAll(fseq.getFiles());
	
	/* move any existing working files which would be overwritten into 
	    the backup directory */ 
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
	    args.add("--update");
	    args.add("--target-directory=" + bwdir);
	    args.addAll(old);

	    SubProcess proc = 
	      new SubProcess(req.getNodeID().getAuthor(), 
			     "CheckOut-Backup", "mv", args, env, wdir);
	    proc.start();
	    
	    try {
	      proc.join();
	    }
	    catch(InterruptedException ex) {
	      throw new PipelineException
		("Interrupted while backing-up the working files for version (" + 
		 req.getNodeID() + ")!");
	    }
	  }
	}

	/* copy the checked-in files to the working directory */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--target-directory=" + wdir);
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcess proc = 
	    new SubProcess(req.getNodeID().getAuthor(), 
			   "CheckOut-Copy", "cp", args, env, rdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying files from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	/* add write permission to files associated with editable nodes */ 
	if(req.isEditable()) {
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("u+w");
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcess proc = 
	    new SubProcess(req.getNodeID().getAuthor(), 
			   "CheckOut-SetWritable", "chmod", args, env, wdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while adding write access permission to the files for the " + 
	       "working version (" + req.getNodeID() + ")!");
	  }
	}
	
	/* overwrite the working checksums with the checked-in checksums */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--force");
	  args.add("--target-directory=" + cwdir);
	  for(File file : files) 
	    args.add(file.getName());

	  SubProcess proc = 
	    new SubProcess("CheckOut-CopyCheckSums", "cp", args, env, crdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while copying checksums from the repository for the " +
	       "working version (" + req.getNodeID() + ")!");
	  }
	}

	return new SuccessRsp(task, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      nodeLock.readLock().unlock();
    }  
  }

  /**
   * Replaces the files associated with a working version of a node with symlinks to  
   * the respective files associated with the checked-in version upon which the working 
   * version is based.
   * 
   * @param req [<B>in</B>]
   *   The freeze request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to freeze the files.
   */
  public Object
  freeze
  (
   FileFreezeReq req
  ) 
  {
    if(req == null) 
      return new FailureRsp("The freeze request cannot be (null)!");
    
    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.freeze(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock nodeLock = getNodeLock(req.getNodeID().getName());
    nodeLock.readLock().lock();
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file, backup and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingDir();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	}

	/* the repository file and checksum directories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	{
	  File rpath = req.getNodeID().getCheckedInDir(rvid);
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);
	}

	/* build the list of files to freeze */ 
	ArrayList<File> files = new ArrayList<File>();
	for(FileSeq fseq : req.getFileSequences()) 
	  files.addAll(fseq.getFiles());
	
	/* replace working files with links to the repository files */ 
	{ 
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--symbolic-link");
	  args.add("--remove-destination");
	  args.add("--target-directory=" + wdir);
	  for(File file : files) 
	    args.add(rdir + "/" + file);
	  
	  SubProcess proc = 
	    new SubProcess(req.getNodeID().getAuthor(), 
			   "Freeze-Link", "cp", args, env, pProdDir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while freezing the working files for version (" + 
	       req.getNodeID() + ")!");
	  }
	}

	/* overwrite working checksums with the repository checksums */ 
	{ 
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + cwdir);
	  for(File file : files) 
	    args.add(file.getName());
	  
	  SubProcess proc = 
	    new SubProcess("Freeze-CopyCheckSums", "cp", args, env, crdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while unfreezing the working checksums for version (" + 
	       req.getNodeID() + ")!");
	  }
	}

	return new SuccessRsp(task, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      nodeLock.readLock().unlock();
    }  
  }

  /**
   * Replace the symlinks associated with the a working version of a node with copies 
   * of the respective checked-in files which are the current targets of the symlinks. 
   * 
   * @param req [<B>in</B>]
   *   The unfreeze request.
   * 
   * @return
   *   <CODE>SuccessRsp</CODE> if successful or 
   *   <CODE>FailureRsp</CODE> if unable to unfreeze the files.
   */
  public Object
  unfreeze
  (
   FileUnfreezeReq req
  ) 
  {
    if(req == null) 
      return new FailureRsp("The unfreeze request cannot be (null)!");
    
    String task = null;
    {
      StringBuffer buf = new StringBuffer();
      buf.append("FileMgr.unfreeze(): " + req.getNodeID() + " ");
      for(FileSeq fseq : req.getFileSequences()) 
	buf.append("[" + fseq + "]");
      task = buf.toString();
    }

    Date start = new Date();
    long wait = 0;
    ReentrantReadWriteLock nodeLock = getNodeLock(req.getNodeID().getName());
    nodeLock.readLock().lock();
    try {
      Object workLock = getWorkLock(req.getNodeID());
      synchronized(workLock) {
	wait  = (new Date()).getTime() - start.getTime();
	start = new Date();

	Map<String,String> env = System.getenv();

	/* verify (or create) the working area file, backup and checksum directories */ 
	File wdir  = null;
	File cwdir = null;
	{
	  File wpath = req.getNodeID().getWorkingDir();
	  wdir  = new File(pProdDir, wpath.getPath());
	  cwdir = new File(pProdDir, "checksum/" + wpath);
	}

	/* the repository file and checksum directories */ 
	VersionID rvid = req.getVersionID();
	File rdir  = null;
	File crdir = null;
	{
	  File rpath = req.getNodeID().getCheckedInDir(rvid);
	  rdir  = new File(pProdDir, rpath.getPath());
	  crdir = new File(pProdDir, "checksum/" + rpath);
	}

	/* build the list of files to unfreeze */ 
	ArrayList<File> files = new ArrayList<File>();
	for(FileSeq fseq : req.getFileSequences()) 
	  files.addAll(fseq.getFiles());
	
	/* replace working links with copies of repository files */ 
	{ 
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + wdir);
	  for(File file : files) 
	    args.add(file.getName());
	  
	  SubProcess proc = 
	    new SubProcess(req.getNodeID().getAuthor(), 
			   "UnFreeze-Copy", "cp", args, env, rdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while unfreezing the working files for version (" + 
	       req.getNodeID() + ")!");
	  }
	}

	/* overwrite working checksums with the repository checksums */ 
	{ 
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("--remove-destination");
	  args.add("--target-directory=" + cwdir);
	  for(File file : files) 
	    args.add(file.getName());
	  
	  SubProcess proc = 
	    new SubProcess("UnFreeze-CopyCheckSums", "cp", args, env, crdir);
	  proc.start();
	  
	  try {
	    proc.join();
	  }
	  catch(InterruptedException ex) {
	    throw new PipelineException
	      ("Interrupted while unfreezing the working checksums for version (" + 
	       req.getNodeID() + ")!");
	  }
	}

	return new SuccessRsp(task, wait, start);
      }
    }
    catch(PipelineException ex) {
      if(wait > 0) 
	return new FailureRsp(task, ex.getMessage(), wait, start);
      else 
	return new FailureRsp(task, ex.getMessage(), start);
    }
    finally {
      nodeLock.readLock().unlock();
    }  
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Lookup the node lock (or create one if none already exists). <P> 
   * 
   * The node lock protects access to both working and checked-in files associated with 
   * the node.  Threads which wish to modify or add files associated with any checked-in 
   * version of the node should aquire the WRITE lock.  Threads which wish only to access
   * the state of checked-in files should aquire the READ lock instread.  Threads which 
   * no not access or modify any checked-in files do not need to aquire the node lock.
   * 
   * @param name [<B>in</B>]
   *   The fully resolved node name
   */
  private ReentrantReadWriteLock
  getNodeLock
  (
   String name
  ) 
  {
    synchronized(pNodeLocks) {
      ReentrantReadWriteLock lock = pNodeLocks.get(name);

      if(lock == null) { 
	lock = new ReentrantReadWriteLock();
	pNodeLocks.put(name, lock);
      }

      return lock;
    }
  }

  /** 
   * Lookup the working version lock (or create one if none already exists). <P> 
   * 
   * The working version lock protects the files associated with working versions from 
   * being accessed or modified by more than one thread at a time.  Any thread which needs
   * to access the state or modify any of the files associated with a working version should
   * protected this access by using this lock as the argument to a <CODE>synchronized</CODE>
   * block. <P> 
   * 
   * If files associated with a checked-in version of the node will also be accessed or 
   * modified by the thread, then this lock should be uses within the scope of an already 
   * aquired READ or WRITE node lock.  See {@link #getNodeLock getNodeLock} for details.
   * 
   * @param id [<B>in</B>]
   *   The unique working version identifier.
   */
  private Object
  getWorkLock
  (
   NodeID id
  ) 
  {
    synchronized(pWorkLocks) {
      Object lock = pWorkLocks.get(id);

      if(lock == null) { 
	lock = new Object();
	pWorkLocks.put(id, lock);
      }

      return lock;
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The per-node locks indexed by fully resolved node name. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with all checked-in 
   * versions of each node. The per-node read-lock should be aquired for operations which 
   * will only access these checked-in file resources.  The per-node write-lock should be 
   * aquired when creating new files, symlinks or checksums.
   */
  private HashMap<String,ReentrantReadWriteLock>  pNodeLocks;

  /**
   * The per-working version locks indexed by NodeID. <P> 
   * 
   * These locks protect the files, symlinks and checksums associated with the working 
   * versions for each user and view of each node.  These locks should be used in a 
   * <CODE>synchronized()<CODE> statement block wrapping any access or modification of 
   * these file resources.
   */
  private HashMap<NodeID,Object>  pWorkLocks;

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
}

