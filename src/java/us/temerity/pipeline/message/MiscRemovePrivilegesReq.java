// $Id: MiscRemovePrivilegesReq.java,v 1.1 2004/05/23 20:01:27 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   R E M O V E   P R I V I L E G E S   R E Q                                    */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to Remove the given user's privileged access status. <P> 
 * 
 * @see MasterMgr
 */
public
class MiscRemovePrivilegesReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request. <P> 
   * 
   * @param author
   *   The user to remove priviledges from.
   */
  public
  MiscRemovePrivilegesReq
  (
   String author
   )
  { 
    if(author == null) 
      throw new IllegalArgumentException
	("The author cannot be (null)!");
    pAuthor = author;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the user to remove priviledges from.
   */
  public String
  getAuthor() 
  {
    return pAuthor;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 4309763776266908757L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The user to remove priviledges from.
   */ 
  private String  pAuthor;

}
  
