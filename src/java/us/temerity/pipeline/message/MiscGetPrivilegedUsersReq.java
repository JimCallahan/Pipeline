// $Id: MiscGetPrivilegedUsersReq.java,v 1.1 2004/05/23 20:01:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P R I V I L E G E D   U S E R S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the set of privileged users.
 * 
 * @see MasterMgr
 */
public
class MiscGetPrivilegedUsersReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   */
  public
  MiscGetPrivilegedUsersReq()
  {}



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5421964582418887755L;

}
  
