// $Id: VersionID.java,v 1.18 2008/07/22 21:37:30 jesse Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N A T I V E   F I L E   I N F O                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A collection of useful information about an individual file associated with a working
 * version of a node. 
 */
public
class NativeFileInfo 
  implements Cloneable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */ 
  public 
  NativeFileInfo() 
  {}

  /**
   * Construct a new instance from component information.
   * 
   * @param size
   *   The size of the file in bytes as returned by
   *   {@link NativeFileStat#fileSize NativeFileStat.fileSize()}.
   * 
   * @param stamp 
   *   The last critical modification timestamp as returned by
   *   {@link NativeFileStat#lastCriticalChange NativeFileStat.lastCriticalChange()}
   * 
   * @parma isSymlink
   *   Whether the file is actually a symbolic link (presumably to the repository) as 
   *   returned by {@link NativeFileStat#isUnresolvedSymlink 
   *   NativeFileStat.isUnresolvedSymlink()}.
   */ 
  public 
  NativeFileInfo
  (
   long size, 
   long stamp, 
   boolean isSymlink
  ) 
  {
    pFileSize  = size; 
    pTimeStamp = stamp; 
    pIsSymlink = isSymlink;
  }

  /**
   * Construct a new instance from a file status and node's last critical change time.
   * 
   * @parma stat
   *   The native file status.
   * 
   * @param critical
   *   The last legitimate change time (ctime) of the file.
   */ 
  public 
  NativeFileInfo 
  (
   NativeFileStat stat, 
   long critical
  ) 
  {
    this(stat.fileSize(), stat.lastCriticalChange(critical), stat.isUnresolvedSymlink());
  }

  /**
   * Copy constructor.
   */ 
  public
  NativeFileInfo
  (
   NativeFileInfo info  
  ) 
  {
    pFileSize  = info.pFileSize; 
    pTimeStamp = info.pTimeStamp; 
    pIsSymlink = info.pIsSymlink;    
  }

    
 
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Get the size of the file in bytes as returned by {@link NativeFileStat#fileSize 
   * NativeFileStat.fileSize()}.
   */ 
  public long
  getFileSize()
  {
    return pFileSize; 
  }

  /** 
   * Get the last critical modification timestamp as returned by 
   * {@link NativeFileStat#lastCriticalChange NativeFileStat.lastCriticalChange()}
   */ 
  public long
  getTimeStamp()
  {
    return pTimeStamp; 
  }

  /** 
   * Set the last critical modification timestamp to suppress what would appear to be
   * newer file timestamps due to the creation of symbolic links when actually nothing
   * has changed.<P> 
   * 
   * This shouldn't ever need to be called from user code and only exists to enable the 
   * MasterMgr to modify timestamps without doing unecessary memory allocation.
   */ 
  public void 
  setTimeStamp
  (
   long stamp
  )
  {
    pTimeStamp = stamp;
  }

  /**
   * Whether the file is actually a symbolic link (presumably to the repository) as 
   * returned by {@link NativeFileStat#isUnresolvedSymlink 
   * NativeFileStat.isUnresolvedSymlink()}.
   */ 
  public boolean
  isSymlink() 
  {
    return pIsSymlink;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj
  )
  {
    if((obj != null) && (obj instanceof NativeFileInfo)) {
      NativeFileInfo info = (NativeFileInfo) obj;
      return ((pFileSize == info.pFileSize) && 
              (pTimeStamp == info.pTimeStamp) && 
              (pIsSymlink == info.pIsSymlink));
    }
    return false;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new NativeFileInfo(this); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    encoder.encode("FileSize", pFileSize);     
    encoder.encode("TimeStamp", pTimeStamp);     
    encoder.encode("IsSymlink", pIsSymlink); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    Long size = (Long) decoder.decode("FileSize");
    if(size == null) 
      throw new GlueException("The \"FileSize\" was missing!");
    pFileSize = size;

    Long stamp = (Long) decoder.decode("TimeStamp");
    if(stamp == null) 
      throw new GlueException("The \"TimeStamp\" was missing!");
    pTimeStamp = stamp;

    Boolean tf = (Boolean) decoder.decode("IsSymlik");
    if(tf == null) 
      throw new GlueException("The \"IsSymlik\" was missing!");
    pIsSymlink = tf;
  }


  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6158894826262908786L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The size of the file in bytes as returned by {@link NativeFileStat#fileSize 
   * NativeFileStat.fileSize()}.
   */
  private long  pFileSize; 

  /**
   * The last critical modification timestamp as returned by 
   * {@link NativeFileStat#lastCriticalChange NativeFileStat.lastCriticalChange()}
   */
  private long  pTimeStamp; 

  /**
   * The file is actually a symbolic link (presumably to the repository) as 
   * returned by {@link NativeFileStat#isUnresolvedSymlink 
   * NativeFileStat.isUnresolvedSymlink()}.
   */
  private boolean  pIsSymlink; 

}



