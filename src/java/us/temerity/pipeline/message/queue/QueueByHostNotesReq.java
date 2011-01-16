// $Id: QueueGetByNameReq.java,v 1.1 2009/12/09 05:05:55 jesse Exp $

package us.temerity.pipeline.message.queue;

import us.temerity.pipeline.message.PrivilegedReq;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   B Y   H O S T   N O T E S   R E Q                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A request which requires the host name for a set of server notes.
 */
public 
class QueueByHostNotesReq
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
   */
  public
  QueueByHostNotesReq
  (
   String hname
  )
  {
    if(hname == null) 
      throw new IllegalArgumentException("The hostname cannot be (null)!");
    pHostName = hname;
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

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5484835582397579002L;    

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The hostname. 
   */
  private String pHostName;

}
