// $Id: MiscAuthorizeOnWindowsReq.java,v 1.2 2007/02/12 19:20:49 jim Exp $

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
   * @param domain
   *   The Windows domain.
   * 
   * @param password
   *   The encrypted Windows password.
   */
  public
  MiscAuthorizeOnWindowsReq
  (
   String domain,
   String password
  )
  {
    super();

    if(domain == null) 
      throw new IllegalArgumentException
	("The Windows domain cannot be (null)!");
    pDomain = domain; 

    if(password == null) 
      throw new IllegalArgumentException
	("The encrypted Windows password cannot be (null)!");
    pPassword = password; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the Windows domain.
   */ 
  public String
  getDomain() 
  {
    return pDomain; 
  }

  /**
   * Gets the encrypted Windows password.
   */ 
  public String
  getPassword() 
  {
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
   * The Windows domain. 
   */
  private String  pDomain; 

  /**
   * The encrypted Windows password.
   */
  private String  pPassword;  

}
  
