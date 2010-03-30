// $Id: MiscGetPrivilegeDetailsRsp.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   G E T   P R I V I L E G E   D E T A I L S   R S P                            */
/*------------------------------------------------------------------------------------------*/

/**
 * Get the privileges granted to a specific user with respect to all other users.
 */
public
class MiscGetPrivilegeDetailsRsp
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
   * @param details
   *   The privileges of the given user.
   */ 
  public
  MiscGetPrivilegeDetailsRsp
  (
   TaskTimer timer, 
   PrivilegeDetails details
  )
  { 
    super(timer);

    if(details == null) 
      throw new IllegalArgumentException("The privilege details cannot be (null)!");
    pDetails = details;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the privileges of the given user.
   */
  public PrivilegeDetails
  getDetails() 
  {
    return pDetails;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3170742848896909636L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The privileges of the given user.
   */ 
  private PrivilegeDetails  pDetails;

}
  
