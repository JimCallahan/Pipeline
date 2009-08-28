// $Id: FileCheckInRsp.java,v 1.1 2009/08/28 02:10:47 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

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
   */
  public
  FileCheckInRsp
  (
   TaskTimer timer, 
   CheckSumCache updatedCheckSums
  )
  { 
    super(timer);

    if(updatedCheckSums == null) 
      throw new IllegalArgumentException("The updated checksums cannot (null)!");
    pUpdatedCheckSums = updatedCheckSums; 

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
  public CheckSumCache  pUpdatedCheckSums; 
  
}
  
