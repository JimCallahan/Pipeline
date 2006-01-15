// $Id: PrivilegedReq.java,v 1.1 2006/01/15 06:29:25 jim Exp $

package us.temerity.pipeline.message;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.core.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P R I V I L E G E D   R E Q                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The base class of all requests which require administrative privileges. 
 * 
 * @see MasterMgr
 */
public
class PrivilegedReq
  implements Serializable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R S                                                              */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Constructs a new request.
   */
  public
  PrivilegedReq() 
  {
    pRequestor = PackageInfo.sUser; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the name of user making the request.
   */ 
  public String
  getRequestor() 
  {
    return pRequestor; 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -5257552895120364689L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of user making the request.
   */
  private String  pRequestor; 

}
  
