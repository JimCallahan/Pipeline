// $Id: MiscSetPrivilegedUsersReq.java,v 1.1 2004/07/25 03:13:17 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   S E T   P R I V I L E G E D   U S E R S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the names of the privileged users. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscSetPrivilegedUsersReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param users
   *    The names of the privileged users.
   */
  public
  MiscSetPrivilegedUsersReq
  (
   TreeSet<String> users
  ) 
  {
    if(users == null) 
      throw new IllegalArgumentException
	("The user names cannot be (null)!");
    pUsers = users;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the privileged users.
   */ 
  public TreeSet<String>
  getUsers() 
  {
    assert(pUsers != null);
    return pUsers;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4422585889192093145L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the privileged users.
   */
  private TreeSet<String>  pUsers;  

}
  
