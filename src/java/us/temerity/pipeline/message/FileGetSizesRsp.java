// $Id: FileGetSizesRsp.java,v 1.1 2004/11/16 03:56:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.Level;

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
   *   The total version file sizes indexed by fully resolved node name and 
   *   revision number.
   */
  public
  FileGetSizesRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,Long>> sizes
  )
  { 
    super(timer);

    if(sizes == null) 
      throw new IllegalArgumentException("The file sizes cannot be (null)!");
    pSizes = sizes;

    Logs.net.finest("FileMgr.getSizes(): \n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the total version file sizes indexed by fully resolved node name and 
   * revision number.
   */
  public TreeMap<String,TreeMap<VersionID,Long>>
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
   * The total version file sizes indexed by fully resolved node name and 
   * revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Long>>  pSizes; 


}
  
