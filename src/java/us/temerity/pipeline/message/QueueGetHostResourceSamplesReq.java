// $Id: QueueGetHostResourceSamplesReq.java,v 1.1 2004/08/01 15:48:53 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   G E T   H O S T   R E S O U R C E   S A M P L E S   R E Q                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the full system resource usage history of the given host.
 */
public 
class QueueGetHostResourceSamplesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hostname
   *   The fully resolved name of the host.
   */
  public
  QueueGetHostResourceSamplesReq
  (
   String hostname
  )
  { 
    if(hostname == null) 
      throw new IllegalArgumentException
	("The hostname cannot be (null)!");
    pHostname = hostname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved name of the host.
   */
  public String
  getHostname() 
  {
    return pHostname; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1874346801984531952L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved name of the host.
   */ 
  private String  pHostname; 

}
  
