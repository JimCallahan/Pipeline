// $Id: NativeFileSys.java,v 1.5 2004/07/28 19:12:03 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.util.concurrent.atomic.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   P R O C E S S                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for interacting with the file system. 
 */
public
class NativeFileSys
  extends Native
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private
  NativeFileSys()
  {}

 
  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Change file access permissions. <P> 
   * 
   * See the manpage for chmod(2) for details about the legal values for <CODE>mode</CODE>.
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

    chmodNative(mode, file.getPath());
  }

   
  /**
   * Set the file creation mask. <P> 
   * 
   * See the manpage for umask(2) for details about the legal values for <CODE>mask</CODE>.
   *
   * @param mask
   *   The file creation bitmask.
   */
  public static void 
  umask
  (
   int mask
  ) 
  {
    umaskNative(mask);
  }
  
  
  /** 
   * Create a symbolic link which points to the given file. <P> 
   * 
   * @param file 
   *   The relative or absolute path to the file pointed to by the symlink. 
   * 
   * @param link 
   *   The fully resolved path of the symlink to create.  
   * 
   * @throws IOException 
   *   If unable to create the symlink.
   */
  public static void 
  symlink
  (
   File file, 
   File link
  ) 
    throws IOException
  {
    if(!link.isAbsolute()) 
      throw new IOException
	("The link argument (" + link + ") must be an absolute path!");
    
    symlinkNative(file.getPath(), link.getPath());
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
    return new File(realpathNative(path.getPath()));
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
  public static native void 
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
  public static native long
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
  public static native long
  totalDiskSpaceNative
  (
   String path
  ) 
    throws IOException;
}
