// $Id: FileCheckInRsp.java,v 1.2 2009/09/21 23:21:45 jim Exp $

package us.temerity.pipeline.message.file;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   C H E C K - I N   R S P                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileCheckInReq FileCheckInReq} request.
 */
public
class FileCheckInRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param updatedCheckSums
   *   The updated cache of checksums for files associated with the working version.
   * 
   * @param movedStamps
   *   The timestamps recorded for files before being moved into the repository and the 
   *   symlink created after the move.
   * 
   * @param fileInfos
   *   Per-file information for each file sequence after the check-in takes place.
   */
  public
  FileCheckInRsp
  (
   TaskTimer timer, 
   CheckSumCache updatedCheckSums, 
   TreeMap<String,Long[]> movedStamps, 
   TreeMap<FileSeq,NativeFileInfo[]> fileInfos
  )
  { 
    super(timer);

    if(updatedCheckSums == null) 
      throw new IllegalArgumentException("The updated checksums cannot (null)!");
    pUpdatedCheckSums = updatedCheckSums; 

    if(movedStamps == null) 
      throw new IllegalArgumentException("The moved timestamps cannot (null)!");
    pMovedStamps = movedStamps; 

    if(fileInfos == null) 
      throw new IllegalArgumentException("The file information cannot (null)!");
    pFileInfos = fileInfos; 

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The updated cache of checksums for files associated with the working version.
   */ 
  public CheckSumCache
  getUpdatedCheckSums()
  {
    return pUpdatedCheckSums; 
  }
  
  /**
   * The timestamps recorded for files before being moved into the repository and the 
   * symlink created after the move.
   */ 
  public TreeMap<String,Long[]>
  getMovedStamps()
  {
    return pMovedStamps; 
  }
  
  /**
   * The per-file information for each file sequence after the check-in takes place.
   */ 
  public TreeMap<FileSeq,NativeFileInfo[]>
  getFileInfos()
  {
    return pFileInfos; 
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8653750598583802108L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The updated cache of checksums for files associated with the working version.
   */ 
  private CheckSumCache  pUpdatedCheckSums; 

  /**
   * The timestamps recorded for files before being moved into the repository and the 
   * symlink created after the move.
   */ 
  private TreeMap<String,Long[]>  pMovedStamps; 

  /**
   * The per-file information for each file sequence after the check-in takes place.
   */ 
  private TreeMap<FileSeq,NativeFileInfo[]> pFileInfos; 

}
  
