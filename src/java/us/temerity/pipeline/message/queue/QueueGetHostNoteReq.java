// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   N O T E   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the note (if any) associated with the given host and timestamp.  
 */
public 
class QueueGetHostNoteReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param name
   *   The hostname. 
   *
   * @param stamp
   *   The timestamp of when the note was written.      
   */
  public
  QueueGetHostNoteReq
  (
   String name, 
   long stamp
  )
  {
    if(name == null) 
      throw new IllegalArgumentException("The hostname cannot be (null)!");
    pName = name;

    pStamp = stamp; 
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the hostname. 
   */
  public String
  getName()
  {
    return pName;
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
  private String pName;

  /**
   * The timestamp of when the note was written.
   */ 
  private long pStamp; 
}
