// $Id: DNotify.java,v 1.1 2004/04/01 03:10:27 jim Exp $

package us.temerity.pipeline.core;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

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
 * There is also an OS limitation on the number of directories which can be monitored by
 * any given thread.  Typically this limit is (1024), so its not very likely to be encountered
 * under normal usage.  The limit can be queried by the {@link #getLimit getLimit} method.  
 * The count of currently monitored directories can be obtained with 
 * {@link #getNumMonitored getNumMonitored}.  If the directory limit is exceeded, the 
 * <CODE>watch</CODE> method will throw an exception.
 */
public
class DNotify
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct a new directory monitor.
   */ 
  public 
  DNotify() 
    throws IOException     
  {
    pThread = Thread.currentThread();

    pLock      = new Object();
    pMonitor   = new HashSet<File>();
    pUnmonitor = new HashSet<File>();
    pDirToDesc = new HashMap<File,Integer>();
    pDescToDir = new HashMap<Integer,File>();

    pMaxDirs = initNative();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the maximum number of directories which may be monitored by a thread.
   */
  public int
  getLimit()
  {
    synchronized(pLock) {
      if(pMinDesc != null)
	return (pMaxDirs - pMinDesc);
      return pMaxDirs;
    }
  }

  /**
   * Get the number of directories currently being monitored.
   */ 
  public int 
  getNumMonitored() 
  {
    synchronized(pLock) {
      return pDirToDesc.size();
    }    
  }


  /**
   * Begin monitoring the given directory.
   * 
   * @param dir
   *   The directory to monitor.
   */ 
  public void 
  monitor
  (
   File dir  
  ) 
    throws IOException 
  {
    File canon = dir.getCanonicalFile();
    if(!canon.isDirectory()) 
      throw new IOException("Path to monitor was NOT a directory: " + canon);
    
    synchronized(pLock) {
      pMonitor.add(dir);
    }
  }

  /**
   * Cease monitoring the given directory.
   * 
   * @param dir
   *   The directory to quit monitoring.
   */ 
  public void 
  unmonitor
  (
   File dir 
  ) 
    throws IOException 
  {
    File canon = dir.getCanonicalFile();
    
    synchronized(pLock) {
      pUnmonitor.add(dir);
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Wait for one of the monitored directories to be modified. <P> 
   *
   * This method also applies the changes to the set of monitored directories requested by 
   * the {@link #monitor monitor} and {@link #unmonitor unmonitor} methods.  The 
   * <CODE>timeout</CODE> argument controls the maximum delay between requesting a change
   * via the <CODE>monitor</CODE> and <CODE>unmonitor</CODE> methods and the application 
   * of that change.  Note that the change may actually be applied much sooner if there is
   * file system activity. <P> 
   * 
   * Due to details with the way the OS supports directory change notification, it is 
   * crucial that this method be called from the same thread which instantiated the 
   * <CODE>DNotify</CODE> instance.  
   * 
   * @param timeout
   *   The maximum number of milliseconds before applying changes to the monitored 
   *   directory list.
   * 
   * @return
   *   The modified directory. 
   */ 
  public synchronized File 
  watch
  (
   int timeout
  ) 
    throws IOException 
  {
    if(!pThread.equals(Thread.currentThread())) 
      throw new IllegalStateException
	("This method must be called from the same thread which instantiated the " + 
	 "DNotify instance!");

    while(true) {
      synchronized(pLock) {
	if(!pMonitor.isEmpty() || !pUnmonitor.isEmpty()) {
	  for(File dir : pMonitor) {
	    if(!pDirToDesc.containsKey(dir)) {
	      if(pDirToDesc.size() >= getLimit())
		throw new IOException
		  ("Maximum number of directories (" + pMaxDirs + ") reached!");

	      int fd = monitorNative(dir.getPath());
	      
	      Logs.ops.fine("Monitoring Directory: [" + fd + "] " + dir);
	    
	      pDirToDesc.put(dir, fd);
	      pDescToDir.put(fd, dir);

	      if(pMinDesc == null) 
		pMinDesc = new Integer(fd);
	      else 
		pMinDesc = Math.min(pMinDesc, fd);		
	    }
	  }
	  pMonitor.clear();
	  
	  for(File dir : pUnmonitor) {
	    if(pDirToDesc.containsKey(dir)) {
	      
	      int fd = pDirToDesc.get(dir);
	      unmonitorNative(fd);

	      Logs.ops.fine("Unmonitoring Directory: [" + fd + "] " + dir);
	      
	      pDirToDesc.remove(dir);
	      pDescToDir.remove(fd); 

	      if(pDirToDesc.isEmpty()) 
		pMinDesc = null;
	    }
	  }
	  pUnmonitor.clear();
	  
	  assert(pDirToDesc.size() == pDescToDir.size());
	  
	  if(!pDirToDesc.isEmpty()) {
	    StringBuffer buf = new StringBuffer();
	    buf.append("Monitored Directories:\n");
	    for(File file : pDirToDesc.keySet()) 
	      buf.append("  [" + pDirToDesc.get(file) + "] " + file + "\n");
	    Logs.ops.finest(buf.toString());
	    Logs.flush();
	  }
	}
      }

      /* wait for a modification signal */ 
      int fd = watchNative(timeout);

      /* lookup the name of the modified directory */ 
      if(fd > 0) {
	synchronized(pLock) {
	  File dir = pDescToDir.get(fd);
	  if(dir == null) {
	    throw new IOException
	      ("Couldn't determine the directory name for the file descriptor (" + fd + ")!");
	  }

	  Logs.ops.fine("Directory Modified: [" + fd + "] " + dir);
	  Logs.flush();
      
	  return dir; 
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
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
   * The lock which protects access to all fields.
   */
  private Object pLock;

  /**
   * The set of directories to add to the monitored tables.
   */ 
  private HashSet<File>  pMonitor;

  /**
   * The set of directories to remove from the monitored tables.
   */ 
  private HashSet<File>  pUnmonitor;

  /**
   * The minimum numbered file descriptor or <CODE>null</CODE> if no file descriptors 
   * are being monitored.
   */
  private Integer pMinDesc;

  /**
   * The maximum number of directories that can be monitored. <P>
   */
  private int pMaxDirs;

  /**
   * The set of monitored directory file descriptors indexed by canonical directory name.
   */ 
  private HashMap<File,Integer>  pDirToDesc;

  /**
   * The set of monitored canonical directory names indexed by directory file descriptor.
   */ 
  private HashMap<Integer,File>  pDescToDir;
  
}
