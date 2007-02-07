// $Id: MiscAuthorizeOnWindowsReq.java,v 1.1 2007/02/07 21:14:38 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   M I S C   A U T H O R I Z E   O N   W I N D O W S   R E Q                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A request to set the Windows password for a user.
 * 
 * @see MasterMgr
 */
public
class MiscAuthorizeOnWindowsReq
  extends PrivilegedReq
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   * 
   * @param password
   *   The encrypted Windows password.
   */
  public
  MiscAuthorizeOnWindowsReq
  (
   String password
  )
  {
    super();

    if(password == null) 
      throw new IllegalArgumentException
	("The encrypted Windows password cannot be (null)!");
    pPassword = password; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the encrypted Windows password.
   */ 
  public String
  getPassword() 
  {
    if(pPassword == null)
      throw new IllegalStateException(); 
    return pPassword;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1458682636382071648L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The encrypted Windows password.
   */
  private String  pPassword;  

}
  
