// $Id: QueueRemoveHostsReq.java,v 1.1 2004/07/28 19:10:23 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   R E M O V E   H O S T S   R E Q                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to remove the given existing execution hosts from the Pipeline queue. <P> 
 */
public 
class QueueRemoveHostsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param hostname
   *   The fully resolved names of the hosts.
   */
  public
  QueueRemoveHostsReq
  (
   TreeSet<String> hostnames
  )
  { 
    if(hostnames == null) 
      throw new IllegalArgumentException
	("The hostnames cannot be (null)!");
    pHostnames = hostnames;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the fully resolved names of the hosts.
   */
  public TreeSet<String>
  getHostnames() 
  {
    return pHostnames; 
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6041394943809402008L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The fully resolved names of the hosts.
   */ 
  private TreeSet<String>  pHostnames; 

}
  
