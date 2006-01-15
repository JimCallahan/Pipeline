// $Id: MiscGetPrivilegeDetailsReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P R I V I L E G E   D E T A I L S   R E Q                            */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to get the privileges granted to a specific user with respect to all other users.
 */
public
class MiscGetPrivilegeDetailsReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param uname
   *   The unique name of the user.
   */
  public
  MiscGetPrivilegeDetailsReq
  (
   String uname
  )
  {
    if(uname == null) 
      throw new IllegalArgumentException
	("The user name cannot be (null)!");
    pUserName = uname;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the unique name of the user. 
   */ 
  public String
  getUserName() 
  {
    return pUserName;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6413285556932431377L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The unique name of the user.
   */
  private String  pUserName;  

}
  
