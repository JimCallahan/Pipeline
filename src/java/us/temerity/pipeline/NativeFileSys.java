// $Id: NativeFileSys.java,v 1.10 2006/09/26 19:32:38 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
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
   * Only limited support for this operation is provided by the Windows operating system which
   * only supports a single Read and/or Write state.  Windows Read mode will be set if any 
   * of the User, Group or Other Read bits are set in the <CODE>mode</CODE> argument.  
   * Similarly, the Windows Write mode will be set if any of the Write mode bits are set.
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

    loadLibrary();
    chmodNative(mode, file.getPath());
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

  /** 
   * Returns the newest of change and modification time for the given file. <P> 
   * 
   * The change time (ctime) changed by writing or by setting inode information 
   * (owner, group, link count, mode, etc.).  The modification time (mtime) is changed by 
   * file modifications, e.g. by mknod(2), truncate(2), utime(2) and write(2) (of more than 
   * zero bytes).  This method will return the newer (larger) of the time stamps, measured in 
   * milliseconds since the epoch (00:00:00 GMT, January 1, 1970) or OL if the file does
   * not exist. <P> 
   * 
   * This method is required because there is no access to the change time from the 
   * {@link File} class which only provides {@link #File.lastModified} to retrieve the 
   * modification time for a file.  
   * 
   * @param path 
   *   The file/directory to test.
   * 
   * @return
   *   The newest of change and modification time or OL if non-existant.
   */
  public static long
  lastChanged
  (
   File path
  ) 
  {
    loadLibrary();

    try { 
      return (lastChangedNative(path.getPath()) * 1000L);
    }
    catch(IOException ex) {
      LogMgr.getInstance().log
	(LogMgr.Kind.Ops, LogMgr.Level.Warning,
	 "NativeFileSys.lastChanged(): " + ex.getMessage()); 
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
   * Returns the newest of change and modification time for the given file. <P> 
   * 
   * @param path 
   *   The file/directory to test.
   * 
   * @return
   *   The newest of change and modification time or OL if non-existant.
   * 
   * @throws IOException 
   *   If unable to determine the status of an existing file or some other I/O problem was 
   *   encountered.
   */
  public static native long
  lastChangedNative
  (
   String path
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
