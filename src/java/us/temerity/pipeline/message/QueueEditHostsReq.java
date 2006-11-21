// $Id: QueueEditHostsReq.java,v 1.6 2006/11/21 19:59:31 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   E D I T   H O S T S   R E Q                                                */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to change the editable properties of the given hosts. <P> 
 */
public
class QueueEditHostsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param changes
   *   The changes in host properties indexed by fully resolved names of the hosts.
   * 
   * @param hostnames
   *   The canonical names of the host from which the request was made.
   */
  public
  QueueEditHostsReq
  (
   TreeMap<String,QueueHostMod> changes, 
   TreeSet<String> hostnames
  )
  { 
    super();

    if(changes == null) 
      throw new IllegalArgumentException("The host property changes cannot be (null)!");
    pChanges = changes;

    if(hostnames == null) 
      throw new IllegalArgumentException("The local hostnames cannot be (null)!");
    pLocalHostnames = hostnames; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the changes in host properties indexed by fully resolved names of the hosts.
   */
  public TreeMap<String,QueueHostMod>
  getChanges() 
  {
    return pChanges;
  }

  /**
   * Gets the canonical names of the host from which the request was made.
   */ 
  public TreeSet<String> 
  getLocalHostnames()
  {
    return pLocalHostnames; 
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5116575566988831720L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The changes in host properties indexed by fully resolved names of the hosts.
   */ 
  private TreeMap<String,QueueHostMod>  pChanges; 

  /**
   * The canonical names of the host from which the request was made.
   */ 
  private TreeSet<String>  pLocalHostnames;
}
  
