// $Id: FileMgr.java,v 1.2 2004/03/12 23:09:16 jim Exp $

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
    pNodeLocks = new HashMap<String,ReentrantReadWriteLock>();
    pWorkLocks = new HashMap<NodeID,Object>();

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
	    pCheckSum.refresh(work, 1024);
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
	  assert(false);
	  return new FailureRsp
	    (task, 
	     "INTERNAL ERROR: No attempt to compute file states should ever be made when " + 
	     "the VersionState for a working version is CheckedIn!", 
	     wait, start);
	  
	case Identical:
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
		  pCheckSum.refresh(wpath, 1024);
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
		    pCheckSum.refresh(wpath, 1024);
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
			  pCheckSum.refresh(wpath, 1024);
			workEqBase = pCheckSum.compare(wpath, lpath);
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
   * The per-node locks indexed by fully resolved node name.  
   */
  private HashMap<String,ReentrantReadWriteLock>  pNodeLocks;

  /**
   * The per-working version locks indexed by NodeID.
   */
  private HashMap<NodeID,Object>  pWorkLocks;
  
 
  /**
   * The root production directory.
   */ 
  private File  pProdDir;

  /**
   * The checksum generator. 
   */ 
  private CheckSum  pCheckSum; 
}

