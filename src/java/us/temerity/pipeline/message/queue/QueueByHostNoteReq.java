// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.message.PrivilegedReq;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   B Y   H O S T   N O T E   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request which requires the host name and timestamp of a server note.
 */
public 
class QueueByHostNoteReq
  extends PrivilegedReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hname
   *   The hostname. 
   *
   * @param stamp
   *   The timestamp of when the note was written.      
   */
  public
  QueueByHostNoteReq
  (
   String hname, 
   long stamp
  )
  {
    if(hname == null) 
      throw new IllegalArgumentException("The hostname cannot be (null)!");
    pHostName = hname;

    pStamp = stamp; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the hostname. 
   */
  public String
  getHostName()
  {
    return pHostName;
  }
                                                                                         
  /**
   * Get the timestamp of when the note was written. 
   */ 
  public long
  getStamp()
  {
    return pStamp; 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -5348549755033187915L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hostname. 
   */
  private String pHostName;

  /**
   * The timestamp of when the note was written.
   */ 
  private long pStamp; 
}
