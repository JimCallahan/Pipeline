// $Id: NodeGetEventsRsp.java,v 1.4 2009/08/19 22:48:06 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 
import us.temerity.pipeline.event.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T H U M B N A I L   C H E C K S U M   R S P                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to looking up a thumbnail's checksum.
 */
public
class ThumbnailCheckSumRsp
  extends TimedRsp
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new response.
   * 
   * @param timer 
   *   The timing statistics for a task.
   * 
   * @param bytes
   *   The checksum bytes.
   */
  public
  ThumbnailCheckSumRsp
  (
   TaskTimer timer, 
   byte[] bytes
  )
  { 
    super(timer);

    if(bytes == null) 
      throw new IllegalArgumentException("The checksum bytes cannot be (null)!");
    pBytes = bytes;

    LogMgr.getInstance().logAndFlush
      (LogMgr.Kind.Net, LogMgr.Level.Finest,
       getTimer().toString());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Get the checksum bytes.
   */
  public byte[] 
  getBytes()
  {
    return pBytes; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7029201786570772369L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The checksum bytes.
   */
  private byte[]  pBytes; 

}
  
