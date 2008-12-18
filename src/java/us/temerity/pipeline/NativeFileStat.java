// $Id: NativeFileStat.java,v 1.1 2008/12/18 00:46:24 jim Exp $

package us.temerity.pipeline;

import java.io.*; 
import java.math.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   F I L E   S T A T                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of low-level JNI based methods for querying file system metadata associated with 
 * a given file system path efficiently.<P> 
 * 
 * This class has two advantages over using the standard File class.  More detailed 
 * information is available using NativeFileStat than from File.  When several type of 
 * queries are required for a given file, this class is more efficient since it only 
 * performs a single "stat" system call while File duplicates these calls for each query.<P>
 * 
 * Only the Unix operating system is supported by this class.
 */
public
class NativeFileStat
  extends Native
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Obtain file status information for the given file path.
   * 
   * @param path
   *   The path to the file to query. 
   * 
   * @throws IOException
   *   If unable to perform the native file status operation successfully.  However, no
   *   exceptions will be thrown for invalid file paths however, only for failures of the 
   *   underlying system to perform status operations in general.
   */ 
  public 
  NativeFileStat
  (
   Path path
  )
    throws IOException
  {
    switch(PackageInfo.sOsType) {
    case Windows:
    case MacOS:
      throw new IllegalStateException
        ("Only the Unix operating system is supported at this time!");
    }
    
    loadLibrary();
    statNative(path.toOsString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Whether the given path specifies a valid file sytem entity. <P> 
   * 
   * Possible reasons for a file system path being invalid include: 
   * <DIV style="margin-left: 40px;">
   *   1. Search permission is denied for one of the directories in the path prefix of 
   *   path. <BR>
   *   2. Too many symbolic links encountered while traversing the path.<BR>
   *   3. File name too long.<BR>
   *   4. A component of the path path does not exist, or the path is an empty string.<BR>
   *   5. A component of the path is not a directory.<BR>
   *   6. Kernel is out of memory.<BR>
   * </DIV> 
   */ 
  public boolean
  isValid() 
  {
    return (pMode != 0); 
  }
  
  /**
   * Whether the given file status is associated with the same underlying file, possibly 
   * using an alternative file system path, as this file status.
   */ 
  public boolean 
  isAlias
  (
   NativeFileStat stat
  ) 
  {
    return (isValid() && stat.isValid() && (pINodeNumber == stat.pINodeNumber));
  }



  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   T Y P E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Whether the given path specifies a regular file.
   */ 
  public boolean
  isFile() 
  {
    return ((pMode & sIFMT) == sIFREG); 
  }
  
  /** 
   * Whether the given path specifies a directory.
   */ 
  public boolean
  isDirectory() 
  {
    return ((pMode & sIFMT) == sIFDIR);
  }
 

  /*----------------------------------------------------------------------------------------*/
  /*   F I L E   S I Z E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total file size in bytes.
   */ 
  public long
  fileSize() 
  {
    return pFileSize;
  }


  /*----------------------------------------------------------------------------------------*/
  /*   T I M E S T A M P S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The time of last file access in milliseconds since January 1, 1970. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2). <P> 
   */ 
  public long
  lastAccess() 
  {
    return pLastAccess;
  }

  /**
   * The time of last modification in milliseconds since January 1, 1970. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2). <P> 
   */ 
  public long
  lastModification() 
  {
    return pLastMod;
  }

  /**
   * The last change time in milliseconds since January 1, 1970. <P> 
   * 
   * The change time (ctime) is changed by writing or by setting inode information 
   * (owner, group, link count, mode, etc.). <P> 
   */ 
  public long
  lastChange() 
  {
    return pLastChange; 
  }

  /**
   * The newest of the last modification or change time in milliseconds since 
   * January 1, 1970. <P> 
   * 
   * The modification time (mtime) is changed by file modifications, e.g. by mknod(2), 
   * truncate(2), utime(2) and write(2). The change time (ctime) changed by writing or 
   * by setting inode information (owner, group, link count, mode, etc.). <P> 
   */ 
  public long
  lastModOrChange() 
  {
    return Math.max(pLastMod, pLastChange); 
  }

  /** 
   * The time of the last critical modification of the given file. <P> 
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
   * @param critical
   *   The last legitimate change time (ctime) of the file.
   */
  public long
  lastCriticalChange
  (
   long critical 
  ) 
  {
    return (pLastChange > critical) ? Math.max(pLastMod, pLastChange) : pLastMod;
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   N A T I V E   H E L P E R S                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Obtain file status information.<P> 
   */ 
  private native void
  statNative
  (
   String path
  )
    throws IOException;



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Useful file mode bitmask values.
   */ 
  private static final int sIFMT  = 0170000;  /* bit mask for the file type bit fields */
  private static final int sIFREG = 0100000;  /* regular file */
  private static final int sIFDIR = 0040000;  /* directory */   



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The INode number of the given file.  Because Java only supports signed long values and
   * the C-value is unsigned, this value will not be literally correct.  However, it will be
   * consistent for a given file and therefore can be used to compare whether two file 
   * system paths point to the same actual file.
   */ 
  private long pINodeNumber; 

  /**
   * The access mode and file type bitmask.  <p> 
   * 
   * If unset (0), then the supplied file system path was not valid.
   */ 
  private int pMode; 

  /**
   * The total file size in bytes.
   */ 
  private long pFileSize; 

  /**
   * The time of last access in milliseconds since January 1, 1970.
   */ 
  private long pLastAccess; 

  /**
   * The time of last modification in milliseconds since January 1, 1970.
   */ 
  private long pLastMod; 

  /**
   * The time of last modification in milliseconds since January 1, 1970.
   */
  private long pLastChange;

}
