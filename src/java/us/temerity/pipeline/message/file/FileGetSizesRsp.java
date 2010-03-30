// $Id: FileGetSizesRsp.java,v 1.5 2009/11/02 03:44:11 jim Exp $

package us.temerity.pipeline.message.file;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.message.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   F I L E   G E T   S I Z E S   R S P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link FileGetSizesReq FileGetSizesReq} request.
 */
public
class FileGetSizesRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response. <P> 
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param sizes
   *   The total per-version file sizes indexed by revision number.
   */
  public
  FileGetSizesRsp
  (
   TaskTimer timer, 
   TreeMap<VersionID,Long> sizes
  )
  { 
    super(timer);

    if(sizes == null) 
      throw new IllegalArgumentException("The file sizes cannot be (null)!");
    pSizes = sizes;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       "FileMgr.getSizes(): \n  " + getTimer());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the total per-version file sizes indexed by revision number.
   */
  public TreeMap<VersionID,Long>
  getSizes() 
  {
    return pSizes; 
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 7010263554464739499L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total per-version file sizes indexed by revision number.
   */ 
  private TreeMap<VersionID,Long>  pSizes; 


}
  
