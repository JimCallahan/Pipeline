// $Id: MiscGetSizesRsp.java,v 1.2 2005/01/22 01:36:36 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   S I Z E S   R S P                                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a {@link MiscGetSizesReq MiscGetSizesReq} request.
 */
public
class MiscGetSizesRsp
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
  MiscGetSizesRsp
  (
   TaskTimer timer, 
   TreeMap<String,TreeMap<VersionID,Long>> sizes
  )
  { 
    super(timer);

    if(sizes == null) 
      throw new IllegalArgumentException("The file sizes cannot be (null)!");
    pSizes = sizes;

    LogMgr.getInstance().log
(LogMgr.Kind.Net, LogMgr.Level.Finest,
"MasterMgr.getSizes(): \n  " + getTimer());
    if(LogMgr.getInstance().isLoggable(LogMgr.Kind.Net, LogMgr.Level.Finest))
      LogMgr.getInstance().flush();
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

  private static final long serialVersionUID = 4190099445440562218L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The total version file sizes indexed by fully resolved node name and 
   * revision number.
   */ 
  private TreeMap<String,TreeMap<VersionID,Long>>  pSizes; 


}
  
