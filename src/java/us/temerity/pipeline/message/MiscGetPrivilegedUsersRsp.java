// $Id: MiscGetPrivilegedUsersRsp.java,v 1.3 2004/06/08 20:05:11 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;
import java.util.logging.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P R I V I L E G E D   U S E R S   R S P                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A successful response to a <CODE>MiscGetPrivilegedUsersReq</CODE> request.
 */
public
class MiscGetPrivilegedUsersRsp
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
   * @param users
   *   The names of privileged users.
   */ 
  public
  MiscGetPrivilegedUsersRsp
  (
   TaskTimer timer, 
   TreeSet<String> users
  )
  { 
    super(timer);

    if(users == null) 
      throw new IllegalArgumentException("The users cannot be (null)!");
    pUsers = users;

    Logs.net.finest("MasterMgr.getPrivilegedUsers():\n  " + getTimer());
    if(Logs.net.isLoggable(Level.FINEST))
      Logs.flush();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the names of the priviledged users.
   */
  public TreeSet<String>
  getUsers() 
  {
    return pUsers;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6613301169756629375L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the priviledged users.
   */ 
  private TreeSet<String>  pUsers;

}
  
