// $Id: DNotify.java,v 1.6 2004/07/14 20:48:29 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.logging.Level;

/*------------------------------------------------------------------------------------------*/
/*   D N O T I F Y                                                                          */
/*------------------------------------------------------------------------------------------*/

/**                                                                                         
 * Directory change notification. <P> 
 * 
 * This class monitors a set of local directories for file creation, modification, removal 
 * and renaming events which occur within the watched directories. <P> 
 * 
 * Due to details with the way the OS supports directory change notification, it is 
 * crucial that the {@link #watch watch} method be called from the same thread which 
 * instantiates the <CODE>DNotify</CODE> instance.  All other methods may be called with
 * safety from any thread. <P>
 * 
 * Due to the OS limit on the number of directories which can be monitored by a process, 
 * this class needs to be run by root in order to monitor more than (1024) directories.
 * When constructed, <CODE>DNotify</CODE> will attempt to raise the number of directories
 * which can be monitored up to (65536).  This may not succeed if not run as root. If the 
 * directory limit is exceeded, the <CODE>watch</CODE> method will throw an 
 * <CODE>IOException</CODE>.
 */
public
class DNotify
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new directory monitor.
   * 
   * @param dir     
   *   The root production directory.
   */ 
  public 
  DNotify
  (
   File dir
  ) 
    throws IOException     
  {
    pThread  = Thread.currentThread();
    pProdDir = dir;

    pLock        = new Object();
    pMonitor     = new TreeSet<File>();
    pUnmonitor   = new TreeSet<File>();
    pDirToEntry  = new TreeMap<File,DirEntry>();
    pDescToEntry = new TreeMap<Integer,DirEntry>();

    pMaxDesc = initNative();

    Logs.ops.finest("Maximum Directories: " + pMaxDesc);
    Logs.flush();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the name of the root production directory.
   */
  public File
  getRoot() 
  {
    return pProdDir;
  }

  /**
   * Get the maximum number of directories which may be monitored by a thread.
   */
  public int
  getLimit()
  {
    return pMaxDesc;
  }

  /**
   * Get the number of currently monitored directories.
   */ 
  public int
  getNumMonitored() 
  {
    synchronized(pLock) {
      return pDescToEntry.size();
    }
  }

  /**
   * Get the names of the currently monitored directories relative to the root 
   * production directory.
   */ 
  public TreeSet<File>
  getMonitored() 
  {
    synchronized(pLock) {
      return new TreeSet(pDirToEntry.keySet());
    }
  }
    

  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Begin monitoring the given directory.
   * 
   * @param dir
   *   The name of the directory to begin monitoring.
   */ 
  public void
  monitor
  (
   File dir  
  ) 
    throws IOException 
  {
    if(!dir.isAbsolute()) 
      throw new IOException
	("The directory to monitor (" + dir + ") must be specified as an absolute path " + 
	 "stripped of the root production directory (" + pProdDir + ") prefix!");

    synchronized(pLock) {
      if((pMonitor.size() + pDescToEntry.size() + 1) > pMaxDesc) 
	throw new IOException("Directory limit (" + pMaxDesc + ") reached!");
      pMonitor.add(dir);
    }
  }

  /**
   * Cease monitoring the given directory.
   * 
   * @param dir
   *   The name of the director to cease monitoring.
   */
  public void 
  unmonitor
  (
   File dir 
  ) 
    throws IOException 
  {
    if(!dir.isAbsolute()) 
      throw new IOException
	("The directory to unmonitor (" + dir + ") must be specified as an absolute path " + 
	 "stripped of the root production directory (" + pProdDir + ") prefix!");

    synchronized(pLock) {
      pUnmonitor.add(dir);
    }
  }

  /** 
   * Wait for the specified amount of time for one or more of the monitored directories to 
   * be created, removed or files located in one of the monitored directories to be 
   * created, modified, removed, or renamed. <P> 
   *
   * Before waiting, this method also applies the changes to the set of monitored 
   * directories requested by the {@link #monitor monitor} and {@link #unmonitor unmonitor} 
   * methods. <P> 
   * 
   * In addition, all existing parent directories of the set of monitored directories up to 
   * the root production directory are also monitored for changes. Changes to one of these
   * parent directories will not be reported unless the directory is also a member of the 
   * set of monitored directories or if the change which occured is the creation or removal 
   * of one the monitored set of directories. <P> 
   * 
   * If no directory changes occured for one of the monitored directories within the 
   * <CODE>timeout</CODE> interval, the returned set will be empty.  In not empty, the 
   * returned set will contain the absolute paths of the changed directories stripped of 
   * the root production directory prefix. <P> 
   * 
   * Due to details with the way the OS supports directory change notification, it is 
   * crucial that this method be called from the same thread which instantiated the 
   * <CODE>DNotify</CODE> instance.  An <CODE>IOException</CODE> will be thrown if this
   * methods is called from a different thread than the one which created the instance.
   * 
   * @param timeout
   *   The maximum number of milliseconds to wait for directory changes.
   * 
   * @return
   *   The set of monitored directories for which a change occured during the 
   *   <CODE>timeout</CODE> interval. 
   */ 
  public synchronized TreeSet<File>
  watch
  (
   int timeout
  ) 
    throws IOException 
  {
    if(!pThread.equals(Thread.currentThread())) 
      throw new IllegalStateException
	("This method must be called from the same thread (" + pThread.getName() + ") " + 
	 "which instantiated the DNotify instance!");

    boolean changed = false;
    synchronized(pLock) {
      if(!pMonitor.isEmpty() || !pUnmonitor.isEmpty()) {
	changed = true;

	/* monitor the root production directory */ 
	if(pRoot == null) {
	  if(!pProdDir.isDirectory()) 
	    throw new IOException
	      ("The root production directory (" + pProdDir + ") does not exist!");
	  
	  int fd = monitorNative(pProdDir.getPath());
	  if(fd == -1) 
	    throw new IOException
	      ("Unable to monitor root production directory (" + pProdDir + ")!");

	  pRoot = new DirEntry(new File("/"), fd, false);
	  pDescToEntry.put(fd, pRoot);
	}


	/* begin monitoring directories and their parents */ 
	for(File dir : pMonitor) {
	  if(!pDirToEntry.containsKey(dir)) {
	    if(pDescToEntry.size() >= pMaxDesc)
	      throw new IOException("Directory limit (" + pMaxDesc + ") reached!");

	    String comps[] = dir.getPath().split("/");
	    DirEntry parent = pRoot;
	    int wk;
	    for(wk=1; wk<comps.length; wk++) {
	      DirEntry child = parent.uChildren.get(comps[wk]);
	      if((child == null) || (child.uFileDesc == null)) {
		File pdir = new File(parent.uDir, comps[wk]);
		
		Integer desc = null;
		File path = new File(pProdDir + pdir.getPath());
		if(path.isDirectory()) {
		  int fd = monitorNative(path.getPath());
		  if(fd != -1) 
		    desc = fd;
		}
		
		boolean leaf = (wk == (comps.length-1));
		if(child == null) {
		  child = new DirEntry(pdir, desc, leaf);
		  parent.uChildren.put(comps[wk], child);
		}
		else {
		  child.uFileDesc = desc;
		  if(leaf) 
		    child.uNotify = true;
		}

		if(desc != null) 
		  pDescToEntry.put(desc, child);
		
		if(leaf) {
		  Logs.ops.fine("Monitoring Directory: " + dir);
		  Logs.flush();
		  
		  pDirToEntry.put(dir, child);
		}
	      }
	      
	      parent = child;
	    }
	  }
	}
	pMonitor.clear();
	

	/* cease monitoring directories and possibly their parents */ 
	for(File dir : pUnmonitor) {
	  Stack<DirEntry> ents = new Stack<DirEntry>();
	  {
	    ents.push(pRoot);
	    String comps[] = dir.getPath().split("/");
	    DirEntry parent = pRoot;
	    int wk;
	    for(wk=1; wk<comps.length; wk++) {
	      DirEntry child = parent.uChildren.get(comps[wk]);
	      if(child == null) 
		break;
	      
	      ents.push(child);
	      parent = child;
	    }
	  }

	  DirEntry leaf = ents.peek();
	  if(leaf.uDir.equals(dir)) {
	    leaf.uNotify = false;
	    pDirToEntry.remove(dir);

	    Logs.ops.fine("Unmonitoring Directory: " + dir);
	    Logs.flush();

	    while(true) {
	      DirEntry de = ents.pop();
	      if(ents.empty() || !de.uChildren.isEmpty()) 
		break;
	      
	      if(de.uFileDesc != null) {
		unmonitorNative(de.uFileDesc);
		pDescToEntry.remove(de.uFileDesc);
	      }

	      DirEntry parent = ents.peek();
	      parent.uChildren.remove(de.uDir.getName());	      
	    }
	  }
	}
	pUnmonitor.clear();
      }
    }
    

    /* wait for a modification signal */ 
    int fd = watchNative(timeout);
    TreeSet<File> modified = new TreeSet<File>();
    if(fd > 0) {
      synchronized(pLock) {
	DirEntry de = pDescToEntry.get(fd);
	if(de != null) {
	  if(de.uNotify) {
	    modified.add(de.uDir);

	    Logs.ops.finest("Directory Modified (native): " + de.uDir);
	    Logs.flush();
	  }
	  
	  for(DirEntry child : de.uChildren.values())
	    changed |= recheckSubdirs(child, modified);
	}
      }
    }


    /* debugging */ 
    if(changed) {
      synchronized(pLock) {
	logInternals();
      }
    }

    return modified;
  }


  /**
   * Recheck the monitored subdirectories of the just modified directory. <P> 
   *
   * Makes sure that any created subdirectories will be monitored and any destroyed 
   * subdirectories will no longer be monitored.
   */ 
  private boolean
  recheckSubdirs
  (
   DirEntry de, 
   TreeSet<File> modified
  ) 
  {
    boolean changed = false;
    try {
      File path = new File(pProdDir + de.uDir.getPath());
      if(path.isDirectory()) {
	if(de.uFileDesc == null) {
	  int cfd = monitorNative(path.getPath());
	  if(cfd != -1) {	      
	    de.uFileDesc = cfd;
	    pDescToEntry.put(cfd, de);
	  }
	  changed = true;
	}
      }
      else {
	if(de.uFileDesc != null) {
	  unmonitorNative(de.uFileDesc);
	  pDescToEntry.remove(de.uFileDesc);
	  de.uFileDesc = null;
	  changed = true;
	}
      }
      
      if(changed && (de.uNotify)) {
	modified.add(de.uDir);
	
	Logs.ops.fine("Directory Modified: " + de.uDir);
	Logs.flush();
      }
    }
    catch(IOException ex) {
      Logs.ops.severe(ex.getMessage());
    }

    for(DirEntry child : de.uChildren.values())
      changed |= recheckSubdirs(child, modified);

    return changed;
  }


  /**
   * Generate a long log message detailing the internal datastrutures of this instance.
   */ 
  private void 
  logInternals() 
  {
    if((pRoot == null) || !Logs.ops.isLoggable(Level.FINEST))
      return;

    StringBuffer buf = new StringBuffer();
    
    buf.append("DNotify Internals:\n" + 
	       "  Directory Tree:\n" + 
	       "    " + pProdDir + "\n");
    logTree(pRoot, 3, buf);
    buf.append("\n");
	  
    buf.append("  Monitored Directories:\n");
    for(File dir : pDirToEntry.keySet())
      buf.append("    " + dir + "\n");
    buf.append("\n");
    
    buf.append("  Open Descriptors:\n");
    for(Integer desc : pDescToEntry.keySet())
      buf.append("    [" + desc + "] " + (pDescToEntry.get(desc).uDir) + "\n");
    
    Logs.ops.finest(buf.toString());
    Logs.flush();
  }
  
  /**
   * Recursively traverse the directory entries generating the debugging log message text.
   */ 
  private void 
  logTree
  (
   DirEntry de, 
   int level, 
   StringBuffer buf
  ) 
  {
    int wk;
    for(wk=0; wk<level; wk++)
      buf.append("  ");

    buf.append(de.uDir);

    if(de.uFileDesc != null) 
      buf.append(" [" + de.uFileDesc + "]");
    
    if(de.uNotify) 
      buf.append(" NOTIFY");

    buf.append("\n");    

    for(DirEntry child : de.uChildren.values()) 
      logTree(child, level+1, buf);
  }

  


  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Initialize the signal handlers and determing resource limits.
   * 
   * @return
   *   The maximum number of directories that can be monitored.
   */ 
  private native int
  initNative() 
    throws IOException;


  /**
   * Begin monitoring the given directory.
   *
   * @param 
   *   The canonical directory name.
   *
   * @return 
   *   The file descriptor of the monitored directory. 
   */ 
  private native int 
  monitorNative
  (
   String dir  
  )
    throws IOException;
  

  /**
   * Cease monitoring the directory with the given file descriptor. 
   * 
   * @param 
   *   The file descriptor.
   */  
  private native void 
  unmonitorNative
  (
   int fd   
  ) 
    throws IOException;

  
  /**
   * Wait for one of the monitored directories to be modified. 
   * 
   * @param timeout
   *   The maximum number of milliseconds before applying changes to the monitored 
   *   directory list.
   * 
   * @return
   *   The file descriptor of the modified directory. 
   */ 
  private native int
  watchNative
  (
   int timeout
  ) 
    throws IOException;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * A tree-node datastructure used to represent a monitored directory.
   */ 
  private class
  DirEntry
  {
    public 
    DirEntry
    (
     File dir, 
     Integer fd, 
     boolean notify
    ) 
    {
      uDir      = dir;
      uFileDesc = fd;
      uNotify   = notify;

      uChildren = new TreeMap<String,DirEntry>();
    }


    /** 
     * The absolute directory path stripped of the root production directory prefix.
     */ 
    public File  uDir;

    /**
     * The open file descriptor associated with the directory 
     * or <CODE>null</CODE> if the directory is not currently open.
     */ 
    public Integer  uFileDesc;

    /**
     * Should clients be notified when changes to the contents of the directory are 
     * detected?
     */ 
    public boolean  uNotify;    


    /**
     * The table of directory entries for directories which are the immeadiate children 
     * of this directory indexed by the relative name of the child directory.
     */ 
    public TreeMap<String,DirEntry>  uChildren;
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
  
  static {
    System.load(PackageInfo.sInstDir + "/lib/libDNotify.so");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The thread which instantiated this instance.
   */
  private Thread pThread;

  /**
   * The root production directory.
   */ 
  private File  pProdDir;


  /**
   * The lock which protects access to the following fields.
   */
  private Object pLock;


  /**
   * The set of directories to begin monitoring (if not already monitored). <P>
   * 
   * The names of the directories are relative to the root production directory.
   */ 
  private TreeSet<File>  pMonitor;

  /**
   * The set of directories to cease monitoring (if currently being monitored). <P>
   * 
   * The names of the directories are relative to the root production directory.
   */ 
  private TreeSet<File>  pUnmonitor;


  /**
   * The per-process limit on the maximum number of file descriptors.
   */
  private int pMaxDesc;

  /**
   * The root of the directory entry tree corresponding to the root production directory.
   */ 
  private DirEntry  pRoot;

  /**
   * The mapping of directory name to directory entry.
   */ 
  private TreeMap<File,DirEntry>  pDirToEntry;

  /**
   * The mapping of directory file descriptor to directory entry.
   */ 
  private TreeMap<Integer,DirEntry>  pDescToEntry;
  
}
