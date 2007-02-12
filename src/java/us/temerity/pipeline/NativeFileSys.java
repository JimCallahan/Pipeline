// $Id: NativeFileSys.java,v 1.15 2007/02/12 19:17:47 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.*; 
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   F I L E   S Y S                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for interacting with the file system. 
 */
public
class NativeFileSys
  extends Native
{  
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change file access permissions. <P> 
   * 
   * See the manpage for chmod(2) for details about the legal values for <CODE>mode</CODE>.<P>
   * 
   * This method is not supported by the Windows operating system.
   *
   * @param mode 
   *   The access mode bitmask.
   *
   * @param file 
   *   The fully resolved path to the file to change.
   * 
   * @throws IOException 
   *   If unable to change the mode of the given file.
   */
  public static void 
  chmod
  (
   int mode, 
   File file
  ) 
    throws IOException
  {
    if(!file.isAbsolute()) 
      throw new IOException
	("The file argument (" + file + ") must be an absolute path!");

    switch(PackageInfo.sOsType) {
    case Unix: 
    case MacOS:
      loadLibrary();
      chmodNative(mode, file.getPath());
      break;

    case Windows:
      throw new IOException
	("Not supported on Windows systems!");
    }
  }

  /**
   * Set the file creation mask. <P> 
   * 
   * See the manpage for umask(2) for details about the legal values for <CODE>mask</CODE>.<P>
   * 
   * This method is not supported by the Windows operating system.
   *
   * @param mask
   *   The file creation bitmask.
   * 
   * @throws IOException
   *   If run under the Windows operating system.
   */
  public static void 
  umask
  (
   int mask
  ) 
    throws IOException
  {
    switch(PackageInfo.sOsType) {
    case Unix: 
    case MacOS:
      loadLibrary();
      umaskNative(mask);
      break;

    case Windows:
      throw new IOException
	("Not supported on Windows systems!");
    }
  }

  
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Create a symbolic link which points to the given file. <P> 
   * 
   * This method is not supported by the Windows operating system.
   * 
   * @param file 
   *   The relative or absolute path to the file pointed to by the symlink. 
   * 
   * @param link 
   *   The fully resolved path of the symlink to create.  
   * 
   * @throws IOException 
   *   If unable to create the symlink or is run under the Windows operating system.
   */
  public static void 
  symlink
  (
   File file, 
   File link
  ) 
    throws IOException
  {
    switch(PackageInfo.sOsType) {
    case Unix: 
    case MacOS:
      if(!link.isAbsolute()) 
	throw new IOException
	  ("The link argument (" + link + ") must be an absolute path!");
      loadLibrary();
      symlinkNative(file.getPath(), link.getPath());
      break;

    case Windows:
      throw new IOException
	("Not supported on Windows systems!");
    }
  }
  

  /*----------------------------------------------------------------------------------------*/

  /** 
   * Determine the canonicalized absolute pathname of the given path. <P> 
   * 
   * This method expands all symbolic links and resolves references to <CODE>'/./'</CODE>,
   * <CODE>'/../'</CODE> and extra </CODE>'/'</CODE> characters in the <CODE>path</CODE>
   * argument and returns the resulting canonicalized absolute file path. In other words, 
   * the resulting path will have no symbolic link, <CODE>'/./'</CODE> or 
   * <CODE>'/../'</CODE> components.
   * 
   * @param path 
   *   The file system path to resolve.
   * 
   * @return
   *   The resolved canonicalized absolute file system path.
   * 
   * @throws IOException 
   *   If the given path is illegal, file system access permissions made it impossible 
   *   to resolve the path or some other I/O problem was encountered.
   */
  public static File
  realpath
  (
   File path
  ) 
    throws IOException
  {
    loadLibrary();
    return new File(realpathNative(path.getPath()));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Returns the last modification time of the given file. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2). <P> 
   * 
   * @param path 
   *   The file to test.
   * 
   * @return
   *   The modification time or OL if non-existant.
   */ 
  public static long
  lastModification
  (
   File path
  ) 
  {
    loadLibrary();

    try { 
      return lastStamps(path.getPath(), -1L);
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning,
	 "NativeFileSys.lastModification(): " + ex.getMessage()); 
      return 0L;
    }
  }

  /**
   * Returns the newest of the last modification or change time of the given file. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2). The change time (ctime) changed by writing or 
   * by setting inode information (owner, group, link count, mode, etc.). <P> 
   * 
   * @param path 
   *   The file to test.
   * 
   * @return
   *   The modification/change time or OL if non-existant.
   */ 
  public static long
  lastModOrChange
  (
   File path
  ) 
  {
    loadLibrary();

    try { 
      return lastStamps(path.getPath(), -2L);
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning,
	 "NativeFileSys.lastModOrChange(): " + ex.getMessage()); 
      return 0L;
    }
  }

  /** 
   * Returns the time of the last critical modification of the given file. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2).The change time (ctime) changed by writing or 
   * by setting inode information (owner, group, link count, mode, etc.). <P> 
   * 
   * When a file on a Unix filesystem is copied from one location to another on a Windows 
   * workstation, the ctime is updated and the mtime is unchanged.  This is clearly 
   * incorrect, but presents problems in a mixed Unix/Windows environment for determining
   * when a file has been modified in the Unix mtime sense.  This method is an attempt to 
   * solve this issue. <P> 
   * 
   * A timestamp of the last time the ctime of the file should legitimately have been 
   * updated (critical) from Unix is passed to this method along with the file to test.  If 
   * this timestamp is older (less-than) than the files current ctime, then the newest of 
   * the ctime mtime is returned.  Otherwise, just the mtime is returned. <P> 
   * 
   * All times are measured in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
   * 
   * @param path 
   *   The file to test.
   * 
   * @param critical
   *   The last legitimate change time (ctime) of the file.
   * 
   * @return
   *   The modification time or OL if non-existant.
   */
  public static long
  lastCriticalChange
  (
   File path, 
   Date critical 
  ) 
  {
    loadLibrary();

    try { 
      return lastStamps(path.getPath(), critical.getTime());
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning,
	 "NativeFileSys.lastCriticalChange(): " + ex.getMessage()); 
      return 0L;
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Determine amount of free disk space available on the file system which contains the 
   * given path. 
   * 
   * @param path 
   *   The file/directory used to determine the file system.
   * 
   * @return
   *   The free disk space (in bytes). 
   * 
   * @throws IOException 
   *   If the given path is illegal or some other I/O problem was encountered.
   */
  public static long
  freeDiskSpace
  (
   File path
  ) 
    throws IOException
  {
    loadLibrary();
    return freeDiskSpaceNative(path.getPath());
  }

  /** 
   * Determine the total amount of disk space on the file system which contains the 
   * given path. 
   * 
   * @param path 
   *   The file/directory used to determine the file system.
   * 
   * @return
   *   The total disk space (in bytes). 
   * 
   * @throws IOException 
   *   If the given path is illegal or some other I/O problem was encountered.
   */
  public static long
  totalDiskSpace
  (
   File path
  ) 
    throws IOException
  {
    loadLibrary();
    return totalDiskSpaceNative(path.getPath());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E    H E L P E R S                                                         */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Change file access permissions. <P> 
   * 
   * @param mode 
   *   The access mode bitmask.
   *
   * @param file 
   *   The fully resolved path to the file to change.
   * 
   * @throws IOException 
   *   If unable to change the mode of the given file.
   */
  private static native void 
  chmodNative
  (
   int mode, 
   String file
  ) 
    throws IOException;

  /**
   * Set the file creation mask. <P> 
   * 
   * See the manpage for umask(2) for details about the legal values for <CODE>mask</CODE>.
   *
   * @param mask
   *   The file creation bitmask.
   */
  private static native void 
  umaskNative
  (
   int mask
  );

  /** 
   * Create a symbolic link which points to the given file. <P> 
   * 
   * @param file 
   *   The relative or absolute path to the file pointed to by the symlink. 
   * 
   * @param link 
   *   The fully resolved path of the symlink to create.
   */
  private static native void 
  symlinkNative
  (
   String file, 
   String link
  ) 
    throws IOException;

  /** 
   * Determine the canonicalized absolute pathname of the given path. <P> 
   * 
   * @param path 
   *   The file system path to resolve.
   * 
   * @return
   *   The resolved canonicalized absolute file system path.
   */
  private static native String
  realpathNative
  (
   String path
  ) 
    throws IOException;

  /** 
   * Returns various combinations of last change and last modification times for a file.
   * 
   * @param path 
   *   The file to test.
   * 
   * @param critical
   *   The last legitimate change time (ctime) of the file, (-1L) for mtime only or 
   *   (-2L) for newest of ctime/mtime.
   * 
   * @throws IOException 
   *   If unable to determine the status of an existing file or some other I/O problem was 
   *   encountered.
   */
  public static native long
  lastStamps
  (
   String path, 
   long critical
  ) 
    throws IOException; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Determine amount of free disk space available on the file system which contains the 
   * given path. 
   * 
   * @param path 
   *   The file/directory used to determine the file system.
   * 
   * @return
   *   The free disk space (in bytes). 
   */
  private static native long
  freeDiskSpaceNative
  (
   String path
  ) 
    throws IOException;

  /** 
   * Determine the total amount of disk space on the file system which contains the 
   * given path. 
   * 
   * @param path 
   *   The file/directory used to determine the file system.
   * 
   * @return
   *   The total disk space (in bytes).
   */
  private static native long
  totalDiskSpaceNative
  (
   String path
  ) 
    throws IOException;
}
